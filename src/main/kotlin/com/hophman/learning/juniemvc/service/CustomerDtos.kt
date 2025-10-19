package com.hophman.learning.juniemvc.service

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

internal data class CreateCustomerDto(
    @field:NotBlank
    val name: String,
    @field:Email
    val email: String? = null,
    val phone: String? = null,
    @field:NotBlank
    val addressLine1: String,
    val addressLine2: String? = null,
    @field:NotBlank
    val city: String,
    @field:NotBlank
    val state: String,
    @field:NotBlank
    val postalCode: String
)

internal data class UpdateCustomerDto(
    @field:NotBlank
    val name: String,
    @field:Email
    val email: String? = null,
    val phone: String? = null,
    @field:NotBlank
    val addressLine1: String,
    val addressLine2: String? = null,
    @field:NotBlank
    val city: String,
    @field:NotBlank
    val state: String,
    @field:NotBlank
    val postalCode: String
)
