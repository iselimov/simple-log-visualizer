package com.defrag.log.visualizer.service.parsing;

import com.defrag.log.visualizer.model.LogEventType;
import com.defrag.log.visualizer.service.parsing.graylog.model.LogDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.defrag.log.visualizer.service.parsing.LoggingConstants.*;
import static com.defrag.log.visualizer.service.parsing.utils.ParserUtils.positionAfterString;

@Service
@Slf4j
public class ErrorLogEventParser implements LogEventParser {

    @Override
    public LogEventType eventType() {
        return LogEventType.ACTION_ERROR;
    }

    @Override
    public void fill(LogDefinition.Builder logBuilder) {
        int nextInd = 0;
        String parsingMessage = logBuilder.getFullMessage();

        int ioPrefixInd = parsingMessage.indexOf(INVOCATION_ORDER, nextInd);
        if (ioPrefixInd == -1) {
            return;
        }
        nextInd = ioPrefixInd;

        int nameInd = parsingMessage.indexOf(NAME, nextInd);
        if (nameInd == -1) {
            return;
        }
        nextInd = nameInd;

        String ioStr = parsingMessage.substring(positionAfterString(INVOCATION_ORDER, ioPrefixInd), nameInd).trim();
        try {
            logBuilder.invocationOrder(Integer.parseInt(ioStr));
        } catch (NumberFormatException e) {
            log.error("Couldn't parse invocation order {} in {}", ioStr, parsingMessage);
            return;
        }

        int exceptionInd = parsingMessage.indexOf(EXCEPTION, nextInd);
        if (exceptionInd == -1) {
            return;
        }

        String name = parsingMessage.substring(positionAfterString(NAME, nameInd), exceptionInd).trim();
        logBuilder.actionName(name);

        String exception = parsingMessage.substring(positionAfterString(EXCEPTION, exceptionInd)).trim();
        logBuilder.exception(exception);
    }
}
