$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$pidFile = Join-Path $projectRoot 'data\shared-backend\backend.pid'

if (-not (Test-Path $pidFile)) {
    Write-Host 'No recorded shared-backend process is running.'
    exit 0
}

$backendPid = [int](Get-Content -Raw $pidFile)
$process = Get-Process -Id $backendPid -ErrorAction SilentlyContinue
if ($process) {
    Stop-Process -Id $backendPid
    $process.WaitForExit(10000)
    Write-Host "Shared backend stopped; database file was preserved (PID $backendPid)."
} else {
    Write-Host 'Shared backend had already stopped; database file was preserved.'
}
Remove-Item -LiteralPath $pidFile -Force
