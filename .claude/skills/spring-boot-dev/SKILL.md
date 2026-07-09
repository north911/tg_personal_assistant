---
name: spring-boot-dev
description: Advice and conventions for developing this Telegram-assistant app with Spring Boot 4.1 (Java 25), Spring Data JPA, SQLite, and the telegrambots starter. Use when adding features, wiring beans, writing JPA entities/repositories, handling bot updates, writing config, or setting up Docker for this project.
---

# Developing with Spring Boot in this project

Practical guidance for this repo: **Spring Boot 4.1.0 on Java 25**, persistence via
**Spring Data JPA over SQLite**, Telegram integration via the
**`telegrambots-spring-boot-starter`** (v6.7.0). Deployment target is Docker.

Follow this whenever you add a feature, wire a bean, touch persistence, or handle a
bot update.

## Layering

Keep Spring's standard layers separated. For a bot, updates flow through:

```
Telegram update → Bot/handler (adapter) → Service (business logic) → Repository (JPA) → SQLite
```

- **Bot/handler** (`com.tgassistant.bot`): parse the incoming update, delegate to a
  service, format the reply. No business logic or persistence here.
- **Service** (`com.tgassistant.service`): the actual logic. `@Service`, stateless,
  transactional where it writes.
- **Repository** (`com.tgassistant.repository`): `interface X extends JpaRepository<Entity, Id>`.
  Don't hand-write CRUD Spring generates for you.
- **Entity/domain** (`com.tgassistant.domain` or `.model`): `@Entity` classes.

Put new classes in a package that matches their role rather than piling everything into `bot`.

## Dependency injection

- **Prefer constructor injection** over `@Autowired` on fields. It makes dependencies
  explicit, supports `final` fields, and keeps the class testable without Spring.

  ```java
  @Service
  public class ReminderService {
      private final ReminderRepository repository;

      public ReminderService(ReminderRepository repository) {
          this.repository = repository;
      }
  }
  ```

- A single constructor needs no `@Autowired` annotation; Spring injects it automatically.
- Don't `new` up beans that Spring manages — inject them.

## Persistence (Spring Data JPA + SQLite)

- Model tables as `@Entity` classes; expose data access through `JpaRepository`
  interfaces. Use derived query methods (`findByChatId`, `existsByName`) before
  reaching for `@Query`.
- **Wrap multi-step writes in `@Transactional`** (on the service method). Reads that
  touch lazy associations also need an open transaction.
- SQLite is single-writer. Keep transactions short; avoid long-held write locks. The
  DB file lives at `data/tg-personal-assistant.db` — see `application.properties`.
- Let Hibernate manage schema in dev via `spring.jpa.hibernate.ddl-auto`, but do not
  rely on `create`/`create-drop` for anything whose data must survive a restart —
  prefer `update` (dev) and plan real migrations before production.
- Choose ID strategy deliberately: `@GeneratedValue(strategy = IDENTITY)` maps well to
  SQLite's `AUTOINCREMENT`.

## Telegram bot specifics

- The starter **auto-registers any Spring bean that is a `LongPollingBot`/`AbilityBot`** —
  no manual `TelegramBotsApi.registerBot(...)`. Just make the bot a `@Component`.
- Keep the bot token and username **out of source**. Read them from config
  (`@Value("${telegram.bot.token}")` or a `@ConfigurationProperties` bean) backed by
  environment variables — never hard-code.
- Keep update handling thin: the bot class routes; a service does the work.

## Configuration & secrets

- **Never commit tokens/secrets.** Externalize via environment variables (loaded through
  Docker Compose `.env`) and reference them in `application.properties`
  (`telegram.bot.token=${TELEGRAM_BOT_TOKEN}`).
- Group related settings into a `@ConfigurationProperties`-annotated record/class
  instead of scattering `@Value` everywhere.
- Use Spring profiles (`application-dev.properties`, `application-docker.properties`)
  to vary behavior per environment.

## Testing

- `./mvnw test`. Use `@SpringBootTest` sparingly (slow) — prefer plain unit tests on
  services with mocked repositories.
- For repository/JPA tests use `@DataJpaTest` (the `spring-boot-starter-data-jpa-test`
  dependency is already present) against an in-memory or throwaway SQLite file.
- Test services in isolation by constructing them with mock/fake dependencies —
  another reason to use constructor injection.

## Build & run

- Build: `./mvnw clean package`
- **Local run needs JDK 25**, which is not installed on this machine (only JDK 13/8).
  So run/build via **Docker** or a JDK 25+ machine — don't assume `spring-boot:run`
  works locally here.
- The SQLite DB file must live in a **mounted Docker volume** so data persists across
  container rebuilds.

## Quick checklist before finishing a change

- [ ] New class is in a package matching its layer, not dumped in `bot`.
- [ ] Dependencies injected via constructor, fields `final`.
- [ ] Writes are `@Transactional`; transactions are short.
- [ ] No secrets/tokens in source — pulled from env/config.
- [ ] Repository uses `JpaRepository` + derived queries, not hand-rolled JDBC.
- [ ] Added/updated a test where behavior changed.
