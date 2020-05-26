package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author: xiepanpan
 * @Date: 2020/5/26
 * @Description:
 */
@Service
public class TaskService {

    @Autowired
    XcTaskRepository xcTaskRepository;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    XcTaskHisRepository xcTaskHisRepository;

    /**
     * 查询前n条任务
     * @param updateTime
     * @param size
     * @return
     */
    public List<XcTask> findXcTaskList(Date updateTime, int size) {
        Pageable pageable = new PageRequest(0, size);
        Page<XcTask> all = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        List<XcTask> taskList = all.getContent();
        return taskList;
    }

    /**
     * 获取任务  使用乐观锁版本号 防止重复发送任务
     * @param id
     * @param version
     * @return
     */
    @Transactional
    public int getTask(String id, Integer version) {
        int count = xcTaskRepository.updateTaskVersion(id, version);
        return count;
    }

    /**
     * 发布消息
     * @param xcTask
     * @param mqExchange
     * @param mqRoutingkey
     */
    public void publish(XcTask xcTask, String mqExchange, String mqRoutingkey) {
        Optional<XcTask> optional = xcTaskRepository.findById(xcTask.getId());
        if (optional.isPresent()) {
            rabbitTemplate.convertAndSend(mqExchange,mqRoutingkey,xcTask);
            //更新任务时间
            XcTask one = optional.get();
            one.setUpdateTime(new Date());
            xcTaskRepository.save(one);
        }
    }

    /**
     * 完成任务
     * @param taskId
     */
    @Transactional
    public void finishTask(String taskId) {
        Optional<XcTask> optionalXcTask = xcTaskRepository.findById(taskId);
        if (optionalXcTask.isPresent()) {
            //当前任务
            XcTask xcTask = optionalXcTask.get();

            //添加历史任务
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask,xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
            //删除当前任务
            xcTaskRepository.delete(xcTask);
        }
    }
}
