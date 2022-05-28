package com.hmdp.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @auther xx
 * @data 2022/5/26
 */
@Configuration
public class RedisConfig {

  @Bean
  public RedissonClient redissonClient(){
    //配置类
    Config config = new Config();
    config.useSingleServer().setAddress("redis://192.168.230.130:6379");
    return Redisson.create(config);
  }

}
