# CLAUDE.md

This file provides guidance to Claude Code when working in this repository.

## Project Overview

A personal Telegram bot / assistant.

## Tech Stack

- **Language**: Java 25 (Java 24 was requested, but Spring Initializr only offers 17/21/25/26 — 25 is the closest LTS)
- **Framework**: Spring Boot 4.1.0
- **Persistence**: Spring Data JPA + `org.xerial:sqlite-jdbc`, with `hibernate-community-dialects` (`org.hibernate.community.dialect.SQLiteDialect`) since core Hibernate has no built-in SQLite dialect
- **Web**: `spring-boot-starter-webmvc` (Boot 4.1 renamed `spring-boot-starter-web`)
- **Database**: SQLite (file at `data/tg-personal-assistant.db`, configured in `application.properties`)
- **Deployment**: Docker (the entire app runs in containers)
- **Build tool**: Maven (generated via start.spring.io)

## Development

- Build: `./mvnw clean package`
- Run locally: `./mvnw spring-boot:run` (requires a local JDK 25 — not installed on this machine; only JDK 13/8 are present, so local builds must happen in Docker or on a machine with JDK 25+)
- Run via Docker: TBD — no Dockerfile/compose file yet
- Tests: `./mvnw test`

## Conventions

- Keep configuration (bot token, secrets) out of source control — use environment variables / `.env` loaded via Docker Compose, never commit tokens.
- SQLite database file should live in a mounted Docker volume so data persists across container rebuilds.
- Prefer Spring's standard layering (controller/service/repository or command-handler equivalents for bot updates) — update this once bot-specific code exists.
