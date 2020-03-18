package com.xuecheng.manage.cms;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;

/**
 * @author: xiepanpan
 * @Date: 2020/3/16
 * @Description:  GridFs测试类
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class GridFsTest {

    @Autowired
    GridFsTemplate gridFsTemplate;

    /**
     * 存文件
     * @throws FileNotFoundException
     */
    @Test
    public void testStore() throws FileNotFoundException {
        File file = new File("E:/temp/index_banner.ftl");
        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectId objectId = gridFsTemplate.store(fileInputStream, "index_banner.ftl");
        log.info(String.valueOf(objectId));
        //5e6f52db2ed406762478389a
    }
}
