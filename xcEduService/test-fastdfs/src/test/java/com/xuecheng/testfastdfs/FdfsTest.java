//package com.xuecheng.testfastdfs;
//
//import com.github.tobato.fastdfs.domain.StorePath;
//import com.github.tobato.fastdfs.service.FastFileStorageClient;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//
//public class FdfsTest {
//
//    @Autowired
//    private FastFileStorageClient storageClient;
//
//
//    @Test
//    public void testUpload() throws FileNotFoundException {
//        File file = new File("D:\\pic\\ceshi.jpg");
//        // 上传并且生成缩略图
//        StorePath storePath = this.storageClient.uploadFile(
//                new FileInputStream(file), file.length(), "jpg", null);
//        // 带分组的路径
//        System.out.println(storePath.getFullPath());
//        // 不带分组的路径
//        System.out.println(storePath.getPath());
//    }
//
//    @Test
//    public void testUploadAndCreateThumb() throws FileNotFoundException {
//        File file = new File("D:\\pic\\ceshi.jpg");
//        // 上传并且生成缩略图
//        StorePath storePath = this.storageClient.uploadFile(
//                new FileInputStream(file), file.length(), "jpg", null);
//        // 带分组的路径
//        System.out.println(storePath.getFullPath());
//        // 不带分组的路径
//        System.out.println(storePath.getPath());
//    }
//}
