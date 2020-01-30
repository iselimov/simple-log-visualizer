package com.defrag.log.visualizer.service.bo;

import com.defrag.log.visualizer.model.LogRoot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class LogRootSummary {

    private final LogRoot logRoot;
    private Long patient;
    private String firstActionName;
}
