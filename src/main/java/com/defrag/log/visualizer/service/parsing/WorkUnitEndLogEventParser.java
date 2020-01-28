package com.defrag.log.visualizer.service.parsing;

import com.defrag.log.visualizer.model.LogEventType;
import com.defrag.log.visualizer.service.parsing.graylog.model.LogDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.defrag.log.visualizer.service.parsing.LoggingConstants.INVOCATION_ORDER;
import static com.defrag.log.visualizer.service.parsing.LoggingConstants.NAME;
import static com.defrag.log.visualizer.service.parsing.utils.ParserUtils.positionAfterString;

@Service
@Slf4j
class WorkUnitEndLogEventParser implements LogEventParser {

    @Override
    public LogEventType eventType() {
        return LogEventType.WORK_UNIT_END;
    }

    @Override
    public void fill(LogDefinition.Builder logBuilder) {
        String parsingMessage = logBuilder.getFullMessage();
        int nextInd = 0;

        int ioPrefixInd = parsingMessage.indexOf(INVOCATION_ORDER, nextInd);
        if (ioPrefixInd == -1) {
            log.error("Invocation order was not found in {}", parsingMessage);
            return;
        }
        nextInd = ioPrefixInd;

        int nameInd = parsingMessage.indexOf(NAME, nextInd);
        if (nameInd == -1) {
            log.error("Name was not found in {}", parsingMessage);
            return;
        }

        String ioStr = parsingMessage.substring(positionAfterString(INVOCATION_ORDER, ioPrefixInd), nameInd).trim();
        try {
            logBuilder.invocationOrder(Integer.parseInt(ioStr));
        } catch (NumberFormatException e) {
            log.error("Couldn't parse invocation order {} in {}", ioStr, parsingMessage);
            return;
        }

        String name = parsingMessage.substring(positionAfterString(NAME, nameInd)).trim();
        logBuilder.actionName(name);
    }
}
