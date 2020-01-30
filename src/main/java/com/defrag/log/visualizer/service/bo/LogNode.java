package com.defrag.log.visualizer.service.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LogNode {

    private final long id;
    private final String name;
    private final LocalDateTime startDate;
    @Setter(AccessLevel.PRIVATE)
    private LocalDateTime endDate;

    private String description;
    private String exception;
    private boolean rootNode;

    @Setter
    private Long timing;

    @Setter(AccessLevel.PRIVATE)
    @JsonIgnore
    private LogNode parent;
    private List<LogNode> children = new ArrayList<>();

    public static LogNode createNode(long id, String name, String description, LocalDateTime startDate) {
        LogNode logNode = new LogNode(id, name, startDate);
        logNode.setDescription(description);
        return logNode;
    }

    public static LogNode createErrorNode(long id, String name, String exception, LocalDateTime startDate) {
        LogNode errNode = new LogNode(id, name, startDate);
        errNode.setException(exception);
        return errNode;
    }

    public void addChild(LogNode child) {
        child.setParent(this);
        children.add(child);
    }

    public void markAsFinished(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
