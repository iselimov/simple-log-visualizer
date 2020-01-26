package com.defrag.log.visualizer.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Types of logged events.
 */
@RequiredArgsConstructor
@Getter
public enum LogEventType {
    ACTION_START("ACTION_START"), ACTION_END("ACTION_END"), ACTION_ERROR("ACTION_ERROR"),
    SPARQL_QUERY("SPARQL_QUERY");

    private final String name;
}

