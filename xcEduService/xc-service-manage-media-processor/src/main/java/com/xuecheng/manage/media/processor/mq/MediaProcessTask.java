package com.xuecheng.manage.media.processor.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage.media.processor.dao.MediaFileRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author: xiepanpan
 * @Date: 2020/4/13
 * @Description:
 */
@Component
public class MediaProcessTask {

    @Value("${xc-service-manage-media.ffmpeg-path}")
    String ffmpegPath;

    //上传文件根目录
    @Value("${xc-service-manage-media.video-location}")
    String serverPath;

    @Autowired
    MediaFileRepository mediaFileRepository;

    /**
     * 监听接收视频处理消息进行视频处理
     * @param msg
     */
    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}",containerFactory = "customContainerFactory")
    public void receiveMediaProcessTask(String msg) {
        //1. 解析消息内容 得到mediaId
        Map map = JSON.parseObject(msg, Map.class);
        String mediaId = (String) map.get("mediaId");
        //2. 拿mediaId从数据库查询文件信息
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()) {
            return;
        }
        MediaFile mediaFile = optional.get();
        String fileType = mediaFile.getFileType();
        //只处理avi类型的视频文件
        if (!fileType.equals("avi")) {
            //无需处理
            mediaFile.setProcessStatus("303004");
            mediaFileRepository.save(mediaFile);
            return;
        }else {
            //处理中
            mediaFile.setProcessStatus("303001");
            mediaFileRepository.save(mediaFile);
        }
        //3、使用工具类将avi文件生成mp4
        String videoPath = serverPath + mediaFile.getFilePath() + mediaFile.getFileName();
        String mp4Name = mediaFile.getFileId() + ".mp4";
        String mp4folderPath = serverPath + mediaFile.getFilePath();
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegPath,videoPath,mp4Name,mp4folderPath);
        String result = mp4VideoUtil.generateMp4();

        if (result == null||!result.equals("success")) {
            //处理失败
            mediaFile.setProcessStatus("303003");
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }

        //4. 将mp4生成m3u8和ts文件
        //mp4视频文件路径
        String mp4VideoPath = serverPath + mediaFile.getFilePath() + mp4Name;
        //m3u8文件名称
        String m3u8Name = mediaFile.getFileId() + ".m3u8";
        //m3u8所在目录
        String m3u8FolderPath = serverPath + mediaFile.getFilePath() + "hls/";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpegPath,mp4VideoPath,m3u8Name,m3u8FolderPath);
        String tsResult = hlsVideoUtil.generateM3u8();
        if(tsResult == null || !tsResult.equals("success")) {
            //处理失败
            mediaFile.setProcessStatus("303003");
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        mediaFile.setProcessStatus("303002");
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);

        //保存fileUrl
        String fileUrl = mediaFile.getFilePath() + "hls/" + m3u8Name;
        mediaFile.setFileUrl(fileUrl);
        mediaFileRepository.save(mediaFile);

    }

}
