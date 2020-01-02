package com.defrag.log.visualizer.graylog.repository.model;

import com.defrag.log.visualizer.common.model.LogSource;
import com.defrag.log.visualizer.common.model.LogSourceType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@DiscriminatorValue("GRAYLOG")
@EqualsAndHashCode(of = {"graylogUId", "graylogTimezone"}, callSuper = true)
public class GraylogSource extends LogSource {

    @Column(name = "graylog_uid")
    private String graylogUId;

    @Column(name = "graylog_timezone")
    private String graylogTimezone;

    @Column(name = "last_success_update")
    private LocalDateTime lastSuccessUpdate;

    @Column(name = "last_update_error")
    private String lastUpdateError;

    public GraylogSource() {
        super(LogSourceType.GRAYLOG);
    }

    @PrePersist
    private void updateDate() {
        lastSuccessUpdate = LocalDateTime.now();
    }
}
