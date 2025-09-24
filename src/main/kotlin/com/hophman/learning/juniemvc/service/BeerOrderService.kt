package com.hophman.learning.juniemvc.service

import com.hophman.learning.juniemvc.model.BeerOrderDto

interface BeerOrderService {

    fun placeOrder(dto: PlaceBeerOrderDto): BeerOrderDto

    fun getOrder(id: Int): BeerOrderDto
}
