package com.hjw.redisdemo.app;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * redis实现简单限流.
 * zset 集合中只有 score 值非常重要， value 值没有特别的意义，只需要保证它是唯一的就可以了。
 * 几个连续的 Redis 操作都是针对同一个 key 的，使用 pipeline 可以显著提升 Redis 存取效率
 * Created by ho on 2018/10/21.
 */
public class SimpleRateLimiter {

    private StringRedisTemplate redisTemplate;

    public SimpleRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isActionAllowed(String userId, String actionKey, int period, int maxCount) {

        String key = String.format("hist:%s:%s", userId, actionKey);
        long nowTs = System.currentTimeMillis();

        //记录行为
        redisTemplate.opsForZSet().add(key, nowTs+"", nowTs);
        //移除时间窗口之前的行为记录，剩下的都是时间窗口内的
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, nowTs - period * 1000);
        //获取窗口内的行为数量
        Long count = redisTemplate.opsForZSet().zCard(key);
        //设置 zset 过期时间，避免冷用户持续占用内存
        //过期时间应该等于时间窗口的长度，再多宽限 1s
        redisTemplate.expire(key, period+1, TimeUnit.SECONDS);

        //比较数量是否超标
        return count <= maxCount;

    }

}
