package com.defrag.log.visualizer.service.utils;

import com.defrag.log.visualizer.config.GraylogProps;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UrlComposer {

    private final GraylogProps graylogProps;

    public String composeApiResourceUrl(String resourceUrl) {
        return graylogProps.getCommonProps().getUrl() + graylogProps.getCommonApiProps().getApiHost() + resourceUrl;
    }

    public String composeResourceUrl(String resourceUrl) {
        return graylogProps.getCommonProps().getUrl() + resourceUrl;
    }
}
