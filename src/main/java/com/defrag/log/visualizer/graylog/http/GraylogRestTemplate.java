package com.defrag.log.visualizer.graylog.http;

import com.defrag.log.visualizer.graylog.config.GraylogProps;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GraylogRestTemplate {

    private static final String BASIC_AUTH_PATTERN = "Basic %s";
    private static final String SESSION_PATTERN = "%s:session";

    private final RestTemplate restTemplate;
    private final GraylogProps applicationProperties;

    private GraylogSession lastSession;

    public <T> T get(String url, Class<T> responseType) {
        return get(url, responseType, new HashMap<>());
    }

    public <T> T get(String url, Class<T> responseType, Map<String, String> requestParams) {
        String sessionId = getSessionId();
        if (sessionId == null) {
            throw new IllegalStateException("Couldn't get session id");
        }

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(url);
        requestParams.forEach(uriComponentsBuilder::queryParam);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.add(HttpHeaders.AUTHORIZATION, createBasicAuthToken(sessionId));
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);

        return restTemplate.exchange(uriComponentsBuilder.build().toUriString(), HttpMethod.GET, requestEntity,
                responseType).getBody();
    }

    private String createBasicAuthToken(String sessionId) {
        return String.format(BASIC_AUTH_PATTERN, new String(Base64.encodeBase64(String.format(SESSION_PATTERN, sessionId).getBytes())));
    }

    private synchronized String getSessionId() {
        if (lastSession != null && lastSession.isValidYet()) {
            return lastSession.getId();
        }

        final GraylogProps.AuthProps authProps = applicationProperties.getAuthProps();
        final GraylogProps.CommonApiProps apiProps = applicationProperties.getCommonApiProps();
        lastSession = restTemplate.exchange(RequestEntity.post(URI.create(apiProps.getApiHost() + apiProps.getSessionUrl()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new GraylogSessionRequest(authProps.getUserName(), authProps.getPassword())), GraylogSession.class)
                .getBody();
        return lastSession.getId();
    }
}
