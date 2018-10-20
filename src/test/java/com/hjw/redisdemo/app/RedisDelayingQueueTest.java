package com.hjw.redisdemo.app;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

/**
 * Created by ho on 2018/10/20.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisDelayingQueueTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void test(){
        RedisDelayingQueue<String> queue = new RedisDelayingQueue<>(redisTemplate, "q-demo");

        Thread producer = new Thread() {

            public void run() {
                for (int i = 0; i < 10; i++) {
                    queue.delay("codehole" + i);
                }
            }

        };
        Thread consumer = new Thread() {

            public void run() {
                queue.loop();
            }

        };
        producer.start();
        consumer.start();

        try {
            producer.join();
            Thread.sleep(6000);
            consumer.interrupt();
            consumer.join();
        } catch (InterruptedException e) {
        }
    }

}
