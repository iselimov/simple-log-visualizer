package com.defrag.log.visualizer.service.parsing;

import com.defrag.log.visualizer.model.LogEventType;
import com.defrag.log.visualizer.service.parsing.graylog.model.LogDefinition;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ErrorLogEventParserTest {

    private ErrorLogEventParser parser = new ErrorLogEventParser();

    @Test
    public void fill() {
        LogDefinition.Builder logBuilder = new LogDefinition.Builder("some uid", LogEventType.ACTION_ERROR, LocalDateTime.of(2019, 1, 1, 0, 0),
                "uuid:some uid action:ACTION_ERROR invocationOrder:1 name:com.some.package.TestAction exception: NPE was thrown ");

        parser.fill(logBuilder);

        LogDefinition result = logBuilder.build();
        assertEquals("some uid", result.getUid());
        assertEquals(LogEventType.ACTION_ERROR, result.getEventType());
        assertEquals(LocalDateTime.of(2019, 1, 1, 0, 0), result.getTimestamp());
        assertEquals(1, result.getInvocationOrder());
        assertNull(result.getDepth());
        assertEquals("com.some.package.TestAction", result.getActionName());
        assertNull(result.getTiming());
        assertNull(result.getArgs());
        assertNull(result.getPatientId());
        assertEquals("NPE was thrown", result.getException());
        assertEquals("uuid:some uid action:ACTION_ERROR invocationOrder:1 name:com.some.package.TestAction exception: NPE was thrown ",
                result.getFullMessage());
    }
}
