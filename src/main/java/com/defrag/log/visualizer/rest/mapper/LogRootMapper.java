package com.defrag.log.visualizer.rest.mapper;

import com.defrag.log.visualizer.rest.dto.LogRootDto;
import com.defrag.log.visualizer.service.bo.LogRootSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LogRootMapper {

    @Mapping(source = "logRoot.id", target = "id")
    @Mapping(source = "logRoot.uid", target = "uid")
    @Mapping(source = "logRoot.firstActionDate", target = "firstActionDate")
    @Mapping(source = "logRoot.lastActionDate", target = "lastActionDate")
    LogRootDto toDto(LogRootSummary source);
}
