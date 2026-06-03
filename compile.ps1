# 编译脚本 - PowerShell版本
Write-Host "正在编译 LLM Chat 应用程序..." -ForegroundColor Green

# 创建输出目录
if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }

# 编译
javac -encoding UTF-8 -d bin $javaFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "编译成功！" -ForegroundColor Green
    Write-Host "运行程序请执行: .\run.ps1" -ForegroundColor Yellow
} else {
    Write-Host "编译失败！" -ForegroundColor Red
}
