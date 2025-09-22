Title: Create Beer Order Feature — Implementation Plan

Objective
- Implement the Beer Order feature per prompts/create-beer-order/requirements.md with Kotlin + Spring Boot + JPA, following the project’s Spring Boot guidelines.

Plan (ordered steps)
1) Domain Model — Entities and Enums
   - Add BeerOrderStatus and BeerOrderLineStatus enums (STRING persistence).
   - Create BeerOrderEntity:
     - Table: beer_orders; columns: id (Int? IDENTITY), version, customer_ref, payment_amount (BigDecimal?), status, created_date, updated_date.
     - OneToMany<List<BeerOrderLineEntity>> lines mappedBy "beerOrder", cascade = ALL, orphanRemoval = true, fetch = LAZY.
     - Helper functions addLine(line) and removeLine(line) to maintain bidirectional links.
     - Index on customer_ref (idx_beer_orders_customer_ref).
   - Create BeerOrderLineEntity:
     - Table: beer_order_lines; composite PK (beer_order_id, beer_id) using @Embeddable key (BeerOrderLineId) with fields beerOrderId, beerId.
     - Columns: version, order_quantity, status, created_date, updated_date; FKs to beer_orders.id and beers.id.
     - ManyToOne beerOrder (LAZY) and beer (LAZY); no cascade to Beer; maintain equals/hashCode based on embedded id.
     - Indexes: idx_bol_order(beer_order_id), idx_bol_beer(beer_id).
   - Conventions:
     - Use GenerationType.IDENTITY for aggregate roots (BeerOrderEntity). Keep BeerEntity as-is.
     - Use @CreationTimestamp and @UpdateTimestamp with LocalDateTime in both entities.
     - Avoid data classes for entities; provide equals/hashCode based on identifiers only.

2) Repositories
   - Create BeerOrderRepository : JpaRepository<BeerOrderEntity, Int>.
   - Reuse existing BeerRepository; no repository for BeerOrderLineEntity.

3) Service Layer — Input DTOs and Operations
   - Create input DTOs (internal data classes in service package):
     - PlaceBeerOrderDto(customerRef: String, items: List<OrderItemDto>)
     - OrderItemDto(beerId: Int, quantity: Int)
   - Define BeerOrderService interface:
     - fun placeOrder(dto: PlaceBeerOrderDto): BeerOrderDto
     - fun getOrder(id: Int): BeerOrderDto
   - Implement JpaBeerOrderService with constructor injection of BeerOrderRepository and BeerRepository.
     - @Transactional on placeOrder; @Transactional(readOnly = true) on getOrder.
     - placeOrder flow:
       1. Validate dto.customerRef not blank and items not empty; each quantity > 0.
       2. Resolve all beerIds via BeerRepository.findAllById; if any missing, throw NotFoundException.
       3. Create BeerOrderEntity(status = NEW, paymentAmount = null, customerRef = dto.customerRef).
       4. For each item, create BeerOrderLineEntity with orderQuantity, status NEW, set beer reference, call addLine.
       5. Save order via BeerOrderRepository.save and map to BeerOrderDto within the transaction.
     - getOrder flow:
       1. Find by id or throw NotFoundException.
       2. Map to BeerOrderDto (initialize lazy lines during mapping within transaction).

4) DTOs and Mapping
   - Web request/response DTOs (data classes) in model package or a dedicated web dto package:
     - PlaceBeerOrderRequest(customerRef: String, items: List<OrderItemRequest>)
     - OrderItemRequest(beerId: Int, quantity: Int)
     - BeerOrderResponse(id: Int, customerRef: String, status: String, paymentAmount: BigDecimal?, createdDate: String, updatedDate: String?, lines: List<BeerOrderLineResponse>)
     - BeerOrderLineResponse(beerId: Int, beerName: String?, orderQuantity: Int, status: String)
   - Service-level DTO for return type:
     - BeerOrderDto(id: Int, customerRef: String, status: BeerOrderStatus, paymentAmount: BigDecimal?, createdDate: LocalDateTime, updatedDate: LocalDateTime?, lines: List<BeerOrderLineDto>)
     - BeerOrderLineDto(beerId: Int, beerName: String?, orderQuantity: Int, status: BeerOrderLineStatus)
   - Mapper(s):
     - Entity -> Service DTO: BeerOrderMapper (internal class or functions) to build BeerOrderDto and BeerOrderLineDto.
     - Service DTO -> Response DTO: extension functions or a simple mapper in web layer.
     - Request DTO -> Input DTO: PlaceBeerOrderRequest.toDto().

5) Web Layer — Controller
   - Create BeerOrderController at /api/v1/beer-orders.
   - Endpoints:
     - POST /api/v1/beer-orders: accepts PlaceBeerOrderRequest (@Valid), returns ResponseEntity.status(201).body(BeerOrderResponse).
     - GET /api/v1/beer-orders/{id}: returns BeerOrderResponse or 404.
   - Depend only on BeerOrderService; use mapper functions to translate DTOs.

6) Validation
   - Apply jakarta.validation constraints:
     - PlaceBeerOrderRequest.customerRef: @NotBlank
     - PlaceBeerOrderRequest.items: @NotEmpty
     - OrderItemRequest.beerId: @NotNull, @Positive
     - OrderItemRequest.quantity: @NotNull, @Positive
   - In service, enforce quantities > 0 and verify all beers exist.

7) Exception Handling
   - Add/extend a @RestControllerAdvice GlobalExceptionHandler if not already present:
     - Map NotFoundException to 404 using ProblemDetail.
     - Map MethodArgumentNotValidException and ConstraintViolationException to 400 with details.

8) Configuration and Conventions
   - Ensure spring.jpa.open-in-view=false in application.yml.
   - Use constructor injection and internal visibility where appropriate.
   - Add SLF4J logger in service/controller; avoid println.

9) Persistence Details and Annotations
   - Ensure snake_case table/column names via @Table/@Column.
   - Add @Index annotations:
     - @Table(indexes = [@Index(name = "idx_beer_orders_customer_ref", columnList = "customer_ref")])
     - @Table(indexes = [@Index(name = "idx_bol_order", columnList = "beer_order_id"), @Index(name = "idx_bol_beer", columnList = "beer_id")]) on BeerOrderLineEntity.
   - Set FetchType.LAZY on all to-one associations (beerOrder, beer).

10) Tests
   - Unit tests for BeerOrderService:
     - placeOrder creates order with correct line count and quantities.
     - placeOrder fails when any beerId is missing (NotFoundException).
     - placeOrder rejects non-positive quantities.
     - getOrder returns order; getOrder for missing id throws NotFoundException.
   - Web slice tests for BeerOrderController (using MockMvc or WebTestClient):
     - POST happy path returns 201 with expected JSON shape.
     - GET returns 200 for existing and 404 for missing.
   - Ensure tests use random port for full SpringBootTest if needed; prefer web slice for controller.

11) Build & Verify
   - Run tests and ensure the project builds.
   - Verify Hibernate schema has beer_orders and beer_order_lines with composite PK and proper FKs and indexes (inspect logs or use schema export in tests).

12) File/Package Layout (proposed)
   - src/main/kotlin/com/hophman/learning/juniemvc/entity/
     - BeerOrderEntity.kt
     - BeerOrderLineEntity.kt
     - BeerOrderLineId.kt (Embeddable)
     - BeerOrderStatus.kt
     - BeerOrderLineStatus.kt
   - src/main/kotlin/com/hophman/learning/juniemvc/repo/
     - BeerOrderRepository.kt
   - src/main/kotlin/com/hophman/learning/juniemvc/service/
     - BeerOrderService.kt
     - BeerOrderServiceImpl.kt
     - Dtos.kt (PlaceBeerOrderDto, OrderItemDto) — optional separate file
   - src/main/kotlin/com/hophman/learning/juniemvc/model/
     - BeerOrderDto.kt, BeerOrderLineDto.kt
     - Web DTOs: PlaceBeerOrderRequest.kt, OrderItemRequest.kt, BeerOrderResponse.kt, BeerOrderLineResponse.kt (or under rest/dto)
   - src/main/kotlin/com/hophman/learning/juniemvc/mapper/
     - BeerOrderMapper.kt (entity -> dto)
   - src/main/kotlin/com/hophman/learning/juniemvc/rest/
     - BeerOrderController.kt
   - src/main/kotlin/com/hophman/learning/juniemvc/exception/
     - NotFoundException.kt (if not present)
     - GlobalExceptionHandler.kt (if not present)

13) Implementation Notes
   - equals/hashCode for entities:
     - BeerOrderEntity: based on id if not null; otherwise identity fallback.
     - BeerOrderLineEntity: based on embedded id.
   - Helper addLine/removeLine must set both sides of association and manage id.link where needed.
   - Mapping should be done within transactional context to avoid LAZY initialization issues.
   - Keep controller methods default visibility (no explicit public).

14) Incremental Delivery Strategy
   - Implement entities and repositories first; compile.
   - Implement service input DTOs and service implementation; add unit tests; compile and run tests.
   - Add DTOs and mappers.
   - Implement controller; add web slice tests.
   - Final build and verification.

15) Acceptance Checklist
   - Build succeeds and tests pass.
   - POST /api/v1/beer-orders returns 201 with correct body; GET returns 200 for existing and 404 for missing.
   - Database schema includes beer_orders and beer_order_lines with composite PK (beer_order_id, beer_id), FKs, indexes.
   - Entities, cascades, fetch types, timestamps follow requirements and guidelines.
