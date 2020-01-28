package com.defrag.log.visualizer.repository;

import com.defrag.log.visualizer.model.LogSource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GraylogSourceRepository extends JpaRepository<LogSource, Long> {
}
