@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build.ps1"
if errorlevel 1 (
  echo.
  echo Build failed. Read the error above and see README.md.
  pause
  exit /b 1
)
echo.
echo Build completed. Check the build\libs folder.
pause
