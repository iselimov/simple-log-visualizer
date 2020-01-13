package com.defrag.log.visualizer.graylog.service.parsing.model;

import com.defrag.log.visualizer.common.model.LogMarker;
import com.defrag.log.visualizer.graylog.service.scheduling.GraylogLogHandler;
import lombok.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"marker", "patientId", "timestamp", "description"})
public class LogDefinition implements Comparable<LogDefinition> {
    private final LogMarker marker;
    private final long patientId;
    private final LocalDateTime timestamp;
    private final String description;

    private String payloadName;
    private String actionName;

    @Override
    public int compareTo(LogDefinition other) {
        if (this.equals(other)) {
            return 0;
        }

        if (timestamp.isBefore(other.getTimestamp())) {
            return -1;
        }
        if (timestamp.isAfter(other.getTimestamp())) {
            return 1;
        }

        if (patientId != other.getPatientId()) {
            return Long.compare(patientId, other.getPatientId());
        }

        if (marker == LogMarker.START_EXT || other.getMarker() == LogMarker.FINISH_EXT) {
            return -1;
        }
        if (marker == LogMarker.FINISH_EXT || other.getMarker() == LogMarker.START_EXT) {
            return 1;
        }

        if (marker == LogMarker.START && other.getMarker() == LogMarker.START) {
            throw new IllegalArgumentException(String.format("Two actions %s, %s with the same start date were found!",
                    this, other));
        }
        if (marker == LogMarker.START || other.getMarker() == LogMarker.FINISH) {
            return -1;
        } else {
            return 1;
        }
    }
}
