# AI Workflow

This document describes how AI assistance (Claude, via Claude Code) was used to build DocSpace,
in the interest of transparency about what was generated, what was verified, and what was
changed along the way.

## AI tools used

- **Claude Code** (Claude Sonnet 5) — used for the entire implementation: planning, writing
  backend and frontend code, running builds/tests, and driving a real browser to visually and
  functionally verify the app end-to-end.

## How AI accelerated implementation

- Scaffolded the full Maven project structure (`pom.xml`, package layout) and Vite/React/
  TypeScript/Tailwind frontend in minutes rather than hand-wiring build tooling.
- Generated the JPA entities, repositories, Flyway migrations, Spring Security session
  configuration, service-layer authorization logic, and REST controllers as a coherent set
  rather than isolated snippets — each layer was written with the others in mind (e.g. DTOs
  shaped around what the frontend actually needed to render).
- Generated the React pages/components (login, dashboard, editor, sharing modal, file upload)
  wired directly to real Axios service calls from the first draft, rather than against mock data.
- Wrote the JUnit/Mockito test suite covering the required scenarios in one pass, and the
  Vitest/React Testing Library frontend tests.
- Used an actual running Chrome-driven browser session to click through the login → dashboard →
  editor → share → logout flow as each of the three demo users, catching real bugs (see below)
  that static review would have missed.

## Prompts used

The driving prompt was a single, detailed engineering-assignment brief covering product
requirements, tech stack, project structure, security/testing/deployment expectations, and
explicit "don't do X" working rules (e.g. "never claim something works without testing it," "no
TODOs for mandatory features"). No significant sub-prompts were needed beyond that — the brief
was specific enough to drive implementation directly, with one clarifying question asked up front
(which authentication mechanism to use, given the brief said "lightweight" but didn't specify a
concrete scheme) before writing any code.

## AI-generated output that was modified or rejected

Three concrete bugs were found via actual execution (not just code reading) and fixed in place:

1. **`@Lob` on the `Document.content` field caused Hibernate to misread the `version` column.**
   The first version of the `Document` entity annotated `content` with `@Lob`. Running a real
   request against real PostgreSQL immediately threw `DataIntegrityViolationException: Bad value
   for type long`, because Hibernate's LOB handling interfered with how the adjacent `@Version`
   column was read back. Fixed by dropping `@Lob` and keeping the plain `@Column(columnDefinition
   = "TEXT")`, which is sufficient for a column that's always read/written whole.

2. **CSRF filter silently blocked every authenticated mutation.** Initial `SecurityConfig` wired
   a cookie-based CSRF token repository with the login endpoint exempted. Every `PUT`/`POST`/
   `DELETE` after login returned `403` because the frontend never read and echoed the CSRF token.
   Rather than adding token-plumbing complexity to the frontend for no real security gain in this
   same-site, cookie-session, CORS-restricted demo, CSRF protection was disabled for `/api/**`
   (see `ARCHITECTURE.md` → Known Limitations for the explicit trade-off and what a production
   deployment should do instead).

3. **TipTap fired `onUpdate` during programmatic content load, triggering a spurious autosave
   attempt (and a visible permission-error banner) the instant a Viewer opened a document**, even
   though they hadn't typed anything. Caught by actually logging in as Charlie (the seeded
   Viewer) in a real browser and seeing the error banner on page load. Fixed two ways: passing
   `{ emitUpdate: false }` to `editor.commands.setContent()` on initial load, and gating the
   `onUpdate` handler with a `readyToEditRef` flag so no save is scheduled until the document has
   actually finished loading and the caller's role is confirmed to allow edits.

A smaller issue — duplicate `Underline` extension registration (StarterKit v3 already bundles it,
and an explicit `@tiptap/extension-underline` import was also added) — was caught from a console
warning during the same manual browser pass and removed.

## How generated code was reviewed and tested

- **Backend**: compiled with `mvn compile` after each major addition; ran the full JUnit/Mockito
  suite (`mvn test`) — 17 tests, all passing, covering document creation/update, viewer-cannot-
  edit, editor-can-edit, non-owner-cannot-delete, non-owner-cannot-manage-sharing, unsupported-
  file-rejected, plus IDOR (404-not-403) and optimistic-concurrency-conflict cases.
- **API surface**: exercised directly with `curl` against a real PostgreSQL instance (via Docker
  Compose) for every endpoint and every permission combination — not just the happy path:
  unauthenticated 401s, cross-role 403s, IDOR 404s, duplicate-share 400s, stale-version 409s, and
  file-import validation (good txt, good md, wrong extension, empty file) were all indeed hit
  with real HTTP requests and real responses inspected, not assumed from reading the code.
- **Frontend**: type-checked and built with `tsc -b && vite build` after each phase; ran the
  Vitest suite (`npm test`) — 5 tests covering autosave debounce/conflict/error behavior and the
  login interaction.
- **Full end-to-end**: driven through an actual Chromium browser session (not a mock DOM) —
  logged in as Alice, Bob, and Charlie in turn; created, edited, and autosaved a document;
  confirmed the save round-tripped to PostgreSQL by reloading; shared and revoked access through
  the real UI (watched the network requests, not just the resulting screen); confirmed Charlie's
  editor is genuinely non-editable (typing into it does nothing, not just visually disabled); and
  confirmed Docker Compose actually builds the backend image and boots the whole stack
  (`docker compose up -d`) with demo data present.

## What was manually verified (not just assumed from code)

- Alice/Bob/Charlie seeded accounts and the Product-Roadmap sharing scenario described in the
  assignment brief, confirmed live via the UI, not just by reading the seed SQL.
- Session-cookie authentication actually gates every `/api/**` route (verified a 401 on an
  unauthenticated request).
- Server-side authorization is the real boundary, not just hidden UI controls: Charlie's browser
  session was used to attempt a PUT against the roadmap document directly, and got a 403 — the
  button being hidden is a UX nicety, not the security control.
- The Docker image for the backend actually builds and the containerized backend serves the same
  API against containerized Postgres (the default `eclipse-temurin:17-jre-alpine` base image was
  swapped to the Debian-based `eclipse-temurin:17-jre` after a real build failure — "no match for
  platform in manifest" — on this machine's architecture).

No deployment to a paid or external hosting provider was attempted or claimed; see
`SUBMISSION.md` for what "deployment" means for this submission.

## Second pass: six additional features

A later request asked for six enhancements beyond the original scope: `.docx` import support, one
more meaningful automated test, real-time collaboration indicators, commenting, document version
history, and export to PDF/Markdown. Three of these (presence, comments, history) were explicitly
scoped down to lightweight-but-real versions with the user's confirmation first (polling presence
instead of WebSocket push; document-level comments instead of text-anchored ones; view-only
history instead of restore/diff) — these trade-offs are documented plainly in `ARCHITECTURE.md`.

Three more concrete bugs were found via actual execution during this pass:

4. **CORS preflight rejected `PATCH` requests.** `SecurityConfig`'s allowed-methods list had never
   included `PATCH` (the original API surface only used GET/POST/PUT/DELETE). The new comment-
   resolve endpoint uses `PATCH`, and the first real click of "resolve" in the browser returned a
   403 on the `OPTIONS` preflight. Fixed by adding `PATCH` to `setAllowedMethods(...)`.

5. **PDF export crashed on its first real run.** `openhtmltopdf` parses its HTML input with a
   strict XML parser, not an HTML5 parser — the first version of the exporter emitted
   `<meta charset="utf-8">` (valid HTML, invalid XML, since the tag isn't self-closed), which threw
   `XRRuntimeException: ... must be terminated by the matching end-tag`. Caught immediately by an
   automated test (`exportsPdfBytesStartingWithPdfMagicNumber`) rather than only in the browser.
   Fixed by emitting well-formed XHTML (`<meta charset="utf-8"/>`, an XML declaration, and the
   XHTML namespace on `<html>`).

6. **DOCX bullet lists from some generators were imported as plain paragraphs.** The DOCX parser
   initially only detected list items via Word's low-level numbering property (`numID`). A real
   `.docx` built with `python-docx` (`doc.add_paragraph(text, style='List Bullet')`) sets the
   paragraph *style* to "List Bullet" without populating `numID`, so those items silently imported
   as plain text instead of a bullet list. Caught by actually importing a generated test file
   through the running API and inspecting the response JSON, not by reading the parser code. Fixed
   by also treating any paragraph whose style name contains "List" as a list item.

Also found and fixed along the way (not a bug, but a real operational issue): two unrelated local
projects on this machine were found to share the same PostgreSQL port (5433), one of them
configured with Hibernate's `ddl-auto=create-drop` — meaning every time that *other* project's
backend restarted, it silently dropped and recreated the shared database, wiping this project's
data. Confirmed live by inspecting `ps aux` for the actual competing process and its
`application.yml`. Fixed by moving this project's Postgres to its own isolated port (5434) with a
distinctly-named container and volume, after confirming with the user that the other process
should be left running rather than killed.

### How this pass was verified

- **Backend**: `mvn test` after each feature — grew from 17 to 26 passing tests, including new
  coverage for DOCX parsing (built a real in-memory `.docx` with `XWPFDocument`, not a fixture
  file), comment authorization (viewer-can-comment, author-can-resolve-own, non-author-cannot-
  resolve-others, owner-can-delete-any), and export (real Markdown string assertions and a real
  PDF-magic-number check on the generated bytes).
- **Manual, through the real running app**: imported a real generated `.docx` and confirmed
  headings and the bullet list rendered correctly in the editor after the list-detection fix;
  opened the same document as two logged-in sessions (Shambhavi and Bob) and confirmed each saw
  the other's presence chip and that it correctly enforces the same access check as every other
  document route (a user with no access to a document gets 404 on the heartbeat endpoint too);
  added, resolved, and deleted a comment through the UI and watched the network requests, not just
  the resulting screen; made two edits and confirmed two distinct version-history entries appeared
  with the correct pre-edit content in the read-only preview; downloaded both a PDF and a Markdown
  export and opened/inspected both files directly rather than assuming a 200 response meant the
  file was well-formed.
