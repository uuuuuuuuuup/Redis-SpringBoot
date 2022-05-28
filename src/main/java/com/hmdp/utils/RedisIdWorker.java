package com.hmdp.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @auther xx
 * @data 2022/5/25
 */
@Component
public class RedisIdWorker {

  //初始时间：2022年1月1日0：00的时间戳
  private static final long BEGIN_TIMESTAMP = 1640995200L;

  private static final int COUNT_BITS = 32;

  private StringRedisTemplate stringRedisTemplate;

  public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  public long nextId(String keyPrefix) {
/*    //1.生成时间戳
    LocalDateTime now = LocalDateTime.now();
    long currentSecnod = now.toEpochSecond(ZoneOffset.UTC);
    long timestamp = currentSecnod - BEGIN_TIMESTAMP;
    //2.生成序列号
    String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
    long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

    //3.拼接并返回
    return timestamp << 32 | count;*/

    // 1.生成时间戳
    LocalDateTime now = LocalDateTime.now();
    long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
    long timestamp = nowSecond - BEGIN_TIMESTAMP;

    // 2.生成序列号
    // 2.1.获取当前日期，精确到天
    String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
    // 2.2.自增长
    /*stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date)*/
    long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

    // 3.拼接并返回
    return timestamp << COUNT_BITS | count;
  }


}
