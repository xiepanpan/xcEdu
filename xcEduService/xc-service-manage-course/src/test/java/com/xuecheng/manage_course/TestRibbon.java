package com.xuecheng.manage_course;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author: xiepanpan
 * @Date: 2020/3/26
 * @Description:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRibbon {

    @Autowired
    RestTemplate restTemplate;

    @Test
    public void testRibbon() {
        String serviceId = "XC-SERVICE-MANAGE-CMS";
        for (int i=0;i<10;i++) {
            ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://" + serviceId + ":/cms/config/getmodel/5a791725dd573c3574ee333f", Map.class);
            Map body = forEntity.getBody();
            System.out.println(body);
        }

    }
}
