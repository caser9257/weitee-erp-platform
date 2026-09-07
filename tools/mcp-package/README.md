# MCP 配置迁移包

## 1. 包含内容

- `spring-ai/application-mcp-template.yaml`
  - 从仓库内 `yudao-server/src/main/resources/application.yaml` 提取并脱敏后的 Spring AI MCP 配置模板。
- `continue/new-config.yaml`
  - 从仓库内 `.continue/agents/new-config.yaml` 提取并整理后的 Continue MCP 客户端示例。
- `scripts/start-yudao-mcp-server.ps1`
  - 在其他终端中启动 Yudao Spring Boot 服务的 PowerShell 示例脚本。

## 2. 已确认的仓库内事实

- 后端存在 Spring AI MCP 配置段：
  - `yudao-server/src/main/resources/application.yaml`
- AI 模块声明了 MCP 依赖：
  - `yudao-module-ai/pom.xml`
  - `spring-ai-starter-mcp-server-webmvc`
  - `spring-ai-starter-mcp-client`
- 当前默认聚合构建并未启用 AI 模块：
  - 根目录 `pom.xml` 中 `yudao-module-ai` 被注释
  - `yudao-server/pom.xml` 中 `yudao-module-ai` 依赖被注释
- Continue 目录下只有一个示例 MCP 配置：
  - `.continue/agents/new-config.yaml`
- 仓库内 `.codex/config.toml` 只有 `sandbox_mode = "danger-full-access"`，没有可迁移的 MCP server 注册清单。

## 3. 这份包能做什么

- 让你在其他终端快速复用当前仓库里“已经落盘”的 MCP 相关配置结构。
- 作为 Spring AI MCP Server / Client 的启动模板和 Continue 客户端配置起点。

## 4. 这份包不能直接保证什么

- 不能保证“开箱即跑”。
  - 原因 1：当前默认 `pom.xml` 没把 `yudao-module-ai` 编进主工程。
  - 原因 2：仓库中的 MCP 配置默认是 `enabled: false`。
  - 原因 3：原始 `application.yaml` 里含有敏感密钥，本迁移包已全部脱敏，没有直接带出。
- 不能导出 Codex 桌面客户端当前会话里的 MCP 注册状态。
  - 原因：这些并不在本仓库落盘，仓库内没有对应配置文件。

## 5. 如何在其他终端使用

### Spring AI MCP Server

1. 把本目录复制到目标机器。
2. 将 `spring-ai/application-mcp-template.yaml` 中的占位符改成真实值。
3. 确认目标工程已经启用 `yudao-module-ai`：
   - 根 `pom.xml` 取消注释 `yudao-module-ai`
   - `yudao-server/pom.xml` 取消注释 `yudao-module-ai` 依赖
4. 将模板内容合并到目标环境的 `application.yaml` 或通过外部配置覆盖。
5. 在项目根目录执行：

```powershell
.\tools\mcp-package\scripts\start-yudao-mcp-server.ps1
```

6. 默认服务主端口来自仓库配置：`48080`
7. 默认 MCP SSE Endpoint 来自仓库配置：`/sse`
8. 因此完整地址通常是：

```text
http://127.0.0.1:48080/sse
```

## 6. Continue 客户端使用

将 `continue/new-config.yaml` 合并到 Continue 的实际配置文件，并按实际情况替换：

- `YOUR_OPENAI_API_KEY_HERE`
- `http://127.0.0.1:48080/sse`

## 7. 验证建议

- 启动服务后先访问 SSE 地址，确认服务已监听。
- 再让 Continue 或其他 MCP Client 指向该 SSE 地址。
- 如果连接失败，优先检查：
  - AI 模块是否真的已参与构建
  - `application.yaml` 中 `spring.ai.mcp.server.enabled` 是否为 `true`
  - 端口是否仍为 `48080`
  - 目标终端是否能访问该地址

## 8. 来源文件

- [application.yaml](/D:/ruoyi-vue-pro/yudao-server/src/main/resources/application.yaml)
- [yudao-module-ai/pom.xml](/D:/ruoyi-vue-pro/yudao-module-ai/pom.xml)
- [AiAutoConfiguration.java](/D:/ruoyi-vue-pro/yudao-module-ai/src/main/java/cn/iocoder/yudao/module/ai/framework/ai/config/AiAutoConfiguration.java)
- [new-config.yaml](/D:/ruoyi-vue-pro/.continue/agents/new-config.yaml)
- [config.toml](/D:/ruoyi-vue-pro/.codex/config.toml)
