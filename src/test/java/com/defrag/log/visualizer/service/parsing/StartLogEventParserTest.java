package com.defrag.log.visualizer.service.parsing;

import com.defrag.log.visualizer.model.LogEventType;
import com.defrag.log.visualizer.service.parsing.graylog.model.LogDefinition;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class StartLogEventParserTest {

    private StartLogEventParser parser = new StartLogEventParser();

    @Test
    public void fill() {
        LogDefinition.Builder logBuilder = new LogDefinition.Builder("some uid", LogEventType.ACTION_START, LocalDateTime.of(2019, 1, 1, 0, 0),
                "uuid:some uid action:ACTION_START invocationOrder:1 depth:10 name:com.some.package.TestAction args:  odId = 1, patientId=PAT997 ");

        parser.fill(logBuilder);

        LogDefinition result = logBuilder.build();
        assertEquals("some uid", result.getUid());
        assertEquals(LogEventType.ACTION_START, result.getEventType());
        assertEquals(LocalDateTime.of(2019, 1, 1, 0, 0), result.getTimestamp());
        assertEquals(1, result.getInvocationOrder().intValue());
        assertEquals(10, result.getDepth().intValue());
        assertEquals("com.some.package.TestAction", result.getActionName());
        assertEquals("odId = 1, patientId=PAT997", result.getArgs());
        assertEquals(997, result.getPatientId().longValue());
        assertEquals("uuid:some uid action:ACTION_START invocationOrder:1 depth:10 name:com.some.package.TestAction args:  odId = 1, patientId=PAT997 ", result.getFullMessage());
    }
}