package com.hophman.learning.juniemvc.mapper

import com.hophman.learning.juniemvc.entity.BeerEntity
import com.hophman.learning.juniemvc.model.BeerDto
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget

@Mapper(componentModel = "spring")
interface BeerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    fun toEntity(dto: BeerDto): BeerEntity

    fun toDto(entity: BeerEntity): BeerDto

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    fun updateEntityFromDto(dto: BeerDto, @MappingTarget entity: BeerEntity): BeerEntity
}