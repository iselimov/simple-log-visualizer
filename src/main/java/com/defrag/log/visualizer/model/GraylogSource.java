package com.defrag.log.visualizer.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@DiscriminatorValue("GRAYLOG")
@EqualsAndHashCode(of = {"name", "graylogUId", "graylogTimezone"})
public class GraylogSource {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "log_source_id_gen", sequenceName = "log_source_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "log_source_id_gen")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "graylog_uid")
    private String graylogUId;

    @Column(name = "graylog_timezone")
    private String graylogTimezone;

    @Column(name = "last_success_update")
    private LocalDateTime lastSuccessUpdate;

    @Column(name = "last_update_error")
    private String lastUpdateError;

    @PrePersist
    private void updateDate() {
        lastSuccessUpdate = LocalDateTime.now();
    }
}
