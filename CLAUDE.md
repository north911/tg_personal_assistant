# CLAUDE.md

This file provides guidance to Claude Code when working in this repository.

## Project Overview

A personal Telegram bot / assistant.

## Tech Stack

- **Language**: Java 25
- **Framework**: Spring Boot 4.1.0
- **Persistence**: Spring Data JPA
- **Database**: SQLite (file at `db/tg-personal-assistant.db`, configured in `application.properties`)
- **Migrations**: Flyway owns the schema (`spring.jpa.hibernate.ddl-auto=none`); migrations live in
  `src/main/resources/db/migration`. Spring Boot 4 splits auto-configuration per technology, so
  `spring-boot-starter-flyway` must stay in the pom — with only `flyway-core` there is no Flyway
  auto-configuration and migrations are skipped silently. SQLite support is built into `flyway-core`
  (no separate `flyway-database-*` module exists for it).
- **Deployment**: Docker (the entire app runs in containers)
- **Build tool**: Maven

## Development

- Build: `./mvnw clean package`
- Run locally: `./mvnw spring-boot:run` (JDK 25 is installed at `C:\Program Files\Java\jdk-25.0.2`, so `./mvnw clean verify` works on this machine)
- Run via Docker: `docker compose -f docker/docker-compose.yml up --build` (the image is runtime-only — build the jar with `./mvnw clean package` first)
- Tests: `./mvnw test`

## Conventions

- Keep configuration (bot token, secrets) out of source control — use environment variables / `.env` loaded via Docker Compose, never commit tokens.
- SQLite database file should live in a mounted Docker volume so data persists across container rebuilds.
- Java naming: use camelCase for all variables, parameters, fields and methods (`messageText`, not
  `message_text`), PascalCase for types, and `UPPER_SNAKE_CASE` only for `static final` constants and
  enum values. No snake_case identifiers in Java code — SQL/Flyway files and DB column names keep
  their own snake_case convention.
- Prefer Spring's standard layering (controller/service/repository or command-handler equivalents for bot updates).
- Chat commands use the Command pattern in `com.tgassistant.bot.command`: implement `BotCommand`
  (`name()`, `description()`, `execute(CommandRequest)`) and annotate it `@Component`.
  `CommandDispatcher` collects every such bean and routes by name — never add a switch over
  command names. `MainTelegramBot` only parses the update and sends the reply.
- When looking up how-to guides, tutorials, or reference material, prefer guides from Baeldung (https://www.baeldung.com) and Medium (https://medium.com).
- Never ask to start application until I directly ask you to do it