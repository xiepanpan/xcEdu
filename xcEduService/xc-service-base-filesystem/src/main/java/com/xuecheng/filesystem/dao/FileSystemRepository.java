package com.xuecheng.filesystem.dao;

import com.xuecheng.framework.domain.filesystem.FileSystem;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author: xiepanpan
 * @Date: 2020/3/25
 * @Description:
 */
public interface FileSystemRepository extends MongoRepository<FileSystem,String> {
}
