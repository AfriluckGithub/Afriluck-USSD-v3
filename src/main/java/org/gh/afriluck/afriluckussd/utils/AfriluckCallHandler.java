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
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        // Set connection timeout to 30 seconds
        requestFactory.setConnectTimeout(30000);
        // Set read timeout to 60 seconds
        requestFactory.setConnectionRequestTimeout(30000);
        return RestClient.builder()
                .requestFactory(requestFactory)
                .messageConverters(converters -> converters.add(new StringHttpMessageConverter()))
                .baseUrl("http://10.180.180.22:5050")
                .defaultHeader("Content-Type", "application/text")
                .defaultHeader("x-afriluck-key", apiKey)
                .build();
    }

    public RestClient staging() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        // Set connection timeout to 30 seconds
        requestFactory.setConnectTimeout(30000);
        // Set read timeout to 60 seconds
        requestFactory.setConnectionRequestTimeout(30000);
        return RestClient.builder()
                .requestFactory(requestFactory)
                .messageConverters(converters -> converters.add(new StringHttpMessageConverter()))
                .baseUrl("http://10.180.180.22:5050")
                .defaultHeader("Content-Type", "application/text")
                .defaultHeader("x-afriluck-key", apiKey)
                .build();
    }
}
