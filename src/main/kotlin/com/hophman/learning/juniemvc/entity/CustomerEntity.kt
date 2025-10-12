package com.hophman.learning.juniemvc.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(
    name = "customers",
    indexes = [
        Index(name = "idx_customers_email", columnList = "email"),
        Index(name = "idx_customers_phone", columnList = "phone")
    ]
)
class CustomerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Version
    var version: Int? = null,

    @field:NotBlank
    @Column(nullable = false)
    var name: String = "",

    @field:Email
    @Column(nullable = true)
    var email: String? = null,

    @Column(nullable = true)
    var phone: String? = null,

    @field:NotBlank
    @Column(name = "address_line1", nullable = false)
    var addressLine1: String = "",

    @Column(name = "address_line2", nullable = true)
    var addressLine2: String? = null,

    @field:NotBlank
    @Column(nullable = false)
    var city: String = "",

    @field:NotBlank
    @Column(nullable = false)
    var state: String = "",

    @field:NotBlank
    @Column(name = "postal_code", nullable = false)
    var postalCode: String = "",

    @OneToMany(
        mappedBy = "customer",
        fetch = FetchType.LAZY
    )
    var orders: MutableList<BeerOrderEntity> = mutableListOf(),

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    var createdDate: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_date")
    var updatedDate: LocalDateTime? = null
) {
    fun addOrder(order: BeerOrderEntity) {
        order.customer = this
        orders.add(order)
    }

    fun removeOrder(order: BeerOrderEntity) {
        orders.remove(order)
        if (order.customer == this) order.customer = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CustomerEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
