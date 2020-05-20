package com.xuecheng.govern.gateway.service;

import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: xiepanpan
 * @Date: 2020/5/19
 * @Description:
 */
@Service
public class AuthService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 查询身份令牌
     * @param request
     * @return
     */
    public String getTokenFromCookie(HttpServletRequest request) {
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        String accessToken = map.get("uid");
        if (StringUtils.isEmpty(accessToken)) {
            return null;
        }
        return accessToken;
    }

    /**
     * 从头取出jwt令牌
     * @param request
     * @return
     */
    public String getJwtFromHeader(HttpServletRequest request) {
        //取出头信息
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authorization)) {
            return null;
        }
        //jwt采用Bearer token  固定格式
        if (!authorization.startsWith("Bearer ")) {
            return null;
        }
        String jwt = authorization.substring(7);
        return jwt;
    }

    public long getExpire(String token) {
        //key
        String key = "user_token:"+token;
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire;
    }
}
