package com.xuecheng.manage.media.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan("com.xuecheng.framework.domain.media")
@ComponentScan(basePackages = {"com.xuecheng.api"})
@ComponentScan(basePackages = {"com.xuecheng.manage.media.processor"})
@ComponentScan(basePackages = {"com.xuecheng.framework"})
public class XcServiceManageMediaProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(XcServiceManageMediaProcessorApplication.class, args);
    }

}
