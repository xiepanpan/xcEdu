package com.xuecheng.manage.cms.client.dao;

import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author: xiepanpan
 * @Date: 2020/3/23
 * @Description:
 */
public interface CmsSiteRepository extends MongoRepository<CmsSite,String> {
}
