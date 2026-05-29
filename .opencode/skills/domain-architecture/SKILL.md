---
name: domain-architecture
description: 架构领域知识索引 — 微服务、CQRS、DDD、缓存策略、消息队列、设计模式
license: MIT
compatibility: tester
metadata:
  category: domain-knowledge
  topics: microservices,cqrs,ddd,rest,graphql,caching,message-queue,design-patterns
---

## 架构最佳实践速查

### 分层架构（本项目默认）
- Controller → Service → Mapper / DAO
- Controller 负责参数校验和路由
- Service 负责业务逻辑和事务
- Mapper / DAO 负责数据访问

### 事务管理
- `@Transactional(rollbackFor = Exception.class)` 在 Service 方法上
- 注意事务失效场景：同类内部调用、异步方法、try-catch 吞掉异常
- 跨数据源事务：使用 Seata / Atomikos

### 缓存策略
- Redis 缓存：Redisson 客户端
- 缓存穿透：布隆过滤器或缓存空值
- 缓存雪崩：随机过期时间 + 互斥锁
- 缓存一致性：先更新 DB 再删除缓存（Cache Aside Pattern）

### 消息队列
- 内置支持：Event（同进程）、Redis Stream（集群）、RocketMQ / Kafka（生产）
- 使用消息队列解耦：订单创建 → 库存扣减 → 积分发放

### 设计模式（本项目常用）
- 模板方法模式：AbstractController / AbstractService
- 策略模式：多种登录方式、多种支付渠道
- 工厂模式：BeanFactory 管理策略实例
- 观察者模式：Spring Event / 消息队列
