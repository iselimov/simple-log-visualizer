package com.defrag.log.visualizer.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "sparql_query")
public class SparqlQuery {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "sparql_query_id_gen", sequenceName = "sparql_query_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sparql_query_id_gen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_action")
    private Log startAction;

    @Column(name = "timing")
    private Long timing;
}
