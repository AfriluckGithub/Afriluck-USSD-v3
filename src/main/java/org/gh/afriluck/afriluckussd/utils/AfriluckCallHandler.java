package org.gh.afriluck.afriluckussd.utils;

import org.springframework.data.web.XmlBeamHttpMessageConverter;
import org.springframework.http.HttpHeaders;
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
                .baseUrl("https://app.afriluck.com")
                .defaultHeader("Content-Type", "application/text")
                .defaultHeader("x-afriluck-key", "dmp2clBMZzo0ZDlmZDI5NTY4MDNzRiZTI4YzZkMmRiMDE4ZTY1NmE0MTE5Y2M4Yg")
                .build();
    }
}
