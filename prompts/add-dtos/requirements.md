Title: Introduce a Single BeerDto for Beer API and Separate Web/Persistence Layers

Objective
- Replace direct exposure of JPA entities in the web layer with a single BeerDto used for both requests and responses.
- Introduce mapping between BeerDto and entities using MapStruct with clear rules for ignored/generated fields.
- Keep service layer APIs DTO-oriented to decouple from persistence and ease future changes.

Scope
- Applies to Beer domain only: controller, service interface/impl, and supporting packages.

Deliverables
1) Model DTOs
- Package: com.hopman.leaning.juniemvc.model
- Data classes:
  - BeerDto
    - id: Int? (output-only; ignored on inbound)
    - name: String (required)
    - style: String (required)
    - upc: String (required, unique)
    - quantityOnHand: Int? (optional)
    - price: BigDecimal (required)
    - version: Int? (output-only; ignored on inbound)
    - createdDate: LocalDateTime? (output-only; ignored on inbound)
    - updateDate: LocalDateTime? (output-only; ignored on inbound)
    - Validation: add Jakarta validation annotations for inbound where appropriate (e.g., @NotBlank for strings, @NotNull for price, positive constraints if applicable).

2) Mapper
- Package: com.hopman.leaning.juniemvc.mapper
- Use MapStruct to convert between com.hopman.leaning.juniemvc.entity.BeerEntity and BeerDto.
- Define an interface BeerMapper with methods:
  - toEntity(dto: BeerDto): BeerEntity
    - Ignore/never map: id, version, createdDate, updateDate.
  - toDto(entity: BeerEntity): BeerDto
  - updateEntityFromDto(dto: BeerDto, @MappingTarget entity: BeerEntity): BeerEntity
    - Update mutable business fields only: name, style, upc, quantityOnHand, price.
    - Never overwrite: id, version, createdDate, updateDate.
- Configure componentModel = "spring" for constructor injection usage.
- If MapStruct is not yet configured in build, specify expected gradle setup in a comment in this file; implementation of build changes is out of scope for these requirements.

3) Service Layer Changes
- Change BeerService to be DTO-oriented, using BeerDto everywhere:
  - fun create(dto: BeerDto): BeerDto
  - fun findAll(): List<BeerDto>
  - fun findById(id: Int): BeerDto?
  - fun update(id: Int, dto: BeerDto): BeerDto?
  - fun delete(id: Int): Boolean
- BeerServiceImpl will:
  - Convert BeerDto -> BeerEntity via mapper for create (ignoring id/version/timestamps).
  - Persist via BeerRepository.
  - Convert stored entity -> BeerDto for returns.
  - For update: load entity, apply values using mapper.updateEntityFromDto, save, return dto; return null if not found.

4) Controller Changes
- Package: com.hopman.leaning.juniemvc.rest (existing)
- Update BeerController to operate on BeerDto:
  - POST /api/beers accepts @Valid @RequestBody BeerDto, returns 201 Created with body BeerDto and Location header /api/beers/{id}.
  - GET /api/beers returns List<BeerDto>.
  - GET /api/beers/{id} returns 200 with BeerDto or 404 if not found.
  - PUT /api/beers/{id} accepts @Valid BeerDto, returns 200 with BeerDto or 404 if not found.
  - DELETE /api/beers/{id} returns 204 No Content or 404 if not found.
- Do not expose JPA entities directly from controller methods.

5) Validation and Error Handling
- Apply Jakarta Validation on BeerDto inbound fields.
- If validation fails, rely on Spring’s default 400 Bad Request handling; global ProblemDetail handling may be introduced later and is out of scope here.

6) Design and Coding Guidelines
- Constructor injection for all Spring components (no field/setter injection).
- Keep visibility restricted where reasonable (e.g., classes can be internal if not part of public API).
- Disable OSIV is already recommended at config level; no change required here.
- Logging via SLF4J if needed; avoid println.

7) Mapping Rules Summary
- From BeerDto to Entity (create/update):
  - Map: name, style, upc, quantityOnHand, price.
  - Ignore: id, version, createdDate, updateDate (managed by DB/Hibernate).
- From Entity to BeerDto: map all fields listed in BeerDto.

8) Package Layout
- com.hopman.leaning.juniemvc.entity – unchanged JPA entities
- com.hopman.leaning.juniemvc.repo – repositories
- com.hopman.leaning.juniemvc.model – new DTO (BeerDto)
- com.hopman.leaning.juniemvc.mapper – new MapStruct mapper (BeerMapper)
- com.hopman.leaning.juniemvc.service – updated service interface & impl
- com.hopman.leaning.juniemvc.rest – controller using DTO

9) Acceptance Criteria
- Controller methods no longer reference BeerEntity in signatures.
- Service interface methods use BeerDto as specified.
- Mapping ignores id, version, createdDate, updateDate on inbound paths.
- POST returns 201 with Location: /api/beers/{id} and response body matching BeerDto schema.
- All existing tests are updated or new tests are added to cover DTO-based API behavior; for this task, only the requirements are defined—test changes are not implemented now.
- Code compiles once MapStruct dependencies are added (build changes may be required and are outside the scope of this requirements file but must be documented below).

10) Build Notes (informational)
- Expected Gradle setup for MapStruct (Kotlin + KAPT) when implementation happens:
  - plugins: kotlin-kapt
  - dependencies: implementation("org.mapstruct:mapstruct:<version>"); kapt("org.mapstruct:mapstruct-processor:<version>")
  - kotlin { sourceSets { ... } } as needed
  - Set mapstruct.defaultComponentModel = spring via @Mapper(componentModel = "spring")

Out of Scope
- Implementing the actual code changes, tests, or Gradle configuration in this step; this file defines the requirements only.
