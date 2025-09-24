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
    @EmbeddedId
    var id: BeerOrderLineId = BeerOrderLineId(),

    @Version
    var version: Int? = null,

    @Column(name = "order_quantity", nullable = false)
    var orderQuantity: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: BeerOrderLineStatus = BeerOrderLineStatus.NEW,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("beerOrderId")
    @JoinColumn(name = "beer_order_id")
    var beerOrder: BeerOrderEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("beerId")
    @JoinColumn(name = "beer_id")
    var beer: BeerEntity? = null,

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    var createdDate: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_date")
    var updatedDate: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BeerOrderLineEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
