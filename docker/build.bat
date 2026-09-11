@echo off
setlocal
cd /d "%~dp0"
echo ==> Building JTrac Docker image from context: ..
docker build -f Dockerfile -t jtrac:latest -t jtrac:2.3.3-2.1.0-beta ..
if %ERRORLEVEL% equ 0 (
    echo ==> Build complete! Images: jtrac:latest, jtrac:2.3.3-2.1.0-beta
) else (
    echo ==> Build failed with error code %ERRORLEVEL%
    exit /b %ERRORLEVEL%
)
