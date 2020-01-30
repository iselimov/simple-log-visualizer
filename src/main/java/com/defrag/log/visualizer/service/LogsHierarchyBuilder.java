package com.defrag.log.visualizer.service;

import com.defrag.log.visualizer.model.Log;
import com.defrag.log.visualizer.service.bo.LogNode;
import com.defrag.log.visualizer.service.bo.LogsHierarchy;

import java.util.*;

import static com.defrag.log.visualizer.model.LogEventType.*;

class LogsHierarchyBuilder {

    private final List<Log> logs;

    LogsHierarchyBuilder(List<Log> logs) {
        this.logs = new ArrayList<>(logs);
    }

    LogsHierarchy build() {
        if (logs.isEmpty()) {
            return LogsHierarchy.empty();
        }

        LogNode root = null;
        LogNode lastNode = null;

        for (Log log : logs) {
            if (log.getEventType() == ACTION_START || log.getEventType() == WORK_UNIT_START) {
                LogNode nextNode = LogNode.createNode(log.getId(), log.getActionName(), log.getFullMessage(), log.getTimestamp());

                if (root == null) {
                    root = nextNode;
                    lastNode = nextNode;
                    continue;
                }

                lastNode.addChild(nextNode);
                lastNode = nextNode;
            } else if (log.getEventType() == ACTION_END || log.getEventType() == WORK_UNIT_END) {
                lastNode.markAsFinished(log.getTimestamp());
                lastNode.setTiming(log.getTiming());
                if (lastNode.getParent() != null) {
                    lastNode = lastNode.getParent();
                }
            } else if (log.getEventType() == ACTION_ERROR) {
                LogNode errNode = LogNode.createErrorNode(log.getId(), log.getActionName(), log.getFullMessage(), log.getTimestamp());
                lastNode.addChild(errNode);
                break;
            } else {
                throw new IllegalStateException("Unknown marker");
            }
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
