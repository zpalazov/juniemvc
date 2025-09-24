package com.hophman.learning.juniemvc.service

import com.hophman.learning.juniemvc.entity.*
import com.hophman.learning.juniemvc.exception.NotFoundBeerException
import com.hophman.learning.juniemvc.repo.BeerOrderRepository
import com.hophman.learning.juniemvc.repo.BeerRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.util.*

class BeerOrderServiceTest {

    private val beerOrderRepository: BeerOrderRepository = mock()
    private val beerRepository: BeerRepository = mock()
    private val service: BeerOrderService = JpaBeerOrderService(beerOrderRepository, beerRepository)

    @Test
    fun `placeOrder happy path creates order with correct lines`() {
        val beer1 = BeerEntity(id = 1, name = "A", style = "IPA", upc = "u1", price = BigDecimal.ONE)
        val beer2 = BeerEntity(id = 2, name = "B", style = "Lager", upc = "u2", price = BigDecimal.ONE)
        `when`(beerRepository.findAllById(listOf(1,2))).thenReturn(listOf(beer1, beer2))
        `when`(beerOrderRepository.save(any(BeerOrderEntity::class.java))).thenAnswer { invocation ->
            val entity = invocation.arguments[0] as BeerOrderEntity
            entity.id = 10
            entity
        }

        val dto = PlaceBeerOrderDto(
            customerRef = "cust-1",
            items = listOf(
                OrderItemDto(beerId = 1, quantity = 3),
                OrderItemDto(beerId = 2, quantity = 2)
            )
        )

        val result = service.placeOrder(dto)

        assertEquals(10, result.id)
        assertEquals(2, result.lines.size)
        assertEquals(3, result.lines.first { it.beerId == 1 }.orderQuantity)
        assertEquals(2, result.lines.first { it.beerId == 2 }.orderQuantity)
    }

    @Test
    fun `placeOrder fails when beer id missing`() {
        `when`(beerRepository.findAllById(listOf(99))).thenReturn(emptyList())
        val dto = PlaceBeerOrderDto("cust", listOf(OrderItemDto(99, 1)))
        assertThrows(NotFoundBeerException::class.java) {
            service.placeOrder(dto)
        }
    }

    @Test
    fun `placeOrder rejects non-positive quantities`() {
        val dto = PlaceBeerOrderDto("cust", listOf(OrderItemDto(1, 0)))
        val ex = assertThrows(IllegalArgumentException::class.java) {
            service.placeOrder(dto)
        }
        assertTrue(ex.message!!.contains("quantity must be > 0"))
    }

    @Test
    fun `getOrder returns order, missing throws`() {
        val entity = BeerOrderEntity(id = 5, customerRef = "c", paymentAmount = null, status = BeerOrderStatus.NEW)
        val line = BeerOrderLineEntity(
            id = BeerOrderLineId(beerOrderId = 5, beerId = 1),
            orderQuantity = 2,
            status = BeerOrderLineStatus.NEW
        )
        val beer = BeerEntity(id = 1, name = "A", style = "IPA", upc = "u1", price = BigDecimal.ONE)
        line.beer = beer
        entity.addLine(line)
        `when`(beerOrderRepository.findById(5)).thenReturn(Optional.of(entity))
        val dto = service.getOrder(5)
        assertEquals(5, dto.id)
        assertEquals(1, dto.lines.size)

        `when`(beerOrderRepository.findById(6)).thenReturn(Optional.empty())
        assertThrows(NotFoundBeerException::class.java) { service.getOrder(6) }
    }
}
