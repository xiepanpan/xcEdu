package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author: xiepanpan
 * @Date: 2020/4/17
 * @Description:
 */
@Service
public class AuthService {

    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;


    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;

    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        //请求spring security 申请令牌
        AuthToken authToken = applyToken(username, password, clientId, clientSecret);
        if (authToken == null) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        String access_token = authToken.getAccess_token();

        String jsonString = JSON.toJSONString(authToken);
        //将令牌存储到redis
        boolean result = saveTokenToRedis(access_token, jsonString, tokenValiditySeconds);
        if (!result) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }
        return authToken;
    }

    private boolean saveTokenToRedis(String access_token, String jsonString, int ttl) {
        String key = "user_token:" +access_token;
        stringRedisTemplate.boundValueOps(key).set(jsonString,ttl, TimeUnit.SECONDS);
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire>0;
    }

    /**
     * 申请令牌
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @return
     */
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret) {
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        URI uri = serviceInstance.getUri();
        String authUrl = uri + "/auth/oauth/token";
        //一键多值的时候使用
        LinkedMultiValueMap<String,String> header = new LinkedMultiValueMap<>();
        String httpBasic = getHttpBasic(clientId, clientSecret);
        header.add("Authorization",httpBasic);

        //定义body
        LinkedMultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body,header);

        //设置restTemplate远程调用时候，对400和401不让报错，正确返回数据
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401) {
                    super.handleError(response);
                }
            }
        });
        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);

        Map bodyMap = exchange.getBody();
        if (bodyMap == null
                || bodyMap.get("access_token") == null
                || bodyMap.get("refresh_token")== null
                || bodyMap.get("jti")==null) {
            String errorDescription = (String) bodyMap.get("error_description");

            if(errorDescription.indexOf("UserDetailsService returned null")>=0){
                ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
            }else if(errorDescription.indexOf("坏的凭证")>=0){
                ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
            }
            return null;
        }
        AuthToken authToken = new AuthToken();
        //用户身份令牌
        authToken.setAccess_token((String) bodyMap.get("jti"));
        //刷新令牌
        authToken.setRefresh_token((String) bodyMap.get("refresh_token"));
        //jwt令牌
        authToken.setJwt_token((String) bodyMap.get("access_token"));
        return authToken;
    }

    /**
     * 获取httpbasic的字符串
     * @param clientId
     * @param clientSecret
     * @return
     */
    private String getHttpBasic(String clientId, String clientSecret) {
        String string = clientId + ":" + clientSecret;
        byte[] encode = Base64Utils.encode(string.getBytes());
        return "Basic "+new String(encode);
    }
}
