package com.defrag.log.visualizer.service;

/**
 * Constants to handle logs.
 */
public final class LoggingConstants {

    public static final String UUID = "uuid:";
    public static final String ACTION = "action:";
    public static final String INVOCATION_ORDER = "invocationOrder:";
    public static final String DEPTH = "depth:";
    public static final String NAME = "name:";
    public static final String ARGS = "args:";

    public static final String TIMING = "timing:";
    public static final String EXCEPTION = "exception:";

    public static final String LOG_SPARQL_STRING = String.format("%s %s %s %s", UUID, ACTION, DEPTH, TIMING);
    public static final String LOG_ACTION_FINISHED_STRING = String.format("%s %s %s %s %s %s", UUID, ACTION, INVOCATION_ORDER, DEPTH, NAME, TIMING);
    public static final String LOG_ACTION_ERROR = String.format("%s %s %s %s %s", UUID, ACTION, INVOCATION_ORDER, NAME, EXCEPTION);

    private LoggingConstants() {
    }
}
