package com.defrag.log.visualizer.service.parsing;

import com.defrag.log.visualizer.model.LogEventType;
import com.defrag.log.visualizer.service.parsing.graylog.model.LogDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.defrag.log.visualizer.service.parsing.LoggingConstants.NAME;
import static com.defrag.log.visualizer.service.parsing.utils.ParserUtils.positionAfterString;

@Service
@Slf4j
class WorkUnitStartLogEventParser implements LogEventParser {

    @Override
    public LogEventType eventType() {
        return LogEventType.WORK_UNIT_START;
    }

    @Override
    public void fill(LogDefinition.Builder logBuilder) {
        String parsingMessage = logBuilder.getFullMessage();

        int nameInd = parsingMessage.indexOf(NAME);
        if (nameInd == -1) {
            log.error("Name was not found in {}", parsingMessage);
            return;
        }

        String name = parsingMessage.substring(positionAfterString(NAME, nameInd)).trim();
        logBuilder.actionName(name);
    }
}
