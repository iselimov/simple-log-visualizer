package com.defrag.log.visualizer.service.scheduling;

import com.defrag.log.visualizer.config.GraylogProps;
import com.defrag.log.visualizer.http.GraylogRestTemplate;
import com.defrag.log.visualizer.repository.GraylogSourceRepository;
import com.defrag.log.visualizer.model.GraylogSource;
import com.defrag.log.visualizer.service.parsing.graylog.model.GraylogCluster;
import com.defrag.log.visualizer.service.parsing.graylog.model.GraylogStream;
import com.defrag.log.visualizer.service.parsing.graylog.model.GraylogStreamWrapper;
import com.defrag.log.visualizer.service.utils.UrlComposer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraylogSourceHandler {

    private final GraylogProps graylogProps;
    private final UrlComposer urlComposer;
    private final GraylogRestTemplate restTemplate;
    private final GraylogSourceRepository sourceRepository;

    private final AtomicBoolean refreshing = new AtomicBoolean();

    @Scheduled(cron = "0 0 6 * * *")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateSources() {
        if (!refreshing.compareAndSet(false, true)) {
            return;
        }

        log.info("Process of seeking log sources started");

        try {
            String timezone = getTimezone();

            Set<GraylogSource> existing = new HashSet<>(sourceRepository.findAll());
            getStreams()
                    .stream()
                    .map(curr -> mapToSource(curr, timezone))
                    .filter(mappedCurr -> !existing.contains(mappedCurr))
                    .forEach(sourceRepository::save);
        } finally {
            refreshing.set(false);
        }

        log.info("Process of seeking log sources finished");
    }

    private String getTimezone() {
        GraylogProps.CommonApiProps apiProps = graylogProps.getCommonApiProps();

        GraylogCluster graylogCluster = restTemplate.get(urlComposer.composeApiResourceUrl(apiProps.getSystemUrl()), GraylogCluster.class);
        if (graylogCluster == null) {
            return null;
        }
        return graylogCluster.getTimezone();
    }

    private Set<GraylogStream> getStreams() {
        GraylogProps.CommonApiProps apiProps = graylogProps.getCommonApiProps();

        GraylogStreamWrapper graylogResponse = restTemplate.get(urlComposer.composeApiResourceUrl(apiProps.getStreamsUrl()), GraylogStreamWrapper.class);
        if (graylogResponse == null) {
            return Collections.emptySet();
        }
        List<String> propertyStreamNames = graylogProps.getOptionalProps().getOnlyStreamNames();
        if (!propertyStreamNames.isEmpty()) {
            return graylogResponse.getStreams().stream()
                    .filter(graylogStream -> propertyStreamNames.contains(graylogStream.getName()))
                    .collect(Collectors.toSet());
        }
        return graylogResponse.getStreams();
    }

    private GraylogSource mapToSource(GraylogStream source, String timezone) {
        GraylogSource result = new GraylogSource();

        result.setName(source.getName());
        result.setGraylogUId(source.getId());
        result.setGraylogTimezone(timezone);

        return result;
    }
}
