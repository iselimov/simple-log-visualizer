package com.defrag.log.visualizer.common.rest;

import com.defrag.log.visualizer.common.model.LogSource;
import com.defrag.log.visualizer.common.rest.dto.LogRootDto;
import com.defrag.log.visualizer.common.rest.mapper.LogRootMapper;
import com.defrag.log.visualizer.common.service.LoggingService;
import com.defrag.log.visualizer.common.service.model.LogsHierarchy;
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
    private final LogRootMapper logRootMapper;

    @GetMapping("sources")
    public List<LogSource> getSources() {
        return service.getSources();
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

    @GetMapping("/root/{rootId}/hierarchy")
    public LogsHierarchy getHierarchy(@PathVariable @NotNull Long rootId) {
        return service.getLogsHierarchy(rootId);
    }
}
