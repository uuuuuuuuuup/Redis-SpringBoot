package com.hmdp.config;

import com.hmdp.utils.LoginInterceptor;
import com.hmdp.utils.ResreshTokenInterceptor;
import javax.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @auther xx
 * @data 2022/5/23
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new ResreshTokenInterceptor(stringRedisTemplate)).addPathPatterns("/**").order(0);
    registry.addInterceptor(new LoginInterceptor())
              .excludePathPatterns("/user/code","/user/login","/blog/hot"
                  ,"/shop/**","/shot-type/**","/upload/**","/voucher/seckill").order(1);
  }
}
