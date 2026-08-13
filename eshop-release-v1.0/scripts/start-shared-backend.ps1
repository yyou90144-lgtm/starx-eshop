param([switch]$SkipUsb)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$previewRoot = Join-Path $projectRoot 'backend\run-preview'
$envFile = Join-Path $projectRoot '.env'
$databaseDir = Join-Path $projectRoot 'data\shared-backend'
$databaseBase = Join-Path $databaseDir 'eshop'
$databaseFile = "$databaseBase.mv.db"
$schemaFile = Join-Path $previewRoot 'schema-test.sql'
$seedFile = Join-Path $previewRoot 'data-seed.sql'
$staticDir = Join-Path $projectRoot 'backend\src\main\resources\static'
$logFile = Join-Path $databaseDir 'backend.log'
$errorLogFile = Join-Path $databaseDir 'backend-error.log'
$pidFile = Join-Path $databaseDir 'backend.pid'
$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'

foreach ($requiredPath in @($previewRoot, $schemaFile, $seedFile, $staticDir)) {
    if (-not (Test-Path $requiredPath)) {
        throw "Missing shared-backend runtime file: $requiredPath"
    }
}
if (-not (Test-Path $envFile)) {
    throw "Missing shared-backend configuration file: $envFile"
}

$settings = @{}
Get-Content $envFile | ForEach-Object {
    if ($_ -match '^([^#=]+)=(.*)$') {
        $settings[$matches[1]] = $matches[2]
    }
}
foreach ($requiredSetting in @('JWT_SECRET', 'BOOTSTRAP_ADMIN_USERNAME', 'BOOTSTRAP_ADMIN_PASSWORD')) {
    if ([string]::IsNullOrWhiteSpace($settings[$requiredSetting])) {
        throw "Missing $requiredSetting in $envFile"
    }
}

New-Item -ItemType Directory -Path $databaseDir -Force | Out-Null

$existingListener = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($existingListener) {
    try {
        $health = Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/actuator/health' -TimeoutSec 3
    } catch {
        throw 'Port 8080 is occupied by a different service.'
    }
    if ($health.status -ne 'UP') {
        throw 'The service on port 8080 is not a healthy STARX backend.'
    }
    Write-Host 'Shared backend is already running at http://127.0.0.1:8080/api/'
} else {
    $isFirstRun = -not (Test-Path $databaseFile)
    $databaseUrl = "jdbc:h2:file:$($databaseBase.Replace('\', '/'));MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE"
    $arguments = @(
        '-cp', $previewRoot,
        'org.springframework.boot.loader.launch.JarLauncher',
        "--spring.datasource.url=$databaseUrl",
        '--spring.datasource.username=sa',
        '--spring.datasource.password=',
        '--spring.datasource.driver-class-name=org.h2.Driver',
        '--spring.flyway.enabled=false',
        "--spring.web.resources.static-locations=file:$($staticDir.Replace('\', '/'))/,classpath:/static/",
        "--jwt.secret=$($settings['JWT_SECRET'])",
        "--app.bootstrap-admin.enabled=$($settings['BOOTSTRAP_ADMIN_ENABLED'])",
        "--app.bootstrap-admin.username=$($settings['BOOTSTRAP_ADMIN_USERNAME'])",
        "--app.bootstrap-admin.password=$($settings['BOOTSTRAP_ADMIN_PASSWORD'])",
        "--app.bootstrap-admin.nickname=$($settings['BOOTSTRAP_ADMIN_NICKNAME'])",
        "--app.upload-dir=$((Join-Path $projectRoot 'data\uploads').Replace('\', '/'))"
    )
    if ($isFirstRun) {
        $arguments += @(
            '--spring.sql.init.mode=always',
            "--spring.sql.init.schema-locations=file:$($schemaFile.Replace('\', '/'))",
            "--spring.sql.init.data-locations=file:$($seedFile.Replace('\', '/'))"
        )
        Write-Host 'First run: creating the persistent shared database...'
    } else {
        $arguments += '--spring.sql.init.mode=never'
    }

    $process = Start-Process `
        -FilePath 'java' `
        -ArgumentList $arguments `
        -WorkingDirectory $projectRoot `
        -WindowStyle Hidden `
        -RedirectStandardOutput $logFile `
        -RedirectStandardError $errorLogFile `
        -PassThru
    Set-Content -LiteralPath $pidFile -Value $process.Id

    $healthy = $false
    foreach ($attempt in 1..45) {
        Start-Sleep -Seconds 1
        if ($process.HasExited) {
            $details = if (Test-Path $errorLogFile) { Get-Content -Raw $errorLogFile } else { '' }
            throw "Shared backend exited during startup.`n$details"
        }
        try {
            $health = Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/actuator/health' -TimeoutSec 3
            if ($health.status -eq 'UP') {
                $healthy = $true
                break
            }
        } catch {
            # Backend is still starting.
        }
    }
    if (-not $healthy) {
        throw 'Shared backend failed its health check.'
    }
    Write-Host 'Shared backend started at http://127.0.0.1:8080/api/'
}

if (-not $SkipUsb -and (Test-Path $adb)) {
    $device = & $adb get-state 2>$null
    if ($device -eq 'device') {
        & $adb reverse tcp:8080 tcp:8080 | Out-Null
        Write-Host 'USB device now shares the same backend: tcp:8080 -> tcp:8080'
    } else {
        Write-Host 'No authorized USB device found. Run: adb reverse tcp:8080 tcp:8080'
    }
}

Write-Host 'PC web: Vite /api -> 127.0.0.1:8080'
Write-Host "Persistent data shared by PC and Android: $databaseFile"
