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

    override fun update(id: Int, beer: BeerEntity): BeerEntity? {
        val existing = beerRepository.findById(id).orElse(null) ?: return null
        // Copy mutable fields (ignore id/version/createdDate)
        existing.name = beer.name
        existing.style = beer.style
        existing.upc = beer.upc
        existing.quantityOnHand = beer.quantityOnHand
        existing.price = beer.price
        return beerRepository.save(existing)
    }

    override fun delete(id: Int): Boolean {
        val exists = beerRepository.existsById(id)
        if (!exists) return false
        beerRepository.deleteById(id)
        return true
    }
}
