package com.hophman.learning.juniemvc.rest

import com.hophman.learning.juniemvc.model.BeerOrderDto
import com.hophman.learning.juniemvc.model.BeerOrderLineDto
import com.hophman.learning.juniemvc.service.OrderItemDto
import com.hophman.learning.juniemvc.service.PlaceBeerOrderDto
import org.mapstruct.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
internal interface BeerOrderRestMapper {

    fun toDto(req: PlaceBeerOrderRequest): PlaceBeerOrderDto
    fun toDto(req: OrderItemRequest): OrderItemDto

    @Mappings(
        Mapping(source = "createdDate", target = "createdDate", qualifiedByName = ["dateToString"]),
        Mapping(source = "updatedDate", target = "updatedDate", qualifiedByName = ["dateToString"])
    )
    fun toResponse(dto: BeerOrderDto): BeerOrderResponse

    fun toResponse(dto: BeerOrderLineDto): BeerOrderLineResponse

    @Named("dateToString")
    fun dateToString(date: LocalDateTime?): String? = date?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}
