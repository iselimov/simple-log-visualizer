package com.defrag.log.visualizer.service.scheduling;

import com.defrag.log.visualizer.config.GraylogProps;
import com.defrag.log.visualizer.config.GraylogProps.SearchApiProps;
import com.defrag.log.visualizer.http.GraylogRestTemplate;
import com.defrag.log.visualizer.model.*;
import com.defrag.log.visualizer.repository.GraylogSourceRepository;
import com.defrag.log.visualizer.repository.LogRepository;
import com.defrag.log.visualizer.repository.LogRootRepository;
import com.defrag.log.visualizer.repository.SparqlQueryRepository;
import com.defrag.log.visualizer.service.parsing.GraylogParser;
import com.defrag.log.visualizer.service.parsing.LoggingConstants;
import com.defrag.log.visualizer.service.parsing.graylog.model.GraylogResponseWrapper;
import com.defrag.log.visualizer.service.parsing.graylog.model.LogDefinition;
import com.defrag.log.visualizer.service.utils.UrlComposer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.defrag.log.visualizer.service.utils.DateTimeUtils.convertDateTimeInZone;
import static com.defrag.log.visualizer.service.utils.DateTimeUtils.toStr;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraylogLogHandler {
    private static final String ZONE = "Z";
    private static final int MILLISECOND_IN_NANOS = 1_000_000;

    private final AtomicBoolean refreshing = new AtomicBoolean();

    private final GraylogParser graylogParser;

    private final GraylogSourceRepository sourceRepository;
    private final LogRootRepository logRootRepository;
    private final LogRepository logRepository;
    private final SparqlQueryRepository sparqlQueryRepository;

    private final GraylogProps graylogProps;
    private final UrlComposer urlComposer;

    private final GraylogRestTemplate restTemplate;

    private final GraylogSourceHandler sourceHandler;

    @Autowired
    private GraylogLogHandler self;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void updateLogs() {
        if (!refreshing.compareAndSet(false, true)) {
            return;
        }
        if (sourceRepository.findAll().isEmpty()) {
            sourceHandler.updateSources();
        }

        log.info("Process of updating logs started");

        SearchApiProps searchApiProps = graylogProps.getSearchApiProps();
        Map<String, String> requestParams = new LinkedHashMap<>();
        requestParams.put(searchApiProps.getUrlQueryParam(), prepareQueryString());
        requestParams.put(searchApiProps.getUrlSortParam(), String.format(searchApiProps.getUrlSortPattern(),
                searchApiProps.getUrlSortValue()));
        String searchUrl = urlComposer.composeApiResourceUrl(searchApiProps.getUrl());

        try {
            for (LogSource source : sourceRepository.findAll()) {
                try {
                    ZoneId sourceTimezone = ZoneId.of(source.getGraylogTimezone());
                    LocalDateTime from = source.getLastSuccessUpdate();

                    requestParams.put(searchApiProps.getUrlFromParam(), toStr(convertDateTimeInZone(from, ZoneId.systemDefault(),
                            sourceTimezone)) + ZONE);
                    requestParams.put(searchApiProps.getUrlFilterParam(), String.format(searchApiProps.getUrlFilterPattern(),
                            source.getGraylogUId()));

                    LocalDateTime to = selectToDateAccordingToTheLimit(searchUrl, requestParams, sourceTimezone, from);

                    requestParams.put(searchApiProps.getUrlToParam(), toStr(convertDateTimeInZone(to, ZoneId.systemDefault(),
                            sourceTimezone)) + ZONE);
                    requestParams.put(searchApiProps.getUrlLimitParam(), String.valueOf(searchApiProps.getLimitPerDownload()));

                    log.info("Processing source {} for the period [{}, {}]", source.getName(), from, to);
                    processSource(source, searchUrl, requestParams);

                    source.setLastSuccessUpdate(to.plusNanos(MILLISECOND_IN_NANOS));
                    source.setLastUpdateError(null);
                    sourceRepository.save(source);
                } catch (Exception e) {
                    log.error("Process of updating logs finished with exception", e);
                    source.setLastUpdateError(e.toString());
                    sourceRepository.save(source);
                }
            }
        } finally {
            refreshing.set(false);
            log.info("Process of updating logs finished");
        }
    }

    private LocalDateTime selectToDateAccordingToTheLimit(String searchUrl,
                                                          Map<String, String> requestParams,
                                                          ZoneId sourceTimezone,
                                                          LocalDateTime from) {
        SearchApiProps searchApiProps = graylogProps.getSearchApiProps();

        requestParams.put(searchApiProps.getUrlLimitParam(), String.valueOf(1));

        LocalDateTime currToDate = LocalDateTime.now();
        while (currToDate.isAfter(from)) {
            requestParams.put(searchApiProps.getUrlToParam(), toStr(convertDateTimeInZone(currToDate, ZoneId.systemDefault(),
                    sourceTimezone)) + ZONE);

            GraylogResponseWrapper responseWrapper = restTemplate.get(searchUrl, GraylogResponseWrapper.class, requestParams);
            if (responseWrapper.getTotalAmount() <= searchApiProps.getLimitPerDownload()) {
                break;
            }

            currToDate = getMiddleAverageForDates(from, currToDate);
        }

        return currToDate;
    }

    private LocalDateTime getMiddleAverageForDates(LocalDateTime from, LocalDateTime to) {
        long middleAverageInSeconds = (from.toEpochSecond(ZoneOffset.UTC) + to.toEpochSecond(ZoneOffset.UTC)) / 2;
        return LocalDateTime.ofEpochSecond(middleAverageInSeconds, MILLISECOND_IN_NANOS, ZoneOffset.UTC);
    }

    //todo annotation not work in one class method without aspect something property in spring config
    //@Transactional(propagation = Propagation.REQUIRES_NEW)
    private void processSource(LogSource source, String searchUrl, Map<String, String> requestParams) {
        GraylogResponseWrapper graylogResponseWrapper = restTemplate.get(searchUrl, GraylogResponseWrapper.class, requestParams);
        log.info("Parsing started");
        List<LogDefinition> logDefinitions = graylogParser.parseDefinitions(graylogResponseWrapper);
        log.info("Parsing finished");
        int logsAmount = processLogDefinitions(logDefinitions, source, ZoneId.of(source.getGraylogTimezone()));

        log.info("{} logs for source {} is going to save", logsAmount, source.getName());
    }

    private int processLogDefinitions(List<LogDefinition> logDefinitions, LogSource source, ZoneId sourceTimezone) {
        int logsAmount = 0;

        for (LogDefinition logDefinition : logDefinitions) {
            if (isLogRoot(logDefinition)) {
                LogRoot newLogRoot = createLogRoot(logDefinition, source, sourceTimezone);
                logRootRepository.save(newLogRoot);

                Log newLog = createLog(logDefinition, sourceTimezone);
                if (newLog == null) {
                    continue;
                }
                logRepository.save(newLog);
            } else if (isLog(logDefinition)) {
                Log newLog = createLog(logDefinition, sourceTimezone);
                if (newLog == null) {
                    continue;
                }
                newLog.getRoot().setLastActionDate(newLog.getTimestamp());
                logRootRepository.save(newLog.getRoot());
                logRepository.save(newLog);
            } else if (isSparqlQuery(logDefinition)) {
                LocalDateTime queryTimestamp = convertDateTimeInZone(logDefinition.getTimestamp(), sourceTimezone, ZoneId.systemDefault());
                Log nearestStartAction = logRepository.findNearestActionToQuery(logDefinition.getUid(), LogEventType.ACTION_START.getName(),
                        logDefinition.getDepth(), queryTimestamp);
                if (nearestStartAction == null) {
                    continue;
                }
                sparqlQueryRepository.save(createSparqlQuery(logDefinition, nearestStartAction, queryTimestamp));
            } else {
                log.error("Unknown log definition {}", logDefinition);
                continue;
            }

            logsAmount++;
        }

        return logsAmount;
    }

    private String prepareQueryString() {
        return String.format("(\"%s%s\" OR \"%s%s\" OR \"%s%s\" OR \"%s%s\" OR \"%s%s\" OR \"%s%s\")", LoggingConstants.ACTION,
                LogEventType.ACTION_START.getName(), LoggingConstants.ACTION, LogEventType.ACTION_END.getName(),
                LoggingConstants.ACTION, LogEventType.ACTION_ERROR.getName(), LoggingConstants.ACTION,
                LogEventType.SPARQL_QUERY.getName(), LoggingConstants.ACTION, LogEventType.WORK_UNIT_START,
                LoggingConstants.ACTION, LogEventType.WORK_UNIT_END);
    }

    private boolean isLogRoot(LogDefinition logDefinition) {
        return isStartLog(logDefinition) && logDefinition.getInvocationOrder() == 0;
    }

    private boolean isLog(LogDefinition logDefinition) {
        return (isStartLog(logDefinition) && logDefinition.getInvocationOrder() > 0)
                || logDefinition.getEventType() == LogEventType.ACTION_END
                || logDefinition.getEventType() == LogEventType.ACTION_ERROR
                || logDefinition.getEventType() == LogEventType.WORK_UNIT_END;
    }

    private boolean isStartLog(LogDefinition logDefinition) {
        return logDefinition.getEventType() == LogEventType.ACTION_START || logDefinition.getEventType() == LogEventType.WORK_UNIT_START;
    }

    private boolean isSparqlQuery(LogDefinition logDefinition) {
        return logDefinition.getEventType() == LogEventType.SPARQL_QUERY;
    }

    private LogRoot createLogRoot(LogDefinition logDefinition, LogSource source, ZoneId sourceTimezone) {
        LogRoot result = new LogRoot();

        result.setSource(source);
        result.setUid(logDefinition.getUid());
        result.setFirstActionDate(convertDateTimeInZone(logDefinition.getTimestamp(), sourceTimezone, ZoneId.systemDefault()));
        result.setLastActionDate(result.getFirstActionDate());

        return result;
    }

    private Log createLog(LogDefinition logDefinition, ZoneId sourceTimezone) {
        Log result = new Log();
        LogRoot logRoot = logRootRepository.findByUid(logDefinition.getUid());
        if (logRoot == null) {
            return null;
        }

        result.setRoot(logRoot);
        result.setEventType(logDefinition.getEventType());
        result.setTimestamp(convertDateTimeInZone(logDefinition.getTimestamp(), sourceTimezone, ZoneId.systemDefault()));
        result.setInvocationOrder(logDefinition.getInvocationOrder());
        result.setDepth(logDefinition.getDepth());
        result.setActionName(logDefinition.getActionName());
        result.setArgs(logDefinition.getArgs());
        result.setFullMessage(logDefinition.getFullMessage());
        result.setPatient(logDefinition.getPatientId());
        result.setTiming(logDefinition.getTiming());
        result.setException(logDefinition.getException());
        result.setTiming(logDefinition.getTiming());

        return result;
    }

    private SparqlQuery createSparqlQuery(LogDefinition logDefinition, Log nearestStartAction, LocalDateTime timestamp) {
        SparqlQuery result = new SparqlQuery();

        result.setStartAction(nearestStartAction);
        result.setTimestamp(timestamp);
        result.setTiming(logDefinition.getTiming());

        return result;
    }
}
