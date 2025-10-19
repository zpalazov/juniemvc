package com.hophman.learning.juniemvc.rest

import com.hophman.learning.juniemvc.mapper.CustomerMapper
import com.hophman.learning.juniemvc.service.CreateCustomerDto
import com.hophman.learning.juniemvc.service.CustomerService
import com.hophman.learning.juniemvc.service.UpdateCustomerDto
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/customers")
internal class CustomerController(
    private val service: CustomerService,
    private val mapper: CustomerMapper
) {

    @PostMapping
    fun create(@Valid @RequestBody req: CreateCustomerDto): ResponseEntity<CustomerResponse> {
        val created = service.create(req)
        val response = mapper.dtoToResponse(created)
        val location = "/api/v1/customers/${created.id}"

        return ResponseEntity.status(HttpStatus.CREATED)
            .header(HttpHeaders.LOCATION, location)
            .body(response)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Int): ResponseEntity<CustomerResponse> {
        val dto = service.getById(id)

        return ResponseEntity.ok(mapper.dtoToResponse(dto))
    }

    @GetMapping
    fun list(pageable: Pageable): Page<CustomerResponse> {
        val page = service.list(pageable)
        val responses = mapper.dtosToResponses(page.content)

        return PageImpl(responses, pageable, page.totalElements)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Int, @Valid @RequestBody req: UpdateCustomerDto): ResponseEntity<CustomerResponse> {
        val updated = service.update(id, req)

        return ResponseEntity.ok(mapper.dtoToResponse(updated))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Int) {
        service.delete(id)
    }
}
