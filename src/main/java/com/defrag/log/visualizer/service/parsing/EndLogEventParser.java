package com.defrag.log.visualizer.service.parsing;

import com.defrag.log.visualizer.model.LogEventType;
import com.defrag.log.visualizer.service.parsing.graylog.model.LogDefinition;
import org.springframework.stereotype.Service;

@Service
class EndLogEventParser implements LogEventParser {

    @Override
    public LogEventType eventType() {
        return LogEventType.ACTION_END;
    }

    @Override
    public void fill(LogDefinition.Builder logBuilder) {

    }
}
