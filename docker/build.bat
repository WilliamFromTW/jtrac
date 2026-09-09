@echo off
setlocal
cd /d "%~dp0"
echo ==> Building JTrac Docker image from context: ..
docker build -f Dockerfile -t jtrac:latest ..
if %ERRORLEVEL% equ 0 (
    echo ==> Build complete! Image: jtrac:latest
) else (
    echo ==> Build failed with error code %ERRORLEVEL%
    exit /b %ERRORLEVEL%
)
