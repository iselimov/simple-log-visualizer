package com.defrag.log.visualizer.service.parsing.graylog.model;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class GraylogStreamWrapper {

    private Set<GraylogStream> streams = new HashSet<>();
}
