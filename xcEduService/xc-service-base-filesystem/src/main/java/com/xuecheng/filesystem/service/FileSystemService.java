package com.xuecheng.filesystem.service;
import java.io.IOException;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: xiepanpan
 * @Date: 2020/3/25
 * @Description:  文件系统service
 */
@Service
@Slf4j
public class FileSystemService {

    @Autowired
    FastFileStorageClient storageClient;
    @Autowired
    FileSystemRepository fileSystemRepository;

    public UploadFileResult upload(MultipartFile multipartFile, String filetag, String businesskey, String metadata) {
        if (multipartFile == null) {
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }
        //第一步：将文件上传到fastDFS中，得到一个文件id
        String fileId = fdfsUpload(multipartFile);
        if (StringUtils.isEmpty(fileId)) {
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
        }
        //第二步：将文件id及其它文件信息存储到mongodb中。
        FileSystem fileSystem = new FileSystem();
        fileSystem.setFileId(fileId);
        fileSystem.setFilePath(fileId);
        fileSystem.setFileName(multipartFile.getOriginalFilename());
        fileSystem.setFileType(multipartFile.getContentType());
        fileSystem.setBusinesskey(businesskey);
        fileSystem.setFiletag(filetag);
        if (StringUtils.isNotEmpty(metadata)) {
            Map map = JSON.parseObject(metadata, Map.class);
            fileSystem.setMetadata(map);
        }
        fileSystemRepository.save(fileSystem);
        return new UploadFileResult(CommonCode.SUCCESS,fileSystem);
    }

    /**
     * fastdfs 文件上传
     * @param multipartFile
     * @return
     */
    private String fdfsUpload(MultipartFile multipartFile) {
        //文件原始名称
        String originalFilename = multipartFile.getOriginalFilename();
        //文件扩展名
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        StorePath storePath = null;
        try {
            storePath = storageClient.uploadFile(multipartFile.getInputStream(), multipartFile.getSize(), ext, null);
        } catch (IOException e) {
            log.error("文件存储异常：{}",e);
        }
        //返回带分组的路径
        return storePath.getFullPath();
    }
}
