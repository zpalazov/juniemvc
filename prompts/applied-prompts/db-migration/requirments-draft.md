### Goal
Implement a DB migration using Spring Boot + Flyway. Consider also `.junie/guidelines.md` for the structure. Use the 
existing JPA entities for the DB schema BeerEntity, BeerOrderEntity and BeerOrderLineEntity and wire all associations 
exactly as in the diagram:
- BeerOrder 1 —* BeerOrderLine
- Beer 1 —* BeerOrderLine

Below are precise, instructions which need to be represented in a valid SQL. migration file (V1__Initial_DB.sql).

---

### Design decisions that match the ERD and Kotlin/JPA best practices
- IDs: Int with IDENTITY 
- Timestamps: LocalDateTime (as in BeerEntity).
- Enums: Persist as String with @Enumerated(EnumType.STRING) to keep the schema stable.
- Table/column names: snake_case plural table names to match your BeerEntity (table "beers").

---

### Status enums
Create simple enums for order and line status. Persist as STRING.

```
enum BeerOrderStatus { NEW, VALIDATION_PENDING, VALIDATED, ALLOCATION_PENDING, ALLOCATED, PICKED_UP, DELIVERED, CANCELLED }

enum BeerOrderLineStatus { NEW, ALLOCATED, CANCELLED }
```

---

### beers
Add a unique index for UPC and aligning column names to the same snake_case pattern:

---

### Schema DDL summary 
- Table beer_orders(id PK, version, customer_ref, payment_amount, status, created_date, updated_date)
- Table beer_order_lines(id PK, version, beer_order_id FK -> beer_orders.id, beer_id FK -> beers.id, order_quantity, status, created_date, updated_date)
- Indexes on beer_order_lines(beer_order_id), beer_order_lines(beer_id), and beer_orders(customer_ref). UPC remains unique on beers.

---
