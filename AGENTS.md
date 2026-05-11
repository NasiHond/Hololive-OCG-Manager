# AGENTS.md

## Project snapshot
- Spring Boot 4.0.3 + Java 25 Gradle app (`build.gradle`, `settings.gradle`).
- Main entry point: `src/main/java/com/fhict/hololiveocgmanager/HololiveOcgManagerApplication.java`.
- No existing `README.md` or agent-specific instructions were found; treat this file as the workspace guide.

## Architecture you should preserve
- Layered flow is `controller -> service -> mapper/domain -> entity/repository`.
- Example path: `CardController` returns `CardResponse`, maps via `CardMapper`, reads `CardEntity` from `CardRepository`, and supports filter queries through `CardSpecification.withFilters(...)`.
- Keep the domain DTO/entity split intact; domain objects (`domain/Card.java`, `domain/User.java`) are not JPA entities.
- Preserve the project’s naming quirks when touching existing code (`Id`, `cardID`, `cardcolour`, `profileImageURL`) to avoid breaking mappers and JSON fields.

## Security and API conventions
- Security is stateless JWT: see `config/SecurityConfig.java`, `config/JwtAuthenticationFilter.java`, and `service/JwtService.java`.
- Public endpoints are deliberately limited: `/api/auth/**`, `POST /api/users`, `GET /api/cards/**`, `GET /api/decks/**`, `GET /api/collections/**`, and all `OPTIONS` requests.
- CORS is pinned to `http://localhost:5173` in both security config and controllers.
- Login uses `AuthController` + `AuthServiceImpl`; tokens are HS256 JWTs signed with `jwt.secret` from properties.

## Persistence and schema
- Production config targets PostgreSQL at `localhost:5432/postgres`; Flyway runs from `classpath:db/migration`.
- Schema changes live in `src/main/resources/db/migration/V1__init.sql` through `V5__drop_cardkeywords.sql`.
- `CardRepository` uses `@EntityGraph` to eagerly fetch card subgraphs (`keywords`, `cardtags`, `cardarts`, `cardcolour`, `cardtype`, `extra`) because the API serializes nested data.
- Collections/decks use `domain/Visibility.java` (`PUBLIC`, `PRIVATE`, `UNLISTED`) and the newer enum column mappings in `V4__visibility_to_enum.sql`.

## Scraper-specific behavior
- `service/CardScraperService.java` scrapes the Hololive official card site with Jsoup and persists cards, arts, costs, keywords, tags, and lookup rows.
- The scraper normalizes Japanese color tokens into stable keys (`red`, `blue - red`, `colorless`); avoid changing these maps unless you update downstream DB references too.
- `HololiveOcgManagerApplication.java` contains a commented startup scrape hook guarded by `app.scraper.run-on-startup`.

## Developer workflow
- Use the Gradle wrapper on Windows: `gradlew.bat test`, `gradlew.bat build`, `gradlew.bat bootRun`.
- Flyway during `build` is optional and only runs with `-PrunFlywayOnBuild=true`.
- Tests run with JUnit Platform; test config switches to H2 and disables Flyway (`src/test/resources/application.properties`).
- Current baseline note: `gradlew.bat test` fails in `service/CollectionServiceImpl.java:97` because `CollectionCardResponse.keyword(...)` is given a `Set<KeywordEntity>`; check that file first if you need a green build.

## Where to look first
- API surface: `src/main/java/com/fhict/hololiveocgmanager/controller/`
- Business logic: `src/main/java/com/fhict/hololiveocgmanager/service/`
- Mapping/shape conversions: `src/main/java/com/fhict/hololiveocgmanager/mapper/`
- Search logic: `src/main/java/com/fhict/hololiveocgmanager/specification/CardSpecification.java`
- Database evolution: `src/main/resources/db/migration/`
- Regression tests: `src/test/java/com/fhict/hololiveocgmanager/specification/CardSpecificationTest.java`


