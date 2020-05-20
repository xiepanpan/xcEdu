package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * @author: xiepanpan
 * @Date: 2020/5/19
 * @Description:
 */
@Component
public class LoginFilter extends ZuulFilter {

    @Autowired
    AuthService authService;

    @Override
    public String filterType() {
        /**
         pre：请求在被路由之前执行

         routing：在路由请求时调用

         post：在routing和errror过滤器之后调用

         error：处理请求时发生错误调用

         */
        return "pre";
    }

    //过虑器序号，越小越被优先执行
    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        //返回true表示要执行此过虑器
        return true;
    }

    /**
     * 过虑器的内容
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() {

        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        String tokenFromCookie = authService.getTokenFromCookie(request);
        //1、从cookie查询用户身份令牌是否存在，不存在则拒绝访问
        if (StringUtils.isEmpty(tokenFromCookie)) {
            //拒绝访问
            accessDenied();
            return null;
        }
        //2、从http header查询jwt令牌是否存在，不存在则拒绝访问
        String jwtFromHeader = authService.getJwtFromHeader(request);
        if (StringUtils.isEmpty(jwtFromHeader)) {
            accessDenied();
            return null;
        }
        //3、从Redis查询user_token令牌是否过期，过期则拒绝访问
        long expire = authService.getExpire(tokenFromCookie);
        if (expire<0) {
            accessDenied();
            return null;
        }
        return null;
    }

    /**
     * 拒绝访问
     */
    private void accessDenied() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletResponse response = requestContext.getResponse();
        //拒绝访问
        requestContext.setSendZuulResponse(false);
        //响应代码
        requestContext.setResponseStatusCode(200);
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
        String jsonString = JSON.toJSONString(responseResult);
        requestContext.setResponseBody(jsonString);
        response.setContentType("application/json; charset=UTF-8");
    }
}
