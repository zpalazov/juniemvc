package com.hopman.leaning.juniemvc.rest

import com.hopman.leaning.juniemvc.entity.BeerEntity
import com.hopman.leaning.juniemvc.service.BeerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/beers")
class BeerController(
    private val beerService: BeerService
) {

    @PostMapping
    fun create(@RequestBody beer: BeerEntity): ResponseEntity<BeerEntity> {
        val saved = beerService.create(beer)
        return ResponseEntity.created(URI.create("/api/beers/${saved.id}")).body(saved)
    }

    @GetMapping
    fun listAll(): List<BeerEntity> = beerService.findAll()

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int): ResponseEntity<BeerEntity> =
        beerService.findById(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
}
