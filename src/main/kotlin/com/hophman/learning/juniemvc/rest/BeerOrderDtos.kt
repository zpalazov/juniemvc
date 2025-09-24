package com.hophman.learning.juniemvc.rest

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

internal data class PlaceBeerOrderRequest(
    @field:NotBlank
    val customerRef: String,
    @field:NotEmpty
    val items: List<OrderItemRequest>
)

internal data class OrderItemRequest(
    @field:NotNull @field:Positive
    val beerId: Int?,
    @field:NotNull @field:Positive
    val quantity: Int?
)

internal data class BeerOrderResponse(
    val id: Int,
    val customerRef: String,
    val status: String,
    val paymentAmount: BigDecimal?,
    val createdDate: String?,
    val updatedDate: String?,
    val lines: List<BeerOrderLineResponse>
)

internal data class BeerOrderLineResponse(
    val beerId: Int,
    val beerName: String?,
    val orderQuantity: Int,
    val status: String
)
