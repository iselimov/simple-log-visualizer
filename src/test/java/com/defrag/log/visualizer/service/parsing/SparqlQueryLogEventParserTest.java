package com.defrag.log.visualizer.service.parsing;

import com.defrag.log.visualizer.model.LogEventType;
import com.defrag.log.visualizer.service.parsing.graylog.model.LogDefinition;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SparqlQueryLogEventParserTest {

    private SparqlQueryLogEventParser parser = new SparqlQueryLogEventParser();

    @Test
    public void fill() {
        LogDefinition.Builder logBuilder = new LogDefinition.Builder("some uid", LogEventType.SPARQL_QUERY, LocalDateTime.of(2019, 1, 1, 0, 0),
                "uuid:some uid action:SPARQL_QUERY depth:10 timing: 753 ");

        parser.fill(logBuilder);

        LogDefinition result = logBuilder.build();
        assertEquals("some uid", result.getUid());
        assertEquals(LogEventType.SPARQL_QUERY, result.getEventType());
        assertEquals(LocalDateTime.of(2019, 1, 1, 0, 0), result.getTimestamp());
        assertEquals(Integer.MAX_VALUE, result.getInvocationOrder());
        assertEquals(10, result.getDepth().intValue());
        assertNull(result.getActionName());
        assertEquals(753, result.getTiming().intValue());
        assertNull(result.getArgs());
        assertNull(result.getPatientId());
        assertEquals("uuid:some uid action:SPARQL_QUERY depth:10 timing: 753 ",
                result.getFullMessage());
    }
}