package com.xuecheng.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author: xiepanpan
 * @Date: 2020/4/17
 * @Description:
 */
@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(30*1000);
        httpRequestFactory.setConnectTimeout(2*60*1000);
        httpRequestFactory.setReadTimeout(10*60*1000);
        return new RestTemplate(httpRequestFactory);
    }
}
