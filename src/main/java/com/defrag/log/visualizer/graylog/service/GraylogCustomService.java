package com.defrag.log.visualizer.graylog.service;

import com.defrag.log.visualizer.common.model.Log;
import com.defrag.log.visualizer.common.model.LogRoot;
import com.defrag.log.visualizer.common.model.LogSource;
import com.defrag.log.visualizer.common.repository.LogRepository;
import com.defrag.log.visualizer.common.repository.LogRootRepository;
import com.defrag.log.visualizer.common.service.impl.CustomLoggingService;
import com.defrag.log.visualizer.graylog.repository.GraylogSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GraylogCustomService implements CustomLoggingService {

    private final GraylogSourceRepository graylogSourceRepository;
    private final LogRootRepository logRootRepository;
    private final LogRepository logRepository;

    @Override
    public List<LogSource> getSources() {
        return new ArrayList<>(graylogSourceRepository.findAll());
    }

    @Override
    public List<LogRoot> getRoots(long sourceId, LocalDateTime from, LocalDateTime to) {
        return logRootRepository.findBySourceIdAndStartDateBetweenOrderByStartDateDesc(sourceId, from, to);
    }

    @Override
    public List<Log> getLogs(long rootId) {
        final List<Log> logs = logRepository.findAllByRootId(rootId);
        return logs
                .stream()
                .sorted(new GraylogComparator(logs))
                .collect(Collectors.toList());
    }
}
