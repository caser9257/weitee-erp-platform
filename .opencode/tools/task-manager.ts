import { tool } from "@tester-ai/plugin"
import * as fs from "fs"
import * as path from "path"

const TASKS_DIR = ".tester/tasks"

export default tool({
  description: "管理任务持久化状态 — 创建、读取、更新、列出任务",
  args: {
    op: tool.schema
      .enum(["create", "get", "update", "list"])
      .describe("操作类型: create=创建任务, get=读取任务, update=更新状态, list=列出任务"),
    id: tool.schema
      .string()
      .optional()
      .describe("任务 ID（kebab-case），create/get/update 时必填"),
    title: tool.schema
      .string()
      .optional()
      .describe("任务标题，create 时必填"),
    status: tool.schema
      .enum(["pending", "in_progress", "completed", "blocked", "cancelled"])
      .optional()
      .describe("新状态，update 时使用"),
    phase: tool.schema
      .string()
      .optional()
      .describe("当前阶段名称"),
    gate: tool.schema
      .string()
      .optional()
      .describe("HARD_STOP 门控描述，设置后 agent 需用户确认才能继续"),
    description: tool.schema
      .string()
      .optional()
      .describe("任务描述"),
    files: tool.schema
      .array(tool.schema.string())
      .optional()
      .describe("受影响的文件列表"),
  },
  async execute(args, context) {
    const worktree = context.worktree || process.cwd()
    const tasksDir = path.join(worktree, TASKS_DIR)

    switch (args.op) {
      case "create": {
        if (!args.id || !args.title) {
          return "错误：create 操作需要 id 和 title 参数"
        }
        const taskDir = path.join(tasksDir, args.id)
        fs.mkdirSync(taskDir, { recursive: true })
        const task = {
          id: args.id,
          title: args.title,
          status: "pending",
          phase: args.phase || null,
          gate: args.gate || null,
          description: args.description || "",
          files: args.files || [],
          created_at: new Date().toISOString(),
          updated_at: new Date().toISOString(),
        }
        fs.writeFileSync(path.join(taskDir, "task.json"), JSON.stringify(task, null, 2))
        return JSON.stringify(task, null, 2)
      }

      case "get": {
        if (!args.id) {
          return "错误：get 操作需要 id 参数"
        }
        const taskPath = path.join(tasksDir, args.id, "task.json")
        if (!fs.existsSync(taskPath)) {
          return `任务 ${args.id} 不存在`
        }
        return fs.readFileSync(taskPath, "utf-8")
      }

      case "update": {
        if (!args.id) {
          return "错误：update 操作需要 id 参数"
        }
        const taskPath = path.join(tasksDir, args.id, "task.json")
        if (!fs.existsSync(taskPath)) {
          return `任务 ${args.id} 不存在`
        }
        const task = JSON.parse(fs.readFileSync(taskPath, "utf-8"))
        if (args.status) task.status = args.status
        if (args.phase) task.phase = args.phase
        if (args.gate !== undefined) task.gate = args.gate
        task.updated_at = new Date().toISOString()
        fs.writeFileSync(taskPath, JSON.stringify(task, null, 2))
        return JSON.stringify(task, null, 2)
      }

      case "list": {
        if (!fs.existsSync(tasksDir)) {
          return "[]"
        }
        const dirs = fs.readdirSync(tasksDir, { withFileTypes: true })
          .filter(d => d.isDirectory())
        const tasks = dirs.map(d => {
          const taskPath = path.join(tasksDir, d.name, "task.json")
          if (fs.existsSync(taskPath)) {
            try {
              return JSON.parse(fs.readFileSync(taskPath, "utf-8"))
            } catch {
              return { id: d.name, status: "error" }
            }
          }
          return { id: d.name, status: "unknown" }
        })
        return JSON.stringify(tasks, null, 2)
      }

      default:
        return `未知操作: ${args.op}`
    }
  },
})
