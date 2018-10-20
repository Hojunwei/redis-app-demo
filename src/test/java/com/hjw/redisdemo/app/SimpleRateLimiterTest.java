package com.hjw.redisdemo.app;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by ho on 2018/10/21.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SimpleRateLimiterTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void test(){
        SimpleRateLimiter limiter = new SimpleRateLimiter(redisTemplate);
        for(int i=0;i<20;i++) {
            System.out.println(limiter.isActionAllowed("laoqian", "reply", 60, 5));
        }
    }

}
