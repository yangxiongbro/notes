# Docker

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