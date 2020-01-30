package com.defrag.log.visualizer.model;

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

    @Column(name = "event_type")
    @Enumerated(EnumType.STRING)
    private LogEventType eventType;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "invocation_order")
    private Integer invocationOrder;

    @Column(name = "depth")
    private Integer depth;

    @Column(name = "action_name")
    private String actionName;

    @Column(name = "args")
    private String args;

    @Column(name = "full_message")
    private String fullMessage;

    @Column(name = "patient")
    private Long patient;

    @Column(name = "timing")
    private Long timing;

    @Column(name = "exception")
    private String exception;

    @Column(name = "timing")
    private Long timing;
}
