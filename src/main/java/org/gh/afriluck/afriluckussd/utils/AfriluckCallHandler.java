package org.gh.afriluck.afriluckussd.utils;

import org.springframework.data.web.XmlBeamHttpMessageConverter;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AfriluckCallHandler {

    public RestClient client() {
        return RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory())
                .messageConverters(converters -> converters.add(new StringHttpMessageConverter()))
                .baseUrl("http://5.75.226.153")
                .defaultHeader("Content-Type", "application/text")
                .build();
    }
}
