package com.xuecheng.manage.media.config;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: xiepanpan
 * @Date: 2020/4/13
 * @Description:
 */
@Configuration
public class RabbitMQConfig {
    public static final String EX_MEDIA_PROCESSTASK = "ex_media_processor";

    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    public String routingkey_media_video;

    /**
     * 配置交换机
     * @return
     */
    @Bean(EX_MEDIA_PROCESSTASK)
    public Exchange exchangeMediaVideoTask() {
        return ExchangeBuilder.directExchange(EX_MEDIA_PROCESSTASK).durable(true).build();
    }

}
