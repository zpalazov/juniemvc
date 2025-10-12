package com.hophman.learning.juniemvc.repo

import com.hophman.learning.juniemvc.entity.BeerOrderEntity
import com.hophman.learning.juniemvc.entity.CustomerEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
internal class CustomerRepositoryTests @Autowired constructor(
    val customerRepository: CustomerRepository,
    val entityManager: TestEntityManager
) {

    @Test
    fun `when customer is saved then findByEmail returns it`() {
        val customer = CustomerEntity(
            name = "John Doe",
            email = "john.doe@example.com",
            phone = "+1234567890",
            addressLine1 = "123 Main St",
            city = "Springfield",
            state = "IL",
            postalCode = "62704"
        )

        val saved = customerRepository.save(customer)
        assertNotNull(saved.id)

        val found = customerRepository.findByEmail("john.doe@example.com")
        assertNotNull(found)
        assertEquals(saved.id, found!!.id)
    }

    @Test
    fun `when persist beer order linked to customer then can be queried by relation`() {
        val customer = CustomerEntity(
            name = "Jane Doe",
            email = "jane.doe@example.com",
            addressLine1 = "456 Oak Ave",
            city = "Metropolis",
            state = "NY",
            postalCode = "10001"
        )
        val savedCustomer = customerRepository.save(customer)

        val order = BeerOrderEntity(
            customerRef = "ORDER-123",
            customer = savedCustomer
        )
        entityManager.persist(order)
        entityManager.flush()

        val count = entityManager.entityManager
            .createQuery("select count(b) from BeerOrderEntity b where b.customer.id = :cid", java.lang.Long::class.java)
            .setParameter("cid", savedCustomer.id)
            .singleResult

        assertEquals(1L, count)
    }
}
