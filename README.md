# SkillForge AI-Driven Learning Platform

SkillForge is a full-stack learning platform focused on coding practice, adaptive learning paths, tutor-led courses, exams, and certificates.

## Repository Layout

This repository is organized as a monorepo with two top-level folders:

- `skillforge-frontend/` - React + Vite frontend (served through `server.ts`)
- `skillforge-backend/` - Spring Boot backend (Java 21 + PostgreSQL)

## Tech Stack

### Frontend
- React 19
- TypeScript
- Vite
- Express (custom dev server)
- Firebase Auth (Google sign-in)

### Backend
- Spring Boot 3.2.4
- Java 21
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Firebase Admin SDK

## Prerequisites

Install these tools before running locally:

- Node.js 20+
- Java 21
- Maven 3.9+
- PostgreSQL 15+

## Environment Configuration

## Backend (`skillforge-backend/.env`)

Create a `.env` file in `skillforge-backend/` using `skillforge-backend/.env.example` as reference.

Required keys:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_SENDER_NAME`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GEMINI_API_KEY`
- `CORS_ORIGINS`

## Frontend

Frontend reads runtime values from environment variables.

Commonly used keys during local run:

- `PORT` (default: `3000`)
- `VITE_HMR_PORT` (example: `24679`)
- `VITE_API_BASE_URL` (example: `http://localhost:8081/api`)

Firebase keys used by client build:

- `VITE_FIREBASE_API_KEY`
- `VITE_FIREBASE_AUTH_DOMAIN`
- `VITE_FIREBASE_PROJECT_ID`
- `VITE_FIREBASE_STORAGE_BUCKET`
- `VITE_FIREBASE_MESSAGING_SENDER_ID`
- `VITE_FIREBASE_APP_ID`

## Run Locally (PowerShell)

From repository root:

### 1) Start Backend

```powershell
Set-Location .\skillforge-backend
$env:PORT='8081'
mvn -DskipTests spring-boot:run
```

### 2) Start Frontend (new terminal)

```powershell
Set-Location .\skillforge-frontend
$env:PORT='3001'
$env:VITE_HMR_PORT='24679'
$env:VITE_API_BASE_URL='http://localhost:8081/api'
npm install
npm run dev -- --host
```

Then open:

- Frontend: `http://localhost:3001`
- Backend API base: `http://localhost:8081/api`

## Build Commands

### Frontend

```powershell
Set-Location .\skillforge-frontend
npm run build
```

### Backend

```powershell
Set-Location .\skillforge-backend
mvn -DskipTests package
```

## Notes

- Keep `.env` files out of version control.
- Keep Firebase service-account files out of version control.
- If default ports are busy, update `PORT` and `VITE_API_BASE_URL` accordingly.
