package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author: xiepanpan
 * @Date: 2020/3/24
 * @Description:
 */
@Service
public class CourseService {

    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    TeachplanRepository teachplanRepository;
    @Autowired
    CoursePicRepository coursePicRepository;
    @Autowired
    CourseMarketRepository courseMarketRepository;
    @Autowired
    CmsPageClient cmsPageClient;
    @Autowired
    CoursePubRepository coursePubRepository;
    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;

    @Value("${course-publish.dataUrlPre}")
    private String publishDataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publishPagePhysicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publishPageWebpath;
    @Value("${course-publish.siteId}")
    private String publishSiteId;
    @Value("${course-publish.templateId}")
    private String publishTemplateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;

    /**
     * 查询课程计划
     * @param courseId
     * @return
     */
    public TeachplanNode findTeachplanList(String courseId) {
        return teachplanMapper.selectList(courseId);
    }

    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if (teachplan==null||
                StringUtils.isEmpty(teachplan.getPname())||
                StringUtils.isEmpty(teachplan.getCourseid())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        String courseid = teachplan.getCourseid();
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)) {
            parentid = getTeachplanRoot(courseid);
        }
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan teachplan1 = optional.get();
        String parentGrade = teachplan1.getGrade();
        //根据父节点级别设置节点级别
        if (parentGrade.equals("1")) {
            teachplan.setGrade("2");
        }else {
            teachplan.setGrade("3");
        }
        //未发布
        teachplan.setStatus("0");
        teachplan.setParentid(parentid);
        teachplanRepository.save(teachplan);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 获取课程根节点
     * @param courseid
     * @return
     */
    private String getTeachplanRoot(String courseid) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseid);
        if (!optional.isPresent()) {
            return null;
        }
        CourseBase courseBase = optional.get();
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseid, "0");
        if (teachplanList==null || teachplanList.size()<=0) {
            //没有课程根节点 新添加一个
            Teachplan teachplan = new Teachplan();
            teachplan.setCourseid(courseid);
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setStatus("0");
            teachplan.setPname(courseBase.getName());
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }

        return teachplanList.get(0).getId();

    }

    //向课程管理数据添加课程与图片的关联信息
    @Transactional
    public ResponseResult addCoursePic(String courseId, String pic) {
        //课程图片信息
        CoursePic coursePic = null;
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        if(picOptional.isPresent()){
            coursePic = picOptional.get();
        }
        if(coursePic == null){
            coursePic  = new CoursePic();
        }
        coursePic.setPic(pic);
        coursePic.setCourseid(courseId);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CoursePic findCoursePic(String courseId) {
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            return coursePic;
        }
        return null;
    }

    //删除课程图片
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        //执行删除
        long result = coursePicRepository.deleteByCourseid(courseId);
        if(result>0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 课程预览
     * @param courseId
     * @return
     */
    public CoursePublishResult preview(String courseId) {
        CourseBase courseBaseById = findCourseBaseById(courseId);
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publishSiteId);
        cmsPage.setPageName(courseId+".html");
        cmsPage.setPageAliase(courseBaseById.getName());
        cmsPage.setPageWebPath(publishPageWebpath);
        cmsPage.setPagePhysicalPath(publishPagePhysicalpath);
        cmsPage.setTemplateId(publishTemplateId);
        cmsPage.setDataUrl(publishDataUrlPre+courseId);

        //远程调用cms
        CmsPageResult cmsPageResult = cmsPageClient.saveCmsPage(cmsPage);
        if (!cmsPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();
        //拼装页面预览的url
        String url = previewUrl + pageId;
        return new CoursePublishResult(CommonCode.SUCCESS,url);

    }

    /**
     * 根据id查询课程基本信息
     * @param courseId
     * @return
     */
    private CourseBase findCourseBaseById(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (optional.isPresent()) {
            CourseBase courseBase = optional.get();
            return courseBase;
        }
        ExceptionCast.cast(CourseCode.COURSE_NOTEXIST);
        return null;
    }

    /**
     * 查询课程视图，包括基本信息、图片、营销、课程计划
     * @param id
     * @return
     */
    public CourseView getCourseView(String id) {
        CourseView courseView = new CourseView();

        //查询课程基本信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if (courseBaseOptional.isPresent()) {
            CourseBase courseBase = courseBaseOptional.get();
            courseView.setCourseBase(courseBase);
        }
        //查询课程图片
        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(id);
        if (coursePicOptional.isPresent()) {
            CoursePic coursePic = coursePicOptional.get();
            courseView.setCoursePic(coursePic);
        }
        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if (marketOptional.isPresent()) {
            CourseMarket courseMarket = marketOptional.get();
            courseView.setCourseMarket(courseMarket);
        }
        //课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }

    @Transactional
    public CoursePublishResult publish(String id) {
        //查询课程
        CourseBase courseBaseById = findCourseBaseById(id);

        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publishSiteId);
        cmsPage.setDataUrl(publishDataUrlPre+id);
        cmsPage.setPageName(id+".html");
        cmsPage.setPageAliase(courseBaseById.getName());
        cmsPage.setPagePhysicalPath(publishPagePhysicalpath);
        cmsPage.setPageWebPath(publishPageWebpath);
        cmsPage.setTemplateId(publishTemplateId);
        //调用cms一键发布接口将课程详情页面发布到服务器
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if (!cmsPostPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //保存课程发布状态为已发布
        CourseBase courseBase = saveCoursePubState(id);
        if (courseBase == null) {
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        String pageUrl = cmsPostPageResult.getPageUrl();

        //保存课程索引信息
        CoursePub coursePub = createCoursePub(id);
        saveCoursePub(id,coursePub);

        //向teachplanMediaPub中保存课程媒资信息
        saveTeachplanMediaPub(id);

        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    private void saveTeachplanMediaPub(String courseId) {
        //先删teachplanMediaPub的数据
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        for (TeachplanMedia teachplanMedia:teachplanMediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub);
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubList.add(teachplanMediaPub);
        }

        teachplanMediaPubRepository.saveAll(teachplanMediaPubList);
    }

    /**
     * 保存课程发布信息 用于搜索
     * @param id
     * @param coursePub
     * @return
     */
    private CoursePub saveCoursePub(String id, CoursePub coursePub) {
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(id);
        CoursePub coursePubNew = null;
        if (coursePubOptional.isPresent()) {
            coursePubNew = coursePubOptional.get();
        }else {
            coursePubNew = new CoursePub();
        }
        BeanUtils.copyProperties(coursePub,coursePubNew);
        coursePubNew.setId(id);
        coursePubNew.setTimestamp(new Date());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(date);
        coursePubRepository.save(coursePubNew);
        return coursePubNew;
    }

    /**
     * 创建coursePub对象
     * @param id
     * @return
     */
    private CoursePub createCoursePub(String id) {
        CoursePub coursePub = new CoursePub();
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(id);
        if (baseOptional.isPresent()) {
            CourseBase courseBase = baseOptional.get();
            BeanUtils.copyProperties(courseBase,coursePub);
        }

        //查询课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if (marketOptional.isPresent()) {
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket,coursePub);
        }
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        String jsonString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(jsonString);
        return coursePub;
    }

    private CourseBase saveCoursePubState(String courseId) {
        CourseBase courseBaseById = findCourseBaseById(courseId);
        courseBaseById.setStatus("202002");
        courseBaseRepository.save(courseBaseById);
        return courseBaseById;
    }

    /**
     * 保存课程计划与媒体文件的关联信息
     * @param teachplanMedia
     * @return
     */
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if (teachplanMedia == null || StringUtils.isEmpty(teachplanMedia.getTeachplanId())) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        String teachplanId = teachplanMedia.getTeachplanId();
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        Teachplan teachplan = optional.get();
        String grade = teachplan.getGrade();
        if (StringUtils.isEmpty(grade)||!grade.equals("3")) {
            //只允许选择第三级的课程计划关联视频
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        Optional<TeachplanMedia> mediaOptional = teachplanMediaRepository.findById(teachplanId);
        TeachplanMedia one = null;
        if (mediaOptional.isPresent()) {
            one = mediaOptional.get();
        }else {
            one = new TeachplanMedia();
        }

        //将one保存到数据库
        one.setCourseId(teachplan.getCourseid());
        one.setMediaId(teachplanMedia.getMediaId());
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        one.setMediaUrl(teachplanMedia.getMediaUrl());
        one.setTeachplanId(teachplanId);
        teachplanMediaRepository.save(one);

        return new ResponseResult(CommonCode.SUCCESS);
    }
}
