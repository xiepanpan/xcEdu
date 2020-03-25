package com.xuecheng.testfastdfs.controller;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author: xiepanpan
 * @Date: 2020/3/25
 * @Description:
 */
@Controller
public class Fast {
    @Autowired
    FastFileStorageClient storageClient;

    @RequestMapping("/test")
    public void testUpload() throws FileNotFoundException {
        File file = new File("D:\\pic\\ceshi.jpg");
        // 上传并且生成缩略图
        StorePath storePath = this.storageClient.uploadFile(
                new FileInputStream(file), file.length(), "jpg", null);
        // 带分组的路径
        System.out.println(storePath.getFullPath());
        // 不带分组的路径
        System.out.println(storePath.getPath());
    }
}
