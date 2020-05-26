package com.xuecheng.framework.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @author: xiepanpan
 * @Date: 2020/5/26
 * @Description:  Feign拦截器 解决微服务之间header中jwt信息无法携带问题
 */
public class FeignClientInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes!=null) {
            HttpServletRequest request = requestAttributes.getRequest();
            //取出当前请求的header 找到jwt令牌
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames!=null) {
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    String headerValue = request.getHeader(headerName);
                    requestTemplate.header(headerName,headerValue);
                }
            }
        }
    }
}
