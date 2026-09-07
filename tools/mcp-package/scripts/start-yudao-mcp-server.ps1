$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\\..\\..")
Set-Location $root

Write-Host "Workspace: $root"
Write-Host "Starting Yudao server with MCP-related config..."
Write-Host "Precondition 1: root pom.xml has yudao-module-ai enabled"
Write-Host "Precondition 2: yudao-server/pom.xml has yudao-module-ai dependency enabled"
Write-Host "Precondition 3: application config enables spring.ai.mcp.server"

mvn -pl yudao-server -am spring-boot:run
