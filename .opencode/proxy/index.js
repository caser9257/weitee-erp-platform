import { readFileSync, writeFileSync, existsSync, mkdirSync } from "fs"
import { join, dirname } from "path"
import { fileURLToPath } from "url"
import http from "http"
import https from "https"

const __dirname = dirname(fileURLToPath(import.meta.url))

// ============================================================
// 配置
// ============================================================

const PROXY_PORT = 3000
const MIMO_BASE_URL = "https://token-plan-cn.xiaomimimo.com"
const USAGE_LOG_FILE = join(__dirname, "..", "token-tray", "config", "usage-log.json")

// 信用消耗率（根据小米文档）
const CREDIT_RATES = {
  "mimo-v2.5-pro": { input: 300, output: 600, cache_hit: 2.5 },
  "mimo-v2.5": { input: 100, output: 200, cache_hit: 2 },
  "mimo-v2-pro": { input: 700, output: 2100, cache_hit: 140 },
  "mimo-v2-omni": { input: 280, output: 1400, cache_hit: 56 },
}

// ============================================================
// 工具函数
// ============================================================

function ensureDir(dir) {
  if (!existsSync(dir)) mkdirSync(dir, { recursive: true })
}

function loadUsageLog() {
  if (!existsSync(USAGE_LOG_FILE)) return []
  try {
    return JSON.parse(readFileSync(USAGE_LOG_FILE, "utf-8"))
  } catch {
    return []
  }
}

function saveUsageLog(records) {
  ensureDir(dirname(USAGE_LOG_FILE))
  const trimmed = records.slice(-1000)
  writeFileSync(USAGE_LOG_FILE, JSON.stringify(trimmed, null, 2))
}

function calculateCredits(model, promptTokens, completionTokens) {
  const rates = CREDIT_RATES[model] || CREDIT_RATES["mimo-v2.5"]
  return promptTokens * rates.input + completionTokens * rates.output
}

function log(msg) {
  console.log(`[Proxy] ${msg}`)
}

// ============================================================
// 代理服务器
// ============================================================

const server = http.createServer(async (req, res) => {
  try {
    // 构建目标 URL
    const targetUrl = `${MIMO_BASE_URL}${req.url}`

    log(`→ ${req.method} ${req.url}`)

    // 收集请求体
    let body = []
    req.on("data", chunk => body.push(chunk))
    req.on("end", async () => {
      const requestBody = Buffer.concat(body)

      // 转发请求到 MiMo API
      const url = new URL(targetUrl)
      const options = {
        hostname: url.hostname,
        port: url.port || 443,
        path: url.pathname + url.search,
        method: req.method,
        headers: {
          ...req.headers,
          host: url.hostname,
        },
      }

      const proxyReq = https.request(options, (proxyRes) => {
        // 收集响应体
        let responseBody = []
        proxyRes.on("data", chunk => responseBody.push(chunk))
        proxyRes.on("end", () => {
          const responseBuffer = Buffer.concat(responseBody)
          const responseStr = responseBuffer.toString("utf-8")

          // 尝试解析响应，提取 usage 数据
          try {
            // 处理流式响应（SSE）
            if (responseStr.includes("data: ")) {
              // 流式响应，逐行解析
              const lines = responseStr.split("\n")
              for (const line of lines) {
                if (line.startsWith("data: ") && line !== "data: [DONE]") {
                  try {
                    const data = JSON.parse(line.slice(6))
                    if (data.usage) {
                      recordUsage(data.model || "unknown", data.usage)
                    }
                  } catch {}
                }
              }
            } else {
              // 非流式响应
              const data = JSON.parse(responseStr)
              if (data.usage) {
                recordUsage(data.model || "unknown", data.usage)
              }
            }
          } catch {}

          // 将响应转发给 tester
          res.writeHead(proxyRes.statusCode, proxyRes.headers)
          res.end(responseBuffer)
        })
      })

      proxyReq.on("error", (err) => {
        log(`Error: ${err.message}`)
        res.writeHead(500)
        res.end(`Proxy Error: ${err.message}`)
      })

      // 发送请求体
      if (requestBody.length > 0) {
        proxyReq.write(requestBody)
      }
      proxyReq.end()
    })
  } catch (err) {
    log(`Error: ${err.message}`)
    res.writeHead(500)
    res.end(`Proxy Error: ${err.message}`)
  }
})

function recordUsage(model, usage) {
  const promptTokens = usage.prompt_tokens || 0
  const completionTokens = usage.completion_tokens || 0
  const totalTokens = usage.total_tokens || promptTokens + completionTokens
  const credits = calculateCredits(model, promptTokens, completionTokens)

  const record = {
    timestamp: new Date().toISOString(),
    model: model,
    prompt_tokens: promptTokens,
    completion_tokens: completionTokens,
    total_tokens: totalTokens,
    credits: credits,
  }

  const logs = loadUsageLog()
  logs.push(record)
  saveUsageLog(logs)

  const totalCredits = logs.reduce((sum, r) => sum + (r.credits || 0), 0)
  log(`✓ ${model}: +${credits} credits (${promptTokens}+${completionTokens} tokens) | Total: ${totalCredits}`)
}

// ============================================================
// 启动服务器
// ============================================================

server.listen(PROXY_PORT, "127.0.0.1", () => {
  log(`Proxy server started on http://127.0.0.1:${PROXY_PORT}`)
  log(`Forwarding to: ${MIMO_BASE_URL}`)
  log(`Usage log: ${USAGE_LOG_FILE}`)
  log("")
  log("请将 tester 的 API Base URL 改为:")
  log(`  http://127.0.0.1:${PROXY_PORT}/v1`)
  log("")
})
