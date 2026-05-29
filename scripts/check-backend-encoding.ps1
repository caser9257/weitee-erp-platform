$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$utf8Strict = New-Object System.Text.UTF8Encoding($false, $true)

$targets = @(
  (Join-Path $repoRoot 'pom.xml'),
  (Join-Path $repoRoot 'lombok.config'),
  (Join-Path $repoRoot 'sql'),
  (Join-Path $repoRoot 'yudao-dependencies'),
  (Join-Path $repoRoot 'yudao-framework'),
  (Join-Path $repoRoot 'yudao-server'),
  (Join-Path $repoRoot 'yudao-module-ai'),
  (Join-Path $repoRoot 'yudao-module-bpm'),
  (Join-Path $repoRoot 'yudao-module-crm'),
  (Join-Path $repoRoot 'yudao-module-erp'),
  (Join-Path $repoRoot 'yudao-module-infra'),
  (Join-Path $repoRoot 'yudao-module-iot'),
  (Join-Path $repoRoot 'yudao-module-member'),
  (Join-Path $repoRoot 'yudao-module-pay'),
  (Join-Path $repoRoot 'yudao-module-project'),
  (Join-Path $repoRoot 'yudao-module-report'),
  (Join-Path $repoRoot 'yudao-module-system')
)

$includeExtensions = @('.java', '.xml', '.yml', '.yaml', '.properties', '.sql', '.config')

function Get-TargetFiles {
  param (
    [string[]]$Paths
  )

  $files = New-Object System.Collections.Generic.List[string]
  foreach ($path in $Paths) {
    if (Test-Path $path -PathType Leaf) {
      $files.Add((Resolve-Path $path).Path)
      continue
    }
    if (Test-Path $path -PathType Container) {
      Get-ChildItem -Path $path -Recurse -File -ErrorAction SilentlyContinue |
        Where-Object {
          $includeExtensions -contains $_.Extension -or
          $_.Name -eq 'pom.xml' -or
          $_.Name -eq '.flattened-pom.xml'
        } |
        ForEach-Object { $files.Add($_.FullName) }
    }
  }
  return $files | Sort-Object -Unique
}

function Get-EncodingState {
  param (
    [byte[]]$Bytes
  )

  if ($Bytes.Length -ge 3 -and $Bytes[0] -eq 0xEF -and $Bytes[1] -eq 0xBB -and $Bytes[2] -eq 0xBF) {
    return 'UTF-8-BOM'
  }
  if ($Bytes.Length -ge 2 -and $Bytes[0] -eq 0xFF -and $Bytes[1] -eq 0xFE) {
    return 'UTF-16-LE'
  }
  if ($Bytes.Length -ge 2 -and $Bytes[0] -eq 0xFE -and $Bytes[1] -eq 0xFF) {
    return 'UTF-16-BE'
  }
  if ($Bytes.Length -ge 4 -and $Bytes[0] -eq 0xFF -and $Bytes[1] -eq 0xFE -and $Bytes[2] -eq 0x00 -and $Bytes[3] -eq 0x00) {
    return 'UTF-32-LE'
  }
  if ($Bytes.Length -ge 4 -and $Bytes[0] -eq 0x00 -and $Bytes[1] -eq 0x00 -and $Bytes[2] -eq 0xFE -and $Bytes[3] -eq 0xFF) {
    return 'UTF-32-BE'
  }

  try {
    [void]$utf8Strict.GetString($Bytes)
    return 'UTF-8'
  } catch {
    return 'Non-UTF8-NoBOM'
  }
}

$invalidFiles = foreach ($file in Get-TargetFiles -Paths $targets) {
  $bytes = [System.IO.File]::ReadAllBytes($file)
  $encoding = Get-EncodingState -Bytes $bytes
  if ($encoding -ne 'UTF-8') {
    [PSCustomObject]@{
      Encoding = $encoding
      Path = $file
    }
  }
}

if ($invalidFiles) {
  Write-Host 'Detected backend encoding violations:' -ForegroundColor Red
  $invalidFiles | Sort-Object Encoding, Path | Format-Table -AutoSize
  exit 1
}

Write-Host 'Backend encoding check passed: all governed files are UTF-8 without BOM.' -ForegroundColor Green
