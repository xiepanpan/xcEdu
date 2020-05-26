package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: xiepanpan
 * @Date: 2020/5/25
 * @Description:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestJwt {

    /**
     * 创建jwt令牌
     */
    @Test
    public void testCreateJwt() {
        //密钥库文件
        String keystore = "xc.keystore";
        //密钥库的密码
        String keystorePassword = "xuechengkeystore";

        //密钥库文件路径
        ClassPathResource classPathResource = new ClassPathResource(keystore);
        //密钥别名
        String alias  = "xckey";
        //密钥的访问密码
        String keyPassword = "xuecheng";
        //密钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(classPathResource, keystorePassword.toCharArray());
        //密钥对（公钥和私钥）
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, keyPassword.toCharArray());
        //获取私钥
        RSAPrivateKey aPrivate = (RSAPrivateKey) keyPair.getPrivate();
        //jwt令牌内容
        Map<String,String> body = new HashMap<>();
        body.put("name","itcast");
        String bodyString = JSON.toJSONString(body);
        //私钥加密 生成jwt令牌
        Jwt jwt = JwtHelper.encode(bodyString, new RsaSigner(aPrivate));
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }

    /**
     * 校验jwt令牌  使用公钥解密
     */
    @Test
    public void testVerify() {
        //
        String publicKey ="-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5UM5z6nA6GTqqfidk7mLyiHwms55COGQGqELpeJSNG1SUQGz9/PMNTKikyrA01LXYNQnNYXIAL8Y+X2ESi8AWLKtXEr0HD1PDdq3L7oA5BPBbPjwYvsO0wlsyRRFZjZ4NaZt+BJKpxfZiAjCsWcqD2kjJKsvxHpHi6HKRhg5KtBmw11elUveVO3J8PJseSCGk+BjDT6ZRQDb4tFhxiRpahI0TE6GpY5p7fqe4kJgSRmVVCpVkzUtF7Xc0i8Ci0acR7m5vXaWoPSYiGe65vjqxElCjNNlFg0TFnr8gE935egN7aGoAZQSbm4S3EF6Wz4KpAmTQ0NPdzdeLPZn09SeSwIDAQAB-----END PUBLIC KEY-----";
        String jwtString = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiaXRjYXN0In0.CKuKB_CrPe0pFuVy-ypoMsoNNAz_gt35dB0sJZTmEEhCJ102KVJrHTvD-zjMh7thd5RbG_9QsDdUeQoIZxeDXHqBOhd70izBw9GMfV3BgMiWmu1CSe0lsW9USxjg8Hnosp6_l0RN6qgBJQUzEPzzo-SezEUzqj7hewfzSw3zdujslzTaPhOIEjE3dKTqm04f9iFN5aLNALD8K2HacROdv2BD1ickPwnOdQi8dnklJJzl1YjkSbbdiE3Ar2es9FNZ1NO6LWxYhpIYssF7JAtl1uJSGgEKeMZlBsBr2DulVO6zsesdA1YJ1PEtFDoA_k4kkZkKrWe0_TVLAIWyI53qWg";
        Jwt jwt = JwtHelper.decodeAndVerify(jwtString, new RsaVerifier(publicKey));
        String claims = jwt.getClaims();
        System.out.println(claims);
    }


}
