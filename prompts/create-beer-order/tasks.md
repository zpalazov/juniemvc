# Create Beer Order Feature — Task List

Note: Check off each item by replacing [] with [x] as you complete it.

1. [x] Review prompts/create-beer-order/requirements.md to confirm scope and constraints.
2. [x] Set application convention checks
   2.1. [x] Ensure spring.jpa.open-in-view=false in src/main/resources/application.yml
   2.2. [x] Confirm SLF4J logging usage; remove/avoid println in new code
   2.3. [x] Follow constructor injection and internal visibility in new components
3. [x] Domain model — Enums
   3.1. [x] Create src/main/kotlin/com/hophman/learning/juniemvc/entity/BeerOrderStatus.kt (enum persisted as STRING)
   3.2. [x] Create src/main/kotlin/com/hophman/learning/juniemvc/entity/BeerOrderLineStatus.kt (enum persisted as STRING)
4. [x] Domain model — Entities and IDs
   4.1. [x] Create BeerOrderEntity.kt with:
        - Table beer_orders
        - id: Int? (IDENTITY), version, customer_ref, payment_amount (BigDecimal?), status, created_date, updated_date
        - OneToMany<List<BeerOrderLineEntity>> lines mappedBy "beerOrder", cascade ALL, orphanRemoval true, LAZY
        - Helper functions addLine/removeLine to maintain bidirectional link
        - @Table index idx_beer_orders_customer_ref on customer_ref
        - @CreationTimestamp/@UpdateTimestamp on timestamps
        - equals/hashCode based on id
   4.2. [x] Create BeerOrderLineId.kt (@Embeddable) with fields beerOrderId: Int, beerId: Int; equals/hashCode
   4.3. [x] Create BeerOrderLineEntity.kt with:
        - Table beer_order_lines
        - EmbeddedId: BeerOrderLineId
        - version, order_quantity, status, created_date, updated_date
        - ManyToOne LAZY beerOrder (FK beer_order_id), ManyToOne LAZY beer (FK beer_id); no cascade to Beer
        - Indexes: idx_bol_order (beer_order_id), idx_bol_beer (beer_id)
        - equals/hashCode based on embedded id
   4.4. [x] Verify all to-one associations are FetchType.LAZY
5. [x] Repositories
   5.1. [x] Create src/main/kotlin/com/hophman/learning/juniemvc/repo/BeerOrderRepository.kt : JpaRepository<BeerOrderEntity, Int>
   5.2. [x] Reuse BeerRepository; no repository for BeerOrderLineEntity
6. [x] Service-level input DTOs
   6.1. [x] Create PlaceBeerOrderDto(customerRef: String, items: List<OrderItemDto>) in service package
   6.2. [x] Create OrderItemDto(beerId: Int, quantity: Int) in service package
7. [x] Service interface and implementation
   7.1. [x] Define BeerOrderService with:
        - fun placeOrder(dto: PlaceBeerOrderDto): BeerOrderDto
        - fun getOrder(id: Int): BeerOrderDto
   7.2. [x] Implement JpaBeerOrderService (constructor-inject BeerOrderRepository, BeerRepository)
        - @Transactional on placeOrder
        - @Transactional(readOnly = true) on getOrder
        - placeOrder flow:
          a) [x] Validate customerRef not blank; items not empty; each quantity > 0
          b) [x] Resolve all beerIds via BeerRepository.findAllById; throw NotFoundException if any missing
          c) [x] Create BeerOrderEntity(status = NEW, paymentAmount = null, customerRef = dto.customerRef)
          d) [x] For each item, create BeerOrderLineEntity with orderQuantity, status NEW; set beer reference; call addLine
          e) [x] Save order via BeerOrderRepository.save; map to BeerOrderDto
        - getOrder flow:
          f) [x] Find by id or throw NotFoundException
          g) [x] Map to BeerOrderDto ensuring lines are initialized within transaction
8. [x] DTOs and mapping
   8.1. [x] Create service return DTOs in model package:
        - BeerOrderDto
        - BeerOrderLineDto
   8.2. [x] Create web request/response DTOs:
        - PlaceBeerOrderRequest, OrderItemRequest
        - BeerOrderResponse, BeerOrderLineResponse
   8.3. [x] Implement BeerOrderMapper (entity -> service DTO)
   8.4. [x] Implement mapping functions:
        - PlaceBeerOrderRequest.toDto(): PlaceBeerOrderDto
        - BeerOrderDto -> BeerOrderResponse (mapper or extension functions)
9. [x] Web layer — Controller
   9.1. [x] Create BeerOrderController at /api/v1/beer-orders
        - POST: accepts @Valid PlaceBeerOrderRequest; returns 201 with BeerOrderResponse
        - GET /{id}: returns 200 with BeerOrderResponse or 404 when not found
   9.2. [x] Ensure controller depends only on BeerOrderService and uses mapper functions
10. [x] Validation
    10.1. [x] Add jakarta.validation constraints:
         - PlaceBeerOrderRequest.customerRef: @NotBlank
         - PlaceBeerOrderRequest.items: @NotEmpty
         - OrderItemRequest.beerId: @NotNull, @Positive
         - OrderItemRequest.quantity: @NotNull, @Positive
    10.2. [x] Enforce quantities > 0 in service
    10.3. [x] Verify NotFound for missing beers
11. [x] Exception handling
    11.1. [x] Add NotFoundException in exception package (if missing)
    11.2. [x] Add/extend GlobalExceptionHandler with @RestControllerAdvice to:
          - Map NotFoundException -> 404 using ProblemDetail
          - Map MethodArgumentNotValidException and ConstraintViolationException -> 400 with details
12. [x] Persistence details
    12.1. [x] Ensure snake_case names via @Table/@Column
    12.2. [x] Add indexes per plan to entities
13. [x] Tests — Service layer
    13.1. [x] Unit tests for BeerOrderService.placeOrder happy path (correct line count and quantities)
    13.2. [x] Unit test: placeOrder fails when any beerId missing (NotFoundException)
    13.3. [x] Unit test: placeOrder rejects non-positive quantities
    13.4. [x] Unit test: getOrder returns order; missing id throws NotFoundException
14. [x] Tests — Web layer
    14.1. [x] Web slice tests for BeerOrderController:
          - POST happy path returns 201 with expected JSON
          - GET existing returns 200; missing returns 404
    14.2. [x] If using SpringBootTest, ensure RANDOM_PORT
15. [x] Build & verify
    15.1. [x] Build project and run all tests
    15.2. [x] Verify Hibernate schema output includes beer_orders and beer_order_lines with composite PK, FKs, and indexes
16. [x] Final review
    16.1. [x] Ensure guidelines adherence (constructor injection, restricted visibility, logging)
    16.2. [x] Ensure code style and package layout matches plan
    16.3. [x] Update this tasks.md by marking completed items as [x] as you progress
