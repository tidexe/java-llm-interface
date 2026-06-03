@echo off
chcp 65001 >nul
echo 正在编译 LLM Chat 应用程序...

:: 创建输出目录
if not exist "bin" mkdir bin

:: 编译所有Java文件
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt

if %errorlevel% equ 0 (
    echo 编译成功！
    echo 运行程序请执行: run.bat
) else (
    echo 编译失败！
)

del sources.txt
pause
