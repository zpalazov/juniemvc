package com.hophman.learning.juniemvc.rest

import com.hophman.learning.juniemvc.model.BeerDto
import com.hophman.learning.juniemvc.service.BeerService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/beers")
class BeerController(
    private val beerService: BeerService
) {

    @PostMapping
    fun create(@Valid @RequestBody beer: BeerDto): ResponseEntity<BeerDto> {
        val saved = beerService.create(beer)
        return ResponseEntity.created(URI.create("/api/beers/${saved.id}")).body(saved)
    }

    @GetMapping
    fun listAll(): List<BeerDto> = beerService.findAll()

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int): ResponseEntity<BeerDto> =
        beerService.findById(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @PutMapping("/{id}")
    fun update(@PathVariable id: Int, @Valid @RequestBody beer: BeerDto): ResponseEntity<BeerDto> =
        beerService.update(id, beer)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<Void> =
        if (beerService.delete(id)) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()
}
