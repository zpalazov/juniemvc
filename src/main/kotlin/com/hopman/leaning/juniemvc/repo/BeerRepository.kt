package com.hopman.leaning.juniemvc.repo

import com.hopman.leaning.juniemvc.entity.BeerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BeerRepository : JpaRepository<BeerEntity, Int>
