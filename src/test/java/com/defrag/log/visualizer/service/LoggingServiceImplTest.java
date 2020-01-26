package com.defrag.log.visualizer.service;

import com.defrag.log.visualizer.model.Log;
import com.defrag.log.visualizer.model.LogRoot;
import com.defrag.log.visualizer.service.bo.LogNode;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoggingServiceImplTest {
    @Test
    public void t() {

    }

//    @Test
//    public void testCreateHierarchyWithoutException() {
//        LocalDate date = LocalDate.of(2019, 1, 1);
//
//        LogRoot lr = createLogRoot("P1", "P1 description", LocalDateTime.of(date, LocalTime.of(10, 1)));
//
//        List<Log> logs = Arrays.asList(
//                createLog("A1", "A1 description", LocalDateTime.of(date, LocalTime.of(10, 2)), LogMarker.START, lr),
//                createLog("A2", "A2 description", LocalDateTime.of(date, LocalTime.of(10, 3)), LogMarker.START, lr),
//                createLog("A2", "A2 description", LocalDateTime.of(date, LocalTime.of(10, 4)), LogMarker.FINISH, lr),
//                createLog("A3", "A3 description", LocalDateTime.of(date, LocalTime.of(10, 5)), LogMarker.START, lr),
//                createLog("A4", "A4 description", LocalDateTime.of(date, LocalTime.of(10, 6)), LogMarker.START, lr),
//                createLog("A4", "A4 description", LocalDateTime.of(date, LocalTime.of(10, 7)), LogMarker.FINISH, lr),
//                createLog("A3", "A3 description", LocalDateTime.of(date, LocalTime.of(10, 8)), LogMarker.FINISH, lr),
//                createLog("A1", "A1 description", LocalDateTime.of(date, LocalTime.of(10, 9)), LogMarker.FINISH, lr)
//        );
//
//        LogNode rootOfHierarchy = new LogsHierarchyBuilder(logs).build().getRoot();
//
//        assertLogNode("P1", LocalDateTime.of(date, LocalTime.of(10, 1)), null, null, rootOfHierarchy);
//
//        assertEquals(1, rootOfHierarchy.getChildren().size());
//        LogNode a1Node = rootOfHierarchy.getChildren().get(0);
//        assertLogNode("A1", LocalDateTime.of(date, LocalTime.of(10, 2)), LocalDateTime.of(date, LocalTime.of(10, 9)),
//                null, a1Node);
//
//        assertEquals(2, a1Node.getChildren().size());
//        LogNode a2Node = a1Node.getChildren().get(0);
//        assertLogNode("A2", LocalDateTime.of(date, LocalTime.of(10, 3)), LocalDateTime.of(date, LocalTime.of(10, 4)),
//                null, a2Node);
//
//        LogNode a3Node = a1Node.getChildren().get(1);
//        assertLogNode("A3", LocalDateTime.of(date, LocalTime.of(10, 5)), LocalDateTime.of(date, LocalTime.of(10, 8)),
//                null, a3Node);
//
//        assertTrue(a2Node.getChildren().isEmpty());
//
//        assertEquals(1, a3Node.getChildren().size());
//        LogNode a4Node = a3Node.getChildren().get(0);
//        assertLogNode("A4", LocalDateTime.of(date, LocalTime.of(10, 6)), LocalDateTime.of(date, LocalTime.of(10, 7)),
//                null, a4Node);
//    }
//
//    @Test
//    public void testCreateHierarchyWithException() {
//        LocalDate date = LocalDate.of(2019, 1, 1);
//
//        LogRoot lr = createLogRoot("P1", "P1 description", LocalDateTime.of(date, LocalTime.of(10, 1)));
//
//        List<Log> logs = Arrays.asList(
//                createLog("A1", "A1 description", LocalDateTime.of(date, LocalTime.of(10, 2)), LogMarker.START, lr),
//                createLog("A2", "A2 description", LocalDateTime.of(date, LocalTime.of(10, 3)), LogMarker.START, lr),
//                createLog("A2", "A2 description", LocalDateTime.of(date, LocalTime.of(10, 4)), LogMarker.FINISH, lr),
//                createLog("A3", "Exception happened during A3", LocalDateTime.of(date, LocalTime.of(10, 5)), LogMarker.EXCEPTION, lr)
//        );
//
//        LogNode rootOfHierarchy = new LogsHierarchyBuilder(logs).build().getRoot();
//
//        assertLogNode("P1", LocalDateTime.of(date, LocalTime.of(10, 1)), null, null, rootOfHierarchy);
//
//        assertEquals(1, rootOfHierarchy.getChildren().size());
//        LogNode a1Node = rootOfHierarchy.getChildren().get(0);
//        assertLogNode("A1", LocalDateTime.of(date, LocalTime.of(10, 2)), null, null, a1Node);
//
//        assertEquals(2, a1Node.getChildren().size());
//        LogNode a2Node = a1Node.getChildren().get(0);
//        assertLogNode("A2", LocalDateTime.of(date, LocalTime.of(10, 3)), LocalDateTime.of(date, LocalTime.of(10, 4)),
//                null, a2Node);
//
//        LogNode a3Node = a1Node.getChildren().get(1);
//        assertLogNode("A3", LocalDateTime.of(date, LocalTime.of(10, 5)), null, "Exception happened during A3", a3Node);
//
//    }
//
//    private void assertLogNode(String name, LocalDateTime startDate, LocalDateTime endDate, String exception, LogNode actual) {
//        assertEquals(name, actual.getName());
//        assertEquals(startDate, actual.getStartDate());
//        assertEquals(endDate, actual.getEndDate());
//        assertEquals(exception, actual.getException());
//    }
//
//    private LogRoot createLogRoot(String payloadName, String description, LocalDateTime startDate) {
//        LogRoot result = new LogRoot();
//
//        result.setPayloadName(payloadName);
//        result.setDescription(description);
//        result.setStartDate(startDate);
//
//        return result;
//    }
//
//    private Log createLog(String actionName, String description, LocalDateTime timestamp, LogMarker marker, LogRoot logRoot) {
//        Log result = new Log();
//
//        result.setActionName(actionName);
//        result.setDescription(description);
//        result.setTimestamp(timestamp);
//        result.setMarker(marker);
//        result.setRoot(logRoot);
//
//        return result;
//    }
}
