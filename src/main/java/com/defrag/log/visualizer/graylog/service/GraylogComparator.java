package com.defrag.log.visualizer.graylog.service;

import com.defrag.log.visualizer.common.model.Log;
import com.defrag.log.visualizer.common.model.LogMarker;
import com.defrag.log.visualizer.common.service.LoggingConstants;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

class GraylogComparator implements Comparator<Log> {

    private final SortedSet<LogActionWithTimestamp> sortedStartLogs = new TreeSet<>();
    private final Map<LogActionWithTimestamp, LogActionWithTimestamp> logsWithStartLogs = new HashMap<>();

    GraylogComparator(List<Log> logs) {
        fillStartLogs(logs);
        boundLogsWithTheirStart(logs);
    }

    @Override
    public int compare(Log first, Log second) {
        if (first.getTimestamp().isBefore(second.getTimestamp())) {
            return -1;
        }
        if (first.getTimestamp().isAfter(second.getTimestamp())) {
            return 1;
        }

        LogActionWithTimestamp firstLogWithTimestamp = toLogWithTimestamp(first);
        LogActionWithTimestamp secondLogWithTimestamp = toLogWithTimestamp(second);
        if (firstLogWithTimestamp.equals(secondLogWithTimestamp)) {
            return 0;
        }

        LogActionWithTimestamp firstStartLog = first.getMarker() == LogMarker.START ? firstLogWithTimestamp
                : logsWithStartLogs.get(firstLogWithTimestamp);
        LogActionWithTimestamp secondStartLog = second.getMarker() == LogMarker.START ? secondLogWithTimestamp
                : logsWithStartLogs.get(secondLogWithTimestamp);

        boolean secondStartIsEarlierThanFirst = sortedStartLogs.headSet(firstStartLog).contains(secondStartLog);
        if (first.getMarker() == LogMarker.FINISH && second.getMarker() == LogMarker.FINISH) {
            return secondStartIsEarlierThanFirst ? -1 : 1;
        } else if (first.getMarker() == LogMarker.FINISH && second.getMarker() != LogMarker.FINISH) {
            return secondStartIsEarlierThanFirst ? 1 : -1;
        } else if (first.getMarker() != LogMarker.FINISH && second.getMarker() == LogMarker.FINISH) {
            return secondStartIsEarlierThanFirst ? 1 : -1;
        }
        return 0;
    }

    private void fillStartLogs(List<Log> logs) {
        logs
                .stream()
                .filter(l -> l.getMarker() == LogMarker.START)
                .map(this::toLogWithTimestamp)
                .forEach(sortedStartLogs::add);
    }

    private void boundLogsWithTheirStart(List<Log> logs) {
        List<Log> sortedByTimestamp = logs
                .stream()
                .sorted(LogTimestampAndMarkerComparator.INSTANCE)
                .collect(Collectors.toList());
        for (int i = 0; i < sortedByTimestamp.size(); i++) {
            Log nonStartLog = sortedByTimestamp.get(i);
            if (nonStartLog.getMarker() == LogMarker.START) {
                continue;
            }

            for (int j = i - 1; j >= 0; j--) {
                final Log currStartLog = sortedByTimestamp.get(j);
                if (currStartLog.getMarker() != LogMarker.START) {
                    continue;
                }

                String currStartLogDescription = currStartLog.getDescription().replaceFirst(LoggingConstants.START_ACTION, "");
                if (currStartLog.getRoot().equals(nonStartLog.getRoot())
                        && currStartLog.getActionName().equals(nonStartLog.getActionName())
                        && nonStartLog.getDescription().contains(currStartLogDescription)) {
                    sortedByTimestamp.remove(j);
                    i--;
                    logsWithStartLogs.put(toLogWithTimestamp(nonStartLog), toLogWithTimestamp(currStartLog));
                    break;
                }
            }
        }
    }

    private LogActionWithTimestamp toLogWithTimestamp(Log log) {
        return new LogActionWithTimestamp(log.getActionName(), log.getDescription(), log.getRoot().getId(), log.getTimestamp());
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class LogActionWithTimestamp implements Comparable<LogActionWithTimestamp> {
        private final String actionName;
        private final String description;
        private final long rootId;
        private final LocalDateTime timestamp;

        @Override
        public int compareTo(LogActionWithTimestamp other) {
            if (timestamp.isBefore(other.timestamp)) {
                return -1;
            }
            if (timestamp.isAfter(other.timestamp)) {
                return 1;
            }

            return 0;
        }
    }

    private static class LogTimestampAndMarkerComparator implements Comparator<Log> {

        static final LogTimestampAndMarkerComparator INSTANCE = new LogTimestampAndMarkerComparator();

        @Override
        public int compare(Log first, Log second) {
            if (first.getTimestamp().isBefore(second.getTimestamp())) {
                return -1;
            }
            if (first.getTimestamp().isAfter(second.getTimestamp())) {
                return 1;
            }

            if (first.getMarker() == LogMarker.START) {
                return -1;
            }
            if (second.getMarker() == LogMarker.START) {
                return 1;
            }

            return first.getActionName().compareTo(second.getActionName());
        }
    }

}
