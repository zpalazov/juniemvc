1. [x] Create DTO package and class
   - [x] Create package: com.hophman.learning.juniemvc.model
   - [x] Add BeerDto.kt as a Kotlin data class with fields:
     - [x] id: Int? (output-only)
     - [x] name: String (required) with @field:NotBlank
     - [x] style: String (required) with @field:NotBlank
     - [x] upc: String (required) with @field:NotBlank
     - [x] quantityOnHand: Int? (optional) consider @field:PositiveOrZero if allowed
     - [x] price: BigDecimal (required) with @field:NotNull (and optionally @field:Positive)
     - [x] version: Int? (output-only)
     - [x] createdDate: LocalDateTime? (output-only)
     - [x] updateDate: LocalDateTime? (output-only)
   - [x] Use restricted visibility (internal) if only used within module

2. [x] Introduce MapStruct mapper
   - [x] Create package: com.hophman.learning.juniemvc.mapper
   - [x] Add BeerMapper.kt interface annotated with @Mapper(componentModel = "spring")
   - [x] Define fun toEntity(dto: BeerDto): BeerEntity
     - [x] Ignore mapping for: id, version, createdDate, updateDate
   - [x] Define fun toDto(entity: BeerEntity): BeerDto
   - [x] Define fun updateEntityFromDto(dto: BeerDto, @MappingTarget entity: BeerEntity): BeerEntity
     - [x] Update only: name, style, upc, quantityOnHand, price
     - [x] Ensure NOT overwriting: id, version, createdDate, updateDate
   - [x] Add explicit @Mappings/@Mapping annotations for ignored fields
   - [x] Add auxiliary mappings if enum/type differences exist (e.g., style)

3. [x] Prepare Gradle configuration (documentation/update)
   - [x] Update build.gradle.kts with MapStruct support (if performing now)
     - [x] Apply plugin: kotlin("kapt")
     - [x] Add dependencies:
       - [x] implementation("org.mapstruct:mapstruct:1.6.3")
       - [x] kapt("org.mapstruct:mapstruct-processor:1.6.3")
     - [x] Ensure Kotlin compilation uses KAPT and Java compatibility for annotation processing
   - [x] Document chosen MapStruct version (e.g., 1.5.x)

4. [x] Refactor service API to use DTOs
   - [x] Update com.hophman.learning.juniemvc.service.BeerService signatures to:
     - [x] fun create(dto: BeerDto): BeerDto
     - [x] fun findAll(): List<BeerDto>
     - [x] fun findById(id: Int): BeerDto?
     - [x] fun update(id: Int, dto: BeerDto): BeerDto?
     - [x] fun delete(id: Int): Boolean
   - [x] Modify com.hophman.learning.juniemvc.service.BeerServiceImpl
     - [x] Inject BeerRepository and BeerMapper via constructor injection
     - [x] Implement create: mapper.toEntity(dto) -> repo.save(entity) -> mapper.toDto(saved)
     - [x] Implement findAll: repo.findAll().map(mapper::toDto)
     - [x] Implement findById: repo.findById(id).map(mapper::toDto).orElse(null)
     - [x] Implement update: repo.findById(id).map { mapper.updateEntityFromDto(dto, it); repo.save(it) }.map(mapper::toDto).orElse(null)
     - [x] Implement delete: if exists then delete and return true else false
     - [x] Add @Transactional annotations
       - [x] readOnly = true for find* methods
       - [x] default for create/update/delete

5. [x] Refactor controller to use DTOs
   - [x] Update com.hophman.learning.juniemvc.rest.BeerController method signatures to use BeerDto
   - [x] Define endpoints behavior:
     - [x] POST /api/beers: @Valid @RequestBody BeerDto -> returns 201 with Location header and BeerDto body
     - [x] GET /api/beers: return List<BeerDto>
     - [x] GET /api/beers/{id}: return BeerDto or 404
     - [x] PUT /api/beers/{id}: @Valid BeerDto -> return BeerDto or 404
     - [x] DELETE /api/beers/{id}: return 204 or 404
   - [x] Ensure no JPA entities are exposed
   - [x] Ensure proper ResponseEntity statuses

6. [x] Validation and error handling
   - [x] Rely on Spring’s default MethodArgumentNotValidException handling for 400 on invalid input
   - [x] (Optional, later) Prepare @RestControllerAdvice skeleton for future centralized handling

7. [x] Verify mapping rules
   - [x] Confirm @Mapping(ignore = true) for id, version, createdDate, updateDate in toEntity and updateEntityFromDto
   - [x] Ensure toDto maps all BeerDto fields

8. [x] Update/consider tests
   - [x] Update controller tests to post/put JSON matching BeerDto and expect DTO-shaped responses
   - [x] Update service tests to assert mapping and DTO usage
   - [x] Keep repository tests unchanged
   - [x] Prefer @WebMvcTest for controller slice with mocked service; use RANDOM_PORT for integration tests if any

9. [] Incremental migration checklist
   - [] After adding DTO and mapper, attempt compile (requires MapStruct config) or use temporary stub mapper if needed
   - [] Refactor service, implementation, and controller in one cohesive change to avoid type conflicts
   - [] Verify application.yml setting spring.jpa.open-in-view=false (consider separate change if not set)

10. [] Non-functional considerations
    - [] Use constructor injection throughout; avoid lateinit/field injection
    - [] Restrict visibility where applicable (internal where not part of public API)
    - [] Use SLF4J logging; avoid println

11. [] Acceptance criteria verification
    - [] Controller no longer exposes BeerEntity
    - [] Service API uses BeerDto
    - [] POST returns 201 with Location header and BeerDto body
    - [] Inbound mapping ignores id/version/timestamps
    - [] Code compiles with MapStruct dependencies present

12. [] Backout plan (document)
    - [] Note steps to revert controller and service signatures to entity-based versions if needed
    - [] Keep DTO work on a feature branch until MapStruct is configured