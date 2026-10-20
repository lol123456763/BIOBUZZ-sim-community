@echo off
powershell.exe -NoProfile -STA -ExecutionPolicy Bypass -File "%~dp0Import-Code.ps1" %*
pause
