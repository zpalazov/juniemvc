# Add Customer — Task List

- [x] 1. Define the Customer domain model
  - [x] 1.2 Create the Customer JPA entity with fields: id PK, name (required), email, phone, addressLine1 (required), addressLine2, city (required), state (required), postalCode (required); add validation annotations (e.g., @NotBlank, @Email, @Pattern).
  - [x] 1.3 Establish one-to-many relationship from Customer to BeerOrder (List<BeerOrder> orders); if BeerOrder exists, add many-to-one back-reference to Customer; set proper owning side (BeerOrder owns FK), cascade, and fetch strategies.

- [x] 2. Plan database schema and Flyway migration
  - [x] 2.1 Create a Flyway SQL migration under src/main/resources/db/migration with proper sequential version (e.g., V3__add_customers_and_relation.sql).
  - [x] 2.2 In the migration: create customers table (all fields + auditing timestamps) and add customer_id FK column to beer_orders (or equivalent) if not present; create indexes on customer_id and frequently queried fields (email, phone if needed).
  - [x] 2.3 Use appropriate SQL types: UUID (or project-convention ID), VARCHAR for strings, numeric/text for phone per decision, timestamp (or equivalent) for created/updated.
  - [x] 2.4 Add constraints: NOT NULL on required fields; optionally UNIQUE on email if business rules require (confirm before enforcing).

- [ ] 3. Introduce DTOs for API/service layers
  - [x] 3.1 Create CreateCustomerRequest and UpdateCustomerRequest with Jakarta validation; do not expose internal IDs in create requests.
  - [x] 3.2 Create CustomerResponse with public fields including id and timestamps.
  - [ ] 3.3 Consider CustomerSummaryResponse for list views if pagination needs a lighter payload.

- [ ] 4. Mapping with MapStruct
  - [x] 4.1 Add MapStruct dependency and processor (kapt/ksp for Kotlin) in build.gradle.kts, aligning versions with Kotlin and Spring Boot.
  - [x] 4.2 Create CustomerMapper with methods: entityToResponse, entitiesToResponses, createRequestToEntity, updateRequestOntoEntity (@MappingTarget), and entityToSummary (if used).
  - [x] 4.3 Configure null handling and date/time mappings; handle phone/string conversions if necessary.
  - [x] 4.4 Verify annotation processing configuration works for Kotlin (kapt or ksp).

- [ ] 5. Repository layer
  - [x] 5.1 Create CustomerRepository extending JpaRepository<Customer, UUID>.
  - [x] 5.2 Add minimal finder methods as needed (e.g., findByEmail), keeping surface small initially.

- [x] 6. Service layer with clear transaction boundaries
  - [x] 6.1 Create CustomerService with methods: create(CreateCustomerDto), getById(UUID), list(Pageable), update(UUID, UpdateCustomerDto), delete(UUID).
  - [x] 6.2 Annotate write methods with @Transactional and read methods with @Transactional(readOnly = true).
  - [x] 6.3 Implement business rules (e.g., prevent duplicate emails if unique) leveraging repository and mapper.
  - [x] 6.4 Throw NotFoundException when customer is missing; return DTOs from service.
  - [x] 6.5 Make sure service layer DTOs are different from persistence entities and rest DTOs. Use mapping layer to convert between them.

- [ ] 7. Web layer (REST Controller)
  - [x] 7.1 Create CustomerController under /api/v1/customers.
  - [x] 7.2 Implement POST /api/v1/customers: create customer; return 201 Created with Location header and CustomerResponse body.
  - [x] 7.3 Implement GET /api/v1/customers/{id}: fetch by id; return 200 with CustomerResponse.
  - [x] 7.4 Implement GET /api/v1/customers: paginated list (Page<CustomerResponse> or wrapper with content + metadata).
  - [x] 7.5 Implement PUT /api/v1/customers/{id}: full update; return 200 with updated CustomerResponse.
  - [ ] 7.6 Implement PATCH /api/v1/customers/{id}: optional partial update if needed; otherwise skip.
  - [x] 7.7 Implement DELETE /api/v1/customers/{id}: delete; return 204 No Content.
  - [x] 7.8 Apply @Valid on request bodies; use ResponseEntity and appropriate status codes; rely on global exception handling for 404/validation errors.

- [ ] 8. OpenAPI specification updates
  - [x] 8.1 Under openapi-starter/openapi/paths, add customers.yaml (and optionally customers_{id}.yaml) for endpoints.
  - [x] 8.2 Under components/schemas, add CustomerRequest.yaml, CustomerUpdateRequest.yaml, CustomerResponse.yaml (and optionally CustomerPage.yaml or reuse generic Page if present).
  - [x] 8.3 Wire new paths in openapi-starter/openapi/openapi.yaml: add /api/v1/customers and /api/v1/customers/{id} entries referencing new path files; reuse Problem response and common headers; keep naming conventions.
  - [x] 8.4 Run redocly lint locally and resolve any schema/reference issues (address warnings as feasible).

- [ ] 9. Persistence mapping with BeerOrder
  - [x] 9.1 Update BeerOrder entity to include many-to-one relationship to Customer with @JoinColumn(name = "customer_id") if not already present.
  - [ ] 9.2 Decide fetch strategies (LAZY for collections); ensure OSIV is disabled and use fetch joins where required.

- [ ] 10. Validation and constraints
  - [x] 10.1 Add Jakarta validation annotations on DTOs to mirror DB constraints (NOT NULL, formats).
  - [ ] 10.2 Consider custom validators for state or postal code formats if business rules exist; otherwise use basic @Pattern.

- [x] 11. Testing strategy and coverage
  - [x] 11.1 Repository: add @DataJpaTest slice tests (persist/find/constraints, relation to beer orders, finder methods); ensure Flyway initializes schema.
  - [x] 11.2 Service: add unit tests with Mockk (or Mockito) to cover success, not found, duplicate email rule; verify transactional boundaries if observable.
  - [x] 11.3 Web: add MockMvc slice tests for JSON, validation errors, 201/200/204 statuses, pagination params, and Problem Details error mapping.
  - [x] 11.4 If Testcontainers are used elsewhere, align approach; otherwise rely on an in-memory DB consistent with Flyway scripts.

- [x] 12. Configuration updates
  - [x] 12.1 Ensure spring.jpa.open-in-view=false in application properties (OSIV disabled).
  - [x] 12.2 Enable auditing via @EnableJpaAuditing configuration class; set timezone/formatting preferences if needed.
  - [x] 12.3 Ensure Flyway is enabled (spring.flyway.enabled=true) and using default locations.

- [x] 13. Gradle and tooling
  - [x] 13.1 Verify build.gradle.kts includes: spring-boot-starter-data-jpa, flyway-core, validation starter, jackson-module-kotlin, mapstruct and mapstruct-processor (with kapt/ksp), and target compatibilities.
  - [x] 13.2 Add/verify test dependencies: spring-boot-starter-test, springmockk (or mockito-kotlin), and spring-security-test if needed.

- [ ] 14. Data migration and backward compatibility
  - [x] 14.1 Ensure the new migration version follows existing sequencing and is idempotent.
  - [x] 14.2 If BeerOrder already has data, make customer_id nullable initially or provide backfill strategy/defaults as needed.
  - [x] 14.3 Add foreign key constraints with appropriate ON DELETE behavior (RESTRICT or SET NULL) per business rule; adjust delete behavior in service accordingly.

- [ ] 15. Logging, exceptions, and observability
  - [x] 15.1 Use SLF4J for logging in service/controller; avoid logging sensitive data.
  - [x] 15.2 Reuse or extend GlobalExceptionHandler to map NotFoundException and validation errors to Problem Details.
  - [ ] 15.3 Add actuator exposure if metrics/timers are added for customer operations.

- [ ] 16. Documentation and examples
  - [ ] 16.1 Update README or API documentation references as needed.
  - [ ] 16.2 Provide sample payloads in OpenAPI components/examples.

- [ ] 17. Post-implementation verification
  - [ ] 17.1 Run unit and integration tests locally and ensure all pass.
  - [ ] 17.2 Verify OpenAPI docs via redocly preview and any in-app Swagger/Docs if present.
