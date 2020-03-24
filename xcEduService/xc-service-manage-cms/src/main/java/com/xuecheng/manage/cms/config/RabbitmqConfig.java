package com.xuecheng.manage.cms.config;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: xiepanpan
 * @Date: 2020/3/23
 * @Description:
 */
@Configuration
public class RabbitmqConfig {

    //交换机名称
    public static final String EX_ROUTING_CMS_POSTPAGE="ex_routing_cms_postpage";

    /**
     *  交换机配置使用direct类型
     * @return
     */
    @Bean
    public Exchange exchangeTopicInform() {
        return ExchangeBuilder.directExchange(EX_ROUTING_CMS_POSTPAGE).durable(true).build();
    }

}
