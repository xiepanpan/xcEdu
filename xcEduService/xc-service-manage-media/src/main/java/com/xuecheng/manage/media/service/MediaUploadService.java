package com.xuecheng.manage.media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage.media.dao.MediaFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * @author: xiepanpan
 * @Date: 2020/4/10
 * @Description:
 */
@Service
@Slf4j
public class MediaUploadService {

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    String uploadLocation;

    /**
     * 文件上传前的注册，检查文件是否存在
     * 根据文件md5得到文件路径
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //文件所属目录的路径
        String fileFolderPath = getFileFolderPath(fileMd5);
        //文件的路径
        String filePath = getFilePath(fileMd5, fileExt);
        File file = new File(filePath);
        boolean exists = file.exists();

        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        if (exists&& optional.isPresent()) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        //文件不存在时作一些准备工作，检查文件所在目录是否存在，如果不存在则创建
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    private String getFilePath(String fileMd5, String fileExt) {
        return uploadLocation + fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" + fileMd5 + "." +fileExt;
    }

    /**
     * 得到文件所属目录
     * @param fileMd5
     * @return
     */
    private String getFileFolderPath(String fileMd5) {
        return uploadLocation+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/";
    }

    /**
     * 得到块文件所属目录路径
     * @param fileMd5
     * @return
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return uploadLocation+ fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+fileMd5+"/chunk/";

    }


    /**
     * 分块检查
     * @param fileMd5
     * @param chunk
     * @param chunkSize
     * @return
     */
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkFile = new File(chunkFileFolderPath+chunk);
        if (chunkFile.exists()) {
            //块文件存在
            return new CheckChunkResult(CommonCode.SUCCESS,true);
        }else {
            //块文件不存在
            return new CheckChunkResult(CommonCode.SUCCESS,false);
        }
    }



    /**
     * 上传分块
     * @param file
     * @param fileMd5
     * @param chunk
     * @return
     */
    public ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        String chunkFilePath = chunkFileFolderPath + chunk;
        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()) {
            chunkFileFolder.mkdirs();
        }
        //得到上传的输入流 写入到分块目录中
        try (InputStream inputStream = file.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(new File(chunkFilePath))){

            IOUtils.copy(inputStream,outputStream);
        } catch (IOException e) {
            log.error("文件异常:",e);
        }
        return new ResponseResult(CommonCode.SUCCESS);

    }

    public ResponseResult mergeChunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {


        //1、合并所有分块
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        File[] files = chunkFileFolder.listFiles();
        List<File> fileList = Arrays.asList(files);
        //创建一个合并文件
        String filePath = getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);

        mergeFile = mergeFile(fileList, mergeFile);
        if (mergeFile == null) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }

        //2、校验文件的md5值是否和前端传入的md5一到
        boolean checkFileMd5 = checkFileMd5(mergeFile, fileMd5);
        if (!checkFileMd5) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }

        //3、将文件的信息写入mongodb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5+"."+fileExt);
        //文件相对路径
        String filePath1 = fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + "." + fileExt;
        mediaFile.setFilePath(filePath1);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态改为上传成功
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    private boolean checkFileMd5(File mergeFile, String fileMd5) {
        try (FileInputStream fileInputStream = new FileInputStream(mergeFile)) {
            String md5Hex = DigestUtils.md5Hex(fileInputStream);
            if (fileMd5.equalsIgnoreCase(md5Hex)) {
                return true;
            }
        }catch (Exception e) {
            log.error("校验文件异常：",e);
            return false;
        }
        return false;
    }

    private File mergeFile(List<File> chunkFileList, File mergeFile) {
        try {
            if (mergeFile.exists()) {
                mergeFile.delete();
            }else {
                mergeFile.createNewFile();
            }

            //对块文件排序
            Collections.sort(chunkFileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())) {
                        return 1;
                    }
                    return -1;
                }
            });

            RandomAccessFile rafWrite = new RandomAccessFile(mergeFile, "rw");
            byte[] b = new byte[1024];
            for (File chunkFile:chunkFileList) {
                RandomAccessFile rafRead = new RandomAccessFile(chunkFile, "r");
                int len = -1;
                while ((len = rafRead.read(b))!=-1) {
                    rafWrite.write(b,0,len);
                }
                rafRead.close();
            }
            rafWrite.close();
            return mergeFile;
        } catch (IOException e) {
            log.error("合并文件异常：",e);
            return null;
        }

    }
}
