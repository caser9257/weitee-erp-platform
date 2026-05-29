---
name: domain-development
description: 开发领域知识索引 — TypeScript/Go/Java 最佳实践、编码规范、测试
license: MIT
compatibility: tester
metadata:
  category: domain-knowledge
  topics: typescript,go,java,spring-boot,testing,code-style
---

## 语言最佳实践速查

### Java / Spring Boot（本项目语言）
- JDK 8 / Spring Boot 2.7 (master 分支)
- JDK 17 / Spring Boot 3.2 (master-jdk17 分支)
- 使用 MapStruct 做 VO/DO/DTO 转换（而非 BeanUtils）
- 使用 Lombok 减少样板代码
- 使用 JUnit 5 + Mockito 做单元测试
- Controller 使用 `@Valid` / `@Validated` 做参数校验
- 统一异常处理：`@RestControllerAdvice` + `GlobalExceptionHandler`

### 编码规范
- 类名：PascalCase
- 方法/变量：camelCase
- 常量：UPPER_SNAKE_CASE
- 包名：全小写，com.company.module.xxx
- SQL：关键字大写，表名/字段小写蛇形

### 单元测试
- 使用 `@SpringBootTest` 做集成测试
- 使用 `@WebMvcTest` 做 Controller 层测试
- Service 层使用 Mockito mock DAO 层
- 测试类命名：`XxxServiceTest` / `XxxControllerTest`
