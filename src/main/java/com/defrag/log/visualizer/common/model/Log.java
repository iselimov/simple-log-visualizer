package com.defrag.log.visualizer.common.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "log")
public class Log {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "log_id_gen", sequenceName = "log_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "log_id_gen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root")
    private LogRoot root;

    @Column(name = "action_name")
    private String actionName;

    @Column(name = "description")
    private String description;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "marker")
    @Enumerated(EnumType.STRING)
    private LogMarker marker;
}
