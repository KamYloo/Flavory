package com.flavory.userservice.mapper;

import com.flavory.userservice.dto.request.UpdateUserRequest;
import com.flavory.userservice.dto.response.UserResponse;
import com.flavory.userservice.entity.User;
import com.flavory.userservice.event.outbound.UserUpdatedEvent;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(
        componentModel = "spring",
        uses = {AddressMapper.class},
        imports = {UUID.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserResponse toResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "auth0Id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isVerified", ignore = true)
    void updateEntityFromDto(UpdateUserRequest request, @MappingTarget User user);

    @Mapping(target = "eventId", expression = "java(UUID.randomUUID().toString())")
    UserUpdatedEvent toUserUpdatedEvent(User user);
}