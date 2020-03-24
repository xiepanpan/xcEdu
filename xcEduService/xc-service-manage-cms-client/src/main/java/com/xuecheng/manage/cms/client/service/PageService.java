package com.xuecheng.manage.cms.client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.manage.cms.client.dao.CmsPageRepository;
import com.xuecheng.manage.cms.client.dao.CmsSiteRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;

/**
 * @author: xiepanpan
 * @Date: 2020/3/23
 * @Description: 页面服务类
 */
@Slf4j
@Service
public class PageService {

    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    CmsSiteRepository cmsSiteRepository;
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;

    /**
     * 保存发布页面到服务器物理路径
     * @param pageId
     */
    public void savePageToServerPath(String pageId) {
        //根据pageId查询cmsPage
        CmsPage cmsPage = findCmsPageById(pageId);
        String htmlFileId = cmsPage.getHtmlFileId();

        //查询静态页面html文件
        InputStream inputStream = getFileById(htmlFileId);
        if (inputStream== null) {
            return;
        }

        //得到站点id
        String siteId = cmsPage.getSiteId();
        CmsSite cmsSite = findCmSiteById(siteId);
        String sitePhysicalPath = cmsSite.getSitePhysicalPath();
        String pagePath = sitePhysicalPath + cmsPage.getPagePhysicalPath() + cmsPage.getPageName();
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File(pagePath));
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (Exception e) {
            log.error("文件输出错误",e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("输入流关闭异常",e);
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                log.error("输出流关闭异常",e);
            }
        }

    }

    //根据站点查询站点信息
    private CmsSite findCmSiteById(String siteId) {
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    private InputStream getFileById(String fileId) {
        //获取文件对象
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        //打开下载流
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //定义GridResource
        GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
        try {
            return gridFsResource.getInputStream();
        } catch (IOException e) {
            log.info("获取文件失败：{}",e);
        }
        return null;
    }

    /**
     * 根据页面id查询页面信息
     * @param pageId
     * @return
     */
    public CmsPage findCmsPageById(String pageId) {
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}
