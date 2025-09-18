package com.hophman.learning.juniemvc.repo

import com.hophman.learning.juniemvc.entity.BeerEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.math.BigDecimal

@DataJpaTest
class BeerRepositoryTests @Autowired constructor(
    val beerRepository: BeerRepository
) {

    @Test
    fun `when beer is created expect saved beer`() {
        val saved = beerRepository.save(newBeer())

        assertNotNull(saved.id)
        val found = beerRepository.findById(saved.id!!)
        assertTrue(found.isPresent)
        assertEquals(saved.upc, found.get().upc)
    }

    @Test
    fun `when beer is updated expect correct beer`() {
        val saved = beerRepository.save(newBeer())
        val id = saved.id!!
        val toUpdate = beerRepository.findById(id).orElseThrow()
        toUpdate.quantityOnHand = 24

        val updated = beerRepository.save(toUpdate)

        assertEquals(24, updated.quantityOnHand)
        // version should increment (optimistic locking)
        assertNotNull(updated.version)
    }

    @Test
    fun `delete beer`() {
        val one = beerRepository.save(newBeer(upc = "UPC-1"))
        val two = beerRepository.save(newBeer(upc = "UPC-2"))
        assertEquals(2, beerRepository.count())

        beerRepository.deleteById(one.id!!)
        assertEquals(1, beerRepository.count())

        beerRepository.delete(two)
        assertEquals(0, beerRepository.count())
    }

    private fun newBeer(name: String = "Test Lager", upc: String = System.nanoTime().toString()): BeerEntity =
        BeerEntity(
            name = name,
            style = "LAGER",
            upc = upc,
            quantityOnHand = 12,
            price = BigDecimal("9.99")
        )
}
