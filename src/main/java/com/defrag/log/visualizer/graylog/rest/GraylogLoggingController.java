package com.defrag.log.visualizer.graylog.rest;

import com.defrag.log.visualizer.common.service.impl.CustomLoggingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "graylog/")
@Validated
public class GraylogLoggingController {

    private final CustomLoggingService service;

    @GetMapping("/root/{logRootId}/query")
    public String getGraylogQueryForLogRoot(@PathVariable @NotNull Long logRootId) {
        return service.getGraylogQueryForLogRoot(logRootId);
    }

    @GetMapping("/{logId}/query")
    public String getGraylogQueryForLog(@PathVariable @NotNull Long logId,
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                        @RequestParam LocalDateTime from,
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                        @RequestParam LocalDateTime to) {
        return service.getGraylogQueryForLog(logId, from, to);
    }
}
