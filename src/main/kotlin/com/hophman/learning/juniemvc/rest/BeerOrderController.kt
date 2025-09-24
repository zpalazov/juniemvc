package com.hophman.learning.juniemvc.rest

import com.hophman.learning.juniemvc.service.BeerOrderService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/beer-orders")
internal class BeerOrderController(
    private val service: BeerOrderService,
    private val restMapper: BeerOrderRestMapper
) {

    @PostMapping
    fun place(@Valid @RequestBody req: PlaceBeerOrderRequest): ResponseEntity<BeerOrderResponse> {
        val created = service.placeOrder(restMapper.toDto(req))
        return ResponseEntity.status(HttpStatus.CREATED).body(restMapper.toResponse(created))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Int): ResponseEntity<BeerOrderResponse> {
        val dto = service.getOrder(id)
        return ResponseEntity.ok(restMapper.toResponse(dto))
    }
}
