package com.hmdp.service.impl;

import static com.hmdp.utils.RedisConstants.LOGIN_CODE_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import com.sun.org.apache.xerces.internal.impl.io.UCSReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Override
  public Result sendCode(String phone, HttpSession session) {
    //1.校验手机号
    if (RegexUtils.isPhoneInvalid(phone)) {
      //1.1不符合，返回错误信息
      return Result.fail("手机号合适错误");
    }
    //1.2 符合
    String code = RandomUtil.randomNumbers(6);
    //2.保存验证码到redis
    stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY+phone,code,2, TimeUnit.MINUTES);
    //3.返回验证码
    log.debug("发送验证码成功，验证码{}",code);
    return Result.ok();
  }

  @Override
  public Result login(LoginFormDTO loginForm, HttpSession session) {

    String phone = loginForm.getPhone();
    //1.校验手机号
    if (RegexUtils.isPhoneInvalid(phone)) {
      //1.1不符合，返回错误信息
      return Result.fail("手机号合适错误");
    }
    //2.校验验证码
    String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY+phone);
    String code = loginForm.getCode();
    if(cacheCode==null || !cacheCode.equals(code)){
      //3.不一致直接报错
      return Result.fail("验证码错误");
    }
    //4.一致根据手机号查询用户
    User user = query().eq("phone", phone).one();
    //5.判断用户是否存在
    if(user==null){
      //6.不存在，创建新用户并保存
      user = creaUserWithPhone(phone);
    }
    //TODO 7保存用户信息到redis中
    //TODO 7.1 随机生成token，作为登陆令牌
    String token = UUID.randomUUID().toString(true);
    //TODO 7.2 将User对象转为Hash存储
    UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
    Map<String, Object> userMap = BeanUtil.beanToMap(userDTO,new HashMap<>(),
        CopyOptions.create().setIgnoreNullValue(true)
            .setFieldValueEditor((filedname,filedValue)->filedValue.toString()));
    //TODO 7.3 存储用户信息
    String tokenKey = LOGIN_USER_KEY+token;
    stringRedisTemplate.opsForHash().putAll(tokenKey,userMap);
    stringRedisTemplate.expire(tokenKey,30,TimeUnit.MINUTES);
    return Result.ok(token);
  }

  private User creaUserWithPhone(String phone) {
    User user = new User();
    user.setPhone(phone);
    user.setNickName(USER_NICK_NAME_PREFIX +RandomUtil.randomString(10));
    save(user);
    return user;
  }
}
