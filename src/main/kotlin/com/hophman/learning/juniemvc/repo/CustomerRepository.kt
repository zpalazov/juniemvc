package com.hophman.learning.juniemvc.repo

import com.hophman.learning.juniemvc.entity.CustomerEntity
import org.springframework.data.jpa.repository.JpaRepository

internal interface CustomerRepository : JpaRepository<CustomerEntity, Int> {
    fun findByEmail(email: String): CustomerEntity?
}
