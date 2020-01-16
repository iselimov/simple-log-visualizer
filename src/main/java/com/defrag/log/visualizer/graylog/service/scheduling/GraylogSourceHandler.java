package com.defrag.log.visualizer.graylog.service.scheduling;

import com.defrag.log.visualizer.graylog.config.GraylogProps;
import com.defrag.log.visualizer.graylog.http.GraylogRestTemplate;
import com.defrag.log.visualizer.graylog.repository.GraylogSourceRepository;
import com.defrag.log.visualizer.graylog.repository.model.GraylogSource;
import com.defrag.log.visualizer.graylog.service.scheduling.model.GraylogCluster;
import com.defrag.log.visualizer.graylog.service.scheduling.model.GraylogStream;
import com.defrag.log.visualizer.graylog.service.scheduling.model.GraylogStreamWrapper;
import com.defrag.log.visualizer.graylog.service.utils.UrlComposer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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
