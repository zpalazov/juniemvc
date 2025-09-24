package com.hophman.learning.juniemvc.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "beer_orders",
    indexes = [Index(name = "idx_beer_orders_customer_ref", columnList = "customer_ref")]
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

    @OneToMany(
        mappedBy = "beerOrder",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var lines: MutableList<BeerOrderLineEntity> = mutableListOf(),

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    var createdDate: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_date")
    var updatedDate: LocalDateTime? = null
) {
    fun addLine(line: BeerOrderLineEntity) {
        line.beerOrder = this
        lines.add(line)
    }

    fun removeLine(line: BeerOrderLineEntity) {
        lines.remove(line)
        line.beerOrder = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BeerOrderEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
