package com.hophman.learning.juniemvc.repo

import com.hophman.learning.juniemvc.entity.BeerOrderEntity
import org.springframework.data.jpa.repository.JpaRepository

interface BeerOrderRepository : JpaRepository<BeerOrderEntity, Int>
