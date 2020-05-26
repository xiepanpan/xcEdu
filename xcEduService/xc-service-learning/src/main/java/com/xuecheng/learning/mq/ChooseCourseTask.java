package com.xuecheng.learning.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.config.RabbitMQConfig;
import com.xuecheng.learning.service.LearningService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: xiepanpan
 * @Date: 2020/5/26
 * @Description: 选择课程自动任务处理
 */
@Component
public class ChooseCourseTask {

    @Autowired
    LearningService learningService;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE)
    public void receiveChooseCourseTask(XcTask xcTask) {
        String requestBody = xcTask.getRequestBody();
        Map map = JSON.parseObject(requestBody, Map.class);
        String userId = (String) map.get("userId");
        String courseId = (String) map.get("courseId");

        ResponseResult responseResult = learningService.addCourse(userId, courseId, null, null, null, xcTask);
        if (responseResult.isSuccess()) {
            rabbitTemplate.convertAndSend(RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE,
                    RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE_KEY,xcTask);
        }
    }
}
