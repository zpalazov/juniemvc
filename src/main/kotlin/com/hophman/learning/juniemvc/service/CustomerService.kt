package com.hophman.learning.juniemvc.service

import com.hophman.learning.juniemvc.exception.ExistingEntityException
import com.hophman.learning.juniemvc.exception.NotFoundEntityException
import com.hophman.learning.juniemvc.mapper.CustomerMapper
import com.hophman.learning.juniemvc.model.CustomerDto
import com.hophman.learning.juniemvc.repo.CustomerRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class CustomerService(private val repository: CustomerRepository, private val mapper: CustomerMapper) {

    private val logger = LoggerFactory.getLogger(CustomerService::class.java)

    @Transactional
    fun create(req: CreateCustomerDto): CustomerDto {
        req.email?.let { email ->
            repository.findByEmail(email)?.let { existing ->
                throw ExistingEntityException("Email '$email' is already in use by customer ${existing.id}")
            }
        }
        val entity = mapper.createRequestToEntity(req)
        val saved = repository.save(entity)
        logger.info("Created customer id={}", saved.id)

        return mapper.entityToDto(saved)
    }

    @Transactional(readOnly = true)
    fun getById(id: Int): CustomerDto =
        repository.findById(id)
            .map { mapper.entityToDto(it) }
            .orElseThrow { NotFoundEntityException("Customer $id not found") }

    @Transactional(readOnly = true)
    fun list(pageable: Pageable): Page<CustomerDto> {
        val page = repository.findAll(pageable)
        val dtos = mapper.entitiesToDtos(page.content)

        return PageImpl(dtos, pageable, page.totalElements)
    }

    @Transactional
    fun update(id: Int, req: UpdateCustomerDto): CustomerDto {
        val entity = repository.findById(id).orElseThrow { NotFoundEntityException("Customer $id not found") }
        req.email?.let { email ->
            repository.findByEmail(email)?.let { existing ->
                if (existing.id != entity.id) {
                    throw ExistingEntityException("Email '$email' is already in use by customer ${existing.id}")
                }
            }
        }
        mapper.updateRequestOntoEntity(req, entity)
        val saved = repository.save(entity)
        logger.info("Updated customer id={}", saved.id)

        return mapper.entityToDto(saved)
    }

    @Transactional
    fun delete(id: Int) {
        if (!repository.existsById(id)) throw NotFoundEntityException("Customer $id not found")
        repository.deleteById(id)
        logger.info("Deleted customer id={}", id)
    }
}
