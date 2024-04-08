@echo off
 
:Main
cls
set num=0
set str=
set /p str=请输入一串测试字符：
if not defined str goto end
set "str=%str:"=%"
if defined str call :count
 
:end
echo 字符串去掉所有的双引号后，长度为：%num%
pause
goto Main
 
:count
set /a num+=1
set "str=%str:~1%"
if defined str goto count
goto :eof