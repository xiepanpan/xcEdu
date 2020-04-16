package com.xuecheng.search.service;

import ch.qos.logback.core.util.StringCollectionUtil;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: xiepanpan
 * @Date: 2020/4/9
 * @Description:
 */
@Service
@Slf4j
public class EsCourseService {

    @Value("${xuecheng.course.index}")
    private String index;
    @Value("${xuecheng.course.type}")
    private String type;
    @Value("${xuecheng.course.source_field}")
    private String sourceField;
    @Value("${xuecheng.media.index}")
    private String mediaIndex;
    @Value("${xuecheng.media.type}")
    private String mediaType;
    @Value("${xuecheng.media.source_field}")
    private String mediaSourceField;

    @Autowired
    RestHighLevelClient restHighLevelClient;


    /**
     * 课程搜索
     * @param page
     * @param size
     * @param courseSearchParam
     * @return
     */
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {
        if (courseSearchParam == null) {
            courseSearchParam = new CourseSearchParam();
        }
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest(index);
        //设置搜索类型
        searchRequest.types(type);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


        //创建布尔查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())) {
            //设置匹配占比
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name", "description", "teachplan")
                    .minimumShouldMatch("70%")
                    .field("name", 10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        //一级分类
        if (StringUtils.isNotEmpty(courseSearchParam.getMt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
        }
        if (StringUtils.isNotBlank(courseSearchParam.getGrade())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }

        //设置boolQueryBuilder到searchSourceBuilder
        searchSourceBuilder.query(boolQueryBuilder);

        //分页
        if (page<=0) {
            page =1;
        }
        if (size<=0) {
            size=12;
        }
        //起始记录下标
        int from = (page-1)*size;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);

        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        //设置高亮的字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);
        searchRequest.source(searchSourceBuilder);


        QueryResult<CoursePub> queryResult =new QueryResult();

        List<CoursePub> list = new ArrayList<>();

        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            //获取响应结果
            SearchHits hits = searchResponse.getHits();
            long totalHits = hits.totalHits;
            queryResult.setTotal(totalHits);
            SearchHit[] searchHits = hits.getHits();

            for(SearchHit searchHit: searchHits) {
                CoursePub coursePub = new CoursePub();
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                //取出id
                String id = (String)sourceAsMap.get("id");
                coursePub.setId(id);
                String name = (String) sourceAsMap.get("name");
                //取出高亮字段name
                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                if (!highlightFields.isEmpty()) {
                    HighlightField highlightField = highlightFields.get("name");
                    if (highlightField!=null) {
                        Text[] fragments = highlightField.fragments();
                        StringBuffer stringBuffer = new StringBuffer();
                        for (Text text:fragments) {
                            stringBuffer.append(text);
                        }
                        name = stringBuffer.toString();
                    }
                }
                coursePub.setName(name);
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                //价格
                Double price = null;
                try {
                    if(sourceAsMap.get("price")!=null ){
                        price = (Double) sourceAsMap.get("price");
                    }

                } catch (Exception e) {
                    log.error("查询异常：{}",e);
                }
                coursePub.setPrice(price);
                //旧价格
                Double price_old = null;
                try {
                    if(sourceAsMap.get("price_old")!=null ){
                        price_old = (Double) sourceAsMap.get("price_old");
                    }
                } catch (Exception e) {
                    log.error("查询异常：{}",e);
                }
                coursePub.setPrice_old(price_old);
                list.add(coursePub);
            }
        } catch (IOException e) {
            log.error("查询异常：{}",e);
        }
        queryResult.setList(list);

        QueryResponseResult<CoursePub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    public Map<String, CoursePub> getAll(String id) {
        //定义搜索请求
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("id",id));
        searchRequest.source(searchSourceBuilder);
        Map<String, CoursePub> map = new HashMap<>();

        try {
            SearchResponse search = restHighLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit searchHit : searchHits) {
                CoursePub coursePub = new CoursePub();
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();

                String courseId = (String) sourceAsMap.get("id");
                String name = (String) sourceAsMap.get("name");
                String grade = (String) sourceAsMap.get("grade");
                String charge = (String) sourceAsMap.get("charge");
                String pic = (String) sourceAsMap.get("pic");
                String description = (String) sourceAsMap.get("description");
                String teachplan = (String) sourceAsMap.get("teachplan");
                coursePub.setId(courseId);
                coursePub.setName(name);
                coursePub.setPic(pic);
                coursePub.setGrade(grade);
                coursePub.setTeachplan(teachplan);
                coursePub.setDescription(description);
                map.put(courseId,coursePub);
            }
        } catch (IOException e) {
            log.error("es查询异常：{}",e);
        }
        return map;
    }

    public QueryResponseResult getmedia(String[] teachplanIds) {
        //定义一个搜索请求对象
        SearchRequest searchRequest = new SearchRequest(mediaIndex);
        //指定type
        searchRequest.types(mediaType);

        //定义SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置使用termsQuery根据多个id 查询
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id",teachplanIds));
        //过虑源字段
        String[] includes = mediaSourceField.split(",");
        searchSourceBuilder.fetchSource(includes,new String[]{});
        searchRequest.source(searchSourceBuilder);
        //使用es客户端进行搜索请求Es
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        long total = 0;
        try {
            //执行搜索
            SearchResponse search = restHighLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();
            total = hits.totalHits;
            SearchHit[] searchHits = hits.getHits();
            for(SearchHit hit:searchHits){
                TeachplanMediaPub teachplanMediaPub= new TeachplanMediaPub();
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //取出课程计划媒资信息
                String courseid = (String) sourceAsMap.get("courseid");
                String media_id = (String) sourceAsMap.get("media_id");
                String media_url = (String) sourceAsMap.get("media_url");
                String teachplan_id = (String) sourceAsMap.get("teachplan_id");
                String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");

                teachplanMediaPub.setCourseId(courseid);
                teachplanMediaPub.setMediaUrl(media_url);
                teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);
                teachplanMediaPub.setMediaId(media_id);
                teachplanMediaPub.setTeachplanId(teachplan_id);
                teachplanMediaPubList.add(teachplanMediaPub);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        //数据集合
        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        queryResult.setList(teachplanMediaPubList);
        queryResult.setTotal(total);
        QueryResponseResult<TeachplanMediaPub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;

    }
}
