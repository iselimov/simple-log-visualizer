package com.defrag.log.visualizer.model;

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

    @Column(name = "uid")
    private String uid;

    @Column(name = "first_action_date")
    private LocalDateTime firstActionDate;

    @Column(name = "last_action_date")
    private LocalDateTime lastActionDate;

    @Column(name = "creation_date")
    @Setter(AccessLevel.PACKAGE)
    private LocalDateTime creationDate;

    @OneToMany(mappedBy = "root", cascade = CascadeType.REMOVE)
    private Set<Log> children = new HashSet<>();

    @PrePersist
    private void prePersist() {
        creationDate = LocalDateTime.now();
    }
}
