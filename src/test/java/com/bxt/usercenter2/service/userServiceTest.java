package com.bxt.usercenter2.service;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.bxt.usercenter2.model.domain.user;
import com.bxt.usercenter2.model.domain.user;
import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Mapper;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

// 用户服务测试
@SpringBootTest
//@Mapper
class userServiceTest {

    @Resource
    private userService userService;


    @Test
    public void test_add_user(){
        user user1 = new user();
        user1.setUsername("bxttttt");
//        user1.setId(0L);
        user1.setUserAccount("123");
        user1.setAvatarUrl("https://ww3.sinaimg.cn/mw690/9eb8646fgy1hvuyrbyn9lj216o1z44at.jpg");
        user1.setGender(0);
        user1.setPassword("xxx");
        user1.setIsDelete(0);
        user1.setPhone("123");
        user1.setEmail("456");
        user1.setUserStatus(0);
        user1.setCreateTime(new Date());
        user1.setUpdateTime(new Date());


        boolean result=userService.save(user1);
        System.out.println("id:"+user1.getId());
//        System.out.println(user1.getId());
        Assertions.assertTrue(result);

    }

//    @Test
//    void userRegister() {
//        String userAccount = "bxtttttt";
//        String userPwd = "12345678";
//        String checkPwd = "12345678";
//        long result=userService.userRegister(userAccount, userPwd, checkPwd);
//        System.out.println("result:"+result);
//        Assertions.assertEquals(-1,result);
//    }

    @Test
    void testSearchUserByTags(){
        List<String> tagNameList= Arrays.asList("java","c");
        List<user> userList=userService.searchUsersByTags(tagNameList);
        Assert.assertNotNull(userList);

    }
}