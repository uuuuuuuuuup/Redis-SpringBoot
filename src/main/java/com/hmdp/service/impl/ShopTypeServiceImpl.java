package com.hmdp.service.impl;

import static com.hmdp.utils.RedisConstants.CACHE_NULL_TTL;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  @Override
  public Result queryShopList() {
    String listShop = "shop:list";
    //查询缓存
    List values = stringRedisTemplate.opsForHash().values(listShop);
    //缓存命中
    if(!values.isEmpty()){
      ArrayList<ShopType> shopTypes = new ArrayList<>();
      for(Object str:values){
        shopTypes.add(JSONUtil.toBean((String)str,ShopType.class));
      }
      return Result.ok(shopTypes);
    }
    //查询数据库
    List<ShopType> typeList =
        query().orderByAsc("sort").list();
    //数据库没有这条记录
    if (typeList==null) {
      return Result.fail("暂无店铺");
    }
    //找到数据写入缓存
    for(ShopType temp : typeList){
      stringRedisTemplate.opsForHash().put(listShop,temp.getId().toString(),JSONUtil.toJsonStr(temp));
    }
    //返回
    return Result.ok(typeList);
  }
}
