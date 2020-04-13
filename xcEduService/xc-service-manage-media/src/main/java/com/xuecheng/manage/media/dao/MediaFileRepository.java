package com.xuecheng.manage.media.dao;

import com.xuecheng.framework.domain.media.MediaFile;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author: xiepanpan
 * @Date: 2020/4/10
 * @Description:
 */
public interface MediaFileRepository extends MongoRepository<MediaFile,String> {
}
