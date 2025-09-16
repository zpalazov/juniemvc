Title: Plan to Introduce BeerDto and Separate Web/Persistence Layers

Overview
This plan implements the requirements defined in prompts/add-dtos/requirements.md. It introduces a BeerDto, MapStruct-based mapping, and refactors controller and service layers to operate on DTOs rather than JPA entities, following the Kotlin Spring Boot guidelines provided.

Prerequisites and Assumptions
- MapStruct dependencies are not yet configured; build changes are documented but not executed here.
- Existing project compiles and tests run with current entity-based API.
- JPA entity BeerEntity already exists; repository BeerRepository provides basic CRUD.

Step-by-Step Implementation Plan

1) Add DTO package and data class
- Create package: com.hopman.leaning.juniemvc.model
- Add file BeerDto.kt with fields and validation per requirements:
  - id: Int? (output-only)
  - name: String (required, @field:NotBlank)
  - style: String (required, @field:NotBlank)
  - upc: String (required, @field:NotBlank)
  - quantityOnHand: Int? (optional, consider @field:PositiveOrZero if domain allows)
  - price: BigDecimal (required, @field:NotNull and optionally @field:Positive)
  - version: Int? (output-only)
  - createdDate: LocalDateTime? (output-only)
  - updateDate: LocalDateTime? (output-only)
- Keep the class as a Kotlin data class under the model package. Use restricted visibility (internal) if consumed only within the module.

2) Introduce MapStruct mapper
- Create package: com.hopman.leaning.juniemvc.mapper
- Add interface BeerMapper.kt with:
  - @Mapper(componentModel = "spring")
  - fun toEntity(dto: BeerDto): BeerEntity
    • Ignore: id, version, createdDate, updateDate
  - fun toDto(entity: BeerEntity): BeerDto
  - fun updateEntityFromDto(dto: BeerDto, @MappingTarget entity: BeerEntity): BeerEntity
    • Update only: name, style, upc, quantityOnHand, price
    • Never overwrite: id, version, createdDate, updateDate
- Provide explicit @Mappings/@Mapping annotations for ignored fields.
- If enums/types differ (e.g., style), add auxiliary mapping or custom methods.

3) Prepare Gradle configuration (informational)
- In build.gradle.kts (not applied now):
  - plugins { kotlin("kapt") }
  - dependencies {
      implementation("org.mapstruct:mapstruct:<version>")
      kapt("org.mapstruct:mapstruct-processor:<version>")
    }
  - Ensure Kotlin compilation uses KAPT, and set Java compatibility for annotation processing.
- Document version (e.g., 1.5.x) per project standards.

4) Refactor Service API to use DTOs
- File: com.hopman.leaning.juniemvc.service.BeerService
  - Change signatures to:
    • fun create(dto: BeerDto): BeerDto
    • fun findAll(): List<BeerDto>
    • fun findById(id: Int): BeerDto?
    • fun update(id: Int, dto: BeerDto): BeerDto?
    • fun delete(id: Int): Boolean
- File: com.hopman.leaning.juniemvc.service.BeerServiceImpl
  - Inject BeerRepository and BeerMapper via constructor injection.
  - Implement methods:
    • create: mapper.toEntity(dto) -> repo.save(entity) -> mapper.toDto(saved)
    • findAll: repo.findAll().map(mapper::toDto)
    • findById: repo.findById(id).map(mapper::toDto).orElse(null)
    • update: repo.findById(id).map { mapper.updateEntityFromDto(dto, it); repo.save(it) }.map(mapper::toDto).orElse(null)
    • delete: if exists then delete and return true else false
- Ensure transactional boundaries: readOnly for find*, @Transactional for create/update/delete.

5) Refactor Controller to use DTOs
- File: com.hopman.leaning.juniemvc.rest.BeerController
  - Replace BeerEntity types in method signatures with BeerDto.
  - Endpoints:
    • POST /api/beers: @Valid @RequestBody BeerDto -> returns ResponseEntity.created(URI("/api/beers/{id}")).body(dto)
    • GET /api/beers: returns List<BeerDto>
    • GET /api/beers/{id}: returns BeerDto or 404
    • PUT /api/beers/{id}: @Valid BeerDto -> returns BeerDto or 404
    • DELETE /api/beers/{id}: 204 or 404
- Do not expose JPA entities. Ensure proper ResponseEntity statuses.

6) Validation and error handling
- Rely on Spring’s default MethodArgumentNotValidException handling for 400 responses on invalid input.
- Optionally add a @RestControllerAdvice later (out of scope per requirements).

7) Mapping rules verification
- Confirm @Mapping(ignore = true) on inbound-only ignored fields (id, version, createdDate, updateDate) for toEntity and updateEntityFromDto.
- Ensure toDto maps all BeerDto fields.

8) Tests impact (plan only)
- Update controller tests to post/put JSON matching BeerDto and to expect DTO-shaped responses.
- Update service tests to assert mapping and DTO usage.
- Keep repository tests unchanged.
- Use @SpringBootTest(webEnvironment = RANDOM_PORT) for integration tests if present; otherwise, slice tests for controller with @WebMvcTest and mock service.

9) Incremental migration checklist
- Compile after adding DTO and mapper (requires MapStruct config) or use stub mapper temporarily if compiling without MapStruct.
- Refactor service interface, implementation, and controller in one cohesive change to avoid type conflicts.
- Verify application.yml already aligns with guidelines (OSIV off recommended; if not, consider setting spring.jpa.open-in-view=false in a separate change).

10) Non-functional considerations
- Constructor injection throughout; avoid lateinit/field injection.
- Restrict visibility where applicable (internal classes where not part of public API).
- Logging via SLF4J; avoid println.

11) Acceptance criteria checklist
- Controller no longer exposes BeerEntity.
- Service API uses BeerDto.
- POST returns 201 with Location header and BeerDto body.
- Inbound mapping ignores id/version/timestamps.
- Code compiles with MapStruct dependencies present.

12) Backout plan
- If issues arise, revert controller and service signatures to entity-based versions temporarily and keep DTO work on a feature branch until MapStruct is configured.
