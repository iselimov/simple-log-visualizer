package com.defrag.log.visualizer.repository;

import com.defrag.log.visualizer.model.SparqlQuery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SparqlQueryRepository extends JpaRepository<SparqlQuery, Long> {
}
