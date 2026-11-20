# Architecture

## Overview

One backend, one frontend, one database. Modular monolith — all the backend logic sits in one
Spring Boot jar, split into `config` / `controller` / `service` / `repository` / `entity` / `dto`
/ `exception` / `security` packages. No microservices, no separate auth service, nothing like
that — didn't need it.

```
Browser (React SPA)
     │  JSON over HTTPS, session cookie
     ▼
Spring Boot API
     │  Spring Data JPA / Hibernate
     ▼
PostgreSQL
```

## Database

Five tables, managed by Flyway migrations in `backend/src/main/resources/db/migration`:

- **users** — id, name, email (unique), password_hash (unused, see below), created_at
- **documents** — id, title, content (text, holds the TipTap JSON), owner_id, version (for
  optimistic locking), timestamps
- **document_shares** — links a document to a user with a permission (VIEWER/EDITOR), unique on
  (document_id, shared_with_id) so you can't share the same doc with the same person twice
- **document_comments** — document_id, author_id, body, resolved flag, created_at
- **document_versions** — a snapshot of a document's content/title/version, taken right before
  every save overwrites it, plus who saved it and when

`content` is stored as plain `TEXT`, not `jsonb`. Keeps it portable between Postgres (real use)
and H2 (fast test runs), and we never need to query inside the JSON from SQL anyway — docs get
read and written whole.

## API shape

REST, JSON everywhere except file upload (multipart) and export (binary). Every document response
includes a `role` field computed server-side for whoever's asking — frontend just renders what the
backend says, doesn't try to work out permissions on its own.

Saves (`PUT /api/documents/{id}`) carry a `version` number. If it doesn't match what's in the DB,
you get a 409 with the latest state instead of silently clobbering someone else's edit.

## Authorization

Everything funnels through one method: `DocumentService.resolveAccess(documentId, userId)`. Every
route that touches a document — read, edit, rename, delete, share, comment, export, whatever —
calls this first. It hands back a `DocumentAccess` with `canRead()` / `canEdit()` / `canManage()`.

- Owner: everything.
- Editor: read + edit content. No rename, no delete, no sharing.
- Viewer: read only.
- Nobody (not owner, no share): same as "not found" — throws a 404, not a 403. Point of this is so
  probing random document IDs you don't have access to never confirms the document even exists.

Identity comes from `CurrentUser.id()`, which reads off Spring Security's session — never from
anything the client sends in the URL or body. A path param like `{id}` only ever says *which*
document, never *who's asking*.

## Auth

Session cookie, no password. Login screen lists the three demo accounts, you pick one,
`POST /api/auth/login` takes just the email, looks up the user, sets up a session. That's it.
`password_hash` sits unused on the User table — kept it so real auth could be bolted on later
without a migration.

CSRF is off for `/api/**`. Session cookie's the only thing carrying auth, CORS already locks down
which origins can talk to the API, and there's no cross-site form-post attack surface here. If
this ever serves a public, cross-origin frontend for real, bring CSRF back.

## Trade-offs I made on purpose

- **No real auth** — frictionless for anyone testing this, and still proves the actual point:
  backend decides who you are, not the client.
- **Content as TEXT, not jsonb** — portability over in-DB querying I don't need.
- **Optimistic locking, not CRDT** — a version number and a 409 is enough for "don't overwrite
  each other," not enough for live co-editing. Said more below.
- **CSRF off** — see above.
- **Markdown/DOCX import+export cover headings, lists, paragraphs** — not full CommonMark, not
  full DOCX fidelity. Good enough to actually use, not meant to replace Word.

## What's not here yet

- **No real-time co-editing.** No CRDTs, no operational transforms, no WebSocket push. Saves just
  check the version number and 409 on conflict. Presence (who else has it open) is a 5-second
  poll, not live cursors — it tells you someone else is looking, not what they're typing.
- **Comments aren't anchored to text.** They're a flat thread on the whole document, not margin
  comments tied to a selection. No suggestion/accept-reject mode either.
- **Version history is view-only.** You can see what it used to say, can't restore to it yet, no
  diff between versions.
- **CSRF disabled** — see trade-offs above.
- **No password/MFA** — demo auth only.
- **Markdown/DOCX import skip tables, links, code blocks, images, inline formatting** — headings,
  lists, and paragraphs only.
- **No pagination anywhere** — fine for a demo, would need it for real scale.

## What I'd add next

- Real collaborative editing (Yjs or similar) with live cursors
- Full CommonMark support both ways, richer DOCX handling
- Real auth (password or OAuth) for anything beyond a demo
- Text-anchored comments, actual suggestion mode
- Restore-to-version, diffing between versions
- Pagination, server-side search
- An audit log of who changed what
