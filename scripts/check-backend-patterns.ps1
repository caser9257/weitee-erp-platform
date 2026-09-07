param(
    [string]$Path = '.',
    [switch]$Fix = $false
)

$ErrorActionPreference = 'Stop'

$issues = New-Object System.Collections.Generic.List[object]
$excludePathPattern = '[\\/](target|test|node_modules|\.git|\.codex-runtime|\.worktrees|\.worktrees-archived|\.worktrees-push|output)[\\/]'

Write-Host 'Starting backend pattern checks...' -ForegroundColor Cyan

$javaFiles = Get-ChildItem -Path $Path -Filter '*.java' -Recurse -File -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -notmatch $excludePathPattern }

foreach ($file in $javaFiles) {
    $lines = Get-Content -LiteralPath $file.FullName

    $inTransaction = $false
    $transactionStartLine = 0
    $braceCount = 0
    $lineNum = 0

    foreach ($line in $lines) {
        $lineNum++

        if ($line -match '@Transactional') {
            $inTransaction = $true
            $transactionStartLine = $lineNum
            $braceCount = 0
        }

        if ($inTransaction -and $line -match '\{') {
            $braceCount += ([regex]::Matches($line, '\{')).Count
        }
        if ($inTransaction -and $line -match '\}') {
            $braceCount -= ([regex]::Matches($line, '\}')).Count
        }

        if ($inTransaction -and $braceCount -gt 0) {
            $externalCallPatterns = @(
                '\w+Api\.\w+',
                '\w+Feign\.\w+',
                '\w+Client\.\w+',
                'restTemplate\.',
                'webClient\.',
                'rabbitTemplate\.',
                'kafkaTemplate\.',
                'redisTemplate\.',
                'mailService\.',
                'smsService\.'
            )

            foreach ($pattern in $externalCallPatterns) {
                if ($line -match $pattern -and
                    $line -notmatch 'log\.' -and
                    $line -notmatch 'logger\.' -and
                    $line -notmatch 'System\.out' -and
                    $line -notmatch ('//.*' + $pattern)) {
                    $issues.Add([PSCustomObject]@{
                        File = $file.FullName
                        Line = $lineNum
                        Type = 'TRANSACTION_EXTERNAL_CALL'
                        Severity = 'CRITICAL'
                        Message = "External call inside transaction: $($line.Trim())"
                        Pattern = $pattern
                    })
                }
            }
        }

        if ($inTransaction -and $braceCount -le 0 -and $lineNum -gt $transactionStartLine) {
            $inTransaction = $false
        }
    }

    $inLoop = $false
    $loopStartLine = 0
    $loopBraceCount = 0
    $lineNum = 0

    foreach ($line in $lines) {
        $lineNum++

        if ($line -match 'for\s*\(' -or
            $line -match 'forEach\(' -or
            $line -match '\.stream\(\).*\.forEach\(') {
            $inLoop = $true
            $loopStartLine = $lineNum
            $loopBraceCount = 0
        }

        if ($inLoop -and $line -match '\{') {
            $loopBraceCount += ([regex]::Matches($line, '\{')).Count
        }
        if ($inLoop -and $line -match '\}') {
            $loopBraceCount -= ([regex]::Matches($line, '\}')).Count
        }

        if ($inLoop -and $loopBraceCount -gt 0) {
            $queryPatterns = @(
                'Mapper\.\w*select\w*',
                'Mapper\.\w*query\w*',
                'Mapper\.\w*get\w*',
                'Mapper\.\w*find\w*',
                'Mapper\.\w*list\w*',
                'Service\.\w*get\w*',
                'Service\.\w*query\w*',
                'Service\.\w*find\w*',
                'Service\.\w*list\w*',
                'Repository\.\w*find\w*',
                'Repository\.\w*get\w*'
            )

            foreach ($pattern in $queryPatterns) {
                if ($line -match $pattern -and
                    $line -notmatch 'Batch' -and
                    $line -notmatch 'List' -and
                    $line -notmatch 'Map' -and
                    $line -notmatch 'Ids' -and
                    $line -notmatch 'selectList' -and
                    $line -notmatch 'selectMap') {
                    $issues.Add([PSCustomObject]@{
                        File = $file.FullName
                        Line = $lineNum
                        Type = 'N_PLUS_ONE_QUERY'
                        Severity = 'CRITICAL'
                        Message = "Possible N+1 query inside loop: $($line.Trim())"
                        Pattern = $pattern
                    })
                }
            }
        }

        if ($inLoop -and $loopBraceCount -le 0 -and $lineNum -gt $loopStartLine) {
            $inLoop = $false
        }
    }
}

Write-Host ''
Write-Host 'Backend pattern check results' -ForegroundColor Cyan
Write-Host ('=' * 80)

if ($issues.Count -eq 0) {
    Write-Host 'No backend pattern issues found.' -ForegroundColor Green
} else {
    Write-Host "Found $($issues.Count) backend pattern issue(s)." -ForegroundColor Red
    foreach ($issue in $issues) {
        Write-Host "[$($issue.Type)] $($issue.File):$($issue.Line)" -ForegroundColor Yellow
        Write-Host "  $($issue.Message)" -ForegroundColor White
    }
}

exit $issues.Count
