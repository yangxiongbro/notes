```shell
# 编译
javac -encoding UTF-8 RenameFile.java
# 运行
java -Xmx128m -Xms128m -Dfile.encoding=UTF-8 RenameFile
# 配置文件 config
# 配置文件格式
# 第一行：要重命名的文件目录，例如：C:\Users\rename\data
# 第二行：文件名称匹配模式
#	--s：字符串字串匹配模式
#	--r：正则表达式匹配模式
# 第三行：对匹配上的文件名字符子串处理模式
#	--d：删除匹配上的字符串
#	--r：使用配置文件第三行之后的替换字符串进行替换
# 第三行之后：匹配模式 --> 替换字符串

# 结果输出文件 result.txt
# 重命名结果以追加形式记录到文件中，文件不存在则自动创建
```



```shell
# 配置样例

# 删除子串匹配上的字符串
C:\Users\rename\data
--s
--d
abc

# 删除正则匹配上的字符串
C:\Users\rename\data
--s
--d
0{2}

# 替换子串匹配上的字符串
C:\Users\rename\data
--s
--d
abc --> 123

# 替换正则匹配上的字符串
C:\Users\rename\data
--s
--d
0{2} --> 99
```

