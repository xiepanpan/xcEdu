package com.xuecheng.learning.config;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: xiepanpan
 * @Date: 2020/5/26
 * @Description:  自动任务的交换机配置
 */
@Configuration
public class RabbitMQConfig {
    //添加选课任务交换机
    public static final String  EX_LEARNING_ADDCHOOSECOURSE = "ex_learning_addchoosecourse";

    //完成添加选课消息队列
    public static final String XC_LEARNING_FINISHADDCHOOSECOURSE = "xc_learning_finishaddchoosecourse";

    //添加选课消息队列
    public static final String XC_LEARNING_ADDCHOOSECOURSE = "xc_learning_addchoosecourse";

    //添加选课路由key
    public static final String XC_LEARNING_ADDCHOOSECOURSE_KEY = "addchoosecourse";
    //完成添加选课路由key
    public static final String XC_LEARNING_FINISHADDCHOOSECOURSE_KEY = "finishaddchoosecourse";

    /**
     * 声明添加课程交换机
     * @return
     */
    @Bean(EX_LEARNING_ADDCHOOSECOURSE)
    public Exchange exDeclare() {
        return ExchangeBuilder.directExchange(EX_LEARNING_ADDCHOOSECOURSE).durable(true).build();
    }

    /**
     * 声明添加课程队列
     * @return
     */
    @Bean(XC_LEARNING_ADDCHOOSECOURSE)
    public Queue queueDeclare() {
        Queue queue = new Queue(XC_LEARNING_ADDCHOOSECOURSE,true,false,true);
        return queue;
    }

    @Bean(XC_LEARNING_FINISHADDCHOOSECOURSE)
    public Queue queueDeclare2() {
        Queue queue = new Queue(XC_LEARNING_FINISHADDCHOOSECOURSE,true,false,true);
        return queue;
    }

    /**
     * 绑定队列到交换机
     * @param queue
     * @param exchange
     * @return
     */
    @Bean
    public Binding bindingAddChooseProcessTask(@Qualifier(XC_LEARNING_ADDCHOOSECOURSE)Queue queue,
                                               @Qualifier(EX_LEARNING_ADDCHOOSECOURSE)Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(XC_LEARNING_ADDCHOOSECOURSE_KEY).noargs();
    }


}
