package com.bxt.usercenter2.controller;

import com.bxt.usercenter2.job.CachePreheatTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private CachePreheatTask cachePreheatTask;

    @GetMapping("/preload")
    public String manuallyTriggerPreload() throws InterruptedException {
        cachePreheatTask.preloadAllUserRecommendCache();
        return "手动触发预热任务成功";
    }
}
