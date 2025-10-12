# Spring Boot Guidelines (Kotlin)

## Prefer Constructor Injection over Field/Setter Injection
* Declare mandatory dependencies as constructor parameters and store them in `val` properties.
* Spring will auto-detect the primary constructor; no need to add `@Autowired`.
* Avoid field/setter injection (including `lateinit var`) in production code.

**Explanation:**

* With Kotlin, making dependencies `val` properties initialized via the primary constructor ensures immutability and a properly initialized state without framework magic.
* It simplifies testing because you can instantiate the class with plain constructors.
* Constructor-based injection makes dependencies explicit and self-documenting.
* Spring Boot provides builder extension points such as `RestClient.Builder`, `ChatClient.Builder`, etc. Use constructor-injection to customize and build the final dependency.

```kotlin
@Service
class OrderService(
    private val orderRepository: OrderRepository,
    builder: RestClient.Builder
) {
    private val restClient: RestClient = builder
        .baseUrl("http://catalog-service.com")
        .requestInterceptor(ClientCredentialTokenInterceptor())
        .build()

    // ... methods
}
```

## Prefer restricted visibility for Spring components
* In Kotlin, top-level declarations and classes are `public` by default. Prefer the most restrictive visibility that works for your use case (e.g., `internal`).
* Controller handler methods do not need to be `public`; default visibility is fine.
* `@Configuration` classes and `@Bean` methods can also use restricted visibility where appropriate.

**Explanation:**

* Restricting visibility improves encapsulation and hides implementation details from other modules/packages.
* Spring's component scanning and reflection can work with non-public members; keep APIs as small as possible.

## Organize Configuration with Typed Properties
* Group application-specific configuration under a common prefix in `application.yml`.
* Bind them to a Kotlin `@ConfigurationProperties` class, using constructor binding and validation annotations.
* Prefer environment variables for environment differences instead of profiles.

**Explanation:**

* Centralizing configuration in a single properties class avoids scattered `@Value("${…}")` injections.
* Validated config fails fast on startup, preventing misconfiguration at runtime.

```kotlin
@ConstructorBinding
@ConfigurationProperties(prefix = "catalog")
data class CatalogProperties(
    @field:jakarta.validation.constraints.NotBlank
    val baseUrl: String,
    val timeout: Duration = Duration.ofSeconds(5)
)

@Configuration
@EnableConfigurationProperties(CatalogProperties::class)
class CatalogConfig
```

Note: With Spring Boot 3, explicit `@ConstructorBinding` is optional when using constructor parameters.

## Define Clear Transaction Boundaries
* Define each service-layer method as a transactional unit.
* Annotate query-only methods with `@Transactional(readOnly = true)`.
* Annotate data-modifying methods with `@Transactional`.
* Keep transactional scope minimal.

**Explanation:**

* Single unit of work, connection reuse, read-only optimizations, and reduced contention all apply equally in Kotlin.

```kotlin
@Service
class CustomerService(private val repo: CustomerRepository) {

    @Transactional(readOnly = true)
    fun findById(id: UUID): CustomerDto =
        repo.findById(id).orElseThrow { NotFoundException("Customer $id") }
            .toDto()

    @Transactional
    fun rename(id: UUID, cmd: RenameCustomerCommand): CustomerDto {
        val entity = repo.findById(id).orElseThrow { NotFoundException("Customer $id") }
        entity.name = cmd.newName
        return repo.save(entity).toDto()
    }
}
```

## Disable Open Session in View Pattern
* With Spring Data JPA, disable OSIV by setting `spring.jpa.open-in-view=false` in `application.yml`.

**Explanation:**

* Prevents unexpected lazy loading at serialization time, avoids N+1 selects, and surfaces missing fetches early.

## Separate Web Layer from Persistence Layer
* Do not expose JPA entities directly from controllers.
* Define explicit request and response DTOs as Kotlin `data class`es.
* Apply Jakarta Validation annotations on request DTOs.

**Explanation:**

* DTOs decouple your API from the database schema and declare exactly what is accepted/returned.
* MapStruct works with Kotlin, or you can write simple mappers/extension functions.

```kotlin
data class CreateOrderRequest(
    @field:jakarta.validation.constraints.NotBlank
    val customerId: String,
    val items: List<OrderItemRequest>
)

data class OrderResponse(
    val id: String,
    val status: String,
    val total: BigDecimal
)
```

## Follow REST API Design Principles
* Versioned, resource-oriented URLs: `/api/v{version}/resources` (e.g., `/api/v1/orders`).
* Consistent patterns for collections and sub-resources (e.g., `/posts` and `/posts/{slug}/comments`).
* Return explicit HTTP status codes via `ResponseEntity` or use `@ResponseStatus` on exceptions.
* Use pagination for large collections.
* JSON top-level should be an object for forward compatibility.
* Be consistent with snake_case or camelCase in JSON.

**Explanation:**

* Predictable, discoverable APIs and reliable integrations.

```kotlin
@RestController
@RequestMapping("/api/v1/orders")
class OrderController(private val service: OrderService) {

    @GetMapping
    fun list(pageable: Pageable): Page<OrderResponse> = service.list(pageable)

    @PostMapping
    fun create(@Valid @RequestBody req: CreateOrderRequest): ResponseEntity<OrderResponse> {
        val created = service.create(req.toCommand())
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }
}
```

## Use Command Objects for Business Operations
* Create purpose-built command objects (Kotlin `data class`) like `CreateOrderCommand`.
* Accept these commands in service methods to drive workflows.

**Explanation:**

* Clarifies inputs and ownership of generated fields (IDs, timestamps).

```kotlin
data class CreateOrderCommand(
    val customerId: UUID,
    val items: List<OrderItem>
)
```

## Centralize Exception Handling
* Create a global handler annotated with `@RestControllerAdvice` using `@ExceptionHandler` methods.
* Return consistent error responses, ideally Problem Details (RFC 9457). In Spring 6, you can use `ProblemDetail`.

**Explanation:**

* Avoids scattering try/catch and provides a uniform error contract.

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException, request: HttpServletRequest): ResponseEntity<ProblemDetail> {
        val pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        pd.title = "Resource not found"
        pd.detail = ex.message
        pd.setProperty("path", request.requestURI)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd)
    }
}
```

## Actuator
* Expose only essential actuator endpoints (`/health`, `/info`, `/metrics`) without authentication; secure the rest.
* In non-production (DEV/QA), you may expose additional endpoints (`/beans`, `/loggers`) for diagnostics.

## Internationalization with ResourceBundles
* Externalize user-facing text into `messages_*.properties` files instead of hardcoding strings.

**Explanation:**

* Enables multiple languages and dynamic locale selection.

## Test Guidelines
## Use Testcontainers for integration tests
* Spin up real services (databases, brokers, etc.) with Testcontainers in integration tests.
* Use fixed, explicit image versions (avoid `latest`).

**Explanation:**

* Reduces environment inconsistencies and increases confidence.

## For simple JPA tests, use slice tests with `@DataJpaTest`
* Use H2 in-memory database for fast tests.

## Use random port for integration tests
* Start the app on a random port to avoid conflicts:

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MyHttpTest
```

**Explanation:**

* Prevents port conflicts in parallel CI builds.

## Unit tests
* Use JUnit 5 (Jupiter) and Mockk for unit tests.

## Logging
* Use a proper logging framework; do not use `println` for application logs. Prefer SLF4J with Logback/Log4j2.
* Never log sensitive data.
* Guard expensive log calls.

```kotlin
private val logger = org.slf4j.LoggerFactory.getLogger(MyService::class.java)

if (logger.isDebugEnabled) {
    logger.debug("Detailed state: {}", computeExpensiveDetails())
}

// Supplier-style with SLF4J 2.x fluent API
logger.atDebug()
    .setMessage("Detailed state: {}")
    .addArgument { computeExpensiveDetails() }
    .log()
```

**Explanation:**

* Flexible verbosity control, rich metadata (MDC), multiple outputs/formats, and better analysis tooling.

## Flyway Migrations with Spring Boot
* Default locations: place SQL migrations under `src/main/resources/db/migration` (classpath:`db/migration`). Spring Boot auto-detects and runs them on startup when Flyway is on the classpath.
* Versioned migration naming: `V{version}__Description.sql` (double underscore). Examples: `V1__init_schema.sql`, `V2_1__add_orders_table.sql`.
* Repeatable migrations (optional): `R__Description.sql` re-run when their checksum changes. Example: `R__refresh_views.sql`.
* Keep migrations idempotent and small; avoid editing applied versioned scripts—add a new one instead.
* Use H2 complaint syntax for DB migrations.
* When altering a table to add a foreign key, add the new column first and then in the next SQL statement add the 
foreign key constraint.

---

# OpenAPI Documentation & Conventions

This project includes a modular OpenAPI 3.1 specification under `openapi-starter/openapi`. The spec is authored using file references to keep the definition organized and maintainable and is served/bundled with Redocly CLI.

Location overview
- Root spec: `openapi-starter/openapi/openapi.yaml`
- Paths (endpoints): `openapi-starter/openapi/paths/*.yaml`
- Components (reusable pieces): `openapi-starter/openapi/components/**` (e.g., `schemas`, `responses`, `headers`)
- Dev tooling: `openapi-starter/package.json` (scripts for preview, bundle, lint/test)

How file references are used
- The root `openapi.yaml` references individual path files:
  - Example mappings from the spec:
    - `/users/{username}` → `$ref: 'paths/users_{username}.yaml'`
    - `/user` → `$ref: 'paths/user.yaml'`
    - `/user/list` → `$ref: 'paths/user-status.yaml'`
    - `/echo` → `$ref: 'paths/echo.yaml'`
  - Each referenced path file contains the operations (get/post/put/delete), their parameters, request bodies, and responses.
- Path files then reference component files using relative paths:
  - Schemas: `$ref: '../components/schemas/User.yaml'`
  - Responses: `$ref: '../components/responses/Problem.yaml'`
  - Headers: `$ref: '../components/headers/ExpiresAfter.yaml'`

Path file naming conventions
- Use descriptive file names that reflect the API path.
  - Single-segment paths typically match the segment name: `/user` → `paths/user.yaml`.
  - Paths with parameters replace slashes with underscores and keep `{param}` braces: `/users/{username}` → `paths/users_{username}.yaml`.
  - Multi-segment paths can use either literal joining or a concise, descriptive name. The repository shows an example of a descriptive alias: `/user/list` → `paths/user-status.yaml`.
- Recommendation when adding new endpoints:
  - Prefer a direct, readable mapping (e.g., `/orders/{id}/items` → `paths/orders_{id}_items.yaml`).
  - If a direct mapping becomes unwieldy, choose a short, meaningful alias (document it in the PR).

Components structure and references
- Schemas: `components/schemas/*.yaml`
  - Example files: `User.yaml`, `Admin.yaml`, `Basic.yaml`, `Email.yaml`, `UserID.yaml`, `Problem.yaml`, `ExampleObject.yaml`, `Schema.yaml`.
  - Schemas compose via `$ref` and keywords like `allOf`, `anyOf`, and can define discriminators for polymorphism.
  - Example: `paths/user.yaml` request body discriminator maps to `../components/schemas/Admin.yaml` and `../components/schemas/Basic.yaml`.
- Responses: `components/responses/*.yaml`
  - Example: `Problem.yaml` wraps the `application/problem+json` schema by referencing `../schemas/Problem.yaml`.
- Headers: `components/headers/*.yaml`
  - Example: `ExpiresAfter.yaml` is referenced in response headers of `paths/echo.yaml` and others.
- Security schemes are defined inline under `components.securitySchemes` in `openapi.yaml` (e.g., `oauth2`, `apiKey`, `basic`).

Adding a new endpoint (quick recipe)
- Create a new file under `openapi-starter/openapi/paths/` with the chosen name.
- Define the operations and use `$ref` to reusable components for schemas/responses/headers.
- Add a `$ref` entry under `paths:` in `openapi-starter/openapi/openapi.yaml` that points to your new path file.

Testing (linting) the OpenAPI specification
- The project uses Redocly CLI via npm scripts in `openapi-starter/package.json`.
- Commands:
  1) cd openapi-starter
  2) npm install   # first time or when dependencies change
  3) npm test      # runs `redocly lint` against the OpenAPI definition
- Expected behavior:
  - The linter will validate structure, references, and recommended practices. Fix any reported issues in the referenced files.

Optional local preview and bundle
- Preview docs locally (Redoc):
  - cd openapi-starter && npm start   # runs `redocly preview-docs`
- Produce a bundled single-file spec:
  - cd openapi-starter && npm run build   # outputs `dist/bundle.yaml`

Notes
- Keep relative `$ref` paths correct when moving files (e.g., `../components/...` from `paths/*`).
- Prefer small, focused component files to maximize reuse.
- When introducing new naming patterns, be consistent and update this section if necessary.
