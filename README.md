# DocSpace

A lightweight collaborative document editor, inspired by Google Docs. Create documents, format
them with a rich-text editor, import TXT/Markdown/DOCX files, comment and track revision history,
export to PDF or Markdown, and share documents with other users as a viewer or editor.

## Product overview

- Create, rename, and delete documents you own
- Rich-text editing (bold, italic, underline, headings, lists) with autosave
- Import `.txt`, `.md`, and `.docx` files as new documents
- Export any document to **PDF** or **Markdown**
- Comment on a document and resolve/delete comments
- Browse a read-only version history of past saves
- See who else currently has a document open (lightweight presence)
- Share documents with other users as **Viewer** (read-only) or **Editor** (can edit content)
- Separate "Owned Documents" and "Shared With Me" views
- All data persisted in PostgreSQL

## Technology stack

**Backend:** Java 17, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security, PostgreSQL,
Flyway, Maven, Jakarta Validation, JUnit 5, Mockito.

**Frontend:** React 19, TypeScript, Vite, Tailwind CSS v4, TipTap, React Router, Axios,
lucide-react, Vitest + React Testing Library.

**Database:** PostgreSQL 16, managed with Flyway migrations.

Architecture is a modular monolith — one Spring Boot backend, one React SPA frontend, no
microservices or message queues.

## Prerequisites

- Java 17+ (project targets Java 17; tested with a Java 21 JDK as well)
- Maven 3.9+
- Node.js 20+ and npm
- Docker + Docker Compose (for PostgreSQL, and optionally the backend container)

## Local setup

### 1. Database setup

Start PostgreSQL via Docker Compose (runs on host port **5434** — intentionally non-default to
avoid colliding with other local Postgres instances, including other projects that may run on
5432/5433):

```bash
docker compose up -d postgres
```

This creates a `docspace` database with user/password `docspace`/`docspace`. Flyway will create
the schema and seed demo data automatically the first time the backend starts — no manual SQL
needed.

### 2. Backend startup

```bash
cd backend
DB_URL="jdbc:postgresql://localhost:5434/docspace" DB_USERNAME=docspace DB_PASSWORD=docspace \
  mvn spring-boot:run
```

The API starts on `http://localhost:8080`. Health check: `GET /api/health`.

### 3. Frontend startup

```bash
cd frontend
npm install
npm run dev
```

The app starts on `http://localhost:5173`. Copy `.env.example` to `.env.local` if you need to
point it at a different backend URL (`VITE_API_BASE_URL`).

### Running everything with Docker Compose

```bash
docker compose up -d
```

This builds and runs PostgreSQL and the backend container together (backend on `:8080`,
Postgres on `:5434`). Run the frontend separately with `npm run dev` for local development, or
`npm run build` to produce a static production bundle (see `frontend/dist`).

## Demo credentials

Authentication is intentionally lightweight for this demo: pick a seeded account on the login
screen, no password required. The backend still determines identity from the server-side session
on every request — the frontend never sends a user id.

| Name | Email | Role in the demo scenario |
|---|---|---|
| Shambhavi | `shambhavi@ajaia.com` | Owns "Product Roadmap" |
| Bob | `bob@agij.com` | Editor on Shambhavi's "Product Roadmap" |
| Charlie | `charlie@agij.com` | Viewer on Shambhavi's "Product Roadmap" |

Log in as each user from the same login screen to see owned vs. shared documents and the
editor/viewer permission difference.

## API endpoints

**Auth**
- `POST /api/auth/login` — `{ "email": "..." }`, sets a session cookie
- `POST /api/auth/logout`
- `GET /api/auth/me`

**Documents**
- `POST /api/documents` — create
- `GET /api/documents` — `{ owned: [...], shared: [...] }`
- `GET /api/documents/{id}`
- `PUT /api/documents/{id}` — `{ title?, content?, version }` (version required for conflict
  detection)
- `DELETE /api/documents/{id}` — owner only

**Sharing** (owner only)
- `POST /api/documents/{id}/shares` — `{ email, permission: "VIEWER"|"EDITOR" }`
- `GET /api/documents/{id}/shares`
- `DELETE /api/documents/{id}/shares/{shareId}`

**Files**
- `POST /api/files/import` — multipart `file` field

**Export**
- `GET /api/documents/{id}/export?format=pdf` — downloads a PDF
- `GET /api/documents/{id}/export?format=markdown` — downloads a `.md` file

**Comments** (any collaborator with read access)
- `POST /api/documents/{id}/comments` — `{ body }`
- `GET /api/documents/{id}/comments`
- `PATCH /api/documents/{id}/comments/{commentId}/resolve`
- `DELETE /api/documents/{id}/comments/{commentId}` — author or document owner only

**Version history** (read access required; view-only, no restore yet)
- `GET /api/documents/{id}/versions` — list of past snapshots
- `GET /api/documents/{id}/versions/{versionId}` — full content of one snapshot

**Presence** (read access required; lightweight polling, not WebSocket push)
- `POST /api/documents/{id}/presence/heartbeat`
- `GET /api/documents/{id}/presence` — other users currently viewing

**Health**
- `GET /api/health`

## Supported upload formats

- `.txt` — split into paragraphs
- `.md` — `#`/`##` headings, `-`/`*` bullet lists, and paragraphs are converted into structured
  editor content (a lightweight Markdown subset, not full CommonMark)
- `.docx` — headings (via paragraph style), bullet lists (via Word numbering or a "List"-named
  style), and paragraphs are converted into the same structured editor content; tables, images,
  and inline text formatting are not preserved
- Max file size: 2MB
- Empty files and unsupported extensions/content-types are rejected

## Tests

```bash
# Backend
cd backend && mvn test

# Frontend
cd frontend && npm test
```
