package com.defrag.log.visualizer.rest.mapper;

import com.defrag.log.visualizer.model.GraylogSource;
import com.defrag.log.visualizer.rest.dto.GraylogSourceDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GraylogSourceMapper {

    GraylogSourceDto toDto(GraylogSource source);
}
