package com.hophman.learning.juniemvc.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal
import java.time.LocalDateTime

data class BeerDto(
    val id: Int? = null,
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val style: String,
    @field:NotBlank
    val upc: String,
    @field:PositiveOrZero
    val quantityOnHand: Int? = null,
    @field:NotNull @field:Positive
    val price: BigDecimal,
    val version: Int? = null,
    val createdDate: LocalDateTime? = null,
    val updateDate: LocalDateTime? = null
)
