# 微泰ERP系统

<p align="center">
 <img src="https://img.shields.io/badge/Spring%20Boot-3.2-blue.svg" alt="Spring Boot">
 <img src="https://img.shields.io/badge/Vue-3.2-blue.svg" alt="Vue">
 <img src="https://img.shields.io/badge/JDK-17+-green.svg" alt="JDK">
 <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="License">
</p>

## 项目简介

微泰ERP系统是一套面向中小制造企业的企业资源计划管理系统，涵盖采购、销售、库存、财务、生产等核心业务模块，帮助企业实现业务流程数字化和精细化管理。

## 技术栈

### 后端
- **框架**：Spring Boot 3.2 + Spring Security
- **ORM**：MyBatis Plus
- **数据库**：MySQL 8.0
- **缓存**：Redis + Redisson
- **工作流**：Flowable
- **消息队列**：支持 Redis / RabbitMQ / Kafka / RocketMQ

### 前端
- **框架**：Vue 3 + TypeScript + Vite
- **UI 组件**：Element Plus
- **状态管理**：Pinia
- **图表**：ECharts

## 项目结构

```
├── yudao-server              # 主服务启动模块
├── yudao-framework           # 框架核心模块
├── yudao-module-system       # 系统管理模块（用户、角色、菜单、字典等）
├── yudao-module-infra        # 基础设施模块（文件、代码生成、定时任务等）
├── yudao-module-bpm          # 工作流模块
├── yudao-module-erp          # ERP 核心业务模块
├── yudao-module-crm          # CRM 客户关系管理模块
├── yudao-module-report       # 报表模块
├── yudao-module-project      # 项目管理模块
└── yudao-ui                  # 前端项目
    └── yudao-ui-admin-vue3   # Vue3 管理后台
```

## 核心功能

### ERP 模块
- **采购管理**：采购订单、采购入库、采购退货、供应商管理
- **销售管理**：销售订单、销售出库、销售退货、客户管理
- **库存管理**：库存查询、库存调拨、库存盘点、库存预警
- **财务管理**：收付款管理、应收应付、费用报销、财务凭证
- **生产管理**：生产工单、BOM 管理、MRP 运算、生产建议

### 系统管理
- 用户管理、角色管理、菜单管理、部门管理
- 字典管理、配置管理、通知公告
- 操作日志、登录日志、错误码管理

### 基础设施
- 文件管理、代码生成器
- 定时任务、API 日志
- 数据权限、系统监控

## 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Node.js 16+ & pnpm

### 后端启动
1. 创建数据库 `weitee_erp`，导入 `sql/mysql/` 下的 SQL 文件
2. 修改 `application-local.yaml` 中的数据库和 Redis 配置
3. 运行 `YudaoServerApplication` 主启动类

### 前端启动
```bash
cd yudao-ui/yudao-ui-admin-vue3
pnpm install
pnpm dev
```

### Docker 部署
```bash
cd script/docker
docker-compose up -d
```

## 文档

- 启动文档：参见项目内 `docs/` 目录
- API 文档：启动后访问 `http://localhost:48080/doc.html`

## 许可证

本项目基于 MIT 协议开源，详见 [LICENSE](./LICENSE) 文件。
