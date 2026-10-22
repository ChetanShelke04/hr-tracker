# HR & Job Follow-up Tracker

A modern, multi-tenant cloud application for tracking HR recruiter contacts, call histories, follow-ups, and job applications across multiple devices (mobile PWA and desktop) with strict data privacy between users.

---

## Architecture Overview

* **Frontend**: React 18 + Vite (Responsive, Glassmorphism, Dark UI)
* **Backend**: Java 21 + Spring Boot 3.3.4 (REST API, Spring Security, Stateless JWT)
* **Databases**:
  * **Local Dev**: In-Memory H2 Database (`application-dev.yml` — Zero setup)
  * **Production Cloud**: Neon Serverless PostgreSQL (`application-prod.yml`)
* **Deployment Targets**: Render (Backend Docker Web Service) + Vercel (Frontend Global CDN)

---

## Quick Start (Local Run)

### 1. Start Backend
```bash
cd backend
mvn spring-boot:run
```
* Backend runs on: `http://localhost:8080`
* H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:hrtracker`, user: `sa`, password: blank)

### 2. Start Frontend
```bash
cd frontend
npm install
npm run dev
```
* Frontend runs on: `http://localhost:5173`

---

## Running Tests

```bash
cd backend
mvn test
```
Runs the automated test suite verifying:
1. Multi-tenant user isolation (User A cannot access User B's contacts)
2. Atomic call logging and contact synchronization
3. Import JSON with deduplication by phone number
4. Soft delete filtering
