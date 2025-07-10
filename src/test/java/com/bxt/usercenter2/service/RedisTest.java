package com.bxt.usercenter2.service;

import com.bxt.usercenter2.model.domain.user;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.AssertTrue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;

@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Test
    void test(){
        ValueOperations valueOperations=redisTemplate.opsForValue();
        valueOperations.set("z1","shenjingbing");
        valueOperations.set("z2","dai");
        valueOperations.set("z3","wangshi");
        valueOperations.set("z4","zhouwang");
        valueOperations.set("c1","maledaji");
        user user =new user();
        user.setUserAccount("lajijiji");
        valueOperations.set("user",user);
        Object result=valueOperations.get("z1");
        Assertions.assertEquals(result,"shenjingbing");
        result=valueOperations.get("z2");
        Assertions.assertEquals(result,"dai");
        result=valueOperations.get("z3");
        Assertions.assertEquals("wangshi",result);
        result=valueOperations.get("z4");
        Assertions.assertEquals(result,"zhouwang");
        result=valueOperations.get("c1");
        Assertions.assertEquals("maledaji",result);
        result= valueOperations.get("user");
        System.out.println(result);
    }
}
