package com.defrag.log.visualizer.rest.mapper;

import com.defrag.log.visualizer.model.LogRoot;
import com.defrag.log.visualizer.rest.dto.LogRootDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LogRootMapper {

    LogRootDto toDto(LogRoot source);
}
