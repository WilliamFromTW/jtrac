@echo off
setlocal
echo ==> Starting JTrac container on http://localhost:8888
docker run -d -p 8888:8080 -v jtrac_data:/jtrac-data --name jtrac jtrac:latest
if %ERRORLEVEL% equ 0 (
    echo ==> JTrac is running! Access it at: http://localhost:8888
) else (
    echo ==> Failed to start JTrac container.
    exit /b %ERRORLEVEL%
)
