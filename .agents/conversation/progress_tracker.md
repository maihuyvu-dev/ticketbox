# Progress Tracker — TicketBox (Member 4)

Last Updated: 2026-06-03

---

## Phase 0 — Project Setup & Scaffolding

- [x] Repository structure created
- [x] README.md with folder structure and git workflow
- [x] .env.example with PostgreSQL, Gemini, Resend keys
- [x] .gitignore configured (.env, .agents)
- [x] Branch strategy defined (main, develop, backend, frontend, mobileapp, infra)

---

## Phase 1 — Infrastructure (Member 4)

### Docker Compose

- [x] docker-compose.yml created (PostgreSQL 15-alpine, Redis 7-alpine, RabbitMQ 3-management-alpine)
- [x] Named volumes for data persistence (postgres_data, redis_data, rabbitmq_data)
- [x] Healthchecks defined for all 3 services
- [ ] **FIX NEEDED:** PostgreSQL healthcheck uses empty `-U` and `-d` flags — must reference env variables
- [x] init.sql with sample concerts table and seed data
- [x] Infrastructure README (infra/README.md) — complete setup guide for Windows/macOS

### Docker Improvements Needed

- [ ] Add `depends_on` with `condition: service_healthy` for future Spring Boot service
- [ ] Fix PostgreSQL healthcheck to use `${POSTGRES_USER}` and `${POSTGRES_DB}`
- [ ] Consider adding network configuration for service discovery
- [ ] Consider memory limits for lightweight development environment

---

## Phase 1.5 — Blueprint Documentation

- [x] C4 Container diagram (Level 2) created (docs/architecture/container_diagram.png)
- [x] design.md referencing container diagram
- [ ] proposal.md (as per project requirements template)
- [ ] Detailed design.md with all sections (architecture, DB schema, access control, protection mechanisms)
- [ ] specs/ directory with feature specifications (auth.md, payment.md, checkin.md, etc.)

---

## Phase 2 — API Protection (Member 4)

### Rate Limiting

- [ ] Bucket4j + Redis dependency in Spring Boot
- [ ] RateLimitFilter implementation
- [ ] Token Bucket configuration
- [ ] Public API throttling (by IP)
- [ ] Private API throttling (by User ID from JWT)
- [ ] HTTP 429 JSON response format

### Payment Protection

- [ ] Resilience4j dependency
- [ ] Circuit Breaker configuration (count-based sliding window)
- [ ] Bulkhead configuration (thread limit for payment calls)
- [ ] Fallback handler (PAYMENT_MAINTENANCE response)
- [ ] Graceful degradation logic

---

## Phase 3 — Async Processing (Member 4)

### RabbitMQ Configuration

- [ ] Exchange definitions
- [ ] Queue definitions
- [ ] DLQ configuration
- [ ] Retry mechanism (max 3 retries)
- [ ] Spring AMQP configuration

### CSV Import Worker

- [ ] CSV streaming reader
- [ ] Fault-tolerant line-by-line processing
- [ ] Idempotent import logic
- [ ] Duplicate detection
- [ ] Error logging for malformed lines

### AI Worker

- [ ] PDF text extraction
- [ ] Text cleaning/preprocessing
- [ ] Gemini API integration
- [ ] Request throttling
- [ ] Async consumer from RabbitMQ

### Email Worker

- [ ] Resend API integration
- [ ] Email queue consumer
- [ ] Retry handling
- [ ] E-ticket email with QR attachment
- [ ] Event reminder email

---

## Blocked Tasks

| Task | Blocked By | Reason |
|------|-----------|--------|
| Rate Limiting Filter | Member 1 | Requires Spring Boot project initialization |
| Circuit Breaker | Member 1 | Requires Spring Boot project + payment service interface |
| RabbitMQ Spring Config | Member 1 | Requires Spring Boot project initialization |
| All Workers | Member 1 | Requires Spring Boot project + entity/DTO definitions |

---

## Dependencies on Other Members

| Member | Dependency | Status |
|--------|-----------|--------|
| Member 1 (Backend) | Spring Boot project initialization | ❌ Not started |
| Member 1 (Backend) | Entity/DTO definitions (Order, Ticket, Concert, User) | ❌ Not started |
| Member 1 (Backend) | Payment service interface | ❌ Not started |
| Member 2 (Frontend) | None — frontend consumes APIs | N/A |
| Member 3 (Mobile) | Technology decision (React Native vs Flutter) | ❌ Pending |
