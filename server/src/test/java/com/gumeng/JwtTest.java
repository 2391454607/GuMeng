package com.gumeng;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/10 下午3:30
 */

public class JwtTest {

    @Test
    public void testGen() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", 1);
        claims.put("username", "张三");
        //生成jwt代码
        String token = JWT.create()
                .withClaim("user",claims) //添加载荷
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 12))  //添加过期时间
                .sign(Algorithm.HMAC256("gumeng")); //指定算法，配置秘钥

        System.out.println(token);
    }

    @Test
    public void testParse(){
        //定义字符串，模拟用户传递过来的token
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjp7ImlkIjoxLCJ1c2VybmFtZSI6IuW8oOS4iSJ9LCJleHAiOjE3NDE2NDA4MDZ9.vokayLJ4Egg82iDPbuTFaQCOQyWpbXbMBUwt-Ufbh_o";

        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256("gumeng")).build();

        DecodedJWT decodedJWT = jwtVerifier.verify(token); //验证token，生成一个解析后的JWT对象
        Map<String, Claim> claims = decodedJWT.getClaims();
        System.out.println(claims.get("user"));
    }

    //篡改了头部和载荷部分的数据，验证失败
    //秘钥改了，验证失败
    //token过期，验证失败
}
