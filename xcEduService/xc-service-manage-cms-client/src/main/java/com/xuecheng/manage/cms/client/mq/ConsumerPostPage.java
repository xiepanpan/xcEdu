package com.xuecheng.manage.cms.client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage.cms.client.service.PageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: xiepanpan
 * @Date: 2020/3/23
 * @Description: 监听mq  接收发布页面消息
 */
@Component
@Slf4j
public class ConsumerPostPage {
    @Autowired
    PageService pageService;

    @RabbitListener(queues={"${xuecheng.mq.queue}"})
    public void postPage(String msg) {
        Map map = JSON.parseObject(msg, Map.class);
        String pageId = (String) map.get("pageId");
        //校验页面是否存在
        CmsPage cmsPage = pageService.findCmsPageById(pageId);
        if (cmsPage==null) {
            log.error("receive postpage msg,cmsPage is null,pageId:{}",pageId);
            return;
        }
        //将页面从GridFs中下载到服务器
        pageService.savePageToServerPath(pageId);
    }

}
