package com.hopman.leaning.juniemvc.service

import com.hopman.leaning.juniemvc.entity.BeerEntity
import com.hopman.leaning.juniemvc.repo.BeerRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.*
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockitoExtension::class)
class BeerServiceTest {

    @Mock
    lateinit var beerRepository: BeerRepository

    lateinit var service: BeerService

    @Captor
    lateinit var captor: ArgumentCaptor<BeerEntity>

    @BeforeEach
    fun setUp() {
        service = BeerServiceImpl(beerRepository)
        captor = ArgumentCaptor.forClass(BeerEntity::class.java)
    }

    @Test 
    fun `update returns updated entity when id exists`() {
        val existing = BeerEntity(
            id = 1,
            name = "Old",
            style = "LAGER",
            upc = "UPC-1",
            quantityOnHand = 5,
            price = BigDecimal("1.99")
        )
        val input = BeerEntity(
            name = "New",
            style = "IPA",
            upc = "UPC-2",
            quantityOnHand = 10,
            price = BigDecimal("2.49")
        )
        given(beerRepository.findById(1)).willReturn(Optional.of(existing))
        given(beerRepository.save(any(BeerEntity::class.java))).willAnswer { it.getArgument<BeerEntity>(0) }

        val result = service.update(1, input)

        assertNotNull(result)
        assertEquals("New", result!!.name)
        assertEquals("IPA", result.style)
        assertEquals("UPC-2", result.upc)
        assertEquals(10, result.quantityOnHand)
        assertEquals(BigDecimal("2.49"), result.price)

        then(beerRepository).should().save(captor.capture())
        val saved = captor.value
        assertEquals("New", saved.name)
    }

    @Test
    fun `update returns null when id not found`() {
        given(beerRepository.findById(99)).willReturn(Optional.empty())
        val result = service.update(99, BeerEntity(name = "X", style = "S", upc = "U", price = BigDecimal.ONE))
        assertNull(result)
        then(beerRepository).should(never()).save(any())
    }

    @Test
    fun `delete returns true when exists and invokes repository`() {
        given(beerRepository.existsById(1)).willReturn(true)
        val result = service.delete(1)
        assertTrue(result)
        then(beerRepository).should().deleteById(1)
    }

    @Test
    fun `delete returns false when not exists`() {
        given(beerRepository.existsById(2)).willReturn(false)
        val result = service.delete(2)
        assertFalse(result)
        then(beerRepository).should(never()).deleteById(anyInt())
    }
}
