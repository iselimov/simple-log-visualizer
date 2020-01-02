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
import com.defrag.log.visualizer.graylog.service.model.GraylogMessage;
import com.defrag.log.visualizer.graylog.service.model.GraylogMessageWrapper;
import com.defrag.log.visualizer.graylog.service.model.GraylogResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraylogLogHandler {
    private static final String ZONE = "Z";

    private static final Pattern ACTION_PATTERN = Pattern.compile("\\w+(Action|Editor|Manager)");
    private static final Pattern PAYLOAD_PATTERN = Pattern.compile("\\w+Payload");
    private static final Pattern PATIENT_PATTERN = Pattern.compile("(?<=PAT)\\d+");

    private final AtomicBoolean refreshing = new AtomicBoolean();

    private final GraylogSourceRepository sourceRepository;
    private final LogRootRepository logRootRepository;
    private final LogRepository logRepository;

    private final GraylogProps graylogProps;
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
        String searchUrl = graylogProps.getCommonApiProps().getApiHost() + searchApiProps.getUrl();

        try {
            for (GraylogSource graylogSource : sourceRepository.findAll()) {
                try {
                    LocalDateTime to = LocalDateTime.now();

                    self.processSource(graylogSource, searchUrl, requestParams, to);

                    graylogSource.setLastSuccessUpdate(to);
                    graylogSource.setLastUpdateError(null);
                    sourceRepository.save(graylogSource);
                } catch (Exception e) {
                    log.error("Process of updating logs finished with exception {}", e);
                    graylogSource.setLastUpdateError(e.toString());
                    sourceRepository.save(graylogSource);
                }
            }
        } finally {
            refreshing.set(false);
            log.info("Process of updating logs finished");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSource(GraylogSource source, String searchUrl, Map<String, String> requestParams, LocalDateTime to) {
        SearchApiProps searchApiProps = graylogProps.getSearchApiProps();

        ZoneId sourceTimezone = ZoneId.of(source.getGraylogTimezone());
        requestParams.put(searchApiProps.getUrlFromParam(), convertDateTimeInZone(source.getLastSuccessUpdate(), ZoneId.systemDefault(),
                sourceTimezone).toString() + ZONE);
        requestParams.put(searchApiProps.getUrlToParam(), convertDateTimeInZone(to, ZoneId.systemDefault(),
                sourceTimezone).toString() + ZONE);
        requestParams.put(searchApiProps.getUrlFilterParam(), String.format(searchApiProps.getUrlFilterPattern(), source.getGraylogUId()));

        int totalLogsAmount;
        final int limit = searchApiProps.getUrlLimitValue();
        int currOffset = -limit;
        int logsAmount = 0;

        List<Log> toSaveLogs = new ArrayList<>();
        do {
            currOffset += limit;

            requestParams.put(searchApiProps.getUrlLimitParam(), String.valueOf(limit));
            requestParams.put(searchApiProps.getUrlOffsetParam(), String.valueOf(currOffset));

            GraylogResponseWrapper graylogResponseWrapper = restTemplate.get(searchUrl, GraylogResponseWrapper.class, requestParams);
            totalLogsAmount = graylogResponseWrapper.getTotalAmount();

            processLogs(graylogResponseWrapper.getMessages()
                            .stream()
                            .map(GraylogMessageWrapper::getMessage)
                            .collect(Collectors.toList()),
                    source, sourceTimezone, toSaveLogs);
            logsAmount += toSaveLogs.size();
        } while (currOffset < totalLogsAmount);

        log.info("{} logs for source {} is going to save", logsAmount, source.getName());
    }

    private void processLogs(List<GraylogMessage> logsFromGraylog,
                             GraylogSource source,
                             ZoneId sourceTimezone,
                             List<Log> toSaveLogs) {
        for (GraylogMessage logFromGraylog : logsFromGraylog) {
            String logMarker;
            final String currMsg = logFromGraylog.getMessage();

            if (currMsg.startsWith(LoggingConstants.START_ACTION)) {
                logMarker = LoggingConstants.START_ACTION;
            } else if (currMsg.startsWith(LoggingConstants.FINISH_ACTION)) {
                logMarker = LoggingConstants.FINISH_ACTION;
            } else if (currMsg.startsWith(LoggingConstants.EXCEPTION_IN_ACTION)) {
                logMarker = LoggingConstants.EXCEPTION_IN_ACTION;
            } else if (currMsg.startsWith(LoggingConstants.START_EXTERNAL_ACTION)) {
                logMarker = LoggingConstants.START_EXTERNAL_ACTION;
            } else if (currMsg.startsWith(LoggingConstants.FINISH_EXTERNAL_ACTION)) {
                logMarker = LoggingConstants.FINISH_EXTERNAL_ACTION;
            } else {
                continue;
            }

            final Matcher actionMatcher = ACTION_PATTERN.matcher(currMsg.substring(logMarker.length()));
            String actionName = null;
            if (actionMatcher.find()) {
                actionName = actionMatcher.group();
            }

            Matcher payloadMatcher = PAYLOAD_PATTERN.matcher(currMsg);
            String payloadName = null;
            if (payloadMatcher.find()) {
                payloadName = payloadMatcher.group();
            }

            Matcher patientMatcher = PATIENT_PATTERN.matcher(currMsg);
            Long patientId = null;
            if (patientMatcher.find()) {
                try {
                    patientId = Long.parseUnsignedLong(patientMatcher.group());
                } catch (NumberFormatException e) {
                    log.warn("Couldn't parse patient in log {}", currMsg);
                }
            }
            if (patientId == null) {
                continue;
            }

            if (actionName != null
                    && (currMsg.startsWith(LoggingConstants.START_ACTION)
                    || currMsg.startsWith(LoggingConstants.FINISH_ACTION)
                    || currMsg.startsWith(LoggingConstants.EXCEPTION_IN_ACTION))) {
                Log newLog = new Log();
                newLog.setRoot(logRootRepository.findTopByPatientAndEndDateIsNullOrderByStartDateDesc(patientId));

                newLog.setActionName(actionName);
                newLog.setDescription(currMsg);
                newLog.setTimestamp(convertDateTimeInZone(logFromGraylog.getTimestamp(), sourceTimezone, ZoneId.systemDefault()));
                newLog.setMarker(defineLogMarker(logMarker));

                if (newLog.getRoot() == null && newLog.getMarker() == LogMarker.FINISH) {
                    LogRoot logRoot = logRootRepository.findTopByPatientAndEndDateIsNotNullOrderByStartDateDesc(patientId);
                    if (newLog.getTimestamp().equals(logRoot.getEndDate())) {
                        newLog.setRoot(logRoot);
                    }
                }

                toSaveLogs.add(newLog);
            } else if (payloadName != null && currMsg.startsWith(LoggingConstants.START_EXTERNAL_ACTION)) {
                LogRoot newLogRoot = new LogRoot();

                newLogRoot.setSource(source);
                newLogRoot.setPayloadName(payloadName);
                newLogRoot.setDescription(currMsg);
                newLogRoot.setPatient(patientId);
                newLogRoot.setStartDate(convertDateTimeInZone(logFromGraylog.getTimestamp(), sourceTimezone, ZoneId.systemDefault()));

                logRootRepository.save(newLogRoot);
            } else if (currMsg.startsWith(LoggingConstants.FINISH_EXTERNAL_ACTION)) {
                LogRoot currLogRoot = logRootRepository.findTopByPatientAndEndDateIsNullOrderByStartDateDesc(patientId);
                currLogRoot.setEndDate(convertDateTimeInZone(logFromGraylog.getTimestamp(), sourceTimezone, ZoneId.systemDefault()));

                for (Log toSaveLog : toSaveLogs) {
                    if (toSaveLog.getRoot() == null) {
                        toSaveLog.setRoot(currLogRoot);
                    }
                }

                logRepository.saveAll(toSaveLogs);
                logRootRepository.save(currLogRoot);
                toSaveLogs.clear();
            }
        }
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

    private LocalDateTime convertDateTimeInZone(LocalDateTime current, ZoneId fromZone, ZoneId toZone) {
        return ZonedDateTime.of(current, fromZone)
                .withZoneSameInstant(toZone)
                .toLocalDateTime();
    }

    private LogMarker defineLogMarker(String source) {
        switch (source) {
            case LoggingConstants.START_ACTION:
                return LogMarker.START;
            case LoggingConstants.EXCEPTION_IN_ACTION:
                return LogMarker.EXCEPTION;
            case LoggingConstants.FINISH_ACTION:
                return LogMarker.FINISH;
            default:
                throw new IllegalArgumentException("Unknown log marker");
        }
    }
}
