# Architecture

## Overview

DocSpace is a modular monolith: one Spring Boot backend exposing a REST API, one React SPA
frontend, backed by a single PostgreSQL database. There is no inter-service messaging, no
separate auth service, and no microservice boundaries — all backend logic lives in one
deployable JAR, organized into `config` / `controller` / `service` / `repository` / `entity` /
`dto` / `exception` / `security` packages for separation of concerns.

```
Browser (React SPA, :5173)
     │  HTTPS/JSON, credentials: include (session cookie)
     ▼
Spring Boot API (:8080)
     │  Spring Data JPA / Hibernate
     ▼
PostgreSQL (:5434 host / :5432 in-container)
```

## Database schema

Three tables, managed by Flyway (`backend/src/main/resources/db/migration`):

- **users** — `id (uuid pk)`, `name`, `email (unique)`, `password_hash (nullable)`, `created_at`
- **documents** — `id (uuid pk)`, `title`, `content (text, JSON-serialized TipTap doc)`,
  `owner_id (fk → users, indexed)`, `version (int, optimistic lock)`, `created_at`, `updated_at`
- **document_shares** — `id (uuid pk)`, `document_id (fk → documents, indexed)`,
  `shared_with_id (fk → users, indexed)`, `permission (VIEWER|EDITOR)`, `created_at`, with a
  **unique constraint on `(document_id, shared_with_id)`** to prevent duplicate shares at the
  database level (in addition to an application-level check that returns a clean 400 instead of
  a raw constraint-violation error).

`content` is stored as `TEXT` rather than native `jsonb`. This was a conscious simplification: it
keeps the schema portable between PostgreSQL (production/dev) and H2 (used for fast in-memory
integration tests), at the cost of not being able to query/index inside the document JSON from
SQL — which this application never needs to do, since documents are always read and written
whole.

## API design

REST, resource-oriented, JSON everywhere except file upload (`multipart/form-data`). Documents
expose both a summary shape (`DocumentSummaryResponse`, used in list views) and a detail shape
(`DocumentResponse`, includes full `content` and `version`). Every document response includes a
`role` field (`OWNER` / `EDITOR` / `VIEWER`) computed server-side for the *requesting user* — the
frontend never has to independently infer or trust a role, it just renders what the backend says
the caller is allowed to do.

Mutating document updates (`PUT /api/documents/{id}`) require the client to send back the
`version` it last read. This is both an optimistic-concurrency check and a natural fit for the
autosave UX (see "Concurrency" below).

## Authorization strategy

All authorization logic is centralized in `DocumentService.resolveAccess(documentId, userId)`,
which every document-touching code path (read, edit, rename, delete, share management) calls
before doing anything else. It returns a `DocumentAccess` record exposing `canRead()`,
`canEdit()`, `canManage()`:

- **Owner**: full access, including rename/delete/share management.
- **Editor** (has a `DocumentShare` row with `permission = EDITOR`): can read and edit content,
  cannot rename, delete, or manage sharing.
- **Viewer** (`permission = VIEWER`): read-only.
- **No relationship** (not owner, no share row): treated identically to "not found" — the
  service throws `NotFoundException`, not `ForbiddenException`. This is a deliberate anti-IDOR
  measure: a user probing random document IDs they have no access to gets a 404 either way,
  so the API never confirms "this document exists but you can't see it."

Identity is resolved from `CurrentUser.id()`, which reads the authenticated principal out of
Spring Security's `SecurityContext` — populated during login from a server-side lookup of the
user record, never from a request parameter or path variable supplied by the client. A path
variable like `{id}` only ever identifies *which document*; it never identifies *who is asking*.

Sharing management (`ShareController` / `ShareService`) reuses `resolveAccess()` rather than
re-implementing an ownership check, so there is exactly one place that decides "is this caller
the owner of this document."

## Authentication

Session-cookie based, no password, by design for this demo: the login screen lists the three
seeded accounts, and `POST /api/auth/login` takes just an email, looks up the matching `User` row
server-side, and establishes a `SecurityContext` backed by an `HttpSession`. The response is set
via Spring Security's `HttpSessionSecurityContextRepository`, and the browser is expected to send
the resulting session cookie (`withCredentials: true` on the frontend's Axios client) on every
subsequent request. The `password_hash` column exists on `User` but is unused — it was kept so
that real password-based login could be layered in later without a schema migration.

CSRF protection is disabled for the `/api/**` surface. This is a deliberate trade-off explained
in "Known limitations" below.

## Key engineering trade-offs

- **No password / real login** — chosen to keep the reviewer experience frictionless (pick a
  name, go) while still proving the more important property for this assignment: that the
  backend determines identity from its own session state, not from anything the client claims.
- **Content as JSON text, not `jsonb`** — portability across Postgres/H2 for tests, at the cost
  of in-database document queries (not needed here).
- **Optimistic locking via a `version` column**, not a CRDT or operational-transform engine —
  appropriate for a single-editor-at-a-time demo; see "Known limitations."
- **CSRF disabled** rather than implemented with a double-submit cookie — see below.
- **Markdown import is a light structural parser** (headings, bullet lists, paragraphs), not a
  full CommonMark implementation — sufficient for demonstrating import, not a general-purpose
  Markdown renderer.

## Known limitations

- **Concurrency model**: DocSpace does not support real-time concurrent co-editing (no
  operational transforms, no CRDTs, no WebSocket sync). Instead, each save carries the last-known
  `version`; if another save has landed in the meantime, the server returns `409 Conflict` with
  the latest document state, and the frontend surfaces "changed elsewhere" rather than silently
  overwriting. Two people editing the same document at the same time will see save conflicts
  rather than a merged result — acceptable for this scope, but a real product would need
  operational transforms or CRDTs (e.g. Yjs) for true concurrent editing.
- **Presence is polling, not push**: `PresenceService` is an in-memory, process-local
  `documentId → {userId → lastSeenInstant}` map (not persisted — presence has no meaning across a
  restart). The frontend heartbeats and re-polls every 5 seconds via `usePresence`. This shows
  "who else has this document open" but does **not** show live cursors, live text as others type,
  or push updates — a genuinely real-time version would need WebSockets and is out of scope here.
  Because the map is process-local, presence also resets to empty if the backend process restarts
  or if the app is ever scaled to multiple instances without a shared presence store.
- **Comments are document-level, not anchored to text**: `document_comments` has no reference to
  a text range or TipTap node position — a comment is a flat discussion thread on the whole
  document, not Google-Docs-style margin comments tied to a selection. Any collaborator with read
  access can comment; only the comment's author or the document owner can resolve or delete it.
  There is no "suggestion mode" (proposed edits accepted/rejected inline) in this pass.
- **Version history is view-only**: `document_versions` captures a full snapshot (title + content
  + version number + who saved it) immediately before every save overwrites the row in
  `documents`, so the full edit history is there and browsable — but there is no restore-to-this-
  version action yet, no diff view between two versions, just "look at what it used to say."
- **CSRF**: disabled on `/api/**`. The session cookie is the only ambient credential, CORS
  restricts which origins can even read API responses, and there is no cross-site form-submission
  attack surface in this demo (no third-party page can usefully POST to this API and read the
  result). A production deployment that serves the frontend from a different origin than a
  public-facing user base should reintroduce double-submit-cookie or synchronizer-token CSRF
  protection.
- **No password / MFA** — not a substitute for production authentication; see "Authentication"
  above for the rationale and the easy upgrade path (the schema already has a `password_hash`
  column).
- **Markdown import/export fidelity** — only headings (`#`, `##`), bullet lists (`-`/`*`),
  ordered lists, and paragraphs are recognized or produced; tables, links, code blocks, nested
  lists, and other CommonMark features import as plain paragraphs and are not emitted on export.
- **DOCX import fidelity** — headings are detected via paragraph style (`Heading1`/`Heading2`/
  `Title`) and list items via either real Word numbering (`numId`) or a "List"-named paragraph
  style (to also support generators like python-docx that don't set `numId`). Tables, images,
  footnotes, and text formatting (bold/italic runs) are not preserved — every run's plain text is
  concatenated into a single paragraph or list item.
- **PDF export fidelity** — renders the same simplified heading/paragraph/list structure as
  Markdown export, through a small internal HTML intermediate and `openhtmltopdf`; no custom
  fonts, page headers/footers, or print-specific styling beyond basic margins and font sizing.
- **No pagination** on document, share, comment, or version lists — fine at demo scale, would need
  to be added for users with large numbers of documents/collaborators/history.

## Future improvements

- Real-time collaborative editing (Yjs/CRDT-based, WebSocket push) instead of
  optimistic-lock-and-retry plus polling presence
- Full CommonMark-compliant Markdown import/export, and richer DOCX fidelity (tables, inline
  formatting, images)
- Password or OAuth-based login for non-demo deployments
- Text-anchored comments (margin comments tied to a selection) and a true suggestion/accept-reject
  mode
- Restore-to-version and version-to-version diffing for document history
- Pagination and search-on-the-server for large document sets
- Granular audit log of who changed what and when
