package com.defrag.log.visualizer.repository;

import com.defrag.log.visualizer.model.LogRoot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface LogRootRepository extends JpaRepository<LogRoot, Long> {

    List<LogRoot> findBySourceIdAndFirstActionDateBetweenOrderByFirstActionDateDesc(long sourceId, LocalDateTime from, LocalDateTime after);

    LogRoot findByUid(String uid);

    @Modifying
    @Query("delete from LogRoot lr where lr.getCreationDate < ?1")
    void deleteAllByUpdateDateBefore(LocalDateTime expiredDate);
}
