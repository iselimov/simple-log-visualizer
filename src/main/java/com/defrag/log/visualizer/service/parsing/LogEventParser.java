package com.defrag.log.visualizer.service.parsing;

import com.defrag.log.visualizer.model.LogEventType;
import com.defrag.log.visualizer.service.parsing.graylog.model.LogDefinition;

import java.util.Set;

interface LogEventParser {

    LogEventType eventType();

    void fill(LogDefinition.Builder logBuilder);
}
