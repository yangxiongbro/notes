# 基础





## 数据类型





## 模块结构

### 目录结构

```shell
photo_helper_app
--common
----read_config.go
--test.go

read_config.go

package files

import "fmt"

func ReadConfig(filePath string, v any) {
	....
}
```



### 公共模块声明

```shell
cd photo_helper_app/common
go mod init common
go mod tidy
```



### 引入公共模块

```
go mod init photo_helper_app
go mod tidy

vim go.mod

module photo_helper_app

go 1.23.4

require (
  common v0.0.0
)

replace (
  common v0.0.0 => ./common
)
```



### 使用公共模块

```shell
test.go
package main

import (
	"fmt"
	"common"
)

func main() {
	files.ReadConfig("", &config)
	fmt.Println("Hello, World!")
}

```





# 问题记录

```shell
# 跨包使用的函数和变量要首字母大写

stats_acr_xmp\stats_acr_xmp.go:4:2: missing go.sum entry for module providing package common (imported by stats_acr_xmp);
# 在stats_acr_xmp引入间接引入的包，后执行 go mod tidy
```

