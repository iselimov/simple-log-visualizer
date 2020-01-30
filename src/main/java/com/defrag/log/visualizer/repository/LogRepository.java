package com.defrag.log.visualizer.repository;

import com.defrag.log.visualizer.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {

    List<Log> findAllByRootIdOrderByInvocationOrder(long logRootId);

    Log findTopByRootIdAndPatientIsNotNull(long rootId);

    Log findTopByRootIdOrderByInvocationOrder(long rootId);

    @Query(value =
            "select l.* " +
                    "from logger.log l " +
                    "join logger.log_root lr on lr.id = l.root " +
                    "where lr.uid = cast(?1 as text) and l.event_type = cast(?2 as text) and l.depth = ?3 and l.timestamp <= ?4 " +
                    "order by l.timestamp desc, l.invocation_order desc " +
                    "limit 1", nativeQuery = true)
    Log findNearestActionToQuery(String uid, String eventType, int depth, LocalDateTime queryTimestamp);
}
