package com.defrag.log.visualizer.common.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Table(name = "log_source")
@Entity
@DiscriminatorColumn(name = "type")
@EqualsAndHashCode(of = {"name", "type"})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class LogSource {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "log_source_id_gen", sequenceName = "log_source_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "log_source_id_gen")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "type", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private LogSourceType type;

    public LogSource() {
    }

    protected LogSource(LogSourceType type) {
        this.type = type;
    }
}
