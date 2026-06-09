# 代码审查记录 - 2026-05-28

审查范围：tester/backend-frontend-fusion 分支全部未提交变更（94个文件）
审查时间：2026-05-28

## 一、变更概览

| 类别 | 文件数 | 说明 |
|------|--------|------|
| FastExcel 迁移 | ~80 | cn.idev.excel → org.apache.fesod.sheet |
| BPM 审批场景 | ~15 | 新增审批场景、快照、运行时服务 |
| 前端登录重构 | 6 | Login.vue 及子组件重构 |
| 双账套服务 | 1 | ErpFinanceDualLedgerResultServiceImpl |
| 依赖变更 | 2 | pom.xml 依赖版本更新 |

## 二、主要变更内容

1. **FastExcel 迁移**：从 `cn.idev.excel:fastexcel:1.3.0` 迁移到 `org.apache.fesod:fesod-sheet:2.0.1-incubating`
2. **BPM 审批场景平台**：新增审批场景（Scene）、审批方案版本（SchemeVersion）、审批规则（Rule）、运行时快照（Snapshot）完整链路
3. **前端登录页重构**：Login.vue 全面重写，新增暗黑模式、品牌动画区、浮动卡片
4. **双账套服务**：Excel 导出相关 import 同步迁移

---

## 三、问题记录

### P1 - 需要关注

#### 3.1 FastExcel 使用 incubating 版本
- **文件**：`yudao-dependencies/pom.xml`
- **描述**：`fesod-sheet` 版本为 `2.0.1-incubating`，incubating 版本表示该库尚在孵化阶段，API 和稳定性未经正式发布验证。
- **风险**：生产环境使用孵化版本可能遇到未发现的 bug 或 API 变更。
- **建议**：确认是否有正式发布版本可用；如必须使用 incubating 版本，需做好回归测试覆盖。

#### 3.2 `BpmApprovalRuntimeServiceImpl.submit()` 中 `getSceneByCode` 已抛异常，后续 null 检查为死代码
- **文件**：`BpmApprovalRuntimeServiceImpl.java`（第 80-83 行）
- **描述**：`approvalSceneService.getSceneByCode(sceneCode)` 在场景不存在时已直接抛出 `APPROVAL_SCENE_NOT_EXISTS` 异常，第 81-83 行的 `if (scene == null)` 检查永远不会为 true。
- **风险**：无功能风险，但死代码会误导后续维护者。
- **建议**：移除第 81-83 行的 null 检查，或改为在 `getSceneByCode` 中返回 null 而非抛异常（需统一风格）。

#### 3.3 `BpmApprovalInstanceSnapshotDO.processJson` 类型设计不一致
- **文件**：`BpmApprovalInstanceSnapshotDO.java`（第 76-77 行）/ `BpmApprovalRuntimeServiceImpl.java`（第 128 行）
- **描述**：`processJson` 字段类型为 `Map<String, Object>`，但在 `submit()` 中被设置为 `Map.of("json", hitRule.getProcessJson())`，即把一个 JSON 字符串包装在 Map 中。`hitRule.getProcessJson()` 本身可能就是 JSON 字符串。
- **风险**：序列化/反序列化时可能出现预期外的嵌套结构。如果 `processJson` 本意是存储流程定义 JSON，直接存字符串或反序列化为 POJO 更清晰。
- **建议**：明确 `processJson` 的语义——是存原始 JSON 字符串还是已解析的 Map。如果是字符串，字段类型应改为 `String`；如果是 Map，应在写入时做 JSON 解析。

#### 3.4 `BpmApprovalRuntimeServiceImpl.cancel()` 中结果处理器异常被静默吞没
- **文件**：`BpmApprovalRuntimeServiceImpl.java`（第 183-190 行）
- **描述**：`cancel()` 方法在调用 `handler.onCancel()` 时 catch Exception 后仅 log.error，不抛出。这是防御性设计（审批撤回不应因处理器异常而失败），但如果处理器负责回滚业务状态（如恢复单据审批状态），静默失败可能导致业务数据不一致。
- **风险**：如果 `onCancel` 负责关键业务状态回滚，异常被吞掉会导致单据卡在"审批中"状态。
- **建议**：确认 `onCancel` 的职责。如果涉及关键状态回滚，应抛出异常让事务回滚；如果仅做通知类操作，当前设计可接受，但建议在日志中增加更多上下文（如 snapshotId）。

#### 3.5 `BpmApprovalSceneDO` 缺少 `activeSchemeId` 的外键约束或校验
- **文件**：`BpmApprovalSceneDO.java`（第 57 行）
- **描述**：`activeSchemeId` 字段直接存储方案 ID，但没有外键约束或应用层校验确保该 ID 对应的方案确实存在且属于当前场景。
- **风险**：如果方案被删除但场景的 `activeSchemeId` 未清理，`submit()` 时会查不到生效版本而报错。
- **建议**：在方案删除时检查是否有关联场景引用，或在 `submit()` 中增加更友好的错误提示。

#### 3.6 前端登录页大量硬编码颜色值
- **文件**：`Login.vue`、`LoginForm.vue`、`LoginFormTitle.vue`、`ForgetPasswordForm.vue`、`SSOLogin.vue`
- **描述**：登录页重构后大量使用硬编码十六进制颜色值（如 `#ff5500`、`#15213b`、`#7f8da8`、`#dfe6f2`、`#e9edf4` 等），未使用 CSS 变量或 Tailwind 语义类。
- **风险**：与项目 AGENTS.md 中"颜色系统优先使用 `slate`"、"主色默认使用 `#1677FF`"的要求不一致。硬编码颜色不利于主题切换和维护。
- **建议**：将颜色替换为 CSS 变量或 Tailwind 类名。主色应统一为 `blue-600`（`#2563eb`）或项目约定的 `#1677FF`，而非自定义的 `#ff5500`（橙色）。

#### 3.7 登录页主色使用橙色而非项目标准蓝色
- **文件**：`Login.vue`、`LoginForm.vue`
- **描述**：登录页按钮、高亮、焦点状态等使用 `#ff5500`（橙色）作为主色，与 AGENTS.md 中"ERP 页面主基调默认采用经典科技蓝：主品牌色优先 `blue-600`"的要求冲突。
- **风险**：登录页与系统内其他页面视觉风格不一致。
- **建议**：确认是否为设计意图（登录页特殊处理）。如非设计意图，应统一为蓝色系。

### P2 - 可接受但值得注意

#### 3.8 `BpmApprovalSceneMapper.selectBySceneCode` 使用 `LIMIT 1` 硬编码
- **文件**：`BpmApprovalSceneMapper.java`（第 17 行）
- **描述**：使用 `.last("LIMIT 1")` 限制查询结果。这是 MyBatis Plus 中常见的做法，但 `.last()` 会将 SQL 片段直接拼接到最终 SQL 中。
- **风险**：低。`sceneCode` 应该是唯一的，LIMIT 1 是合理的防御性措施。
- **建议**：可接受。如需更严谨，可在 `sceneCode` 字段上添加唯一索引。

#### 3.9 `BpmApprovalInstanceSnapshotDO.bizId` 使用 String 类型
- **文件**：`BpmApprovalInstanceSnapshotDO.java`（第 40 行）
- **描述**：`bizId` 字段类型为 `String`，但在 `BpmApprovalRuntimeServiceImpl.submit()` 中传入的是 `Long bizId`，通过 `String.valueOf(bizId)` 转换。
- **风险**：低。使用 String 可以兼容不同业务系统的 ID 类型（Long、String 等），设计上是合理的。
- **建议**：可接受。但需确保查询时类型一致，避免隐式类型转换问题。

#### 3.10 `BpmApprovalRuntimeServiceImpl` 中 `contextProviderMap` 和 `resultHandlerMap` 非线程安全
- **文件**：`BpmApprovalRuntimeServiceImpl.java`（第 57-58 行）
- **描述**：`contextProviderMap` 和 `resultHandlerMap` 使用普通 `HashMap`，通过 `@Resource` setter 方法在 Spring 初始化时填充。
- **风险**：极低。Spring 单例 bean 初始化完成后，这两个 Map 不会再被修改，实际是不可变的。
- **建议**：可接受。如需更严谨，可使用 `Collections.unmodifiableMap()` 包装。

#### 3.11 前端 `useLoginCopy.ts` 新增文件未见完整内容
- **文件**：`yudao-ui/yudao-ui-admin-vue3/src/views/Login/components/useLoginCopy.ts`
- **描述**：新增的 composable 文件，从 LoginForm.vue 中引入了 `loginText` 函数。
- **风险**：未读取完整内容，无法确认是否有问题。
- **建议**：后续补充审查此文件。

### P3 - 无问题

#### 3.12 FastExcel 注解迁移一致性
- **描述**：所有 RespVO 类的 `@ExcelProperty`、`@ExcelIgnoreUnannotated` 等注解已统一从 `cn.idev.excel.annotation` 迁移到 `org.apache.fesod.sheet.annotation`，迁移完整。
- **状态**：✅ 无问题

#### 3.13 ExcelUtils 工具类迁移
- **描述**：`ExcelUtils.java` 中的 `FastExcel`、`LongStringConverter` 等 import 已正确迁移到 `org.apache.fesod.sheet` 包。
- **状态**：✅ 无问题

#### 3.14 BPM 审批场景错误码定义
- **描述**：新增的错误码（1-009-016-000 到 1-009-018-003）编号连续、命名清晰、消息模板合理。
- **状态**：✅ 无问题

---

## 四、总结

| 级别 | 数量 | 说明 |
|------|------|------|
| P1 | 7 | 需要关注：incubating 版本、死代码、类型设计、颜色硬编码等 |
| P2 | 4 | 可接受但值得注意 |
| P3 | 4 | 无问题 |

**核心风险点**：
1. `fesod-sheet:2.0.1-incubating` 是孵化版本，生产稳定性待验证
2. 登录页颜色体系与项目规范不一致（橙色 vs 蓝色）
3. BPM 审批运行时服务存在死代码和类型设计不一致
