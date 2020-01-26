package com.defrag.log.visualizer.service.parsing.utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ParserUtils {

    public static int positionAfterString(String str, int currPos) {
        return currPos + str.length();
    }
}

