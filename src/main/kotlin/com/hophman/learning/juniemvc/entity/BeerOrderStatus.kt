package com.hophman.learning.juniemvc.entity

enum class BeerOrderStatus {
    NEW,
    VALIDATION_PENDING,
    VALIDATED,
    ALLOCATION_PENDING,
    ALLOCATED,
    PICKED_UP,
    DELIVERED,
    CANCELLED
}
