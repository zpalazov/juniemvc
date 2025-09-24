package com.hophman.learning.juniemvc.service

data class PlaceBeerOrderDto(
    val customerRef: String,
    val items: List<OrderItemDto>
)

data class OrderItemDto(
    val beerId: Int,
    val quantity: Int
)
