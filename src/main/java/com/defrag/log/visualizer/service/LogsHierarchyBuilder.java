package com.defrag.log.visualizer.service;

import com.defrag.log.visualizer.model.Log;
import com.defrag.log.visualizer.model.LogRoot;
import com.defrag.log.visualizer.service.bo.LogNode;
import com.defrag.log.visualizer.service.bo.LogsHierarchy;

import java.util.*;
import java.util.stream.Collectors;

class LogsHierarchyBuilder {

    private final List<Log> logs;

    LogsHierarchyBuilder(List<Log> logs) {
        this.logs = logs
                .stream()
                .sorted(Comparator.comparing(Log::getInvocationOrder))
                .collect(Collectors.toList());
    }

    LogsHierarchy build() {
        if (logs.isEmpty()) {
            return LogsHierarchy.empty();
        }

        final LogRoot logRoot = logs.get(0).getRoot();
        final LogNode root = null;
//                = LogNode.createRootNode(logRoot.getId(), logRoot.getPayloadName(), logRoot.getDescription(), logRoot.getStartDate());

        LogNode lastNode = root;
        for (Log log : logs) {
//            if (log.getMarker() == START) {
//                LogNode nextNode = LogNode.createNode(log.getId(), log.getActionName(), log.getDescription(), log.getTimestamp());
//                lastNode.addChild(nextNode);
//                lastNode = nextNode;
//            } else if (log.getMarker() == FINISH) {
//                if (lastNode.getParent() != null) {
//                    lastNode.markAsFinished(log.getTimestamp());
//                    lastNode = lastNode.getParent();
//                }
//            } else if (log.getMarker() == EXCEPTION) {
//                LogNode errNode = LogNode.createErrorNode(log.getId(), log.getActionName(), log.getDescription(), log.getTimestamp());
//                lastNode.addChild(errNode);
//                break;
//            } else {
//                throw new IllegalStateException("Unknown marker");
//            }
        }

//        if (logRoot.getEndDate() != null) {
//            root.markAsFinished(logRoot.getEndDate());
//        }

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
