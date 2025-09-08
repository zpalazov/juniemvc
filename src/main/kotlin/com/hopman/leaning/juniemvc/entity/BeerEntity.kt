package com.hopman.leaning.juniemvc.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "beers")
open class BeerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Version
    var version: Int? = null,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var style: String = "",

    @Column(nullable = false, unique = true)
    var upc: String = "",

    var quantityOnHand: Int? = null,

    @Column(nullable = false, precision = 19, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO,

    @CreationTimestamp
    @Column(updatable = false)
    var createdDate: LocalDateTime? = null,

    @UpdateTimestamp
    var updateDate: LocalDateTime? = null
)
