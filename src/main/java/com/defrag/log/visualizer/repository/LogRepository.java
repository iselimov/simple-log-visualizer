package com.defrag.log.visualizer.repository;

import com.defrag.log.visualizer.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {

    List<Log> findAllByRootId(long logRootId);
}
