package com.hmdp.utils;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @auther xx
 * @data 2022/5/23
 */
public class ResreshTokenInterceptor implements HandlerInterceptor {

  private StringRedisTemplate stringRedisTemplate;

  public ResreshTokenInterceptor(){

  }

  public ResreshTokenInterceptor(StringRedisTemplate stringRedisTemplate){
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    //TODO 1. 获取请求体的token
    String token = request.getHeader("authorization");
    if (StrUtil.isBlank(token)) {
      return  true;
    }
    //TODO 2.基于token获取redis用户
    String tokenKey = LOGIN_USER_KEY + token;
    Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(tokenKey);
    //判断用户是否存在
    if(userMap.isEmpty()){
      return true;
    }
    //TODO 将查询的Hash数据转为UserDTO对象
    UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
    //TODO 存在，保存用户信息到ThreadLocal
    UserHolder.saveUser(userDTO);
    //TODO 刷新token有效期
    stringRedisTemplate.expire(tokenKey,30, TimeUnit.DAYS); //为了方便测试改成了30天 实际 30min
    return true;
  }


  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) throws Exception {
    UserHolder.removeUser();
  }
}
