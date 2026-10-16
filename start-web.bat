@echo off
chcp 65001 >nul
title HongguoTV Web Player
echo ======================================================
echo           HongguoTV 网页版播放器正在启动
echo ======================================================
echo 正在启动本地服务并打开浏览器...
start "" "http://127.0.0.1:8787/"
echo.
echo 本地服务运行中，请勿关闭本窗口...
node server/index.mjs
pause
