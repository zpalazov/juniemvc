package com.hophman.learning.juniemvc.repo

import com.hophman.learning.juniemvc.entity.BeerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BeerRepository : JpaRepository<BeerEntity, Int>
