package com.xuecheng.manage.media.processor.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: xiepanpan
 * @Date: 2020/4/13
 * @Description:
 */
@Configuration
public class RabbitMQConfig {

    public static final String EX_MEDIA_PROCESSOR="ex_media_processor";

    //视频处理队列
    @Value("${xc-service-manage-media.mq.queue-media-video-processor}")
    public  String queue_media_video_processtask;

    //视频处理路由
    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    public  String routingkey_media_video;

    //消费者并发数量
    public static final int DEFAULT_CONCURRENT = 10;

    /**
     * 配置容器工厂 增加并发处理数量 默认情况是单线程监听队列
     * @param configurer
     * @param connectionFactory
     * @return
     */
    @Bean("customContainerFactory")
    public SimpleRabbitListenerContainerFactory containerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer,
                                                                 ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConcurrentConsumers(DEFAULT_CONCURRENT);
        factory.setMaxConcurrentConsumers(DEFAULT_CONCURRENT);
        configurer.configure(factory,connectionFactory);
        return factory;
    }

    /**
     * 配置交换机
     * @return
     */
    @Bean(EX_MEDIA_PROCESSOR)
    public Exchange exMediaVideoTask() {
        return ExchangeBuilder.directExchange(EX_MEDIA_PROCESSOR).durable(true).build();
    }

    /**
     * 声明队列
     * @return
     */
    @Bean("queue_meida_video_processtask")
    public Queue queueProcessTask() {
        Queue queue = new Queue(queue_media_video_processtask,true,false,true);
        return queue;
    }

    /**
     * 用什么路由规则绑定队列到交换机
     * @param queue
     * @param exchange
     * @return
     */
    @Bean
    public Binding bindingQueueMediaProcessTask(@Qualifier("queue_meida_video_processtask")Queue queue,
                                                @Qualifier(EX_MEDIA_PROCESSOR)Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingkey_media_video).noargs();
    }

}
