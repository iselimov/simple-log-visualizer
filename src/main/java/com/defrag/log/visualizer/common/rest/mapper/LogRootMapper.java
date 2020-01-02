package com.defrag.log.visualizer.common.rest.mapper;

import com.defrag.log.visualizer.common.model.LogRoot;
import com.defrag.log.visualizer.common.rest.dto.LogRootDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LogRootMapper {

    LogRootDto toDto(LogRoot source);
}
