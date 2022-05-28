-- 确定参数列表
-- 优惠券id
local voucherId = ARGV[1]
-- 用户id
local userId = ARGV[2]
-- 订单id
local orderId = ARGV[3]
-- 数据库key
-- 优惠券库存
local stockKey = 'seckill:stock:' .. voucherId
-- 订单key
local orderKey = 'seckill:order:' .. voucherId
-- 判断优惠券的值是否小于1
if(tonumber(redis.call('get',stockKey)) < 1) then
    -- 库存不足
    return 1
end
-- 判断用户是否下单
if(redis.call('sismember',orderKey,userId)==1) then
    -- 存在（已经下单）
    return 2
end
-- 不存在（没下过单）
-- 扣库存 incrby stockKey -1
redis.call('incrby',stockKey,-1)
-- 保存用户 sadd orderKey userId
redis.call('sadd',orderKey,userId)
-- 向stream队列发送消息，XADD stream.orders * k1 v1 k2 v2 .. ..
redis.call('xadd','stream.orders','*','userId',userId,'voucherId',voucherId,'id',orderId)
return 0