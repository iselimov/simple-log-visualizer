package com.defrag.log.visualizer.service;

import com.defrag.log.visualizer.config.GraylogProps;
import com.defrag.log.visualizer.model.GraylogSource;
import com.defrag.log.visualizer.model.Log;
import com.defrag.log.visualizer.model.LogRoot;
import com.defrag.log.visualizer.repository.GraylogSourceRepository;
import com.defrag.log.visualizer.repository.LogRepository;
import com.defrag.log.visualizer.repository.LogRootRepository;
import com.defrag.log.visualizer.rest.exception.ValidationException;
import com.defrag.log.visualizer.service.bo.LogsHierarchy;
import com.defrag.log.visualizer.service.utils.UrlComposer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.defrag.log.visualizer.service.utils.DateTimeUtils.convertDateTimeInZone;
import static com.defrag.log.visualizer.service.utils.DateTimeUtils.toStr;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoggingService {

    private final GraylogSourceRepository graylogSourceRepository;
    private final LogRootRepository logRootRepository;
    private final LogRepository logRepository;

    private final GraylogProps graylogProps;
    private final UrlComposer urlComposer;

    public List<GraylogSource> getSources() {
        return new ArrayList<>(graylogSourceRepository.findAll());
    }

    public List<LogRoot> getRoots(long sourceId, LocalDateTime from, LocalDateTime to) {
        return logRootRepository.findBySourceIdAndFirstActionDateBetweenOrderByFirstActionDateDesc(sourceId, from, to);
    }

    public LogsHierarchy getLogsHierarchy(long rootId) {
        final List<Log> logs = logRepository.findAllByRootId(rootId);
        return new LogsHierarchyBuilder(logs).build();
    }

    public String getGraylogQueryForLogRoot(long logRootId) {
        Optional<LogRoot> logRoot = logRootRepository.findById(logRootId);
        if (!logRoot.isPresent()) {
            throw new ValidationException(String.format("Unknown log root with id %d was not found", logRootId));
        }

        LogRoot lr = logRoot.get();

        GraylogSource graylogSource = graylogSourceRepository.getOne(lr.getSource().getId());
        String graylogTimezone = graylogSource.getGraylogTimezone();

        LocalDateTime fromInSourceTimezone = convertDateTimeInZone(lr.getFirstActionDate(), ZoneId.systemDefault(), ZoneId.of(graylogTimezone));
        LocalDateTime to = lr.getLastActionDate();
        if (to == null) {
            to = LocalDateTime.of(2222, 1, 1, 1, 1, 1, 100_000_000);
        }
        LocalDateTime toInSourceTimezone = convertDateTimeInZone(to, ZoneId.systemDefault(), ZoneId.of(graylogTimezone));

        return composeFilterQuery(graylogSource.getGraylogUId(), fromInSourceTimezone, toInSourceTimezone);
    }

    public String getGraylogQueryForLog(long logId, LocalDateTime from, LocalDateTime to) {
        Optional<Log> log = logRepository.findById(logId);
        if (!log.isPresent()) {
            throw new ValidationException(String.format("Unknown log with id %d was not found", logId));
        }

        GraylogSource graylogSource = graylogSourceRepository.getOne(log.get().getRoot().getSource().getId());
        String graylogTimezone = graylogSource.getGraylogTimezone();

        LocalDateTime fromInSourceTimezone = convertDateTimeInZone(from, ZoneId.systemDefault(), ZoneId.of(graylogTimezone));
        if (to == null) {
            to = LocalDateTime.of(2222, 1, 1, 1, 1, 1, 100_000_000);
        }
        LocalDateTime toInSourceTimezone = convertDateTimeInZone(to, ZoneId.systemDefault(), ZoneId.of(graylogTimezone));

        return composeFilterQuery(graylogSource.getGraylogUId(), fromInSourceTimezone, toInSourceTimezone);
    }

    private String composeFilterQuery(String sourceUid, LocalDateTime from, LocalDateTime to) {
        return String.format("%s/%s/search?sortOrder=asc&sortField=timestamp&relative=%d&q=timestamp: [\"%s %s\" TO \"%s %s\"]",
                urlComposer.composeResourceUrl(graylogProps.getCommonApiProps().getStreamsUrl()), sourceUid, 0,
                from.toLocalDate().toString(), toStr(from.toLocalTime()), to.toLocalDate().toString(), toStr(to.toLocalTime()));
    }
}
