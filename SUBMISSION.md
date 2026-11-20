# Submission

## Deliverables included

- `backend/` — Spring Boot 3 / Java 17 REST API (Maven project)
- `frontend/` — React 19 / TypeScript / Vite SPA
- `docker-compose.yml` — PostgreSQL + backend container, for local one-command startup
- `README.md` — setup, run, and API reference
- `ARCHITECTURE.md` — schema, API design, authorization strategy, trade-offs, limitations
- `AI_WORKFLOW.md` — how AI was used, what was changed/rejected, what was verified
- `SUBMISSION.md` — this file

## Local setup instructions

See `README.md` for full detail. Short version:

```bash
# 1. Start Postgres
docker compose up -d postgres

# 2. Start the backend (new terminal)
cd backend
DB_URL="jdbc:postgresql://localhost:5434/docspace" DB_USERNAME=docspace DB_PASSWORD=docspace \
  mvn spring-boot:run

# 3. Start the frontend (new terminal)
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173` and pick a demo account.

Alternatively, `docker compose up -d` builds and runs Postgres + the backend together; run the
frontend separately with `npm run dev`.

## Live product URL

**Not deployed to any external host.** This submission was built and verified entirely on a
local machine (local PostgreSQL via Docker, local Spring Boot process, local Vite dev server).
No cloud deployment was attempted, so no live URL is provided — adding one here would be
fabricating a result that wasn't actually produced. The app runs locally per the setup
instructions above, and the full Docker Compose stack (Postgres + backend container) was verified
to build and run together.

## Demo credentials

No passwords — pick one of the three seeded accounts on the login screen:

| Name | Email |
|---|---|
| Shambhavi | `shambhavi@ajaia.com` |
| Bob | `bob@agij.com` |
| Charlie | `charlie@agij.com` |

Seeded scenario: **Shambhavi** owns "Product Roadmap" and has shared it with **Bob** as Editor and
**Charlie** as Viewer. Logging in as each user and opening that document demonstrates the full
owned/shared/permission model.

## Walkthrough video URL

**Not recorded / no URL provided.** No video walkthrough was produced as part of this session;
this placeholder is left here rather than inventing a link.

## Completed functionality

- Create, rename (title), edit content, and delete owned documents
- Rich-text editing via TipTap: bold, italic, underline, H1/H2, bullet/ordered lists, undo/redo
- Debounced autosave (~1s) with Saving / Saved / Save failed / Changed-elsewhere states
- Optimistic-concurrency conflict detection (`version` field, `409` on stale writes)
- Import `.txt`, `.md`, and `.docx` files (drag-and-drop or file picker) as new owned documents,
  with extension/content-type/size/empty-file validation enforced server-side
- Export any document to PDF or Markdown
- Comment on a document (any collaborator with read access); resolve/delete comments
  (author or owner only)
- Read-only version history: every save snapshots the prior state; past versions are listable and
  viewable (no restore-to-version yet, see Incomplete functionality)
- Lightweight presence indicators: see who else currently has a document open (polling-based, not
  WebSocket push)
- Share documents by email with VIEWER or EDITOR permission; list and revoke collaborators;
  duplicate shares rejected
- Dashboard with Owned Documents, Shared With Me, Recent Documents, search, and empty states
- Session-cookie authentication; identity resolved server-side on every request
- Authorization enforced in the service layer for every mutating and read path, including IDOR
  protection (no-access documents return 404, not 403)
- Centralized error handling (`@RestControllerAdvice`) with consistent error shape and no leaked
  stack traces
- CORS configured via environment variable; health check endpoint (`/api/health`)
- 26 backend tests (JUnit/Mockito) covering all 7 originally required scenarios plus IDOR,
  concurrency, DOCX parsing, comment authorization, and export cases — all passing
- 5 frontend tests (Vitest/RTL) covering autosave debounce/conflict/error handling and the login
  interaction — all passing
- Docker Compose stack for Postgres + backend, verified to build and boot successfully

## Incomplete functionality

- No deployment to a hosted/cloud environment (see "Live product URL" above)
- No real-time multi-user concurrent editing or live cursors (optimistic-lock-and-retry for
  content, polling presence for "who's here" — see `ARCHITECTURE.md` → Known Limitations)
- No password/OAuth-based authentication (by design for this demo; schema supports adding it
  later without migration)
- Markdown import/export handles headings/bullet-lists/ordered-lists/paragraphs only, not full
  CommonMark (no tables, links, code blocks)
- DOCX import does not preserve tables, images, or inline text formatting (bold/italic runs) —
  only heading styles and list items, flattened to plain text
- Comments are document-level, not anchored to a specific text selection; no suggestion/accept-
  reject mode
- Version history is view-only — no restore-to-this-version action and no diff between versions
- No pagination on document, collaborator, comment, or version lists

## Next improvements

- Real-time collaborative editing (Yjs or similar CRDT-based sync) and live cursors
- Deploy to a free-tier host (e.g. Railway/Render for the backend + Postgres, Vercel/Netlify for
  the frontend) and replace the placeholder URL above
- Record a short walkthrough video
- Expand Markdown/DOCX import and export to full CommonMark/richer DOCX coverage
- Text-anchored comments and a real suggestion mode
- Restore-to-version and version diffing
