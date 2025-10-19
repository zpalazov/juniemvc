package com.hophman.learning.juniemvc.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.hophman.learning.juniemvc.rest.GlobalRestExceptionHandler
import com.hophman.learning.juniemvc.mapper.CustomerMapper
import com.hophman.learning.juniemvc.model.CustomerDto
import com.hophman.learning.juniemvc.service.CreateCustomerDto
import com.hophman.learning.juniemvc.service.CustomerService
import io.mockk.every
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import com.ninjasquad.springmockk.MockkBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [CustomerController::class])
@Import(GlobalRestExceptionHandler::class)
internal class CustomerControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper
) {

    @MockkBean
    private lateinit var service: CustomerService

    @MockkBean
    private lateinit var mapper: CustomerMapper

    @Test
    fun `POST create returns 201 with Location and body`() {
        val request = CreateCustomerDto(
            name = "John Doe",
            email = "john@example.com",
            phone = "+123",
            addressLine1 = "123 Main St",
            city = "Springfield",
            state = "IL",
            postalCode = "62704"
        )
        val dto = CustomerDto(
            id = 42,
            name = request.name,
            email = request.email,
            phone = request.phone,
            addressLine1 = request.addressLine1,
            addressLine2 = null,
            city = request.city,
            state = request.state,
            postalCode = request.postalCode,
            createdDate = null,
            updatedDate = null
        )
        val response = CustomerResponse(
            id = 42,
            name = request.name,
            email = request.email,
            phone = request.phone,
            addressLine1 = request.addressLine1,
            addressLine2 = null,
            city = request.city,
            state = request.state,
            postalCode = request.postalCode,
            createdDate = null,
            updatedDate = null
        )

        every { service.create(request) } returns dto
        every { mapper.dtoToResponse(dto) } returns response

        mockMvc.perform(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", "/api/v1/customers/42"))
            .andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.name").value("John Doe"))
    }

    @Test
    fun `POST create with malformed JSON returns 400 invalid body problem`() {
        val invalidJson = "{ invalid" // malformed JSON, triggers HttpMessageNotReadableException

        mockMvc.perform(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("Invalid request body"))
            .andExpect(jsonPath("$.detail", containsString("Unexpected")))
    }

    @Test
    fun `POST create with invalid fields returns 400 validation problem`() {
        val invalidPayload = mapOf(
            // name required and NotBlank
            "name" to "",
            // invalid email
            "email" to "not-an-email",
            // required fields blank
            "addressLine1" to "",
            "city" to "",
            "state" to "",
            "postalCode" to ""
        )

        mockMvc.perform(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPayload))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.detail", containsString("name: must not be blank")))
            .andExpect(jsonPath("$.detail", containsString("email: must be a well-formed email address")))
            .andExpect(jsonPath("$.detail", containsString("addressLine1: must not be blank")))
            .andExpect(jsonPath("$.detail", containsString("city: must not be blank")))
            .andExpect(jsonPath("$.detail", containsString("state: must not be blank")))
            .andExpect(jsonPath("$.detail", containsString("postalCode: must not be blank")))
    }

    @Test
    fun `GET by id returns 200 with body`() {
        val dto = CustomerDto(
            id = 7,
            name = "Jane",
            email = null,
            phone = null,
            addressLine1 = "a",
            addressLine2 = null,
            city = "c",
            state = "s",
            postalCode = "p",
            createdDate = null,
            updatedDate = null
        )
        val response = CustomerResponse(
            id = 7,
            name = "Jane",
            email = null,
            phone = null,
            addressLine1 = "a",
            addressLine2 = null,
            city = "c",
            state = "s",
            postalCode = "p",
            createdDate = null,
            updatedDate = null
        )
        every { service.getById(7) } returns dto
        every { mapper.dtoToResponse(dto) } returns response

        mockMvc.perform(get("/api/v1/customers/7"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(7))
    }
}
