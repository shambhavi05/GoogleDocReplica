# Submission

Drive folder (screenshots, video, anything that doesn't belong in git):
https://drive.google.com/drive/folders/1QJOEy2o-9BJIiHDv9m600iaJI8Tu-x3X

## Deliverables index

| Deliverable | Doc / path in repo |
|---|---|
| Source code | `backend/`, `frontend/` |
| README with setup + run instructions | [`README.md`](README.md) |
| Architecture note | [`ARCHITECTURE.md`](ARCHITECTURE.md) |
| AI workflow note | [`AI_WORKFLOW.md`](AI_WORKFLOW.md) |
| Submission file (what's included) | [`SUBMISSION.md`](SUBMISSION.md) — this file |
| Live product URL | [`SUBMISSION.md`](SUBMISSION.md#live-url) — to be filled in once deployed |
| Walkthrough video URL | [`walkthrough-video.txt`](walkthrough-video.txt) + Drive folder above |
| Screenshots / demo GIF | [`screenshots/`](screenshots/) + Drive folder above |
| Manual test checklist (extra) | [`UI_TEST_CASES.md`](UI_TEST_CASES.md) |
| Sample files for testing import (extra) | [`test-files/`](test-files/) |
| Render deploy blueprint (extra) | [`render.yaml`](render.yaml) |

## What's included

- `backend/` — Spring Boot 3 / Java 17 API
- `frontend/` — React 19 / TypeScript / Vite app
- `docker-compose.yml` — Postgres + backend, one command to run locally
- `render.yaml` — Render blueprint for the live deploy
- `test-files/` — sample files for testing import (txt/md/docx, plus deliberately broken ones)
- `README.md` — setup, run, API reference
- `ARCHITECTURE.md` — schema, API design, auth, trade-offs, what's not built yet
- `AI_WORKFLOW.md` — how this got built, what broke, how it was checked
- `UI_TEST_CASES.md` — manual test checklist, every feature
- `SUBMISSION.md` — this file

## Running it yourself

Full detail's in `README.md`. Short version:

```bash
# 1. Postgres
docker compose up -d postgres

# 2. Backend (new terminal)
cd backend
DB_URL="jdbc:postgresql://localhost:5434/docspace" DB_USERNAME=docspace DB_PASSWORD=docspace \
  mvn spring-boot:run

# 3. Frontend (new terminal)
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`, pick a demo account.

Or `docker compose up -d` to bring up Postgres and the backend together, frontend still separate
with `npm run dev`.

## Live URL

**[TO BE FILLED IN]** — backend's deploying to Render (blueprint's in `render.yaml`, pushed and
tested against a real Postgres connection string before going live), frontend's going to Vercel.
Not filling in a URL here until both are actually confirmed working — won't paste something that
hasn't been checked.

## Demo logins

No passwords, pick an account:

| Name | Email |
|---|---|
| Shambhavi | `shambhavi@ajaia.com` |
| Bob | `bob@agij.com` |
| Charlie | `charlie@agij.com` |

Shambhavi owns "Product Roadmap," shared it with Bob as Editor and Charlie as Viewer. Log in as
each to see the owned/shared/permission split in action.

## Walkthrough video

See `walkthrough-video.txt` — not recorded yet, placeholder for now. Will be linked here and
dropped into the Drive folder above once done.

## What's done

- Create, rename, edit, delete owned documents
- Rich text editing: bold, italic, underline, H1/H2, bullet/ordered lists, undo/redo
- Autosave, debounced ~1s, with Saving/Saved/Failed/Changed-elsewhere states
- Version conflict detection — 409 on a stale write instead of silently overwriting
- Import `.txt`, `.md`, `.docx` as new documents, validated server-side
- Export any document to PDF or Markdown
- Comment on documents, resolve/delete (author or owner only)
- Read-only version history — every save snapshots the prior state
- Lightweight presence — see who else has a document open
- Share by email, Viewer or Editor, revoke access, no duplicate shares
- Dashboard: owned, shared, recent, search, empty states
- Session-cookie auth, identity resolved server-side always
- Authorization in the service layer for every path, no-access returns 404 not 403
- Centralized error handling, no leaked stack traces
- 26 backend tests, 5 frontend tests, all passing

## What's not done

- Not deployed yet (see Live URL above)
- No real-time co-editing or live cursors — version-based conflict detection and polling presence
  only
- No real auth (password/OAuth) — by design for a demo, schema supports adding it later
- Markdown/DOCX import+export: headings, lists, paragraphs only, not full CommonMark or full DOCX
  fidelity
- Comments aren't anchored to specific text, no suggestion mode
- Version history is view-only, no restore, no diff
- No pagination anywhere

## What I'd do next

- Real collaborative editing with live cursors
- Finish the Render + Vercel deploy and drop the real URL in here
- Record the walkthrough video
- Full CommonMark both ways, better DOCX fidelity
- Text-anchored comments, real suggestion mode
- Restore-to-version and diffing
