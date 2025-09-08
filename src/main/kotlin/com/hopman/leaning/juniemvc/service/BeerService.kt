package com.hopman.leaning.juniemvc.service

import com.hopman.leaning.juniemvc.entity.BeerEntity

interface BeerService {
    fun create(beer: BeerEntity): BeerEntity
    fun findAll(): List<BeerEntity>
    fun findById(id: Int): BeerEntity?
}
