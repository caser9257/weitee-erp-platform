import fs from 'node:fs'
import path from 'node:path'
import { spawnSync } from 'node:child_process'
import { fileURLToPath } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const scriptDir = path.dirname(__filename)
const workspaceDir = path.resolve(scriptDir, '..')
const relevantExtensions = ['.ts', '.tsx', '.vue', '.d.ts']

const runCommand = (command, args, options = {}) => {
  const result = spawnSync(command, args, {
    cwd: options.cwd ?? workspaceDir,
    encoding: 'utf8',
    stdio: options.stdio ?? 'pipe'
  })

  if (result.error) {
    throw result.error
  }
  if (result.status !== 0) {
    const errorText = [result.stdout, result.stderr].filter(Boolean).join('\n').trim()
    throw new Error(errorText || `${command} ${args.join(' ')} failed with exit code ${result.status}`)
  }
  return result.stdout ?? ''
}

const escapePowerShellArgument = (value) => `'${String(value).replace(/'/g, "''")}'`

const runGitCommand = (args) => {
  if (process.platform !== 'win32') {
    return runCommand('git', args)
  }
  // 本机为 PowerShell 7（pwsh.exe），无旧版 powershell.exe（v5.1）；
  // 优先使用 pwsh.exe，不存在时回退 powershell.exe，避免 spawn 时 ENOENT
  const hasPwsh = spawnSync('where.exe', ['pwsh.exe'], { encoding: 'utf8' }).status === 0
  const shell = hasPwsh ? 'pwsh.exe' : 'powershell.exe'
  const gitCommand = `& git ${args.map(escapePowerShellArgument).join(' ')}`
  return runCommand(shell, ['-NoProfile', '-ExecutionPolicy', 'Bypass', '-Command', gitCommand])
}

const repoRoot = runGitCommand(['rev-parse', '--show-toplevel']).trim()
const workspaceRelativeRoot = path.relative(repoRoot, workspaceDir).split(path.sep).join('/')

const isRelevantFile = (filePath) => {
  if (filePath.endsWith('.d.ts')) {
    return true
  }
  return relevantExtensions.includes(path.extname(filePath))
}

const toPosixRelativePath = (absolutePath) => {
  const relativePath = path.relative(workspaceDir, absolutePath)
  return relativePath.split(path.sep).join('/')
}

const collectFilesFromDirectory = (directoryPath) => {
  const collected = []
  for (const entry of fs.readdirSync(directoryPath, { withFileTypes: true })) {
    const fullPath = path.join(directoryPath, entry.name)
    if (entry.isDirectory()) {
      if (entry.name === 'node_modules' || entry.name === 'dist' || entry.name === 'target') {
        continue
      }
      collected.push(...collectFilesFromDirectory(fullPath))
      continue
    }
    if (entry.isFile() && isRelevantFile(fullPath)) {
      collected.push(fullPath)
    }
  }
  return collected
}

const resolveExplicitTargets = (targets) => {
  const resolved = []
  for (const target of targets) {
    const absoluteTarget = path.resolve(workspaceDir, target)
    const relativeTarget = path.relative(workspaceDir, absoluteTarget)
    if (relativeTarget.startsWith('..') || path.isAbsolute(relativeTarget)) {
      throw new Error(`Target is outside workspace: ${target}`)
    }
    if (!fs.existsSync(absoluteTarget)) {
      throw new Error(`Target does not exist: ${target}`)
    }
    const stat = fs.statSync(absoluteTarget)
    if (stat.isDirectory()) {
      resolved.push(...collectFilesFromDirectory(absoluteTarget))
      continue
    }
    if (stat.isFile() && isRelevantFile(absoluteTarget)) {
      resolved.push(absoluteTarget)
    }
  }
  return resolved
}

const readGitLines = (args) => {
  const output = runGitCommand(args)
  return output
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)
}

const getChangedFiles = () => {
  const trackedUnstaged = readGitLines(['diff', '--name-only', '--diff-filter=ACMR', '--', '.'])
  const trackedStaged = readGitLines(['diff', '--cached', '--name-only', '--diff-filter=ACMR', '--', '.'])
  const untracked = readGitLines(['ls-files', '--others', '--exclude-standard', '--', '.'])
  return [...trackedUnstaged, ...trackedStaged, ...untracked]
    .map((relativePath) => path.resolve(repoRoot, relativePath))
    .filter((absolutePath) => fs.existsSync(absolutePath))
    .filter((absolutePath) => {
      const relativePath = path.relative(workspaceDir, absolutePath)
      return !relativePath.startsWith('..') && !path.isAbsolute(relativePath)
    })
    .filter(isRelevantFile)
}

const getUniqueIncludes = (files) => {
  const includeSet = new Set([
    'types/**/*.d.ts',
    'src/types/auto-imports.d.ts',
    'src/types/auto-components.d.ts'
  ])
  for (const filePath of files) {
    includeSet.add(toPosixRelativePath(filePath))
  }
  return Array.from(includeSet)
}

const getTempConfigPath = () =>
  path.join(workspaceDir, 'node_modules', '.cache', 'tscheck', '.tsconfig.tscheck.changed.json')

const writeTempTsconfig = (includes) => {
  const tempConfigPath = getTempConfigPath()
  fs.mkdirSync(path.dirname(tempConfigPath), { recursive: true })
  const tempConfig = {
    extends: './tsconfig.json',
    include: includes
  }
  fs.writeFileSync(tempConfigPath, `${JSON.stringify(tempConfig, null, 2)}\n`, 'utf8')
  return tempConfigPath
}

const cleanupTempConfig = (tempConfigPath) => {
  try {
    fs.rmSync(tempConfigPath, { force: true })
  } catch (error) {
    if (error && typeof error === 'object' && 'code' in error && error.code === 'EPERM') {
      return
    }
    const reason = error instanceof Error ? error.message : String(error)
    console.warn(`[tscheck-changed] Failed to delete temp config: ${reason}`)
  }
}

const runVueTsc = (tempConfigPath) => {
  const args = [
    '--max_old_space_size=4096',
    path.join(workspaceDir, 'node_modules', 'vue-tsc', 'bin', 'vue-tsc.js'),
    '--noEmit',
    '-p',
    tempConfigPath
  ]
  const result = spawnSync(process.execPath, args, {
    cwd: workspaceDir,
    stdio: 'inherit',
    encoding: 'utf8'
  })
  if (result.error) {
    throw result.error
  }
  return result.status ?? 1
}

const main = () => {
  const explicitTargets = process.argv.slice(2)
  const files =
    explicitTargets.length > 0 ? resolveExplicitTargets(explicitTargets) : getChangedFiles()
  const uniqueFiles = Array.from(new Set(files.map((filePath) => path.normalize(filePath)))).sort()

  if (uniqueFiles.length === 0) {
    console.log(`[tscheck-changed] No changed TypeScript/Vue files under ${workspaceRelativeRoot}.`)
    return
  }

  console.log('[tscheck-changed] Checking files:')
  for (const filePath of uniqueFiles) {
    console.log(`- ${toPosixRelativePath(filePath)}`)
  }

  const includes = getUniqueIncludes(uniqueFiles)
  const tempConfigPath = writeTempTsconfig(includes)
  let exitCode = 1
  try {
    exitCode = runVueTsc(tempConfigPath)
  } finally {
    cleanupTempConfig(tempConfigPath)
  }
  process.exit(exitCode)
}

main()
