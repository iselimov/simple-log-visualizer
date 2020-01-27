package com.defrag.log.visualizer.service.parsing;

import com.defrag.log.visualizer.model.LogEventType;
import com.defrag.log.visualizer.service.parsing.graylog.model.LogDefinition;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EndLogEventParserTest {

    private EndLogEventParser parser = new EndLogEventParser();

    @Test
    public void fill() {
        LogDefinition.Builder logBuilder = new LogDefinition.Builder("some uid", LogEventType.ACTION_END, LocalDateTime.of(2019, 1, 1, 0, 0),
                "uuid:some uid action:ACTION_END invocationOrder:1 depth:10 name:com.some.package.TestAction timing: 753");

        parser.fill(logBuilder);

        LogDefinition result = logBuilder.build();
        assertEquals("some uid", result.getUid());
        assertEquals(LogEventType.ACTION_END, result.getEventType());
        assertEquals(LocalDateTime.of(2019, 1, 1, 0, 0), result.getTimestamp());
        assertEquals(1, result.getInvocationOrder());
        assertEquals(10, result.getDepth().intValue());
        assertEquals("com.some.package.TestAction", result.getActionName());
        assertEquals(753, result.getTiming().intValue());
        assertNull(result.getArgs());
        assertNull(result.getPatientId());
        assertEquals("uuid:some uid action:ACTION_END invocationOrder:1 depth:10 name:com.some.package.TestAction timing: 753",
                result.getFullMessage());
    }
}