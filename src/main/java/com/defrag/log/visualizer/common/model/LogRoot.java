package com.defrag.log.visualizer.common.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "log_root")
@EqualsAndHashCode(of = "id")
public class LogRoot {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "log_root_id_gen", sequenceName = "log_root_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "log_root_id_gen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source")
    private LogSource source;

    @Column(name = "payload_name")
    private String payloadName;

    @Column(name = "description")
    private String description;

    @Column(name = "patient")
    private Long patient;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "update_date")
    @Setter(AccessLevel.PACKAGE)
    private LocalDateTime updateDate;

    @OneToMany(mappedBy = "root", cascade = CascadeType.REMOVE)
    private Set<Log> children = new HashSet<>();

    @PrePersist
    private void prePersist() {
        changeUpdateDate();
    }

    public void changeUpdateDate() {
        updateDate = LocalDateTime.now();
    }
}
