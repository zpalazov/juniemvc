package com.hophman.learning.juniemvc.mapper

import com.hophman.learning.juniemvc.entity.BeerOrderEntity
import com.hophman.learning.juniemvc.model.BeerOrderDto
import com.hophman.learning.juniemvc.model.BeerOrderLineDto

internal object BeerOrderMapper {

    fun toDto(entity: BeerOrderEntity): BeerOrderDto = BeerOrderDto(
        id = entity.id!!,
        customerRef = entity.customerRef,
        status = entity.status.name,
        paymentAmount = entity.paymentAmount,
        createdDate = entity.createdDate,
        updatedDate = entity.updatedDate,
        lines = entity.lines.map { line ->
            checkNotNull(line.beer)
            BeerOrderLineDto(
                beerId = line.id.beerId,
                beerName = line.beer!!.name,
                orderQuantity = line.orderQuantity,
                status = line.status.name
            )
        }
    )
}
