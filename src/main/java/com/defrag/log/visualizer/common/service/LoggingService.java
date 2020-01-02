package com.defrag.log.visualizer.common.service;

import com.defrag.log.visualizer.common.model.LogRoot;
import com.defrag.log.visualizer.common.model.LogSource;
import com.defrag.log.visualizer.common.service.model.LogsHierarchy;

import java.time.LocalDateTime;
import java.util.List;

public interface LoggingService {

    List<LogSource> getSources();

    List<LogRoot> getRoots(long sourceId, LocalDateTime from, LocalDateTime to);

    LogsHierarchy getLogsHierarchy(long rootId);
}
