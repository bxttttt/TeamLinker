package com.bxt.usercenter2.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxt.usercenter2.mapper.userMapper;
import com.bxt.usercenter2.model.domain.user;
import com.bxt.usercenter2.service.userService;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CachePreheatTask {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private userService userService;

    @Autowired
    private userMapper userMapper; // 假设你用 MyBatis-Plus
    @Autowired
    private RedissonClient redissonClient;

    @Scheduled(cron = "0 40 21 * * ?") // 每天21:40执行
    public void preloadAllUserRecommendCache() throws InterruptedException {
        // 获取 Redisson 的分布式锁
        RLock lock = redissonClient.getLock("bxt:lock:preloadUserRecommend");
        System.out.println("当前 Redis 地址: " +
                redissonClient.getConfig().useSingleServer().getAddress());

        boolean isLockAcquired = false;
        try {
            // 尝试获取锁，最多等待3秒，成功后10分钟后自动释放锁（防止死锁）
            isLockAcquired = lock.tryLock(3, 10, TimeUnit.MINUTES);

            if (isLockAcquired) {
                System.out.println("成功获取分布式锁，开始预热用户推荐缓存");

                List<user> userList = userMapper.selectList(new QueryWrapper<user>()
                        .ge("create_time", LocalDate.now().minusDays(7))
                );

                int successCount = 0;
                for (user user : userList) {
                    try {
                        Page<user> pagedResult = userService.recommendUsersPagedResultThroughUser(user, 1, 10);
                        String redisKey = String.format("bxt:user:recommend:%d", user.getId());
                        redisTemplate.opsForValue().set(redisKey, pagedResult, 1, TimeUnit.HOURS);
                        successCount++;
                    } catch (Exception e) {
                        System.err.println("预热用户推荐缓存失败 userId=" + user.getId());
                        e.printStackTrace();
                    }
                }

                System.out.printf("共预热用户缓存 %d 条%n", successCount);

            } else {
                System.out.println("未获取到分布式锁，跳过预热任务");
            }
        } catch (InterruptedException e) {
            System.err.println("获取锁时发生中断异常");
            e.printStackTrace();
        } finally {
//            Thread.sleep(10000);
            if (isLockAcquired && lock.isHeldByCurrentThread()) {
                lock.unlock(); // 确保只有当前线程释放锁
                System.out.println("释放分布式锁成功");
            }
        }
    }

}

