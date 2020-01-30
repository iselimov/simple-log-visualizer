package com.defrag.log.visualizer.rest.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LogRootDto {

    private long id;

    private String uid;

    private Long patient;

    private String firstActionName;

    private LocalDateTime firstActionDate;

    private LocalDateTime lastActionDate;
}
