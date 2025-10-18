package com.hophman.learning.juniemvc.mapper

import com.hophman.learning.juniemvc.entity.CustomerEntity
import com.hophman.learning.juniemvc.model.CustomerDto
import com.hophman.learning.juniemvc.rest.CustomerResponse
import com.hophman.learning.juniemvc.service.CreateCustomerDto
import com.hophman.learning.juniemvc.service.UpdateCustomerDto
import org.mapstruct.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
internal interface CustomerMapper {

    // Entity -> Service DTO
    fun entityToDto(entity: CustomerEntity): CustomerDto
    fun entitiesToDtos(entities: List<CustomerEntity>): List<CustomerDto>

    // Service DTO -> REST DTO
    @Mappings(
        Mapping(source = "createdDate", target = "createdDate", qualifiedByName = ["dateToString"]),
        Mapping(source = "updatedDate", target = "updatedDate", qualifiedByName = ["dateToString"])
    )
    fun dtoToResponse(dto: CustomerDto): CustomerResponse
    fun dtosToResponses(dtos: List<CustomerDto>): List<CustomerResponse>

    // Requests -> Entity
    fun createRequestToEntity(req: CreateCustomerDto): CustomerEntity

    fun updateRequestOntoEntity(req: UpdateCustomerDto, @MappingTarget entity: CustomerEntity)

    @Named("dateToString")
    fun dateToString(date: LocalDateTime?): String? = date?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}
