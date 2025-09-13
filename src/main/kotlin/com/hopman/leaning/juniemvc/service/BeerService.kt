package com.hopman.leaning.juniemvc.service

import com.hopman.leaning.juniemvc.entity.BeerEntity

interface BeerService {
    fun create(beer: BeerEntity): BeerEntity
    fun findAll(): List<BeerEntity>
    fun findById(id: Int): BeerEntity?
    fun update(id: Int, beer: BeerEntity): BeerEntity?
    fun delete(id: Int): Boolean
}
