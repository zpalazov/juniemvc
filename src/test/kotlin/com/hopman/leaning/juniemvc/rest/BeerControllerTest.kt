package com.hopman.leaning.juniemvc.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.hopman.leaning.juniemvc.entity.BeerEntity
import com.hopman.leaning.juniemvc.repo.BeerRepository
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
class BeerControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val beerRepository: BeerRepository,
) {

    @Test
    fun `POST create beer returns 201 and persists`() {
        val beer = newBeer()
        val json = objectMapper.writeValueAsString(beer)

        mockMvc.perform(post("/api/beers").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", matchesRegex("/api/beers/\\d+")))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.upc", `is`(beer.upc)))

        // verify it was actually saved
        assertTrue(beerRepository.findAll().any { it.upc == beer.upc })
    }

    @Test
    fun `GET list all beers`() {
        beerRepository.save(newBeer(upc = "UPC-GET-1"))
        beerRepository.save(newBeer(upc = "UPC-GET-2"))

        mockMvc.perform(get("/api/beers"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(greaterThanOrEqualTo(2))))
    }

    @Test
    fun `GET beer by id returns 200 or 404`() {
        val saved = beerRepository.save(newBeer(upc = "UPC-ONE"))

        mockMvc.perform(get("/api/beers/${saved.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", `is`(saved.id)))

        mockMvc.perform(get("/api/beers/999999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `PUT update beer returns 200 or 404`() {
        val saved = beerRepository.save(newBeer(upc = "UPC-UPD-1"))
        val updated = BeerEntity(
            name = "Updated Name",
            style = "IPA",
            upc = "UPC-UPD-2",
            quantityOnHand = 20,
            price = BigDecimal("10.49")
        )
        val json = objectMapper.writeValueAsString(updated)

        mockMvc.perform(put("/api/beers/${saved.id}").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name", `is`("Updated Name")))
            .andExpect(jsonPath("$.style", `is`("IPA")))
            .andExpect(jsonPath("$.upc", `is`("UPC-UPD-2")))
            .andExpect(jsonPath("$.quantityOnHand", `is`(20)))

        mockMvc.perform(put("/api/beers/999999").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `DELETE beer returns 204 or 404`() {
        val saved = beerRepository.save(newBeer(upc = "UPC-DEL-1"))

        mockMvc.perform(delete("/api/beers/${saved.id}"))
            .andExpect(status().isNoContent)

        assertFalse(beerRepository.findById(saved.id!!).isPresent)

        mockMvc.perform(delete("/api/beers/999999"))
            .andExpect(status().isNotFound)
    }

    private fun newBeer(name: String = "Test Lager", upc: String = System.nanoTime().toString()) =
        BeerEntity(
            name = name,
            style = "LAGER",
            upc = upc,
            quantityOnHand = 12,
            price = BigDecimal("9.99")
        )
}
