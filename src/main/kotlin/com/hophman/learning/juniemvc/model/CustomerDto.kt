package com.hophman.learning.juniemvc.model

import java.time.LocalDateTime

internal data class CustomerDto(
    val id: Int,
    val name: String,
    val email: String?,
    val phone: String?,
    val addressLine1: String,
    val addressLine2: String?,
    val city: String,
    val state: String,
    val postalCode: String,
    val createdDate: LocalDateTime?,
    val updatedDate: LocalDateTime?
)
