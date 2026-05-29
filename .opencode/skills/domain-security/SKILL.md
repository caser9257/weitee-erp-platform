---
name: domain-security
description: 安全领域知识索引 — 认证、加密、XSS、SQL 注入、CORS、OAuth、JWT 等
license: MIT
compatibility: tester
metadata:
  category: domain-knowledge
  topics: authentication,authorization,encryption,xss,sql-injection,cors,oauth,jwt,csrf
---

## 安全最佳实践速查

### 认证（Authentication）
- Spring Security + JWT Token：使用 `jjwt` 库生成和验证 JWT
- Token 存储在 Redis：设置过期时间，支持刷新
- 密码存储：BCryptPasswordEncoder，不存明文
- 多端登录：不同端使用不同的 Token 前缀

### 授权（Authorization）
- 接口级别：`@PreAuthorize("@ss.hasPermission('xxx')")`
- 按钮级别：`v-hasPermi="'xxx'"`
- 数据权限：MyBatis Plus 拦截器 + DataPermission 注解

### 防注入
- SQL：MyBatis 用 `#{}` 而非 `${}`，Like 查询用 `concat('%',#{val},'%')`
- XSS：前端模板引擎默认转义，后端 `@XssFilter` 注解
- 命令执行：禁止直接拼接操作系统命令

### CSRF
- 使用自定义 Header（如 `Http-Subject`）+ Token 校验
- 前后端分离架构天然免疫 Cookie-based CSRF

### CORS
- Spring Boot 配置 `AllowedOriginPatterns`，不使用 `allowedOrigins("*")`
- 生产环境限定具体域名
