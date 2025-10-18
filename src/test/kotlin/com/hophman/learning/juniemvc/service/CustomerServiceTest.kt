package com.hophman.learning.juniemvc.service

import com.hophman.learning.juniemvc.entity.CustomerEntity
import com.hophman.learning.juniemvc.exception.ExistingEntityException
import com.hophman.learning.juniemvc.exception.NotFoundEntityException
import com.hophman.learning.juniemvc.mapper.CustomerMapper
import com.hophman.learning.juniemvc.model.CustomerDto
import com.hophman.learning.juniemvc.repo.CustomerRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*

internal class CustomerServiceTest {

    private val repository: CustomerRepository = mockk()
    private val mapper: CustomerMapper = mockk()
    private val service = CustomerService(repository, mapper)

    @Test
    fun `when create then success`() {
        val customerDto = CreateCustomerDto(
            name = "John Doe",
            email = "john@example.com",
            phone = null,
            addressLine1 = "123 Main St",
            city = "Springfield",
            state = "IL",
            postalCode = "62704"
        )
        every { repository.findByEmail("john@example.com") } returns null
        val transient = CustomerEntity(
            name = customerDto.name,
            email = customerDto.email,
            addressLine1 = customerDto.addressLine1,
            city = customerDto.city,
            state = customerDto.state,
            postalCode = customerDto.postalCode
        )
        every { mapper.createRequestToEntity(customerDto) } returns transient
        val persisted = CustomerEntity(
            id = 1,
            name = customerDto.name,
            email = customerDto.email,
            addressLine1 = customerDto.addressLine1,
            city = customerDto.city,
            state = customerDto.state,
            postalCode = customerDto.postalCode
        )
        every { repository.save(transient) } returns persisted
        val dto = CustomerDto(
            id = 1,
            name = customerDto.name,
            email = customerDto.email,
            phone = null,
            addressLine1 = customerDto.addressLine1,
            addressLine2 = null,
            city = customerDto.city,
            state = customerDto.state,
            postalCode = customerDto.postalCode,
            createdDate = null,
            updatedDate = null
        )
        every { mapper.entityToDto(persisted) } returns dto

        val result = service.create(customerDto)

        assertEquals(1, result.id)
    }

    @Test
    fun `create duplicate email throws ConflictException`() {
        val createCustomerDto = CreateCustomerDto(
            name = "John Doe",
            email = "john@example.com",
            phone = null,
            addressLine1 = "123 Main St",
            city = "Springfield",
            state = "IL",
            postalCode = "62704"
        )
        every { repository.findByEmail("john@example.com") } returns
                CustomerEntity(
                    id = 42,
                    name = "Existing",
                    addressLine1 = "a",
                    city = "c",
                    state = "s",
                    postalCode = "p"
                )

        assertThrows(ExistingEntityException::class.java) {
            service.create(createCustomerDto)
        }
    }

    @Test
    fun `getById not found throws NotFoundException`() {
        every { repository.findById(99) } returns Optional.empty()

        assertThrows(NotFoundEntityException::class.java) {
            service.getById(99)
        }
    }

    @Test
    fun `delete not found throws NotFoundException`() {
        every { repository.existsById(77) } returns false

        assertThrows(NotFoundEntityException::class.java) {
            service.delete(77)
        }
    }
}
