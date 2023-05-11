#!/bin/bash

# 如果
#   命令输出内容包含第一个参数
#   或者重试次数小于第二个参数
# 则进行重试，否则结束重试

index=1
retry_result=$1
fail_retry_count=$2
command=""
temp_output_file_name="try_temp_output"
for arg in $*
do
  if [ $index -gt 2 ]; then
    command=$command$arg" "
  fi
  let index++
done
command=$command"> "$temp_output_file_name
echo "$command"

index=0
`touch $temp_output_file_name`
while true; do
  let index++
  echo "try count:$index"

  eval "$command"
  result=`cat $temp_output_file_name`
  echo "$result"
#  if [[ $result != *$retry_result* || $index -ge $fail_retry_count ]]; then
  if [[ $index -ge $fail_retry_count ]]; then
    break
  fi
done
`rm $temp_output_file_name`
