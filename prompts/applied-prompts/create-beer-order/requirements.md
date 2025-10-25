Title: Create Beer Order Feature — Requirements

Objective
- Implement Beer Order domain modeled per ERD (beers_erd.png) and expose minimal REST endpoints to place and read orders.
- Apply Kotlin + Spring Boot + JPA best practices from project guidelines (constructor injection, restricted visibility, DTO separation, transactions, OSIV disabled, etc.).

Scope
- Persistence model: Add BeerOrderEntity and BeerOrderLineEntity and related enums; reuse existing BeerEntity.
- Associations (as in ERD):
  - BeerOrder 1 —* BeerOrderLine
  - Beer 1 —* BeerOrderLine
- Repositories: BeerOrderRepository; reuse BeerRepository. No repository required for BeerOrderLine.
- Service layer: DTO-based operations to place orders and query by id.
- Web layer: Request/response DTOs and a controller with basic endpoints.
- Validation, error handling, and basic happy-path tests.

Domain Model Requirements
1) Entities
- BeerEntity
  - Reuse existing entity unchanged. Optional: ensure table name "beers" and unique index on upc (column unique is already present).
- BeerOrderEntity
  - Table: beer_orders
  - Columns: id (PK, INT IDENTITY), version, customer_ref (NOT NULL), payment_amount (DECIMAL 19,2, nullable), status (STRING enum), created_date, updated_date
  - Relationships: OneToMany lines mappedBy="beerOrder"; cascade ALL; orphanRemoval true; LAZY
  - Helper methods: addLine/removeLine maintain bidirectional link
- BeerOrderLineEntity
  - Table: beer_order_lines
  - Primary Key: composite (beer_order_id, beer_id)
  - Columns: beer_order_id (FK -> beer_orders.id NOT NULL), beer_id (FK -> beers.id NOT NULL), version, order_quantity (INT NOT NULL), status (STRING enum), created_date, updated_date
  - Relationships: ManyToOne beerOrder LAZY; ManyToOne beer LAZY; no cascade to Beer

2) Enums
- BeerOrderStatus: NEW, VALIDATION_PENDING, VALIDATED, ALLOCATION_PENDING, ALLOCATED, PICKED_UP, DELIVERED, CANCELLED
- BeerOrderLineStatus: NEW, ALLOCATED, CANCELLED
- Persist enums as STRING

3) JPA/Hibernate Conventions
- Aggregate roots (BeerEntity, BeerOrderEntity) use nullable Int? ids with GenerationType.IDENTITY
- BeerOrderLineEntity uses a composite primary key (beer_order_id, beer_id) via @EmbeddedId or @IdClass
- Use @CreationTimestamp/@UpdateTimestamp with LocalDateTime
- All to-one associations must be LAZY
- Avoid data classes for entities; equals/hashCode based on identifier(s) only
- Use snake_case table/column names; add indexes:
  - beer_orders: idx_beer_orders_customer_ref(customer_ref)
  - beer_order_lines: idx_bol_order(beer_order_id), idx_bol_beer(beer_id)

Repositories
- interface BeerOrderRepository : JpaRepository<BeerOrderEntity, Int>
- Reuse existing BeerRepository

Service Layer
- Define BeerOrderService with constructor injection.
- Transactions:
  - @Transactional for create/update operations
  - @Transactional(readOnly = true) for queries
- Input DTOs (to service):
  - PlaceBeerOrderDto(customerRef: String, items: List<OrderItemDto>)
  - OrderItemDto(beerId: Int, quantity: Int)
- Core operations:
  - placeOrder(cmd): BeerOrderDto
    - Validate quantity (>0) and that beerIds exist
    - Create BeerOrderEntity with status NEW
    - For each item, get Beer reference, create BeerOrderLineEntity with orderQuantity, status NEW, add via addLine
    - Save via BeerOrderRepository and return mapped DTO
  - getOrder(id: Int): BeerOrderDto
    - Throw NotFoundException if missing

Web Layer (REST)
- Base path: /api/v1/beer-orders
- DTOs (data classes):
  - PlaceBeerOrderRequest(customerRef: String, items: List<OrderItemRequest>) with validation
  - OrderItemRequest(beerId: Int, quantity: Int)
  - BeerOrderResponse(id: Int, customerRef: String, status: String, paymentAmount: BigDecimal?, createdDate: String, updatedDate: String?, lines: List<BeerOrderLineResponse>)
  - BeerOrderLineResponse(beerId: Int, beerName: String?, orderQuantity: Int, status: String)
- Endpoints:
  - POST /api/v1/beer-orders
    - Body: PlaceBeerOrderRequest
    - 201 Created with BeerOrderResponse on success
    - 400 Bad Request on validation errors
    - 404 Not Found if any beerId does not exist
  - GET /api/v1/beer-orders/{id}
    - 200 OK with BeerOrderResponse
    - 404 Not Found if order not found
- Controller should depend on BeerOrderService only; do not expose entities

Validation
- PlaceBeerOrderRequest:
  - customerRef: @NotBlank
  - items: @NotEmpty
- OrderItemRequest:
  - beerId: @NotNull, @Positive
  - quantity: @NotNull, @Positive
- Service validation: verify referenced beers exist; reject zero or negative quantities

Error Handling
- Use centralized @RestControllerAdvice returning Problem Details
- NotFoundException -> 404; Validation exceptions -> 400

Mapping
- Provide simple mapper functions or a mapper class to convert between Entities and DTOs
- Do not leak LAZY associations beyond service layer; assemble DTOs within transaction

Testing Requirements
- Unit tests for BeerOrderService.placeOrder() and getOrder():
  - Creates order with correct line count and quantities
  - Fails with NotFound when beerId missing
  - Rejects invalid quantities
- Web slice tests for controller (MockMvc/WebTestClient):
  - POST happy path returns 201 and expected JSON shape
  - GET returns 200 for existing and 404 for missing
- Use random web port for any SpringBootTest; prefer slice tests for controller

Non-Functional
- Follow Kotlin Spring guidelines:
  - Constructor injection, internal visibility where applicable
  - No field/setter injection
  - Keep controller methods default visibility
- Logging via SLF4J; no println
- Do not enable OSIV (set spring.jpa.open-in-view=false if not already)

Acceptance Criteria
- The project builds successfully
- Database schema generated by Hibernate contains beer_orders and beer_order_lines with composite primary key (beer_order_id, beer_id), correct FKs and indexes
- Creating an order via POST returns 201 and persisted data can be retrieved via GET
- Entities follow specified associations, fetch types, cascades, and timestamps
- Tests for the service and controller pass
