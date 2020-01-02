package com.defrag.log.visualizer.graylog.repository;

import com.defrag.log.visualizer.graylog.repository.model.GraylogSource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GraylogSourceRepository extends JpaRepository<GraylogSource, Long> {
}
