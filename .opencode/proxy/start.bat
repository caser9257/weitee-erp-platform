@echo off
chcp 65001 >nul
cd /d "D:\ruoyi-vue-pro\.tester\proxy"
echo.
echo ========================================
echo   MiMo Proxy Server
echo ========================================
echo.
echo Starting proxy server on port 3000...
echo.
echo IMPORTANT: After starting, you need to:
echo   1. Change tester's API Base URL to:
echo      http://127.0.0.1:3000/v1
echo   2. Keep the same API Key
echo.
echo Press Ctrl+C to stop the proxy.
echo.
node index.js
