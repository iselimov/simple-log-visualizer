package com.defrag.log.visualizer.service.parsing.graylog.model;

import com.defrag.log.visualizer.model.LogEventType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class LogDefinition {
    private final String uid;

    private final LogEventType eventType;
    private final LocalDateTime timestamp;
    private final Long patientId;
    private final String actionName;
    private final String fullMessage;
    private final int invocationOrder;
    private final Integer depth;
    private final Long timing;
    private final String args;
    private final String exception;

    private LogDefinition(Builder builder) {
        uid = builder.uid;
        eventType = builder.eventType;
        timestamp = builder.timestamp;
        patientId = builder.patientId;
        actionName = builder.actionName;
        fullMessage = builder.fullMessage;
        invocationOrder = builder.invocationOrder == null ? Integer.MAX_VALUE : builder.invocationOrder;
        depth = builder.depth;
        timing = builder.timing;
        args = builder.args;
        exception = builder.exception;
    }

    @RequiredArgsConstructor
    public static final class Builder {
        private final String uid;
        private final LogEventType eventType;
        private final LocalDateTime timestamp;
        @Getter
        private final String fullMessage;

        private Long patientId;
        private String actionName;
        private Integer invocationOrder;
        private Integer depth;
        private Long timing;
        private String args;
        private String exception;

        public Builder patientId(Long patientId) {
            this.patientId = patientId;
            return this;
        }

        public Builder actionName(String actionName) {
            this.actionName = actionName;
            return this;
        }

        public Builder invocationOrder(Integer invocationOrder) {
            this.invocationOrder = invocationOrder;
            return this;
        }

        public Builder depth(Integer depth) {
            this.depth = depth;
            return this;
        }

        public Builder timing(Long timing) {
            this.timing = timing;
            return this;
        }

        public Builder args(String args) {
            this.args = args;
            return this;
        }

        public Builder exception(String exception) {
            this.exception = exception;
            return this;
        }

        public LogDefinition build() {
            return new LogDefinition(this);
        }
    }
}
