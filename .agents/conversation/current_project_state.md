# Current Project State — TicketBox

Last Updated: 2026-06-03

---

## Current Phase

**Phase 1 — Infrastructure** (partially complete)

The Docker infrastructure is in place but has minor issues to fix. The Blueprint documentation is partially complete (C4 diagram exists, but design.md and specs/ are still stubs).

No application code (Spring Boot, Next.js, Mobile) has been created yet.

---

## Existing Architecture Status

### Confirmed by C4 Container Diagram

The system architecture has been designed and documented as a C4 Level 2 Container diagram. The following containers are defined:

| Container | Technology | Status |
|-----------|-----------|--------|
| Web Application | Next.js 14, Zustand, Tailwind CSS | ❌ Not started (placeholder README only) |
| Mobile Check-in App | React Native/Flutter, SQLite | ❌ Not started (placeholder README only) |
| Backend Core API | Java, Spring Boot 3 | ❌ Not started (placeholder README only) |
| Redis Cache & Distributed Lock | Redis 7 (Docker) | ✅ Docker container configured |
| Message Broker | RabbitMQ 3 (Docker) | ✅ Docker container configured |
| Async Workers | Java, Spring Boot 3 | ❌ Not started |
| PostgreSQL Database | PostgreSQL 15 (Docker) | ✅ Docker container configured |
| Payment Gateway | VNPAY/MoMo (External) | ❌ Not integrated |
| Email Service | Resend/Brevo (Cloud) | ❌ Not integrated |
| AI Model | Gemini/OpenAI (Cloud) | ❌ Not integrated |

---

## Existing Decisions

### Architecture

* Monolithic Spring Boot backend with Docker-containerized infrastructure services
* Frontend: Next.js 14 SPA
* Mobile: Offline-first with SQLite + RSA
* Communication: REST API + SSE for real-time

### Database

* PostgreSQL 15 as primary database
* Pessimistic Locking for ticket contention
* Redis for caching, rate limiting, distributed locks, idempotency keys

### Message Broker

* RabbitMQ for async processing
* DLQ with max 3 retries
* Workers for: CSV import, AI bio generation, Email notifications

### Security

* JWT stateless auth
* RBAC: CUSTOMER, ORGANIZER, CHECKER
* Rate limiting: Token Bucket via Bucket4j + Redis
* Circuit Breaker + Bulkhead via Resilience4j

---

## Existing Infrastructure

### Docker Compose (infra/docker/docker-compose.yml)

| Service | Image | Container Name | Ports | Healthcheck | Status |
|---------|-------|---------------|-------|-------------|--------|
| PostgreSQL | postgres:15-alpine | ticketbox_postgres | 5432 | ⚠️ Broken (empty user/db) | Needs fix |
| Redis | redis:7-alpine | ticketbox_redis | 6379 | ✅ Working | OK |
| RabbitMQ | rabbitmq:3-management-alpine | ticketbox_rabbitmq | 5672, 15672 | ✅ Working | OK |

### Known Issue

The PostgreSQL healthcheck on line 15 of docker-compose.yml has empty `-U` and `-d` flags:

```yaml
test: [CMD-SHELL, pg_isready -U -d ]
```

Should be:

```yaml
test: ["CMD-SHELL", "pg_isready -U $$POSTGRES_USER -d $$POSTGRES_DB"]
```

### Environment Configuration

* `.env.example` provides template with PostgreSQL, Gemini, Resend keys
* `.env` file exists (gitignored)

### Database Init

* `init.sql` creates a basic `concerts` table with a sample record
* This is a test/placeholder schema, not the final production schema

---

## Existing Implementations

| Component | Files | Status |
|-----------|-------|--------|
| Docker Infrastructure | `infra/docker/docker-compose.yml`, `infra/docker/init.sql` | ✅ Functional (with PG healthcheck bug) |
| Infrastructure Docs | `infra/README.md` | ✅ Complete setup guide |
| Architecture Diagram | `docs/architecture/container_diagram.png`, `docs/architecture/design.md` | ✅ C4 Level 2 exists |
| Project README | `README.md` | ✅ Folder structure + git workflow |
| Environment Config | `.env.example`, `.env` | ✅ Template ready |
| Agent Rules | `.agents/rules/*` | ✅ 3 rule files |
| Agent Knowledge | `.agents/knowledge/*` | ✅ 3 knowledge files |

---

## Missing Implementations

### Critical Path (Blocking All Members)

1. **Spring Boot project initialization** — no `pom.xml`, no `src/` directory, no Spring Boot application class
2. **Entity/DTO definitions** — no database schema design beyond the placeholder `concerts` table
3. **Application configuration** — no `application.yml` for Spring Boot

### Member 4 Specific

1. Fix PostgreSQL healthcheck in docker-compose.yml
2. Add `depends_on: condition: service_healthy` for Spring Boot service
3. All Phase 2 deliverables (Rate Limiting, Circuit Breaker, Bulkhead)
4. All Phase 3 deliverables (RabbitMQ config, DLQ, Workers)
5. Blueprint documentation (proposal.md, detailed design.md, specs/)

### Other Members

1. **Member 1:** Entire backend (entities, repositories, services, controllers, security)
2. **Member 2:** Entire frontend (Next.js project, components, pages)
3. **Member 3:** Entire mobile app (technology not yet decided)

---

## Recommended Next Task

### Option A: Fix Docker Compose (Quick Win)

Fix the PostgreSQL healthcheck bug and add proper `depends_on` configuration. This is a 5-minute fix that improves infrastructure reliability.

### Option B: Enhance Docker Compose for Spring Boot Integration (Recommended)

Prepare the docker-compose.yml for the Spring Boot service by:

1. Fixing PostgreSQL healthcheck
2. Adding Spring Boot service definition with `depends_on` conditions
3. Adding proper network configuration
4. Adding memory limits for lightweight development

### Option C: Create Blueprint Documentation

Write the proposal.md, detailed design.md, and feature specs as required by the project deliverables. This can be done independently of other members.

**Recommended:** Option B or C — both can be done independently without blocking on other members.
