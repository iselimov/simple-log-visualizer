package com.defrag.log.visualizer.service.scheduling;

import com.defrag.log.visualizer.repository.LogRootRepository;
import com.defrag.log.visualizer.config.GraylogProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraylogExpiredLogsHandler {

    private final LogRootRepository logRootRepository;
    private final GraylogProps graylogProps;

    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    public void removeExpired() {
        log.info("Process of removing expired log sources started");

        logRootRepository.deleteAllByUpdateDateBefore(LocalDateTime.now().minusDays(graylogProps.getCommonProps().getExpiredLogsDays()));

        log.info("Process of removing expired log sources finished");
    }
}
