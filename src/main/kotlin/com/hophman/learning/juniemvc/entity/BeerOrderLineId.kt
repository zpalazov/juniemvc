package com.hophman.learning.juniemvc.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class BeerOrderLineId(
    @Column(name = "beer_order_id")
    var beerOrderId: Int = 0,
    @Column(name = "beer_id")
    var beerId: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BeerOrderLineId) return false
        return beerOrderId == other.beerOrderId && beerId == other.beerId
    }

    override fun hashCode(): Int {
        var result = beerOrderId
        result = 31 * result + beerId
        return result
    }
}
