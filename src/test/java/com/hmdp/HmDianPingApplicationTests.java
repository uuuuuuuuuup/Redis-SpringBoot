package com.hmdp;

import cn.hutool.json.JSONUtil;
import com.hmdp.entity.Voucher;
import com.hmdp.mapper.VoucherMapper;
import com.hmdp.utils.RedisIdWorker;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class HmDianPingApplicationTests {


  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Resource
  private VoucherMapper voucherMapper;

  @Autowired
  private RedisIdWorker redisIdWorker;

  private ExecutorService es = Executors.newFixedThreadPool(500);

  @Test
  void testKill(){
    List<Voucher> vouchers = voucherMapper.queryVoucherOfShop(1L);
    Voucher voucher = vouchers.get(0);
    stringRedisTemplate.opsForValue().set("kill", JSONUtil.toJsonStr(voucher));
  }

  @Test
  void testMethod(){
    System.out.println("hello");
    stringRedisTemplate.opsForValue().increment("icr:" + "order" + ":" + "2022:5:25");
  }
}
