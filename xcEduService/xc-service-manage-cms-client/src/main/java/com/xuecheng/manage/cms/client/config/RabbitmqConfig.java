package com.xuecheng.manage.cms.client.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: xiepanpan
 * @Date: 2020/3/23
 * @Description: 页面发布rabbitmq配置类
 */
@Configuration
public class RabbitmqConfig {

    //队列bean的名称  页面发布
    public static final String EX_ROUTING_CMS_POSTPAGE="ex_routing_cms_postpage";

    //队列名称
    @Value("${xuecheng.mq.queue}")
    public String queue_cms_postpage_name;
    @Value("${xuecheng.mq.routingKey}")
    public String routingKey;

    /**
     * 交换机配置使用direct类型
     * @return
     */
    @Bean
    public Exchange exchangeTopicInform() {
        return ExchangeBuilder.directExchange(EX_ROUTING_CMS_POSTPAGE).durable(true).build();
    }

    /**
     * 声明队列
     * @return
     */
    @Bean
    public Queue queueCmsPostPage() {
        Queue queue = new Queue(queue_cms_postpage_name);
        return queue;
    }

    /**
     * 绑定队列到交换机
     * @param queue
     * @param exchange
     * @return
     */
    @Bean
    public Binding bindingQueueInformCms(Queue queue,Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs();
    }
}


