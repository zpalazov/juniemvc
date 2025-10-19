package com.hophman.learning.juniemvc.rest

internal data class CustomerResponse(
    val id: Int,
    val name: String,
    val email: String?,
    val phone: String?,
    val addressLine1: String,
    val addressLine2: String?,
    val city: String,
    val state: String,
    val postalCode: String,
    val createdDate: String?,
    val updatedDate: String?
)
