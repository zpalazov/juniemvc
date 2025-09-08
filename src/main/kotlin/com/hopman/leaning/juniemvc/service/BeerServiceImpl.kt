package com.hopman.leaning.juniemvc.service

import com.hopman.leaning.juniemvc.entity.BeerEntity
import com.hopman.leaning.juniemvc.repo.BeerRepository
import org.springframework.stereotype.Service

@Service
class BeerServiceImpl(
    private val beerRepository: BeerRepository
) : BeerService {

    override fun create(beer: BeerEntity): BeerEntity = beerRepository.save(beer)

    override fun findAll(): List<BeerEntity> = beerRepository.findAll()

    override fun findById(id: Int): BeerEntity? = beerRepository.findById(id).orElse(null)
}
