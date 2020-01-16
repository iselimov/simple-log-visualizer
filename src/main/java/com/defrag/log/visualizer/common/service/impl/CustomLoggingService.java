package com.defrag.log.visualizer.common.service.impl;

import com.defrag.log.visualizer.common.model.Log;
import com.defrag.log.visualizer.common.model.LogRoot;
import com.defrag.log.visualizer.common.model.LogSource;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomLoggingService {

    List<LogSource> getSources();

    List<LogRoot> getRoots(long sourceId, LocalDateTime from, LocalDateTime to);

    List<Log> getLogs(long rootId);

    String getGraylogQueryForLogRoot(long logRootId);

    String getGraylogQueryForLog(long logId, LocalDateTime from, LocalDateTime to);
}
