### Goal
Implement the relationships shown in the ERD using Spring Boot + JPA (Kotlin). You already have BeerEntity. Add BeerOrderEntity and BeerOrderLineEntity and wire all associations exactly as in the diagram:
- BeerOrder 1 —* BeerOrderLine
- Beer 1 —* BeerOrderLine

Below are precise, Kotlin-friendly instructions and ready-to-paste entity templates that follow the provided Spring Boot guidelines.

---

### Design decisions that match the ERD and Kotlin/JPA best practices
- IDs: Int with IDENTITY (matches BeerEntity). Use nullable Int? for new entities.
- Timestamps: Hibernate’s @CreationTimestamp/@UpdateTimestamp with LocalDateTime (as in BeerEntity).
- Enums: Persist as String with @Enumerated(EnumType.STRING) to keep the schema stable.
- Fetch types: Always make to-one associations LAZY (JPA default is EAGER, which we don’t want). Collections remain LAZY.
- Cascades:
    - Cascade from BeerOrder to BeerOrderLine: cascade = [CascadeType.ALL], orphanRemoval = true.
    - Do not cascade from BeerOrderLine to Beer.
- Owning sides:
    - BeerOrderLine owns both FKs (beer_order_id, beer_id) via @ManyToOne with @JoinColumn.
    - BeerOrder’s collection is the inverse side (mappedBy = "beerOrder").
- Helper methods on BeerOrder to keep both sides in sync (addLine/removeLine).
- Equality: Base equals/hashCode on id only and avoid collections to prevent circular references. In Kotlin with JPA 
avoid data classes for entities.
- Table/column names: snake_case plural table names to match your BeerEntity (table "beers").

---

### 1) Status enums
Create simple enums for order and line status. Persist as STRING.

```kotlin
package com.hophman.learning.juniemvc.entity

enum class BeerOrderStatus { NEW, VALIDATION_PENDING, VALIDATED, ALLOCATION_PENDING, ALLOCATED, PICKED_UP, DELIVERED, CANCELLED }

enum class BeerOrderLineStatus { NEW, ALLOCATED, CANCELLED }
```

---

### 2) BeerOrderEntity

```kotlin
package com.hophman.learning.juniemvc.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "beer_orders",
    indexes = [
        Index(name = "idx_beer_orders_customer_ref", columnList = "customer_ref")
    ]
)
open class BeerOrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Version
    var version: Int? = null,

    @Column(name = "customer_ref", nullable = false)
    var customerRef: String = "",

    @Column(name = "payment_amount", precision = 19, scale = 2)
    var paymentAmount: BigDecimal? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: BeerOrderStatus = BeerOrderStatus.NEW,

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    var createdDate: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_date")
    var updatedDate: LocalDateTime? = null,

    @OneToMany(
        mappedBy = "beerOrder",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var lines: MutableList<BeerOrderLineEntity> = mutableListOf()
) {
    fun addLine(line: BeerOrderLineEntity) {
        line.beerOrder = this
        lines.add(line)
    }
    fun removeLine(line: BeerOrderLineEntity) {
        if (lines.remove(line)) line.beerOrder = null
    }

    override fun equals(other: Any?): Boolean =
        this === other || (other is BeerOrderEntity && id != null && id == other.id)

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
```

Notes:
- BeerOrder is the aggregate root for its lines. Orphan removal ensures deleting a line from the collection deletes it in DB.

---

### 3) BeerOrderLineEntity

```kotlin
package com.hophman.learning.juniemvc.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(
    name = "beer_order_lines",
    indexes = [
        Index(name = "idx_bol_order", columnList = "beer_order_id"),
        Index(name = "idx_bol_beer", columnList = "beer_id")
    ]
)
open class BeerOrderLineEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Version
    var version: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beer_order_id", nullable = false)
    var beerOrder: BeerOrderEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beer_id", nullable = false)
    var beer: BeerEntity? = null,

    @Column(name = "order_quantity", nullable = false)
    var orderQuantity: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: BeerOrderLineStatus = BeerOrderLineStatus.NEW,

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    var createdDate: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_date")
    var updatedDate: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean =
        this === other || (other is BeerOrderLineEntity && id != null && id == other.id)

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
```

Notes:
- The Beer association is read-only from an order perspective; do not cascade to Beer.
- Both to-one relations are explicitly LAZY to avoid N+1 and serialization surprises.

---

### 4) Optional: Back-reference on BeerEntity, not required at the moment
You can keep Beer -> lines unidirectional (recommended unless needed). If you do need it:

```kotlin
@OneToMany(mappedBy = "beer", fetch = FetchType.LAZY)
open var orderLines: MutableList<BeerOrderLineEntity> = mutableListOf()
```

Do not cascade here; Beer is not the aggregate root of order lines.

---

### 5) Existing BeerEntity tweaks
Add a unique index for UPC (you already mark it unique) and aligning column names to the same snake_case pattern:

```kotlin
@Table(
    name = "beers",
    indexes = [Index(name = "uk_beers_upc", columnList = "upc", unique = true)]
)
```

This is optional since you already have unique = true on the column.

---

### 6) Repositories
Create simple Spring Data repositories for each aggregate root. Typically you only need repositories for Beer and BeerOrder. BeerOrderLine is managed by BeerOrder via cascades.

```kotlin
interface BeerOrderRepository : JpaRepository<BeerOrderEntity, Int>
interface BeerRepository : JpaRepository<BeerEntity, Int>
```

---

### 7) Transaction boundaries and service usage
- Apply @Transactional at service methods that create/update orders and their lines. Read-only queries should use @Transactional(readOnly = true).
- With OSIV disabled (recommended), map entities to DTOs in the service layer before returning from controllers to avoid lazy-loading during JSON serialization.

Example create flow:

```kotlin
@Service
class BeerOrderService(
    private val beerRepo: BeerRepository,
    private val orderRepo: BeerOrderRepository,
) {
    @Transactional
    fun placeOrder(customerRef: String, items: List<Pair<Int, Int>>): BeerOrderEntity {
        val order = BeerOrderEntity(customerRef = customerRef)
        items.forEach { (beerId, qty) ->
            val beer = beerRepo.getReferenceById(beerId)
            order.addLine(BeerOrderLineEntity(beer = beer, orderQuantity = qty))
        }
        return orderRepo.save(order)
    }
}
```

---

### 8) DTOs and mapping (keep web separate from persistence)
- Do not expose entities from controllers. Create request/response DTOs for BeerOrder and BeerOrderLine and map in the service or via MapStruct/Kotlin mappers.

---

### 9) Schema DDL summary (what Hibernate will produce)
- Table beer_orders(id PK, version, customer_ref, payment_amount, status, created_date, updated_date)
- Table beer_order_lines(id PK, version, beer_order_id FK -> beer_orders.id, beer_id FK -> beers.id, order_quantity, status, created_date, updated_date)
- Indexes on beer_order_lines(beer_order_id), beer_order_lines(beer_id), and beer_orders(customer_ref). UPC remains unique on beers.

---

### 10) Common pitfalls to avoid
- Forgetting to set both sides of the relationship. Use addLine/removeLine helpers.
- Leaving ManyToOne eager (causes N+1 and large object graphs). Always set fetch = LAZY.
- Cascading to Beer from lines (don’t do it).
- Putting collections in equals/hashCode (can cause performance and stack issues).

With the three entities defined as above, you’ll have the exact relationships from the ERD, implemented in a Kotlin-idiomatic, Spring Boot-friendly way that follows the provided guidelines.