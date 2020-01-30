package com.defrag.log.visualizer.service;

import com.defrag.log.visualizer.config.GraylogProps;
import com.defrag.log.visualizer.model.Log;
import com.defrag.log.visualizer.model.LogRoot;
import com.defrag.log.visualizer.model.LogSource;
import com.defrag.log.visualizer.repository.GraylogSourceRepository;
import com.defrag.log.visualizer.repository.LogRepository;
import com.defrag.log.visualizer.repository.LogRootRepository;
import com.defrag.log.visualizer.rest.exception.ValidationException;
import com.defrag.log.visualizer.service.bo.LogRootSummary;
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
import java.util.stream.Collectors;

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

    public List<LogSource> getSources() {
        return new ArrayList<>(graylogSourceRepository.findAll());
    }

    public List<LogRootSummary> getRoots(long sourceId, LocalDateTime from, LocalDateTime to) {
        return logRootRepository.findBySourceIdAndFirstActionDateBetweenOrderByFirstActionDateDesc(sourceId, from, to)
                .stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    public LogsHierarchy getLogsHierarchy(long rootId) {
        final List<Log> logs = logRepository.findAllByRootIdOrderByInvocationOrder(rootId);
        return new LogsHierarchyBuilder(logs).build();
    }

    public String getGraylogQueryForLog(long logId, LocalDateTime from, LocalDateTime to) {
        Optional<Log> log = logRepository.findById(logId);
        if (!log.isPresent()) {
            throw new ValidationException(String.format("Unknown log with id %d was not found", logId));
        }

        LogSource logSource = graylogSourceRepository.getOne(log.get().getRoot().getSource().getId());
        String graylogTimezone = logSource.getGraylogTimezone();

        LocalDateTime fromInSourceTimezone = convertDateTimeInZone(from, ZoneId.systemDefault(), ZoneId.of(graylogTimezone));
        if (to == null) {
            to = LocalDateTime.of(2222, 1, 1, 1, 1, 1, 100_000_000);
        }
        LocalDateTime toInSourceTimezone = convertDateTimeInZone(to, ZoneId.systemDefault(), ZoneId.of(graylogTimezone));

        return composeFilterQuery(logSource.getGraylogUId(), fromInSourceTimezone, toInSourceTimezone);
    }

    private LogRootSummary mapToSummary(LogRoot lr) {
        LogRootSummary result = new LogRootSummary(lr);

        Log log = logRepository.findTopByRootIdAndPatientIsNotNull(lr.getId());
        if (log != null) {
            result.setPatient(log.getPatient());
        }
        Log firstStartAction = logRepository.findTopByRootIdOrderByInvocationOrder(lr.getId());
        if (firstStartAction != null) {
            result.setFirstActionName(firstStartAction.getActionName());
        }

        return result;
    }

    private String composeFilterQuery(String sourceUid, LocalDateTime from, LocalDateTime to) {
        return String.format("%s/%s/search?sortOrder=asc&sortField=timestamp&relative=%d&q=timestamp: [\"%s %s\" TO \"%s %s\"]",
                urlComposer.composeResourceUrl(graylogProps.getCommonApiProps().getStreamsUrl()), sourceUid, 0,
                from.toLocalDate().toString(), toStr(from.toLocalTime()), to.toLocalDate().toString(), toStr(to.toLocalTime()));
    }
}
