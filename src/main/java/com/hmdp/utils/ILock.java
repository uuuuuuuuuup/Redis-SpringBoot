package com.hmdp.utils;

/**
 * @auther xx
 * @data 2022/5/26
 */
public interface ILock {

  /**
   * 尝试获取锁
   * @param timeoutSec 锁持有的超市时间，过期后自动释放
   * @return true 代表获取锁成功，false代表获取锁失败
   */
  boolean tryLock(long timeoutSec);

  /**
   * 释放锁
   */
  void unlock();

}
