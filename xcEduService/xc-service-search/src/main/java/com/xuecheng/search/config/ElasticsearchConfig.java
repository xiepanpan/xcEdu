package com.xuecheng.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: xiepanpan
 * @Date: 2020/4/9
 * @Description:
 */
@Configuration
public class ElasticsearchConfig {
    @Value("${xuecheng.elasticsearch.hostlist}")
    private String hostlist;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        String[] split = hostlist.split(",");
        HttpHost[] httpHosts = new HttpHost[split.length];
        for (int i = 0; i < split.length; i++) {
            String item = split[i];
            httpHosts[i] = new HttpHost(item.split(":")[0], Integer.parseInt(item.split(":")[1]),"http");
        }
        //创建RestHighLevelClient客户端
        return new RestHighLevelClient(RestClient.builder(httpHosts));
    }

    @Bean
    public RestClient restClient() {
        String[] split = hostlist.split(",");
        HttpHost[] httpHosts = new HttpHost[split.length];
        for (int i = 0; i <split.length; i++) {
            String item = split[i];
            httpHosts[i] = new HttpHost(item.split(":")[0], Integer.parseInt(item.split(":")[1]),"http");
        }
        return RestClient.builder(httpHosts).build();
    }

}
