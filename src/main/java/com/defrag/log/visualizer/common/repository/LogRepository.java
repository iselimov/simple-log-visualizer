package com.defrag.log.visualizer.common.repository;

import com.defrag.log.visualizer.common.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {

    List<Log> findAllByRootId(long logRootId);
}
