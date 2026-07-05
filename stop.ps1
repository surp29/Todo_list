function Stop-Port {
    param([int]$Port)

    $conns = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if (-not $conns) {
        Write-Host "Khong co tien trinh nao dang chay tren cong $Port" -ForegroundColor Yellow
        return
    }

    $conns | ForEach-Object {
        try {
            Stop-Process -Id $_.OwningProcess -Force -ErrorAction Stop
            Write-Host "Da dung tien trinh tren cong $Port (PID $($_.OwningProcess))" -ForegroundColor Green
        } catch {
            Write-Host "Khong the dung PID $($_.OwningProcess): $_" -ForegroundColor Red
        }
    }
}

Stop-Port -Port 8080
Stop-Port -Port 5173
