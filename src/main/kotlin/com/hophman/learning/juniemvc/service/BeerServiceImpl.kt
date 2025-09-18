package com.hophman.learning.juniemvc.service

import com.hophman.learning.juniemvc.model.BeerDto
import com.hophman.learning.juniemvc.mapper.BeerMapper
import com.hophman.learning.juniemvc.repo.BeerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BeerServiceImpl(
    private val beerRepository: BeerRepository,
    private val beerMapper: BeerMapper
) : BeerService {

    @Transactional
    override fun create(beer: BeerDto): BeerDto {
        val entity = beerMapper.toEntity(beer)
        val saved = beerRepository.save(entity)
        return beerMapper.toDto(saved)
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<BeerDto> = beerRepository.findAll().map(beerMapper::toDto)

    @Transactional(readOnly = true)
    override fun findById(id: Int): BeerDto? = beerRepository.findById(id).map(beerMapper::toDto).orElse(null)

    @Transactional
    override fun update(id: Int, beer: BeerDto): BeerDto? {
        val existing = beerRepository.findById(id).orElse(null) ?: return null
        beerMapper.updateEntityFromDto(beer, existing)
        val saved = beerRepository.save(existing)

        return beerMapper.toDto(saved)
    }

    @Transactional
    override fun delete(id: Int): Boolean {
        val exists = beerRepository.existsById(id)
        if (!exists) return false
        beerRepository.deleteById(id)

        return true
    }
}
