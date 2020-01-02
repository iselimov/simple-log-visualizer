package com.defrag.log.visualizer.common.service.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class LogsHierarchy {
    private final LogNode root;
    private final int depth;
    private final int breadth;

    public static LogsHierarchy createInstance(LogNode root, int depth, int breadth) {
        return new LogsHierarchy(root, depth, breadth);
    }

    public static LogsHierarchy empty() {
        return new LogsHierarchy(null, 0, 0);
    }
}
