package com.bxt.usercenter2.once;

import com.bxt.usercenter2.Usercenter2Application;
import com.bxt.usercenter2.mapper.userMapper;
import com.bxt.usercenter2.model.domain.user;
import com.bxt.usercenter2.service.userService;
import jakarta.annotation.Resource;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.imageio.stream.IIOByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class insertUsers {
    @Resource
    private userMapper userMapper;

    @Resource
    private userService userService;

    public void doInsertUsers(){
        List<user> userList=new ArrayList<>();
        final int INSERT_NUM=50000;
        final int THREAD_COUNT=10;
        final int BATCH_SIZE=1000;
        ExecutorService executorService = Executors.newFixedThreadPool(INSERT_NUM);
        for (int i=0;i<INSERT_NUM;i++){
            user user=new user();
            user.setUsername("lilililili");
            user.setUserAccount("lilililili");
            user.setTags("");
            user.setAvatarUrl("");
            user.setGender(0);
            user.setPassword("12345678");
            user.setUserRole(0);
            user.setPhone("");
            user.setEmail("");
            user.setUserStatus(0);
            userList.add(user);
        }
        int batch_count=(int) Math.ceil((double) INSERT_NUM / BATCH_SIZE);
        for (int i=0;i<batch_count;i++){
            int start=i*BATCH_SIZE;
            int end=Math.min(INSERT_NUM,start+BATCH_SIZE);
            List<user> subList=userList.subList(start,end);
            executorService.execute(()->userService.saveBatch(subList));
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


    }



    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        ConfigurableApplicationContext context = SpringApplication.run(Usercenter2Application.class, args);
        insertUsers insertUsers = context.getBean(insertUsers.class);
        insertUsers.doInsertUsers();
        long end = System.currentTimeMillis();
        System.out.println("运行时间：" + (end - start) + " ms");
    }

}
