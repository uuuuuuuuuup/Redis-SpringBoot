package com.hmdp.utils;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.BooleanUtil;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * @auther xx
 * @data 2022/5/26
 */
public class SimoleRedisLock implements ILock{

  private String name;

  private StringRedisTemplate stringRedisTemplate;

  private static final String KEY_PREFIX = "lock:";

  private static final String THREAD_ID = UUID.randomUUID().toString()+"-";

  private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

  static {
    UNLOCK_SCRIPT = new DefaultRedisScript<>();
    UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
    UNLOCK_SCRIPT.setResultType(Long.class);
  }

  public SimoleRedisLock(){

  }

  public SimoleRedisLock(String name,
      StringRedisTemplate stringRedisTemplate) {
    this.name = name;
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Override
  public boolean tryLock(long timeoutSec) {
    String threadId = THREAD_ID+Thread.currentThread().getId();
    Boolean isLock = stringRedisTemplate.opsForValue()
        .setIfAbsent(KEY_PREFIX + name, threadId, timeoutSec, TimeUnit.SECONDS);
    return Boolean.TRUE.equals(isLock);
  }

  @Override
  public void unlock() {
    stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(KEY_PREFIX + name),THREAD_ID+Thread.currentThread().getId());
  }
/*  @Override
  public void unlock() {
    //获取线程标识
    String threadId = THREAD_ID+Thread.currentThread().getId();
    //获取锁中标识
    String currectId = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
    //判断是否一致
    if (currectId.equals(threadId)){
      stringRedisTemplate.delete(KEY_PREFIX + name);
    }
  }*/
}
