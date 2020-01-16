package com.defrag.log.visualizer.graylog.service;

import com.defrag.log.visualizer.common.model.Log;
import com.defrag.log.visualizer.common.model.LogRoot;
import com.defrag.log.visualizer.common.model.LogSource;
import com.defrag.log.visualizer.common.repository.LogRepository;
import com.defrag.log.visualizer.common.repository.LogRootRepository;
import com.defrag.log.visualizer.common.rest.ValidationException;
import com.defrag.log.visualizer.common.service.impl.CustomLoggingService;
import com.defrag.log.visualizer.graylog.config.GraylogProps;
import com.defrag.log.visualizer.graylog.repository.GraylogSourceRepository;
import com.defrag.log.visualizer.graylog.repository.model.GraylogSource;
import com.defrag.log.visualizer.graylog.service.utils.UrlComposer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.defrag.log.visualizer.graylog.service.utils.DateTimeUtils.convertDateTimeInZone;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GraylogCustomService implements CustomLoggingService {

    private final GraylogSourceRepository graylogSourceRepository;
    private final LogRootRepository logRootRepository;
    private final LogRepository logRepository;

    private final GraylogProps graylogProps;
    private final UrlComposer urlComposer;

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

    @Override
    public String getGraylogQueryForLogRoot(long logRootId) {
        LogRoot logRoot = logRootRepository.getOne(logRootId);
        if (logRoot == null) {
            throw new ValidationException(String.format("Unknown log root with id %d was not found", logRootId));
        }


        GraylogSource graylogSource = graylogSourceRepository.getOne(logRoot.getSource().getId());
        String graylogTimezone = graylogSource.getGraylogTimezone();

        LocalDateTime fromInSourceTimezone = convertDateTimeInZone(logRoot.getStartDate(), ZoneId.systemDefault(), ZoneId.of(graylogTimezone));
        LocalDateTime toInSourceTimezone = convertDateTimeInZone(logRoot.getEndDate(), ZoneId.systemDefault(), ZoneId.of(graylogTimezone));

        return composeFilterQuery(graylogSource.getGraylogUId(), fromInSourceTimezone, toInSourceTimezone);
    }

    @Override
    public String getGraylogQueryForLog(long logId, LocalDateTime from, LocalDateTime to) {
        Log log = logRepository.getOne(logId);
        if (log == null) {
            throw new ValidationException(String.format("Unknown log with id %d was not found", logId));
        }

        GraylogSource graylogSource = graylogSourceRepository.getOne(log.getRoot().getSource().getId());
        String graylogTimezone = graylogSource.getGraylogTimezone();

        LocalDateTime fromInSourceTimezone = convertDateTimeInZone(from, ZoneId.systemDefault(), ZoneId.of(graylogTimezone));
        LocalDateTime toInSourceTimezone = convertDateTimeInZone(to, ZoneId.systemDefault(), ZoneId.of(graylogTimezone));

        return composeFilterQuery(graylogSource.getGraylogUId(), fromInSourceTimezone, toInSourceTimezone);
    }

    private String composeFilterQuery(String sourceUid, LocalDateTime from, LocalDateTime to) {
        if (to == null) {
            to = LocalDateTime.of(2222, 1, 1, 0, 0);
        }

        return String.format("%s/%s/search?sortOrder=asc&sortField=timestamp&relative=%d&q=timestamp: [\"%s %s\" TO \"%s %s\"]",
                urlComposer.composeResourceUrl(graylogProps.getCommonApiProps().getStreamsUrl()), sourceUid, 0,
                from.toLocalDate().toString(), from.toLocalTime().toString(), to.toLocalDate().toString(), to.toLocalTime().toString());
    }
}
