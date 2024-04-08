@REM 不显示本行命令行
@echo off
setlocal enabledelayedexpansion
echo hello world

:loop
@REM git fetch --all
@REM git reset --hard origin/develop
@REM git pull
git pull >auto_retry.output.txt 2>&1
for /f "delims=" %%i in (auto_retry.output.txt) do ( 
  set "content=%%i"
  echo !content!
  call str_length.bat !content!
  set "output_len=!ERRORLEVEL!"
  echo !output_len!
  if !content! gtr 4 (
    set content_start=!content:~0,5!
    if !content_start!==fatal (
      echo 重试
      goto loop
    )
  )
)

echo 结束重试