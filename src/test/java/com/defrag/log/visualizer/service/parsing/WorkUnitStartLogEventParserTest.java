package com.defrag.log.visualizer.service.parsing;

import com.defrag.log.visualizer.model.LogEventType;
import com.defrag.log.visualizer.service.parsing.graylog.model.LogDefinition;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class WorkUnitStartLogEventParserTest {

    private WorkUnitStartLogEventParser parser = new WorkUnitStartLogEventParser();

    @Test
    public void fillStartWorkUnit() {
        LogDefinition.Builder logBuilder = new LogDefinition.Builder("some uid", LogEventType.WORK_UNIT_START,
                LocalDateTime.of(2019, 1, 1, 0, 0),
                "uuid:some uid action:WORK_UNIT_START invocationOrder:1 name: createSmth ");

        parser.fill(logBuilder);

        LogDefinition result = logBuilder.build();
        assertEquals("some uid", result.getUid());
        assertEquals(LogEventType.WORK_UNIT_START, result.getEventType());
        assertEquals(LocalDateTime.of(2019, 1, 1, 0, 0), result.getTimestamp());
        assertEquals(1, result.getInvocationOrder());
        assertNull(result.getDepth());
        assertEquals("createSmth", result.getActionName());
        assertNull(result.getArgs());
        assertNull(result.getPatientId());
        assertEquals("uuid:some uid action:WORK_UNIT_START invocationOrder:1 name: createSmth ", result.getFullMessage());
    }
}
