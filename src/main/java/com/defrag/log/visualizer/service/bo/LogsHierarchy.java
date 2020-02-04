package com.defrag.log.visualizer.service.bo;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class LogsHierarchy {
    private final LogNode root;
    private final int depth;
    private final int breadth;
    private final long minRealTiming;
    private final long maxRealTiming;

    private boolean notFullFilled;
    private boolean errorsContain;

    public static LogsHierarchy createInstance(LogNode root, int depth, int breadth, long minRealTiming, long maxRealTiming) {
        return new LogsHierarchy(root, depth, breadth, minRealTiming, maxRealTiming);
    }

    public static LogsHierarchy empty() {
        return new LogsHierarchy(null, 0, 0, 0, 0);
    }

    public void markAsNotFullFilled() {
        notFullFilled = true;
    }

    public void markAsErrorContains() {
        errorsContain = true;
    }
}
