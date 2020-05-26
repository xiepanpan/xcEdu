package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author: xiepanpan
 * @Date: 2020/5/26
 * @Description:
 */
@Component
public class ChooseCourseTask {

    @Autowired
    TaskService taskService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);

    /**
     * 定时发送加选课任务 3秒执行一次
     */
    @Scheduled(cron = "0/3 * * * * *")
    public void sendChooseCourseTask() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.set(GregorianCalendar.MINUTE,-1);
        Date time = calendar.getTime();
        List<XcTask> xcTaskList = taskService.findXcTaskList(time, 1000);
        for (XcTask xcTask:xcTaskList) {
            if (taskService.getTask(xcTask.getId(),xcTask.getVersion())>0) {
                String mqExchange = xcTask.getMqExchange();
                String mqRoutingkey = xcTask.getMqRoutingkey();
                taskService.publish(xcTask,mqExchange,mqRoutingkey);
            }
        }
    }

    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE)
    public void receiveFinishChooseCourseTask(XcTask xcTask) {
        if (xcTask!=null&& StringUtils.isNotEmpty(xcTask.getId())) {
            taskService.finishTask(xcTask.getId());
        }
    }
}
