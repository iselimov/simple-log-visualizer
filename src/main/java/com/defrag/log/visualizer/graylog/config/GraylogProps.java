package com.defrag.log.visualizer.graylog.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "graylog", ignoreUnknownFields = false)
@Getter
public class GraylogProps {

    private final CommonProps commonProps = new CommonProps();
    private final AuthProps authProps = new AuthProps();
    private final CommonApiProps commonApiProps = new CommonApiProps();
    private final SearchApiProps searchApiProps = new SearchApiProps();

    @Getter
    @Setter
    public static class CommonProps {
        private int expiredLogsDays;
    }

    @Getter
    @Setter
    public static class AuthProps {
        private String userName;
        private String password;
    }

    @Getter
    @Setter
    public static class CommonApiProps {
        private String apiHost;
        private String sessionUrl;
        private String systemUrl;
        private String streamsUrl;
    }

    @Getter
    @Setter
    public static class SearchApiProps {
        private String url;
        private String urlQueryParam;
        private String urlFromParam;
        private String urlToParam;
        private String urlFilterParam;
        private String urlSortParam;
        private String urlLimitParam;
        private String urlOffsetParam;

        private String urlFilterPattern;
        private String urlSortPattern;
        private int limitPerDownload;
        private String urlSortValue;
    }
}
