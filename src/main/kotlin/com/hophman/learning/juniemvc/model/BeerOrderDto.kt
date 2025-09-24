package com.hophman.learning.juniemvc.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class BeerOrderDto(
    val id: Int,
    val customerRef: String,
    val status: String,
    val paymentAmount: BigDecimal?,
    val createdDate: LocalDateTime?,
    val updatedDate: LocalDateTime?,
    val lines: List<BeerOrderLineDto>
)

data class BeerOrderLineDto(
    val beerId: Int,
    val beerName: String,
    val orderQuantity: Int,
    val status: String
)
