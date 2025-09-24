package com.hophman.learning.juniemvc.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.hophman.learning.juniemvc.model.BeerOrderDto
import com.hophman.learning.juniemvc.model.BeerOrderLineDto
import com.hophman.learning.juniemvc.service.BeerOrderService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.LocalDateTime

@WebMvcTest(controllers = [BeerOrderController::class])
@Import(BeerOrderControllerTest.StubConfig::class)
class BeerOrderControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var service: BeerOrderService

    @TestConfiguration
    internal class StubConfig {
        @Bean
        fun beerOrderRestMapper(): BeerOrderRestMapper = object : BeerOrderRestMapper {
            override fun toDto(req: PlaceBeerOrderRequest) = com.hophman.learning.juniemvc.service.PlaceBeerOrderDto(
                req.customerRef,
                req.items.map { com.hophman.learning.juniemvc.service.OrderItemDto(it.beerId!!, it.quantity!!) }
            )
            override fun toDto(req: OrderItemRequest) = com.hophman.learning.juniemvc.service.OrderItemDto(req.beerId!!, req.quantity!!)
            override fun toResponse(dto: BeerOrderDto) = BeerOrderResponse(
                id = dto.id,
                customerRef = dto.customerRef,
                status = dto.status,
                paymentAmount = dto.paymentAmount,
                createdDate = dto.createdDate?.toString(),
                updatedDate = dto.updatedDate?.toString(),
                lines = dto.lines.map { toResponse(it) }
            )
            override fun toResponse(dto: BeerOrderLineDto) = BeerOrderLineResponse(
                beerId = dto.beerId,
                beerName = dto.beerName,
                orderQuantity = dto.orderQuantity,
                status = dto.status
            )
        }
    }

    @Test
    fun postCreatesOrder() {
        val req = PlaceBeerOrderRequest(
            customerRef = "c1",
            items = listOf(OrderItemRequest(beerId = 1, quantity = 2))
        )
        val dto = BeerOrderDto(
            id = 11,
            customerRef = "c1",
            status = "NEW",
            paymentAmount = null,
            createdDate = LocalDateTime.of(2025,1,1,10,0),
            updatedDate = null,
            lines = listOf(BeerOrderLineDto(1, "A", 2, "NEW"))
        )
        `when`(service.placeOrder(com.hophman.learning.juniemvc.service.PlaceBeerOrderDto("c1", listOf(com.hophman.learning.juniemvc.service.OrderItemDto(1,2))))).thenReturn(dto)

        mockMvc.perform(
            post("/api/v1/beer-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(11))
            .andExpect(jsonPath("$.lines[0].beerId").value(1))
    }

    @Test
    fun getReturns200() {
        val dto = BeerOrderDto(
            id = 11,
            customerRef = "c1",
            status = "NEW",
            paymentAmount = BigDecimal.ZERO,
            createdDate = LocalDateTime.of(2025,1,1,10,0),
            updatedDate = null,
            lines = listOf(BeerOrderLineDto(1, "A", 2, "NEW"))
        )
        `when`(service.getOrder(11)).thenReturn(dto)

        mockMvc.perform(get("/api/v1/beer-orders/11"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(11))
    }
}
