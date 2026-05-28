package com.example.notificationservice.meeting;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MeetingReadModelMapper {

    MeetingReadModelEntity toEntity(MeetingProfile profile);

    MeetingProfile toProfile(MeetingReadModelEntity entity);

    void updateEntity(MeetingProfile profile, @MappingTarget MeetingReadModelEntity entity);
}
