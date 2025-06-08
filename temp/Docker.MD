# Docker

## 安装

```shell
# 更新软件包
sudo apt update
sudo apt upgrade

# 安装依赖包
sudo apt install apt-transport-https ca-certificates curl software-properties-common

# 添加 Docker 官方 GPG 密钥
sudo curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

# 添加 Docker 仓库
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
# 更新
sudo apt update

# 确保你现在从 Docker 官方仓库安装 Docker 而不是 Debian 默认仓库
# 你应该看到它指向 https://download.docker.com/，确保这就是官方的 Docker 仓库
apt-cache policy docker-ce

# 安装 Docker
sudo apt install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 动 Docker 并设置为开机自启
sudo systemctl start docker
sudo systemctl enable docker

# 使用以下命令来验证 Docker 是否安装成功
sudo docker --version

# 卸载 docker
sudo apt purge docker-ce
sudo rm -rf /var/lib/docker
```



## Docker基本使用

### Image

```shell
# 查看本机所有镜像
docker images
# 删除本机镜像
docker rmi [IMAGE ID/REPOSITORY:TAG]
# 查找镜像
docker search [REPOSITORY]
# 拉取镜像
docker pull [REPOSITORY:TAG]
# 设置镜像标签
docker tag [IMAGE ID] [REPOSITORY]:[New Tag Name]
# 将指定镜像保存成 tar 归档文件
docker save [-o] [fineName] [IMAGE ID/REPOSITORY:TAG]
# 导入使用 save 命令导出的镜像
docker load [-i/-q] [fineName]
# --input , -i : 指定导入的文件，代替 STDIN
# --quiet , -q : 精简输出信息
# 将指定镜像保存成 tar 归档文件
docker export [IMAGE ID/REPOSITORY:TAG] > [fineName] 
# 导出镜像
docker import [-c/-m] file|URL| - [REPOSITORY[:TAG]]
#   -c, --change list       Apply Dockerfile instruction  the created image
#   -m, --message string	Set commit message for imported image
#       --platform string	Set platform if server is multi-platform capable

# docker save保存的是镜像，docker export保存的是容器
# docker save会保留镜像所有的历史记录，docker export不会，即没有commit历史
# docker load用来载入镜像包，docker import用来载入容器包，但两者都会恢复为镜像；
# docker load不能对载入的镜像重命名，而docker import可以为镜像指定新名称。

# 将本地的镜像上传到镜像仓库,要先登陆到镜像仓库
docker push [--disable-content-trust] NAME[:TAG]
# --disable-content-trust :忽略镜像的校验,默认开启
```

### Container

```shell
# 查看已启动容器
docker container ls
# 查看所有容器
docker ps -a
# 查看已停止容器
docker ps -a -q
# 启动一个容器
docker start [CONTAINER ID/CONTAINER NAME]
# 创建启动容器
docker run [-t/-i/-p/-P HostPort:ContainerPort/--name CONTAINER NAME -v HostDir:ContainerDir/--network NETNAME/--ip ipaddress --privileged] [IMAGE ID/REPOSITORY:TAG] [COMMOND]
# -t :终端/-i :交互式操作
# -d :后台运行容器，并返回容器ID
# -P :是容器内部端口随机映射到主机的高端口
# -p :是容器内部端口绑定到指定的主机端口
# --name :指定容器名字
# -v :目录映射 主机目录：容器目录
# --network :docker网络名称，详细请看Docker network
# --ip :指定ip地址
# --privileged :用该参数，container内的root拥有真正的root权限。否则，container内的root只是外部的一个普通用户权限。
# COMMOND 例：/bin/bash

# 停止容器
docker stop [CONTAINER ID/CONTAINER NAME]
# 删除容器
docker rm [-f/-l/-v] [CONTAINER ID/CONTAINER NAME]
#-f, --force=false:强项终止并删除一个运行中的容器。
#-l, --link=false:删除容器的连接，但保留容器。
#-v, --volumes=false:删除容器挂载的数据卷

# 查看容器启动日志
docker logs [CONTAINER ID/CONTAINER NAME]

```

## Docker 镜像加速

- 网易：**https://hub-mirror.c.163.com/**
- 阿里云：**https://<你的ID>.mirror.aliyuncs.com**
- 七牛云加速器：**https://reg-mirror.qiniu.com**

阿里云镜像获取地址：https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors

Ubuntu16.04+、Debian8+、CentOS7

对于使用 systemd 的系统，请在 /etc/docker/daemon.json 中写入如下内容（如果文件不存在请新建该文件）：

```
{"registry-mirrors":["https://reg-mirror.qiniu.com/"]}
```

之后重新启动服务：

```sh
sudo systemctl daemon-reload
sudo systemctl restart docker
```



## Dockerfile

https://www.cnblogs.com/panwenbin-logs/p/8007348.html

### 什么是dockerfile?

Dockerfile是一个包含用于组合映像的命令的文本文档。可以使用在命令行中调用任何命令。 Docker通过读取`Dockerfile`中的指令自动生成映像。

`docker build`命令用于从Dockerfile构建映像。可以在`docker build`命令中使用`-f`标志指向文件系统中任何位置的Dockerfile。

```shell
docker build -f /path/to/a/Dockerfile -t 镜像名:tag .
```

### Dockerfile的基本结构

Dockerfile 一般分为四部分：基础镜像信息、维护者信息、镜像操作指令和容器启动时执行指令，’#’ 为 Dockerfile 中的注释。

### Dockerfile文件说明

Docker以从上到下的顺序运行Dockerfile的指令。为了指定基本映像，第一条指令必须是*FROM*。

#### 常用的指令

**注意：Dockerfile 的指令每执行一次都会在 docker 上新建一层。所以过多无意义的层，会造成镜像膨胀过大。以 && 符号连接命令，这样执行后，只会创建 1 层镜像。**

##### FROM

**指定基础镜像，必须为第一个命令**

```dockerfile
FROM <image>
FROM <image>:<tag>
FROM <image>@<digest>
# tag或digest是可选的，如果不使用这两个值时，会使用latest版本的基础镜像
```

##### MAINTAINER

**维护者信息**

```dockerfile
MAINTAINER <name>
```

##### RUN

**构建镜像时执行的命令**

RUN用于在镜像容器中执行命令，其有以下两种命令执行方式：

```dockerfile
# shell执行
RUN <command>
# exec执行
RUN ["executable", "param1", "param2"]
# RUN指令创建的中间镜像会被缓存，并会在下次构建中使用。如果不想使用这些缓存镜像，可以在构建时指定--no-cache参数，如：docker build --no-cache
```

##### ADD

**将本地文件添加到容器中，tar类型文件会自动解压(网络压缩资源不会被解压)，可以访问网络资源，类似wget**

- ADD 的优点：在执行 <src> 为 tar 压缩文件的话，压缩格式为 gzip, bzip2 以及 xz 的情况下，会自动复制并解压到 <dest>。
- ADD 的缺点：在不解压的前提下，无法复制 tar 压缩文件。会令镜像构建缓存失效，从而可能会令镜像构建变得比较缓慢。具体是否使用，可以根据是否需要自动解压来决定。

```dockerfile
ADD [--chown=<user>:<group>] <src>... <dest>
ADD [--chown=<user>:<group>] ["<src>",... "<dest>"] # 用于支持包含空格的路径
# 示例：
ADD hom* /mydir/          # 添加所有以"hom"开头的文件
ADD hom?.txt /mydir/      # ? 替代一个单字符,例如："home.txt"
ADD test relativeDir/     # 添加 "test" 到 `WORKDIR`/relativeDir/
ADD test /absoluteDir/    # 添加 "test" 到 /absoluteDir/
```

##### COPY

**功能类似ADD，但是是不会自动解压文件，也不能访问网络资源**

##### CMD

**构建容器后调用，也就是在容器启动时才进行调用**

```dockerfile
CMD ["executable","param1","param2"] # (执行可执行文件，优先)
CMD ["param1","param2"] # (设置了ENTRYPOINT，则直接调用ENTRYPOINT添加参数)
CMD command param1 param2 # (执行shell内部命令)
# 示例：
CMD echo "This is a test." | wc -
CMD ["/usr/bin/wc","--help"]
# 注：CMD不同于RUN，CMD用于指定在容器启动时所要执行的命令，而RUN用于指定镜像构建时所要执行的命令。
```

##### ENTRYPOINT

**配置容器，使其可执行化。配合CMD可省去"application"，只使用参数**

```dockerfile
ENTRYPOINT ["executable", "param1", "param2"] (可执行文件, 优先)
ENTRYPOINT command param1 param2 (shell内部命令)
# 示例：
FROM ubuntu
ENTRYPOINT ["top", "-b"]
CMD ["-c"]
# 注：ENTRYPOINT与CMD非常类似，不同的是通过docker run执行的命令不会覆盖ENTRYPOINT，而docker run命令中指定的任何参数，都会被当做参数再次传递给ENTRYPOINT。Dockerfile中只允许有一个ENTRYPOINT命令，多指定时会覆盖前面的设置，而只执行最后的ENTRYPOINT指令。
```

##### LABEL

**用于为镜像添加元数据**

```dockerfile
LABEL <key>=<value> <key>=<value> <key>=<value> ...
# 示例：
LABEL version="1.0" description="这是一个Web服务器" by="IT笔录"
# 注：使用LABEL指定元数据时，一条LABEL指定可以指定一或多条元数据，指定多条元数据时不同元数据之间通过空格分隔。推荐将所有的元数据通过一条LABEL指令指定，以免生成过多的中间镜像。
```

##### ENV

**设置环境变量**

```dockerfile
ENV <key> <value>  #<key>之后的所有内容均会被视为其<value>的组成部分，因此，一次只能设置一个变量
ENV <key>=<value> ...  #可以设置多个变量，每个变量为一个"<key>=<value>"的键值对，如果<key>中包含空格，可以使用\来进行转义，也可以通过""来进行标示；另外，反斜线也可以用于续行
示例：
ENV myName John Doe
ENV myDog Rex The Dog
ENV myCat=fluffy
```

##### EXPOSE

**指定于外界交互的端口,仅仅只是声明端口**

```dockerfile
EXPOSE <port> [<port>...]
# 示例：
EXPOSE 80 443
EXPOSE 8080    
EXPOSE 11211/tcp 11211/udp
# 注：EXPOSE并不会让容器的端口访问到主机。要使其可访问，需要在docker run运行容器时通过-p来发布这些端口，或通过-P参数来发布EXPOSE导出的所有端口
```

##### VOLUME

**定义匿名数据卷。在启动容器时忘记挂载数据卷，会自动挂载到匿名卷**

```dockerfile
VOLUME ["/path/to/dir"]
# 示例：
VOLUME ["/data"]
VOLUME ["/var/www", "/var/log/apache2", "/etc/apache2"]
# 注：一个卷可以存在于一个或多个容器的指定目录，该目录可以绕过联合文件系统，并具有以下功能：

# 1 卷可以容器间共享和重用
# 2 容器并不一定要和其它容器共享卷
# 3 修改卷后会立即生效
# 4 对卷的修改不会对镜像产生影响
# 5 卷会一直存在，直到没有任何容器在使用它

#在启动容器 docker run 的时候，我们可以通过 -v 参数修改挂载点
```

##### WORKDIR

**工作目录，类似于cd命令**

```dockerfile
WORKDIR /path/to/workdir
# 示例：
WORKDIR /a  # (这时工作目录为/a)
WORKDIR b   # (这时工作目录为/a/b)
WORKDIR c   # (这时工作目录为/a/b/c)
# 注：通过WORKDIR设置工作目录后，Dockerfile中其后的命令RUN、CMD、ENTRYPOINT、ADD、COPY等命令都会在该目录下执行。在使用docker run运行容器时，可以通过-w参数覆盖构建时所设置的工作目录。
```

##### USER

**指定运行容器时的用户名或 UID，后续的 RUN 也会使用指定用户。使用USER指定用户时，可以使用用户名、UID或GID，或是两者的组合。当服务不需要管理员权限时，可以通过该命令指定运行用户。并且可以在之前创建所需要的用户**

```dockerfile
USER user　　
USER user:group　　
USER uid　　
USER uid:gid　　
USER user:gid　　
USER uid:group
# 注：使用USER指定用户后，Dockerfile中其后的命令RUN、CMD、ENTRYPOINT都将使用该用户。镜像构建完成后，通过docker run运行容器时，可以通过-u参数来覆盖所指定的用户。
```

##### ARG

**用于指定传递给构建运行时的变量**

```dockerfile
ARG <name>[=<default value>]
# 示例：
ARG site
ARG build_user=www
```

##### ONBUILD

**用于设置镜像触发器**

```dockerfile
ONBUILD [INSTRUCTION]
# 示例：
ONBUILD ADD . /app/src
ONBUILD RUN /usr/local/bin/python-build --dir /app/src
# 注：当所构建的镜像被用做其它镜像的基础镜像，该镜像中的触发器将会被钥触发
```



## Docker Compose

### 概述

Compose 是用于定义和运行多容器 Docker 应用程序的工具。通过 Compose，您可以使用 YML 文件来配置应用程序需要的所有服务。然后，使用一个命令，就可以从 YML 文件配置中创建并启动所有服务

- 使用 Dockerfile 定义应用程序的环境。
- 使用 docker-compose.yml 定义构成应用程序的服务，这样它们可以在隔离环境中一起运行。
- 最后，执行 docker-compose up 命令来启动并运行整个应用程序。



## Docker network

```shell
fs-user067@fs-user067-PC:~$ sudo docker network ls
NETWORK ID          NAME                DRIVER              SCOPE
d8884786fc66        bridge              bridge              local
b2a224dea752        host                host                local
1ab55ea2dbc4        none                null                local
```

Docker有三种网络模式，bridge、host、none，在你创建容器的时候，不指定--network默认是bridge。

bridge：为每一个容器分配IP，并将容器连接到一个docker0虚拟网桥，通过docker0网桥与宿主机通信。也就是说，**此模式下，你不能用宿主机的IP+容器映射端口来进行Docker容器之间的通信。**

host：容器不会虚拟自己的网卡，配置自己的IP，而是使用宿主机的IP和端口。这样一来，**Docker容器之间的通信就可以用宿主机的IP+容器映射端口**

none：无网络。

### 创建子网

```shell
docker network create --driver bridge --subnet=172.18.0.0/16 --gateway=172.18.0.1 zoonet
```

相关命令

```shell
# 查看所有网络
docker network ls
# 查看某个网络详细信息
docker network inspect [NETWORK ID]
# 删除某个网络
docker network rm [NETWORK ID]
# 创建网络
docker network create --driver bridge --subnet=172.18.0.0/16 --gateway=172.18.0.1 zoonet
# --driver :网络模式
# --subnet :子网地址
# --gateway :网关
# zoonet :网络名称
```



# Docker部署Mysql

## 单节点

### my.cnf

```properties
# Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; version 2 of the License.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA

#
# The MySQL  Server configuration file.
#
# For explanations see
# http://dev.mysql.com/doc/mysql/en/server-system-variables.html

[mysqld]
pid-file        = /var/run/mysqld/mysqld.pid
socket          = /var/run/mysqld/mysqld.sock
datadir         = /var/lib/mysql
secure-file-priv= NULL
```



### 5.7

```shell
# 拉取镜像
sudo docker pull mysql:5.7.31

# 创建目录
mkdir /opt/data/docker-mapping/single/mysql/5.7/
mkdir /opt/data/docker-mapping/single/mysql/5.7/conf
# 编辑my.cnf
vim /opt/data/docker-mapping/single/mysql/5.7/conf/my.cnf

# 运行容器
sudo docker run -itd -p 3306:3306 --name mysql5.7.31 -v /opt/data/docker-mapping/single/mysql/5.7/conf:/etc/mysql -v /opt/data/docker-mapping/single/mysql/5.7/logs:/logs -v /opt/data/docker-mapping/single/mysql/5.7/data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=123456 42cdba9f1b08

# -e MYSQL_ROOT_PASSWORD：配置mysql的root用户的登陆密码

#连接mysql
sudo docker exec -it 40f09acfaf44 /bin/bash
```



### 8.0

```sh
# 拉取镜像
sudo docker pull mysql:8.0.21

# 创建目录
mkdir /opt/data/docker-mapping/single/mysql/8.0/data
mkdir /opt/data/docker-mapping/single/mysql/8.0/conf
# 编辑my.cnf
vim /opt/data/docker-mapping/single/mysql/8.0/conf/my.cnf

# 运行容器
sudo docker run -itd -p 3306:3306 --name mysql8.0.21 -v /opt/data/docker-mapping/single/mysql/8.0/conf:/etc/mysql -v /opt/data/docker-mapping/single/mysql/8.0/data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=123456 8e85dd5c3255

# -e MYSQL_ROOT_PASSWORD：配置mysql的root用户的登陆密码

#连接mysql
sudo docker exec -it c01103ab7608 /bin/bash
```



## cluster



# Docker部署Redis

## 单节点

```sh
# 拉取镜像
sudo docker pull redis:6.0.8
```

https://blog.csdn.net/smartsteps/article/details/103678263/

## cluster



# Docker部署Zookeeper

## 单节点

```sh
# 拉取镜像
sudo docker pull zookeeper:3.5.8

# 创建映射目录
mkdir /opt/data/docker-mapping/single/zookeeper/3.5.8

# 运行容器
sudo docker run -d -p 2181:2181 --name zookeeper3.5.8 --privileged -v /opt/data/docker-mapping/single/zookeeper/3.5.8/data:/data -v /opt/data/docker-mapping/single/zookeeper/3.5.8/datalog:/datalog -v /opt/data/docker-mapping/single/zookeeper/3.5.8/logs:/logs 910b6d43da81

# 进入容器
sudo docker exec -it d5c6f857cd88 /bin/bash
```



## cluster

```sh
# 拉取镜像
sudo docker pull zookeeper:3.5.8

# 搭建集群子网
sudo docker network create --driver bridge --subnet=172.19.0.0/16 --gateway=172.19.0.1 cluster_net

# 创建映射目录
mkdir /opt/data/docker-mapping/cluster/zookeeper/node-0
mkdir /opt/data/docker-mapping/cluster/zookeeper/node-1
mkdir /opt/data/docker-mapping/cluster/zookeeper/node-2

# 运行容器 zookeeper-0
sudo docker run -d -p 2180:2181 --name zookeeper-0 --privileged --network cluster_net --ip 172.19.1.0 -v /opt/data/docker-mapping/cluster/zookeeper/node-0/data:/data -v /opt/data/docker-mapping/cluster/zookeeper/node-0/datalog:/datalog -v /opt/data/docker-mapping/cluster/zookeeper/node-0/logs:/logs -e ZOO_MY_ID=0 -e "ZOO_SERVERS=server.0=172.19.1.0:2888:3888;2181 server.1=172.19.1.1:2888:3888;2181 server.2=172.19.1.2:2888:3888;2181" 910b6d43da81

# 运行容器 zookeeper-1
sudo docker run -d -p 2181:2181 --name zookeeper-1 --privileged  --network cluster_net --ip 172.19.1.1 -v /opt/data/docker-mapping/cluster/zookeeper/node-1/data:/data -v /opt/data/docker-mapping/cluster/zookeeper/node-1/datalog:/datalog -v /opt/data/docker-mapping/cluster/zookeeper/node-1/logs:/logs -e ZOO_MY_ID=1 -e "ZOO_SERVERS=server.0=172.19.1.0:2888:3888;2181 server.1=172.19.1.1:2888:3888;2181 server.2=172.19.1.2:2888:3888;2181" 910b6d43da81

# 运行容器 zookeeper-2
sudo docker run -d -p 2182:2181 --name zookeeper-2 --privileged  --network cluster_net --ip 172.19.1.2 -v /opt/data/docker-mapping/cluster/zookeeper/node-2/data:/data -v /opt/data/docker-mapping/cluster/zookeeper/node-2/datalog:/datalog -v /opt/data/docker-mapping/cluster/zookeeper/node-2/logs:/logs -e ZOO_MY_ID=2 -e "ZOO_SERVERS=server.0=172.19.1.0:2888:3888;2181 server.1=172.19.1.1:2888:3888;2181 server.2=172.19.1.2:2888:3888;2181" 910b6d43da81

# 外部连接测试
./bin/zkCli.sh -server 59.110.23.195:2180,59.110.23.195:2181,59.110.23.195:2182
```



# Docker部署Kafka

## 构建kafka镜像

### 下载jdk和kafka

jdk-8u261-linux-x64.tar.gz  kafka_2.13-2.6.0.tgz

解压出来

jdk1.8.0_261  kafka_2.13-2.6.0

### 编写Dockerfile

使用已解压的安装包

```sh
FROM debian:buster
MAINTAINER imyxiong@163.com
RUN mkdir /opt/jdk && \
    mkdir /opt/kafka && \
    mkdir /opt/data/ && \
    mkdir /opt/data/kafka && \
    mkdir /opt/data/kafka/data
VOLUME /opt/data/kafka/
COPY ./jdk* /opt/jdk
COPY ./kafka* /opt/kafka
COPY ./docker-entrypoint.sh /
RUN chmod a+x /docker-entrypoint.sh
ENV JAVA_HOME=/opt/jdk
ENV CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
ENV PATH=$JAVA_HOME/bin:$PATH
EXPOSE 9092
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["/opt/kafka/bin/kafka-server-start.sh","/opt/kafka/config/my.server.properties"]
```

不解压安装包

不解压安装包构建的镜像会比解压安装包构建的镜像大大约一倍

```sh
FROM debian:buster
MAINTAINER imyxiong@163.com
ADD ./jdk* /opt/
ADD ./kafka* /opt/
COPY ./docker-entrypoint.sh /
RUN mv /opt/jdk* /opt/jdk && \
    mv /opt/kafka* /opt/kafka && \
    mkdir /opt/data/ && \
    mkdir /opt/data/kafka && \
    mkdir /opt/data/kafka/data && \
    chmod a+x /docker-entrypoint.sh
VOLUME /opt/data/kafka/
ENV JAVA_HOME=/opt/jdk 
ENV CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar 
ENV PATH=$JAVA_HOME/bin:$PATH 
EXPOSE 9092
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["/opt/kafka/bin/kafka-server-start.sh","/opt/kafka/config/my.server.properties"]

```

### 编写docker-entrypoint.sh

```sh
#!/bin/bash
# 告诉bash如果任何语句的执行结果不是true则应该退出
set -e
PORT=9092
echo "broker.id=$BROKER_ID" > /opt/kafka/config/my.server.properties
echo "host.name=$HOST_NAME" >> /opt/kafka/config/my.server.properties
echo "listeners=PLAINTEXT://$HOST_NAME:$PORT" >> /opt/kafka/config/my.server.properties
echo "advertised.listeners=PLAINTEXT://$HOST_NAME:$PORT" >> /opt/kafka/config/my.server.properties
echo "log.dirs=/opt/data/kafka/data" >> /opt/kafka/config/my.server.properties
echo "zookeeper.connect=$ZOO_CONNECT" >> /opt/kafka/config/my.server.properties
echo "num.network.threads=3" >> /opt/kafka/config/my.server.properties
echo "num.io.threads=8" >> /opt/kafka/config/my.server.properties
echo "socket.send.buffer.bytes=102400" >> /opt/kafka/config/my.server.properties
echo "socket.receive.buffer.bytes=102400" >> /opt/kafka/config/my.server.properties
echo "socket.request.max.bytes=104857600" >> /opt/kafka/config/my.server.properties
echo "num.partitions=1" >> /opt/kafka/config/my.server.properties
echo "num.recovery.threads.per.data.dir=1" >> /opt/kafka/config/my.server.properties
echo "offsets.topic.replication.factor=1" >> /opt/kafka/config/my.server.properties
echo "transaction.state.log.replication.factor=1" >> /opt/kafka/config/my.server.properties
echo "transaction.state.log.min.isr=1" >> /opt/kafka/config/my.server.properties
echo "log.retention.check.interval.ms=300000" >> /opt/kafka/config/my.server.properties
echo "zookeeper.connection.timeout.ms=18000" >> /opt/kafka/config/my.server.properties
echo "group.initial.rebalance.delay.ms=0" >> /opt/kafka/config/my.server.properties
exec "$@"

```

### 构建镜像

将jdk1.8.0_261  kafka_2.13-2.6.0  Dockerfile  docker-entrypoint.sh文件放在build目录下并进入该目录执行

```sh
sudo docker build -f ./Dockerfile -t kafka:2.13-2.6.0 .
```

构建完成后即可看见本地镜像生成了kafka:2.13-2.6.0



## 单节点

```sh
# 创建映射目录
mkdir /opt/data/docker-mapping/single/kafka/2.13-2.6.0

# 搭建集群子网
sudo docker network create --driver bridge --subnet=172.19.0.0/16 --gateway=172.19.0.1 cluster_net

# 运行容器
# 同一主机下zookeeper也部署在相同子网
sudo docker run -itd -p 9092:9092 --name kafka2.13-2.6.0 --network cluster_net --ip 172.19.1.1 --privileged -e BROKER_ID=0 -e HOST_NAME=172.19.1.1 -e ZOO_CONNECT="172.19.1.0:2181" -v /opt/data/docker-mapping/single/kafka/2.13-2.6.0:/opt/data/kafka/ 2c2f3329f57b

# 查看启动输出
sudo docker logs c65c1d843bdd

# 进入容器
sudo docker exec -it c65c1d843bdd /bin/bash
```



## cluster

```sh
# 准备几台zookeeper
# 172.19.1.0:2181
# 172.19.1.1:2181
# 172.19.1.2:2181

# 创建映射目录
mkdir /opt/data/docker-mapping/cluster/kafka/

# 搭建集群子网
sudo docker network create --driver bridge --subnet=172.19.0.0/16 --gateway=172.19.0.1 cluster_net

# 运行容器 kafka-0
# 同一主机下zookeeper也部署在相同子网
sudo docker run -itd -p 9090:9092 --name kafka-0 --network cluster_net --ip 172.19.2.0 --privileged -e BROKER_ID=0 -e HOST_NAME=172.19.2.0 -e ZOO_CONNECT="172.19.1.0:2181,172.19.1.1:2181,172.19.1.2:2181" -v /opt/data/docker-mapping/cluster/kafka/node-0:/opt/data/kafka/ 3a6950e98451

# 运行容器 kafka-1
sudo docker run -itd -p 9091:9092 --name kafka-1 --network cluster_net --ip 172.19.2.1 --privileged -e BROKER_ID=1 -e HOST_NAME=172.19.2.1 -e ZOO_CONNE
CT="172.19.1.0:2181,172.19.1.1:2181,172.19.1.2:2181" -v /opt/data/docker-mapping/cluster/kafka/node-1:/opt/data/kafka/ 3a6950e98451

# 运行容器 kafka-2
sudo docker run -itd -p 9092:9092 --name kafka-2 --network cluster_net --ip 172.19.2.2 --privileged -e BROKER_ID=2 -e HOST_NAME=172.19.2.2 -e ZOO_CONNECT="172.19.1.0:2181,172.19.1.1:2181,172.19.1.2:2181" -v /opt/data/docker-mapping/cluster/kafka/node-2:/opt/data/kafka/ 3a6950e98451

# 查看启动输出
sudo docker logs c65c1d843bdd

# 进入容器
sudo docker exec -it c65c1d843bdd /bin/bash
```





# Docker部署RabbitMQ

## 单节点

```sh
# 拉取镜像
# 如果docker pull rabbitmq 后面不带management，启动rabbitmq后是无法打开管理界面的
sudo docker pull rabbitmq:3.8.8-management

# 创建映射目录

# 运行容器
sudo docker run -itd --name rabbitmq3.8.8 -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=123456 -p 15672:15672 -p 5672:5672 -p 4369:4369 -p 25672:25672 cc20c50012e2

# 查看启动输出
sudo docker logs fa52730455e2

# 进入容器
sudo docker exec -it fa52730455e2 /bin/bash
```





## cluster





# Kubernetes







## K8s 集群搭建

[Kubernetes 深入学习（一） —— 入门和集群安装部署 - bojiangzhou - 博客园 (cnblogs.com)](https://www.cnblogs.com/chiangchou/p/k8s-1.html)

[etcd错误总结 - 杨灏 - 博客园 (cnblogs.com)](https://www.cnblogs.com/HByang/p/13340857.html)

[【错误解决】kubernetes 1.21.10 apiserver报错 Error: [service-account-issuer is a required flag_error: [service-account-issuer is a required flag,-CSDN博客](https://blog.csdn.net/weixin_42072280/article/details/123428647)

[kubernetes 概念&部署_master节点需要部署kubelet 吗-CSDN博客](https://blog.csdn.net/wjs7740/article/details/74452055)

### 操作系统初始化

```shell
# 1、关闭防火墙
$ sudo systemctl stop firewalld
$ sudo systemctl disable firewalld

# 2、关闭 selinux
# # 临时生效
$ setenforce 0

# # 永久生效
$ sed -i 's/enforcing/disabled/' /etc/selinux/config

# 3、关闭 swap
# # 临时关闭
$ sudo swapoff -a

# # 永久生效
$ vim /etc/fstab
# #将 [UUID=5b59fd54-eaad-41d6-90b2-ce28ac65dd81 swap                    swap    defaults        0 0] 这一行注释掉

# 4、添加 hosts
$ vim /etc/hosts
192.168.31.24 k8s-master-1
192.168.31.26 k8s-master-2
192.168.31.35 k8s-node-1
192.168.31.71 k8s-node-2
192.168.31.178 k8s-lb-master
192.168.31.224 k8s-lb-backup

# 5、同步系统时间
# 各个节点之间需保持时间一致，因为自签证书是根据时间校验证书有效性，如果时间不一致，将校验不通过。
# # 联网情况可使用如下命令
$ ntpdate time.windows.com

# # 如果不能联外网可使用 date 命令设置时间

# 创建用户用户组
$ sudo groupadd kubernetes
$ sudo useradd -g kubernetes -s /bin/bash -m k8s
$ sudo passwd k8s
$ sudo usermod -aG sudoers k8s
```



### 部署 Etcd 集群

#### 生成证书

##### 准备 cfssl 工具

```shell
# 准备 cfssl 工具
$ sudo curl -L https://pkg.cfssl.org/R1.2/cfssl_linux-amd64 -o /usr/local/bin/cfssl
$ sudo curl -L https://pkg.cfssl.org/R1.2/cfssljson_linux-amd64 -o /usr/local/bin/cfssljson
$ sudo curl -L https://pkg.cfssl.org/R1.2/cfssl-certinfo_linux-amd64 -o /usr/local/bin/cfssl-certinfo

$ sudo chmod +x /usr/local/bin/cfssl*

# 创建目录
$ sudo mkdir -p /opt/apps/k8s/etcd/{ssl,cfg,bin}
$ cd /opt/apps/k8s/etcd/ssl
```



##### 生成 CA 证书和私钥

```shell
# 创建 CA 配置文件：ca-config.json
$ sudo vim ca-config.json
{
  "signing": {
    "default": {
      "expiry": "87600h"
    },
    "profiles": {
      "etcd": {
        "usages": [
            "signing",
            "key encipherment",
            "server auth",
            "client auth"
        ],
        "expiry": "87600h"
      }
    }
  }
}

# signing：表示该证书可用于签名其它证书；生成的 ca.pem 证书中 CA=TRUE；
# profiles：可以定义多个 profiles，分别指定不同的过期时间、使用场景等参数；后续在签名证书时使用某个 profile；
# expiry：证书过期时间
# server auth：表示client可以用该 CA 对server提供的证书进行验证；
# client auth：表示server可以用该CA对client提供的证书进行验证；

# 创建 CA 证书签名请求文件：ca-csr.json
$ sudo vim ca-csr.json
{
  "CN": "etcd",
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "ST": "Shanghai",
      "L": "Shanghai",
      "O": "etcd",
      "OU": "System"
    }
  ],
    "ca": {
       "expiry": "87600h"
    }
}

# CN：Common Name，kube-apiserver 从证书中提取该字段作为请求的用户名 (User Name)；浏览器使用该字段验证网站是否合法；
# key：加密算法
# C：国家
# ST：地区
# L：城市
# O：组织，kube-apiserver 从证书中提取该字段作为请求用户所属的组 (Group)；
# OU：组织单位

# 生成 CA 证书和私钥
$ sudo bash -c "cfssl gencert -initca ca-csr.json | cfssljson -bare ca"
# ca-key.pem：CA 私钥
# ca.pem：CA 数字证书
```



##### 生成 etcd 证书和私钥

```shell
# 创建证书签名请求文件：etcd-csr.json
$ sudo vim etcd-csr.json
{
  "CN": "etcd",
  "hosts": [
    "192.168.31.101",
    "192.168.31.102",
    "192.168.31.103"
  ],
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "ST": "Shanghai",
      "L": "Shanghai",
      "O": "etcd",
      "OU": "System"
    }
  ]
}

# 为 etcd 生成证书和私钥
$ sudo bash -c "cfssl gencert -ca=ca.pem -ca-key=ca-key.pem -config=ca-config.json -profile=etcd etcd-csr.json | cfssljson -bare etcd"
# -ca：指定 CA 数字证书
# -ca-key：指定 CA 私钥
# -config：CA 配置文件
# -profile：指定环境
# -bare：指定证书名前缀

# etcd-key.pem：etcd 私钥
# etcd.pem：etcd 数字证书
```



证书生成完成，后面部署 Etcd 时主要会用到如下几个证书：ca-key.pem、ca.pem、etcd-key.pem、etcd.pem



#### Etcd 数据库集群部署

etcd 集群采用主从架构模式（一主多从）部署，集群通过选举产生 leader，因此需要部署奇数个节点（3/5/7）才能正常工作。etcd使用raft一致性算法保证每个节点的一致性。



##### 下载 etcd

```shell
# 从 github 上下载合适版本的 etcd
$ wget https://github.com/etcd-io/etcd/releases/download/v3.5.14/etcd-v3.5.14-linux-amd64.tar.gz
$ tar zxf etcd-v3.5.14-linux-amd64.tar.gz
$ sudo cp etcd-v3.5.14-linux-amd64/{etcd,etcdctl} /opt/apps/k8s/etcd/bin
$ rm -rf etcd-v3.5.14-linux-amd64
```



##### 配置 etcd 

创建 etcd 配置文件：**etcd.conf**

`/opt/apps/k8s/etcd/cfg/etcd.conf`

```conf
# [member]
ETCD_NAME=etcd-1
ETCD_DATA_DIR=/opt/data/k8s/default.etcd
ETCD_LISTEN_PEER_URLS=https://192.168.31.101:2380
ETCD_LISTEN_CLIENT_URLS=https://192.168.31.101:2379

# [cluster]
ETCD_INITIAL_ADVERTISE_PEER_URLS=https://192.168.31.101:2380
ETCD_ADVERTISE_CLIENT_URLS=https://192.168.31.101:2379
ETCD_INITIAL_CLUSTER=etcd-1=https://192.168.31.101:2380,etcd-2=https://192.168.31.102:2380,etcd-3=https://192.168.31.103:2380
ETCD_INITIAL_CLUSTER_TOKEN=etcd-cluster
ETCD_INITIAL_CLUSTER_STATE=new

# [security]
ETCD_CERT_FILE=/opt/apps/k8s/etcd/ssl/etcd.pem
ETCD_KEY_FILE=/opt/apps/k8s/etcd/ssl/etcd-key.pem
ETCD_TRUSTED_CA_FILE=/opt/apps/k8s/etcd/ssl/ca.pem
ETCD_PEER_CERT_FILE=/opt/apps/k8s/etcd/ssl/etcd.pem
ETCD_PEER_KEY_FILE=/opt/apps/k8s/etcd/ssl/etcd-key.pem
ETCD_PEER_TRUSTED_CA_FILE=/opt/apps/k8s/etcd/ssl/ca.pem
```



**说明：**

- ETCD_NAME：etcd在集群中的唯一名称
- ETCD_DATA_DIR：etcd数据存放目录
- ETCD_LISTEN_PEER_URLS：etcd集群间通讯的地址，设置为本机IP
- ETCD_LISTEN_CLIENT_URLS：客户端访问的地址，设置为本机IP
- 
- ETCD_INITIAL_ADVERTISE_PEER_URLS：初始集群通告地址，集群内部通讯地址，设置为本机IP
- ETCD_ADVERTISE_CLIENT_URLS：客户端通告地址，设置为本机IP
- ETCD_INITIAL_CLUSTER：集群节点地址，以 key=value 的形式添加各个 etcd 的地址
- ETCD_INITIAL_CLUSTER_TOKEN：集群令牌，用于集群间做简单的认证
- ETCD_INITIAL_CLUSTER_STATE：集群状态
- 
- ETCD_CERT_FILE：客户端 etcd 数字证书路径
- ETCD_KEY_FILE：客户端 etcd 私钥路径
- ETCD_TRUSTED_CA_FILE：客户端 CA 证书路径
- ETCD_PEER_CERT_FILE：集群间通讯etcd数字证书路径
- ETCD_PEER_KEY_FILE：集群间通讯etcd私钥路径
- ETCD_PEER_TRUSTED_CA_FILE：集群间通讯CA证书路径



##### 创建 etcd 服务

通过EnvironmentFile指定 **etcd.conf** 作为环境配置文件

`/opt/apps/k8s/etcd/etcd.service`

```conf
[Unit]
Description=Etcd Server
After=network.target
After=network-online.target
Wants=network-online.target

[Service]
Type=notify
EnvironmentFile=/opt/apps/k8s/etcd/cfg/etcd.conf
WorkingDirectory=/opt/data/k8s/default.etcd

ExecStart=/opt/apps/k8s/etcd/bin/etcd \
# ETCD3.4版本会自动读取环境变量的参数，所以EnvironmentFile文件中有的参数，不需要再次在ExecStart启动参数中添加，二选一，如同时配置，会触发以下类似报错
# etcd: conflicting environment variable "ETCD_NAME" is shadowed by corresponding command-line flag (either unset environment variable or disable flag)
  --name=${ETCD_NAME} \
  --data-dir=${ETCD_DATA_DIR} \
  --listen-peer-urls=${ETCD_LISTEN_PEER_URLS} \
  --listen-client-urls=${ETCD_LISTEN_CLIENT_URLS},http://127.0.0.1:2379 \
  --initial-advertise-peer-urls=${ETCD_INITIAL_ADVERTISE_PEER_URLS} \
  --advertise-client-urls=${ETCD_ADVERTISE_CLIENT_URLS} \
  --initial-cluster=${ETCD_INITIAL_CLUSTER} \
  --initial-cluster-token=${ETCD_INITIAL_CLUSTER_TOKEN} \
  --initial-cluster-state=${ETCD_INITIAL_CLUSTER_STATE} \
  --cert-file=${ETCD_CERT_FILE} \
  --key-file=${ETCD_KEY_FILE} \
  --trusted-ca-file=${ETCD_TRUSTED_CA_FILE} \
  --peer-cert-file=${ETCD_PEER_CERT_FILE} \
  --peer-key-file=${ETCD_PEER_KEY_FILE} \
  --peer-trusted-ca-file=${ETCD_PEER_TRUSTED_CA_FILE}

Restart=on-failure
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
```



etcd.service 更多的配置以及说明可以通过如下命令查看：`/opt/apps/k8s/etcd/bin/etcd --help`



##### 其它节点配置 etcd

```shell
# 将 etcd 目录拷贝到另外两个节点
$ sudo scp -r /opt/apps/k8s young@192.168.31.102:/home/young/k8s
$ sudo scp -r /opt/apps/k8s young@192.168.31.103:/home/young/k8s
$ sudo mv /home/young/k8s /opt/apps/

# 修改两个节点配置文件
$ sudo vim /opt/apps/k8s/etcd/cfg/etcd.conf
# [member]
ETCD_NAME=etcd-2
ETCD_LISTEN_PEER_URLS=https://192.168.31.102:2380
ETCD_LISTEN_CLIENT_URLS=https://192.168.31.102:2379

# [cluster]
ETCD_INITIAL_ADVERTISE_PEER_URLS=https://192.168.31.102:2380
ETCD_ADVERTISE_CLIENT_URLS=https://192.168.31.102:2379
```



##### 启动 etcd 服务

```shell
# 首先在三个节点将 etcd.service 拷贝到 /usr/lib/systemd/system/
$ sudo cp /opt/apps/k8s/etcd/etcd.service /usr/lib/systemd/system/
# 创建 WorkingDirectory 目录
$ sudo mkdir /opt/data/k8s/default.etcd

$ sudo systemctl daemon-reload

# 在三个节点启动 etcd 服务
$ sudo systemctl start etcd

# 设置开机启动
$ sudo systemctl enable etcd

# 查看 etcd 的日志
$ sudo journalctl -u etcd.service -e

```



##### 查看 etcd 集群状态

```shell
/opt/apps/k8s/etcd/bin/etcdctl \
    --ca-file=/opt/apps/k8s/etcd/ssl/ca.pem \
    --cert-file=/opt/apps/k8s/etcd/ssl/etcd.pem \
    --key-file=/opt/apps/k8s/etcd/ssl/etcd-key.pem \
    --endpoints=https://192.168.31.101:2379,https://192.168.31.102:2379,https://192.168.31.103:2379 cluster-health
```



### 部署 Master 组件

K8S 集群中所有资源的访问和变更都是通过 kube-apiserver 的 REST API 来实现的，首先在 master 节点上部署 kube-apiserver 组件。

#### 自签 ApiServer SSL 证书

过程与 etcd 自签SSL证书类似

```shell
# 创建目录
$ sudo mkdir -p /opt/apps/k8s/kubernetes/{ssl,cfg,bin,logs}
$ cd /opt/apps/k8s/kubernetes/ssl
```



##### 生成 CA 证书和私钥

```shell
# 创建 CA 配置文件：ca-config.json
$ sudo vim ca-config.json
{
  "signing": {
    "default": {
      "expiry": "87600h"
    },
    "profiles": {
      "kubernetes": {
        "usages": [
            "signing",
            "key encipherment",
            "server auth",
            "client auth"
        ],
        "expiry": "87600h"
      }
    }
  }
}

# 创建 CA 证书签名请求文件：ca-csr.json
$ sudo vim ca-csr.json
{
  "CN": "kubernetes",
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "ST": "Shanghai",
      "L": "Shanghai",
      "O": "kubernetes",
      "OU": "System"
    }
  ],
  "ca": {
    "expiry": "87600h"
  }
}

# 生成 CA 证书和私钥
$sudo bash -c "cfssl gencert -initca ca-csr.json | cfssljson -bare ca"
```



##### 生成 kubernetes 证书和私钥

```shell
# 创建证书签名请求文件：kubernetes-csr.json
$ sudo vim kubernetes-csr.json
{
  "CN": "kubernetes",
  "hosts": [
  	"127.0.0.1",
	"10.0.0.1",
    "192.168.31.101",
    "192.168.31.102",
    "192.168.31.103",
    "kubernetes",
    "kubernetes.default",
    "kubernetes.default.svc",
    "kubernetes.default.svc.cluster",
    "kubernetes.default.svc.cluster.local"
  ],
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "ST": "Shanghai",
      "L": "Shanghai",
      "O": "kubernetes",
      "OU": "System"
    }
  ]
}

# hosts：指定会直接访问 apiserver 的IP列表，一般需指定 etcd 集群、kubernetes master 集群的主机 IP 和 kubernetes 服务的服务 IP，Node 的IP一般不需要加入。

# 为 kubernetes 生成证书和私钥
$ sudo bash -c "cfssl gencert -ca=ca.pem -ca-key=ca-key.pem -config=ca-config.json -profile=kubernetes kubernetes-csr.json | cfssljson -bare kubernetes"
```



##### 生成 admin 证书和私钥

```shell
# 创建证书签名请求文件：admin-csr.json
$ sudo vim admin-csr.json
{
  "CN": "admin",
  "hosts": [],
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "ST": "Shanghai",
      "L": "Shanghai",
      "O": "system:masters",
      "OU": "System"
    }
  ]
}

# 注意：重点在于上面的"O":“system:masters”,
# kube-apiserver 预定义了一些 RBAC 使用的 RoleBindings，如 cluster-admin 将 Group system:masters 与 Role cluster-admin 绑定，该 Role 授予了调用kube-apiserver 的所有 API的权限； O 指定该证书的 Group 为 system:masters，kubelet 使用该证书访问 kube-apiserver 时 ，由于证书被 CA 签名，所以认证通过，同时由于证书用户组为经过预授权的 system:masters，所以被授予访问所有 API 的权限

# 生成证书和私钥
$ sudo bash -c "cfssl gencert -ca=ca.pem -ca-key=ca-key.pem -config=ca-config.json -profile=kubernetes admin-csr.json | cfssljson -bare admin"
```



#### 部署 kube-apiserver 组件

##### 下载

```shell
# server 二进制包已经包含了 master、node 上的各个组件，下载 server 二进制包即可
$ wget https://dl.k8s.io/v1.30.2/kubernetes-server-linux-amd64.tar.gz
$ tar zxf kubernetes-server-linux-amd64.tar.gz
$ sudo cp -p kubernetes/server/bin/{kube-apiserver,kube-controller-manager,kube-scheduler} /opt/apps/k8s/kubernetes/bin/
$ sudo cp -p kubernetes/server/bin/kubectl /usr/local/bin/
$ sudo mv kubernetes /usr/local/src/
$ sudo mkdir /var/log/k8s/kubernetes
```



##### 配置

###### 创建 Node 令牌文件

Master apiserver 启用 TLS 认证后，Node节点 kubelet 组件想要加入集群，必须使用CA签发的有效证书才能与apiserver通信，当Node节点很多时，签署证书是一件很繁琐的事情，因此有了 TLS Bootstrap 机制，kubelet 会以一个低权限用户自动向 apiserver 申请证书，kubelet 的证书由 apiserver 动态签署。因此先为 apiserver 生成一个令牌文件，令牌之后会在 Node 中用到。

apiserver 配置的 token 必须与 Node 节点 bootstrap.kubeconfig 配置保持一致。

```shell
# 生成 token，一个随机字符串，可使用如下命令生成 token
$ head -c 16 /dev/urandom | od -An -t x | tr -d ' '
# 创建 token.csv，格式：token，用户，UID，用户组
sudo vim /opt/apps/k8s/kubernetes/cfg/token.csv
dd8dcf475a5f0568d99aaf939e525727,kubelet-bootstrap,10001,"system:node-bootstrapper"
09581b712dad24fcca9717cce19ae8f7,k8s,10002,"system:admin"
```



###### 配置 kube-apiserver

kube-apiserver 有很多配置项，可以参考官方文档查看每个配置项的用途：[kube-apiserver](https://kubernetes.io/zh/docs/reference/command-line-tools-reference/kube-apiserver/)

`/opt/apps/k8s/kubernetes/cfg/kube-apiserver.conf`

> “\” 后面不要有空格，不要有多余的换行，否则启动失败。

```conf
KUBE_APISERVER_OPTS="--etcd-servers=https://192.168.31.101:2379,https://192.168.31.102:2379,https://192.168.31.103:2379 \
  --bind-address=192.168.31.101 \
  --secure-port=6443 \
  --advertise-address=192.168.31.101 \
  --allow-privileged=true \
  --service-cluster-ip-range=10.0.0.0/24 \
  --service-node-port-range=30000-32767 \
  --enable-admission-plugins=NamespaceLifecycle,LimitRanger,ServiceAccount,ResourceQuota,NodeRestriction \
  --authorization-mode=RBAC,Node \
  --enable-bootstrap-token-auth=true \
  --token-auth-file=/opt/apps/k8s/kubernetes/cfg/token.csv \
  --kubelet-client-certificate=/opt/apps/k8s/kubernetes/ssl/kubernetes.pem \
  --kubelet-client-key=/opt/apps/k8s/kubernetes/ssl/kubernetes-key.pem \
  --tls-cert-file=/opt/apps/k8s/kubernetes/ssl/kubernetes.pem \
  --tls-private-key-file=/opt/apps/k8s/kubernetes/ssl/kubernetes-key.pem \
  --client-ca-file=/opt/apps/k8s/kubernetes/ssl/ca.pem \
  --service-account-key-file=/opt/apps/k8s/kubernetes/ssl/ca.pem \
  --service-account-signing-key-file=/opt/apps/k8s/kubernetes/ssl/ca-key.pem \
  --service-account-issuer=api \
  --etcd-cafile=/opt/apps/k8s/etcd/ssl/ca.pem \
  --etcd-certfile=/opt/apps/k8s/etcd/ssl/etcd.pem \
  --etcd-keyfile=/opt/apps/k8s/etcd/ssl/etcd-key.pem \
  --v=2 \
  --audit-log-maxage=30 \
  --audit-log-maxbackup=3 \
  --audit-log-maxsize=100 \
  --audit-log-path=/var/log/k8s/kubernetes/k8s-audit.log"
```



##### 创建 apiserver 服务

`/usr/lib/systemd/system/kube-apiserver.service`

```
[Unit]
Description=Kubernetes API Server
Documentation=https://github.com/GoogleCloudPlatform/kubernetes
After=network.target

[Service]
EnvironmentFile=/opt/apps/k8s/kubernetes/cfg/kube-apiserver.conf
ExecStart=/opt/apps/k8s/kubernetes/bin/kube-apiserver $KUBE_APISERVER_OPTS

Restart=on-failure
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
```



##### 启动 kube-apiserver 组件

[云原生|kubernetes部署和运行维护中的错误汇总（不定时更新）_etcd恢复后pod服务没生效-CSDN博客](https://blog.csdn.net/alwaysbefine/article/details/126531104)

```shell
$ sudo systemctl daemon-reload
# 设置开机启动
$ sudo systemctl enable kube-apiserver

# 启动服务
$ sudo systemctl start kube-apiserver

# 检查启动状态
$ sudo systemctl status kube-apiserver

# 查看启动日志
$ sudo journalctl -u kube-apiserver.service -e

# 检查端口，6443端口应该监听成功，代表apiserver安装成功
$ ss -pl | grep 6443
```



##### 配置 kubectl

[k8s kubeconfig配置文件 - 小吉猫 - 博客园 (cnblogs.com)](https://www.cnblogs.com/wangguishe/p/17600637.html)

[kubectl学习_kubectl 配置文件-CSDN博客](https://blog.csdn.net/mmxhappy/article/details/136020685)

[【错误解决】kubectl权限不够，报错：User “kubernetes“ cannot create resource “clusterrolebindings“ in API group “rb_error: failed to create clusterrolebinding: cluste-CSDN博客](https://blog.csdn.net/weixin_42072280/article/details/123431653)

`$HOME/.kube/config`

```shell
$ sudo kubectl config view

# kubectl config set-cluster <cluster-name> --server=<master-url> 设置集群信息
$ sudo kubectl config set-cluster kubernetes \
--server="https://192.168.31.101:6443" \
--certificate-authority=/opt/apps/k8s/kubernetes/ssl/ca.pem \
--embed-certs=true \
--kubeconfig=$HOME/.kube/config

# kubectl config set-credentials <user-name> 设置用户认证信息
$ sudo kubectl config set-credentials kubelet-bootstrap \
--client-certificate=/opt/apps/k8s/kubernetes/ssl/admin.pem \
--client-key=/opt/apps/k8s/kubernetes/ssl/admin-key.pem \
--embed-certs=true \
--kubeconfig=$HOME/.kube/config

# kubectl config set-context <context-name> --cluster=<cluster-name> --user=<user-name> 设置上下文信息
$ sudo kubectl config set-context kubelet-bootstrap@kubernetes \
--cluster=kubernetes \
--namespace=default \
--user=kubelet-bootstrap \
--kubeconfig=$HOME/.kube/config

$ sudo kubectl config set-credentials k8s --embed-certs=true --client-certificate=/opt/apps/k8s/kubernetes/ssl/admin.pem --client-key=/opt/apps/k8s/kubernetes/ssl/admin-key.pem --kubeconfig=$HOME/.kube/config
$ sudo kubectl config set-context k8s@kubernetes --cluster=kubernetes --namespace=default --user=k8s --kubeconfig=$HOME/.kube/config
```



###### 创建 kubelet bootstrapping kubeconfig 文件

```shell
$ export KUBE_APISERVER="https://192.168.31.101:6443"
# 设置集群参数
$ sudo kubectl config set-cluster kubernetes \
--certificate-authority=/opt/apps/k8s/kubernetes/ssl/ca.pem \
--embed-certs=true \
--server=${KUBE_APISERVER} \
--kubeconfig=bootstrap.kubeconfig
# 设置客户端认证参数
$ sudo kubectl config set-credentials kubelet-bootstrap \
--token=dd8dcf475a5f0568d99aaf939e525727 \
--kubeconfig=bootstrap.kubeconfig
# 设置上下文参数
$ sudo kubectl config set-context default \
--cluster=kubernetes \
--user=kubelet-bootstrap \
--kubeconfig=bootstrap.kubeconfig
# 设置默认上下文
$ sudo kubectl config use-context default --kubeconfig=bootstrap.kubeconfig

sudo kubectl create clusterrolebinding kubelet-bootstrap --clusterrole=system:node-bootstrapper --user=kubelet-bootstrap --kubeconfig=bootstrap.kubeconfig
```



###### 创建 kube-proxy kubeconfig 文件

```shell
# 设置集群参数
$ sudo kubectl config set-cluster kubernetes \
--certificate-authority=/opt/apps/k8s/kubernetes/ssl/ca.pem \
--embed-certs=true \
--server=${KUBE_APISERVER} \
--kubeconfig=kube-proxy.kubeconfig

# 设置客户端认证参数
$ sudo kubectl config set-credentials kube-proxy \
--client-certificate=/opt/apps/k8s/kubernetes/ssl/kube-proxy.pem \
--client-key=/opt/apps/k8s/kubernetes/ssl/kube-proxy-key.pem \
--embed-certs=true \
--kubeconfig=kube-proxy.kubeconfig

# 设置上下文参数
$ sudo kubectl config set-context default \
--cluster=kubernetes \
--user=kube-proxy \
--kubeconfig=kube-proxy.kubeconfig

# 设置默认上下文
$ sudo kubectl config use-context default --kubeconfig=kube-proxy.kubeconfig
```



##### kubelet-bootstrap 用户绑定系统集群角色

 将 kubelet-bootstrap 用户绑定到系统集群角色，之后便于 Node 使用token请求证书

```shell
$ sudo kubectl create clusterrolebinding kubelet-bootstrap \
  --kubeconfig=$HOME/.kube/config \
  --context=kubelet-bootstrap@kubernetes \
  --clusterrole=system:node-bootstrapper \
  --user=kubelet-bootstrap
```



#### 部署 kube-controller-manager 组件

##### 配置

详细的配置可参考官方文档：[kube-controller-manager](https://kubernetes.io/docs/reference/command-line-tools-reference/kube-controller-manager/)

`/opt/apps/k8s/kubernetes/cfg/kube-controller-manager.conf`

```conf
KUBE_CONTROLLER_MANAGER_OPTS="--leader-elect=true \
  --master=127.0.0.1:6443 \
  --allocate-node-cidrs=true \
  --cluster-cidr=10.244.0.0/16 \
  --service-cluster-ip-range=10.0.0.0/24 \
  --cluster-signing-cert-file=/opt/apps/k8s/kubernetes/ssl/ca.pem \
  --cluster-signing-key-file=/opt/apps/k8s/kubernetes/ssl/ca-key.pem \
  --root-ca-file=/opt/apps/k8s/kubernetes/ssl/ca.pem \
  --service-account-private-key-file=/opt/apps/k8s/kubernetes/ssl/ca-key.pem \
  --v=2"
```



##### 创建 kube-controller-manager 服务

`/usr/lib/systemd/system/kube-controller-manager.service`

```
[Unit]
Description=Kubernetes Controller Manager
Documentation=https://github.com/GoogleCloudPlatform/kubernetes
After=network.target

[Service]
EnvironmentFile=/opt/apps/k8s/kubernetes/cfg/kube-controller-manager.conf
ExecStart=/opt/apps/k8s/kubernetes/bin/kube-controller-manager $KUBE_CONTROLLER_MANAGER_OPTS

Restart=on-failure
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
```



##### 启动 kube-controller-manager 组件

```shell
$ sudo systemctl daemon-reload
# 设置开机启动
$ sudo systemctl enable kube-controller-manager

# 启动服务
$ sudo systemctl start kube-controller-manager

# 检查启动状态
$ sudo systemctl status kube-controller-manager

# 查看启动日志
$ sudo journalctl -u kube-controller-manager.service -e
```



#### 部署 kube-scheduler 组件

##### 配置

详细的配置可参考官方文档：[kube-scheduler](https://kubernetes.io/docs/reference/command-line-tools-reference/kube-scheduler/)

`/opt/apps/k8s/kubernetes/cfg/kube-scheduler.conf`

```conf
KUBE_SCHEDULER_OPTS="--leader-elect=true \
  --master=127.0.0.1:6443 \
  --v=2"
```



#####  创建 kube-scheduler 服务

`/usr/lib/systemd/system/kube-scheduler.service`

```
[Unit]
Description=Kubernetes Scheduler
Documentation=https://github.com/GoogleCloudPlatform/kubernetes
After=network.target

[Service]
EnvironmentFile=/opt/apps/k8s/kubernetes/cfg/kube-scheduler.conf
ExecStart=/opt/apps/k8s/kubernetes/bin/kube-scheduler $KUBE_SCHEDULER_OPTS

Restart=on-failure
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
```



##### 启动 kube-scheduler 组件

```shell
$ sudo systemctl daemon-reload
# 设置开机启动
$ sudo systemctl enable kube-scheduler

# 启动服务
$ sudo systemctl start kube-scheduler

# 检查启动状态
$ sudo systemctl status kube-scheduler

# 查看启动日志
$ sudo journalctl -u kube-scheduler.service -e
```



#### 查看组件状态

```shell
# 查看组件状态
$ sudo kubectl get cs
```



### 部署 Node 组件

#### 安装 Docker

##### 配置安装源

`/etc/apt/sources.list`

```
# 阿里源 
deb [arch=amd64 signed-by=/etc/apt/trusted.gpg.d/docker-archive-keyring.gpg] https://mirrors.aliyun.com/docker-ce/linux/debian bullseye stable
# 官方源
# deb [arch=amd64 signed-by=/etc/apt/trusted.gpg.d/docker-archive-keyring.gpg] https://download.docker.com/linux/debian bullseye stable
```



##### 安装

```shell
$ sudo apt update
$ sudo apt install docker-ce containerd.io
# 设置开机启动
$ sudo systemctl enable docker

# 启动 Docker
$ sudo systemctl start docker

# 检查启动状态
$ sudo systemctl status docker

# 验证安装是否成功
$ docker -v
$ docker info

# 建立 docker 组
$ sudo groupadd docker
# 将当前用户加入 docker 组
$ sudo usermod -aG docker $USER
```



##### 镜像加速

```shell
$ sudo mkdir -p /etc/docker
$ sudo vim /etc/docker/daemon.json
$ sudo systemctl daemon-reload
$ sudo systemctl restart docker
```

`/etc/docker/daemon.json`

```json
{
  "registry-mirrors": ["https://82m9ar63.mirror.aliyuncs.com"],
  "exec-opts": ["native.cgroupdriver=systemd"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m"
  },
  "storage-driver": "overlay2"
}
```



#### Node 节点证书

##### 生成 kube-proxy 证书和私钥

首先在 master 节点上，通过颁发的 CA 证书先创建好 Node 节点要使用的证书，先创建证书签名请求文件。

`/opt/apps/k8s/kubernetes/ssl/kube-proxy-csr.json`

```json
{
    "CN": "system:kube-proxy",
    "hosts": [],
    "key": {
        "algo": "rsa",
        "size": 2048
    },
    "names": [
        {
          "C": "CN",
          "ST": "Shanghai",
          "L": "Shanghai",
          "O": "kubernetes",
          "OU": "System"
        }
    ]
}
```

```shell
$ cd /opt/apps/k8s/kubernetes/ssl
$ sudo bash -c "cfssl gencert -ca=ca.pem -ca-key=ca-key.pem -config=ca-config.json -profile=kubernetes kube-proxy-csr.json | cfssljson -bare kube-proxy"
$ ls kube-proxy*
```



#### 创建相关文件和目录

```shell
$ mkdir -p /opt/apps/k8s/kubernetes/{bin,cfg,logs,ssl}

# 将 kubelet、kube-proxy 拷贝到 node 节点上
$ scp -r /usr/local/src/kubernetes/server/bin/{kubelet,kube-proxy} root@192.168.31.102:/opt/apps/k8s/kubernetes/bin/

# 将证书拷贝到 node 节点上
$ scp -r  /opt/apps/k8s/kubernetes/ssl/{ca.pem,kube-proxy.pem,kube-proxy-key.pem} root@192.168.31.102:/opt/apps/k8s/kubernetes/ssl/
```



#### 安装 kubelet

##### 配置

###### bootstrap.kubeconfig

bootstrap.kubeconfig 将用于向 apiserver 请求证书，apiserver 会验证 token、证书 是否有效，验证通过则自动颁发证书。

`/opt/apps/k8s/kubernetes/cfg/bootstrap.kubeconfig`

```json
apiVersion: v1
clusters:
- cluster: 
    certificate-authority: /opt/apps/k8s/kubernetes/ssl/ca.pem
    server: https://192.168.31.101:6443
  name: kubernetes
contexts:
- context:
    cluster: kubernetes
    user: kubelet-bootstrap
  name: default
current-context: default
kind: Config
preferences: {}
users:
- name: kubelet-bootstrap
  user:
    token: dd8dcf475a5f0568d99aaf939e525727


apiVersion: v1
clusters:
- cluster: 
    certificate-authority: /opt/apps/k8s/kubernetes/ssl/ca.pem
    server: https://192.168.31.101:6443
  name: kubernetes
contexts:
- context:
    cluster: kubernetes
    namespace: default
    user: kubelet-bootstrap
  name: kubelet-bootstrap@kubernetes
current-context: kubelet-bootstrap@kubernetes
kind: Config
preferences: {}
users:
- name: kubelet-bootstrap
  user:
    token: dd8dcf475a5f0568d99aaf939e525727
```



###### kubelet-config.yml

为了安全性，kubelet 禁止匿名访问，必须授权才可以，通过 kubelet-config.yml 授权 apiserver 访问 kubelet。

`/opt/apps/k8s/kubernetes/cfg/kubelet-config.yml`

```yml
kind: KubeletConfiguration
apiVersion: kubelet.config.k8s.io/v1beta1
address: 0.0.0.0
port: 10250
readOnlyPort: 10255
cgroupDriver: cgroupfs
clusterDNS:
- 10.0.0.2 
clusterDomain: cluster.local
failSwapOn: false
authentication:
  anonymous:
    enabled: false
  webhook:
    cacheTTL: 2m0s
    enabled: true
  x509: 
    clientCAFile: /opt/apps/k8s/kubernetes/ssl/ca.pem
authorization:
  mode: Webhook
  webhook:
    cacheAuthorizedTTL: 5m0s
    cacheUnauthorizedTTL: 30s
evictionHard:
  imagefs.available: 15%
  memory.available: 100Mi
  nodefs.available: 10%
  nodefs.inodesFree: 5%
maxOpenFiles: 100000
maxPods: 110
```



###### kubelet.conf

详细的配置可参考官方文档：[kubelet ](https://kubernetes.io/docs/reference/command-line-tools-reference/kubelet/)

`/opt/apps/k8s/kubernetes/cfg/kubelet.conf`

```conf
KUBELET_OPTS="--hostname-override=server02 \
  --cgroups-per-qos=false \
  --enforce-node-allocatable="" \
  --kubeconfig=/opt/apps/k8s/kubernetes/cfg/kubelet.kubeconfig \
  --bootstrap-kubeconfig=/opt/apps/k8s/kubernetes/cfg/bootstrap.kubeconfig \
  --config=/opt/apps/k8s/kubernetes/cfg/kubelet-config.yml \
  --cert-dir=/opt/apps/k8s/kubernetes/ssl \
  --pod-infra-container-image=kubernetes/pause:latest \
  --v=2"
```



##### 创建 kubelet 服务

`/usr/lib/systemd/system/kubelet.service`

```
[Unit]
Description=Kubernetes Kubelet
After=docker.service
Before=docker.service

[Service]
EnvironmentFile=/opt/apps/k8s/kubernetes/cfg/kubelet.conf
ExecStart=/opt/apps/k8s/kubernetes/bin/kubelet $KUBELET_OPTS
Restart=on-failure
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
```



##### 启动 kubelet

```shell
$ sudo systemctl daemon-reload
# 设置开机启动
$ sudo systemctl enable kubelet

# 启动服务
$ sudo systemctl start kubelet

# 检查启动状态
$ sudo systemctl status kubelet

# 查看启动日志
$ sudo journalctl -u kubelet.service -e


# 报错
"command failed" err="failed to run Kubelet: validate service connection: validate CRI v1 runtime API for endpoint \"unix:///run/containerd/containerd.sock\": rpc error: code = Unimplemented desc = unknown service runtime.v1.RuntimeService"
# 解决方法：删除 /etc/containerd/config.toml 后 sudo systemctl restart containerd

```



#### Master 给 Node 授权

[管理集群中的 TLS 认证 | Kubernetes](https://kubernetes.io/zh-cn/docs/tasks/tls/managing-tls-in-a-cluster/)

[K8s二进制部署单节点 master组件 node组件 ——头悬梁 - 隐姓埋名4869 - 博客园 (cnblogs.com)](https://www.cnblogs.com/lvrui/p/15477149.html)

[自签证书 · k8s基础搭建 · 看云 (kancloud.cn)](https://www.kancloud.cn/linux_qfc/k8s_/754593)

[IT苦工指南 | Kubernetes v1.8.x全手动安装 - 知乎 (zhihu.com)](https://zhuanlan.zhihu.com/p/37181324?utm_id=0)

kubelet 启动后，还没加入到集群中，会向 apiserver 请求证书，需手动在 master 上对 node 授权。

```shell
# 查看是否有新的客户端请求颁发证书
$ sudo kubectl get csr

# 批准证书签名请求
$ sudo kubectl certificate approve node-csr-1f1_mtv3fG_L-oYVsEZ0jMj4KZY21pqmTyEOkI1DAEY

# 颁发证书
$ cd /opt/apps/k8s/kubernetes/ssl
$ sudo bash -c 'cat <<EOF | cfssl genkey - | cfssljson -bare server
{
  "hosts": [
  	"127.0.0.1",
	"10.0.0.1",
    "192.168.31.101",
    "192.168.31.102",
    "192.168.31.103",
    "kubernetes",
    "kubernetes.default",
    "kubernetes.default.svc",
    "kubernetes.default.svc.cluster",
    "kubernetes.default.svc.cluster.local"
  ],
  "CN": "kubernetes",
  "key": {
    "algo": "ecdsa",
    "size": 256
  }
}
EOF'

$ sudo bash -c "cat <<EOF | kubectl apply \
--kubeconfig=$HOME/.kube/config \
--context=kubelet-bootstrap@kubernetes \
-f -
apiVersion: certificates.k8s.io/v1
kind: CertificateSigningRequest
metadata:
  name: node-csr-omYpHO_QHRs8DvIm_EqbAAm8_if-Y2vl9KZz5lTYB0g
spec:
  request: $(cat server.csr | base64 | tr -d '\n')
  signerName: kubernetes.io/kube-apiserver-client-kubelet
  usages:
  - digital signature
  - key encipherment
  - server auth
EOF"

$ sudo kubectl certificate approve node-csr-omYpHO_QHRs8DvIm_EqbAAm8_if-Y2vl9KZz5lTYB0g \
  --kubeconfig=$HOME/.kube/config \
  --context=kubelet-bootstrap@kubernetes

$ sudo kubectl get csr \
  --kubeconfig=$HOME/.kube/config \
  --context=kubelet-bootstrap@kubernetes

$ sudo vim server-signing-config.json
{
  "signing": {
    "default": {
      "usages": [
        "digital signature",
        "key encipherment",
        "server auth"
      ],
      "expiry": "876000h",
      "ca_constraint": {
        "is_ca": false
      }
    }
  }
}

$ sudo bash -c "kubectl get csr node-csr-omYpHO_QHRs8DvIm_EqbAAm8_if-Y2vl9KZz5lTYB0g \
  --kubeconfig=$HOME/.kube/config \
  --context=kubelet-bootstrap@kubernetes \
  -o jsonpath='{.spec.request}' | \
  base64 --decode | \
  cfssl sign -ca ca.pem -ca-key ca-key.pem -config server-signing-config.json - | \
  cfssljson -bare ca-signed-server"
  
$ sudo bash -c "kubectl get csr node-csr-omYpHO_QHRs8DvIm_EqbAAm8_if-Y2vl9KZz5lTYB0g \
  --kubeconfig=$HOME/.kube/config \
  --context=kubelet-bootstrap@kubernetes \
  -o json | \
  jq '.status.certificate = "'$(base64 ca-signed-server.pem | tr -d '\n')'"' | \
  kubectl replace --raw /apis/certificates.k8s.io/v1/certificatesigningrequests/node-csr-omYpHO_QHRs8DvIm_EqbAAm8_if-Y2vl9KZz5lTYB0g/status -f -"

# 这使用命令行工具 jq 在 .status.certificate 字段中填充 base64 编码的内容。 如果你没有 jq 工具，你还可以将 JSON 输出保存到文件中，手动填充此字段，然后上传结果文件
# sudo bash -c "kubectl get csr node-csr-omYpHO_QHRs8DvIm_EqbAAm8_if-Y2vl9KZz5lTYB0g  \
  --kubeconfig=$HOME/.kube/config \
  --context=kubelet-bootstrap@kubernetes \
  -o json > csr.status.certificate.json"
# sudo echo $(base64 ca-signed-server.pem | tr -d '\n')
# sudo bash -c "cat csr.status.certificate.json | kubectl replace --raw /apis/certificates.k8s.io/v1/certificatesigningrequests/node-csr-omYpHO_QHRs8DvIm_EqbAAm8_if-Y2vl9KZz5lTYB0g/status \
  --kubeconfig=$HOME/.kube/config \
  --context=kubelet-bootstrap@kubernetes \
  -f -"
  
$ sudo bash -c "kubectl get csr node-csr-omYpHO_QHRs8DvIm_EqbAAm8_if-Y2vl9KZz5lTYB0g \
  --kubeconfig=$HOME/.kube/config \
  --context=kubelet-bootstrap@kubernetes \
  -o jsonpath='{.status.certificate}' \
    | base64 --decode > server.crt"

$ sudo kubectl create secret tls server \
  --kubeconfig=$HOME/.kube/config \
  --context=kubelet-bootstrap@kubernetes \
  --cert server.crt --key server-key.pem
kubernetes-key.pem

$ sudo vim server-signing-csr.json
{
  "CN": "kubernetes",
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "ST": "Shanghai",
      "L": "Shanghai",
      "O": "kubernetes",
      "OU": "System"
    }
  ],
  "hosts": [],
  "ca": {
    "expiry": "87600h"
  }
}

$ sudo bash -c "kubectl get csr node-csr-omYpHO_QHRs8DvIm_EqbAAm8_if-Y2vl9KZz5lTYB0g -o jsonpath='{.spec.request}' | \
  base64 --decode | \
  cfssl sign -ca=ca.pem -ca-key=ca-key.pem -config=server-signing-config.json -profile=server-signing server-signing-csr.json | \
  cfssljson -bare server-signing"
# 2024/07/24 23:20:08 [INFO] signed certificate with serial number 431187033489866991912280955242051011460961436566
# 这会生成一个签名的服务证书文件，ca-signed-server.pem

$ sudo bash -c "cfssl gencert -ca=ca.pem -ca-key=ca-key.pem -config=ca-config.json -profile=server-signing server-signing-csr.json | cfssljson -bare server-signing"

# 上传签名证书
# 最后，在 API 对象的状态中填充签名证书：
$ sudo kubectl get csr node-csr-1f1_mtv3fG_L-oYVsEZ0jMj4KZY21pqmTyEOkI1DAEY -o json | \
  jq '.status.certificate = "'$(base64 ca-signed-server.pem | tr -d '\n')'"' | \
  kubectl replace --raw /apis/certificates.k8s.io/v1/certificatesigningrequests/node-csr-1f1_mtv3fG_L-oYVsEZ0jMj4KZY21pqmTyEOkI1DAEY/status -f -
# 这使用命令行工具 jq 在 .status.certificate 字段中填充 base64 编码的内容。 如果你没有 jq 工具，你还可以将 JSON 输出保存到文件中，手动填充此字段，然后上传结果文件。


# 批准 CSR 并上传签名证书后，运行：
$ sudo kubectl get csr
# 输入类似于：
#NAME                                                   AGE   SIGNERNAME                                    REQUESTOR           REQUESTEDDURATION   CONDITION
#node-csr-1f1_mtv3fG_L-oYVsEZ0jMj4KZY21pqmTyEOkI1DAEY   47h   kubernetes.io/kube-apiserver-client-kubelet   kubelet-bootstrap   <none>              Approved,Issued

$ sudo bash -c "kubectl get csr node-csr-1f1_mtv3fG_L-oYVsEZ0jMj4KZY21pqmTyEOkI1DAEY \
    -o jsonpath='{.status.certificate}' \
    | base64 --decode > server.crt"
    
# 现在你可以将 server.crt 和 server-key.pem 填充到 Secret 中， 稍后你可以将其挂载到 Pod 中（例如，用于提供 HTTPS 的网络服务器）。
$ sudo kubectl create secret tls server \
  --cert server.crt --key kubernetes-key.pem

# 查看 node 是否加入集群(此时的 node 还处于未就绪的状态，因为还没有安装 CNI 组件)
$ sudo kubectl get node

# 颁发证书后，可以在 /opt/apps/k8s/kubernetes/ssl 下看到 master 为 kubelet 颁发的证书
$ ls /opt/apps/k8s/kubernetes/ssl/kubelet*
# kubelet-client-current.pem kublet.crt kubelet.key
# 在 /opt/apps/k8s/kubernetes/cfg 下可以看到自动生成的 kubelet.kubeconfig 配置文件


$ sudo kubectl delete csr node-csr-omYpHO_QHRs8DvIm_EqbAAm8_if-Y2vl9KZz5lTYB0g \
  --kubeconfig=$HOME/.kube/config
```



#### 安装 kube-proxy

##### 配置

###### kube-proxy.kubeconfig

`/opt/apps/k8s/kubernetes/cfg/kube-proxy.kubeconfig`

```conf
apiVersion: v1
clusters:
- cluster:
    certificate-authority: /opt/apps/k8s/kubernetes/ssl/ca.pem
    server: https://192.168.31.101:6443
  name: kubernetes
contexts:
- context:
    cluster: kubernetes
    user: kube-proxy
  name: default
current-context: default
kind: Config
preferences: {}
users:
- name: kube-proxy
  user:
    client-certificate: /opt/apps/k8s/kubernetes/ssl/kube-proxy.pem
    client-key: /opt/apps/k8s/kubernetes/ssl/kube-proxy-key.pem
```



###### kube-proxy-config.yml

`/opt/apps/k8s/kubernetes/cfg/kube-proxy-config.yml`

```yml
kind: KubeProxyConfiguration
apiVersion: kubeproxy.config.k8s.io/v1alpha1
address: 0.0.0.0
metrisBindAddress: 0.0.0.0:10249
clientConnection:
  kubeconfig: /opt/apps/k8s/kubernetes/cfg/kube-proxy.kubeconfig
hostnameOverride: server02
clusterCIDR: 10.0.0.0/24
mode: ipvs
ipvs:
  scheduler: "rr"
iptables:
  masqueradeAll: true
```



###### kube-proxy.conf

详细的配置可参考官方文档：[kube-proxy](https://kubernetes.io/docs/reference/command-line-tools-reference/kube-proxy/)

`/opt/apps/k8s/kubernetes/cfg/kube-proxy.conf`

```conf
KUBE_PROXY_OPTS="--config=/opt/apps/k8s/kubernetes/cfg/kube-proxy-config.yml \
  --v=2"
```



##### 创建 kube-proxy 服务

`/usr/lib/systemd/system/kube-proxy.service`

```
[Unit]
Description=Kubernetes Proxy
After=network.target

[Service]
EnvironmentFile=/opt/apps/k8s/kubernetes/cfg/kube-proxy.conf
ExecStart=/opt/apps/k8s/kubernetes/bin/kube-proxy $KUBE_PROXY_OPTS
Restart=on-failure
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
```



##### 启动 kube-proxy

```shell
$ sudo systemctl daemon-reload
# 设置开机启动
$ sudo systemctl enable kube-proxy

# 启动服务
$ sudo systemctl start kube-proxy

# 检查启动状态
$ sudo systemctl status kube-proxy

# 查看启动日志
$ sudo journalctl -u kube-proxy.service -e
```



### 部署K8S容器集群网络(Flannel)





[手把手教你部署一个最小化的 Kubernetes 集群 (qq.com)](https://mp.weixin.qq.com/s/MEopULKBhvejRiEfLXW3jw)

[Debian11安装k8s - scmie - 博客园 (cnblogs.com)](https://www.cnblogs.com/scmie/articles/18222941)

## K8s集群搭建

```shell
# 配置kubeadm的阿里云镜像源
$ sudo vim /etc/apt/sources.list
deb https://mirrors.aliyun.com/kubernetes/apt kubernetes-xenial main
$ sudo gpg --keyserver keyserver.debian.com --recv-keys BA07F4FB
$ sudo gpg --export --armor BA07F4FB | sudo apt-key add -

# 配置docker安装
# 阿里源
curl -fsSL https://mirrors.aliyun.com/docker-ce/linux/debian/gpg | sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/docker-archive-keyring.gpg
# 官方源
# $ curl -fsSL https://download.docker.com/linux/debian/gpg | sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/docker-archive-keyring.gpg
# 本地 key
# $ cat /home/young/docker.gpg | sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/docker-archive-keyring.gpg

# $ sudo gpg --recv-keys 0EBFCD88
# $ sudo gpg --fingerprint 0EBFCD88
# $ sudo gpg --fingerprint /etc/apt/trusted.gpg.d/docker.gpg

$ sudo vim /etc/apt/sources.list
# 阿里源 
deb [arch=amd64 signed-by=/etc/apt/trusted.gpg.d/docker-archive-keyring.gpg] https://mirrors.aliyun.com/docker-ce/linux/debian bullseye stable
# 官方源
# deb [arch=amd64 signed-by=/etc/apt/trusted.gpg.d/docker-archive-keyring.gpg] https://download.docker.com/linux/debian bullseye stable

sudo apt update
sudo apt install docker-ce docker-ce-cli containerd.io
# 启动 Docker
sudo systemctl enable docker
sudo systemctl start docker

# 建立 docker 组
$ sudo groupadd docker
# 将当前用户加入 docker 组
$ sudo usermod -aG docker $USER
# 测试 Docker 是否安装正确
$ sudo docker run --rm hello-world

# 镜像加速
# 您可以通过修改daemon配置文件/etc/docker/daemon.json来使用加速器
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://82m9ar63.mirror.aliyuncs.com"],
  "exec-opts": ["native.cgroupdriver=systemd"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m"
  },
  "storage-driver": "overlay2"
}
EOF

sudo systemctl daemon-reload
sudo systemctl restart docker
```



### kubernetes 环境配置

```shell
# 关闭swap
$ sudo swapoff -a
$ sudo sed -ri 's/.*swap.*/#&/' /etc/fstab

# 同步各节点的时区
# 设置系统时区为中国/上海
$ timedatectl set-timezone Asia/Shanghai

# 关闭各节点的防火墙
$ sudo systemctl disable nftables.service 
$ sudo systemctl stop nftables.service 
$ sudo systemctl status nftables.service

# 设置rp_filter的值
$ cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
EOF
```



### 安装 kubeadm（所有节点都需要安装）

```shell
$ wget https://github.com/Mirantis/cri-dockerd/releases/download/v0.3.14/cri-dockerd-0.3.14.amd64.tgz
tar xf cri-dockerd-0.3.14.amd64.tgz
$ sudo cp cri-dockerd/cri-dockerd /usr/bin/
$ sudo rm -r cri-dockerd

# 配置启动⽂件，执行如下命令
$ sudo cat <<"EOF" > /usr/lib/systemd/system/cri-docker.service
[Unit]
Description=CRI Interface for Docker Application Container Engine
Documentation=https://docs.mirantis.com
After=network-online.target firewalld.service docker.service
Wants=network-online.target
Requires=cri-docker.socket

[Service]
Type=notify
ExecStart=/usr/bin/cri-dockerd --network-plugin=cni --pod-infra-container-image=registry.cn-hangzhou.aliyuncs.com/google_containers/pause:3.9
ExecReload=/bin/kill -s HUP $MAINPID
TimeoutSec=0
RestartSec=2
Restart=always

# Note that StartLimit* options were moved from "Service" to "Unit" in systemd 229.
# Both the old, and new location are accepted by systemd 229 and up, so using the old location
# to make them work for either version of systemd.
StartLimitBurst=3

# Note that StartLimitInterval was renamed to StartLimitIntervalSec in systemd 230.
# Both the old, and new name are accepted by systemd 230 and up, so using the old name to make
# this option work for either version of systemd.
StartLimitInterval=60s


# Having non-zero Limit*s causes performance problems due to accounting overhead
# in the kernel. We recommend using cgroups to do container-local accounting.
LimitNOFILE=infinity
LimitNPROC=infinity
LimitCORE=infinity

# Comment TasksMax if your systemd version does not support it.
# Only systemd 226 and above support this option.
TasksMax=infinity
Delegate=yes
KillMode=process

[Install]
WantedBy=multi-user.target
EOF

# ⽣成 socket ⽂件，执行如下命令
$ sudo cat <<"EOF" > /usr/lib/systemd/system/cri-docker.socket
[Unit]
Description=CRI Docker Socket for the API
PartOf=cri-docker.service

[Socket]
ListenStream=%t/cri-dockerd.sock
SocketMode=0660
SocketUser=root
SocketGroup=docker

[Install]
WantedBy=sockets.target
EOF

# 启动 cri-docker 并设置开机⾃动启动
$ sudo systemctl daemon-reload
$ sudo systemctl enable cri-docker --now
$ sudo systemctl is-active cri-docker
```



### 安装 kubeadm、kubelet 和 kubectl

```shell
# 安装使用 Kubernetes 存储库所需的包
$ sudo apt update
$ sudo apt install -y apt-transport-https ca-certificates curl gpg

# 下载 Kubernetes 软件包存储库的公共签名密钥。 所有存储库都使用相同的签名密钥，因此您可以忽略 URL 中的版本
# sudo mkdir -p -m 755 /etc/apt/keyrings
$ curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.29/deb/Release.key | sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/kubernetes-apt-keyring.gpg

# 添加相应的 Kubernetes 存储库。请注意，此存储库包含软件包 仅适用于 Kubernetes 1.29;对于其他 Kubernetes 次要版本，您需要 更改 URL 中的 Kubernetes 次要版本以匹配所需的次要版本 （您还应该检查是否正在阅读 Kubernetes 版本的文档 您计划安装）
$ echo 'deb [signed-by=/etc/apt/trusted.gpg.d/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.29/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list

# 更新软件包索引，安装 kubelet、kubeadm 和 kubectl，并固定其版本
$ sudo apt update
$ sudo apt install -y kubelet kubeadm kubectl
$ sudo apt-mark hold kubelet kubeadm kubectl

# (可选）在运行 kubeadm 之前启用 kubelet 服务
$ sudo systemctl enable --now kubelet

/var/lib/kubelet/config.yaml
```



#### 下载各个机器需要的镜像

```shell
# 查看k8s所需的镜像版本
kubeadm config images list

# 下载k8s所需的镜像
docker pull registry.cn-hangzhou.aliyuncs.com/google_containers/kube-apiserver:v1.29.6
docker pull registry.cn-hangzhou.aliyuncs.com/google_containers/kube-controller-manager:v1.29.6
docker pull registry.cn-hangzhou.aliyuncs.com/google_containers/kube-scheduler:v1.29.6
docker pull registry.cn-hangzhou.aliyuncs.com/google_containers/kube-proxy:v1.29.6
docker pull registry.cn-hangzhou.aliyuncs.com/google_containers/coredns:v1.11.1
docker pull registry.cn-hangzhou.aliyuncs.com/google_containers/pause:3.9
docker pull registry.cn-hangzhou.aliyuncs.com/google_containers/etcd:3.5.12-0

# 查看是否全部下载（没有下载的单独下载）7个
docker images
```



### 配置 cri-dockerd

```shell
# 上面配置过了查看一下/usr/lib/systemd/system/cri-docker.service的--pod-infra-container-image是不是下载的镜像
# cat /usr/lib/systemd/system/cri-docker.service | grep registry.cn-hangzhou.aliyuncs.com/google_containers/pause:3.9

#重启
sudo systemctl daemon-reload && sudo systemctl restart cri-docker

#检查效果
cri-dockerd --version
```



### 集群搭建

#### 集群初始化（Master节点执行）

```shell
# cat /etc/hosts
sudo kubeadm reset --cri-socket=unix:///var/run/cri-dockerd.sock
$ sudo kubeadm init \
--apiserver-advertise-address=192.168.31.101 \
--control-plane-endpoint=server01 \
--image-repository=registry.cn-hangzhou.aliyuncs.com/google_containers \
--kubernetes-version=v1.29.4 \
--service-cidr=10.96.0.0/16 \
--pod-network-cidr=10.114.0.0/16 \
--cri-socket=unix:///var/run/cri-dockerd.sock \
--upload-certs

[kubelet-check] The HTTP call equal to 'curl -sSL http://localhost:10248/healthz' failed with error: Get "http://localhost:10248/healthz": dial tcp [::1]:10248: connect: connection refused.

cat /etc/docker/daemon.json
{

}

# 执行下面三行代码让当前用户能使用集群命令
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

# 安装网络插件calico(可以使用其他的)，参数信息见下面说明
#下面网站查看最新calico部署文件
# https://docs.projectcalico.org/manifests/calico.yaml

#下载到文件中
$ curl https://calico-v3-25.netlify.app/archive/v3.25/manifests/calico.yaml -O  >> calico.yaml

#使用kubectl命令安装
$ kubectl apply -f calico.yaml

This error is likely caused by:
	- The kubelet is not running
```



#### 工作节点加入集群

```shell
# 忘记加入集群的命令可以用这个命令获取
$ kubeadm token create --print-join-command

# 将从节点加入集群，在从节点执行
$ sudo kubeadm join server01:6443 \
--token 98ci8i.4lpdtalg6y6g7fi4 \
--discovery-token-ca-cert-hash sha256:67a3153af0ff19f8c5840947a37145abaad8060e084f5514ad4d541868dd0406 \
--cri-socket=unix:///var/run/cri-dockerd.sock

# 加入成功会出现
# Run 'kubectl get nodes' on the control-plane to see this node join the cluster.

# 遇到以下错误
# root@k8s-node-2:~# kubeadm join k8s-master-1:6443 --token 0yncwd.j7xvheb2phj6o8q6 --discovery-token-ca-cert-hash sha256:9c98ae6274330d6c3db163d582e707e6fae8b51883cbbec0d0f3c13fefb1c85a 
Found multiple CRI endpoints on the host. Please define which one do you wish to use by setting the 'criSocket' field in the kubeadm configuration file: unix:///var/run/containerd/containerd.sock, unix:///var/run/cri-dockerd.sock
To see the stack trace of this error execute with --v=5 or higher

# 原因：由于没有指定cri的套接字
# 解决: 在join命令后加上以下选项
# --cri-socket unix:///var/run/cri-dockerd.sock 
```



## 安装nfs

### 安装配置nfs服务器

```shell
# 安装nfs-kernel-server
$ sudo apt update
$ sudo apt install -y nfs-kernel-server nfs-common 

# 创建导出目录
# 导出目录是用于与nfs客户端共享的目录，这个目录可以是linux上的任意目录。这里我们使用一个创建的新目录。
$ sudo mkdir -p /mnt/basic-service-delete/
$ sudo mkdir -p /mnt/basic-service-reserve/
$ sudo mkdir -p /mnt/business-service-delete/
$ sudo mkdir -p /mnt/data/

# 后边两步非常关键，如果没有这两步，可能导致其它客户端连接后出现访问禁止的错误
sudo chown nobody:nogroup /mnt/basic-service-reserve/
sudo chmod 777 /mnt/basic-service-reserve/

sudo chown nobody:nogroup /mnt/basic-service-delete/
sudo chmod 777 /mnt/basic-service-delete/


sudo chown nobody:nogroup /mnt/business-service-delete/
sudo chmod 777 /mnt/business-service-delete/

sudo chown nobody:nogroup /mnt/data/
sudo chmod 777 /mnt/data/

# 执行以下就可以
sudo chmod 777 /mnt/basic-service-reserve/
sudo chmod 777 /mnt/basic-service-delete/
sudo chmod 777 /mnt/business-service-delete/
sudo chmod 777 /mnt/data/

# 通过nfs输出文件为客户端分配服务器访问权限
# 在master 执行以下命令
echo "/mnt/basic-service-delete/ *(insecure,rw,sync,no_root_squash,no_subtree_check)
/mnt/basic-service-reserve/ *(insecure,rw,sync,no_root_squash,no_subtree_check)
/mnt/business-service-delete/ *(insecure,rw,sync,no_root_squash,no_subtree_check)
/mnt/data/ *(insecure,rw,sync,no_root_squash,no_subtree_check)" > /etc/exports

$ sudo systemctl enable rpcbind
$ sudo systemctl enable nfs-server
$ sudo systemctl start rpcbind
$ sudo systemctl start nfs-server

# 输出共享目录
$ sudo exportfs -r

# 检查配置是否生效
$ sudo exportfs

# 防火墙放行
# NFS服务使用的111和2049端口是固定的，mountd端口是动态的，需要固定，然后在防火墙放行。
$ sudo vim /etc/sysconfig/nfs
#追加端口配置
MOUNTD_PORT=4001　　
STATD_PORT=4002
LOCKD_TCPPORT=4003
LOCKD_UDPPORT=4003
RQUOTAD_PORT=4004

sudo iptables -A INPUT -p tcp --dport 2049 -j ACCEPT
sudo iptables -A INPUT -p udp --dport 2049 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport  111 -j ACCEPT
sudo iptables -A INPUT -p udp --dport 111 -j ACCEPT
#重载配置
sudo exportfs -ra

# 从节点安装nfs 客户端
# 安装NFS客户端软件包
sudo apt install nfs-common

# 所有从节点执行
sudo showmount -e 192.168.31.101
sudo mkdir -p /mnt/basic-service-delete/
sudo mkdir -p /mnt/basic-service-reserve/
sudo mkdir -p /mnt/business-service-delete/
sudo mkdir -p /mnt/data/

sudo mount -t nfs 192.168.31.101:/mnt/basic-service-delete/ /mnt/basic-service-delete/
sudo mount -t nfs 192.168.31.101:/mnt/basic-service-reserve/ /mnt/basic-service-reserve/
sudo mount -t nfs 192.168.31.101:/mnt/business-service-delete/ /mnt/business-service-delete/
sudo mount -t nfs 192.168.31.101:/mnt/data/ /mnt/data/

# NFS动态供给
# 直接通过命令下载：
$ sudo wget https://github.com/kubernetes-sigs/nfs-subdir-external-provisioner/archive/refs/tags/nfs-subdir-external-provisioner-4.0.18.tar.gz
# 直接解压它
sudo tar -zxvf nfs-subdir-external-provisioner-4.0.18.tar.gz
# 来到这个文件夹下的deploy目录
cd nfs-subdir-external-provisioner-nfs-subdir-external-provisioner-4.0.18/deploy/

# 需要修改 deployment.yaml
$ sudo vim deployment.yaml
#我已经通过一些方法将它拉取下来并且上传到了国内的阿里云镜像仓库，我们可以直接用下面这个镜像来替换：
# 这个镜像是在谷歌上的，国内拉取不到
# image: registry.k8s.io/sig-storage/nfs-subdir-external-provisioner:v4.0.2
# 使用这个我先在谷歌上拉取下来再上传到阿里云上的镜像
# image: registry.cn-hangzhou.aliyuncs.com/lfy_k8s_images/nfs-subdir-external-provisioner:v4.0.2

yamls=$(grep -rl 'namespace: default' ./)
for yaml in ${yamls}; do
  echo ${yaml}
  cat ${yaml} | grep 'namespace: default'
done

#我们可以新创建一个命名空间专门装这个驱动，也方便以后管理，所以我决定创建一个名为 nfs-provisioner 命名空间，为了方便就不用yaml文件了，直接通过命令创建：
sudo kubectl create namespace nfs-provisioner

#涉及命名空间这个配置的文件还挺多的，所以我们干脆通过一行脚本更改所有：
sed -i 's/namespace: default/namespace: base-cluster/g' `grep -rl 'namespace: default' ./`

yamls=$(grep -rl 'namespace:' ./)
for yaml in ${yamls}; do
  echo ${yaml}
  cat ${yaml} | grep 'namespace:'
done

#直接执行安装
kubectl apply -k .
kubectl delete -k .

kubectl get pod -n base-cluster
```

