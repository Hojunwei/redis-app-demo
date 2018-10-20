package com.hjw.redisdemo.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.UUID;

/**
 * redis 实现延时队列
 * Created by ho on 2018/10/20.
 */
public class RedisDelayingQueue<T> {

    static class TaskItem<T>{
        public String id;
        public T msg;
    }

    // fastjson 序列化对象中存在 generic 类型时，需要使用 TypeReference
    private Type TaskType = new TypeReference<TaskItem<T>>(){}.getType();

    private StringRedisTemplate redisTemplate;
    private String queueKey;

    public RedisDelayingQueue(StringRedisTemplate redisTemplate, String queueKey) {
        this.redisTemplate = redisTemplate;
        this.queueKey = queueKey;
    }

    public void delay(T msg){
        TaskItem<T> task = new TaskItem<T>();
        task.id = UUID.randomUUID().toString(); // 分配唯一的 uuid
        task.msg = msg;
        String s = JSON.toJSONString(task); // fastjson 序列化
        redisTemplate.opsForZSet().add(queueKey, s, System.currentTimeMillis() + 5000);
    }

    public void loop() {
        while (!Thread.interrupted()) {
            // 只取一条
            Set<String> values =  redisTemplate.opsForZSet().rangeByScore(queueKey, 0, System.currentTimeMillis(), 0, 1);
            if (values.isEmpty()) {
                try {
                    Thread.sleep(500); // 歇会继续
                } catch (InterruptedException e) {
                    break;
                }
                continue;
            }
            String s = values.iterator().next();
            if (redisTemplate.opsForZSet().remove(queueKey, s) > 0) { // 抢到了
                TaskItem<T> task = JSON.parseObject(s, TaskType); // fastjson 反序列化
                this.handleMsg(task.msg);
            }
        }
    }

    public void handleMsg(T msg) {
        System.out.println(msg);
    }

}
