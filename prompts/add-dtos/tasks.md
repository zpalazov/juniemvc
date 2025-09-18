1. [] Create DTO package and class
   - [] Create package: com.hophman.leaning.juniemvc.model
   - [] Add BeerDto.kt as a Kotlin data class with fields:
     - [] id: Int? (output-only)
     - [] name: String (required) with @field:NotBlank
     - [] style: String (required) with @field:NotBlank
     - [] upc: String (required) with @field:NotBlank
     - [] quantityOnHand: Int? (optional) consider @field:PositiveOrZero if allowed
     - [] price: BigDecimal (required) with @field:NotNull (and optionally @field:Positive)
     - [] version: Int? (output-only)
     - [] createdDate: LocalDateTime? (output-only)
     - [] updateDate: LocalDateTime? (output-only)
   - [] Use restricted visibility (internal) if only used within module

2. [] Introduce MapStruct mapper
   - [] Create package: com.hophman.learning.juniemvc.mapper
   - [] Add BeerMapper.kt interface annotated with @Mapper(componentModel = "spring")
   - [] Define fun toEntity(dto: BeerDto): BeerEntity
     - [] Ignore mapping for: id, version, createdDate, updateDate
   - [] Define fun toDto(entity: BeerEntity): BeerDto
   - [] Define fun updateEntityFromDto(dto: BeerDto, @MappingTarget entity: BeerEntity): BeerEntity
     - [] Update only: name, style, upc, quantityOnHand, price
     - [] Ensure NOT overwriting: id, version, createdDate, updateDate
   - [] Add explicit @Mappings/@Mapping annotations for ignored fields
   - [] Add auxiliary mappings if enum/type differences exist (e.g., style)

3. [] Prepare Gradle configuration (documentation/update)
   - [] Update build.gradle.kts with MapStruct support (if performing now)
     - [] Apply plugin: kotlin("kapt")
     - [] Add dependencies:
       - [] implementation("org.mapstruct:mapstruct:<version>")
       - [] kapt("org.mapstruct:mapstruct-processor:<version>")
     - [] Ensure Kotlin compilation uses KAPT and Java compatibility for annotation processing
   - [] Document chosen MapStruct version (e.g., 1.5.x)

4. [] Refactor service API to use DTOs
   - [] Update com.hophman.learning.juniemvc.service.BeerService signatures to:
     - [] fun create(dto: BeerDto): BeerDto
     - [] fun findAll(): List<BeerDto>
     - [] fun findById(id: Int): BeerDto?
     - [] fun update(id: Int, dto: BeerDto): BeerDto?
     - [] fun delete(id: Int): Boolean
   - [] Modify com.hophman.learning.juniemvc.service.BeerServiceImpl
     - [] Inject BeerRepository and BeerMapper via constructor injection
     - [] Implement create: mapper.toEntity(dto) -> repo.save(entity) -> mapper.toDto(saved)
     - [] Implement findAll: repo.findAll().map(mapper::toDto)
     - [] Implement findById: repo.findById(id).map(mapper::toDto).orElse(null)
     - [] Implement update: repo.findById(id).map { mapper.updateEntityFromDto(dto, it); repo.save(it) }.map(mapper::toDto).orElse(null)
     - [] Implement delete: if exists then delete and return true else false
     - [] Add @Transactional annotations
       - [] readOnly = true for find* methods
       - [] default for create/update/delete

5. [] Refactor controller to use DTOs
   - [] Update com.hophman.learning.juniemvc.rest.BeerController method signatures to use BeerDto
   - [] Define endpoints behavior:
     - [] POST /api/beers: @Valid @RequestBody BeerDto -> returns 201 with Location header and BeerDto body
     - [] GET /api/beers: return List<BeerDto>
     - [] GET /api/beers/{id}: return BeerDto or 404
     - [] PUT /api/beers/{id}: @Valid BeerDto -> return BeerDto or 404
     - [] DELETE /api/beers/{id}: return 204 or 404
   - [] Ensure no JPA entities are exposed
   - [] Ensure proper ResponseEntity statuses

6. [] Validation and error handling
   - [] Rely on Spring’s default MethodArgumentNotValidException handling for 400 on invalid input
   - [] (Optional, later) Prepare @RestControllerAdvice skeleton for future centralized handling

7. [] Verify mapping rules
   - [] Confirm @Mapping(ignore = true) for id, version, createdDate, updateDate in toEntity and updateEntityFromDto
   - [] Ensure toDto maps all BeerDto fields

8. [] Update/consider tests
   - [] Update controller tests to post/put JSON matching BeerDto and expect DTO-shaped responses
   - [] Update service tests to assert mapping and DTO usage
   - [] Keep repository tests unchanged
   - [] Prefer @WebMvcTest for controller slice with mocked service; use RANDOM_PORT for integration tests if any

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