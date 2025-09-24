package com.hophman.learning.juniemvc.service

import com.hophman.learning.juniemvc.entity.*
import com.hophman.learning.juniemvc.exception.NotFoundBeerException
import com.hophman.learning.juniemvc.mapper.BeerOrderMapper
import com.hophman.learning.juniemvc.model.BeerOrderDto
import com.hophman.learning.juniemvc.repo.BeerOrderRepository
import com.hophman.learning.juniemvc.repo.BeerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class JpaBeerOrderService(
    private val beerOrderRepository: BeerOrderRepository,
    private val beerRepository: BeerRepository
) : BeerOrderService {

    @Transactional
    override fun placeOrder(dto: PlaceBeerOrderDto): BeerOrderDto {
        validatePlaceOrderInput(dto)
        val beersById: Map<Int?, BeerEntity?> = findBeers(dto)
        val newOrder = BeerOrderEntity(
            customerRef = dto.customerRef,
            paymentAmount = null,
            status = BeerOrderStatus.NEW
        )
        dto.items.forEach { item -> newOrder.run { addLine(prepareNewOrderLine(beersById, item)) } }

        val savedNewOrder: BeerOrderEntity = beerOrderRepository.save(newOrder)
        /**
         * This line accesses the lines property, which is a lazily-loaded collection managed by JPA/Hibernate.
         * By calling .size, the code forces Hibernate to initialize the collection within the transaction, ensuring
         * that all associated BeerOrderLineEntity objects are loaded and available. This prevents potential
         * LazyInitializationException errors if the collection is accessed outside the transaction.
        **/
        savedNewOrder.lines.size

        return BeerOrderMapper.toDto(savedNewOrder)
    }

    private fun prepareNewOrderLine(beersById: Map<Int?, BeerEntity?>, item: OrderItemDto): BeerOrderLineEntity {
        val beer: BeerEntity? = beersById[item.beerId]
        checkNotNull(beer)
        checkNotNull(beer.id)
        val newOrderLine = BeerOrderLineEntity(
            id = BeerOrderLineId(beerOrderId = 0, beerId = beer.id!!),
            orderQuantity = item.quantity,
            status = BeerOrderLineStatus.NEW
        )
        newOrderLine.beer = beer

        return newOrderLine
    }

    private fun validatePlaceOrderInput(dto: PlaceBeerOrderDto) {
        require(dto.customerRef.isNotBlank()) { "customerRef must not be blank" }
        require(dto.items.isNotEmpty()) { "items must not be empty" }
        dto.items.forEach { item ->
            require(item.quantity > 0) { "quantity must be > 0 for beerId ${item.beerId}" }
        }
    }

    private fun findBeers(dto: PlaceBeerOrderDto): Map<Int?, BeerEntity?> {
        val beersById = beerRepository.findAllById(dto.items.map { it.beerId }).associateBy { it.id }
        validateBeersExist(dto, beersById)

        return beersById
    }

    private fun validateBeersExist(dto: PlaceBeerOrderDto, beersById: Map<Int?, BeerEntity>) {
        val missingBeerIds = dto.items.map { it.beerId }.filter { id -> !beersById.containsKey(id) }
        if (missingBeerIds.isNotEmpty()) {
            throw NotFoundBeerException("Beer(s) not found: ${missingBeerIds.joinToString()}")
        }
    }

    @Transactional(readOnly = true)
    override fun getOrder(id: Int): BeerOrderDto {
        val entity = beerOrderRepository.findById(id).orElseThrow { NotFoundBeerException("Order $id not found") }
        entity.lines.size

        return BeerOrderMapper.toDto(entity)
    }
}
