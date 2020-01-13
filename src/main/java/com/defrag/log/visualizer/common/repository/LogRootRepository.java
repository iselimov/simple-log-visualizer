package com.defrag.log.visualizer.common.repository;

import com.defrag.log.visualizer.common.model.LogRoot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface LogRootRepository extends JpaRepository<LogRoot, Long> {

    List<LogRoot> findBySourceIdAndStartDateBetweenOrderByStartDateDesc(long sourceId, LocalDateTime from, LocalDateTime after);

    LogRoot findTopByPatientAndEndDateIsNullOrderByStartDateDesc(long patientId);

    @Modifying
    @Query("delete from LogRoot lr where lr.updateDate < ?1")
    void deleteAllByUpdateDateBefore(LocalDateTime expiredDate);
}
