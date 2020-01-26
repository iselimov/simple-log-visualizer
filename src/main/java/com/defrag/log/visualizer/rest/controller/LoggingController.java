package com.defrag.log.visualizer.rest.controller;

import com.defrag.log.visualizer.rest.dto.GraylogSourceDto;
import com.defrag.log.visualizer.rest.dto.LogRootDto;
import com.defrag.log.visualizer.rest.exception.ValidationException;
import com.defrag.log.visualizer.rest.mapper.GraylogSourceMapper;
import com.defrag.log.visualizer.rest.mapper.LogRootMapper;
import com.defrag.log.visualizer.service.LoggingService;
import com.defrag.log.visualizer.service.bo.LogsHierarchy;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "logs/", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Validated
public class LoggingController {

    private final LoggingService service;

    private final GraylogSourceMapper sourceMapper;
    private final LogRootMapper logRootMapper;

    @GetMapping("/sources")
    public List<GraylogSourceDto> getSources() {
        return service.getSources()
                .stream()
                .map(sourceMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/source/{sourceId}/roots")
    public List<LogRootDto> getRoots(@PathVariable @NotNull Long sourceId,
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                     @RequestParam LocalDateTime from,
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                     @RequestParam LocalDateTime to) {
        if (from.isAfter(to)) {
            throw new ValidationException("From must be before than to");
        }

        return service.getRoots(sourceId, from, to)
                .stream()
                .map(logRootMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/root/{logRootId}/hierarchy")
    public LogsHierarchy getHierarchy(@PathVariable @NotNull Long logRootId) {
        return service.getLogsHierarchy(logRootId);
    }

    @GetMapping("/root/{logRootId}/query")
    public String getGraylogQueryForLogRoot(@PathVariable @NotNull Long logRootId) {
        return service.getGraylogQueryForLogRoot(logRootId);
    }

    @GetMapping("/{logId}/query")
    public String getGraylogQueryForLog(@PathVariable @NotNull Long logId,
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                        @RequestParam LocalDateTime from,
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                        @RequestParam(required = false) LocalDateTime to) {
        return service.getGraylogQueryForLog(logId, from, to);
    }
}
