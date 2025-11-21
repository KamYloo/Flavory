package com.flavory.userservice.mapper;

import com.flavory.userservice.dto.request.CreateAddressRequest;
import com.flavory.userservice.dto.request.UpdateAddressRequest;
import com.flavory.userservice.dto.response.AddressResponse;
import com.flavory.userservice.entity.Address;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AddressMapper {
    AddressResponse toResponse(Address address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Address toEntity(CreateAddressRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(UpdateAddressRequest request, @MappingTarget Address address);
}