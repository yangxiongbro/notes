@echo off
 
:Main
cls
set num=0
set str=
set /p str=������һ�������ַ���
if not defined str goto end
set "str=%str:"=%"
if defined str call :count
 
:end
echo �ַ���ȥ�����е�˫���ź󣬳���Ϊ��%num%
pause
goto Main
 
:count
set /a num+=1
set "str=%str:~1%"
if defined str goto count
goto :eof