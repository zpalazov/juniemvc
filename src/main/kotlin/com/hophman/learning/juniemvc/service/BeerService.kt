package com.hophman.learning.juniemvc.service

import com.hophman.learning.juniemvc.model.BeerDto

interface BeerService {
    fun create(beer: BeerDto): BeerDto
    fun findAll(): List<BeerDto>
    fun findById(id: Int): BeerDto?
    fun update(id: Int, beer: BeerDto): BeerDto?
    fun delete(id: Int): Boolean
}
