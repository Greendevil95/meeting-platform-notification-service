package com.example.notificationservice.user;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserReadModelMapper {

    UserReadModelEntity toEntity(UserProfile profile);

    UserProfile toProfile(UserReadModelEntity entity);

    void updateEntity(UserProfile profile, @MappingTarget UserReadModelEntity entity);
}
