# 微泰ERP管理后台

基于 Vue3 + TypeScript + Vite + Element Plus 构建的企业级 ERP 管理后台。

## 环境要求

- Node.js >= 16.18.0
- pnpm >= 8.6.0

## 快速开始

```bash
# 安装依赖
pnpm install

# 启动开发服务器
pnpm dev

# 构建生产版本
pnpm build:prod
```

## 技术栈

| 框架 | 说明 | 版本 |
|------|------|------|
| Vue | Vue 框架 | 3.3.8 |
| Vite | 开发与构建工具 | 4.5.0 |
| Element Plus | UI 组件库 | 2.4.2 |
| TypeScript | JavaScript 超集 | 5.2.2 |
| Pinia | Vue 状态管理 | 2.1.7 |
| VueUse | 常用工具集 | 10.6.1 |
| Vue Router | Vue 路由 | 4.2.5 |
| UnoCSS | 原子 CSS | 0.57.4 |

## 开发工具

推荐使用 VS Code 开发，配合以下插件：

| 插件 | 功能 |
|------|------|
| Vue - Official | Vue 与 TypeScript 支持 |
| UnoCSS | UnoCSS 智能提示 |
| Iconify IntelliSense | 图标预览和搜索 |
| i18n Ally | 国际化智能提示 |
| ESLint | 代码检查 |
| Prettier | 代码格式化 |

## 项目结构

```
src/
├── api/          # API 接口定义
├── components/   # 公共组件
├── hooks/        # 组合式函数
├── layout/       # 布局组件
├── router/       # 路由配置
├── store/        # 状态管理
├── styles/       # 全局样式
├── utils/        # 工具函数
└── views/        # 页面视图
```
