$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
$envFile = Join-Path $root ".env"

if (-not (Test-Path $envFile)) {
    Write-Host "Khong tim thay file .env. Hay copy .env.example thanh .env va dien thong tin (mat khau Postgres, ...)." -ForegroundColor Red
    exit 1
}

Write-Host "Doc cau hinh tu .env ..." -ForegroundColor Cyan
Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq "" -or $line.StartsWith("#")) { return }
    $parts = $line -split "=", 2
    if ($parts.Count -eq 2) {
        [System.Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), "Process")
    }
}

# Tìm Maven: ưu tiên bản cài trong PATH, nếu không có thì dùng bản cache sẵn trên máy
$mvnCmd = "mvn"
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    $cachedMvn = "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-3.9.6-bin\3311e1d4\apache-maven-3.9.6\bin\mvn.cmd"
    if (Test-Path $cachedMvn) {
        $mvnCmd = $cachedMvn
    } else {
        Write-Host "Khong tim thay Maven. Hay cai dat bang lenh: choco install maven -y" -ForegroundColor Red
        exit 1
    }
}

$backendPath = Join-Path $root "backend"
$frontendPath = Join-Path $root "frontend"

Write-Host "Dang khoi dong Backend (Spring Boot) tren cong 8080 ..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    "-NoExit", "-Command",
    "cd '$backendPath'; & '$mvnCmd' spring-boot:run"
) | Out-Null

Write-Host "Cho backend san sang (co the mat 20-30 giay lan dau) ..." -ForegroundColor Cyan
$backendReady = $false
for ($i = 0; $i -lt 60; $i++) {
    try {
        $resp = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/todos" -UseBasicParsing -TimeoutSec 2
        if ($resp.StatusCode -eq 200) { $backendReady = $true; break }
    } catch {}
    Start-Sleep -Seconds 2
}

if ($backendReady) {
    Write-Host "Backend da san sang: http://localhost:8080" -ForegroundColor Green
} else {
    Write-Host "Backend chua phan hoi sau 2 phut. Kiem tra lai cua so PowerShell cua backend de xem loi." -ForegroundColor Yellow
}

Write-Host "Dang khoi dong Frontend (Vite) tren cong 5173 ..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    "-NoExit", "-Command",
    "cd '$frontendPath'; npm run dev"
) | Out-Null

Start-Sleep -Seconds 3
Write-Host "Mo trinh duyet: http://localhost:5173" -ForegroundColor Green
Start-Process "http://localhost:5173"

Write-Host ""
Write-Host "Da mo 2 cua so PowerShell rieng cho Backend va Frontend." -ForegroundColor Cyan
Write-Host "De dung, chay: .\stop.ps1" -ForegroundColor Cyan
