# Junie Project Guidelines

A quick, practical guide for new developers joining this Kotlin/Spring Boot project.

## 1) Tech Stack & Layout
- Language: Kotlin (JDK 21)
- Framework: Spring Boot 3.x (Web, Validation, Data JPA)
- DB: H2 (runtime), Flyway for migrations
- Mapping: MapStruct (kapt)
- Build: Gradle Kotlin DSL (Gradle Wrapper)
- Tests: JUnit 5

Structure:
- build.gradle.kts, settings.gradle.kts
- src/main/kotlin/...: application, controllers, services, repositories, entities
- src/main/resources: configuration (application.yml), db/migration (Flyway)
- src/test/kotlin/...: tests (unit, slice, integration)

Key packages:
- com.hopman.leaning.juniemvc
  - rest: controllers
  - service: interfaces + impl
  - repo: Spring Data repositories
  - entity: JPA entities

## 2) Getting Started
- Requirements: JDK 21, Internet access. No global Gradle needed; use the wrapper.
- First build (downloads dependencies):
  - Unix/macOS: ./gradlew build
  - Windows: gradlew.bat build

Run the app:
- ./gradlew bootRun

Build runnable jar:
- ./gradlew bootJar  (jar in build/libs)

## 3) Running Tests
- All tests: ./gradlew test
- Single test class: ./gradlew test --tests "com.hopman.leaning.juniemvc.service.BeerServiceTest"
- Single test method: ./gradlew test --tests "*BeerServiceTest.someMethodName"
- Clean then test: ./gradlew clean test

Notes:
- JUnit Platform is enabled; use @Test from org.junit.jupiter.api.
- For logging debug in tests, print with prefix [DEBUG_LOG] to help CI logs.

## 4) Executing Scripts & Utilities
- Custom Gradle tasks can be added to build.gradle.kts.
- Kotlin/Java one-offs can be run via:
  - Temporary main class under src/main/kotlin (remember to remove or guard behind a profile).
- Spring Boot command-line args: ./gradlew bootRun --args='--server.port=9090'

## 5) Database & Migrations
- Default profile uses in-memory H2 (see src/main/resources/application.yml).
- Flyway runs automatically on startup. Place migrations under src/main/resources/db/migration using versioned filenames (e.g., V1__init.sql).

## 6) Coding Practices
- Kotlin + Spring
  - Favor constructor injection.
  - Keep controllers thin; business logic in services.
  - Entities: use val for immutable fields when possible; default no-args handled by @Entity + allOpen plugin.
  - Null-safety: project enables -Xjsr305=strict.
- MapStruct
  - Place mappers in main; kapt generates sources into build/generated/sources/kapt/main (already added to sourceSets).
- Validation
  - Use javax/jakarta validation annotations on DTOs; validate at controller boundaries.

## 7) Testing Practices
- Unit tests for services (mock repos where appropriate).
- Slice tests for controllers using @WebMvcTest when suitable.
- Data tests with @DataJpaTest or H2; keep them independent and idempotent.
- Name tests clearly and assert expected HTTP status/payloads for REST.

## 8) Troubleshooting
- Delete caches and rebuild: ./gradlew clean build --refresh-dependencies
- Kapt issues: ./gradlew cleanKotlin compileKotlin
- Port conflicts: change server.port or stop existing process.
- Verify Java version: java -version should be 21.

## 9) Conventions
- Use Gradle wrapper in all commands.
- Keep guidelines concise and update when stack changes.
- Keep HELP.md for user-facing notes; this file is developer-focused.
