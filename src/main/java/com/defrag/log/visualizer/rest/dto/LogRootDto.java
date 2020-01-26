package com.defrag.log.visualizer.rest.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LogRootDto {

    private long id;

    private String payloadName;

    private String description;

    private long patient;

    private LocalDateTime startDate;

    private LocalDateTime endDate;
}
