package com.defrag.log.visualizer.graylog.service.scheduling.model;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class GraylogStreamWrapper {

    private Set<GraylogStream> streams = new HashSet<>();
}
