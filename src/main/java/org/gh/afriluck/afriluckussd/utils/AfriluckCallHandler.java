package org.gh.afriluck.afriluckussd.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AfriluckCallHandler {

    @Value("${env.data.apikey}")
    private String apiKey;

    @Value("${env.data.baseUrl}")
    private String baseUrl;

    public RestClient client() {
        return RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory())
                .messageConverters(converters -> converters.add(new StringHttpMessageConverter()))
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/text")
                .defaultHeader("x-afriluck-key", apiKey)
                .build();
    }
}
