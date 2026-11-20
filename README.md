# DocSpace

Collaborative document editor. Think Google Docs, lightweight. Create docs, format them, import
files, share with people, comment, see version history, export to PDF/Markdown.

## What it does

- Create, rename, delete your own documents
- Rich text editing — bold, italic, underline, headings, lists, autosave
- Import `.txt`, `.md`, `.docx` files as new docs
- Export any doc to PDF or Markdown
- Comment on docs, resolve/delete comments
- See past versions of a doc (read-only for now)
- See who else has a doc open right now
- Share docs as Viewer (read-only) or Editor (can edit)
- Separate views for docs you own vs docs shared with you
- Everything's in Postgres

## Stack

**Backend:** Java 17, Spring Boot 3, Spring Data JPA, Spring Security, PostgreSQL, Flyway, Maven,
JUnit 5, Mockito.

**Frontend:** React 19, TypeScript, Vite, Tailwind v4, TipTap, React Router, Axios, lucide-react,
Vitest.

One backend, one frontend, one database. No microservices, no message queues — didn't need them
at this scale.

## Before you start

You'll need:
- Java 17+ (built with 21, works fine)
- Maven 3.9+
- Node 20+ and npm
- Docker (for Postgres, and optionally for running the backend too)

## Running it locally

### 1. Start Postgres

```bash
docker compose up -d postgres
```

Runs on port **5434** on purpose — not 5432 or 5433, to avoid clashing with any other Postgres
you've got running locally. Creates a `docspace` database, user/pass `docspace`/`docspace`.

### 2. Start the backend

```bash
cd backend
DB_URL="jdbc:postgresql://localhost:5434/docspace" DB_USERNAME=docspace DB_PASSWORD=docspace \
  mvn spring-boot:run
```

Flyway sets up the schema and seeds demo data automatically the first time it runs — nothing to
do manually. API's on `http://localhost:8080`. Check `GET /api/health` if you want to confirm
it's up.

### 3. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

App's on `http://localhost:5173`. If you need to point it at a different backend, copy
`.env.example` to `.env.local` and set `VITE_API_BASE_URL`.

### Or just run everything via Docker Compose

```bash
docker compose up -d
```

Brings up Postgres and the backend together. Run the frontend separately with `npm run dev`, or
`npm run build` for a production bundle.

## Demo logins

No passwords — pick an account on the login screen and you're in. Backend still figures out who
you are from its own session, not from anything the frontend tells it.

| Name | Email | Role |
|---|---|---|
| Shambhavi | `shambhavi@ajaia.com` | Owns "Product Roadmap" |
| Bob | `bob@agij.com` | Editor on Shambhavi's roadmap |
| Charlie | `charlie@agij.com` | Viewer on Shambhavi's roadmap |

Log in as each one to see owned vs shared docs and what editor/viewer permissions actually look
like.

## API endpoints

**Auth**
- `POST /api/auth/login` — `{ email }`, sets a session cookie
- `POST /api/auth/logout`
- `GET /api/auth/me`

**Documents**
- `POST /api/documents`
- `GET /api/documents` — returns `{ owned: [...], shared: [...] }`
- `GET /api/documents/{id}`
- `PUT /api/documents/{id}` — `{ title?, content?, version }`
- `DELETE /api/documents/{id}` — owner only

**Sharing** (owner only)
- `POST /api/documents/{id}/shares` — `{ email, permission }`
- `GET /api/documents/{id}/shares`
- `DELETE /api/documents/{id}/shares/{shareId}`

**Files**
- `POST /api/files/import`

**Export**
- `GET /api/documents/{id}/export?format=pdf`
- `GET /api/documents/{id}/export?format=markdown`

**Comments**
- `POST /api/documents/{id}/comments`
- `GET /api/documents/{id}/comments`
- `PATCH /api/documents/{id}/comments/{commentId}/resolve`
- `DELETE /api/documents/{id}/comments/{commentId}`

**Version history**
- `GET /api/documents/{id}/versions`
- `GET /api/documents/{id}/versions/{versionId}`

**Presence**
- `POST /api/documents/{id}/presence/heartbeat`
- `GET /api/documents/{id}/presence`

**Health**
- `GET /api/health`

## Upload formats

- `.txt` → paragraphs
- `.md` → headings (`#`/`##`), bullet lists, paragraphs — light Markdown subset, not full
  CommonMark
- `.docx` → headings, bullet lists, paragraphs — no tables/images/inline formatting yet
- 2MB max, empty files rejected, wrong extensions rejected

## Running tests

```bash
# Backend
cd backend && mvn test

# Frontend
cd frontend && npm test
```

26 backend tests, 5 frontend tests, all passing as of the last run.
