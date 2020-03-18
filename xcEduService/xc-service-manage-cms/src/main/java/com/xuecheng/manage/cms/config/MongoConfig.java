package com.xuecheng.manage.cms.config;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: xiepanpan
 * @Date: 2020/3/18
 * @Description: mongodb 配置类
 */
@Configuration
public class MongoConfig {
    @Value("${spring.data.mongodb.database}")
    private String db;


    /**
     * GridFSBucket用于打开下载流对象
     * @param mongoClient
     * @return
     */
    @Bean
    public GridFSBucket getGridFsBucket(MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase(db);
        GridFSBucket bucket = GridFSBuckets.create(database);
        return bucket;
    }

}
