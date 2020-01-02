package com.defrag.log.visualizer.common.service.impl;

import com.defrag.log.visualizer.common.model.Log;
import com.defrag.log.visualizer.common.model.LogRoot;
import com.defrag.log.visualizer.common.model.LogSource;
import com.defrag.log.visualizer.common.service.LoggingService;
import com.defrag.log.visualizer.common.service.model.LogNode;
import com.defrag.log.visualizer.common.service.model.LogsHierarchy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import static com.defrag.log.visualizer.common.model.LogMarker.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoggingServiceImpl implements LoggingService {

    private final CustomLoggingService loggingService;

    @Override
    public List<LogSource> getSources() {
        return loggingService.getSources();
    }

    @Override
    public List<LogRoot> getRoots(long sourceId, LocalDateTime from, LocalDateTime to) {
        return loggingService.getRoots(sourceId, from, to);
    }

    @Override
    public LogsHierarchy getLogsHierarchy(long rootId) {
        List<Log> logs = loggingService.getLogs(rootId);
        return createHierarchy(logs);
    }

    LogsHierarchy createHierarchy(List<Log> logs) {
        if (logs.isEmpty()) {
            return LogsHierarchy.empty();
        }

        final LogRoot logRoot = logs.get(0).getRoot();
        final LogNode root = LogNode.createNode(logRoot.getPayloadName(), logRoot.getDescription(), logRoot.getStartDate());

        LogNode lastNode = root;
        for (Log log : logs) {
            if (log.getMarker() == START) {
                LogNode nextNode = LogNode.createNode(log.getActionName(), log.getDescription(), log.getTimestamp());
                lastNode.addChild(nextNode);
                lastNode = nextNode;
            } else if (log.getMarker() == FINISH) {
                if (lastNode.getParent() != null) {
                    lastNode.markAsFinished(log.getTimestamp());
                    lastNode = lastNode.getParent();
                }
            } else if (log.getMarker() == EXCEPTION) {
                LogNode errNode = LogNode.createErrorNode(log.getActionName(), log.getDescription(), log.getTimestamp());
                lastNode.addChild(errNode);
                break;
            } else {
                throw new IllegalStateException("Unknown marker");
            }
        }

        if (logRoot.getEndDate() != null) {
            root.markAsFinished(logRoot.getEndDate());
        }

        return toLogsHierarchy(root);
    }

    private LogsHierarchy toLogsHierarchy(LogNode root) {
        int depth = 0;
        int breadth = 0;

        Stack<Iterator<LogNode>> logNodes = new Stack<>();
        logNodes.add(Collections.singletonList(root).iterator());

        int currDepth = 0;
        Iterator<LogNode> currIter;
        while (!logNodes.isEmpty()) {
            currIter = logNodes.peek();

            if (currIter.hasNext()) {
                LogNode next = currIter.next();
                if (next.getChildren().isEmpty()) {
                    breadth++;
                } else {
                    logNodes.push(next.getChildren().iterator());
                    currDepth++;
                }
            } else {
                logNodes.pop();
                if (currDepth > depth) {
                    depth = currDepth;
                }
                currDepth--;
            }
        }

        return LogsHierarchy.createInstance(root, depth + 1, breadth);
    }
}
