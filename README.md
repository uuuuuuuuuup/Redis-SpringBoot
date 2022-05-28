# Redis-SpringBoot
基于Redis、SpringBoot的分布式餐饮点评系统
## 完成模块
1. 登录模块：Redis解决Session共享问题
2. 缓存模块：为查询的店铺，店铺分类列表添加缓存。
3. 防止缓存穿透模块：采用空value方法解决缓存击穿，TTL为两分钟
4. 防止缓存击穿模块：采用逻辑过期方法，防止热点Key小时导致服务器负载过大问题，本质其实是延迟更新
5. 秒杀模块：秒杀优惠券功能的基本实现
6. 基于乐观锁解决超卖现象
7. 一人一单限制功能：解决集群模式的线程安全问题，采用Redisson的分布式锁（采用可重入锁）
8. 秒杀优化：基于Redis的一人一单限制，基于Redis实现秒杀减库存，基于阻塞队列的异步下单
9. 秒杀异步优化：基于消息队列的异步下单
10. 基于Stream的消息队列