@echo off
 
:Main
cls
set num=0
set str=
set /p str=������һ�������ַ���
@REM ������ǿ�ֵ��set "str=%str:"=%" ִ��֮��str��ֵ��Ϊ"=�������ǿ�ֵ�����������
if not defined str goto end

@REM ȥ���ַ��������п��ܴ��ڵ�˫����
@REM ԭ��Ϊ��set str=%str1:str2=str3%������Ϊ���� str1 �е��ַ���str2�滻Ϊstr3�������滻��Ľ������str
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