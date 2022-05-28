package com.hmdp.service.impl;

import static com.hmdp.utils.RedisConstants.CACHE_NULL_TTL;
import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TTL;
import static com.hmdp.utils.RedisConstants.LOCK_SHOP_KEY;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  @Override
  public Result queryById(Long id) {

    //缓存穿透
    //queryWithPassThrough(id);

    //互斥锁解决缓存击穿
    Shop shop = queryWithMutex(id);
    if (shop==null){
      return Result.fail("get failed");
    }
    //返回
    return Result.ok(shop);
  }

  public Shop queryWithMutex(Long id)  {
    String shopId = CACHE_SHOP_KEY+id;
    String info = stringRedisTemplate.opsForValue().get(shopId);
    if (StrUtil.isNotBlank(info)){
      //找到信息
      Shop shop = JSONUtil.toBean(info, Shop.class);
      return shop;
    }
    if(info!=null){
      return null;
    }
    //实现缓存重建
    //1.1 获取互斥锁
    //1.2 判断是否获取成功
    //1.3 如果失败，休眠并重试
    String lockId = LOCK_SHOP_KEY+id;
    Shop shop = null;
    try {
      boolean isLock = tryLock(lockId);
      if(!isLock){
        Thread.sleep(50);
        return queryWithMutex(id);
      }
      //未找到
      shop = getById(id);
      //2. 不存在，返回错误信息2
      if(shop==null){//既没有命中缓存，也没有命中数据库
        //防止缓存穿透
        stringRedisTemplate.opsForValue().set(shopId,"",CACHE_NULL_TTL,TimeUnit.MINUTES);
        return null;
      }
      //3. 写入redis
      stringRedisTemplate.opsForValue().set(shopId,JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL,TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      //4. 释放锁
      unLock(lockId);
    }
    return shop;
  }

  public Shop queryWithPassThrough(Long id) {
    String shopId = CACHE_SHOP_KEY+id;
    String info = stringRedisTemplate.opsForValue().get(shopId);
    if (StrUtil.isNotBlank(info)){
      //找到信息
      Shop shop = JSONUtil.toBean(info, Shop.class);
      return shop;
    }
    if(info!=null){
      return null;
    }
    //未找到
    Shop shop = getById(id);
    if(shop==null){
      //防止缓存穿透
      stringRedisTemplate.opsForValue().set(shopId,"",CACHE_NULL_TTL,TimeUnit.MINUTES);
      return null;
    }
    stringRedisTemplate.opsForValue().set(shopId,JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL,TimeUnit.MINUTES);
    return shop;
  }

  private boolean tryLock(String key){
    Boolean flag = stringRedisTemplate.opsForValue()
        .setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
    //防止自动拆箱发生异常导致判断错误
    return BooleanUtil.isTrue(flag);
  }

  private void unLock(String key){
    stringRedisTemplate.delete(key);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Result update(Shop shop) {
    Long id = shop.getId();
    if (id==null){
      return Result.fail("店铺id不能为空！");
    }
    //1. 更新数据库
    updateById(shop);
    //2. 删除缓存
    stringRedisTemplate.delete(CACHE_SHOP_KEY+id);
    return Result.ok("success");
  }
}
