package com.defrag.log.visualizer.graylog.service.scheduling;

import com.defrag.log.visualizer.common.model.Log;
import com.defrag.log.visualizer.common.model.LogMarker;
import com.defrag.log.visualizer.common.model.LogRoot;
import com.defrag.log.visualizer.common.repository.LogRepository;
import com.defrag.log.visualizer.common.repository.LogRootRepository;
import com.defrag.log.visualizer.common.service.LoggingConstants;
import com.defrag.log.visualizer.graylog.config.GraylogProps;
import com.defrag.log.visualizer.graylog.config.GraylogProps.SearchApiProps;
import com.defrag.log.visualizer.graylog.http.GraylogRestTemplate;
import com.defrag.log.visualizer.graylog.repository.GraylogSourceRepository;
import com.defrag.log.visualizer.graylog.repository.model.GraylogSource;
import com.defrag.log.visualizer.graylog.service.parsing.GraylogParser;
import com.defrag.log.visualizer.graylog.service.parsing.model.GraylogResponseWrapper;
import com.defrag.log.visualizer.graylog.service.parsing.model.LogDefinition;
import com.defrag.log.visualizer.graylog.service.utils.UrlComposer;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.defrag.log.visualizer.graylog.service.utils.DateTimeUtils.convertDateTimeInZone;

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
            for (GraylogSource source : sourceRepository.findAll()) {
                try {
                    ZoneId sourceTimezone = ZoneId.of(source.getGraylogTimezone());
                    LocalDateTime from = source.getLastSuccessUpdate();

                    requestParams.put(searchApiProps.getUrlFromParam(), convertDateTimeInZone(from, ZoneId.systemDefault(),
                            sourceTimezone).toString() + ZONE);
                    requestParams.put(searchApiProps.getUrlFilterParam(), String.format(searchApiProps.getUrlFilterPattern(),
                            source.getGraylogUId()));

                    LocalDateTime to = selectToDateAccordingToTheLimit(searchUrl, requestParams, sourceTimezone, from);

                    requestParams.put(searchApiProps.getUrlToParam(), convertDateTimeInZone(to, ZoneId.systemDefault(), sourceTimezone)
                            .toString() + ZONE);
                    requestParams.put(searchApiProps.getUrlLimitParam(), String.valueOf(searchApiProps.getLimitPerDownload()));

                    log.info("Processing source {} for the period [{}, {}]", source.getName(), from, to);
                    self.processSource(source, searchUrl, requestParams);

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
            requestParams.put(searchApiProps.getUrlToParam(), convertDateTimeInZone(currToDate, ZoneId.systemDefault(), sourceTimezone)
                    .toString() + ZONE);

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSource(GraylogSource source, String searchUrl, Map<String, String> requestParams) {
        GraylogResponseWrapper graylogResponseWrapper = restTemplate.get(searchUrl, GraylogResponseWrapper.class, requestParams);
        Set<LogDefinition> logDefinitions = graylogParser.parseDefinitions(graylogResponseWrapper);

        int logsAmount = processDefinitions(logDefinitions, source, ZoneId.of(source.getGraylogTimezone()));

        log.info("{} logs for source {} is going to save", logsAmount, source.getName());
    }

    private int processDefinitions(Set<LogDefinition> logDefinitions,
                                   GraylogSource source,
                                   ZoneId sourceTimezone) {
        int logsAmount = 0;

        for (LogDefinition logDefinition : logDefinitions) {
            if (logDefinition.getMarker() == LogMarker.START_EXT) {
                LogRoot newLogRoot = new LogRoot();

                newLogRoot.setSource(source);
                newLogRoot.setPayloadName(logDefinition.getPayloadName());
                newLogRoot.setDescription(logDefinition.getDescription());
                newLogRoot.setPatient(logDefinition.getPatientId());
                newLogRoot.setStartDate(convertDateTimeInZone(logDefinition.getTimestamp(), sourceTimezone, ZoneId.systemDefault()));

                logRootRepository.save(newLogRoot);
            } else if (logDefinition.getMarker() == LogMarker.START || logDefinition.getMarker() == LogMarker.FINISH
                    || logDefinition.getMarker() == LogMarker.EXCEPTION) {
                Log newLog = new Log();
                newLog.setRoot(logRootRepository.findTopByPatientAndEndDateIsNullOrderByStartDateDesc(logDefinition.getPatientId()));

                newLog.setMarker(logDefinition.getMarker());
                newLog.setActionName(logDefinition.getActionName());
                newLog.setDescription(logDefinition.getDescription());
                newLog.setTimestamp(convertDateTimeInZone(logDefinition.getTimestamp(), sourceTimezone, ZoneId.systemDefault()));

                logRepository.save(newLog);
                logsAmount++;
            } else if (logDefinition.getMarker() == LogMarker.FINISH_EXT) {
                LogRoot existingLogRoot = logRootRepository.findTopByPatientAndEndDateIsNullOrderByStartDateDesc(logDefinition.getPatientId());
                existingLogRoot.setEndDate(convertDateTimeInZone(logDefinition.getTimestamp(), sourceTimezone, ZoneId.systemDefault()));

                logRootRepository.save(existingLogRoot);
            }
        }

        return logsAmount;
    }

    private String prepareQueryString() {
        return String.format("(\"%s\" OR \"%s\" OR \"%s\" OR \"%s\" OR \"%s\")  " +
                        "NOT \"Start action loop\" " +
                        "NOT \"DefaultCarepathEditor\" " +
                        "NOT \"DefaultGroupEditor\" " +
                        "NOT \"DefaultTemplateEditor\" ", LoggingConstants.START_ACTION,
                LoggingConstants.EXCEPTION_IN_ACTION, LoggingConstants.FINISH_ACTION, LoggingConstants.START_EXTERNAL_ACTION,
                LoggingConstants.FINISH_EXTERNAL_ACTION);
    }
}
