# JVM

## 简介

### 什么是JVM

Java Virtual Machine - java 程序的运行环境（java 二进制字节码的运行环境）

### 好处

- 一次编写，到处运行
- 自动内存管理，垃圾回收功能
- 数组下标越界检查
- 多态
- ...

### jvm jre jdk 比较

![](D:\documents\notes\solid\Java.assets\001.PNG)



### 常见的JVM

![](D:\documents\notes\solid\Java.assets\002.PNG)



### JVM 基本结构

![](D:\documents\notes\solid\Java.assets\003.PNG)



## 内存结构

### 程序计数器（PC Register）

#### 定义

Program Counter Register 程序计数器（寄存器）

#### 特点

- 线程私有
- 不会内存溢出

#### 作用

记住下一条jvm指令的执行地址

![](D:\documents\notes\solid\Java.assets\004.PNG)



### 虚拟机栈（JVM Stacks）

#### 定义

Java Virtual Machine Stacks （Java 虚拟机栈）

- 每个线程运行时所需的内存，称为虚拟机栈
- 每个栈由多个栈帧（Frame）组成，对应每次方法调用时所占用的内存
- 每个线程只能有一个活动的栈帧，对应当前正在执行的那个方法

栈帧：每个方法运行时需要的内存（参数、局部变量、返回地址）

![](D:\documents\notes\solid\Java.assets\005.PNG)



#### 特点

- 线程私有

#### 相关 JVM 参数

-Xss size

Linux/x64(64-bit): 1024 KB

macOS(64-bit): 1024 KB

Oracle Solaris/x64(64-bit): 1024 KB

Windows: 根据虚拟内存决定



#### 栈内存溢出

##### 栈帧过多导致栈内存溢出

递归没有正确设置递归出口、类循环引用时进行序列化

##### 栈帧过大



#### 线程运行诊断

##### CPU占用过多

```sh
nohup java -jar test.jar > runoob.log 2>&1 &
# 2>&1 解释：

# 将标准错误 2 重定向到标准输出 &1 ，标准输出 &1 再被重定向输入到 runoob.log 文件中。

# 0 – stdin (standard input，标准输入)
# 1 – stdout (standard output，标准输出)
# 2 – stderr (standard error，标准错误输出)
top
# 获取PID
ps H -eo pid, tid, %cpu | grep PID
# 通过PID获取线程
jstack tid
# 通过 java 工具获取线程信息
# 将tid转16进制，通过匹配 jstack 输出的 nid 定位到对应线程，查看对应占用cpu过多的源码
```



##### 程序长时间无响应

**死锁**

通过 jstack 获取死锁信息



#### 问题辨析

垃圾回收是否涉及栈内存？

不涉及，方法调用结束栈帧出栈后自动释放栈帧内存。

栈内存分配越大越好吗？

不一定，栈内存和线程数成反比，栈内存越大线程数越少。

方法内的局部变量是否线程安全？

如果方法内局部变量没有逃离方法的作用范围，则是线程安全的。

如果局部变量引用了对象，并逃离方法的作用范围，则需要考虑线程安全问题。如果返回的是基本数据类型则没有线程安全问题。



### 本地方法栈（Native Method Stacks）

#### 定义

本地方法运行需要的内存

#### 特点

- 线程私有

### 堆（Heap）

#### 定义

通过 new 关键字创建对象都会使用堆内存

#### 特点

- 它是线程共享的，堆中对象都需要考虑线程安全问题
- 有垃圾回收机制

#### 堆内存溢出

java.lang.OutOfMemoryError:  GC overhead limit exceeded

垃圾回收器花费超过 98% 的时间只回收了不到 %2 的堆空间时将报这个错。使用JVM参数 -XX:-UseGCOverheadLimit 关闭该错误。

java.lang.OutOfMemoryError: Java heap space

堆空间内存不足错误。





#### 相关 JVM 参数

-Xmx size 设置最大堆大小

-Xmx8m

-Xms size 设置初始堆大小

-Xms8m



#### 堆内存诊断

##### jps

查看当前系统中有哪些 java 进程。



##### jmap

查看堆内存占用情况，只能查询某个时刻的堆内存信息。

```sh
jps
# 查看 java 进程
jmap -heap pid
# 查看堆内存信息
```



##### jconsole

图形界面，多功能的监测工具，可以连续监测。

```sh
jconsole
# 连接到启动的应用
```



##### jvisualvm

堆 Dump 保存快照，查找分析占用内存过多的对象。

```sh
jvisualvm
# 连接到启动的应用
```



### 方法区（Method Area）

#### 定义

通过 new 关键字创建对象都会使用堆内存

存放类结构相关信息，例如运行时常量池，成员变量，方法数据，方法和构造函数的代码，特殊方法（类构造器）。

方法区在JVM启动时被创建，逻辑上是堆的一部分

HotSpot 在 java 1.8之前的永久代和 1.8 之后的元空间都是方法区的一种实现。

内存不足时抛出内存不足异常。

#### 组成

HotSpot 方法区实现:

![](D:\documents\notes\solid\Java.assets\006.PNG)



#### 方法区内存溢出

##### 例子

1.8 之前

-XX:MaxPermSize=8m

1.8 之后

-XX:MaxMetaspaceSize=8m

```java
public class Demo extends ClassLoader{ // 可以用来加载类的二进制字节码
    public static void main(String[] args){
        int j = 0;
        try{
            Demo demo = new Demo();
            for(int i = 0; i < 20000; i++, j++){
                // ClassWriter 作用是生成类的二进制字节码
                ClassWriter cw = new ClassWriter(0);
                // 版本号，类修饰符，类名，包名，父类，接口
                cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Class"+i,null,"lava/lang/Object", null);
                // 类的二进制字节码
                byte[] code = cw.toByteArray();
                // 执行类加载
                test.defineClass("Class"+i, code,0,code.length);
            }
        } finally{
            System.out.println(j);
        }
    }
}

// java.lang.OutOfMemoryError: PermGen space
// java.lang.OutOfMemoryError: Metaspace
```



##### 出现场景

- spring
- mybatis

使用 CGLib 生成代理类，mybatis 接口实现类等



#### 相关 JVM 参数

设置方法区大小

1.8 之前

-XX:MaxPermSize=size

-XX:MaxPermSize=8m

1.8 之后

-XX:MaxMetaspaceSize=size

-XX:MaxMetaspaceSize=8m



#### 运行时常量池

- 常量池，就是一张表，虚拟机指令根据这张常量表找到要执行的类名、方法名、参数类型、字面量等信息
- 运行时常量池，常量池是 *.class 文件中的，当该类被加载，它的常量池信息就会被放入运行时常量池，并把里面的符号地址变为真实地址。

```java
// 二进制字节码（类基本信息，常量池，类方法定义，包含了虚拟机指令）
public class JavaApp {
    public static void main(String [] args){
        System.out.println("hello world");
    }
}

// 反编译二进制字节码
// PS D:\develop\code\java\jvm\out\production\jvm> javap -v .\JavaApp.class
// Classfile /D:/develop/code/java/jvm/out/production/jvm/JavaApp.class
//   Last modified 2022年6月27日; size 524 bytes
//   MD5 checksum 7c885a2b9385027997581b72fac41fd9
//   Compiled from "JavaApp.java"
//类基本信息
// public class JavaApp
//   minor version: 0
//   major version: 55
//   flags: (0x0021) ACC_PUBLIC, ACC_SUPER
//   this_class: #5                          // JavaApp
//   super_class: #6                         // java/lang/Object
//   interfaces: 0, fields: 0, methods: 2, attributes: 1
//常量池
// Constant pool:
//    #1 = Methodref          #6.#20         // java/lang/Object."<init>":()V
//    #2 = Fieldref           #21.#22        // java/lang/System.out:Ljava/io/PrintStream;
//    #3 = String             #23            // hello world
//    #4 = Methodref          #24.#25        // java/io/PrintStream.println:(Ljava/lang/String;)V
//    #5 = Class              #26            // JavaApp
//    #6 = Class              #27            // java/lang/Object
//    #7 = Utf8               <init>
//    #8 = Utf8               ()V
//    #9 = Utf8               Code
//   #10 = Utf8               LineNumberTable
//   #11 = Utf8               LocalVariableTable
//   #12 = Utf8               this
//   #13 = Utf8               LJavaApp;
//   #14 = Utf8               main
//   #15 = Utf8               ([Ljava/lang/String;)V
//   #16 = Utf8               args
//   #17 = Utf8               [Ljava/lang/String;
//   #18 = Utf8               SourceFile
//   #19 = Utf8               JavaApp.java
//   #20 = NameAndType        #7:#8          // "<init>":()V
//   #21 = Class              #28            // java/lang/System
//   #22 = NameAndType        #29:#30        // out:Ljava/io/PrintStream;
//   #23 = Utf8               hello world
//   #24 = Class              #31            // java/io/PrintStream
//   #25 = NameAndType        #32:#33        // println:(Ljava/lang/String;)V
//   #26 = Utf8               JavaApp
//   #27 = Utf8               java/lang/Object
//   #28 = Utf8               java/lang/System
//   #29 = Utf8               out
//   #30 = Utf8               Ljava/io/PrintStream;
//   #31 = Utf8               java/io/PrintStream
//   #32 = Utf8               println
//   #33 = Utf8               (Ljava/lang/String;)V
//类方法定义
// {
//   public JavaApp();
//     descriptor: ()V
//     flags: (0x0001) ACC_PUBLIC
//     Code:
//       stack=1, locals=1, args_size=1
//虚拟机指令
//          0: aload_0
//          1: invokespecial #1                  // Method java/lang/Object."<init>":()V
//          4: return
//       LineNumberTable:
//         line 1: 0
//       LocalVariableTable:
//         Start  Length  Slot  Name   Signature
//             0       5     0  this   LJavaApp;
// 
//   public static void main(java.lang.String[]);
//     descriptor: ([Ljava/lang/String;)V
//     flags: (0x0009) ACC_PUBLIC, ACC_STATIC
//     Code:
//       stack=2, locals=1, args_size=1
//虚拟机指令
//          0: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
//          3: ldc           #3                  // String hello world
//          5: invokevirtual #4                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
//          8: return
//       LineNumberTable:
//         line 3: 0
//         line 4: 8
//       LocalVariableTable:
//         Start  Length  Slot  Name   Signature
//             0       9     0  args   [Ljava/lang/String;
// }
// SourceFile: "JavaApp.java"



```



#### StringTable

```java
public class JavaApp {
    public static void main(String [] args){
        String s1 = "a";
        String s2 = "b";
        String s3 = "a" + "b";
        String s4 = s1 + s2;
        String s5 = "ab";
        String s6 = s4.intern();
        System.out.println(s3 == s4); // false
        System.out.println(s3 == s5); // true
        System.out.println(s3 == s6); // true

        String x2 = new String("c") + new String("d");
        String x1 = "cd";
        x2.intern();
        System.out.println(x1 == x2); // false
    }
}
```

常量池中的字符串仅是符号，第一次用到时才会把字符串从常量池中放入 StringTable 并变为对象。

StringTable 是一个哈希表，可以避免重复创建字符串对象。

字符串变量拼接的原理是 StringBuilder(1.8) ，从反编译知道。

字符串常量拼接的原理是编译器优化。

可以使用 intern 方法，将 StringTable 中还没有的字符串对象放入 StringTable 中。

##### intern()

1.8 将字符串对象尝试放入 StringTable 中，如果有则不放入，如果没有则放入 StringTable，然后返回 StringTable 中的对象。



1.6 将字符串对象尝试放入 StringTable 中，如果有则不放入，如果没有则将此对象复制一份，再放入 StringTable，然后返回 StringTable 中的对象。

```java
public class JavaApp {
    public static void main(String [] args){
        String x2 = new String("c") + new String("d");
        String x1 = "cd";
        String x3 = x2.intern();
        //1.6 1.8
        System.out.println(x1 == x2); // false
        System.out.println(x1 == x3); // true
    }
}

public class JavaApp {
    public static void main(String [] args){
        String x2 = new String("c") + new String("d");
        String x3 = x2.intern();
        String x1 = "cd";
        // 1.6
        System.out.println(x1 == x2); // false
        System.out.println(x1 == x3); // true
        // 1.8
        System.out.println(x1 == x2); // true
        System.out.println(x1 == x3); // true
    }
}
```



#### StringTable 位置

从 1.7 开始， StringTable 从永久代中移动到堆中，由于永久代的垃圾回收需要 Full GC ，转移到堆中，只需要 Mirror GC 就可以进行回收 StringTable 中的对象。

##### 证明案例

分别在 1.6 和 1.8 的环境下向 StringTable 不断添加字符串，导致 StringTable 内存溢出。

```java
// 1.8 设置 -Xmx10m -XX:-UseGCOverheadLimit
// 1.6 设置 -XX:MaxPermSize=10m
public class JavaApp {
    public static void main(String [] args){
        List<String> list = new ArrayList<>();
        int i = 0;
        try{
            for(int j = 0;j<260000;j++){
                list.add(String.valueOf(j).intern());
                i++;
            }
        }catch (Throwable e){
            e.printStackTrace();
        }finally {
            System.out.println(i);
        }
    }
}

// 在 1.6 中为  java.lang.OutOfMemoryError: PermGen space
// 在 1.8 中为 java.lang.OutOfMemoryError: Java heap space
```



#### StringTable 垃圾回收

```java
// -Xmx10m ：设置堆内存大小
// -XX:+PrintStringTableStatistics ：打印 StringTable 统计信息
// -XX:+PrintGCDetails -verbose:gx ：打印垃圾回收信息
public class JavaApp {
    public static void main(String [] args){
        int i = 0;
        try{
            
        }catch (Throwable e){
            e.printStackTrace();
        }finally {
            System.out.println(i);
        }
    }
}
// [0.002s][warning][gc] -XX:+PrintGCDetails is deprecated. Will use -Xlog:gc* instead.
// [0.021s][info   ][gc,heap] Heap region size: 1M
// [0.022s][info   ][gc     ] Using G1
// [0.022s][info   ][gc,heap,coops] Heap address: 0x00000000ff600000, size: 10 MB, Compressed Oops mode: 32-bit
// 0
// [0.134s][info   ][gc,heap,exit ] Heap
// [0.134s][info   ][gc,heap,exit ]  garbage-first heap   total 10240K, used 1024K [0x00000000ff600000, 0x0000000100000000)
// [0.134s][info   ][gc,heap,exit ]   region size 1024K, 2 young (2048K), 0 survivors (0K)
// [0.134s][info   ][gc,heap,exit ]  Metaspace       used 6380K, capacity 6443K, committed 6528K, reserved 1056768K
// [0.134s][info   ][gc,heap,exit ]   class space    used 559K, capacity 570K, committed 640K, reserved 1048576K
// SymbolTable statistics:
// Number of buckets       :     20011 =    160088 bytes, each 8
// Number of entries       :     26605 =    638520 bytes, each 24
// Number of literals      :     26605 =   1125416 bytes, avg  42.301
// Total footprint         :           =   1924024 bytes
// Average bucket size     :     1.330
// Variance of bucket size :     1.336
// Std. dev. of bucket size:     1.156
// Maximum bucket size     :         9
// StringTable statistics:
// Number of buckets       :     65536 =    524288 bytes, each 8
// Number of entries       :      2926 =     46816 bytes, each 16
// Number of literals      :      2926 =    242696 bytes, avg  82.945 // 字符串对象数量
// Total footprsize_t         :           =    813800 bytes
// Average bucket size     :     0.045
// Variance of bucket size :     0.045
// Std. dev. of bucket size:     0.212
// Maximum bucket size     :         2
```



将大量字符串加入到 StringTable 中，会发现 StringTable 中有些字符串对象被回收了

```java
// -Xmx10m ：设置堆内存大小
// -XX:+PrintStringTableStatistics ：打印 StringTable 统计信息
// -XX:+PrintGCDetails -verbose:gx ：打印垃圾回收信息
public class JavaApp {
    public static void main(String [] args){
        int i = 0;
        try{
            for(int j = 0;j<50000;j++){
                String.valueOf(j).intern();
                i++;
            }
        }catch (Throwable e){
            e.printStackTrace();
        }finally {
            System.out.println(i);
        }
    }
}
// [0.002s][warning][gc] -XX:+PrintGCDetails is deprecated. Will use -Xlog:gc* instead.
// [0.005s][info   ][gc,heap] Heap region size: 1M
// [0.006s][info   ][gc     ] Using G1
// [0.006s][info   ][gc,heap,coops] Heap address: 0x00000000ff600000, size: 10 MB, Compressed Oops mode: 32-bit
// [0.108s][info   ][gc,start     ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
// [0.108s][info   ][gc,task      ] GC(0) Using 2 workers of 13 for evacuation
// [0.114s][info   ][gc,phases    ] GC(0)   Pre Evacuate Collection Set: 0.0ms
// [0.114s][info   ][gc,phases    ] GC(0)   Evacuate Collection Set: 6.3ms
// [0.114s][info   ][gc,phases    ] GC(0)   Post Evacuate Collection Set: 0.1ms
// [0.114s][info   ][gc,phases    ] GC(0)   Other: 0.2ms
// [0.114s][info   ][gc,heap      ] GC(0) Eden regions: 4->0(1)
// [0.114s][info   ][gc,heap      ] GC(0) Survivor regions: 0->1(1)
// [0.114s][info   ][gc,heap      ] GC(0) Old regions: 0->3
// [0.114s][info   ][gc,heap      ] GC(0) Humongous regions: 0->0
// [0.114s][info   ][gc,metaspace ] GC(0) Metaspace: 6420K->6420K(1056768K)
// [0.114s][info   ][gc           ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 4M->3M(10M) 6.590ms
// [0.114s][info   ][gc,cpu       ] GC(0) User=0.00s Sys=0.00s Real=0.01s
// 50000
// [0.117s][info   ][gc,heap,exit ] Heap
// [0.117s][info   ][gc,heap,exit ]  garbage-first heap   total 10240K, used 3100K [0x00000000ff600000, 0x0000000100000000)
// [0.117s][info   ][gc,heap,exit ]   region size 1024K, 2 young (2048K), 1 survivors (1024K)
// [0.117s][info   ][gc,heap,exit ]  Metaspace       used 6439K, capacity 6539K, committed 6784K, reserved 1056768K
// [0.117s][info   ][gc,heap,exit ]   class space    used 564K, capacity 602K, committed 640K, reserved 1048576K
// SymbolTable statistics:
// Number of buckets       :     20011 =    160088 bytes, each 8
// Number of entries       :     26688 =    640512 bytes, each 24
// Number of literals      :     26688 =   1127872 bytes, avg  42.261
// Total footprint         :           =   1928472 bytes
// Average bucket size     :     1.334
// Variance of bucket size :     1.340
// Std. dev. of bucket size:     1.158
// Maximum bucket size     :         9
// StringTable statistics:
// Number of buckets       :     65536 =    524288 bytes, each 8
// Number of entries       :     52920 =    846720 bytes, each 16
// Number of literals      :     52920 =   2642672 bytes, avg  49.937 // 字符串对象数量
// Total footprsize_t         :           =   4013680 bytes
// Average bucket size     :     0.807
// Variance of bucket size :     1.567
// Std. dev. of bucket size:     1.252
// Maximum bucket size     :         7
```



#### StringTable 性能调优

**减小哈希冲突**

-XX:StringTableSize=200000 ：设置 HashTable bucket 大小

**重复使用的字符串考虑入池**

```java
// -XX:StringTableSize=200000 ：设置 HashTable bucket 大小
// -XX:+PrintStringTableStatistics ：打印 StringTable 统计信息
public class JavaApp {
    public static void main(String [] args){
        long start = System.nanoTime();
        for(int j = 0;j<400000;j++){
            String.valueOf(j).intern();
        }
        System.out.println("cost:"+(System.nanoTime()-start)/1000000);
    }
}
// 设置 HashTable bucket 大小为 200000
// cost:90
// SymbolTable statistics:
// Number of buckets       :     20011 =    160088 bytes, each 8
// Number of entries       :     27108 =    650592 bytes, each 24
// Number of literals      :     27108 =   1149848 bytes, avg  42.417
// Total footprint         :           =   1960528 bytes
// Average bucket size     :     1.355
// Variance of bucket size :     1.359
// Std. dev. of bucket size:     1.166
// Maximum bucket size     :         9
// StringTable statistics:
// Number of buckets       :    262144 =   2097152 bytes, each 8
// Number of entries       :    402971 =   6447536 bytes, each 16
// Number of literals      :    402971 =  19446096 bytes, avg  48.257
// Total footprsize_t         :           =  27990784 bytes
// Average bucket size     :     1.537
// Variance of bucket size :     1.529
// Std. dev. of bucket size:     1.237
// Maximum bucket size     :         7

// 不设置 HashTable bucket
// cost:193
// SymbolTable statistics:
// Number of buckets       :     20011 =    160088 bytes, each 8
// Number of entries       :     27108 =    650592 bytes, each 24
// Number of literals      :     27108 =   1149848 bytes, avg  42.417
// Total footprint         :           =   1960528 bytes
// Average bucket size     :     1.355
// Variance of bucket size :     1.359
// Std. dev. of bucket size:     1.166
// Maximum bucket size     :         9
// StringTable statistics:
// Number of buckets       :     65536 =    524288 bytes, each 8
// Number of entries       :    402971 =   6447536 bytes, each 16
// Number of literals      :    402971 =  19446096 bytes, avg  48.257
// Total footprsize_t         :           =  26417920 bytes
// Average bucket size     :     6.149
// Variance of bucket size :    17.513
// Std. dev. of bucket size:     4.185
// Maximum bucket size     :        19
```





## 直接内存

#### 定义

Direct Memory

- 常见于 NIO 操作时，用于数据缓冲区
- 分别配回收陈本较高，但读写性能高
- 不受 JVM 内存回收管理



#### 应用



```java
public class JavaApp {

    private static final String FROM = "E:\\downloads\\youtube\\Sig M18.mp4";

    private static final String TO = "E:\\downloads\\youtube\\Sig M18.mp4.bak";

    private static final int _1MB = 1024 * 1024;

    public static void main(String [] args) throws IOException {
        io();
        directBuffer();
    }

    private static void directBuffer() throws IOException {
        long start = System.nanoTime();
        try(FileChannel from = new FileInputStream(FROM).getChannel();
            FileChannel to = new FileOutputStream(TO).getChannel();){
            ByteBuffer buf = ByteBuffer.allocateDirect(_1MB);
            while (true) {
                int len = from.read(buf);
                if(-1 == len){
                    break;
                }
                buf.flip();
                to.write(buf);
                buf.clear();
            }
        }
        System.out.println("directBuffer 用时:"+(System.nanoTime()-start) / 1000000.0);
    }

    private static void io() throws IOException {
        long start = System.nanoTime();
        try(FileInputStream from = new FileInputStream(FROM);
            FileOutputStream to = new FileOutputStream(TO);){
            byte[] buf = new byte[_1MB];
            while (true) {
                int len = from.read(buf);
                if(-1 == len){
                    break;
                }
                to.write(buf, 0 , len);
            }
        }
        System.out.println("io 用时:"+(System.nanoTime()-start) / 1000000.0);
    }
}

// io 用时:310.6705
// directBuffer 用时:160.1515
```



阻塞 IO

![](D:\documents\notes\solid\Java.assets\007.PNG)



NIO

![](D:\documents\notes\solid\Java.assets\008.PNG)



#### 分配和回收原理

使用了 Unsafe 对象完成直接内存的分配和回收，并且回收需要主动调用 freeMemory 方法。

ByteBuffer 的实现类内部，使用了 Cleaner （虚引用）来监测 ByteBuffer 对象，一旦 ByteBuffer 对象被垃圾回收，那么就会由 ReferenceHandler 线程通过 Cleaner 的 clean方法调用 freeMemory 来释放直接内存。



##### 内存申请和释放

```java
public class JavaApp {

    private static final int _1GB = 1024 * 1024 * 1024;

    public static void main(String [] args) throws NoSuchFieldException, IllegalAccessException, IOException {
        Unsafe unsafe = getUnsafe();
        // 分配内存
        long base = unsafe.allocateMemory(_1GB);
        unsafe.setMemory(base, _1GB, (byte)0);
        System.in.read();
        // 释放内存
        unsafe.freeMemory(base);
        System.in.read();
    }

    public static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }
}
```

![](D:\documents\notes\solid\Java.assets\009.PNG)



![](D:\documents\notes\solid\Java.assets\010.PNG)



##### ByteBuffer 的申请与释放

```java
public class JavaApp {

    private static final int _1GB = 1024 * 1024 * 1024;

    public static void main(String [] args) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(_1GB);
        System.out.println("分配完毕");
        System.in.read();
        System.out.println("开始释放");
        byteBuffer = null;
        System.gc();
    }
}
```

在方法 allocateDirect 中创建了对象 DirectByteBuffer

```java
public static ByteBuffer allocateDirect(int capacity) {
    return new DirectByteBuffer(capacity);
}
```

在对象 DirectByteBuffer 的构造函数中 进行了内存分配。

```java
DirectByteBuffer(int cap) {                   // package-private

    super(-1, 0, cap, cap);
    boolean pa = VM.isDirectMemoryPageAligned();
    int ps = Bits.pageSize();
    long size = Math.max(1L, (long)cap + (pa ? ps : 0));
    Bits.reserveMemory(size, cap);

    long base = 0;
    try {
        base = UNSAFE.allocateMemory(size);
    } catch (OutOfMemoryError x) {
        Bits.unreserveMemory(size, cap);
        throw x;
    }
    UNSAFE.setMemory(base, size, (byte) 0);
    if (pa && (base % ps != 0)) {
        // Round up to page boundary
        address = base + ps - (base & (ps - 1));
    } else {
        address = base;
    }
    cleaner = Cleaner.create(this, new Deallocator(base, size, cap));
    att = null;
}
```

并将当前 DirectByteBuffer 对象和虚引用对象  Cleaner 关联，当当前 DirectByteBuffer 对象被垃圾回收器回收时会触发虚引用函数的 clean() 函数。

```java
public void clean() {
    if (!remove(this))
        return;
    try {
        thunk.run();
    } catch (final Throwable x) {
        AccessController.doPrivileged(new PrivilegedAction<>() {
            public Void run() {
                if (System.err != null)
                    new Error("Cleaner terminated abnormally", x)
                    .printStackTrace();
                System.exit(1);
                return null;
            }});
    }
}
```

clean() 函数会调用 Deallocator 进行内存释放。

```java
private static class Deallocator implements Runnable
{

    private long address;
    private long size;
    private int capacity;

    private Deallocator(long address, long size, int capacity) {
        assert (address != 0);
        this.address = address;
        this.size = size;
        this.capacity = capacity;
    }

    public void run() {
        if (address == 0) {
            // Paranoia
            return;
        }
        UNSAFE.freeMemory(address);
        address = 0;
        Bits.unreserveMemory(size, capacity);
    }

}
```

##### 内存溢出

```java
public class JavaApp {

    private static final int _100MB = 100 * 1024 * 1024;

    public static void main(String [] args) {
        List<ByteBuffer> list = new ArrayList<>(100);
        int i = 0;
        try{
            while(true){
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(_100MB);
                list.add(byteBuffer);
                i++;
            }
        } finally {
            System.out.println(i);
        }
    }
}

// 39
// Exception in thread "main" java.lang.OutOfMemoryError: Direct buffer memory
// 	at java.base/java.nio.Bits.reserveMemory(Bits.java:175)
// 	at java.base/java.nio.DirectByteBuffer.<init>(DirectByteBuffer.java:118)
// 	at java.base/java.nio.ByteBuffer.allocateDirect(ByteBuffer.java:318)
// 	at JavaApp.main(JavaApp.java:15)
```



#### 禁用显式垃圾回收对直接内存的影响

调用 System.gc(); 通知 GC 进行垃圾回收，执行的时 Full GC。会对程序造成较大影响。

禁用显式垃圾回收时无法手动通知 GC 进行垃圾回收，从而无法及时回收直接内存。只能等到 GC 自动进行垃圾回收时才能将直接内存回收。

在禁用显式垃圾回收时可以手动调用释放内存。

```java
// -XX:+DisableExplicitGC 禁用显式垃圾回收
public class JavaApp {

    private static final int _1GB = 1024 * 1024 * 1024;

    public static void main(String [] args) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(_1GB);
        System.out.println("分配完毕");
        System.in.read();
        System.out.println("开始释放");
        byteBuffer = null;
        System.gc(); // 无效
        System.in.read();
    }
}
```



## 执行引擎

### 垃圾回收器

#### 如何判断对象可以回收

##### 引用计数法

根据对象被引用的数量决定是否回收该对象，当被引用的数量为 0 时，则可以对该对象进行垃圾回收。

###### 出现的问题

当多个对象出现循环引用时，这些对象由于被引用的数量都不可能为 0 ，所以无法回收这些对象。

![](D:\documents\notes\solid\Java.assets\011.PNG)



##### 可达性分析算法

对堆中的对象进行扫描，判断是否能够沿着 GC Root 对象 为起点的引用链找到该对象，如果找不到，则标记当前对象为可回收对象，否则不回收当前对象。



###### 根对象（GC Root 对象）

**案例**

使用 Memory Analyzer 内存分析工具寻找根对象

**1、运行程序**

```java
public class JavaApp {

    public static void main(String [] args) throws IOException {
        List<Object> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        System.out.println(1);
        System.in.read();

        list = null;
        System.out.println(2);
        System.in.read();
        System.out.println("end");
    }
}
```

**2、使用 jps 查看 java 进程**

```sh
PS D:\develop\code\java\jvm> jps
20912 Jps
21220 Launcher
8388
17708 JavaApp
```

**3、使用 jmap 抓取堆快照**

```sh
# format:二进制格式，存活的对象（抓取之前会进行一次垃圾回收），保存文件
PS D:\develop\code\java\jvm> jmap -dump:format=b,live,file=JavaApp.dump.bin 17708
Heap dump file created
# list 被置为空之后抓取
PS D:\develop\code\java\jvm> jmap -dump:format=b,live,file=JavaApp2.dump.bin 17708 
Heap dump file created
```

**4、使用 mat 打开两份堆快照**

打开 Open Query Browser -> Java Basics -> GC Roots 可以看到根对象被分为几大类

![](D:\documents\notes\solid\Java.assets\012.PNG)

线程内的局部变量所引用的对象都可以作为根对象

![](D:\documents\notes\solid\Java.assets\013.PNG)



当对象没有被引用时，对象将被回收

![](D:\documents\notes\solid\Java.assets\014.PNG)





##### 四种引用

###### 强引用（StrongReference）

只有所有 GC Roots 对象都不通过强引用引用该对象，该对象才能被垃圾回收。

```java
Object object = new Object();
String str = "scc";
//都是强引用
```

###### 软引用(SoftReference)

仅有软引用引用该对象时，在垃圾回收后，内存仍然不足时会再次发起垃圾回收，回收软引用对象。

可以配合引用队列来释放软引用自身。

###### 软引用示例

```java
// -Xmx20m
public class JavaApp {
    private static final int _4MB = 4 * 1024 * 1024;
    public static void main(String [] args) {
        List<SoftReference<byte[]>> list = new ArrayList();
        for(int i = 0; i<5;i++){
            SoftReference<byte[]> ref = new SoftReference<>(new byte[_4MB]);
            System.out.println(ref.get());
            list.add(ref);
            System.out.println(list.size());
        }
        System.out.println("循环结束:"+list.size());
        for(SoftReference<byte[]> ref:list){
            System.out.println(ref.get());
        }
    }
}
// 结果如下
// [B@4c203ea1
// 1
// [B@27f674d
// 2
// [B@1d251891
// 3
// [B@48140564
// 4
// [B@58ceff1
// 5
// 循环结束:5
// null
// null
// null
// [B@48140564
// [B@58ceff1

// 垃圾回收详细信息
// [0.002s][warning][gc] -XX:+PrintGCDetails is deprecated. Will use -Xlog:gc* instead.
// [0.020s][info   ][gc,heap] Heap region size: 1M
// [0.021s][info   ][gc     ] Using G1
// [0.021s][info   ][gc,heap,coops] Heap address: 0x00000000fec00000, size: 20 MB, Compressed Oops mode: 32-bit
// [B@4c203ea1
// 1
// [0.147s][info   ][gc,start     ] GC(0) Pause Young (Concurrent Start) (G1 Humongous Allocation)
// [0.147s][info   ][gc,task      ] GC(0) Using 2 workers of 13 for evacuation
// [0.148s][info   ][gc,phases    ] GC(0)   Pre Evacuate Collection Set: 0.0ms
// [0.148s][info   ][gc,phases    ] GC(0)   Evacuate Collection Set: 1.3ms
// [0.148s][info   ][gc,phases    ] GC(0)   Post Evacuate Collection Set: 0.1ms
// [0.148s][info   ][gc,phases    ] GC(0)   Other: 0.2ms
// [0.148s][info   ][gc,heap      ] GC(0) Eden regions: 3->0(8)
// [0.148s][info   ][gc,heap      ] GC(0) Survivor regions: 0->1(2)
// [0.148s][info   ][gc,heap      ] GC(0) Old regions: 0->0
// [0.148s][info   ][gc,heap      ] GC(0) Humongous regions: 5->5
// [0.148s][info   ][gc,metaspace ] GC(0) Metaspace: 6369K->6369K(1056768K)
// [0.148s][info   ][gc           ] GC(0) Pause Young (Concurrent Start) (G1 Humongous Allocation) 7M->5M(20M) 1.644ms
// [0.148s][info   ][gc,cpu       ] GC(0) User=0.03s Sys=0.00s Real=0.00s
// [0.148s][info   ][gc           ] GC(1) Concurrent Cycle
// [0.148s][info   ][gc,marking   ] GC(1) Concurrent Clear Claimed Marks
// [0.149s][info   ][gc,marking   ] GC(1) Concurrent Clear Claimed Marks 0.004ms
// [0.149s][info   ][gc,marking   ] GC(1) Concurrent Scan Root Regions
// [0.150s][info   ][gc,marking   ] GC(1) Concurrent Scan Root Regions 0.861ms
// [0.150s][info   ][gc,marking   ] GC(1) Concurrent Mark (0.150s)
// [0.150s][info   ][gc,marking   ] GC(1) Concurrent Mark From Roots
// [0.150s][info   ][gc,task      ] GC(1) Using 3 workers of 3 for marking
// [0.150s][info   ][gc,marking   ] GC(1) Concurrent Mark From Roots 0.331ms
// [0.150s][info   ][gc,marking   ] GC(1) Concurrent Preclean
// [0.150s][info   ][gc,marking   ] GC(1) Concurrent Preclean 0.040ms
// [0.150s][info   ][gc,marking   ] GC(1) Concurrent Mark (0.150s, 0.150s) 0.388ms
// [B@27f674d[0.150s][info   ][gc,start     ] GC(1) Pause Remark
// [0.151s][info   ][gc,stringtable] GC(1) Cleaned string and symbol table, strings: 2932 processed, 0 removed, symbols: 26618 processed, 0 removed
// [0.151s][info   ][gc            ] GC(1) Pause Remark 11M->11M(20M) 1.032ms
// [0.151s][info   ][gc,cpu        ] GC(1) User=0.00s Sys=0.00s Real=0.00s
// [0.151s][info   ][gc,marking    ] GC(1) Concurrent Rebuild Remembered Sets
// [0.151s][info   ][gc,marking    ] GC(1) Concurrent Rebuild Remembered Sets 0.035ms
// [0.151s][info   ][gc,start      ] GC(1) Pause Cleanup
// [0.151s][info   ][gc            ] GC(1) Pause Cleanup 11M->11M(20M) 0.024ms
// [0.151s][info   ][gc,cpu        ] GC(1) User=0.00s Sys=0.00s Real=0.00s
// [0.151s][info   ][gc,marking    ] GC(1) Concurrent Cleanup for Next Mark
// 
// 2
// [0.151s][info   ][gc,marking    ] GC(1) Concurrent Cleanup for Next Mark 0.091ms
// [0.151s][info   ][gc            ] GC(1) Concurrent Cycle 2.818ms
// [B@1d251891
// 3
// [0.153s][info   ][gc,start      ] GC(2) Pause Young (Concurrent Start) (G1 Humongous Allocation)
// [0.153s][info   ][gc,task       ] GC(2) Using 2 workers of 13 for evacuation
// [0.154s][info   ][gc,phases     ] GC(2)   Pre Evacuate Collection Set: 0.0ms
// [0.154s][info   ][gc,phases     ] GC(2)   Evacuate Collection Set: 0.8ms
// [0.154s][info   ][gc,phases     ] GC(2)   Post Evacuate Collection Set: 0.1ms
// [0.154s][info   ][gc,phases     ] GC(2)   Other: 0.1ms
// [0.154s][info   ][gc,heap       ] GC(2) Eden regions: 1->0(1)
// [0.154s][info   ][gc,heap       ] GC(2) Survivor regions: 1->1(2)
// [0.154s][info   ][gc,heap       ] GC(2) Old regions: 0->0
// [0.154s][info   ][gc,heap       ] GC(2) Humongous regions: 15->15
// [0.154s][info   ][gc,metaspace  ] GC(2) Metaspace: 6419K->6419K(1056768K)
// [0.154s][info   ][gc            ] GC(2) Pause Young (Concurrent Start) (G1 Humongous Allocation) 16M->16M(20M) 1.069ms
// [0.154s][info   ][gc,cpu        ] GC(2) User=0.00s Sys=0.00s Real=0.00s
// [0.154s][info   ][gc            ] GC(3) Concurrent Cycle
// [0.154s][info   ][gc,marking    ] GC(3) Concurrent Clear Claimed Marks
// [0.154s][info   ][gc,marking    ] GC(3) Concurrent Clear Claimed Marks 0.005ms
// [0.154s][info   ][gc,marking    ] GC(3) Concurrent Scan Root Regions
// [0.155s][info   ][gc,marking    ] GC(3) Concurrent Scan Root Regions 0.545ms
// [0.155s][info   ][gc,marking    ] GC(3) Concurrent Mark (0.155s)
// [0.155s][info   ][gc,marking    ] GC(3) Concurrent Mark From Roots
// [0.155s][info   ][gc,start      ] GC(4) Pause Young (Normal) (G1 Humongous Allocation)
// [0.155s][info   ][gc,task       ] GC(3) Using 3 workers of 3 for marking
// [0.155s][info   ][gc,task       ] GC(4) Using 2 workers of 13 for evacuation
// [0.155s][info   ][gc,phases     ] GC(4)   Pre Evacuate Collection Set: 0.5ms
// [0.155s][info   ][gc,phases     ] GC(4)   Evacuate Collection Set: 0.8ms
// [0.155s][info   ][gc,phases     ] GC(4)   Post Evacuate Collection Set: 0.0ms
// [0.155s][info   ][gc,phases     ] GC(4)   Other: 0.1ms
// [0.155s][info   ][gc,heap       ] GC(4) Eden regions: 0->0(1)
// [0.155s][info   ][gc,heap       ] GC(4) Survivor regions: 1->1(1)
// [0.155s][info   ][gc,heap       ] GC(4) Old regions: 0->1
// [0.155s][info   ][gc,heap       ] GC(4) Humongous regions: 15->15
// [0.155s][info   ][gc,metaspace  ] GC(4) Metaspace: 6419K->6419K(1056768K)
// [0.155s][info   ][gc            ] GC(4) Pause Young (Normal) (G1 Humongous Allocation) 16M->16M(20M) 0.890ms
// [0.155s][info   ][gc,cpu        ] GC(4) User=0.00s Sys=0.00s Real=0.00s
// [0.156s][info   ][gc,task       ] GC(5) Using 2 workers of 13 for full compaction
// [0.156s][info   ][gc,start      ] GC(5) Pause Full (G1 Humongous Allocation)
// [0.156s][info   ][gc,phases,start] GC(5) Phase 1: Mark live objects
// [0.157s][info   ][gc,stringtable ] GC(5) Cleaned string and symbol table, strings: 2933 processed, 3 removed, symbols: 26667 processed, 0 removed
// [0.157s][info   ][gc,phases      ] GC(5) Phase 1: Mark live objects 1.068ms
// [0.157s][info   ][gc,phases,start] GC(5) Phase 2: Prepare for compaction
// [0.157s][info   ][gc,phases      ] GC(5) Phase 2: Prepare for compaction 0.168ms
// [0.157s][info   ][gc,phases,start] GC(5) Phase 3: Adjust pointers
// [0.157s][info   ][gc,phases      ] GC(5) Phase 3: Adjust pointers 0.356ms
// [0.157s][info   ][gc,phases,start] GC(5) Phase 4: Compact heap
// [0.158s][info   ][gc,phases      ] GC(5) Phase 4: Compact heap 0.205ms
// [0.158s][info   ][gc,heap        ] GC(5) Eden regions: 0->0(1)
// [0.158s][info   ][gc,heap        ] GC(5) Survivor regions: 1->0(1)
// [0.158s][info   ][gc,heap        ] GC(5) Old regions: 1->2
// [0.158s][info   ][gc,heap        ] GC(5) Humongous regions: 15->15
// [0.158s][info   ][gc,metaspace   ] GC(5) Metaspace: 6419K->6419K(1056768K)
// [0.158s][info   ][gc             ] GC(5) Pause Full (G1 Humongous Allocation) 16M->15M(20M) 2.052ms
// [0.158s][info   ][gc,cpu         ] GC(5) User=0.00s Sys=0.00s Real=0.00s
// [0.158s][info   ][gc,task        ] GC(6) Using 2 workers of 13 for full compaction
// [0.158s][info   ][gc,start       ] GC(6) Pause Full (G1 Humongous Allocation)
// [0.158s][info   ][gc,phases,start] GC(6) Phase 1: Mark live objects
// [0.159s][info   ][gc,stringtable ] GC(6) Cleaned string and symbol table, strings: 2930 processed, 2 removed, symbols: 26667 processed, 0 removed
// [0.159s][info   ][gc,phases      ] GC(6) Phase 1: Mark live objects 0.727ms
// [0.159s][info   ][gc,phases,start] GC(6) Phase 2: Prepare for compaction
// [0.159s][info   ][gc,phases      ] GC(6) Phase 2: Prepare for compaction 0.148ms
// [0.159s][info   ][gc,phases,start] GC(6) Phase 3: Adjust pointers
// [0.159s][info   ][gc,phases      ] GC(6) Phase 3: Adjust pointers 0.311ms
// [0.159s][info   ][gc,phases,start] GC(6) Phase 4: Compact heap
// [0.159s][info   ][gc,phases      ] GC(6) Phase 4: Compact heap 0.190ms
// [0.160s][info   ][gc,heap        ] GC(6) Eden regions: 0->0(1)
// [0.160s][info   ][gc,heap        ] GC(6) Survivor regions: 0->0(1)
// [0.160s][info   ][gc,heap        ] GC(6) Old regions: 2->2
// [0.160s][info   ][gc,heap        ] GC(6) Humongous regions: 15->0
// [0.160s][info   ][gc,metaspace   ] GC(6) Metaspace: 6419K->6419K(1056768K)
// [0.160s][info   ][gc             ] GC(6) Pause Full (G1 Humongous Allocation) 15M->0M(8M) 1.838ms
// [0.160s][info   ][gc,cpu         ] GC(6) User=0.00s Sys=0.00s Real=0.00s
// [0.160s][info   ][gc,marking     ] GC(3) Concurrent Mark From Roots 5.160ms
// [0.160s][info   ][gc,marking     ] GC(3) Concurrent Mark Abort
// [0.160s][info   ][gc             ] GC(3) Concurrent Cycle 5.757ms
// [B@48140564
// 4
// [0.160s][info   ][gc,start       ] GC(7) Pause Young (Concurrent Start) (G1 Humongous Allocation)
// [0.160s][info   ][gc,task        ] GC(7) Using 2 workers of 13 for evacuation
// [0.161s][info   ][gc,phases      ] GC(7)   Pre Evacuate Collection Set: 0.0ms
// [0.161s][info   ][gc,phases      ] GC(7)   Evacuate Collection Set: 0.1ms
// [0.161s][info   ][gc,phases      ] GC(7)   Post Evacuate Collection Set: 0.0ms
// [0.161s][info   ][gc,phases      ] GC(7)   Other: 0.1ms
// [0.161s][info   ][gc,heap        ] GC(7) Eden regions: 1->0(1)
// [0.161s][info   ][gc,heap        ] GC(7) Survivor regions: 0->1(1)
// [0.161s][info   ][gc,heap        ] GC(7) Old regions: 2->2
// [0.161s][info   ][gc,heap        ] GC(7) Humongous regions: 5->5
// [0.161s][info   ][gc,metaspace   ] GC(7) Metaspace: 6419K->6419K(1056768K)
// [0.161s][info   ][gc             ] GC(7) Pause Young (Concurrent Start) (G1 Humongous Allocation) 5M->5M(9M) 0.251ms
// [0.161s][info   ][gc,cpu         ] GC(7) User=0.00s Sys=0.00s Real=0.00s
// [0.161s][info   ][gc             ] GC(8) Concurrent Cycle
// [0.161s][info   ][gc,marking     ] GC(8) Concurrent Clear Claimed Marks
// [0.161s][info   ][gc,marking     ] GC(8) Concurrent Clear Claimed Marks 0.004ms
// [0.161s][info   ][gc,marking     ] GC(8) Concurrent Scan Root Regions
// [0.161s][info   ][gc,marking     ] GC(8) Concurrent Scan Root Regions 0.028ms
// [0.161s][info   ][gc,marking     ] GC(8) Concurrent Mark (0.161s)
// [0.161s][info   ][gc,marking     ] GC(8) Concurrent Mark From Roots
// [0.161s][info   ][gc,task        ] GC(8) Using 3 workers of 3 for marking
// [0.162s][info   ][gc,marking     ] GC(8) Concurrent Mark From Roots 1.270ms
// [0.162s][info   ][gc,marking     ] GC(8) Concurrent Preclean
// [0.162s][info   ][gc,marking     ] GC(8) Concurrent Preclean 0.037ms
// [0.162s][info   ][gc,marking     ] GC(8) Concurrent Mark (0.161s, 0.162s) 1.323ms
// [0.162s][info   ][gc,start       ] GC(8) Pause Remark
// [0.163s][info   ][gc,stringtable ] GC(8) Cleaned string and symbol table, strings: 2928 processed, 0 removed, symbols: 26667 processed, 0 removed
// [0.163s][info   ][gc             ] GC(8) Pause Remark 10M->10M(14M) 0.846ms
// [0.163s][info   ][gc,cpu         ] GC(8) User=0.00s Sys=0.00s Real=0.00s
// [0.163s][info   ][gc,marking     ] GC(8) Concurrent Rebuild Remembered Sets
// [B@58ceff1
// 5
// [0.163s][info   ][gc,marking     ] GC(8) Concurrent Rebuild Remembered Sets 0.426ms
// [0.164s][info   ][gc,start       ] GC(8) Pause Cleanup
// [0.164s][info   ][gc             ] GC(8) Pause Cleanup 10M->10M(14M) 0.027ms
// [0.164s][info   ][gc,cpu         ] GC(8) User=0.00s Sys=0.00s Real=0.00s
// [0.164s][info   ][gc,marking     ] GC(8) Concurrent Cleanup for Next Mark
// [0.164s][info   ][gc,marking     ] GC(8) Concurrent Cleanup for Next Mark 0.042ms
// [0.164s][info   ][gc             ] GC(8) Concurrent Cycle 2.887ms
// 循环结束:5
// null
// null
// null
// [B@48140564
// [B@58ceff1
// [0.175s][info   ][gc,heap,exit   ] Heap
// [0.175s][info   ][gc,heap,exit   ]  garbage-first heap   total 14336K, used 11241K [0x00000000fec00000, 0x0000000100000000)
// [0.175s][info   ][gc,heap,exit   ]   region size 1024K, 2 young (2048K), 1 survivors (1024K)
// [0.175s][info   ][gc,heap,exit   ]  Metaspace       used 6686K, capacity 6766K, committed 7040K, reserved 1056768K
// [0.175s][info   ][gc,heap,exit   ]   class space    used 593K, capacity 614K, committed 640K, reserved 1048576K
// 
```

###### 软引用配合引用队列使用

```java
// -Xmx20m -XX:+PrintGCDetails -verbose:gc
public class JavaApp {
    private static final int _4MB = 4 * 1024 * 1024;
    public static void main(String [] args) {
        List<SoftReference<byte[]>> list = new ArrayList();
        ReferenceQueue<byte[]> queue = new ReferenceQueue<>();
        for(int i = 0; i<5;i++){
            // 关联了引用队列，当软引用所关联的 byte[] 被垃圾回收时，软引用会自己加入到 queue 中去
            SoftReference<byte[]> ref = new SoftReference<>(new byte[_4MB], queue);
            System.out.println(ref.get());
            list.add(ref);
            System.out.println(list.size());
        }
        System.out.println("循环结束:"+list.size());

        Reference<? extends byte[]> poll = queue.poll();
        while(poll != null){
            list.remove(poll);
            poll = queue.poll();
        }

        for(SoftReference<byte[]> ref:list){
            System.out.println(ref.get());
        }
    }
}

// [B@7c75222b
// 1
// [B@4c203ea1
// 2
// [B@27f674d
// 3
// [B@1d251891
// 4
// [B@48140564
// 5
// 循环结束:5
// [B@1d251891
// [B@48140564
```



###### 弱引用(WeakReference)

仅有弱引用引用该对象时，在垃圾回收时，无论内存是否充足，都会回收弱引用对象。

可以配合引用队列来释放弱引用自身。

###### 弱引用示例

```java
// -Xmx20m -XX:+PrintGCDetails -verbose:gc
public class JavaApp {
    private static final int _4MB = 4 * 1024 * 1024;
    public static void main(String [] args) {
        List<WeakReference<byte[]>> list = new ArrayList();
        for(int i = 0; i<5;i++){
            WeakReference<byte[]> ref = new WeakReference<>(new byte[_4MB]);
            list.add(ref);
            for(WeakReference<byte[]> w:list){
                System.out.print(w.get());
            }
            System.out.println(list.size());
        }
        System.out.println("循环结束:"+list.size());
    }
}
```



###### 弱引用配合引用队列使用

与软引用类似



###### 虚引用(PhantomReference)

- 不会决定对象的生命周期
- 任何时候都可能被垃圾收集器回收
- 跟踪对象被垃圾收集器回收的活动,起哨兵作用
- 必须和引用队列`ReferenceQueue`联合使用
- 主要用来跟踪对象被垃圾回收的活动

​    当垃圾回收器准备回收一个对象时，如果发现它还有虚引用，就会把这个虚引用加入到与之 关联的引用队列中。由 Reference Handler 线程调用虚引用的相关方法。

```java
Object obj = new Object();
ReferenceQueue queue = new ReferenceQueue();
PhantomReference reference = new PhantomReference(obj, queue);
//强引用对象滞空，保留软引用
obj = null;
```

###### 终结器引用

无需手动编码，但其内部配合引用队列使用，在垃圾回收时，终结器引用入队（被引用对象暂时没有被回收），再由 Finalizer 线程通过终结器引用找到被引用对象并调用它的finalize 方法，第二次 GC 时才能回收被引用对象。



#### 垃圾回收算法

在垃圾回收过程中垃圾回收器会根据不同的情况使用不同的垃圾回收算法

##### 标记清除

Mark Sweep

优点

速度快

缺点

会产生内存碎片



![](D:\documents\notes\solid\Java.assets\015.PNG)



##### 标记整理

Mark Compact

优点

不会产生内存碎片

缺点

整理内存过程中需要移动对象，速度较慢

![](D:\documents\notes\solid\Java.assets\016.PNG)

##### 复制

Copy

优点

不会产生内存碎片

缺点

需要占用双倍内存空间

![](D:\documents\notes\solid\Java.assets\017.PNG)



#### 分代垃圾回收

![](D:\documents\notes\solid\Java.assets\018.PNG)

- 新对象首先分配在伊甸园区域
- 新生代空间不足时，触发 Minor GC，伊甸园和幸存区 From 中存活（可达性分析算法）的对象使用复制算法复制到幸存区 To 中，存活的对象年龄加1，然后在逻辑上交换 From 和 To
- Minor GC 会引发 stop the world(STW)。暂停其它用户线程，等垃圾回收结束后，用户线程才能继续运行
- 当对象年龄超过阈值时，会晋升到老年代，最大寿命为 15。（在对象头中保存年龄的字段大小为 4bit，所以最大为 15 ）
- 当老年代空间不足时，会先尝试触发 Minor GC，如果之后空间仍不足，那么将触发 Full GC，STW 的时间更长（对象多、回收算法不同）
- Full GC 后空间仍然不足，将抛出内存不足异常



##### 相关 JVM 参数

| 含义                | 参数                                                        |
| ------------------- | ----------------------------------------------------------- |
| 堆初始大小          | -Xms                                                        |
| 堆最大大小          | -Xmx 或 -XX:MaxHeapSize=size                                |
| 新生代大小          | -Xmn 或 -XX:NewSize=size + -XX:MaxNewSize=size              |
| 幸存区比例（动态）  | -XX:InitialSurvivorRatio=ratio 和 -XX:UseAdaptiveSizePolicy |
| 幸存区比例          | -XX:SurvivorRatio=ratio（伊甸园的占比）                     |
| 晋升阈值            | -XX:MaxTenuringThreshold=threshold                          |
| 晋升详情            | -XX:PrintTenuringDistribution                               |
| GC 详情             | -XX:PrintGCDetails -verbose:gc                              |
| Full GC 前 Minor GC | -XX:+ScavengeBeforeFullGC                                   |



##### GC 分析

###### 不进行垃圾回收

伊甸园存放初始对象，占用 28%

幸存区 from 、幸存区 to 占用 0%

```java
// -Xms20m -Xmx20m -Xmn10m -XX:+UseSerialGC -XX:+PrintGCDetails -verbose:gc
public class JavaApp {
    private static final int _512KB = 512 * 1024;
    private static final int _1MB = 1024 * 1024;
    private static final int _6MB = 6 * _1MB;
    private static final int _7MB = 7 * _1MB;
    private static final int _8MB = 8 * _1MB;
    public static void main(String [] args) {
        
    }
}

// [0.003s][warning][gc] -XX:+PrintGCDetails is deprecated. Will use -Xlog:gc* instead.
// [0.034s][info   ][gc] Using Serial
// [0.034s][info   ][gc,heap,coops] Heap address: 0x00000000fec00000, size: 20 MB, Compressed Oops mode: 32-bit
// [0.159s][info   ][gc,heap,exit ] Heap
// [0.159s][info   ][gc,heap,exit ]  def new generation   total 9216K, used 2351K [0x00000000fec00000, 0x00000000ff600000, 0x00000000ff600000)
// [0.159s][info   ][gc,heap,exit ]   eden space 8192K,  28% used [0x00000000fec00000, 0x00000000fee4bf00, 0x00000000ff400000)
// [0.159s][info   ][gc,heap,exit ]   from space 1024K,   0% used [0x00000000ff400000, 0x00000000ff400000, 0x00000000ff500000)
// [0.159s][info   ][gc,heap,exit ]   to   space 1024K,   0% used [0x00000000ff500000, 0x00000000ff500000, 0x00000000ff600000)
// [0.159s][info   ][gc,heap,exit ]  tenured generation   total 10240K, used 0K [0x00000000ff600000, 0x0000000100000000, 0x0000000100000000)
// [0.159s][info   ][gc,heap,exit ]    the space 10240K,   0% used [0x00000000ff600000, 0x00000000ff600000, 0x00000000ff600200, 0x0000000100000000)
// [0.159s][info   ][gc,heap,exit ]  Metaspace       used 6371K, capacity 6443K, committed 6528K, reserved 1056768K
// [0.159s][info   ][gc,heap,exit ]   class space    used 559K, capacity 570K, committed 640K, reserved 1048576K
```

###### 一次垃圾回收

新增 7 MB 对象

先对新生代进行垃圾回收，2351K->966K

不能被回收的对象放在幸存区 from，占用 94%

新对象放在伊甸园，占用 92%

```java
// -Xms20m -Xmx20m -Xmn10m -XX:+UseSerialGC -XX:+PrintGCDetails -verbose:gc
public class JavaApp {
    private static final int _512KB = 512 * 1024;
    private static final int _1MB = 1024 * 1024;
    private static final int _6MB = 6 * _1MB;
    private static final int _7MB = 7 * _1MB;
    private static final int _8MB = 8 * _1MB;
    public static void main(String [] args) {
        List<byte[]> list = new ArrayList<>();
        list.add(new byte[_7MB]);
    }
}

// [0.003s][warning][gc] -XX:+PrintGCDetails is deprecated. Will use -Xlog:gc* instead.
// [0.006s][info   ][gc] Using Serial
// [0.006s][info   ][gc,heap,coops] Heap address: 0x00000000fec00000, size: 20 MB, Compressed Oops mode: 32-bit
// [0.093s][info   ][gc,start     ] GC(0) Pause Young (Allocation Failure)
// [0.094s][info   ][gc,heap      ] GC(0) DefNew: 2351K->966K(9216K)
// [0.094s][info   ][gc,heap      ] GC(0) Tenured: 0K->0K(10240K)
// [0.094s][info   ][gc,metaspace ] GC(0) Metaspace: 6386K->6386K(1056768K)
// [0.094s][info   ][gc           ] GC(0) Pause Young (Allocation Failure) 2M->0M(19M) 1.597ms
// [0.094s][info   ][gc,cpu       ] GC(0) User=0.00s Sys=0.00s Real=0.00s
// [0.096s][info   ][gc,heap,exit ] Heap
// [0.096s][info   ][gc,heap,exit ]  def new generation   total 9216K, used 8517K [0x00000000fec00000, 0x00000000ff600000, 0x00000000ff600000)
// [0.096s][info   ][gc,heap,exit ]   eden space 8192K,  92% used [0x00000000fec00000, 0x00000000ff35fb00, 0x00000000ff400000)
// [0.096s][info   ][gc,heap,exit ]   from space 1024K,  94% used [0x00000000ff500000, 0x00000000ff5f1a80, 0x00000000ff600000)
// [0.096s][info   ][gc,heap,exit ]   to   space 1024K,   0% used [0x00000000ff400000, 0x00000000ff400000, 0x00000000ff500000)
// [0.096s][info   ][gc,heap,exit ]  tenured generation   total 10240K, used 0K [0x00000000ff600000, 0x0000000100000000, 0x0000000100000000)
// [0.096s][info   ][gc,heap,exit ]    the space 10240K,   0% used [0x00000000ff600000, 0x00000000ff600000, 0x00000000ff600200, 0x0000000100000000)
// [0.096s][info   ][gc,heap,exit ]  Metaspace       used 6434K, capacity 6539K, committed 6784K, reserved 1056768K
// [0.096s][info   ][gc,heap,exit ]   class space    used 564K, capacity 602K, committed 640K, reserved 1048576K
```

###### 两次垃圾回收

第一次垃圾回收：幸存对象放在幸存区 from

第二次垃圾回收：发现新生代放不下 7MB 的大对象，直接将大对象放在老年代

```java
// -Xms20m -Xmx20m -Xmn10m -XX:+UseSerialGC -XX:+PrintGCDetails -verbose:gc
public class JavaApp {
    private static final int _512KB = 512 * 1024;
    private static final int _1MB = 1024 * 1024;
    private static final int _6MB = 6 * _1MB;
    private static final int _7MB = 7 * _1MB;
    private static final int _8MB = 8 * _1MB;
    public static void main(String [] args) {
        List<byte[]> list = new ArrayList<>();
        list.add(new byte[_7MB]);
        list.add(new byte[_1MB]);
    }
}

// [0.002s][warning][gc] -XX:+PrintGCDetails is deprecated. Will use -Xlog:gc* instead.
// [0.006s][info   ][gc] Using Serial
// [0.006s][info   ][gc,heap,coops] Heap address: 0x00000000fec00000, size: 20 MB, Compressed Oops mode: 32-bit
// [0.097s][info   ][gc,start     ] GC(0) Pause Young (Allocation Failure)
// [0.098s][info   ][gc,heap      ] GC(0) DefNew: 2351K->967K(9216K)
// [0.098s][info   ][gc,heap      ] GC(0) Tenured: 0K->0K(10240K)
// [0.098s][info   ][gc,metaspace ] GC(0) Metaspace: 6393K->6393K(1056768K)
// [0.098s][info   ][gc           ] GC(0) Pause Young (Allocation Failure) 2M->0M(19M) 1.501ms
// [0.098s][info   ][gc,cpu       ] GC(0) User=0.02s Sys=0.00s Real=0.00s
// [0.100s][info   ][gc,start     ] GC(1) Pause Young (Allocation Failure)
// [0.104s][info   ][gc,heap      ] GC(1) DefNew: 8462K->24K(9216K)
// [0.104s][info   ][gc,heap      ] GC(1) Tenured: 0K->8134K(10240K)
// [0.104s][info   ][gc,metaspace ] GC(1) Metaspace: 6413K->6413K(1056768K)
// [0.104s][info   ][gc           ] GC(1) Pause Young (Allocation Failure) 8M->7M(19M) 3.613ms
// [0.104s][info   ][gc,cpu       ] GC(1) User=0.00s Sys=0.00s Real=0.00s
// [0.104s][info   ][gc,heap,exit ] Heap
// [0.104s][info   ][gc,heap,exit ]  def new generation   total 9216K, used 1265K [0x00000000fec00000, 0x00000000ff600000, 0x00000000ff600000)
// [0.104s][info   ][gc,heap,exit ]   eden space 8192K,  15% used [0x00000000fec00000, 0x00000000fed362b0, 0x00000000ff400000)
// [0.104s][info   ][gc,heap,exit ]   from space 1024K,   2% used [0x00000000ff400000, 0x00000000ff4062c0, 0x00000000ff500000)
// [0.104s][info   ][gc,heap,exit ]   to   space 1024K,   0% used [0x00000000ff500000, 0x00000000ff500000, 0x00000000ff600000)
// [0.104s][info   ][gc,heap,exit ]  tenured generation   total 10240K, used 8134K [0x00000000ff600000, 0x0000000100000000, 0x0000000100000000)
// [0.104s][info   ][gc,heap,exit ]    the space 10240K,  79% used [0x00000000ff600000, 0x00000000ffdf1818, 0x00000000ffdf1a00, 0x0000000100000000)
// [0.104s][info   ][gc,heap,exit ]  Metaspace       used 6428K, capacity 6539K, committed 6784K, reserved 1056768K
// [0.104s][info   ][gc,heap,exit ]   class space    used 564K, capacity 602K, committed 640K, reserved 1048576K
```

###### 大对象

发现新生代放不下 8MB 的大对象，直接将大对象放在老年代，而不是将大对象放在新生代

```java
// -Xms20m -Xmx20m -Xmn10m -XX:+UseSerialGC -XX:+PrintGCDetails -verbose:gc
public class JavaApp {
    private static final int _512KB = 512 * 1024;
    private static final int _1MB = 1024 * 1024;
    private static final int _6MB = 6 * _1MB;
    private static final int _7MB = 7 * _1MB;
    private static final int _8MB = 8 * _1MB;
    public static void main(String [] args) {
        List<byte[]> list = new ArrayList<>();
        list.add(new byte[_8MB]);
    }
}

// [0.002s][warning][gc] -XX:+PrintGCDetails is deprecated. Will use -Xlog:gc* instead.
// [0.006s][info   ][gc] Using Serial
// [0.006s][info   ][gc,heap,coops] Heap address: 0x00000000fec00000, size: 20 MB, Compressed Oops mode: 32-bit
// [0.097s][info   ][gc,heap,exit ] Heap
// [0.097s][info   ][gc,heap,exit ]  def new generation   total 9216K, used 2515K [0x00000000fec00000, 0x00000000ff600000, 0x00000000ff600000)
// [0.097s][info   ][gc,heap,exit ]   eden space 8192K,  30% used [0x00000000fec00000, 0x00000000fee74e80, 0x00000000ff400000)
// [0.097s][info   ][gc,heap,exit ]   from space 1024K,   0% used [0x00000000ff400000, 0x00000000ff400000, 0x00000000ff500000)
// [0.097s][info   ][gc,heap,exit ]   to   space 1024K,   0% used [0x00000000ff500000, 0x00000000ff500000, 0x00000000ff600000)
// [0.097s][info   ][gc,heap,exit ]  tenured generation   total 10240K, used 8192K [0x00000000ff600000, 0x0000000100000000, 0x0000000100000000)
// [0.097s][info   ][gc,heap,exit ]    the space 10240K,  80% used [0x00000000ff600000, 0x00000000ffe00010, 0x00000000ffe00200, 0x0000000100000000)
// [0.097s][info   ][gc,heap,exit ]  Metaspace       used 6433K, capacity 6539K, committed 6784K, reserved 1056768K
// [0.097s][info   ][gc,heap,exit ]   class space    used 564K, capacity 602K, committed 640K, reserved 1048576K
```

###### 大对象OOM

存放第二个大对象时先触发了新生代 GC 后触发 Full GC 后发现内存还是不足导致 OOM。

```java
// -Xms20m -Xmx20m -Xmn10m -XX:+UseSerialGC -XX:+PrintGCDetails -verbose:gc
public class JavaApp {
    private static final int _512KB = 512 * 1024;
    private static final int _1MB = 1024 * 1024;
    private static final int _6MB = 6 * _1MB;
    private static final int _7MB = 7 * _1MB;
    private static final int _8MB = 8 * _1MB;
    public static void main(String [] args) {
        List<byte[]> list = new ArrayList<>();
        list.add(new byte[_8MB]);
        list.add(new byte[_8MB]);
    }
}

// [0.002s][warning][gc] -XX:+PrintGCDetails is deprecated. Will use -Xlog:gc* instead.
// [0.006s][info   ][gc] Using Serial
// [0.006s][info   ][gc,heap,coops] Heap address: 0x00000000fec00000, size: 20 MB, Compressed Oops mode: 32-bit
// [0.095s][info   ][gc,start     ] GC(0) Pause Young (Allocation Failure)
// [0.096s][info   ][gc,start     ] GC(1) Pause Full (Allocation Failure)
// [0.096s][info   ][gc,phases,start] GC(1) Phase 1: Mark live objects
// [0.098s][info   ][gc,phases      ] GC(1) Phase 1: Mark live objects 1.356ms
// [0.098s][info   ][gc,phases,start] GC(1) Phase 2: Compute new object addresses
// [0.098s][info   ][gc,phases      ] GC(1) Phase 2: Compute new object addresses 0.113ms
// [0.098s][info   ][gc,phases,start] GC(1) Phase 3: Adjust pointers
// [0.098s][info   ][gc,phases      ] GC(1) Phase 3: Adjust pointers 0.357ms
// [0.098s][info   ][gc,phases,start] GC(1) Phase 4: Move objects
// [0.099s][info   ][gc,phases      ] GC(1) Phase 4: Move objects 0.455ms
// [0.099s][info   ][gc             ] GC(1) Pause Full (Allocation Failure) 8M->8M(19M) 2.371ms
// [0.099s][info   ][gc,heap        ] GC(0) DefNew: 2351K->0K(9216K)
// [0.099s][info   ][gc,heap        ] GC(0) Tenured: 8192K->9183K(10240K)
// [0.099s][info   ][gc,metaspace   ] GC(0) Metaspace: 6418K->6418K(1056768K)
// [0.099s][info   ][gc             ] GC(0) Pause Young (Allocation Failure) 10M->8M(19M) 3.914ms
// [0.099s][info   ][gc,cpu         ] GC(0) User=0.00s Sys=0.00s Real=0.00s
// [0.099s][info   ][gc,start       ] GC(2) Pause Full (Allocation Failure)
// [0.099s][info   ][gc,phases,start] GC(2) Phase 1: Mark live objects
// [0.100s][info   ][gc,phases      ] GC(2) Phase 1: Mark live objects 0.726ms
// [0.100s][info   ][gc,phases,start] GC(2) Phase 2: Compute new object addresses
// [0.100s][info   ][gc,phases      ] GC(2) Phase 2: Compute new object addresses 0.112ms
// [0.100s][info   ][gc,phases,start] GC(2) Phase 3: Adjust pointers
// [0.100s][info   ][gc,phases      ] GC(2) Phase 3: Adjust pointers 0.345ms
// [0.100s][info   ][gc,phases,start] GC(2) Phase 4: Move objects
// [0.100s][info   ][gc,phases      ] GC(2) Phase 4: Move objects 0.111ms
// [0.100s][info   ][gc,heap        ] GC(2) DefNew: 0K->0K(9216K)
// [0.100s][info   ][gc,heap        ] GC(2) Tenured: 9183K->9169K(10240K)
// [0.100s][info   ][gc,metaspace   ] GC(2) Metaspace: 6418K->6418K(1056768K)
// [0.100s][info   ][gc             ] GC(2) Pause Full (Allocation Failure) 8M->8M(19M) 1.385ms
// [0.100s][info   ][gc,cpu         ] GC(2) User=0.00s Sys=0.00s Real=0.00s
// [0.101s][info   ][gc,heap,exit   ] Heap
// [0.101s][info   ][gc,heap,exit   ]  def new generation   total 9216K, used 218K [0x00000000fec00000, 0x00000000ff600000, 0x00000000ff600000)
// [0.101s][info   ][gc,heap,exit   ]   eden space 8192K,   2% used [0x00000000fec00000, 0x00000000fec36b58, 0x00000000ff400000)
// [0.101s][info   ][gc,heap,exit   ]   from space 1024K,   0% used [0x00000000ff500000, 0x00000000ff500000, 0x00000000ff600000)
// [0.101s][info   ][gc,heap,exit   ]   to   space 1024K,   0% used [0x00000000ff400000, 0x00000000ff400000, 0x00000000ff500000)
// [0.101s][info   ][gc,heap,exit   ]  tenured generation   total 10240K, used 9169K [0x00000000ff600000, 0x0000000100000000, 0x0000000100000000)
// [0.101s][info   ][gc,heap,exit   ]    the space 10240K,  89% used [0x00000000ff600000, 0x00000000ffef4618, 0x00000000ffef4800, 0x0000000100000000)
// [0.101s][info   ][gc,heap,exit   ]  Metaspace       used 6437K, capacity 6539K, committed 6784K, reserved 1056768K
// [0.101s][info   ][gc,heap,exit   ]   class space    used 565K, capacity 602K, committed 640K, reserved 1048576K
// Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
// 	at JavaApp.main(JavaApp.java:14)
// 
// Process finished with exit code 1
```

###### 子线程 OOM 不会影响到主线程

```java
// -Xms20m -Xmx20m -Xmn10m -XX:+UseSerialGC -XX:+PrintGCDetails -verbose:gc
public class JavaApp {
    private static final int _512KB = 512 * 1024;
    private static final int _1MB = 1024 * 1024;
    private static final int _6MB = 6 * _1MB;
    private static final int _7MB = 7 * _1MB;
    private static final int _8MB = 8 * _1MB;
    public static void main(String [] args) throws InterruptedException {
        new Thread(()->{
            List<byte[]> list = new ArrayList<>();
            list.add(new byte[_8MB]);
            list.add(new byte[_8MB]);
        }).start();
        Thread.sleep(5000L);
    }
}

```



#### 垃圾回收器

##### 串行（Serial GC）

单线程

堆内存较小

Serial：新生代垃圾回收（复制回收算法）

SerialOld：老年代垃圾回收（标记整理回收算法）

![](D:\documents\notes\solid\Java.assets\019.PNG)



##### 吞吐量优先（Parallel GC）

多线程

堆内存较大，多核 CPU

在单位时间内，STW 的时间最短

1.8 默认垃圾回收器

-XX:+UseParalleGC：新生代垃圾回收（复制回收算法），并行，垃圾回收过程用户线程不能同时运行

-XX:+UseParalleOlGC：老年代垃圾回收（标记整理回收算法），并行，垃圾回收过程用户线程不能同时运行

-XX:UseAdaptiveSizePolicy：动态调整幸存区比例

-XX:GCTimeRatio： 默认99，1/(1+ratio)*运行时间=垃圾回收最长时间，如果垃圾回收时长超过垃圾回收最长时间则会增大堆空间大小

-XXMaxGCPauseMillis：每次垃圾回收最长时间，如果垃圾回收时长超过每次垃圾回收最长时间则会减小堆空间大小

-XX:ParallelGCThreads：指定垃圾回收工作线程数

![](D:\documents\notes\solid\Java.assets\020.PNG)





##### 响应时间优先（CMS）

多线程

堆内存较大，多核 CPU

尽可能让单次 STW 的时间最短

-XX:+UseParNewGC：新生代垃圾回收（复制回收算法）

-XX:+UseConcMarkSweepGC：老年代垃圾回收（标记清除回收算法），并发，垃圾回收过程的某些阶段用户线程也能同时运行。标记清除回收算法会产生比较多的内存碎片导致并发回收失败的问题，这时候会使用串行垃圾回收 SerialOld 进行垃圾回收

-XX:ParallelGCThreads：并行线程数

=XX:ConcGCThreads：并发线程数=XX:ParallelGCThreads*0.25

-XX:CMSInitiatingOccupancyFraction：进行垃圾回收的垃圾空间占比

-XX:+CMSScanvengeBeforeRemark：在重新标记前对新生代进行一次垃圾回收

![](D:\documents\notes\solid\Java.assets\021.PNG)

初始标记：标记根对象

并发标记：标记

重新标记：



##### G1

###### 定义：Garbage First

- 2004 论文发布
- 2009 JDK 6u14 体验
- 2012 JDK 7u4 官方支持
- 2017 JDK 9 默认



###### 适用场景

同时注重吞吐量（Throughput）和低延迟（Low Latency），默认的暂停目标是 200 ms

超大堆内存，会将堆划分为多个大小相等的 Region

整体上是标记+整理算法，两个区域之间是复制算法



###### 相关 JVM 参数

| 含义                              | 参数                                       |
| --------------------------------- | ------------------------------------------ |
| 开启 G1 垃圾回收器                | -XX:+UseG1GC                               |
| 堆区域大小（2 的 n 次幂 MB）      | -XX:G1HeapRegionSize=size                  |
| 垃圾回收暂停目标                  | -XX:MaxGCPauseMillis=time                  |
| 老年代进行并发标记阈值（默认45%） | -XX:InitiatingHeapOccupancyPercent=percent |



###### G1 垃圾回收过程

![](D:\documents\notes\solid\Java.assets\022.PNG)

先对新生代进行垃圾回收，经过一段时间后老年代的垃圾积累变多之后（达到某个阈值），在新生代垃圾回收的同时对老年代的垃圾对象进行标记，标记完成之后对新生代和老年代同时进行垃圾回收。



###### Young Collection

堆内存分为多个区域（Region），每个区域都独立作为伊甸园、幸存区和老年代。



![](D:\documents\notes\solid\Java.assets\023.jpg)

当新生代堆内存达到阈值时进行垃圾回收，使用复制算法将存活对象复制到幸存区

![image-20220725221339123](D:\documents\notes\solid\Java.assets\024.jpg)

经过多次新生代垃圾回收，一部分幸存区对象晋升到老年代

![image-20220725221727044](D:\documents\notes\solid\Java.assets\025.jpg)



###### Young Collection 跨代引用

新生代回收的跨代引用（老年代引用新生代）问题

![image-20220726221009543](D:\documents\notes\solid\Java.assets\028.jpg)



- 卡表 Remembered Set
- 在引用表更时通过 post-write barrier + dirty card queue
- concurrent refinement threads 更新 Remembered Set

![image-20220726221426697](D:\documents\notes\solid\Java.assets\029.jpg)



###### Young Collection + Concurrent Mark

在 Young GC 时会进行 GC Root 的初始标记

老年代占用空间比例达到阈值时，进行并发标记（不会 STW）

-XX:InitiatingHeapOccupancyPercent=percent（默认45%）

![image-20220725222216767](D:\documents\notes\solid\Java.assets\026.jpg)



###### Mixed Collection

会对 E、S、O 区域进行全面垃圾回收

最终标记（Remark）会 STW

拷贝存活（Evacuation）会 STW

老年代的垃圾回收会根据最大暂停时间进行有选择的垃圾回收，优先回收价值高的区域

-XX:MaxGCPauseMillis=ms

![image-20220725223044911](D:\documents\notes\solid\Java.assets\027.jpg)



###### Remark

pre-write barrier + satb_mark_queue





![image-20220726221946069](D:\documents\notes\solid\Java.assets\030.jpg)





##### Full GC

###### Serial GC

- 新生代内存不足发生的垃圾收集 - minor gc
- 老年代内存不足发生的垃圾收集 - full gc

###### Parallel GC

- 新生代内存不足发生的垃圾收集 - minor gc
- 老年代内存不足发生的垃圾收集 - full gc

###### CMS

- 新生代内存不足发生的垃圾收集 - minor gc
- 老年代内存不足（当并发标记时回收的速度比垃圾产生的速度快时不会产生 Full GC，当由于并发回收失败时，才会产生 Full GC）

###### G1

- 新生代内存不足发生的垃圾收集 - minor gc
- 老年代内存不足（当并发标记时回收的速度比垃圾产生的速度快时不会产生 Full GC，当由于并发回收失败时，才会产生 Full GC）



##### G1 优化

###### JDK 8u20 字符串去重

- 优点：节省大量内存
- 缺点：略微多占用了 cpu 时间，新生代回收时间略微增加

-XX:+UseStringDeduplication

```java
String s1 = new String("hello"); // char[]{'h','e','l','l','o'}
String s2 = new String("hello"); // char[]{'h','e','l','l','o'}
```



- 将所有新分配的字符串放入一个队列
- 当新生代回收时，G1 并发检查是否有字符串重复
- 如果他们的值一样，让他们引用同一个 char[]
- 注意，与String.intern()不一样，String.intern()关注的是字符串对象，而字符串去重关注的是 char[]，在 JAVA 内部，使用了不同的字符串表



###### JDK 8u40 并发标记类卸载

所有对象都经过并发标记后，就能知道哪些类不再被使用，当一个类加载器的所有类都不再使用，则卸载它所加载的所有类

-XX:+ClassUnloadingWithConcurrentMark 默认启用



###### JDK 8u60 回收巨型对象

一个对象大于 Region 的一半时，称之为巨型对象

G1 不会对巨型对象进行拷贝

回收时被优先考虑

G1 会跟踪老年代所有 incoming 引用，这样老年代 incoming 引用为0的巨型对象就可以在新生代垃圾回收时处理掉



###### JDK 9 并发标记起始时间的调整

- 并发标记必须在堆空间占满前完成，否则退化为 Full GC
- JDK 9 之前需要使用 -XX:InitiatingHeapOccupancyPercent
- JDK 9 可以动态调整 -XX:InitiatingHeapOccupancyPercent 用来设置初始值，进行数据采样并动态调整，总会添加一个安全的空档空间



###### JDK 9 更高效的回收

- 250+增强
- 180+bug修复

https://docs.oracle.com/en/java/javase/12/gctuning





###### JDK 9 更高效的回收





#### 垃圾回收器调优

java -XX:+PrintFlagsFinal -version | findstr "GC"

##### 新生代调优

新生代特点：

所有的 new 操作的内存分配非常廉价，线程内 new 的对象会保存在属于线程的一块内存

TLAB(thread-local allocation buffer)

死亡对象的回收代价时零

大部分对象用过即死

Minor GC 的时间远远低于 Full GC

Oracle 推荐设置新生代大小为整个堆空间的 25% ~ 50%

新生代能容纳所有 【并发量*（请求-响应）】的数据

幸存区大到能保留【当前活跃对象+需要晋升对象】

晋升阈值配置得当，让长时间存活对象尽快晋升

对象晋升阈值 -XX:MaxTenuringThreshold=threshold

打印存活对象信息 -XX:+PrintTenuringDistribution



##### 老年代调优

老年代的内存越大越好

先不尝试做调优，如果有 Full GC 则先尝试调优新生代

观察发生 Full GC 时老年代内存占用，将老年代内存预设调大 1/4 ~ 1/3

老年代垃圾回收内存占用阈值 -XX:CMSInitiatingOccupancyFraction=percent



##### 案例

案例1 Full GC 和 Minor GC 频繁（由于新生代过小，导致年龄未够的新生代对象提前晋升老年代。解决方案：调大新生代大小）

案例2 请求高峰期发生 Full GC，单次暂停时间特别长（CMS，CMS 重新标记阶段对象过多导致标记时间过长，使用 -XX:+CMSScavengeBeForeRemark 开启重新标记前进行新生代垃圾回收，减少标记对象数量）

案例3 老年代充裕的情况下，发生 Full GC（CMS jdk1.7，元空间内存不足导致 Full GC，增大元空间大小）



### 即时编译器(JIT)

#### 分层编译(TieredCompilation)



JVM 将执行状态分成了 5 个层次：

- 第 0 层，解释执行（Interpreter）
- 第 1 层，使用 C1 即时编译器编译执行（不带 profiling) 
- 第 2 层，使用 C1 即时编译器编译执行（带基本的 profiling）
- 第 3 层，使用 C1 即时编译器编译执行（带完全的 profiling）
- 第 4 层，使用 C2 即时编译器编译执行

>  profiling 是指在运行过程中收集一些程序执行状态的数据，例如【方法的调用次数】，【循环的回边次数】等 

即时编译器(JIT）与解释器的区别

- 解释器是将字节码解释为机器码，下次即使遇到相同的字节码，仍会执行重复的解释 
- JIT 是将一些热点字节码编译为机器码，并存入 Code Cache 下次遇到相同的代码，直接执行，无需再编译 
- 解释器是将字节码解释为针对所有平台都通用的机器码 
- JIT 会根据平台类型，生成平台特定的机器码 

对于占据大部分的不常用的代码，我们无需耗费时间将其编译成机器码，而是采取解释执行的方式运行；另 一方面对于仅占据小部分的热点代码，我们则可以将其编译成机器码以达到理想的运行速度。执行效率上简单比较一下 Interpreter < C1 < C2，总的目标是发现热点代码（hotspot 名称的由来）优化之。



#### 逃逸分析

##### 概念

一种确定指针动态范围的静态分析，它可以分析在程序的哪些地方可以访问到指针。

在JVM的即时编译语境下，逃逸分析将判断新建的对象是否逃逸。即时编译判断对象是否逃逸的依据：一种是对象是否被存入堆中（静态字段或者堆中对象的实例字段），另一种就是对象是否被传入未知代码。

对象逃逸示例

- 对象被复制给成员变量或者静态变量，可能被外部使用，此时变量就发生了逃逸
- 对象通过return语句返回。如果对象通过return语句返回了，此时的程序并不能确定这个对象后续会不会被使用，外部的线程可以访问到这个变量，此时对象也发生了逃逸
- ......



##### 优点

逃逸分析的优点总体上来说可以分为三个：对象可能分配在栈上、分离对象或标量替换、消除同步锁。

###### **对象可能分配在栈上**

JVM通过逃逸分析，分析出新对象的使用范围，就可能将对象在栈上进行分配。栈分配可以快速地在栈帧上创建和销毁对象，不用再将对象分配到堆空间，可以有效地减少 JVM 垃圾回收的压力。

###### **分离对象或标量替换**

当JVM通过逃逸分析，确定要将对象分配到栈上时，即时编译可以将对象打散，将对象替换为一个个很小的局部变量，我们将这个打散的过程叫做标量替换。将对象替换为一个个局部变量后，就可以非常方便的在栈上进行分配了。

###### **同步锁消除**

如果JVM通过逃逸分析，发现一个对象只能从一个线程被访问到，则访问这个对象时，可以不加同步锁。如果程序中使用了synchronized锁，则JVM会将synchronized锁消除。

**这里，需要注意的是：这种情况针对的是synchronized锁，而对于Lock锁，则JVM并不能消除。**

要开启同步消除，需要加上 -XX:+EliminateLocks 参数。因为这个参数依赖逃逸分析，所以同时要打开 -XX:+DoEscapeAnalysis 选项。

**所以，并不是所有的对象和数组，都是在堆上进行分配的，由于即时编译的存在，如果JVM发现某些对象没有逃逸出方法，就很有可能被优化成在栈上分配。**



```java
public class JavaApp {
    public static void main(String [] args) {
        for(int i = 0;i < 200; i++){
            long start = System.nanoTime();
            for(int j = 0;j < 1000; j++){
                new Object();
            }
            long end = System.nanoTime();
            System.out.printf("%d\t%d\n", i, (end - start));
        }
    }
}
// 这段代码的内循环运行了一定次数之后运行速度变快的原因是什么呢？ 
```


刚才的一种优化手段称之为【逃逸分析】，发现新建的对象是否逃逸。可以使用 -XX:-DoEscapeAnalysis 关闭逃逸分析，再运行刚才的示例观察结果



#### 方法内联（Inlining）

```java
private static int square(final int i) {
    return i * i;
}
System.out.println(square(9));
```



如果发现 square 是热点方法，并且长度不会太长时，会进行内联，所谓的内联就是把方法内代码 拷贝、粘贴到调用者的位置：

```
System.out.println(9 * 9);
```



还能够进行常量折叠（constant folding）的优化

```java
System.out.println(81);
```



例

```java
public class JavaApp {

    public static void main(String [] args) {
        int x = 0;
        for(int i = 0;i < 500; i++){
            long start = System.nanoTime();
            for(int j = 0;j < 1000; j++){
                x = square(9);
            }
            long end = System.nanoTime();
            System.out.printf("%d\t%d\t%d\n", i, x, (end - start));
        }
    }

    private static int square(final int i) {
        return i * i;
    }
}

// 分别设置下面两种 JVM 参数后观察输出情况
// 输出内联信息
// -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining

// 禁用内联
// -XX:CompileCommand=dontinline,*JavaApp.square
```



#### 字段优化

JMH 基准测试请参考：http://openjdk.java.net/projects/code-tools/jmh/

##### 样例代码

创建 maven 工程，添加依赖如下

```xml
<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-core</artifactId>
    <version>1.35</version>
</dependency>
<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-generator-annprocess</artifactId>
    <version>1.35</version>
</dependency>
```



```java
package test;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

// 程序预热，方便 JIT 统计出热点代码
@Warmup(iterations = 2, time = 1)
// 进行五轮测试
@Measurement(iterations = 5,time = 1)
@State(Scope.Benchmark)
public class MyBenchmark {
    int [] elements = randomInts(1000);

    private static int[] randomInts(int size){
        Random random = ThreadLocalRandom.current();
        int[] values = new int[size];
        for(int i = 0; i < size; i++){
            values[i] = random.nextInt();
        }
        return values;
    }

    @Benchmark
    public void test1(){
        for(int i = 0; i < elements.length; i++){
            doSum(elements[1]);
        }
    }

    @Benchmark
    public void test2(){
        int [] local = this.elements;
        for(int i = 0;i < local.length; i++){
            doSum(elements[1]);
        }
    }

    @Benchmark
    public void test3(){
        for(int element:elements){
            doSum(element);
        }
    }

    static int sum = 0;

    @CompilerControl(CompilerControl.Mode.INLINE)
    static void doSum(int x){
        sum += x;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MyBenchmark.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}

```



##### 结果

```shell
# 允许内联
						样本数 		  得分 		 误差    吞吐量
Benchmark           Mode  Cnt         Score         Error  Units
MyBenchmark.test1  thrpt    5  46665184.592 ± 2171282.544  ops/s
MyBenchmark.test2  thrpt    5  47003670.065 ± 2226975.078  ops/s
MyBenchmark.test3  thrpt    5   4330830.982 ±  178196.438  ops/s

# 不允许内联
Benchmark           Mode  Cnt       Score       Error  Units
MyBenchmark.test1  thrpt    5  581505.643 ± 19885.459  ops/s
MyBenchmark.test2  thrpt    5  390550.301 ± 12229.899  ops/s
MyBenchmark.test3  thrpt    5  696184.667 ± 18680.990  ops/s
```



##### 分析

test1 和 test2 关闭内联时差距为何变大了。

在刚才的示例中，doSum 方法是否内联会影响 elements 成员变量读取的优化：

如果 doSum 方法内联了，刚才的 test 方法会被优化成下面的样子（伪代码）：

```java
@Benchmark
public void test1(){
    // elements 首次读取会被缓存起来 -> int[] local
    for(int i = 0; i < elements.length; i++){// 后续 999 次 求长度 <- local
        sum += elements[1];// 1000 次取下标 i 的元素 <- local
    }
}
```

可以节省 1999 次 Field 读取操作

但如果 doSum 方法没有内联，则不会进行上面的内联优化



##### 结论

尽量使用局部变量，不要使用静态变量和全局变量

##### 练习

在内联情况下将 elements 添加 volatile 修饰符，观察测试结果



#### **关于面试题**

**Java中的对象一定是在堆上分配的吗？**



#### 反射优化

##### 样例

```java
package test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JavaApp {
    public static void foo(){
        System.out.println("foo...");
    }

    public static void main(String[]args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        Method foo = JavaApp.class.getMethod("foo");
        for(int i = 0; i <= 16; i++){
            System.out.printf("%d\t", i);
            foo.invoke(null);
        }
        System.in.read();
    }
}

```

样例中 前面 0~15 次调用的时间会比 16 次调用的时间长。

##### 源码解析

foo.invoke 前面 0~15 次调用使用的是 MethodAccessor 的 NativeMethodAccessorImpl 实现

```java
@CallerSensitive
public Object invoke(Object obj, Object... args)
    throws IllegalAccessException, IllegalArgumentException,
InvocationTargetException
{
    if (!override) {
        if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
            Class<?> caller = Reflection.getCallerClass();
            checkAccess(caller, clazz, obj, modifiers);
        }
    }
    MethodAccessor ma = methodAccessor;             // read volatile

    if (ma == null) {
        ma = acquireMethodAccessor();
    }
    return ma.invoke(obj, args);
}

```

MethodAccessor 的几个实现类

- MethodAccessorImpl，抽象实现类，不用管
- DelegatingMethodAccessorImpl， 默认实现类，继承至 MethodAccessorImpl ，使用 MethodAccessorImpl 的子类去 invoke 函数
- NativeMethodAccessorImpl，实际 invoke 函数的类



NativeMethodAccessorImpl 源码

```java

class NativeMethodAccessorImpl extends MethodAccessorImpl {
    private final Method method;
    private DelegatingMethodAccessorImpl parent;
    private int numInvocations;

    NativeMethodAccessorImpl(Method var1) {
        this.method = var1;
    }

    public Object invoke(Object var1, Object[] var2) throws IllegalArgumentException, InvocationTargetException {
        // 方法的调用次数与膨胀阈值比较，膨胀阈值默认值为 15
        // 当调用次数大于阈值时，将会替换掉 invoke 函数的实现类
        // 运行期间动态生成新的方法访问器类
        if (++this.numInvocations > ReflectionFactory.inflationThreshold() && !ReflectUtil.isVMAnonymousClass(this.method.getDeclaringClass())) {
            MethodAccessorImpl var3 = (MethodAccessorImpl)(new MethodAccessorGenerator()).generateMethod(this.method.getDeclaringClass(), this.method.getName(), this.method.getParameterTypes(), this.method.getReturnType(), this.method.getExceptionTypes(), this.method.getModifiers());
            this.parent.setDelegate(var3);
        }

        return invoke0(this.method, var1, var2);
    }

    void setParent(DelegatingMethodAccessorImpl var1) {
        this.parent = var1;
    }

    private static native Object invoke0(Method var0, Object var1, Object[] var2);
}

```



查看动态生成的方法访问器类

[arthas-boot](https://github.com/alibaba/arthas)

下载：https://alibaba.github.io/arthas/arthas-boot.jar

在生成方法访问器类的地方打断点，使用 idea 的 Evaluate 执行 var3.getClass() 可以获取到生成的类的名字 sun.reflectGeneratedMethodAccessor1

java -jar arthas-boot.jar

连接到对应程序后执行：

jad sun.reflect.GeneratedMethodAccessor1

```java
ClassLoader:
+-sun.reflect.DelegatingClassLoader@22a71081
  +-sun.misc.Launcher$AppClassLoader@18b4aac2
    +-sun.misc.Launcher$ExtClassLoader@4fccd51b

Location:

/*
 * Decompiled with CFR.
 *
 * Could not load the following classes:
 *  test.JavaApp
 */
package sun.reflect;

import java.lang.reflect.InvocationTargetException;
import sun.reflect.MethodAccessorImpl;
import test.JavaApp;

public class GeneratedMethodAccessor1
extends MethodAccessorImpl {
    /*
     * Loose catch block
     */
    public Object invoke(Object object, Object[] objectArray) throws InvocationTargetException {
        block4: {
            if (objectArray == null || objectArray.length == 0) break block4;
            throw new IllegalArgumentException();
        }
        try {
            JavaApp.foo();
            return null;
        }
        catch (Throwable throwable) {
            throw new InvocationTargetException(throwable);
        }
        catch (ClassCastException | NullPointerException runtimeException) {
            throw new IllegalArgumentException(super.toString());
        }
    }
}
```

动态生成的方法访问器对要执行的方法进行直接调用，效率比反射调用高。



##### 注意

```java
private static void checkInitted() {
    if (!initted) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                if (System.out == null) {
                    return null;
                } else {
                    String var1 = System.getProperty("sun.reflect.noInflation");
                    if (var1 != null && var1.equals("true")) {
                        ReflectionFactory.noInflation = true;
                    }

                    var1 = System.getProperty("sun.reflect.inflationThreshold");
                    if (var1 != null) {
                        try {
                            ReflectionFactory.inflationThreshold = Integer.parseInt(var1);
                        } catch (NumberFormatException var3) {
                            throw new RuntimeException("Unable to parse property sun.reflect.inflationThreshold", var3);
                        }
                    }

                    ReflectionFactory.initted = true;
                    return null;
                }
            }
        });
    }
}
```

通过查看 ReflectionFactory 源码可知

- sun.reflect.noInflation 可以用来禁用膨胀（直接生成 GeneratedMethodAccessor1 ，但首次生成比较耗时，如果仅反射调用一次，不划算

- sun.reflect.inflation 可以修改膨胀阈值




### 解释器



## 类加载与字节码技术

### 类文件结构

```java
public class JavaApp {
    public static void main(String [] args) {
        System.out.println("hello world");
    }
}
// 编译:javac -parameters -d . JavaApp.java
root@server01:/media/sf_shared# od -t xC JavaApp.class 
0000000 ca fe ba be 00 00 00 37 00 22 0a 00 06 00 14 09
0000020 00 15 00 16 08 00 17 0a 00 18 00 19 07 00 1a 07
0000040 00 1b 01 00 06 3c 69 6e 69 74 3e 01 00 03 28 29
0000060 56 01 00 04 43 6f 64 65 01 00 0f 4c 69 6e 65 4e
0000100 75 6d 62 65 72 54 61 62 6c 65 01 00 12 4c 6f 63
0000120 61 6c 56 61 72 69 61 62 6c 65 54 61 62 6c 65 01
0000140 00 04 74 68 69 73 01 00 09 4c 4a 61 76 61 41 70
0000160 70 3b 01 00 04 6d 61 69 6e 01 00 16 28 5b 4c 6a
0000200 61 76 61 2f 6c 61 6e 67 2f 53 74 72 69 6e 67 3b
0000220 29 56 01 00 04 61 72 67 73 01 00 13 5b 4c 6a 61
0000240 76 61 2f 6c 61 6e 67 2f 53 74 72 69 6e 67 3b 01
0000260 00 0a 53 6f 75 72 63 65 46 69 6c 65 01 00 0c 4a
0000300 61 76 61 41 70 70 2e 6a 61 76 61 0c 00 07 00 08
0000320 07 00 1c 0c 00 1d 00 1e 01 00 0b 68 65 6c 6c 6f
0000340 20 77 6f 72 6c 64 07 00 1f 0c 00 20 00 21 01 00
0000360 07 4a 61 76 61 41 70 70 01 00 10 6a 61 76 61 2f
0000400 6c 61 6e 67 2f 4f 62 6a 65 63 74 01 00 10 6a 61
0000420 76 61 2f 6c 61 6e 67 2f 53 79 73 74 65 6d 01 00
0000440 03 6f 75 74 01 00 15 4c 6a 61 76 61 2f 69 6f 2f
0000460 50 72 69 6e 74 53 74 72 65 61 6d 3b 01 00 13 6a
0000500 61 76 61 2f 69 6f 2f 50 72 69 6e 74 53 74 72 65
0000520 61 6d 01 00 07 70 72 69 6e 74 6c 6e 01 00 15 28
0000540 4c 6a 61 76 61 2f 6c 61 6e 67 2f 53 74 72 69 6e
0000560 67 3b 29 56 00 21 00 05 00 06 00 00 00 00 00 02
0000600 00 01 00 07 00 08 00 01 00 09 00 00 00 2f 00 01
0000620 00 01 00 00 00 05 2a b7 00 01 b1 00 00 00 02 00
0000640 0a 00 00 00 06 00 01 00 00 00 01 00 0b 00 00 00
0000660 0c 00 01 00 00 00 05 00 0c 00 0d 00 00 00 09 00
0000700 0e 00 0f 00 01 00 09 00 00 00 37 00 02 00 01 00
0000720 00 00 09 b2 00 02 12 03 b6 00 04 b1 00 00 00 02
0000740 00 0a 00 00 00 0a 00 02 00 00 00 03 00 08 00 04
0000760 00 0b 00 00 00 0c 00 01 00 00 00 09 00 10 00 11
0001000 00 00 00 01 00 12 00 00 00 02 00 13

```



根据 JVM 规范，类文件结构如下

```
ClassFile {
	u4				magic;
	u2				minor_version;
	u2				major_version;
	u2				constant_pool_count;
	cp_info			constant_pool[constant_pool_count-1];
	u2				access_flags;
	u2				this_class;
	u2				super_class;
	u2				interfaces_count;
	u2				interfaces[interfaces_count];
	u2				fields_count;
	field_info		fields[fields_count];
	u2				methods_count;
	method_info		methods[methods_count];
	u2				attributes_count;
	attribute_info	attributes[attributes_count];
}
```



#### 魔数

0~3 字节，表示它是否是【class】类型文件

0000000 **ca fe ba be** 00 00 00 37 00 22 0a 00 06 00 14 09

#### 版本

4~7 字节，表示类的版本  

00 34（52）表示是 Java 8

00 37（55）表示是 Java 11

0000000 ca fe ba be **00 00 00 37** 00 22 0a 00 06 00 14 09

#### 常量池

| Constant Type               | Value |
| --------------------------- | ----- |
| CONSTANT_Class              | 7     |
| CONSTANT_Fieldref           | 9     |
| CONSTANT_Methodref          | 10    |
| CONSTANT_InterfaceMethodred | 11    |
| CONSTANT_String             | 8     |
| CONSTANT_Integer            | 3     |
| CONSTANT_Float              | 4     |
| CONSTANT_Long               | 5     |
| CONSTANT_Double             | 6     |
| CONSTANT_NameAndType        | 12    |
| CONSTANT_utf8               | 1     |
| CONSTANT_MethodHandle       | 15    |
| CONSTANT_MethodType         | 16    |
| CONSTANT_InvokeDynamic      | 18    |

8~9字节，表示常量池长度，00 22（34）表示常量池有 #1~#33项，注意 #0项不计入，也没有值

0000000 ca fe ba be 00 00 00 37 **00 22** 0a 00 06 00 14 09



第#1项 0a 表示一个 Method 信息，00 06（6） 和 00 14（20）表示它引用了常量池中#6和#20项来获得这个方法的【所属类】和【方法名】

0000000 ca fe ba be 00 00 00 37 00 22 **0a 00 06 00 14** 09



第#2项 09 表示一个 Field 信息，00 15（21） 和 00 16（22）表示它引用了常量池中 #21 和 #22 项来获得这个成员变量的【所属类】和【成员变量名】

0000000 ca fe ba be 00 00 00 37 00 22 0a 00 06 00 14 **09**

0000020 **00 15 00 16** 08 00 17 0a 00 18 00 19 07 00 1a 07



第#3项 08 表示一个字符串常量名称，00 17（23）表示它引用了常量池中#23项

0000020 00 15 00 16 **08 00 17** 0a 00 18 00 19 07 00 1a 07



第#4项 0a 表示一个 Method 信息，00 18（24） 和  00 19（25）表示它引用了常量池中 #24 和 #25 项来获得这个成员变量的【所属类】和【方法名】

0000020 00 15 00 16 08 00 17 **0a 00 18 00 19** 07 00 1a 07



第#5项 07 表示一个 Class 信息，00 1a（26）表示它引用了常量池中#26项

0000020 00 15 00 16 08 00 17 0a 00 18 00 19 **07 00 1a** 07



第#6项 07 表示一个 Class 信息，00 1b（27）表示它引用了常量池中#27项

0000020 00 15 00 16 08 00 17 0a 00 18 00 19 07 00 1a **07**

0000040 **00 1b** 01 00 06 3c 69 6e 69 74 3e 01 00 03 28 29



第#7项  01 表示一个 utf8 信息，00 06 表示长度，3c 69 6e 69 74 3e 是【<init>】

0000040 00 1b **01 00 06 3c 69 6e 69 74 3e** 01 00 03 28 29



第#8项  01 表示一个 utf8 信息，00 03 表示长度，28 29 56 是【()V】其实就是表示无参、无返回值

0000040 00 1b 01 00 06 3c 69 6e 69 74 3e **01 00 03 28 29**

0000060 **56** 01 00 04 43 6f 64 65 01 00 0f 4c 69 6e 65 4e



第#9项  01 表示一个 utf8 信息，00 04 表示长度，43 6f 64 65 是【Code】

0000060 56 **01 00 04 43 6f 64 65** 01 00 0f 4c 69 6e 65 4e



第#10项  01 表示一个 utf8 信息，00 0f（15） 表示长度，4c 69 6e 65 4e 75 6d 62 65 72 54 61 62 6c 65 是【LineNumberTable】

0000060 56 01 00 04 43 6f 64 65 **01 00 0f 4c 69 6e 65 4e**

0000100 **75 6d 62 65 72 54 61 62 6c 65** 01 00 12 4c 6f 63



第#11项  01 表示一个 utf8 信息，00 12（18） 表示长度，4c 6f 63 61 6c 56 61 72 69 61 62 6c 65 54 61 62 6c 65 是【LocalVariableTable】

0000100 75 6d 62 65 72 54 61 62 6c 65 **01 00 12 4c 6f 63**

0000120 **61 6c 56 61 72 69 61 62 6c 65 54 61 62 6c 65** 01



第#12项  01 表示一个 utf8 信息，00 04（4） 表示长度，74 68 69 73 是【this】

0000120 61 6c 56 61 72 69 61 62 6c 65 54 61 62 6c 65 **01**

0000140 **00 04 74 68 69 73** 01 00 09 4c 4a 61 76 61 41 70



第#13项  01 表示一个 utf8 信息，00 09（9） 表示长度，4c 4a 61 76 61 41 70 70 3b 是【LJavaApp;】

0000140 00 04 74 68 69 73 **01 00 09 4c 4a 61 76 61 41 70**

0000160 **70 3b** 01 00 04 6d 61 69 6e 01 00 16 28 5b 4c 6a



第#14项  01 表示一个 utf8 信息，00 04（4） 表示长度，6d 61 69 6e 是【main】

0000160 70 3b **01 00 04 6d 61 69 6e** 01 00 16 28 5b 4c 6a



第#15项  01 表示一个 utf8 信息，00 16（22） 表示长度，28 5b 4c 6a 61 76 61 2f 6c 61 6e 67 2f 53 74 72 69 6e 67 3b 29 56 是【([Ljava/lang/String;)V】

0000160 70 3b 01 00 04 6d 61 69 6e **01 00 16 28 5b 4c 6a**

0000200 **61 76 61 2f 6c 61 6e 67 2f 53 74 72 69 6e 67 3b**

0000220 **29 56** 01 00 04 61 72 67 73 01 00 13 5b 4c 6a 61



第#16项  01 表示一个 utf8 信息，00 04（4） 表示长度，61 72 67 73 是【args】

0000220 29 56 **01 00 04 61 72 67 73** 01 00 13 5b 4c 6a 61



第#17项  01 表示一个 utf8 信息，00 13（19） 表示长度，5b 4c 6a 61 76 61 2f 6c 61 6e 67 2f 53 74 72 69 6e 67 3b 是【[Ljava/lang/String;】

0000220 29 56 01 00 04 61 72 67 73 **01 00 13 5b 4c 6a 61**

0000240 **76 61 2f 6c 61 6e 67 2f 53 74 72 69 6e 67 3b** 01



第#18项  01 表示一个 utf8 信息，00 0a（10） 表示长度，53 6f 75 72 63 65 46 69 6c 65 是【SourceFile】

0000240 76 61 2f 6c 61 6e 67 2f 53 74 72 69 6e 67 3b **01**

0000260 **00 0a 53 6f 75 72 63 65 46 69 6c 65** 01 00 0c 4a



第#19项  01 表示一个 utf8 信息，00 0c（12） 表示长度，4a  61 76 61 41 70 70 2e 6a 61 76 61 是【JavaApp.java】

0000260 00 0a 53 6f 75 72 63 65 46 69 6c 65 **01 00 0c 4a**

0000300 **61 76 61 41 70 70 2e 6a 61 76 61** 0c 00 07 00 08



第#20项  0c 表示一个【名+类型】，00 07 00 08 引用了常量池中 #7 #8 两项

0000300 61 76 61 41 70 70 2e 6a 61 76 61 **0c 00 07 00 08**



第#21项 07 表示一个 Class 信息，00 1c（28）表示它引用了常量池中 #28 项

0000320 **07 00 1c** 0c 00 1d 00 1e 01 00 0b 68 65 6c 6c 6f



第#22项  0c 表示一个【名+类型】，00 1d（29） 00 1e（30） 引用了常量池中 #29#30两项

0000320 07 00 1c **0c 00 1d 00 1e** 01 00 0b 68 65 6c 6c 6f



第#23项  01 表示一个 utf8 信息，00 0b（11） 表示长度， 68 65 6c 6c 6f 20 77 6f 72 6c 64 是【hello world】

0000320 07 00 1c 0c 00 1d 00 1e **01 00 0b 68 65 6c 6c 6f**

0000340 **20 77 6f 72 6c 64** 07 00 1f 0c 00 20 00 21 01 00



第#24项 07 表示一个 Class 信息，00 1f（31）表示它引用了常量池中 #31项

0000340 20 77 6f 72 6c 64 **07 00 1f** 0c 00 20 00 21 01 00



第#25项  0c 表示一个【名+类型】，00 20（32） 00 21（33） 引用了常量池中 #32 #33两项

0000340 20 77 6f 72 6c 64 07 00 1f **0c 00 20 00 21** 01 00



第#26项  01 表示一个 utf8 信息，00 07 表示长度，4a 61 76 61 41 70 70 是【JavaApp】

0000340 20 77 6f 72 6c 64 07 00 1f 0c 00 20 00 21 **01 00**

0000360 **07 4a 61 76 61 41 70 70** 01 00 10 6a 61 76 61 2f



第#27项  01 表示一个 utf8 信息，00 10（16） 表示长度，6a 61 76 61 2f 6c 61 6e 67 2f 4f 62 6a 65 63 74 是【java/lang/Object】

0000360 07 4a 61 76 61 41 70 70 **01 00 10 6a 61 76 61 2f**

0000400 **6c 61 6e 67 2f 4f 62 6a 65 63 74** 01 00 10 6a 61



第#28项  01 表示一个 utf8 信息，00 10（16） 表示长度，6a 61 76 61 2f 6c 61 6e 67 2f 53 79 73 74 65 6d 是【java/lang/System】

0000400 6c 61 6e 67 2f 4f 62 6a 65 63 74 **01 00 10 6a 61**

0000420 **76 61 2f 6c 61 6e 67 2f 53 79 73 74 65 6d** 01 00



第#29项  01 表示一个 utf8 信息，00 03 表示长度，6f 75 74 是【out】

0000420 76 61 2f 6c 61 6e 67 2f 53 79 73 74 65 6d **01 00**

0000440 **03 6f 75 74** 01 00 15 4c 6a 61 76 61 2f 69 6f 2f



第#30项  01 表示一个 utf8 信息，00 15（21） 表示长度，4c 6a 61 76 61 2f 69 6f 2f 50 72 69 6e 74 53 74 72 65 61 6d 3b 是【Ljava/io/PrintStream;】

0000440 03 6f 75 74 **01 00 15 4c 6a 61 76 61 2f 69 6f 2f**

0000460 **50 72 69 6e 74 53 74 72 65 61 6d 3b** 01 00 13 6a



第#31项  01 表示一个 utf8 信息，00 13（19） 表示长度，6a 61 76 61 2f 69 6f 2f 50 72 69 6e 74 53 74 72 65 61 6d 是【java/io/PrintStream】

0000460 50 72 69 6e 74 53 74 72 65 61 6d 3b **01 00 13 6a**

0000500 **61 76 61 2f 69 6f 2f 50 72 69 6e 74 53 74 72 65**

0000520 **61 6d** 01 00 07 70 72 69 6e 74 6c 6e 01 00 15 28



第#32项  01 表示一个 utf8 信息，00 07（7）表示长度，70 72 69 6e 74 6c 6e 是【println】

0000520 61 6d **01 00 07 70 72 69 6e 74 6c 6e** 01 00 15 28



第#33项  01 表示一个 utf8 信息，00 15（21） 表示长度，28 4c 6a 61 76 61 2f 6c 61 6e 67 2f 53 74 72 69 6e 67 3b 29 56 是【(Ljava/lang/String;)V】

0000520 61 6d 01 00 07 70 72 69 6e 74 6c 6e **01 00 15 28**

0000540 **4c 6a 61 76 61 2f 6c 61 6e 67 2f 53 74 72 69 6e**

0000560 **67 3b 29 56** 00 21 00 05 00 06 00 00 00 00 00 02



#### 访问标识与继承信息



21 表示该 class 是一个类，公共的

0000560 67 3b 29 56 **00 21** 00 05 00 06 00 00 00 00 00 02



05 表示根据常量池中 #5 找到本类全限定名

0000560 67 3b 29 56 00 21 **00 05** 00 06 00 00 00 00 00 02



06 表示根据常量池中 #6 找到父类全限定名

0000560 67 3b 29 56 00 21 00 05 **00 06** 00 00 00 00 00 02



表示接口数量，本类为 0 

0000560 67 3b 29 56 00 21 00 05 00 06 **00 00** 00 00 00 02



| Flag Name      | Value  | Interpretation                                               |
| -------------- | ------ | ------------------------------------------------------------ |
| ACC_PUBLIC     | 0x0001 | Declared public; may be accessed from outside its package.   |
| ACC_FINAL      | 0x0010 | Declared final; no subclasses allowed.                       |
| ACC_SUPER      | 0x0020 | Threat superclass methods specially when invoked by the invokespecial instruction. |
| ACC_INTERFACE  | 0x0200 | Is an interface,not a class.                                 |
| ACC_ABSTRACT   | 0x0400 | Declared abstract; must not be instantiated.                 |
| ACC_SYNTHETIC  | 0x1000 | Declared synthetic; not present in the source code.          |
| ACC_ANNOTATION | 0x2000 | Declared as an annotation type.                              |
| ACC_ENUM       | 0x4000 | Declared as an enum type.                                    |



#### Field 信息

表示成员变量，本类为 0 

0000560 67 3b 29 56 00 21 00 05 00 06 00 00 **00 00** 00 02

| FieldType    | Type      | Interpretation                                               |
| ------------ | --------- | ------------------------------------------------------------ |
| B            | byte      | signed byte                                                  |
| C            | char      | Unicode character code point in the Basic Multilingual Plane,encoded with UTF-16 |
| D            | double    | double-percision floating-point value                        |
| F            | float     | single-percision floating-point value                        |
| I            | int       | integer                                                      |
| J            | long      | long integer                                                 |
| L ClassName; | reference | an instance of calss ClassName                               |
| S            | short     | signed short                                                 |
| Z            | boolean   | true or false                                                |
| [            | reference | one array dimension                                          |



#### Method 信息

00 02 表示方法数量，本类为 2

0000560 67 3b 29 56 00 21 00 05 00 06 00 00 00 00 **00 02**

一个方法由 访问修饰符，名称，参数描述，方法属性数量，方法属性组成

##### init

00 01 表示访问修饰符（根据访问标识为 public）

0000600 **00 01** 00 07 00 08 00 01 00 09 00 00 00 2f 00 01



00 07 表示引用了常量池 #07 项作为方法名称

0000600 00 01 **00 07** 00 08 00 01 00 09 00 00 00 2f 00 01



00 08 表示引用了常量池 #08 项作为方法参数描述

0000600 00 01 00 07 **00 08** 00 01 00 09 00 00 00 2f 00 01



00 01 表示方法属性数量，本方法是 1

0000600 00 01 00 07 00 08 **00 01** 00 09 00 00 00 2f 00 01



00 09 表示引用了常量池 #09 项，发现是【Code】属性，00 00 00 2f 表示此属性的长度是 47。

0000600 00 01 00 07 00 08 00 01 **00 09 00 00 00 2f 00 01**

0000620 **00 01 00 00 00 05 2a b7 00 01 b1 00 00 00 02 00**

0000640 **0a 00 00 00 06 00 01 00 00 00 01 00 0b 00 00 00**

0000660 **0c 00 01 00 00 00 05 00 0c 00 0d 00 00** 00 09 00

- 00 01 表示【操作数栈】最大深度。
- 00 01 表示【局部变量表】最大槽（slot）数。
- 00 00 00 05 表示字节码长度是 5。
- 2a b7 00 01 b1 是字节码指令。

00 00 00 02 表示方法细节属性数量是 2。

00 0a 表示引用了常量池 #10 项，发现是【LineNumberTable】属性。

- 00 00 00 06 表示此属性的总长度是 6。
- 00 01 表示【LineNumberTable】长度。
- 00 00 表示【字节码】行号 00 01【java 源码】行号。

00 0b 表示引用了常量池 #11 项，发现是【LocalVariableTable】属性。

- 00 00 00 0c 表示此属性的总长度是 12。
- 00 01 表示【LocalVariableTable】长度。
- 00 00 表示局部变量生命周期开始，相对于字节码的偏移量
- 00 05 表示局部变量覆盖的范围长度
- 00 0c 表示局部变量名称，本例引用了常量池 #12 项，是【this】。
- 00 0d 表示局部变量的类型，本例引用了常量池 #13 项，是【LJavaApp;】。
- 00 00 表示局部变量占有的槽位（slot）编号，本例是0。

##### main

00 09 表示访问修饰符（根据访问标识为 public static）

0000660 0c 00 01 00 00 00 05 00 0c 00 0d 00 00 **00 09** 00



00 0e 表示引用了常量池 #14 项作为方法名称

0000660 0c 00 01 00 00 00 05 00 0c 00 0d 00 00 00 09 **00**

0000700 **0e** 00 0f 00 01 00 09 00 00 00 37 00 02 00 01 00



00 0f 表示引用了常量池 #15 项作为方法参数描述

0000700 0e **00 0f** 00 01 00 09 00 00 00 37 00 02 00 01 00



00 01 表示方法属性数量，本方法是 1

0000700 0e 00 0f **00 01** 00 09 00 00 00 37 00 02 00 01 00



00 09 表示引用了常量池 #09 项，发现是【Code】属性，00 00 00 37 表示此属性的长度是 55。

0000700 0e 00 0f 00 01 **00 09 00 00 00 37 00 02 00 01 00**

0000720 **00 00 09 b2 00 02 12 03 b6 00 04 b1 00 00 00 02**

0000740 **00 0a 00 00 00 0a 00 02 00 00 00 03 00 08 00 04**

0000760 **00 0b 00 00 00 0c 00 01 00 00 00 09 00 10 00 11**

0001000 **00 00** 00 01 00 12 00 00 00 02 00 13

- 00 02 表示【操作数栈】最大深度。
- 00 01 表示【局部变量表】最大槽（slot）数。
- 00 00 00 09 表示字节码长度是 9。
- b2 00 02 12 03 b6 00 04 b1 是字节码指令。

00 00 00 02 表示方法细节属性数量是 2。

00 0a 表示引用了常量池 #10 项，发现是【LineNumberTable】属性。

- 00 00 00 0a 表示此属性的总长度是 10。
- 00 02 表示【LineNumberTable】长度。
- 00 00 表示【字节码】行号 00 03【java 源码】行号。
- 00 08 表示【字节码】行号 00 04【java 源码】行号。

00 0b 表示引用了常量池 #11 项，发现是【LocalVariableTable】属性。

- 00 00 00 0c 表示此属性的总长度是 12。
- 00 01 表示【LocalVariableTable】长度。
- 00 00 表示局部变量生命周期开始，相对于字节码的偏移量
- 00 09 表示局部变量覆盖的范围长度
- 00 10 表示局部变量名称，本例引用了常量池 #16 项，是【args】。
- 00 11 表示局部变量的类型，本例引用了常量池 #17 项，是[Ljava/lang/String;】。
- 00 00 表示局部变量占有的槽位（slot）编号，本例是0。







#### 附加信息

0001000 00 00 **00 01 00 12 00 00 00 02 00 13**

00 01 表示附加属性数量。

00 12 表示引用了常量池 #18 项，即【SourceFile】。

00 00 00 02 表示此属性的长度。

00 13 表示引用了常量池 #18 项，即【LJavaApp;】。



#### 参考文献

[Chapter 4. The class File Format (oracle.com)](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html)



### 字节码指令

#### 指令列表

[JVM详解_指令集 - 简书 (jianshu.com)](https://www.jianshu.com/p/5718ebecff84)



[The Java® Virtual Machine Specification (oracle.com)](https://docs.oracle.com/javase/specs/jvms/se8/html/index.html)

#### 字节码解读

##### init

```shell
# 构造函数字节码指令
2a b7 00 01 b1
aload_0 = 42 (0x2a)
invokespecial = 183 (0xb7)
return = 177 (0xb1)
```

1. 2a => aload_0 加载 slot 0 的局部变量到操作数栈，即 this，做为下面的 invokespecial 构造方法调用的参数
2. b7 => invokespecial 预备调用构造方法
3. 00 01 引用常量池中 #1 项， 即【Method java/lang/Object."<init>":()V】
4. b1 => return 表示返回



##### main

main 函数字节码指令

```shell
# 构造函数字节码指令
b2 00 02 12 03 b6 00 04 b1
getstatic = 178 (0xb2)
ldc = 18 (0x12)
invokevirtual = 182 (0xb6)
return = 177 (0xb1)
```

1. b2 => getstatic 加载静态变量
2. 00 02 引用常量池中 #2 项， 即【java/lang/System.out:Ljava/io/PrintStream;】
3. 12 => ldc 加载参数
4. 03 引用常量池中 #3 项， 即【hello world】
5. b6 => invokevirtual 预备调用成员方法
6. 00 04 引用常量池中 #4 项， 即【Method java/io/PrintStream.println:(Ljava/lang/String;)V】
7. b1 => return 表示返回



#### javap 工具

```shell
# javap -v JavaApp.class
# -v 显示详细信息
E:\develop\code\java\jvm\out\production\jvm> javap -v JavaApp.class
Classfile /E:/develop/code/java/jvm/out/production/jvm/JavaApp.class
  Last modified 2022年8月17日; size 524 bytes
  MD5 checksum 7c885a2b9385027997581b72fac41fd9
  Compiled from "JavaApp.java"
public class JavaApp
  minor version: 0
  major version: 55
  flags: (0x0021) ACC_PUBLIC, ACC_SUPER
  this_class: #5                          // JavaApp
  super_class: #6                         // java/lang/Object
  interfaces: 0, fields: 0, methods: 2, attributes: 1
Constant pool:
   #1 = Methodref          #6.#20         // java/lang/Object."<init>":()V
   #2 = Fieldref           #21.#22        // java/lang/System.out:Ljava/io/PrintStream;
   #3 = String             #23            // hello world
   #4 = Methodref          #24.#25        // java/io/PrintStream.println:(Ljava/lang/String;)V
   #5 = Class              #26            // JavaApp
   #6 = Class              #27            // java/lang/Object
   #7 = Utf8               <init>
   #8 = Utf8               ()V
   #9 = Utf8               Code
  #10 = Utf8               LineNumberTable
  #11 = Utf8               LocalVariableTable
  #12 = Utf8               this
  #13 = Utf8               LJavaApp;
  #14 = Utf8               main
  #15 = Utf8               ([Ljava/lang/String;)V
  #16 = Utf8               args
  #17 = Utf8               [Ljava/lang/String;
  #18 = Utf8               SourceFile
  #19 = Utf8               JavaApp.java
  #20 = NameAndType        #7:#8          // "<init>":()V
  #21 = Class              #28            // java/lang/System
  #22 = NameAndType        #29:#30        // out:Ljava/io/PrintStream;
  #23 = Utf8               hello world
  #24 = Class              #31            // java/io/PrintStream
  #25 = NameAndType        #32:#33        // println:(Ljava/lang/String;)V
  #26 = Utf8               JavaApp
  #27 = Utf8               java/lang/Object
  #28 = Utf8               java/lang/System
  #29 = Utf8               out
  #30 = Utf8               Ljava/io/PrintStream;
  #31 = Utf8               java/io/PrintStream
  #32 = Utf8               println
  #33 = Utf8               (Ljava/lang/String;)V
{
  public JavaApp();
    descriptor: ()V
    flags: (0x0001) ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 1: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   LJavaApp;

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=1, args_size=1
         0: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
         3: ldc           #3                  // String hello world
         5: invokevirtual #4                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
         8: return
      LineNumberTable:
        line 3: 0
        line 4: 8
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       9     0  args   [Ljava/lang/String;
}
SourceFile: "JavaApp.java"

```



#### 图解方法执行流程

##### 原始 java 代码

```java
public class JavaApp {
    public static void main(String [] args) {
        int a = 10;
        int b = Short.MAX_VALUE + 1;
        int c = a + b;
        System.out.println(c);
    }
}
```



##### 反编译后的字节码文件内容

```shell
PS E:\develop\code\java\jvm\out\production\jvm> javap -v JavaApp.class
Classfile /E:/develop/code/java/jvm/out/production/jvm/JavaApp.class
  Last modified 2022年8月24日; size 583 bytes
  MD5 checksum 8aebb6be38d08be95f671e48b13c6786
  Compiled from "JavaApp.java"
public class JavaApp
  minor version: 0
  major version: 55
  flags: (0x0021) ACC_PUBLIC, ACC_SUPER
  this_class: #6                          // JavaApp
  super_class: #7                         // java/lang/Object
  interfaces: 0, fields: 0, methods: 2, attributes: 1
Constant pool:
   #1 = Methodref          #7.#25         // java/lang/Object."<init>":()V
   #2 = Class              #26            // java/lang/Short
   #3 = Integer            32768
   #4 = Fieldref           #27.#28        // java/lang/System.out:Ljava/io/PrintStream;
   #5 = Methodref          #29.#30        // java/io/PrintStream.println:(I)V
   #6 = Class              #31            // JavaApp
   #7 = Class              #32            // java/lang/Object
   #8 = Utf8               <init>
   #9 = Utf8               ()V
  #10 = Utf8               Code
  #11 = Utf8               LineNumberTable
  #12 = Utf8               LocalVariableTable
  #13 = Utf8               this
  #14 = Utf8               LJavaApp;
  #15 = Utf8               main
  #16 = Utf8               ([Ljava/lang/String;)V
  #17 = Utf8               args
  #18 = Utf8               [Ljava/lang/String;
  #19 = Utf8               a
  #20 = Utf8               I
  #21 = Utf8               b
  #22 = Utf8               c
  #23 = Utf8               SourceFile
  #24 = Utf8               JavaApp.java
  #25 = NameAndType        #8:#9          // "<init>":()V
  #26 = Utf8               java/lang/Short
  #27 = Class              #33            // java/lang/System
  #28 = NameAndType        #34:#35        // out:Ljava/io/PrintStream;
  #29 = Class              #36            // java/io/PrintStream
  #30 = NameAndType        #37:#38        // println:(I)V
  #31 = Utf8               JavaApp
  #32 = Utf8               java/lang/Object
  #33 = Utf8               java/lang/System
  #34 = Utf8               out
  #35 = Utf8               Ljava/io/PrintStream;
  #36 = Utf8               java/io/PrintStream
  #37 = Utf8               println
  #38 = Utf8               (I)V
{
  public JavaApp();
    descriptor: ()V
    flags: (0x0001) ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 1: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   LJavaApp;

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=4, args_size=1
         0: bipush        10
         2: istore_1
         3: ldc           #3                  // int 32768
         5: istore_2
         6: iload_1
         7: iload_2
         8: iadd
         9: istore_3
        10: getstatic     #4                  // Field java/lang/System.out:Ljava/io/PrintStream;
        13: iload_3
        14: invokevirtual #5                  // Method java/io/PrintStream.println:(I)V
        17: return
      LineNumberTable:
        line 3: 0
        line 4: 3
        line 5: 6
        line 6: 10
        line 7: 17
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      18     0  args   [Ljava/lang/String;
            3      15     1     a   I
            6      12     2     b   I
           10       8     3     c   I
}
SourceFile: "JavaApp.java"

```



##### 常量池载入运行时常量池

![image-20220824215832233](D:\documents\notes\solid\Java.assets\031.jpg)



##### 方法字节码载入方法区

![image-20220824220259780](D:\documents\notes\solid\Java.assets\032.jpg)



main 线程开始运行，分配栈帧内存

stack=2, locals=4, args_size=1

栈帧左边绿色区域（局部变量表）：栈帧根据局部变量表的变量数量（locals）分配 4 个槽位。

栈帧右边蓝色区域（操作数栈）：根据最大栈深（stack）分配长度为 2 的栈。

![image-20220824220409748](D:\documents\notes\solid\Java.assets\033.jpg)



##### 执行引擎开始执行字节码

###### bipush 10

- 将一个 byte 压入操作数栈（其长度会补齐 4 个字节），类似的指令还有：
- sipush 将一个 short 压入操作数栈（其长度会补齐 4 个字节）
- ldc 将一个 int 压入操作数栈
- ldc2_w 将一个 long 压入操作数栈（分两次压入，因为 long 是 8 个字节）
- 这里小的数字都是喝字节码指令存在一起，超过 short 范围的数字存入了常量池

![image-20220824222035534](D:\documents\notes\solid\Java.assets\034.jpg)



###### istore_1

- 将操作数栈顶数据出栈，存入局部变量表的 slot 1

![image-20220824222303286](D:\documents\notes\solid\Java.assets\035.jpg)



![image-20220824222427231](D:\documents\notes\solid\Java.assets\036.jpg)



###### ldc #3

- 从常量池加载 #3 数据到操作数栈
- 注意 Short.MAX_VALUE 是 32767，所以 32768 = Short.MAX_VALUE + 1 实际是在编译期间计算好的

![image-20220824222737605](D:\documents\notes\solid\Java.assets\037.jpg)



###### istore_2

将操作数栈顶数据出栈，存入局部变量表的 slot 2

![image-20220824222820676](D:\documents\notes\solid\Java.assets\038.jpg)



![image-20220824223104437](D:\documents\notes\solid\Java.assets\039.jpg)



###### iload_1



![image-20220824223347591](D:\documents\notes\solid\Java.assets\040.jpg)



###### iload_2



![image-20220824223417636](D:\documents\notes\solid\Java.assets\041.jpg)



###### iadd



![image-20220824223514348](D:\documents\notes\solid\Java.assets\042.jpg)



###### istore_3



![image-20220824223633610](D:\documents\notes\solid\Java.assets\043.jpg)



![image-20220824223722050](D:\documents\notes\solid\Java.assets\044.jpg)



###### getstatic #4



![image-20220824223819070](D:\documents\notes\solid\Java.assets\045.jpg)



![image-20220824223934416](D:\documents\notes\solid\Java.assets\046.jpg)



###### iload_3



![image-20220824224118335](D:\documents\notes\solid\Java.assets\047.jpg)



###### invokevirtual #5

- 找到常量池 #5 项
- 定位到方法区 Method java/io/PrintStream.println:(I)V 方法
- 生成新的栈帧（分配 locals、stack 等）
- 传递参数，执行新栈帧中的字节码

![image-20220824224427626](D:\documents\notes\solid\Java.assets\048.jpg)



- 执行完毕，弹出栈帧

- 清楚 main 操作数栈内容

![image-20220824224640115](D:\documents\notes\solid\Java.assets\049.jpg)



###### return

- 完成 main 方法调用，弹出 main 栈帧
- 程序结束



#### 分析 i++

##### 原始 java 代码

```java
public class JavaApp {
    public static void main(String [] args) {
        int a = 10;
        int b = a++ + ++a + a--;
        System.out.println(a);
        System.out.println(b);
    }
}

```



##### 反编译后的字节码文件内容

```shell
PS E:\develop\code\java\jvm\out\production\jvm> javap -v JavaApp.class
Classfile /E:/develop/code/java/jvm/out/production/jvm/JavaApp.class
  Last modified 2022年8月25日; size 558 bytes
  MD5 checksum 3e1b4120f5597e33e27729a437fac72f
  Compiled from "JavaApp.java"
public class JavaApp
  minor version: 0
  major version: 55
  flags: (0x0021) ACC_PUBLIC, ACC_SUPER
  this_class: #4                          // JavaApp
  super_class: #5                         // java/lang/Object
  interfaces: 0, fields: 0, methods: 2, attributes: 1
Constant pool:
   #1 = Methodref          #5.#22         // java/lang/Object."<init>":()V
   #2 = Fieldref           #23.#24        // java/lang/System.out:Ljava/io/PrintStream;
   #3 = Methodref          #25.#26        // java/io/PrintStream.println:(I)V
   #4 = Class              #27            // JavaApp
   #5 = Class              #28            // java/lang/Object
   #6 = Utf8               <init>
   #7 = Utf8               ()V
   #8 = Utf8               Code
   #9 = Utf8               LineNumberTable
  #10 = Utf8               LocalVariableTable
  #11 = Utf8               this
  #12 = Utf8               LJavaApp;
  #13 = Utf8               main
  #14 = Utf8               ([Ljava/lang/String;)V
  #15 = Utf8               args
  #16 = Utf8               [Ljava/lang/String;
  #17 = Utf8               a
  #18 = Utf8               I
  #19 = Utf8               b
  #20 = Utf8               SourceFile
  #21 = Utf8               JavaApp.java
  #22 = NameAndType        #6:#7          // "<init>":()V
  #23 = Class              #29            // java/lang/System
  #24 = NameAndType        #30:#31        // out:Ljava/io/PrintStream;
  #25 = Class              #32            // java/io/PrintStream
  #26 = NameAndType        #33:#34        // println:(I)V
  #27 = Utf8               JavaApp
  #28 = Utf8               java/lang/Object
  #29 = Utf8               java/lang/System
  #30 = Utf8               out
  #31 = Utf8               Ljava/io/PrintStream;
  #32 = Utf8               java/io/PrintStream
  #33 = Utf8               println
  #34 = Utf8               (I)V
{
  public JavaApp();
    descriptor: ()V
    flags: (0x0001) ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 1: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   LJavaApp;

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=3, args_size=1
         0: bipush        10
         2: istore_1
         3: iload_1
         4: iinc          1, 1
         7: iinc          1, 1
        10: iload_1
        11: iadd
        12: iload_1
        13: iinc          1, -1
        16: iadd
        17: istore_2
        18: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
        21: iload_1
        22: invokevirtual #3                  // Method java/io/PrintStream.println:(I)V
        25: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
        28: iload_2
        29: invokevirtual #3                  // Method java/io/PrintStream.println:(I)V
        32: return
      LineNumberTable:
        line 3: 0
        line 4: 3
        line 5: 18
        line 6: 25
        line 7: 32
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      33     0  args   [Ljava/lang/String;
            3      30     1     a   I
           18      15     2     b   I
}
SourceFile: "JavaApp.java"

```



##### 分析

注意 iinc 指令是直接在局部变量 slot 上进行运算

a++ 和 ++a 的区别是先执行 iload 还是先执行 iinc

![image-20220825220623066](D:\documents\notes\solid\Java.assets\050.jpg)

![image-20220825220816258](D:\documents\notes\solid\Java.assets\051.jpg)

![image-20220825220947520](D:\documents\notes\solid\Java.assets\052.jpg)

![image-20220825221034946](D:\documents\notes\solid\Java.assets\053.jpg)

![image-20220825221118100](D:\documents\notes\solid\Java.assets\054.jpg)

![image-20220825221340496](D:\documents\notes\solid\Java.assets\055.jpg)

![image-20220825221232406](D:\documents\notes\solid\Java.assets\056.jpg)

![image-20220825221432500](D:\documents\notes\solid\Java.assets\057.jpg)

![image-20220825221538734](D:\documents\notes\solid\Java.assets\058.jpg)

![image-20220825221634613](D:\documents\notes\solid\Java.assets\059.jpg)

![image-20220825221742905](D:\documents\notes\solid\Java.assets\060.jpg)



#### 条件判断指令

##### 指令

| 指令 | 助记符    | 含义             |
| ---- | --------- | ---------------- |
| 0x99 | ifeq      | 判断是否 == 0    |
| 0x9a | ifne      | 判断是否 != 0    |
| 0x9b | iflt      | 判断是否 < 0     |
| 0x9c | ifge      | 判断是否 >= 0    |
| 0x9d | ifgt      | 判断是否 > 0     |
| 0x9e | ifle      | 判断是否 <= 0    |
| 0x9f | if_icmpeq | 两个 int 是否 == |
| 0xa0 | if_icmpne | 两个 int 是否 != |
| 0xa1 | if_icmplt | 两个 int 是否 <  |
| 0xa2 | if_icmpge | 两个 int 是否 >= |
| 0xa3 | if_icmpgt | 两个 int 是否 >  |
| 0xa4 | if_icmple | 两个 int 是否 <= |
| 0xa5 | if_icmpeq | 两个引用是否 ==  |
| 0xa6 | if_icmpne | 两个引用是否 !=  |
| 0xc6 | ifnull    | 判断是否 == null |
| 0xc7 | ifnonnull | 判断是否 != null |

说明：

- byte、short、char 都会按 int 比较，因为操作数栈都是 4 字节
- goto 用来进行跳转到指定行号的字节码
- long、float、double 的比较，查阅 [Chapter 6. The Java Virtual Machine Instruction Set (oracle.com)](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html#jvms-6.5.lcmp)



##### java 源码

```java
public class JavaApp {
    public static void main(String [] args) {
        int a = 0;
        if(a == 0){
            a = 10;
        }else{
            a = 20;
        }
    }
}
```



##### 字节码

```shell
{
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=1, locals=2, args_size=1
         0: iconst_0
         1: istore_1
         2: iload_1
         3: ifne          12
         6: bipush        10
         8: istore_1
         9: goto          15
        12: bipush        20
        14: istore_1
        15: return
      LineNumberTable:
        line 3: 0
        line 4: 2
        line 5: 6
        line 7: 12
        line 9: 15
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      16     0  args   [Ljava/lang/String;
            2      14     1     a   I
      StackMapTable: number_of_entries = 2
        frame_type = 252 /* append */
          offset_delta = 12
          locals = [ int ]
        frame_type = 2 /* same */
}

```





#### 循环控制指令

##### 指令

循环控制使用的还是条件判断等基本指令



##### while

###### java 源码

```java
public class JavaApp {
    public static void main(String [] args) {
        int a = 0;
        while(a<10){
            a++;
        }
    }
}
```



###### 字节码

```shell
{
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=2, args_size=1
         0: iconst_0
         1: istore_1
         2: iload_1
         3: bipush        10
         5: if_icmpge     14
         8: iinc          1, 1
        11: goto          2
        14: return
      LineNumberTable:
        line 3: 0
        line 4: 2
        line 5: 8
        line 7: 14
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      15     0  args   [Ljava/lang/String;
            2      13     1     a   I
      StackMapTable: number_of_entries = 2
        frame_type = 252 /* append */
          offset_delta = 2
          locals = [ int ]
        frame_type = 11 /* same */
}

```



##### do while

###### java 源码

```java
public class JavaApp {
    public static void main(String [] args) {
        int a = 0;
        do{
            a++;
        }
        while(a<10);
    }
}
```



###### 字节码

```shell
{
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=2, args_size=1
         0: iconst_0
         1: istore_1
         2: iinc          1, 1
         5: iload_1
         6: bipush        10
         8: if_icmplt     2
        11: return
      LineNumberTable:
        line 3: 0
        line 5: 2
        line 7: 5
        line 8: 11
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      12     0  args   [Ljava/lang/String;
            2      10     1     a   I
      StackMapTable: number_of_entries = 1
        frame_type = 252 /* append */
          offset_delta = 2
          locals = [ int ]
}
```



##### for

###### java 源码

```java
public class JavaApp {
    public static void main(String [] args) {
        for(int a=0;a<10;a++){

        }
    }
}
```



###### 字节码

```shell
{
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=2, args_size=1
         0: iconst_0
         1: istore_1
         2: iload_1
         3: bipush        10
         5: if_icmpge     14
         8: iinc          1, 1
        11: goto          2
        14: return
      LineNumberTable:
        line 3: 0
        line 6: 14
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            2      12     1     a   I
            0      15     0  args   [Ljava/lang/String;
      StackMapTable: number_of_entries = 2
        frame_type = 252 /* append */
          offset_delta = 2
          locals = [ int ]
        frame_type = 250 /* chop */
          offset_delta = 11
}
```



##### 注意

比较 while 和 for 的字节码，你会发现他们是一摸一样的，殊途同归





#### 分析 x++

##### 原始 java 代码

```java
public class JavaApp {
    public static void main(String [] args) {
        int i = 0;
        int x = 0;
        while (i < 10){
            x=x++;
            i++;
        }
        System.out.println(x);
    }
}
```



##### 反编译后的字节码文件内容

```shell
{
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=3, args_size=1
         0: iconst_0
         1: istore_1
         2: iconst_0
         3: istore_2
         4: iload_1
         5: bipush        10
         7: if_icmpge     21
        10: iload_2
        11: iinc          2, 1
        14: istore_2
        15: iinc          1, 1
        18: goto          4
        21: getstatic     #7                  // Field java/lang/System.out:Ljava/io/PrintStream;
        24: iload_2
        25: invokevirtual #13                 // Method java/io/PrintStream.println:(I)V
        28: return
      LineNumberTable:
        line 3: 0
        line 4: 2
        line 5: 4
        line 6: 10
        line 7: 15
        line 9: 21
        line 10: 28
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      29     0  args   [Ljava/lang/String;
            2      27     1     i   I
            4      25     2     x   I
      StackMapTable: number_of_entries = 2
        frame_type = 253 /* append */
          offset_delta = 4
          locals = [ int, int ]
        frame_type = 16 /* same */
}
```



##### 分析

将 x 加载到操作数栈后对局部变量表中的 x 自增，然后再把操作数栈的 x 复制回局部变量表，所以 x 一直等于初始值。

```shell
2: iconst_0
3: istore_2
......
10: iload_2
11: iinc          2, 1
14: istore_2
```



#### 构造方法

##### <cinit>()V

```java
public class JavaApp {
    static int i = 10;
    static {
        i = 20;
    }

    static {
        i = 30;
    }

    public static void main(String [] args) {

    }
}
```

编译器会按照从上至下的顺序，收集所有 static 静态代码块和静态成员赋值的代码，合并为一个特殊的方法<cinit>()V

```shell
{
  static int i;
    descriptor: I
    flags: (0x0008) ACC_STATIC
    
  static {};
    descriptor: ()V
    flags: (0x0008) ACC_STATIC
    Code:
      stack=1, locals=0, args_size=0
         0: bipush        10
         2: putstatic     #7                  // Field i:I
         5: bipush        20
         7: putstatic     #7                  // Field i:I
        10: bipush        30
        12: putstatic     #7                  // Field i:I
        15: return
      LineNumberTable:
        line 2: 0
        line 4: 5
        line 8: 10
        line 9: 15
}
```

<cinit>()V 方法会在类加载的初始化阶段被调用



##### <init>()V

```java
public class JavaApp {
    private String a = "s1";
    {
        b=20;
    }

    private int b = 10;

    {
        a = "s2";
    }

    public JavaApp(String a, int b){
        this.a=a;
        this.b=b;
    }

    public static void main(String [] args) {
        JavaApp d=new JavaApp("s3", 30);
        System.out.println(d.a);
        System.out.println(d.b);
    }
}
```

编译器会按从上到下的顺序，收集所有 {} 代码块和成员变量赋值的代码，形成新的构造方法，但原始构造方法内的代码总在最后

```shell
{
  public JavaApp(java.lang.String, int);
    descriptor: (Ljava/lang/String;I)V
    flags: (0x0001) ACC_PUBLIC
    Code:
      stack=2, locals=3, args_size=3
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: aload_0
         5: ldc           #7                  // String s1
         7: putfield      #9                  // Field a:Ljava/lang/String;
        10: aload_0
        11: bipush        20
        13: putfield      #15                 // Field b:I
        16: aload_0
        17: bipush        10
        19: putfield      #15                 // Field b:I
        22: aload_0
        23: ldc           #19                 // String s2
        25: putfield      #9                  // Field a:Ljava/lang/String;
        28: aload_0
        29: aload_1
        30: putfield      #9                  // Field a:Ljava/lang/String;
        33: aload_0
        34: iload_2
        35: putfield      #15                 // Field b:I
        38: return
      LineNumberTable:
        line 13: 0
        line 2: 4
        line 4: 10
        line 7: 16
        line 10: 22
        line 14: 28
        line 15: 33
        line 16: 38
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      39     0  this   LJavaApp;
            0      39     1     a   Ljava/lang/String;
            0      39     2     b   I
}

```



#### 方法调用

看一下几种不同的方法调用对应的字节码指令

对于构造方法和静态方法执行的是 invokespecial（静态绑定），其他方法执行的是 invokevirtual（动态绑定）。

构造方法和静态方法是确定的，不能被重载，所以在编译期间会被绑定。其他方法可以被重载，所以不能被静态绑定。

在java 8 中私有方法执行的是 invokespecial，在 java 11 中对 invokespecial 这个字节码指令作了修改，所以在 java 11 中私有方法执行的是 invokevirtual。

[Java为什么对private方法使用invokevirtual指令？ - 知乎 (zhihu.com)](https://www.zhihu.com/question/439794633/answer/1682842839)

```java
public class JavaApp {
    public JavaApp(){

    }

    private void test1(){

    }

    private final void test2(){

    }

    public void test3(){

    }

    public static void test4(){

    }

    public static void main(String [] args) {
        JavaApp d=new JavaApp();
        d.test1();
        d.test2();
        d.test3();
        d.test4();
        JavaApp.test4();
    }
}

```



```shell
# java 8
{
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=2, args_size=1
         0: new           #2                  // class JavaApp
         3: dup
         4: invokespecial #3                  // Method "<init>":()V
         7: astore_1
         8: aload_1
         9: invokespecial #4                  // Method test1:()V
        12: aload_1
        13: invokespecial #5                  // Method test2:()V
        16: aload_1
        17: invokevirtual #6                  // Method test3:()V
        20: aload_1
        21: pop
        22: invokestatic  #7                  // Method test4:()V
        25: invokestatic  #7                  // Method test4:()V
        28: return
      LineNumberTable:
        line 23: 0
        line 24: 8
        line 25: 12
        line 26: 16
        line 27: 20
        line 28: 25
        line 29: 28
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      29     0  args   [Ljava/lang/String;
            8      21     1     d   LJavaApp;
}

# java 11及以上
{
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=2, args_size=1
         0: new           #7                  // class JavaApp
         3: dup
         4: invokespecial #9                  // Method "<init>":()V
         7: astore_1
         8: aload_1
         9: invokevirtual #10                 // Method test1:()V
        12: aload_1
        13: invokevirtual #13                 // Method test2:()V
        16: aload_1
        17: invokevirtual #16                 // Method test3:()V
        20: aload_1
        21: pop
        22: invokestatic  #19                 // Method test4:()V
        25: invokestatic  #19                 // Method test4:()V
        28: return
      LineNumberTable:
        line 23: 0
        line 24: 8
        line 25: 12
        line 26: 16
        line 27: 20
        line 28: 25
        line 29: 28
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      29     0  args   [Ljava/lang/String;
            8      21     1     d   LJavaApp;
}

```





#### 多态的原理

##### java 代码

```java
import java.io.IOException;

/**
 * 演示多态原理，注意加上下面的 JVM 参数，禁用指针压缩
 * -XX:-UseCompressedOops -XX:-UseCompressedClassPointers
 */
public class JavaApp {

    public static void test(Animal animal){
        animal.eat();
        System.out.println(animal.toString());
    }

    public static void main(String [] args) throws IOException {
        test(new Cat());
        test(new Dog());
        System.in.read();
    }

}

abstract class Animal{
    public abstract void eat();

    @Override
    public String toString() {
        return "我是"+this.getClass().getSimpleName();
    }
}

class Dog extends Animal{
    @Override
    public void eat() {
        System.out.println("啃骨头");
    }
}

class Cat extends Animal{
    @Override
    public void eat() {
        System.out.println("吃鱼");
    }
}

```



##### 运行代码

停在 System.in.read() 方法上，这时运行 jps 获取进程 id



##### 运行 HSDB 工具

进入 JDK 安装目录，执行

```shell
./bin/java -cp ./lib/sa-jdi.jar sun.jvm.hotspot.HSDB
```

进入图形界面 -> File -> Attach to HotSpot process -> 进程 id



##### 查找某个对象的内存地址

打开 Tools -> Find Object By Query -> 输入

```sql
-- select 别名 from 类全限名 别名
select d from Dog d
-- 0x000002ada4c27a40
```



##### 查看对象内存结构

打开 Tools -> Inspector

输入类型指针地址后回车

![image-20220906002617124](D:\documents\notes\solid\Java.assets\061.jpg)



查看对象 Class 的内存地址



打开 Windows -> Console 

输入 

```shell
# mem 开始地址 查看的word数
mem 0x000001e3a4227a40 2
# mark word
0x000001e3a4227a40: 0x0000000000000001 
# 对象的类型指针
0x000001e3a4227a48: 0x000001e3ff8241a0 
```



打开 Tools -> Inspector

输入类型指针地址后回车

![image-20220906221433930](D:\documents\notes\solid\Java.assets\062.jpg)



##### vtable

多态方法存在一张叫做vtable的虚方法表中

静态方法，final方法，私有方法不会列在虚方法表中



![image-20220906221516923](D:\documents\notes\solid\Java.assets\063.jpg)



vtable 与 对象的地址偏移量为 1b8 

0x000001E3FF824358

在对象头中可以看到 vtable 的长度

![image-20220906222330123](D:\documents\notes\solid\Java.assets\064.jpg)



```shell
hsdb> mem 0x000001E3FF824358 6
# 该对象中支持多态的方法入口地址
# Object.finalize()
0x000001e3ff824358: 0x000001e3ff421b10 
# Object.equals()
0x000001e3ff824360: 0x000001e3ff4215e8 
# Animal.toString()
0x000001e3ff824368: 0x000001e3ff823758 
# Object.hashCode()
0x000001e3ff824370: 0x000001e3ff421540 
# Object.clone()
0x000001e3ff824378: 0x000001e3ff421678 
# Dog.eat()
0x000001e3ff824380: 0x000001e3ff824148 
```



##### 验证方法地址

Tools -> Class Browser

输入类名，点击下面的地址，可以看到 eat() 方法的地址等于 vtable 中的一个地址

![image-20220906223308107](D:\documents\notes\solid\Java.assets\065.jpg)



点击父类，可以看到 toString() 方法的地址等于 vtable 中的一个地址

![image-20220906223654571](D:\documents\notes\solid\Java.assets\066.jpg)



点击 Object 类，可以看到 finalize()、equals()、hashCode()、clone() 方法的地址等于 vtable 中的一个地址

![image-20220906223929816](D:\documents\notes\solid\Java.assets\067.jpg)



##### 小结

当执行 invokevirtual 指令时

先通过栈帧中的对象引用找到对象

分析对象头，找到对象的实际 Class

Class 结构中有 vtable，它在类加载的链接阶段就已经根据方法的重写规则生成好了

查表得到方法的具体地址

执行方法的字节码



#### 异常处理

##### try-catch

Java 源码

```java
public class JavaApp {

    public static void main(String [] args) {
        int i = 0;
        try {
            i = 10;
        } catch (Exception e){
            i = 20;
        }
    }

}
```



主要字节码

```shell
{
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=1, locals=3, args_size=1
         0: iconst_0
         1: istore_1
         2: bipush        10
         4: istore_1
         5: goto          12
         8: astore_2
         9: bipush        20
        11: istore_1
        12: return
      Exception table:
         from    to  target type
             2     5     8   Class java/lang/Exception
      LineNumberTable:
        line 4: 0
        line 6: 2
        line 9: 5
        line 7: 8
        line 8: 9
        line 10: 12
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            9       3     2     e   Ljava/lang/Exception;
            0      13     0  args   [Ljava/lang/String;
            2      11     1     i   I
      StackMapTable: number_of_entries = 2
        frame_type = 255 /* full_frame */
          offset_delta = 8
          locals = [ class "[Ljava/lang/String;", int ]
          stack = [ class java/lang/Exception ]
        frame_type = 3 /* same */
}

```

 Exception table 内的代码 [from, to) 块出现异常与声明的异常类型（type）进行匹配，如果一致就会进入目标行（target）

第 8 行的字节码指令 astore_2 是将异常对象引用存入局部变量表的 Slot 2 位置



##### 多个 single-catch 块的情况

java 源码

```java
public class JavaApp {

    public static void main(String [] args) {
        int i = 0;
        try {
            i = 10;
        } catch (ArithmeticException e){
            i = 20;
        } catch (NullPointerException e){
            i = 30;
        } catch (Exception e){
            i = 40;
        }
    }

}
```



主要字节码

```shell
{
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=1, locals=3, args_size=1
         0: iconst_0
         1: istore_1
         2: bipush        10
         4: istore_1
         5: goto          26
         8: astore_2
         9: bipush        20
        11: istore_1
        12: goto          26
        15: astore_2
        16: bipush        30
        18: istore_1
        19: goto          26
        22: astore_2
        23: bipush        40
        25: istore_1
        26: return
      Exception table:
         from    to  target type
             2     5     8   Class java/lang/ArithmeticException
             2     5    15   Class java/lang/NullPointerException
             2     5    22   Class java/lang/Exception
      LineNumberTable:
        line 4: 0
        line 6: 2
        line 13: 5
        line 7: 8
        line 8: 9
        line 13: 12
        line 9: 15
        line 10: 16
        line 13: 19
        line 11: 22
        line 12: 23
        line 14: 26
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            9       3     2     e   Ljava/lang/ArithmeticException;
           16       3     2     e   Ljava/lang/NullPointerException;
           23       3     2     e   Ljava/lang/Exception;
            0      27     0  args   [Ljava/lang/String;
            2      25     1     i   I
      StackMapTable: number_of_entries = 4
        frame_type = 255 /* full_frame */
          offset_delta = 8
          locals = [ class "[Ljava/lang/String;", int ]
          stack = [ class java/lang/ArithmeticException ]
        frame_type = 70 /* same_locals_1_stack_item */
          stack = [ class java/lang/NullPointerException ]
        frame_type = 70 /* same_locals_1_stack_item */
          stack = [ class java/lang/Exception ]
        frame_type = 3 /* same */
}
```

 

因为异常出现时，只能进入 Exception table 中一个分支，所以局部变量表 Slot 2 位置被共用。



##### multi-catch 的情况

java 源码

```java
public class JavaApp {

    public static void main(String [] args) {
        try {
            Method test = JavaApp.class.getMethod("test");
            test.invoke(null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }

    public static void test(){
        System.out.println("ok");
    }
}
```



主要字节码

```shell
{
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=3, locals=2, args_size=1
         0: ldc           #2                  // class JavaApp
         2: ldc           #3                  // String test
         4: iconst_0
         5: anewarray     #4                  // class java/lang/Class
         8: invokevirtual #5                  // Method java/lang/Class.getMethod:(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
        11: astore_1
        12: aload_1
        13: aconst_null
        14: iconst_0
        15: anewarray     #6                  // class java/lang/Object
        18: invokevirtual #7                  // Method java/lang/reflect/Method.invoke:(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
        21: pop
        22: goto          30
        25: astore_1
        26: aload_1
        27: invokevirtual #11                 // Method java/lang/ReflectiveOperationException.printStackTrace:()V
        30: return
      Exception table:
         from    to  target type
             0    22    25   Class java/lang/NoSuchMethodException
             0    22    25   Class java/lang/IllegalAccessException
             0    22    25   Class java/lang/reflect/InvocationTargetException
      LineNumberTable:
        line 8: 0
        line 9: 12
        line 12: 22
        line 10: 25
        line 11: 26
        line 13: 30
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
           12      10     1  test   Ljava/lang/reflect/Method;
           26       4     1     e   Ljava/lang/ReflectiveOperationException;
            0      31     0  args   [Ljava/lang/String;
      StackMapTable: number_of_entries = 2
        frame_type = 89 /* same_locals_1_stack_item */
          stack = [ class java/lang/ReflectiveOperationException ]
        frame_type = 4 /* same */

  public static void test();
    descriptor: ()V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=0, args_size=0
         0: getstatic     #12                 // Field java/lang/System.out:Ljava/io/PrintStream;
         3: ldc           #13                 // String ok
         5: invokevirtual #14                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
         8: return
      LineNumberTable:
        line 16: 0
        line 17: 8
}
```

与多个 single-catch 情况相比异常处理的字节码为相同一段。



##### finally

java 源码

```java
public class JavaApp {

    public static void main(String [] args) {
        int i = 0;
        try {
            i = 10;
        } catch (Exception e){
            i = 20;
        } finally {
            i = 30;
        }
    }
}
```



主要字节码

```shell
{
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=1, locals=4, args_size=1
         0: iconst_0
         1: istore_1
         2: bipush        10
         4: istore_1
         5: bipush        30
         7: istore_1
         8: goto          27
        11: astore_2
        12: bipush        20
        14: istore_1
        15: bipush        30
        17: istore_1
        18: goto          27
        21: astore_3
        22: bipush        30
        24: istore_1
        25: aload_3
        26: athrow
        27: return
      Exception table:
         from    to  target type
             2     5    11   Class java/lang/Exception
             2     5    21   any
            11    15    21   any
      LineNumberTable:
        line 4: 0
        line 6: 2
        line 10: 5
        line 11: 8
        line 7: 11
        line 8: 12
        line 10: 15
        line 11: 18
        line 10: 21
        line 11: 25
        line 12: 27
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
           12       3     2     e   Ljava/lang/Exception;
            0      28     0  args   [Ljava/lang/String;
            2      26     1     i   I
      StackMapTable: number_of_entries = 3
        frame_type = 255 /* full_frame */
          offset_delta = 11
          locals = [ class "[Ljava/lang/String;", int ]
          stack = [ class java/lang/Exception ]
        frame_type = 73 /* same_locals_1_stack_item */
          stack = [ class java/lang/Throwable ]
        frame_type = 5 /* same */
}
```



可以看到 finally 中的代码被复制了 3 份，分别放入 try 流程，catch 流程以及catch 剩余的异常类型流程。

athrow 当抛出了未被 catch 块捕获到的异常，或者 catch 块内的代码发生了异常。



##### finally 出现了 return

java 源码

```java
public class JavaApp {

    public static void main(String [] args) {
        int result = test();
        System.out.println(result);
    }

    public static int test(){
        try{
            return 10;
        } finally {
            return 20;
        }
    }
}
```



主要字节码

```shell
{
  public static int test();
    descriptor: ()I
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=1, locals=2, args_size=0
         0: bipush        10
         2: istore_0
         3: bipush        20
         5: ireturn
         6: astore_1
         7: bipush        20
         9: ireturn
      Exception table:
         from    to  target type
             0     3     6   any
      LineNumberTable:
        line 10: 0
        line 12: 3
      StackMapTable: number_of_entries = 1
        frame_type = 70 /* same_locals_1_stack_item */
          stack = [ class java/lang/Throwable ]
}

```



由于 finally 中的 ireturn 被插入了所有可能的流程，因此返回结果肯定以 finally 的为准。

至于字节码中第 2 行，似乎没啥用，且看下个例子

跟上例中的 finally 相比，发现没有了 athrow ，这告诉我们：如果在 finally 中出现 retuen，会吞掉异常。

```java
public class JavaApp {

    public static void main(String [] args) {
        int result = test();
        System.out.println(result);
    }

    public static int test(){
        try{
            int i = 1/0;
            return 10;
        } finally {
            return 20;
        }
    }
}
```



##### finally 对返回值影响

java 源码

```java
public class JavaApp {

    public static void main(String [] args) {
        int result = test();
        System.out.println(result);
    }

    public static int test(){
        int i = 10;
        try{
            return i;
        } finally {
            i = 20;
        }
    }
}
```



主要字节码

```shell
{
  public static int test();
    descriptor: ()I
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=1, locals=3, args_size=0
         0: bipush        10
         2: istore_0
         3: iload_0
         4: istore_1
         5: bipush        20
         7: istore_0
         8: iload_1
         9: ireturn
        10: astore_2
        11: bipush        20
        13: istore_0
        14: aload_2
        15: athrow
      Exception table:
         from    to  target type
             3     5    10   any
      LineNumberTable:
        line 9: 0
        line 11: 3
        line 13: 5
        line 11: 8
        line 13: 10
        line 14: 14
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            3      13     0     i   I
      StackMapTable: number_of_entries = 1
        frame_type = 255 /* full_frame */
          offset_delta = 10
          locals = [ int ]
          stack = [ class java/lang/Throwable ]
}
```



返回之前  4: istore_1，先把返回值暂存到 Slot 1，等到返回时，再把 Slot 1 中的值取出来。



##### synchronized

在同步时进行加锁解锁，为了保证加锁之后一定能将锁释放，使用了异常处理表进行异常处理，使得同步代码执行结束之后一定能释放锁。

- 注意：方法级别的 synchronized 不会在字节码指令中有所体现。

java 源码

```java
public class JavaApp {

    public static void main(String [] args) {
        Object lock = new Object();
        synchronized (lock){
            System.out.println("ok");
        }
    }
}
```



主要字节码

```shell
{
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=4, args_size=1
         0: new           #2                  // class java/lang/Object
         3: dup
         4: invokespecial #1                  // Method java/lang/Object."<init>":()V
         7: astore_1
         8: aload_1
         9: dup
        10: astore_2				# 将锁对象存储在 Slot 2 给 monitorenter 使用
        11: monitorenter			
        12: getstatic     #3                  // Field java/lang/System.out:Ljava/io/PrintStream;
        15: ldc           #4                  // String ok
        17: invokevirtual #5                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        20: aload_2					# 将 Slot 2 中的锁对象加载到操作数栈顶给 monitorexit 使用
        21: monitorexit
        22: goto          30
        25: astore_3
        26: aload_2
        27: monitorexit
        28: aload_3
        29: athrow
        30: return
      Exception table:
         from    to  target type
            12    22    25   any
            25    28    25   any
      LineNumberTable:
        line 4: 0
        line 5: 8
        line 6: 12
        line 7: 20
        line 8: 30
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      31     0  args   [Ljava/lang/String;
            8      23     1  lock   Ljava/lang/Object;
      StackMapTable: number_of_entries = 2
        frame_type = 255 /* full_frame */
          offset_delta = 25
          locals = [ class "[Ljava/lang/String;", class java/lang/Object, class java/lang/Object ]
          stack = [ class java/lang/Throwable ]
        frame_type = 250 /* chop */
          offset_delta = 4
}
SourceFile: "JavaApp.java"

```







### 编译期处理

所谓的语法糖，其实就是 java 编译器把 *.java 源码编译为 *.class 字节码的过程中，自动生成和转换的一些代码，主要是为了减轻程序员的负担，算是 java 编译器给我们的一个额外福利（给糖吃嘛）

注意，以下代码的分析，借助了 javap 工具， idea 的反编译功能， idea 插件 jclasslib 等工具．另外，编译器转换的结果直接就是 class 字节码，只是为了便于阅读，给出了几乎等价的 java 源码方式，并不是编译器还会转换出中间的 java 源码，切记。



#### 默认构造器

##### java 源码

```java
public class JavaApp {

    public static void main(String [] args) {

    }
}
```



##### 主要字节码

在类中没有实现构造函数的情况下，

编译之后自动生成无参构造函数，并在无参构造函数中调用父类无参构造函数。

```shell
{
  public JavaApp();
    descriptor: ()V
    flags: (0x0001) ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 1: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   LJavaApp;
}
SourceFile: "JavaApp.java"

```



#### 自动拆装箱

这个特性是 JDK 5 开始加入的

##### 代码片段 1

```java
public class JavaApp {

    public static void main(String [] args) {
        Integer x = 1;
        int y = x;
    }
}
```

这段代码在 JDK 5 之前是无法编译通过的，必须改写为

##### 代码片段 2

```java
public class JavaApp {

    public static void main(String [] args) {
        Integer x = Integer.valueOf(1);
        int y = x.intValue();
    }
}
```

显然之前版本的代码太麻烦了，需要在基本类型和包装类型之间来回转换（尤其是集合类中操作的都是包装类型），因此这些转换的事情在 JDK 5 以后都由编译器在编译阶段完成。即 代码片段 1 都会在编译阶段被转换为 代码片段 2。

##### 代码片段 1 主要字节码

```shell
{
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=1, locals=3, args_size=1
         0: iconst_1
         1: invokestatic  #2                  // Method java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
         4: astore_1
         5: aload_1
         6: invokevirtual #3                  // Method java/lang/Integer.intValue:()I
         9: istore_2
        10: return
      LineNumberTable:
        line 4: 0
        line 5: 5
        line 6: 10
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      11     0  args   [Ljava/lang/String;
            5       6     1     x   Ljava/lang/Integer;
           10       1     2     y   I
}
```



#### 泛型集合取值

##### 泛型擦除

泛型也是在 JDK 5 开始加入的特性，但 java 在编译泛型代码后会执行 泛型擦除 的动作，即泛型信息在编译为字节码之后就丢失了，实际的类型都当做了 Object 类型来处理：

```java
public class JavaApp {

    public static void main(String [] args) {
        List<Integer> list = new ArrayList<>();
        list.add(10);// 实际调用的是 List.add(Object e)
        Integer x = list.get(0);// 实际调用的是 Object obj = List.get(int index);
    }
}
```

所以在取值时，编译器真正生成的字节码中，还要额外做一个类型转换的操作：

```java
Integer x = (Integer)list.get(0);
```

主要字节码

```java
{
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=3, args_size=1
         0: new           #2                  // class java/util/ArrayList
         3: dup
         4: invokespecial #3                  // Method java/util/ArrayList."<init>":()V
         7: astore_1
         8: aload_1
         9: bipush        10
        11: invokestatic  #4                  // Method java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
        14: invokeinterface #5,  2            // InterfaceMethod java/util/List.add:(Ljava/lang/Object;)Z
        19: pop
        20: aload_1
        21: iconst_0
        22: invokeinterface #6,  2            // InterfaceMethod java/util/List.get:(I)Ljava/lang/Object;
        27: checkcast     #7                  // class java/lang/Integer
        30: astore_2
        31: return
      LineNumberTable:
        line 7: 0
        line 8: 8
        line 9: 20
        line 10: 31
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      32     0  args   [Ljava/lang/String;
            8      24     1  list   Ljava/util/List;
           31       1     2     x   Ljava/lang/Integer;
      LocalVariableTypeTable:
        Start  Length  Slot  Name   Signature
            8      24     1  list   Ljava/util/List<Ljava/lang/Integer;>;
}
```

如果前面的 x 变量类型修改为 int 基本类型那么最终生成的字节码是：

```java
int x = ((Integer)list.get(0)).intValue();
```

主要字节码

```shell
{
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=3, args_size=1
         0: new           #2                  // class java/util/ArrayList
         3: dup
         4: invokespecial #3                  // Method java/util/ArrayList."<init>":()V
         7: astore_1
         8: aload_1
         9: bipush        10
        11: invokestatic  #4                  // Method java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
        14: invokeinterface #5,  2            // InterfaceMethod java/util/List.add:(Ljava/lang/Object;)Z
        19: pop
        20: aload_1
        21: iconst_0
        22: invokeinterface #6,  2            // InterfaceMethod java/util/List.get:(I)Ljava/lang/Object;
        27: checkcast     #7                  // class java/lang/Integer
        30: invokevirtual #8                  // Method java/lang/Integer.intValue:()I
        33: istore_2
        34: return
      LineNumberTable:
        line 7: 0
        line 8: 8
        line 9: 20
        line 10: 34
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      35     0  args   [Ljava/lang/String;
            8      27     1  list   Ljava/util/List;
           34       1     2     x   I
      LocalVariableTypeTable:
        Start  Length  Slot  Name   Signature
            8      27     1  list   Ljava/util/List<Ljava/lang/Integer;>;
}
```

还好这些麻烦事都不用自己做。

##### 获取泛型信息

擦除的是字节码上的泛型信息，可以看到 LocalVariableTypeTable 仍然保留了方法参数泛型的信息。

局部变量的泛型信息无法获取，只有参数、返回值的泛型信息才能通过使用反射，够获得这些信息：

```java
public class JavaApp {

    public static void main(String [] args) throws NoSuchMethodException {
        Method test = JavaApp.class.getMethod("test", List.class, Map.class);
        Type[] types = test.getGenericParameterTypes();
        for(Type type:types){
            print(type);
        }
        print(test.getGenericReturnType());
    }

    private static void print(Type type){
        if(type instanceof ParameterizedType){ // 参数化类型
            ParameterizedType parameterizedType = (ParameterizedType) type;
            System.out.println("原始类型 - " + parameterizedType.getRawType());
            Type[] arguments = parameterizedType.getActualTypeArguments();
            for(int i = 0;i < arguments.length; i++){
                System.out.printf("泛型参数[%d] - %s\n", i, arguments[i]);
            }
        }
    }

    public Set<Integer> test(List<Integer> list, Map<Integer, Object> map){
        return null;
    }
}
```





#### 可变参数

可变参数也是 JDK 5 开始加入的新特性：

##### java 源码

###### 可变参数

```java
public class JavaApp {

    public static void main(String [] args) {
        foo("hello", "world");
    }

    private static void foo(String... args){
        String[] array = args;
        System.out.println(array);
    }
}
```

可变参数 String... args 其实是一个 String[] args，从代码中的赋值语句中就可以看出来。

同样 java 编译器会在编译期间将上述代码变换为：

```java
public class JavaApp {

    public static void main(String [] args) {
        foo(new String[]{"hello", "world"});
    }

    private static void foo(String[] args){
        String[] array = args;
        System.out.println(array);
    }
}
```

###### 注意

如果调用了 foo() 则等价代码为 foo(new String[]{})，创建了一个空数组，而不会传递 null 进去



##### 主要字节码

```shell
{
  public JavaApp();
    descriptor: ()V
    flags: (0x0001) ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 2: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   LJavaApp;

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=4, locals=1, args_size=1
         0: iconst_2
         1: anewarray     #2                  // class java/lang/String
         4: dup
         5: iconst_0
         6: ldc           #3                  // String hello
         8: aastore
         9: dup
        10: iconst_1
        11: ldc           #4                  // String world
        13: aastore
        14: invokestatic  #5                  // Method foo:([Ljava/lang/String;)V
        17: return
      LineNumberTable:
        line 5: 0
        line 6: 17
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      18     0  args   [Ljava/lang/String;
}
```



#### foreach 循环

仍是 JDK 5 开始引入的语法糖

##### Java 源码

###### 数组的循环：

```java
public class JavaApp {

    public static void main(String [] args) {
        int[] array = {1, 2, 3, 4, 5};
        for(int e:array){
            System.out.println(e);
        }
    }
}
```

会被编译器转换为：

```java
public class JavaApp {

    public static void main(String [] args) {
        int[] array = {1, 2, 3, 4, 5};
        for(int i = 0:i < array.length; i++){
            int e = array[i];
            System.out.println(e);
        }
    }
}
```

###### 集合的循环：

```java
public class JavaApp {

    public static void main(String [] args) {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        for(Integer e:array){
            System.out.println(e);
        }
    }
}
```

实际被编译器转换为对迭代器的调用：

```java
public class JavaApp {

    public static void main(String [] args) {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        Iterator iter = list.iterator();
        while(iter.hasNext()){
        	Integer e = (Integer)iter.next();
        	System.out.println(e);
        }
    }
}
```

###### 注意

foreach 循环写法，能够配合数组，以及所有实现了 Iterable 接口的集合类一起使用，其中 Iterable 用来获取集合的迭代器（Iterator）



##### 主要字节码

```shell
{
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=4, locals=6, args_size=1
         0: iconst_5
         1: newarray       int
         3: dup
         4: iconst_0
         5: iconst_1
         6: iastore
         7: dup
         8: iconst_1
         9: iconst_2
        10: iastore
        11: dup
        12: iconst_2
        13: iconst_3
        14: iastore
        15: dup
        16: iconst_3
        17: iconst_4
        18: iastore
        19: dup
        20: iconst_4
        21: iconst_5
        22: iastore
        23: astore_1
        24: aload_1
        25: astore_2
        26: aload_2
        27: arraylength
        28: istore_3
        29: iconst_0
        30: istore        4
        32: iload         4
        34: iload_3
        35: if_icmpge     58
        38: aload_2
        39: iload         4
        41: iaload
        42: istore        5
        44: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
        47: iload         5
        49: invokevirtual #3                  // Method java/io/PrintStream.println:(I)V
        52: iinc          4, 1
        55: goto          32
        58: return
      LineNumberTable:
        line 5: 0
        line 6: 24
        line 7: 44
        line 6: 52
        line 9: 58
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
           44       8     5     e   I
            0      59     0  args   [Ljava/lang/String;
           24      35     1 array   [I
      StackMapTable: number_of_entries = 2
        frame_type = 255 /* full_frame */
          offset_delta = 32
          locals = [ class "[Ljava/lang/String;", class "[I", class "[I", int, int ]
          stack = []
        frame_type = 248 /* chop */
          offset_delta = 25
}
```







#### switch 字符串

##### java 源码

```java
public class JavaApp {

    public static void choose(String str){
        switch (str){
            case "hello":
                System.out.println("h");
                break;
            case "world":
                System.out.println("w");
                break;
        }
    }
}
```



会被编译器转换为：

```java
public class JavaApp {

    public static void choose(String str){
        byte x = -1;
        switch (str.hashCode()){
            case 99162322:
                if(str.equals("hello")){
                    x = 0;
                }
                break;
            case 113318802:
                if(str.equals("world")){
                    x = 1;
                }
                break;
        }
        switch (x){
            case 0:
                System.out.println("h");
                break;
            case 1:
                System.out.println("w");
                break;
        }
    }
}
```

可以看到，执行了两遍 wsitch，第一遍是根据字符串的 hashCode 和 equals 将字符串转换为相应 byte 类型，第二遍才是利用 byte 执行进行比较。

为什么第一遍时必须比较 hashCode，又利用 equals 比较呢？hashCode 是为了提高效率，减少可能的比较；而 equals 是为了防止 hashCode1 冲突，例如 BM 和 C. 这两个字符串的 hashCode 值都是2123。

##### 注意

swith 配合 String 和枚举使用时，变量不能为 null，原因是



##### 主要字节码

```shell
{
  public static void choose(java.lang.String);
    descriptor: (Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=3, args_size=1
         0: aload_0
         1: astore_1
         2: iconst_m1
         3: istore_2
         4: aload_1
         5: invokevirtual #4                  // Method java/lang/String.hashCode:()I
         8: lookupswitch  { // 2
                99162322: 36
               113318802: 50
                 default: 61
            }
        36: aload_1
        37: ldc           #2                  // String hello
        39: invokevirtual #5                  // Method java/lang/String.equals:(Ljava/lang/Object;)Z
        42: ifeq          61
        45: iconst_0
        46: istore_2
        47: goto          61
        50: aload_1
        51: ldc           #6                  // String world
        53: invokevirtual #5                  // Method java/lang/String.equals:(Ljava/lang/Object;)Z
        56: ifeq          61
        59: iconst_1
        60: istore_2
        61: iload_2
        62: lookupswitch  { // 2
                       0: 88
                       1: 99
                 default: 107
            }
        88: getstatic     #7                  // Field java/lang/System.out:Ljava/io/PrintStream;
        91: ldc           #8                  // String h
        93: invokevirtual #9                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        96: goto          107
        99: getstatic     #7                  // Field java/lang/System.out:Ljava/io/PrintStream;
       102: ldc           #10                 // String w
       104: invokevirtual #9                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
       107: return
      LineNumberTable:
        line 9: 0
        line 11: 88
        line 12: 96
        line 14: 99
        line 17: 107
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0     108     0   str   Ljava/lang/String;
      StackMapTable: number_of_entries = 6
        frame_type = 253 /* append */
          offset_delta = 36
          locals = [ class java/lang/String, int ]
        frame_type = 13 /* same */
        frame_type = 10 /* same */
        frame_type = 26 /* same */
        frame_type = 10 /* same */
        frame_type = 249 /* chop */
          offset_delta = 7
}
```





#### switch 枚举

##### Java 源码

```java
public class JavaApp {

    public static void choose(Sex sex){
        switch (sex){
            case MALE:
                System.out.println("男");
                break;
            case FEMALE:
                System.out.println("女");
                break;
        }
    }
}

enum Sex{
    MALE,FEMALE
}
```

会被编译器转换为：

```java
public class JavaApp {
	/**
	 * 定义一个合成类（仅 jvm 使用，对我们不可见）
	 * 用来映射枚举的 ordinal 与数组元素的关系
	 * 枚举的 ordinal 表示枚举对象的序号，从 0 开始
	 * 即 MALE 的 ordinal() = 0，FEMALE 的 ordinal() = 1
	 */
    public class $MAP {
        // 数组大小即为枚举元素个数，里面存储 case 用来对比的数字
        static int[] map = new int[2];
        static {
            map[Sex.MALE.ordinal()] = 1;
            map[Sex.FEMALE.ordinal()] = 2;
        }
    }
    
    public static void choose(Sex sex){
        int x = $MAP.amp[sex.ordinal()];
        switch (x){
            case 1:
                System.out.println("男");
                break;
            case 2:
                System.out.println("女");
                break;
        }
    }
}
```



##### 主要字节码

```shell
{
  public static void choose(Sex);
    descriptor: (LSex;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=1, args_size=1
         0: getstatic     #4                  // Field JavaApp$1.$SwitchMap$Sex:[I
         3: aload_0
         4: invokevirtual #5                  // Method Sex.ordinal:()I
         7: iaload
         8: lookupswitch  { // 2
                       1: 36
                       2: 47
                 default: 55
            }
        36: getstatic     #6                  // Field java/lang/System.out:Ljava/io/PrintStream;
        39: ldc           #7                  // String 男
        41: invokevirtual #8                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        44: goto          55
        47: getstatic     #6                  // Field java/lang/System.out:Ljava/io/PrintStream;
        50: ldc           #9                  // String 女
        52: invokevirtual #8                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        55: return
      LineNumberTable:
        line 9: 0
        line 11: 36
        line 12: 44
        line 14: 47
        line 17: 55
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      56     0   sex   LSex;
      StackMapTable: number_of_entries = 3
        frame_type = 36 /* same */
        frame_type = 10 /* same */
        frame_type = 7 /* same */
}
```



#### 枚举类

JDK 7 新增了枚举类，以前面的性别枚举为例：

##### java 源码

```java
enum Sex{
    MALE,FEMALE
}
```



会被编译器转换为：

```java
public final class Sex extends Enum<Sex>{
    public static final Sex MALE;
    public static final Sex FEMALE;
    private static final Sex[] $VALUES;
    
    static {
        MALE = new Sex("MALE", 0);
        FEMALE = new Sex("FEMALE", 1);
        $VALUES = new Sex[]{MALE,FEMALE};
    }
    
    private Sex(String name, int ordinal){
        super(name, ordinal);
    }
    
    public static Sex[] values(){
        return $VALUES.clone();
    }
    
    public static Sex valueOf(String name){
        return Enum.valueOf(Sex.class, name)
    }
}
```



##### 主要字节码

```shell
public final class Sex extends java.lang.Enum<Sex>
  minor version: 0
  major version: 52
  flags: (0x4031) ACC_PUBLIC, ACC_FINAL, ACC_SUPER, ACC_ENUM
  this_class: #4                          // Sex
  super_class: #12                        // java/lang/Enum
  interfaces: 0, fields: 3, methods: 4, attributes: 2
Constant pool:
   #1 = Fieldref           #4.#36         // Sex.$VALUES:[LSex;
   #2 = Methodref          #37.#38        // "[LSex;".clone:()Ljava/lang/Object;
   #3 = Class              #17            // "[LSex;"
   #4 = Class              #39            // Sex
   #5 = Methodref          #12.#40        // java/lang/Enum.valueOf:(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;
   #6 = Methodref          #12.#41        // java/lang/Enum."<init>":(Ljava/lang/String;I)V
   #7 = String             #13            // MALE
   #8 = Methodref          #4.#41         // Sex."<init>":(Ljava/lang/String;I)V
   #9 = Fieldref           #4.#42         // Sex.MALE:LSex;
  #10 = String             #15            // FEMALE
  #11 = Fieldref           #4.#43         // Sex.FEMALE:LSex;
  #12 = Class              #44            // java/lang/Enum
  #13 = Utf8               MALE
  #14 = Utf8               LSex;
  #15 = Utf8               FEMALE
  #16 = Utf8               $VALUES
  #17 = Utf8               [LSex;
  #18 = Utf8               values
  #19 = Utf8               ()[LSex;
  #20 = Utf8               Code
  #21 = Utf8               LineNumberTable
  #22 = Utf8               valueOf
  #23 = Utf8               (Ljava/lang/String;)LSex;
  #24 = Utf8               LocalVariableTable
  #25 = Utf8               name
  #26 = Utf8               Ljava/lang/String;
  #27 = Utf8               <init>
  #28 = Utf8               (Ljava/lang/String;I)V
  #29 = Utf8               this
  #30 = Utf8               Signature
  #31 = Utf8               ()V
  #32 = Utf8               <clinit>
  #33 = Utf8               Ljava/lang/Enum<LSex;>;
  #34 = Utf8               SourceFile
  #35 = Utf8               Sex.java
  #36 = NameAndType        #16:#17        // $VALUES:[LSex;
  #37 = Class              #17            // "[LSex;"
  #38 = NameAndType        #45:#46        // clone:()Ljava/lang/Object;
  #39 = Utf8               Sex
  #40 = NameAndType        #22:#47        // valueOf:(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;
  #41 = NameAndType        #27:#28        // "<init>":(Ljava/lang/String;I)V
  #42 = NameAndType        #13:#14        // MALE:LSex;
  #43 = NameAndType        #15:#14        // FEMALE:LSex;
  #44 = Utf8               java/lang/Enum
  #45 = Utf8               clone
  #46 = Utf8               ()Ljava/lang/Object;
  #47 = Utf8               (Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;
{
  public static final Sex MALE;
    descriptor: LSex;
    flags: (0x4019) ACC_PUBLIC, ACC_STATIC, ACC_FINAL, ACC_ENUM

  public static final Sex FEMALE;
    descriptor: LSex;
    flags: (0x4019) ACC_PUBLIC, ACC_STATIC, ACC_FINAL, ACC_ENUM

  public static Sex[] values();
    descriptor: ()[LSex;
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=1, locals=0, args_size=0
         0: getstatic     #1                  // Field $VALUES:[LSex;
         3: invokevirtual #2                  // Method "[LSex;".clone:()Ljava/lang/Object;
         6: checkcast     #3                  // class "[LSex;"
         9: areturn
      LineNumberTable:
        line 1: 0

  public static Sex valueOf(java.lang.String);
    descriptor: (Ljava/lang/String;)LSex;
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=1, args_size=1
         0: ldc           #4                  // class Sex
         2: aload_0
         3: invokestatic  #5                  // Method java/lang/Enum.valueOf:(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;
         6: checkcast     #4                  // class Sex
         9: areturn
      LineNumberTable:
        line 1: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      10     0  name   Ljava/lang/String;

  static {};
    descriptor: ()V
    flags: (0x0008) ACC_STATIC
    Code:
      stack=4, locals=0, args_size=0
         0: new           #4                  // class Sex
         3: dup
         4: ldc           #7                  // String MALE
         6: iconst_0
         7: invokespecial #8                  // Method "<init>":(Ljava/lang/String;I)V
        10: putstatic     #9                  // Field MALE:LSex;
        13: new           #4                  // class Sex
        16: dup
        17: ldc           #10                 // String FEMALE
        19: iconst_1
        20: invokespecial #8                  // Method "<init>":(Ljava/lang/String;I)V
        23: putstatic     #11                 // Field FEMALE:LSex;
        26: iconst_2
        27: anewarray     #4                  // class Sex
        30: dup
        31: iconst_0
        32: getstatic     #9                  // Field MALE:LSex;
        35: aastore
        36: dup
        37: iconst_1
        38: getstatic     #11                 // Field FEMALE:LSex;
        41: aastore
        42: putstatic     #1                  // Field $VALUES:[LSex;
        45: return
      LineNumberTable:
        line 2: 0
        line 1: 26
}
Signature: #33                          // Ljava/lang/Enum<LSex;>;
```





#### try-with-resources

JDK 7 开始新增了对需要关闭的资源处理的特殊语法 try-with-resources

```java
try(资源变量 = 创建资源对象){
    
}catch(){
    
}
```



##### java 源码

```java
public class JavaApp {

    public static void main(String [] args) {
        try(InputStream is = new FileInputStream("")) {

        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
```



其中资源对象需要实现 AutoCloseable 接口，例如 InputStream、OutputStream、Connection、Statement、ResultSet 等接口都实现了 AutoCloseable ，使用 try-with-resources 可以不用写 finally 语句块，编译器会帮助生成关闭资源代码。

会被编译器转换为：

```java
public class JavaApp {

    public static void main(String [] args) {
        try{
			InputStream is = new FileInputStream("");
            Throwable t = null;
            try {
                
            } catch (Throwable e1) {
                t = e1;
                throw e1;
            } finally {
                if(is != null) {
                    if(t != null) {
                        try {
                            is.close();
                        } catch (Throwable e2) {
                            // 如果 close 出现异常，作为被压制异常添加
                            t.addSuppressed(e2);
                        }
                    } else {
                        is.close();
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
```



##### 压制异常

为什么要设计一个 addSuppressed(Throwable e)（添加被压制异常）的方法呢？是为了防止异常信息的丢失（想想 try-with-resources 生成的 finally 中如果抛出了异常）：

```java
public class MyResources implements AutoCloseable{

    @Override
    public void close() throws Exception {
        throw new Exception("close 异常");
    }
}

public class JavaApp {

    public static void main(String [] args) {
        try(MyResources resource = new MyResources()){
            int i = 1/0;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}

```

输出：

```shell
java.lang.ArithmeticException: / by zero
	at JavaApp.main(JavaApp.java:9)
	Suppressed: java.lang.Exception: close 异常
		at MyResources.close(MyResources.java:5)
		at JavaApp.main(JavaApp.java:10)
```

如上所示，两个异常信息都不会丢失。



##### 主要字节码

```shell
{
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=3, locals=4, args_size=1
         0: new           #2                  // class java/io/FileInputStream
         3: dup
         4: ldc           #3                  // String
         6: invokespecial #4                  // Method java/io/FileInputStream."<init>":(Ljava/lang/String;)V
         9: astore_1
        10: aconst_null
        11: astore_2
        12: aload_1
        13: ifnull        40
        16: aload_2
        17: ifnull        36
        20: aload_1
        21: invokevirtual #5                  // Method java/io/InputStream.close:()V
        24: goto          40
        27: astore_3
        28: aload_2
        29: aload_3
        30: invokevirtual #7                  // Method java/lang/Throwable.addSuppressed:(Ljava/lang/Throwable;)V
        33: goto          40
        36: aload_1
        37: invokevirtual #5                  // Method java/io/InputStream.close:()V
        40: goto          48
        43: astore_1
        44: aload_1
        45: invokevirtual #9                  // Method java/io/IOException.printStackTrace:()V
        48: return
      Exception table:
         from    to  target type
            20    24    27   Class java/lang/Throwable
             0    40    43   Class java/io/IOException
      LineNumberTable:
        line 8: 0
        line 10: 12
        line 12: 40
        line 10: 43
        line 11: 44
        line 13: 48
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
           10      30     1    is   Ljava/io/InputStream;
           44       4     1     e   Ljava/io/IOException;
            0      49     0  args   [Ljava/lang/String;
      StackMapTable: number_of_entries = 5
        frame_type = 255 /* full_frame */
          offset_delta = 27
          locals = [ class "[Ljava/lang/String;", class java/io/InputStream, class java/lang/Throwable ]
          stack = [ class java/lang/Throwable ]
        frame_type = 8 /* same */
        frame_type = 249 /* chop */
          offset_delta = 3
        frame_type = 66 /* same_locals_1_stack_item */
          stack = [ class java/io/IOException ]
        frame_type = 4 /* same */
}
```





#### 方法重写时的桥接方法

我们都知道，方法重写时对返回值分两种情况：

- 父子类的返回值完全一致
- 子类返回值可以是父类返回值的子类

##### java 源码

```java
public class Parent{
    public Number age(){
        return 35;
    }
}

public class Child extends Parent{
    @Override
    public Integer age(){
        return 10;
    }
}

```



对于子类，java 编译器会做如下处理：

```java
public class Child extends Parent{
    public Integer age(){
        return 10;
    }
    // 此方法才是真正重写了父类 public Number age() 方法
    public synthetic bridge Number age(){
        return age();
    }
}
```

其中桥接方法比较特殊，仅对 java 虚拟机可见，并且与原来的 public Integer age() 没有命名冲突，可以用下面的反射代码来验证：

```java
public class JavaApp {

    public static void main(String [] args) {
        Method[] methods = Child.class.getDeclaredMethods();
        for(Method method : methods){
            System.out.println(method);
        }
    }

}
// 输出
// public java.lang.Integer Child.age()
// public java.lang.Number Child.age()
```



##### 主要字节码

```shell
{
  public java.lang.Integer age();
    descriptor: ()Ljava/lang/Integer;
    flags: (0x0001) ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: bipush        10
         2: invokestatic  #2                  // Method java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
         5: areturn
      LineNumberTable:
        line 4: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       6     0  this   LChild;

  public java.lang.Number age();
    descriptor: ()Ljava/lang/Number;
    flags: (0x1041) ACC_PUBLIC, ACC_BRIDGE, ACC_SYNTHETIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokevirtual #3                  // Method age:()Ljava/lang/Integer;
         4: areturn
      LineNumberTable:
        line 1: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   LChild;
}
```



#### 匿名内部类

##### java 源码

```java
public class JavaApp {

    public static void main(String [] args) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("ok");
            }
        };
    }

}
```

会被编译器转换为：

```java
// 额外生成的类
final class JavaApp$1 implements Runable {
    JavaApp$1() {
        
    }
    public void run() {
        System.out.println("ok");
    }
}

public class JavaApp {

    public static void main(String [] args) {
        Runnable runnable = new JavaApp$1();
    }

}
```



##### 引用局部变量

```java
public class JavaApp {

    public static void main(String [] args) {
        test(1);
    }

    public static void test(final int x){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("ok:" + x);
            }
        };
    }
}
```

会被编译器转换为：

```java
// 额外生成的类
final class JavaApp$1 implements Runable {
	int val$x;
    JavaApp$1(int x) {
        this.val$x = x;
    }
    public void run() {
        System.out.println("ok:" + this.val$x);
    }
}

public class JavaApp {

    public static void main(String [] args) {
        test(1);
    }

    public static void test(final int x){
        Runnable runnable = new JavaApp$1(x);;
    }
}
```



##### 主要字节码

```java
public class JavaApp
  minor version: 0
  major version: 52
  flags: (0x0021) ACC_PUBLIC, ACC_SUPER
  this_class: #4                          // JavaApp
  super_class: #5                         // java/lang/Object
  interfaces: 0, fields: 0, methods: 2, attributes: 2
Constant pool:
   #1 = Methodref          #5.#22         // java/lang/Object."<init>":()V
   #2 = Class              #23            // JavaApp$1
   #3 = Methodref          #2.#22         // JavaApp$1."<init>":()V
   #4 = Class              #24            // JavaApp
   #5 = Class              #25            // java/lang/Object
   #6 = Utf8               InnerClasses
   #7 = Utf8               <init>
   #8 = Utf8               ()V
   #9 = Utf8               Code
  #10 = Utf8               LineNumberTable
  #11 = Utf8               LocalVariableTable
  #12 = Utf8               this
  #13 = Utf8               LJavaApp;
  #14 = Utf8               main
  #15 = Utf8               ([Ljava/lang/String;)V
  #16 = Utf8               args
  #17 = Utf8               [Ljava/lang/String;
  #18 = Utf8               runnable
  #19 = Utf8               Ljava/lang/Runnable;
  #20 = Utf8               SourceFile
  #21 = Utf8               JavaApp.java
  #22 = NameAndType        #7:#8          // "<init>":()V
  #23 = Utf8               JavaApp$1
  #24 = Utf8               JavaApp
  #25 = Utf8               java/lang/Object
{
  public JavaApp();
    descriptor: ()V
    flags: (0x0001) ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 2: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   LJavaApp;

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=2, args_size=1
         0: new           #2                  // class JavaApp$1
         3: dup
         4: invokespecial #3                  // Method JavaApp$1."<init>":()V
         7: astore_1
         8: return
      LineNumberTable:
        line 5: 0
        line 11: 8
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       9     0  args   [Ljava/lang/String;
            8       1     1 runnable   Ljava/lang/Runnable;
}
InnerClasses:
  static #2;                              // class JavaApp$1
```



### 类加载阶段

#### 加载

- 将类的字节码载入方法区中，内部采用 C++ 的 instanceKlass 描述 Java 类，它的重要 field 有：

  - _java_mirror 即 java 的类镜像，例如对 String 来说，就是 String.class，作用是把 Klass 暴露给 Java 使用

  - _super 即父类

  - _fields 即成员变量

  - _methods 即方法

  - _constants 即常量池

  - _class_loader 即类加载器

  - _vtable 虚方法表

  - _itable 接口方法表



- 如果这个类还有父类没有加载，先加载父类
- 加载和链接可能是交替运行的



**注意**

- instanceKlass 这样的【元数据】是存储在方法区的（1.8后的元空间内），但_java_mirror 是存储在堆中
- 可以通过前面介绍的 HSDB 工具查看



![image-20220926222510328](D:\documents\notes\solid\Java.assets\068.jpg)



#### 链接

- 验证：验证类是否符合 JVM 规范，安全性检查


用 UE 等支持二进制的编辑器修改字节码的魔数，在控制台运行

```shell
PS D:\develop\code\java\jvm\out\production\jvm> java JavaApp
错误: 加载主类 JavaApp 时出现 LinkageError
        java.lang.ClassFormatError: Incompatible magic value 3405691579 in class file JavaApp
```



- 准备：为 static 变量分配空间，设置默认值
  - static 变量在 JDK 7 之前存储于 instanceKlass 末尾，从JDK 7开始，存储于 _java_mirror 末尾 
  - static 变量分配空间和赋值是两个步骤，分配空间在准备阶段完成，赋值在初始化阶段完成
  - 如果 static 变量是 final 的基本类型，以及字符串常量，那么编译阶段值就确定了，赋值在准备阶段完成 
  - 如果 static 变量是 final 的，但属于引用类型，那么赋值也会在初始化阶段完成 



- 解析：将常量池中的符号引用解析为直接引用

```java
package load;

public class C {
    D d = new D();
    class D {

    }
}
```



```java
import load.C;
import java.io.IOException;

public class JavaApp {
    public static void main(String [] args) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = JavaApp.class.getClassLoader();
        // classLoader 方法不会导致类的解析和初始化
        Class<?> c = classLoader.loadClass("load.C");
        System.in.read();
    }
}


```

Jps 查看 java 进程号

打开 HSDB -> Tools -> Class Browser 可以找得到 load.C 找不到 load.D

查看类 load.C 的常量池，发现 JVM_CONSTANT_UnresolvedClass load/C$D 未解析的一个类



```java
import load.C;
import java.io.IOException;

public class JavaApp {
    public static void main(String [] args) throws IOException {
        C c = new C();
        System.in.read();
    }
}

```

打开 HSDB 查看 load.C 的常量池，发现和类 load.D 相关的常量名不再为 未解析，而是具体的内存地址。



#### 初始化

##### <cinit>()v 方法

初始化即调用 <cinit>()v，虚拟机会保证这个类的【构造方法】的线程安全



##### 发生的时机

概括的说，类初始化是【懒惰的】

- main 方法所在的类，总会被首先初始化 
- 首次访问这个类的静态变量或静态方法时
- 子类初始化，如果父类还没初始化，会引发 
- 子类访问父类的静态变量，只会触发父类的初始化
- Class.farName 
- new 会导致初始化 

不会导致类初始化的情况

- 访问类的 static final 静态常量（基本类型和字符串）不会触发初始化 
- 类对象 .class 不会触发初始化 
- 创建该类的数组不会触发初始化 
- 类加载器的loadClass方法 
- Class.forNarne的参数2为 false 时 



##### 验证

```java
package load;

public class A {
    public static int a = 0;
    static {
        System.out.println("a init");
    }
}

```

```java
package load;

public class B extends A {
    public final static double b = 5.0;
    public static boolean c = false;
    static {
        System.out.println("b init");
    }
}

```

```java
import load.A;
import load.B;

public class JavaApp {
    static {
        System.out.println("main init");
    }
    public static void main(String [] args) throws ClassNotFoundException {
        // 静态常量（基本类型和字符串）不会触发初始化
        System.out.println(B.b);
        // 类对象 .class 不会触发初始化
        System.out.println(B.class);
        // 创建该类的数组不会触发初始化
        System.out.println(new B[0]);
        // 不会初始化类 B，但会加载 B、A
        ClassLoader c1 = Thread.currentThread().getContextClassLoader();
        c1.loadClass("load.B");
        // 不会初始化类 B，但会加载 B、A
        ClassLoader c2 = Thread.currentThread().getContextClassLoader();
        Class.forName("load.B",false, c2);

        // 首次访问这个类的静态变量或静态方法时
        System.out.println(A.a);
        // 子类初始化，如果父类还没初始化，会引发
        System.out.println(B.c);
        // 子类访问父类的静态变量，只会触发父类的初始化
        System.out.println(B.a);
        // 会初始化类 B，并先初始化类 A
        Class.forName("load.B");
    }
}
```



#### 练习

##### 静态常量初始化

从字节码分析，使用a，b，c 这三个常量是否会导致 E 初始化

```java
package load;

public class E {
    public static final int a = 10;
    public static final String b = "hello";
    public static final Integer c = 20;
}

import load.E;

public class JavaApp {

    public static void main(String [] args) {
        System.out.println(E.a);
        System.out.println(E.b);
        System.out.println(E.c);
    }
}

```

可以看到 a，b 两个常量的值是确定的，常量 c 的在初始化方法中进行赋值

```shell
  public static final int a;
    descriptor: I
    flags: (0x0019) ACC_PUBLIC, ACC_STATIC, ACC_FINAL
    ConstantValue: int 10

  public static final java.lang.String b;
    descriptor: Ljava/lang/String;
    flags: (0x0019) ACC_PUBLIC, ACC_STATIC, ACC_FINAL
    ConstantValue: String hello

  public static final java.lang.Integer c;
    descriptor: Ljava/lang/Integer;
    flags: (0x0019) ACC_PUBLIC, ACC_STATIC, ACC_FINAL

  public load.E();
    descriptor: ()V
    flags: (0x0001) ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 3: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   Lload/E;

  static {};
    descriptor: ()V
    flags: (0x0008) ACC_STATIC
    Code:
      stack=1, locals=0, args_size=0
         0: bipush        20
         2: invokestatic  #2                  // Method java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
         5: putstatic     #3                  // Field c:Ljava/lang/Integer;
         8: return
      LineNumberTable:
        line 6: 0
}
SourceFile: "E.java"

```



##### 懒惰初始化单例模式

```java
public class Singleton {
    private Singleton(){

    }
    // 内部类中保存单例
    private static class LazyHolder {
        static final Singleton INSTANCE = new Singleton();
    }
    // 第一次调用 getInstance 方法，才会导致内部类加载和初始化其静态成员
    public static Singleton getInstance(){
        return LazyHolder.INSTANCE;
    }
}
```

以上的实现特点是：

- 懒惰实例化
- 初始化时的线程安全是有保障的



### 类加载器

以 JDK 8 为例

| 名称                    | 加载哪的类            | 说明                          |
| ----------------------- | --------------------- | ----------------------------- |
| Bootstrap ClassLoader   | JAVA_HOME/jre/lib     | 无法直接访问                  |
| Extension ClassLoader   | JAVA_HOME/jre/lib/ext | 上级为 Bootstrap，显示为 null |
| Application ClassLoader | classpath             | 上级为 Extension              |
| 自定义加载器            | 自定义                | 上级为 Application            |



#### 启动类加载器

用 Bootstrap 类加载器加载类

```java
package load;

public class F {
    static {
        System.out.println("bootstarp F init");
    }
}

public class JavaApp {

    public static void main(String [] args) throws ClassNotFoundException {
        Class<?> clazz = Class.forName("load.F");
        System.out.println(clazz.getClassLoader());
    }
}

// 输出
// bootstarp F init
```

输出

```shell
# 在字节码输出目录执行
java -Xbootclasspath/a:. JavaApp
bootstarp F init
null
```



- -Xbootclasspath 表示设置 bootclasspath
- /a:. 表示将当前目录追加至 bootclasspath 之后
- 可以用这个办法替换核心类

  - java -Xbootclasspath:<new bootclasspath> ，完全替换掉原有加载路径

  - java -Xbootclasspath/a:<追加路径> ，后追加加载路径

  - java -Xbootclasspath/p:<追加路径> ，前追加加载路径



#### 扩展类加载器

```java
package load;

public class G {
    static {
        System.out.println("classpath G init");
    }
}

public class JavaApp {

    public static void main(String [] args) throws ClassNotFoundException {
        Class<?> clazz = Class.forName("load.G");
        System.out.println(clazz.getClassLoader());
    }
}

// 直接运行输出
// classpath G init
// sun.misc.Launcher$AppClassLoader@18b4aac2
```

然后修改一下 G 类的初始化输出并打包，然后放在目录 JAVA_HOME/jre/lib/ext 下

jar -cvf my.jar load\G.class

```java
package load;

public class G {
    static {
        System.out.println("ext G init");
    }
}
```

再运行一次程序，输出为

```shell
ext G init
sun.misc.Launcher$ExtClassLoader@330bedb4
```

说明加载的是扩展目录下的类，而不是自定义类。通过双亲委派模式加载了扩展目录下的 G 类。



#### 线程上下文类加载器

我们在使用 JDBC 时，都需要加载 Driver 驱动，不知道你有没有注意到，不写

```java
Class.forName("com.mysql.jdbs.Driver")
```

也是可以让 com.mysql.jdbc.Driver 正确加载的，你知道怎么做的吗？

我们追踪一下源码：

```java
public class DriverManager {
    // 注册驱动的集合
    private final static CopyOnWriteArrayList<DriverInfo> registeredDrivers = new CopyOnWriteArrayList<>();
    /**
     * 初始化驱动
     *
     */
    static {
        loadInitialDrivers();
        println("JDBC DriverManager initialized");
    }
}
```

看看 DriverManager 的类加载器

```java
System.out.println(DriverManager.class.getClassLoader());
// null
```

输出 null，表示它的类加载器是 Bootstrap ClassLoader，会到 JAVA_HOME/jre/lib 下搜索类，但 JAVA_HOME/jre/lib 下显然没有 mysql jdbc 的 jar 包，这样问题来了，在 DriverManager 的静态代码块中，怎么能正确加载 com.mysql.jdbs.Driver 呢？

继续看 loadInitialDrivers() 方法：

```java
    private static void loadInitialDrivers() {
        String drivers;
        try {
            drivers = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty("jdbc.drivers");
                }
            });
        } catch (Exception ex) {
            drivers = null;
        }
        // If the driver is packaged as a Service Provider, load it.
        // Get all the drivers through the classloader
        // exposed as a java.sql.Driver.class service.
        // ServiceLoader.load() replaces the sun.misc.Providers()
		// 1) 使用 ServiceLoader 机制加载驱动，即 SPI
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {

                ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
                Iterator<Driver> driversIterator = loadedDrivers.iterator();

                /* Load these drivers, so that they can be instantiated.
                 * It may be the case that the driver class may not be there
                 * i.e. there may be a packaged driver with the service class
                 * as implementation of java.sql.Driver but the actual class
                 * may be missing. In that case a java.util.ServiceConfigurationError
                 * will be thrown at runtime by the VM trying to locate
                 * and load the service.
                 *
                 * Adding a try catch block to catch those runtime errors
                 * if driver not available in classpath but it's
                 * packaged as service and that service is there in classpath.
                 */
                try{
                    while(driversIterator.hasNext()) {
                        driversIterator.next();
                    }
                } catch(Throwable t) {
                // Do nothing
                }
                return null;
            }
        });

        println("DriverManager.initialize: jdbc.drivers = " + drivers);
		// 2) 使用 jdbc.drivers 定义的驱动名加载驱动
        if (drivers == null || drivers.equals("")) {
            return;
        }
        String[] driversList = drivers.split(":");
        println("number of Drivers:" + driversList.length);
        for (String aDriver : driversList) {
            try {
                println("DriverManager.Initialize: loading " + aDriver);
                // 这里的 ClassLoader.getSystemClassLoader()就是应用程序类加载器
                Class.forName(aDriver, true,
                        ClassLoader.getSystemClassLoader());
            } catch (Exception ex) {
                println("DriverManager.Initialize: load failed: " + ex);
            }
        }
    }
```

这里打破了双亲委派模式，DriverManager 使用的是启动类加载器加载，与 DriverManager 相关的类也应当使用启动类加载器加载。但是这里使用了应用程序加载器加载



##### ServiceLoader

ServiceLoader 实现了大名鼎鼎的 Service Provider Interface (SPI)

约定如下，在 jar 包的 META-INF/service 包下，以接口全限定名名为文件，文件内容是实现类名称

这样就可以使用

```java
ServiceLoader<接口类型> allImpls = ServiceLoader.load(接口类型.class);
Iterator<接口类型> iter = allImpls.iterator();
while(iter.hasNext()){
    iter.next();
}
```

通过 load 方法在文件中找到实现类，体现的是【面向接口编程+解耦】的思想，在下面一些框架中运用了此思想：

- JDBC
- Servlet 初始化器
- Spring 容器
- Dubbo （对 SPI 进行了扩展）

接着看 ServiceLoader.load 方法：

```java
public static <S> ServiceLoader<S> load(Class<S> service) {
    // 获取线程上下文类加载器
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return ServiceLoader.load(service, cl);
}
```

线程上下文类加载器是当前线程使用的类加载器，默认就是应用程序类加载器，它内部又是由 Class.forName 调用了线程上下文类加载器完成类加载，具体代码在 ServiceLoader 的内部类 LazyIterator 中：

```java
private S nextService() {
    if (!hasNextService())
        throw new NoSuchElementException();
    String cn = nextName;
    nextName = null;
    Class<?> c = null;
    try {
        c = Class.forName(cn, false, loader);
    } catch (ClassNotFoundException x) {
        fail(service,
             "Provider " + cn + " not found");
    }
    if (!service.isAssignableFrom(c)) {
        fail(service,
             "Provider " + cn  + " not a subtype");
    }
    try {
        S p = service.cast(c.newInstance());
        providers.put(cn, p);
        return p;
    } catch (Throwable x) {
        fail(service,
             "Provider " + cn + " could not be instantiated",
             x);
    }
    throw new Error();          // This cannot happen
}
```



#### 自定义类加载器

##### 什么时候需要自定义类加载器 

- 想加载非classpath随意路径中的类文件
- 都是通过接口来使用实现，希望解藕时，常用在框架设计
- 这些类希望予以隔离，不同应用的同名类都可以加载，不冲突，常见于tomcat容器



##### 步骤

- 继承ClassLoader父类 
- 要遵从双亲委派机制，重写 findclass 方法
  - 注意不是重写 IoadClass 方法．否则不会走双亲委派机制
- 读取类文件的字节码
- 调用父类的 defineClass 方法来加载类
- 使用者调用该类加载器的 IoadClass 方法

##### 示例

###### 准备

准备好两个类文件放入 C:\Users\young\Desktop\temp\files\class 它实现了 java.util.Map 接口

```java
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public class MapImpl1 extends AbstractMap implements Map {

    static {
        System.out.println("MapImpl1 init");
    }

    @Override
    public Set<Entry> entrySet() {
        return null;
    }
}

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public class MapImpl2 extends AbstractMap implements Map {

    static {
        System.out.println("MapImpl2 init");
    }

    @Override
    public Set<Entry> entrySet() {
        return null;
    }
}

```

###### 编译

```shell
C:\Users\young\Desktop\temp\files\class>E:\Applications\Portable\Oracle\JDK\8u301\bin\javac MapImpl1.java

C:\Users\young\Desktop\temp\files\class>E:\Applications\Portable\Oracle\JDK\8u301\bin\javac MapImpl2.java
```



###### 定义类加载器

```java
package load;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MyClassLoader extends ClassLoader{
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = "C:\\Users\\young\\Desktop\\temp\\files\\class\\"+name+".class";
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            Files.copy(Paths.get(path), os);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClassNotFoundException("类文件未找到", e);
        }
        byte[] bytes = os.toByteArray();
        return defineClass(name, bytes, 0, bytes.length);
    }
}

```



###### 检验

```java
import load.MyClassLoader;

public class JavaApp {

    public static void main(String [] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        MyClassLoader classLoader = new MyClassLoader();
        Class<?> c1 = classLoader.loadClass("MapImpl1");
        Class<?> c2 = classLoader.loadClass("MapImpl1");
        // 加载过一次之后不会重复加载同一个类
        System.out.println(c1 == c2);
        MyClassLoader classLoader2 = new MyClassLoader();
        Class<?> c3 = classLoader2.loadClass("MapImpl1");
        // 包名相同且类加载器为同一个对象才认为是完全一致的
        System.out.println(c1 == c3);
        // 打印初始化信息
        c1.newInstance();
    }
}

```



#### 双亲委派模式

所谓的双亲委派，就是指调用类加载器的 loadClass 方法时，查找类的规则。

双亲委派模式是指一个类在收到类加载请求后不会尝试自己加载这个类，而是把该类加载请求向上委派给其父类加载器去完成，其父类加载器在接收到该类加载请求后又会将其委派给自己的父类，以此类推，这样所有的类加载请求都被向上委派到启动类加载器中。若父类加载器在接收到类加载请求后发现自己也无法加载该类（通常原因是该类的Class文件在父类的类加载路径中不存在），则父类会将该信息反馈给子类并向下委派子类加载器加载该类，直到该类被成功加载，若找不到该类，则JVM会抛出ClassNotFoud异常。

**这里的双亲，翻译为上级似乎更为合适，因为它们并没有继承关系**



##### 源码分析

查看抽象类 `ClassLoader 的 `loadClass` 方法

```java
    /**
     * Loads the class with the specified <a href="#name">binary name</a>.  The
     * default implementation of this method searches for classes in the
     * following order:
     *
     * <ol>
     *
     *   <li><p> Invoke {@link #findLoadedClass(String)} to check if the class
     *   has already been loaded.  </p></li>
     *
     *   <li><p> Invoke the {@link #loadClass(String) <tt>loadClass</tt>} method
     *   on the parent class loader.  If the parent is <tt>null</tt> the class
     *   loader built-in to the virtual machine is used, instead.  </p></li>
     *
     *   <li><p> Invoke the {@link #findClass(String)} method to find the
     *   class.  </p></li>
     *
     * </ol>
     *
     * <p> If the class was found using the above steps, and the
     * <tt>resolve</tt> flag is true, this method will then invoke the {@link
     * #resolveClass(Class)} method on the resulting <tt>Class</tt> object.
     *
     * <p> Subclasses of <tt>ClassLoader</tt> are encouraged to override {@link
     * #findClass(String)}, rather than this method.  </p>
     *
     * <p> Unless overridden, this method synchronizes on the result of
     * {@link #getClassLoadingLock <tt>getClassLoadingLock</tt>} method
     * during the entire class loading process.
     *
     * @param  name
     *         The <a href="#name">binary name</a> of the class
     *
     * @param  resolve
     *         If <tt>true</tt> then resolve the class
     *
     * @return  The resulting <tt>Class</tt> object
     *
     * @throws  ClassNotFoundException
     *          If the class could not be found
     */
    protected Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        synchronized (getClassLoadingLock(name)) {
            // 1.检查该类是否已经加载
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                long t0 = System.nanoTime();
                try {
                    if (parent != null) {
                        // 2.有上级的话，委派上级 loadClass
                        c = parent.loadClass(name, false);
                    } else {
                        // 3.如果没有上级了 (ExtClassLoader), 则委派 BootstrapClassLoader
                        c = findBootstrapClassOrNull(name);
                    }
                } catch (ClassNotFoundException e) {
                    // 非空上级类加载器如果没有找到该类会抛出 ClassNotFoundException 异常
                }

                if (c == null) {
                    // 4.每一层都找不到，调用 findClass 方法（每个类加载器自己扩展）来加载
                    long t1 = System.nanoTime();
                    c = findClass(name);

                    // 记录耗时
                    sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                    sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                    sun.misc.PerfCounter.getFindClasses().increment();
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }
```



## 内存模型（JMM）

很多人将【java 内存结构】与【java 内存模型】傻傻分不清，【java 内存模型】是 Java Memory Model（JMM）的意思。

关于它的权威解释，[请参考](https://download.oracle.com/otn-pub/jcp/memory_model-1.0-pfd-spec-oth-JSpec/memory_model-1_0-pfd-spec.pdf) 

简单的说，JMM 定义了一套在多线程读写共享数据时（成员变量、数组）时，对数据的可见性、有序性和原子性的规则保障。



### java 内存模型



#### 原子性



#### 问题分析



#### 解决方法（synchronized）

synchronized（同步关键字）

- 一把锁只能同时被一个线程获取，没有获得锁的线程只能等待
- 同步代码块锁的锁对象为自己指定的锁对象
- 同步方法锁默认锁对象为 this，当前方法的实例对象
- 锁对象是 *.class 以及 synchronized 修饰的是 static 方法的时候，所有对象公用同一把锁
- synchronized修饰的方法，无论方法正常执行完毕还是抛出异常，都会释放锁

语法

```java
// 同步代码块
synchronized(对象){
    // 要作为原子操作的代码
}

// 同步方法
修饰符 synchronized 返回值 方法名(参数);
public synchronized void method()
public static synchronized void method();

```



用 synchronized 解决并发问题：

```java
public class JavaApp {
    static int i = 0;
    static Object obj = new Object();

    public static void main(String[]args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
           for(int j = 0; j < 5000; j++){
               synchronized (obj){
                   i++;
               }
           }
        });

        Thread t2 = new Thread(() -> {
            for(int j = 0; j < 5000; j++){
                synchronized (obj){
                    i--;
                }
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();
        System.out.println(i);
    }
}
```



#### 线程同步原理

每个 Java 对象都可以关联一个 Monitor 对象，如果使用 synchronized 给对象加锁（重量级锁）之后，该对象头的 Mark Word 中就被设置指向Monitor对象的指针。

Monitor 结构如下

![](D:\documents\notes\solid\Java.assets\069.png)



- 刚开始 Monitor 中 Owner 为 null

- 当 Thread-2 执行 synchronized(obj) 就会将 Monitor 的所有者 Owner 置为 Thread-2，Monitor中只能有一个 Owner
- 在 Thread-2 上锁的过程中，如果 Thread-3，Thread-4，Thread-5 也来执行 synchronized(obj)，就会进入EntryList，此时线程状态变为BLOCKED状态
- Thread-2 执行完同步代码块的内容，然后唤醒 EntryList 中等待的线程来竞争锁，竞争是非公平的（synchronized是非公平锁）
- WaitSet 中的 Thread-0，Thread-1 是之前获得过锁的线程，此时的状态是 WAITING 状态，后面讲wait-notify 时会分析

> 注意
>
> synchronized 必须是进入同一个对象的 monitor 才有上述的效果
>
> 不加 synchronized 的对象不会关联 monitor ，不遵从以上规则



#### 加锁和释放锁的原理

Monitorenter 和Monitorexit 指令，会让对象在执行，使其锁计数器加1或者减1。每一个对象在同一时间只与一个 monitor (锁)相关联，而一个 monitor 在同一时间只能被一个线程获得，一个对象在尝试获得与这个对象相关联的 Monitor 锁的所有权的时候，monitorenter 指令会发生如下3中情况之一：

- monitor 计数器为0，意味着目前还没有被获得，那这个线程就会立刻获得然后把锁计数器+1，一旦+1，别的线程再想获取，就需要等待
- 如果这个 monitor 已经拿到了这个锁的所有权，又重入了这把锁，那锁计数器就会累加，变成2，并且随着重入的次数，会一直累加
- 这把锁已经被别的线程获取了，等待锁释放

Monitorexit 指令：释放对于 monitor 的所有权，释放过程很简单，就是讲 monitor 的计数器减1，如果减完以后，计数器不是0，则代表刚才是重入进来的，当前线程还继续持有这把锁的所有权，如果计数器变成0，则代表当前线程不再拥有该 monitor 的所有权，即释放锁。

**可重入原理：加锁次数计数器**

锁的重入性，即在同一锁程中，线程不需要再次获取同一把锁。

synchronized 先天具有重入性。每个对象拥有一个计数器，当线程获取该对象锁后，计数器就会加一，释放锁后就会将计数器减一。





### 可见性

#### 退不出的循环



main 线程对变量 run 的修改对于线程 t 不可见，导致线程 t 无法停止。

```java
public class JavaApp {
    static boolean run = true;

    public static void main(String[]args) throws InterruptedException {
        Thread t = new Thread(() -> {
           while(run){

           }
        });
        t.start();

        Thread.sleep(1000);
        run = false;
    }
}
```



为什么呢？

1.初始状态，t 线程刚开始从主内存读取了 run 的值到工作内存。

![image-20221024225345509](D:\documents\notes\solid\Java.assets\070.png)

2.因为 t 线程要频繁从主内存中读取 run 的值，JIT 编译器会将 run 的值缓存到自己工作内存中的高速缓存中，减少对主存中 run 的访问，提高效率。

![image-20221024225442836](D:\documents\notes\solid\Java.assets\071.png)

3.1秒之后，main 线程修改了 run 的值，并同步至内存，而 t 是从自己工作内存中的高速缓存中读取这个变量的值，结果永远是旧值

![image-20221024225753674](D:\documents\notes\solid\Java.assets\072.png)



#### 解决方法（volatile）

volatile（易变关键字）

它可以用来修饰成员变量和静态变量，它可以避免线程从自己的工作缓存中查找变量的值，必须到主存中获取它的值，线程操作 volatile 变量都是直接操作主存。



#### 可见性

前面的例子体现的实际就是可见性，它保证的是在多个线程之间，一个线程对 volatile 变量的修改对另一个线程的可见，不能保证原子性，仅用在一个写线程，多个读线程的情况：

上例从字节码理解是这样的：

```java
getstatic	run	// 线程 t 获取 run true
getstatic	run	// 线程 t 获取 run true
getstatic	run	// 线程 t 获取 run true
getstatic	run	// 线程 t 获取 run true
getstatic	run	// 线程 main 修改 run 为 false
getstatic	run	// 线程 t 获取 run false
```

比较一下之前我们将线程安全是举的例子：两个线程一个 i++ 一个 i--

```java
// 假设 i 的初始值为 0 
getstatic	i // 线程 1-获取静态变量 i 的值，线程内 i = 0
getstatic	i // 线程 2-获取静态变量 i 的值，线程内 i = 0
iconst_1	  // 线程 1-准备常量 1
iadd		  // 线程 1-自增 线程内 i = 1;
putstatic	i // 线程 1-将修改后的值存入静态变量 i，静态变量 i = 1
iconst_1	  // 线程 2-准备常量 1
iadd		  // 线程 2-自减 线程内 i = -1;
putstatic	i // 线程 2-将修改后的值存入静态变量 i，静态变量 i = -1
```

#### 

#### 注意

synchronized 语句块既可以保证代码块的原子性，也同时保证代码块内变量的可见性。但缺点是 synchronized 是属于重量级操作，性能相对更低

如果在前面示例的死循环中加入 System.out.println(）会泼现即使不加 vointile 修饰符，线程t也能正确看到对 run 变量的修改了，想一想为什么？

> println() 方法内使用了 synchronized



### 有序性

#### 诡异的结果

```java
public class Actor{
    int num = 0;

    boolean ready = false;

    public int actor1(){
        if (ready) {
            return num + num;
        } else {
            return 1;
        }
    }

    public void actor2(){
        num = 2;
        ready = true;
    }
}
```

actor1()、actor2()分别使用两个线程调用，actor1() 可能会什么值？

情况 1：线程 1 先执行，这时 ready = false，所以进入 else 分支，结果为 1

情况 2：线程 2 先执行 num = 2，但没来得及执行 ready = false，线程 1 执行，还是进入 else 分支，结果为 1 

情况 3：线程 2 执行到 ready = true，线程 1 执行，这回进入 if 分支，结果为 4（这时候 num = 2 已经执行）

情况 4：线程 2 执行到 ready = true，线程 1 执行，这回进入 if 分支，结果为 0（这时候 num = 2 还未执行）

情况 4 叫做指令重排，是 JIT 编译器在运行时的一些优化，这个现象需要通过大量测试才能复现



借助并发压测工具 jcstress

创建一个 jcstress 骨架项目

```xml
<dependency>
    <groupId>org.openjdk.jcstress</groupId>
    <artifactId>jcstress-java-test-archetype</artifactId>
    <version>0.15</version>
</dependency>
```

编写如下测试类

```java
@JCStressTest
@Outcome(id = {"1", "4"}, expect = Expect.ACCEPTABLE, desc = "ok")
@Outcome(id = {"0"}, expect = Expect.ACCEPTABLE_INTERESTING, desc = "!!!!")
@State
public class ConcurrentTest{
    int num = 0;

    boolean ready = false;

    @Actor
    public void actor1(I_Result r){
        if (ready) {
            r.r1 = num + num;
        } else {
            r.r1 = 1;
        }
    }

    @Actor
    public void actor2(){
        num = 2;
        ready = true;
    }
}
```



先执行 `mvn clean install`

打包完成后执行 `E:\Applications\Portable\Oracle\JDK\8u301\bin\java -jar .\target\jcstress.jar`





#### 解决方法（volatile）

volatile 修饰变量后可以实现禁止指令重排序！

java 编译器会在生成指令系列时在适当的位置会插入内存屏障指令来禁止特定类型的处理器重排序。 为了实现 volatile 的内存语义，JMM 会限制特定类型的编译器和处理器重排序，JMM会针对编译器制定 volatile 重排序规则表：

![](D:\documents\notes\solid\Java.assets\073.png)



需要注意的是：volatile 写是在前面和后面分别插入内存屏障，而 volatile 读操作是在后面插入两个内存屏障。

volatile 写

![](D:\documents\notes\solid\Java.assets\074.jpeg)

volatile 读

![](D:\documents\notes\solid\Java.assets\075.jpeg)



#### 有序性理解

在同一个线程内，JVM 会在不影响正确性的前提下，可以调整语句的执行顺序，思考下面一段代码

```java
static int i;
static int j;

// 在某个线程内执行如下赋值操作
i = ...; // 较为耗时的操作
j = ...;
```

可以看到，至于是先执行 i，还是先执行 j，对最终的结果不会产生影响。所以，上面代码真正执行时，既可以是

```java
i = ...; // 较为耗时的操作
j = ...;
```

也可以是

```java
j = ...;
i = ...; // 较为耗时的操作
```

这种特性称之为【指令重排】，多线程下【指令重排】会影响正确性，例如著名的 double-checked locking 模式实现单例

```java
public final class Singleton {
    private Singleton() {}
    private static Singleton INSTANCE = null;
    public static Singleton getInstance(){
        if(null == INSTANCE){
            synchronized (Singleton.class) {
                if(null == INSTANCE){
                    INSTANCE = new Singleton();
                }
            }
        }
        return INSTANCE;
    }
}

```

以上的实现特点是：

懒惰实例化

首次使用 getInstance() 才使用 synchronized 加锁，后续使用时无需加锁

但在多线程环境下，上面的代码是有问题的，`INSTANCE = new Singleton();` 对应的字节码为：

```shell
19: new           #3                  // class app/Singleton
22: dup
23: invokespecial #4                  // Method "<init>":()V
26: putstatic     #2                  // Field INSTANCE:Lapp/Singleton;
```

其中 23 26两个字节码的顺序不是固定的，也许 JVM 会优化为：先将引用地址赋值给 INSTANCE 静态变量后，再执行构造方法，如果两个线程t1，t2 按如下时间顺序执行：

```java
时间1 t1 线程执行到 INSTANCE = new Singleton();
时间2 t1 线程分配空间，为 Singleton 对象生成了引用地址（19）
时间3 t1 线程将引用地址赋值给 INSTANCE，这时，INSTANCE != null（26）
时间4 t2 线程进入 getInstance() 方法，发现 INSTANCE != null（synchronied 块外），直接返回 INSTANCE
时间5 t1 线程执行 Singleton 的构造方法（23）
```

这时 t1 还未完全将构造方法执行完毕，如果在构造方法中要执行很多初始化操作，那么 t2 拿到的将是一个未初始化完毕的单例

对 INSTANCE 使用 volatile 修饰即可，可以禁用指令重排，但要注意在 JDK 5 以上的版本的 volatile 才会真正有效



#### happens-before

happens-before 规定了哪些写操作对其它线程的读操作可见，它是可见性与有序性的一套规则总结：

线程解锁 m 之前对变量的写，对于接下来对 m 加锁的其它线程对该变量的读可见

```java
public class JavaApp {
    static int x;
    static Object m = new Object();
    public static void main(String[]args) {
        new Thread(() ->{
            synchronized (m) {
                x = 10;
            }
        }, "t1").start();
        new Thread(() ->{
            synchronized (m) {
                System.out.println(x);
            }
        }, "t2").start();
    }
}
```

线程对 volatile 变量的写，对接下来其它线程对该变量的读可见

```java
public class JavaApp {
    volatile static int x;
    public static void main(String[]args) {
        new Thread(() ->{
            x = 10;
        }, "t1").start();
        new Thread(() ->{
            System.out.println(x);
        }, "t2").start();
    }
}
```

线程 start 前对变量的写，对该线程 start 后对该变量的读可见

```java
public class JavaApp {
    static int x;
    public static void main(String[]args) {
        x = 10;
        new Thread(() ->{
            System.out.println(x);
        }, "t2").start();
    }
}
```

线程结束前对变量的写，对其它线程得知它结束后的读可见（比如其它线程调用 t1.isAlive() 或 t1.join() 等待它结束）

```java
public class JavaApp {
    static int x;
    public static void main(String[]args) throws InterruptedException {
        Thread t1 = new Thread(() ->{
            x = 10;
        }, "t1");
        t1.start();

        t1.join();
        System.out.println(x);
    }
}
```

线程 t1 打断 t2（interrupt）前对变量的写，对于其它线程得知 t2 被打断后对变量的读可见（通过 t2.interrupted 或 t2.isInterrupted）

```java
public class JavaApp {
    static int x;
    public static void main(String[]args) {
        Thread t2 = new Thread(() ->{
            while (true){
                if(Thread.currentThread().isInterrupted()){
                    System.out.println(x);
                    break;
                }
            }
        }, "t2");
        t2.start();

        new Thread(() ->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x = 10;
            t2.interrupt();
        }, "t1").start();

        while (!t2.isInterrupted()){
            Thread.yield();
        }
        System.out.println(x);
    }
}
```

对变量默认值（0，false，null）的写，对其它线程对该变量的读可见

具有传递性，如果 x hb -> y 并且 y hb -> z 那么有 x hb -> z

> 变量都是指成员变量或静态成员变量
>
> 参考[](https://download.oraele.com/otn-pub/jcp/memory_model-1.0-pfd-spec-oth-JSpec/memory model-1_0-pfd-spec?AuthParam=1562811549_4d4994cbd5b59d964cd2907ea22ca08b) 第 17 页

### CAS 与原子类



#### CAS

CAS 即 Compare And Swap，它体现的一种乐观锁的思想，比如多个线程要对一个共享 `int` 变量执行 `+1` 操作：

```java
while(true){
    int 旧值 = 共享变量; // 比如拿到了当前值 0
    int 结果 = 旧值 + 1; // 在旧值 0 的基础上增加 1，正确结果是 1
    /**
     * 这时候如果别的线程把共享变量修改成 5，本线程的正确结果 1 就作废了，这时候
     * compareAndSwap 返回 false，重新尝试，直到
     * compareAndSwap 返回 true，表示本线程做修改的同时，别的线程没有干扰
     */
    if(compareAndSwap(旧值,结果)){
        // 成功，退出循环
    }
}
```

获取共享变量时，为了保证该变量的可见性，需要使用voIflIe修饰。结合 CAS 和 volatile 可以实现无锁并发，适用于竞争不激烈、多核CPU的场景下。 

- 因为没有使用 synchronized，所以线程不会陷入阻塞，这是效率提升的因素之一 
- 但如果竟争激烈，可以想到重试必然频繁发生，反而效率会受影响

CAS底层依赖于一个 Unsafe 类来直接调用操作系统底层的 CAS 指令，下面是直接使用 Unsafe 对象进行线程安全保的一个例子 

```java
public class JavaApp {
    public static void main(String[]args) throws InterruptedException {
        DataContainer dc = new DataContainer();
        int count = 5;
        Thread t1 = new Thread(() -> {
           for(int i = 0; i < count; i++){
               dc.increase();
           }
        });
        t1.start();
        t1.join();
        System.out.println(dc.getData());
    }
}

class DataContainer{
    private volatile int data;
    static final Unsafe unsafe;
    static final long DATA_OFFSET;

    static {
        try{
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e){
            throw new Error(e);
        }
        try{
            // data 属性在 DataContainer 对象中的偏移量，用于 unsafe 直接访问该属性
            DATA_OFFSET = unsafe.objectFieldOffset(DataContainer.class.getDeclaredField("data"));
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    public void increase(){
        int oldValue;
        while (true){
            oldValue = data;
            if(unsafe.compareAndSwapInt(this, DATA_OFFSET, oldValue, oldValue + 1)){
                return ;
            }
        }
    }

    public void decrease(){
        int oldValue;
        while (true){
            oldValue = data;
            if(unsafe.compareAndSwapInt(this, DATA_OFFSET, oldValue, oldValue - 1)){
                return ;
            }
        }
    }

    public int getData(){
        return data;
    }
}

```



#### 乐观锁与悲观锁

- CAS 是基于乐观锁的思想：最乐观的估计，不怕别的线程来修改共享变量，就算改了也没关系，我吃亏点再重试呗。 
- synchronized 是基于悲观锁的思想：最悲观的估计，得防看其它线程来修改共亨变量，我上了锁你们都别想改，我改完了解开锁，你们才有机会 。





#### 原子操作类

juc（java.util.concurrent）中提供了原子操作类，可以提供线程安全的操作，例如：AtomicInteger、AtomicBoolean 等，它们底层就是采用 CAS 技术 + volatile 来实现的。

可以使用 AtomicInteger 改写之前的例子：

```java
public class JavaApp {
    private static AtomicInteger i = new AtomicInteger(0);
    public static void main(String[]args) throws InterruptedException {
        DataContainer dc = new DataContainer();
        Thread t1 = new Thread(() -> {
           for(int i = 0; i < 5000; i++){
               i.getAndIncrease();
           }
        });
         Thread t2 = new Thread(() -> {
           for(int i = 0; i < 5000; i++){
               i.getAndDecrease();
           }
        });
        t1.start();
        t2.start();
        System.out.println(dc.getData());
    }
}
```





### synchronized 优化

Java HetSpot 虚拟机中每个对象都有对象头（包括 class 指针和 Mark Ward）。 Mark Ward 平时存储这个对象的哈希码、分代年龄，当加锁时，这些信息就根据倩况被替换为标记位、线程锁记录指针、重量级锁指针、线程 ID 等内容 。

#### 轻量级锁

如果一个对象虽然有多线程访问但多线程访问的时间是错开的（也就是没有竞争），那么可以使用轻量级锁来优化。这就好比： 

学生（线程 A 用课本占座上了半节课，出门了（CPU时间到）回来一看发现课本没变，说明没有竟争，继续上他的课。

如果这期间有其它学生（线程 B）来了，会告知（线移 A）有并发访问，线程 A 随即升级为重量级锁，进入重量级锁的流程。

而重量级锁就不是那么用课本占座那么简单了，可以想象线程 A 走之前把座位用一个铁栅栏围起来，假设有两个方法同步块，利用同一个对象加锁

```java
public class JavaApp {
    private static Object object = new Object();
    public static void main(String[]args) {

    }
    public static void method1(){
        synchronized (object){
            method2();
        }
    }
    public static void method2(){
        synchronized (object){

        }
    }
}
```

每个线程都的栈帧都会包含一个锁记录的结构，内部可以存储锁定对象的 Mark Word

| 线程 1                                           | 对象 Mark Word                | 线程 2                                           |
| ------------------------------------------------ | ----------------------------- | ------------------------------------------------ |
| 访问同步块 A，把 Mark Word 赋值到线程 1 的锁记录 | 01（无锁）                    | -                                                |
| CAS 修改 Mark Word 为线程 1 锁记录地址           | 01（无锁）                    | -                                                |
| 成功（加锁）                                     | 00（轻量锁）线程 1 锁记录地址 | -                                                |
| 执行同步块 A                                     | 00（轻量锁）线程 1 锁记录地址 | -                                                |
| 访问同步块 B，把 Mark Word 复制到线程 1 的锁记录 | 00（轻量锁）线程 1 锁记录地址 | -                                                |
| CAS 修改 Mark Word 为线程 1 锁记录地址           | 00（轻量锁）线程 1 锁记录地址 | -                                                |
| 失败（发现是自己的锁）                           | 00（轻量锁）线程 1 锁记录地址 | -                                                |
| 锁重入                                           | 00（轻量锁）线程 1 锁记录地址 | -                                                |
| 执行同步块 B                                     | 00（轻量锁）线程 1 锁记录地址 | -                                                |
| 同步块 B 执行完毕                                | 00（轻量锁）线程 1 锁记录地址 | -                                                |
| 同步块 A 执行完毕                                | 00（轻量锁）线程 1 锁记录地址 | -                                                |
| 成功（解锁）                                     | 01（无锁）                    | -                                                |
| -                                                | 01（无锁）                    | 访问同步块 A，把 Mark Word 赋值到线程 2 的锁记录 |
| -                                                | 01（无锁）                    | CAS 修改 Mark Word 为线程 2 锁记录地址           |
| -                                                | 00（轻量锁）线程 2 锁记录地址 | 成功（加锁）                                     |
| ...                                              | ...                           | ...                                              |



#### 锁膨胀

如果再尝试加轻量级锁的过程中，CAS 操作无法成功，这时一种情况就是有其它线程为此对象加上了轻量级锁（有竞争），这时需要进行锁膨胀，将轻量级锁变为重量级锁。

```java
public class JavaApp {
    private static Object object = new Object();
    public static void main(String[]args) {

    }
    public static void method1(){
        synchronized (object){
            method2();
        }
    }
}

```



| 线程 1                                           | 对象 Mark Word                                           | 线程 2                                         |
| ------------------------------------------------ | -------------------------------------------------------- | ---------------------------------------------- |
| 访问同步块 A，把 Mark Word 赋值到线程 1 的锁记录 | 01（无锁）                                               | -                                              |
| CAS 修改 Mark Word 为线程 1 锁记录地址           | 01（无锁）                                               | -                                              |
| 成功（加锁）                                     | 00（轻量锁）线程 1 锁记录地址                            | -                                              |
| 执行同步块                                       | 00（轻量锁）线程 1 锁记录地址                            | -                                              |
| 执行同步块                                       | 00（轻量锁）线程 1 锁记录地址                            | 访问同步块，把 Mark Word 赋值到线程 2 的锁记录 |
| 执行同步块                                       | 00（轻量锁）线程 1 锁记录地址                            | CAS 修改 Mark Word 为线程 2 锁记录地址         |
| 执行同步块                                       | 00（轻量锁）线程 1 锁记录地址                            | 失败（发现别人已经占了锁）                     |
| 执行同步块                                       | 00（轻量锁）线程 1 锁记录地址                            | CAS 修改 Mark Word 为重量级锁                  |
| 执行同步块                                       | 10（重量锁）重量锁指针（用于唤醒那些竞争重量级锁的线程） | 阻塞中                                         |
| 执行完毕                                         | 10（重量锁）重量锁指针                                   | 阻塞中                                         |
| 失败（解锁）                                     | 10（重量锁）重量锁指针                                   | 阻塞中                                         |
| 释放重量锁，唤起阻塞线程竞争                     | 10（重量锁）                                             | 阻塞中                                         |
| -                                                | 10（重量锁）                                             | 竞争重量锁                                     |
| -                                                | 10（重量锁）                                             | 成功（加锁）                                   |
| ...                                              | ...                                                      | ...                                            |



#### 重量锁

重量级锁竟争的时候，还可以使用自旋来进行优化，如果当前线程自旋成功（即这时候持锁线程已经退出了同步块，释放了锁），这时当前线程就可以避免阻塞。

在 Java 6 之后自旋锁是自适应的，比如对象刚刚的一次自旋操作成功过，那么认为这次自旋成功的可能性会高，就多自旋几次；反之，就少自旋甚至不自旋，总之，比较智能。

- 自旋会占用 CPU 时间，单核 CPU 自旋就是浪费，多核 CPU 自旋才能发挥优势。 
- 好比等红灯时汽车是不是熄火，不熄火相当于自旋（等待时间短了划算），熄火了相当于阻塞（等待时间长了划算）
- Java 7 之后不能控制是否开启自旋功能



自旋重试成功的情况

| 线程 1（CPU 1 上）       | 对象 Mark Word         | 线程 2（CPU 2 上）       |
| ------------------------ | ---------------------- | ------------------------ |
| -                        | 10（重量锁）           | -                        |
| 访问同步块，获取 monitor | 10（重量锁）重量锁指针 | -                        |
| 成功（加锁）             | 10（重量锁）重量锁指针 | -                        |
| 执行同步块               | 10（重量锁）重量锁指针 | -                        |
| 执行同步块               | 10（重量锁）重量锁指针 | 访问同步块，获取 monitor |
| 执行同步块               | 10（重量锁）重量锁指针 | 自旋重试                 |
| 执行完毕                 | 10（重量锁）重量锁指针 | 自旋重试                 |
| 成功（解锁）             | 01（无锁）             | 自旋重试                 |
| -                        | 10（重量锁）重量锁指针 | 成功（加锁）             |
| -                        | 10（重量锁）重量锁指针 | 执行同步块               |
| -                        | ...                    | ...                      |

自旋重试失败的情况

| 线程 1（CPU 1 上）       | 对象 Mark Word         | 线程 2（CPU 2 上）       |
| ------------------------ | ---------------------- | ------------------------ |
| -                        | 10（重量锁）           | -                        |
| 访问同步块，获取 monitor | 10（重量锁）重量锁指针 | -                        |
| 成功（加锁）             | 10（重量锁）重量锁指针 | -                        |
| 执行同步块               | 10（重量锁）重量锁指针 | -                        |
| 执行同步块               | 10（重量锁）重量锁指针 | 访问同步块，获取 monitor |
| 执行同步块               | 10（重量锁）重量锁指针 | 自旋重试                 |
| 执行同步块               | 10（重量锁）重量锁指针 | 自旋重试                 |
| 执行同步块               | 10（重量锁）重量锁指针 | 自旋重试                 |
| 执行同步块               | 10（重量锁）重量锁指针 | 阻塞                     |
| ...                      | ...                    | ...                      |



#### 偏向锁

轻量级锁在没有竞争时（就自己这个线程），每次重入仍然需要执行 CAS 操作。Java 6 中引入了偏向锁来做进一步优化：只有第一次使用 CAS 将线程 ID 设置到对象的 Merk Word 头，之后发现这个线程 ID 是自己的就表示没有竞争，不用重新 CAS。

- 撤销偏向锁需要将持锁线程升级为轻量级锁，这个过程中所有线程需要暂停（STW）
- 访问对象的 hashCode 也会撤销偏向锁
- 如果对象虽然被多个线程访问，但没有竞争，这时偏向了线程 T1 的对象仍有机会重新偏向 T2，重偏向会重置对象的 Thread ID
- 撤销偏向和重偏向都是批量进行的，以类为单位
- 如果撤销偏向到达某个阈值，整个类的所有对象都会变为不可偏向的
- 可以主动使用 -XX：-UseBiasedLocking 禁用偏向锁

可以参考这篇 [论文](https://www.oracle.com/technetwork/java/biasedlocking-oopsia2006-wp-149958.pdf)

假设有两个同步块，利用同一个对象加锁

```java
public class JavaApp {
    private static Object object = new Object();
    public static void main(String[]args) {

    }
    public static void method1(){
        synchronized (object){
            method2();
        }
    }
    public static void method2(){
        synchronized (object){

        }
    }
}
```



| 线程 1                                       | 对象 Mark Word                  |
| -------------------------------------------- | ------------------------------- |
| 访问同步块 A，检查 Mark Word 中是否有线程 ID | 101 （无锁可偏向）              |
| 尝试加偏向锁                                 | 101 （无锁可偏向）对象 hashCode |
| 成功                                         | 101 （无锁可偏向）线程 ID       |
| 执行同步块 A                                 | 101 （无锁可偏向）线程 ID       |
| 访问同步块 B，检查 Mark Word 中是否有线程 ID | 101 （无锁可偏向）线程 ID       |
| 是自己的线程 ID，锁是自己的，无需做更多操作  | 101 （无锁可偏向）线程 ID       |
| 执行同步块 B                                 | 101 （无锁可偏向）线程 ID       |
| 执行完毕                                     | 101 （无锁可偏向）对象 hashCode |





#### 其它优化



##### 减少上锁时间

同步代码块尽量短。



##### 减少锁的粒度

将一个锁拆分为多个锁提高并发度，例如：

- ConcurrentHashMap
- LongAdder 分为 base 和 cells 两部分．没有并发争用的时候或者是 cells 数组正在初始化的时候，会使用 CAS 来累加值到 base．有并发争用，会初始化 cells 数组，数组有多少个 cell，就允许有多少线程并行修改，最后将数组中每个 cell 累加，再加上 base 就是最终的值
- LinkedBlockiugQueue 入队和出队使用不同的锁，相对于 LinkedBlockiugArray 只有一个锁效率要高 



##### 锁粗化

多次循环进入同步块不如同步块内多次循环

另外 JVM 可能会做如下优化，把多次 append 的加锁操作粗化为一次（因为都是对同一个对象加锁，没必要重入多次）

```java
new StringBuffer().append("a").append("b").append("c");
```



##### 锁消除

JVM 会进行代码的逃逸分析，例如某个加锁对象是方法内局部变量，不会被其它线程所访问到，这时候就会被即时编译器忽略掉所有同步操作。



##### 读写分离

CopyOnWriteArrayList

CopyOnWriteSet

参考

https://wiki.openjdk.java.net/display/HotSpot/Synchronization

http://luojinping.com/2015/07/09/java锁优化/

https://www.infoq.cn/article/java-se-16-synchronized

https://www.jianshu.com/p/9932047a89be

https://www.cnblogs.com/sheeva/p/6366782.html





# Java

[java启动jar包引入外部配置文件_jar包加载外部配置文件_飘零未归人的博客-CSDN博客](https://blog.csdn.net/qq_34484062/article/details/128021066)

```shell
java -jar ./test.jar -Dspring.config.location=../config/application-my.yaml
java -jar ./test.jar -Dspring.config.additional-location=../config/application-my.yaml
# spring.config.location：会覆盖内部配置参数
# spring.config.additional-location：会和内部配置参数互补
```



## 多线程

### 相关概念

#### 进程

​		进程是程序运行和资源分配的基本单位，一个程序至少有一个进程，一个进程至少有一个线程。进程在执行过程中拥有独立的内存单元，而多个线程共享内存资源，减少切换次数，从而效率更高。线程是进程的一个实体，是cpu调度和分派的基本单位，是比程序更小的能独立运行的基本单位。同一进程中的多个线程之间可以并发执行。



#### 线程

​		线程是操作系统调度的最小单元，也叫轻量级进程。它被包含在进程之中，是进程中的实际运作单位。同一进程可以创建多个线程。

​		一个进程中可以有多个线程，多个线程共享进程的**堆**和**方法区 (JDK1.8 之后的元空间)**资源，但是每个线程有自己的**程序计数器**、**虚拟机栈** 和 **本地方法栈**。



#### 并发

​		当有多个线程在操作时,如果系统只有一个CPU,则它根本不可能真正同时进行一个以上的线程，它只能把CPU运行时间划分成若干个时间段,再将时间 段分配给各个线程执行，在一个时间段的线程代码运行时，其它线程处于挂起状。这种方式我们称之为并发(Concurrent)。



#### 并行

​		当系统有一个以上CPU时,则线程的操作有可能非并发。当一个CPU执行一个线程时，另一个CPU可以执行另一个线程，两个线程互不抢占CPU资源，可以同时进行，这种方式我们称之为并行(Parallel)。



#### 线程的生命周期和状态

​																*获得同步锁									等待同步锁*

​																*阻塞IO方法返回							调用IO阻塞方法*

​																*调用notify()方法							调用wait()方法*

​																*调用join()的线程终止					调用join()方法*

​																*sleep()时间到								调用sleep()方法*

​															<-------------  **阻塞状态(Blocked)**	<-------------

​								     	  				|							*获得CPU使用权*					|

​								*start()*				|							-------------------->   				 |

**新建状态(New)**	----------->   **就绪状态(Runnable)**                       			**运行状态(Running)**	----------->   **死亡状态(Terminated)**

​																						<--------------------											*run()执行完*

​																						*失去CPU使用权*											*Exception或Error*



#### 上下文切换

​		线程在执行过程中会有自己的运行条件和状态（也称上下文），比如上文所说到过的程序计数器，栈信息等。当出现如下情况的时候，线程会从占用 CPU 状态中退出。

- 主动让出 CPU，比如调用了 `sleep()`, `wait()` 等。
- 时间片用完，因为操作系统要防止一个线程或者进程长时间占用CPU导致其他线程或者进程饿死。
- 调用了阻塞类型的系统中断，比如请求 IO，线程被阻塞。
- 被终止或结束运行

这其中前三种都会发生线程切换，线程切换意味着需要保存当前线程的上下文，留待线程下次占用 CPU 的时候恢复现场。并加载下一个将要占用 CPU 的线程上下文。这就是所谓的 **上下文切换**。

​		上下文切换是现代操作系统的基本功能，因其每次需要保存信息恢复信息，这将会占用 CPU，内存等系统资源进行处理，也就意味着效率会有一定损耗，如果频繁切换就会造成整体效率低下。



#### 死锁及避免死锁

​		死锁是指两个或两个以上的进程在执行过程中，由于竞争资源或者由于彼此通信而造成的一种阻塞的现象，若无外力作用，它们都将无法推进下去。此时称系统处于死锁状态或系统产生了死锁，这些永远在互相等待的进程称为死锁进程。

产生死锁必须具备以下四个条件：

1. 互斥条件：该资源任意一个时刻只由一个线程占用。
2. 请求与保持条件：一个进程因请求资源而阻塞时，对已获得的资源保持不放。
3. 不剥夺条件:线程已获得的资源在未使用完之前不能被其他线程强行剥夺，只有自己使用完毕后才释放资源。
4. 循环等待条件:若干进程之间形成一种头尾相接的循环等待资源关系。

**如何预防死锁？** 

破坏死锁的产生的必要条件即可：

1. **破坏请求与保持条件** ：一次性申请所有的资源。
2. **破坏不剥夺条件** ：占用部分资源的线程进一步申请其他资源时，如果申请不到，可以主动释放它占有的资源。
3. **破坏循环等待条件** ：靠按序申请资源来预防。按某一顺序申请资源，释放资源则反序释放。破坏循环等待条件。

**如何避免死锁？**

避免死锁就是在资源分配时，借助于算法（比如银行家算法）对资源分配进行计算评估，使其进入安全状态。



#### 线程的 run()和 start()有什么区别？

​		每个线程都是通过某个特定Thread对象所对应的方法run()来完成其操作的，方法run()称为线程体。通过调用Thread类的start()方法来启动一个线程。

​		start()方法来启动一个线程，真正实现了多线程运行。这时无需等待run方法体代码执行完毕，可以直接继续执行下面的代码； 这时此线程是处于就绪状态， 并没有运行。 然后通过此Thread类调用方法run()来完成其运行状态， 这里方法run()称为线程体，它包含了要执行的这个线程的内容， Run方法运行结束， 此线程终止。然后CPU再调度其它线程。

​		run()方法是在本线程里的，只是线程里的一个函数,而不是多线程的。 如果直接调用run(),其实就相当于是调用了一个普通函数而已，直接待用run()方法必须等待run()方法执行完毕才能执行下面的代码，所以执行路径还是只有一条，根本就没有线程的特征，所以在多线程执行时要使用start()方法而不是run()方法。



#### sleep() 和 wait() 有什么区别？

​		sleep()：方法是线程类（Thread）的静态方法，让调用线程进入睡眠状态，让出执行机会给其他线程，等到休眠时间结束后，线程进入就绪状态和其他线程一起竞争cpu的执行时间。因为sleep() 是static静态的方法，他不能改变对象的机锁，当一个synchronized块中调用了sleep() 方法，线程虽然进入休眠，但是对象的机锁没有被释放，其他线程依然无法访问这个对象。

​		wait()：wait()是Object的方法，当一个线程执行到wait方法时，它就进入到一个和该对象相关的等待池，同时释放对象的机锁，使得其他线程能够访问，可以通过notify，notifyAll方法来唤醒等待的线程。



####  Runnable 和 Callable 有什么区别？

Runnable接口中的run()方法的返回值是void，它做的事情只是纯粹地去执行run()方法中的代码而已；
Callable接口中的call()方法是有返回值的，是一个泛型，和Future、FutureTask配合可以用来获取异步执行的结果。



### 创建线程的方式

#### Thread

```java
public class TestThread {
    public static void main(String[]args){
        MyThread myThread=new MyThread();
        myThread.start();
    }
}

class MyThread extends Thread{
    public void run(){
        System.out.println("MyThread类的run()方法在运行");
    }
}
```



#### Runnable

```java
public class TestThread {
    public static void main(String[]args){
        Thread thread=new Thread(new MyThread());
        thread.start();
    }
}

class MyThread implements Runnable{
    public void run(){
        System.out.println("MyThread类的run()方法在运行");
    }
}
```

实现Runnbale接口相对于继承Thread类来说，有如下显著好处：

- 适合多个相同程序代码的线程去处理同一个资源的情况，把线程同程序代码、数据有效分离，很好地体现了面向对象的设计思想。
- 可以避免由于Java的单继承带来的局限性。在开发中经常碰到这样一种情况，就是使用一个已经继承了某一个类的子类创建线程，由于一个类不能同时有两个父类，所以不能用继承Thread类的方式，那么就只能采用Runnable接口的方式。



#### Callable(Future、FutureTask)

https://www.cnblogs.com/dolphin0520/p/3949310.html

```java
public class TestThread {
    public static void main(String[]args) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        MyThread myThread = new MyThread();
        Future<Integer> result = executor.submit(myThread);
        executor.shutdown();
        System.out.println("myThread返回"+result.get());
    }
}

class MyThread implements Callable<Integer> {
    public Integer call(){
        System.out.println("MyThread类的call()方法在运行");
        Random random=new Random();
        return random.nextInt();
    }
}
```



### 线程池

#### **线程池的优势**

- 降低系统资源消耗，通过重用已存在的线程，降低线程创建和销毁造成的消耗；
- 提高系统响应速度，当有任务到达时，通过复用已存在的线程，无需等待新线程的创建便能立即执行；
- 方便线程并发数的管控。因为线程若是无限制的创建，可能会导致内存占用过多而产生OOM，并且会造成cpu过度切换（cpu切换线程是有时间成本的（需要保持当前执行线程的现场，并恢复要执行线程的现场））
- 提供更强大的功能，延时定时线程池。



```java
public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
    this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
         Executors.defaultThreadFactory(), defaultHandler);
}
```



#### **Java 通过 Executors 提供四种线程池**

| 方法                    | corePoolSize | maximumPoolSize   | keepAliveTime | unit                  | workQueue           |
| ----------------------- | ------------ | ----------------- | ------------- | --------------------- | ------------------- |
| newSingleThreadExecutor | 1            | 1                 | 0             | TimeUnit.MILLISECONDS | LinkedBlockingQueue |
| newFixedThreadPool      | nThreads     | nThreads          | 0             | TimeUnit.MILLISECONDS | LinkedBlockingQueue |
| newScheduledThreadPool  | corePoolSize | Integer.MAX_VALUE | 0             | TimeUnit.NANOSECONDS  | DelayedWorkQueue    |
| newCachedThreadPool     | 0            | Integer.MAX_VALUE | 60s           | TimeUnit.SECONDS      | SynchronousQueue    |



```java
public class RunTask implements Runnable{
	private String msg="";
	public RunTask(String msg) {
		this.msg=msg;
	}
	public void run() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("线程名称："+Thread.currentThread().getName()+","+msg);
	}
}

public class CallTask implements Callable<String>{
	private String msg="";
	public CallTask(String msg) {
		this.msg=msg;
	}
	public String call() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "线程名称："+Thread.currentThread().getName()+","+msg;
	}
}
```



#### newSingleThreadExecutor 

- 创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行。当该线程正繁忙时，对于新任务会进入阻塞队列中(无界的阻塞队列)

- 适用：一个任务一个任务执行的场景

  ```java
  public void testSingleThreadExecutor() throws InterruptedException, ExecutionException {
  	ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
      for (int i = 0; i < 10; i++) {
  //  	singleThreadPool.execute(new RunTask("任务: "+i));
      	Future<String> result=(Future<String>) singleThreadPool.submit(new CallTask("任务: "+i));
  		System.out.println(result.get());
      }
      singleThreadPool.shutdown();
  }
  
  //创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，每秒输出一个
  //线程名称：pool-1-thread-1,任务: 0
  //线程名称：pool-1-thread-1,任务: 1
  //线程名称：pool-1-thread-1,任务: 2
  //线程名称：pool-1-thread-1,任务: 3
  //线程名称：pool-1-thread-1,任务: 4
  //线程名称：pool-1-thread-1,任务: 5
  //线程名称：pool-1-thread-1,任务: 6
  //线程名称：pool-1-thread-1,任务: 7
  //线程名称：pool-1-thread-1,任务: 8
  //线程名称：pool-1-thread-1,任务: 9
  ```

#### newFixedThreadPool 

- 创建可容纳固定数量线程的池子，每个线程的存活时间是无限的，当池子满了就不在添加线程了；如果池中的所有线程均在繁忙状态，对于新任务会进入阻塞队列中(无界的阻塞队列)

- 适用：执行长期的任务，性能好很多

```java
public void testFixedThreadPool() throws InterruptedException, ExecutionException {
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
		for (int i = 0; i < 10; i++) {
//			fixedThreadPool.execute(new RunTask("任务: "+i));
			Future<String> result=(Future<String>) fixedThreadPool.submit(new CallTask("任务: "+i));
			System.out.println(result.get());
		}
		fixedThreadPool.shutdown();
}

//因为线程池大小为3，每个任务sleep 1秒，所以每1秒打印3个输出
//线程名称：pool-1-thread-1,任务: 0
//线程名称：pool-1-thread-3,任务: 2
//线程名称：pool-1-thread-2,任务: 1
//线程名称：pool-1-thread-3,任务: 4
//线程名称：pool-1-thread-1,任务: 3
//线程名称：pool-1-thread-2,任务: 5
//线程名称：pool-1-thread-1,任务: 7
//线程名称：pool-1-thread-3,任务: 6
//线程名称：pool-1-thread-2,任务: 8
//线程名称：pool-1-thread-1,任务: 9
```



#### newScheduledThreadPool 

- 创建一个固定大小的线程池，线程池内线程存活时间无限制，线程池可以支持定时及周期性任务执行，如果所有线程均处于繁忙状态，对于新任务会进入DelayedWorkQueue队列中，这是一种按照超时时间排序的队列结构

- 适用：周期性执行任务的场景

```java
public void testScheduledThreadPool() throws InterruptedException, ExecutionException {
	ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
	scheduledThreadPool.schedule(new RunTask("任务: 3秒后执行"), 3, TimeUnit.SECONDS);
	scheduledThreadPool.scheduleAtFixedRate(new RunTask("任务: 延迟2秒后每3秒执行一次"), 2, 3, TimeUnit.SECONDS);
	for (int i = 0; i < 10; i++) {
		Future<String> result=(Future<String>) scheduledThreadPool.submit(new CallTask("普通任务: "+i));
		System.out.println(result.get());
	}
	scheduledThreadPool.shutdown();
}

//线程名称：pool-1-thread-2,普通任务: 0
//线程名称：pool-1-thread-1,普通任务: 1
//线程名称：pool-1-thread-3,任务: 延迟2秒后每3秒执行一次
//线程名称：pool-1-thread-4,普通任务: 2
//线程名称：pool-1-thread-2,任务: 3秒后执行
//线程名称：pool-1-thread-5,普通任务: 3
//线程名称：pool-1-thread-1,普通任务: 4
//线程名称：pool-1-thread-3,任务: 延迟2秒后每3秒执行一次
//线程名称：pool-1-thread-2,普通任务: 5
//线程名称：pool-1-thread-4,普通任务: 6
//线程名称：pool-1-thread-3,普通任务: 7
//线程名称：pool-1-thread-5,任务: 延迟2秒后每3秒执行一次
//线程名称：pool-1-thread-2,普通任务: 8
//线程名称：pool-1-thread-1,普通任务: 9
```



#### newCachedThreadPool

- 当有新任务到来，则插入到 SynchronousQueue 中，由于 SynchronousQueue 是同步队列，因此会在池中寻找可用线程来执行，若有可以线程则执行，若没有可用线程则创建一个线程来执行该任务；若池中线程空闲时间超过指定大小，则该线程会被销毁

- 适用：执行很多短期异步的小程序或者负载较轻的服务器

```java
public void testCachedThreadPool() throws InterruptedException, ExecutionException {
	ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
	for (int i = 0; i < 10; i++) {
//			cachedThreadPool.execute(new RunTask("任务: "+i));
		Future<String> result=(Future<String>) cachedThreadPool.submit(new CallTask("任务: "+i));
		System.out.println(result.get());
	}
	cachedThreadPool.shutdown();
}

//线程名称：pool-1-thread-1,任务: 0
//线程名称：pool-1-thread-1,任务: 1
//线程名称：pool-1-thread-1,任务: 2
//线程名称：pool-1-thread-1,任务: 3
//线程名称：pool-1-thread-1,任务: 4
//线程名称：pool-1-thread-1,任务: 5
//线程名称：pool-1-thread-1,任务: 6
//线程名称：pool-1-thread-1,任务: 7
//线程名称：pool-1-thread-1,任务: 8
//线程名称：pool-1-thread-1,任务: 9
```



#### 手动创建线程池







## 集合

### 概述



![](D:\documents\notes\solid\Java.assets\076.jpg)

![](D:\documents\notes\solid\Java.assets\077-178892435315081.jpg)





| 集合              | 初始大小 | 加载因子 | 扩容 | 线程安全？ |
| ----------------- | -------- | -------- | ---- | ---------- |
| HashSet           | 16       | 0.75     | 1    | 不安全     |
| TreeSet           |          |          |      |            |
| LinkedHashSet     |          |          |      |            |
| ArrayList         | 10       | 1        | 0.5  | 不安全     |
| Vector            | 10       | 1        | 1    | 安全       |
| LinkedList        |          |          |      | 不安全     |
| HashMap           | 16       | 0.75     | 1    | 不安全     |
| TreeMap           |          |          |      |            |
| LinkedHashMap     |          |          |      |            |
| ConcurrentHashMap |          |          |      | 安全       |
| HashTable         |          |          |      | 安全       |





### List



#### 相关概念

##### ArrayList 和 LinkedList 的线程安全处理

​		ArrayList是线程不安全的，因此在并发编程时，经常会使用Collections.synchronizedList与CopyOnWriteArrayList来替代ArrayList，接下来对这两种list进行性能的比较。其中Collections.synchronizedList在更新操作中使用了同步锁，而CopyOnWriteArrayList在更新操作中不仅使用了可重入锁，而且还需要进行数组的复制。

方法一:List<String> list = Collections.synchronizedList(new LinkedList<String>());

方法二:将LinkedList全部换成ConcurrentLinkedQueue；



##### ArrayList 是如何扩容的？

​		如果通过无参构造的话，初始数组容量为 0 ，当真正对数组进行添加时，才真正分配容量。每次按照 1.5 倍（位运算）的比率通过 copeOf 的方式扩容。

在 JKD6 中实现是，如果通过无参构造的话，初始数组容量为10，每次通过 copeOf 的方式扩容后容量为原来的 1.5 倍。



##### Array 和 ArrayList 有何区别？什么时候更适合用 Array？

Array 可以容纳基本类型和对象，而 ArrayList 只能容纳对象。

Array 是指定大小的，而 ArrayList 大小是固定的，可自动扩容。

Array 没有提供 ArrayList 那么多功能，比如 addAll、removeAll 和 iterator 等。



#### ArrayList

​		ArrayList 是最常用的 List 实现类，内部是通过数组实现的，它允许对元素进行快速随机访问。数组的缺点是每个元素之间不能有间隔，当数组大小不满足时需要增加存储能力，就要将已经有数组的数据复制到新的存储空间中。当从 ArrayList 的中间位置插入或者删除元素时，需要对数组进行复制、移动、代价比较高。因此，它适合随机查找和遍历，不适合插入和删除。排列有序，可重复，容量不够的时候，当前容量*1.5+1。



#### Vector

​		Vector 与 ArrayList 一样，也是通过数组实现的，不同的是它支持线程的同步，即某一时刻只有一个线程能够写 Vector，避免多线程同时写而引起的不一致性，但实现同步需要很高的花费，因此，访问它比访问 ArrayList 慢。默认扩展一倍容量。



#### LinkedList

​		LinkedList 是用链表结构存储数据的，很适合数据的动态插入和删除，随机访问和遍历速度比较慢。另外，他还提供了 List 接口中没有定义的方法，专门用于操作表头和表尾元素，可以当作堆栈、队列和双向队列使用。底层使用双向循环链表数据结构。线程不安全。



#### CopyOnWriteArrayList

​		CopyOnWriteArrayList : 写时加锁，当添加一个元素的时候，将原来的容器进行copy，复制出一个新的容器，然后在新的容器里面写，写完之后再将原容器的引用指向新的容器，而读的时候是读旧容器的数据，所以可以进行并发的读，但这是一种弱一致性的策略。
使用场景：CopyOnWriteArrayList适合使用在读操作远远大于写操作的场景里，比如缓存。



#### List 转 Map

```java
// 第一种：实体Bean的某一属性作为K,V
public Map<Long, String> getIdNameMap(List<User> users) {
    return accounts.stream().collect(Collectors.toMap(User::getId, User::getUserName));
}
// 第二种：将某一属性和实体Bean做为K,V account -> account是一个返回本身的lambda表达式，后面的使用Function接口中的一个默认方法代替，使整个方法更简洁优雅。
public Map<Long, Account> getIdAccountMap(List<Account> accounts) {
    return accounts.stream().collect(Collectors.toMap(Account::getId, account -> account));
}
public Map<Long, Account> getIdAccountMap(List<Account> accounts) {
    return accounts.stream().collect(Collectors.toMap(Account::getId, Function.identity()));
}
// 第三种： key存在重复记录时处理,如果使用第一种方法会出错，所以这里只是简单的使用后者覆盖前者来解决key重复问题
public Map<String, Account> getNameAccountMap(List<Account> accounts) {
    return accounts.stream().collect(Collectors.toMap(Account::getUsername, Function.identity(), (key1, key2) -> key2));
}
// 第四种： 使用某个具体的 Map 类来保存，如保存时使用 LinkedHashMap
public Map<String, Account> getNameAccountMap(List<Account> accounts) {
    return accounts.stream().collect(Collectors.toMap(Account::getUsername, Function.identity(), (key1, key2) -> key2, LinkedHashMap::new));
}

// 处理当value值为空时会报空指针异常
// 解决方案一，使用Optional类处理null
public Map<String, Account> getNameAccountMap(List<Account> accounts) {
    return accounts.stream().collect(Collectors.toMap(item -> Optional.ofNullable(item.getId()).orElse(null), item -> Optional.ofNullable(item.getUserName()).orElse("unknown"), (key1, key2) -> key2));
}
// 解决方案二，直接使用collect()方法进行规约操作
public Map<String, String> getNameAccountMap(List<Account> accounts) {
    return accounts.stream().collect(HashMap::new, (map, item) -> map.put(item.getUsername(), item.getPassword()), HashMap::putAll);
}
```



#### List对象 转 List属性

```java
List<String> names=list.stream().map(PersonData::getName).collect(Collectors.toList());
```



#### List 分组

```java
Map<String, List<DataVO>> map = list.stream().collect(Collectors.groupingBy(item -> item.getName()));
// 或
Map<String, List<DataVO>> map = list.stream().collect(Collectors.groupingBy(DataVO::getName));
```



#### 字符串 List 拼接

```java
String str = list.stream().collect(Collectors.joining(","));
String str = String.join(",", list);
```



#### 遍历过程中对集合进行增删



#### 二维数组转一维数组

```java
// 数组
public static int[] convertToArray(int[][] matrix) {
    return Arrays.stream(matrix)
            .flatMapToInt(Arrays::stream)
            .toArray();
}

// list
public static List<DataVO> convertToArray(List<List<DataVO>> dataList) {
    return dataList.stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
}

public static List<ChildDataVO> convertToArray(List<ParentDataVO> parentList) {
    return parentList.stream()
        .map(ParentDataVO::getChildList)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
}
```





### Set



#### HashSet（Hash **表）**

​		哈希表边存放的是哈希值。HashSet 存储元素的顺序并不是按照存入时的顺序（和 List 显然不同） 而是按照哈希值来存的所以取数据也是按照哈希值取得。元素的哈希值是通过元素的hashcode 方法来获取的, HashSet 首先判断两个元素的哈希值，如果哈希值一样，接着会比较equals 方法 如果 equls 结果为 true ，HashSet 就视为同一个元素。如果 equals 为 false 就不是同一个元素。

哈希值相同 equals 为 false 的元素是怎么存储呢,就是在同样的哈希值下顺延（可以认为哈希值相同的元素放在一个哈希桶中）。也就是哈希一样的存一列。如图 1 表示 hashCode 值不相同的情况；图 2 表示 hashCode 值相同，但 equals 不相同的情况。

HashSet 通过 hashCode 值来确定元素在内存中的位置。一个 hashCode 位置上可以存放多个元素。



#### TreeSet（二叉树）

1. TreeSet()是使用二叉树的原理对新 add()的对象按照指定的顺序排序（升序、降序），每增

加一个对象都会进行排序，将对象插入的二叉树指定的位置。

2. Integer 和 String 对象都可以进行默认的 TreeSet 排序，而自定义类的对象是不可以的，自己定义的类必须实现 Comparable 接口，并且覆写相应的 compareTo()函数，才可以正常使用。

3. 在覆写 compare()函数时，要返回相应的值才能使 TreeSet 按照一定的规则来排序

4. 比较此对象与指定对象的顺序。如果该对象小于、等于或大于指定对象，则分别返回负整数、零或正整数。

   

#### LinkHashSet（HashSet+LinkedHashMap）

​		对于 LinkedHashSet 而言，它继承与 HashSet、又基于 LinkedHashMap 来实现的。LinkedHashSet 底层使用 LinkedHashMap 来保存所有元素，它继承与 HashSet，其所有的方法操作上又与 HashSet 相同，因此 LinkedHashSet 的实现上非常简单，只提供了四个构造方法，并通过传递一个标识参数，调用父类的构造器，底层构造一个 LinkedHashMap 来实现，在相关操作上与父类 HashSet 的操作相同，直接调用父类 HashSet 的方法即可。



### Map

#### 相关概念

##### hashmap的工作原理是什么？

​		HashMap是基于hashing的原理，我们使用put(key, value)存储对象到HashMap中，使用get(key)从HashMap中获取对象。当我们给put()方法传递键和值时，我们先对键调用hashCode()方法，计算并返回的hashCode是用于找到Map数组的bucket位置来储存Node 对象。这里关键点在于指出，HashMap是在bucket中储存键对象和值对象，作为Map.Node 。

以下是具体的put过程（JDK1.8版）

1.对Key求Hash值，然后再计算下标

2.如果没有碰撞，直接放入桶中（碰撞的意思是计算得到的Hash值相同，需要放到同一个bucket中）

3.如果碰撞了，则调用equals() 比较value，相同则替换旧值，不同则以链表的方式链接到后面

4.如果链表长度超过阀值( TREEIFY THRESHOLD==8)，就把链表转成红黑树，链表长度低于6，就把红黑树转回链表

5.如果桶满了(容量16*加载因子0.75)，就需要 resize（扩容2倍后重排）

以下是具体get过程(考虑特殊情况如果两个键的hashcode相同，你如何获取值对象？)

当我们调用get()方法，HashMap会使用键对象的hashcode找到bucket位置，找到bucket位置之后，会调用keys.equals()方法去找到链表中正确的节点，最终找到要找的值对象。



##### 拉链法导致的链表过深问题为什么不用二叉查找树代替，而选择红黑树？为什么不一直使用红黑树？

​		之所以选择红黑树是为了解决二叉查找树的缺陷，二叉查找树在特殊情况下会变成一条线性结构（这就跟原来使用链表结构一样了，造成很深的问题），遍历查找会非常慢。而红黑树在插入新数据后可能需要通过左旋，右旋、变色这些操作来保持平衡，引入红黑树就是为了查找数据快，解决链表查询深度的问题，我们知道红黑树属于平衡二叉树，但是为了保持“平衡”是需要付出代价的，但是该代价所损耗的资源要比遍历线性链表要少，所以当长度大于8的时候，会使用红黑树，如果链表长度很短的话，根本不需要引入红黑树，引入反而会慢。



#####  解决 hash 碰撞还有那些办法？

**开放定址法**

​		当冲突发生时，使用某种探查技术在散列表中形成一个探查(测)序列。沿此序列逐个单元地查找，直到找到给定的地址。按照形成探查序列的方法不同，可将开放定址法区分为线性探查法、二次探查法、双重散列法等。



**再哈希法**

Hi = RHi（key），i=1,2,...k

RHi均是不同的哈希函数，即在同义词产生地址冲突时计算另一个哈希函数地址，直到不发生冲突为止。这种方法不易产生聚集，但是增加了计算时间。

缺点：增加了计算时间。



**建立一个公共溢出区**

​		假设哈希函数的值域为[0,m-1]，则设向量HashTable[0...m-1]为基本表，每个分量存放一个记录，另设立向量OverTable[0....v]为溢出表。所有关键字和基本表中关键字为同义词的记录，不管他们由哈希函数得到的哈希地址是什么，一旦发生冲突，都填入溢出表。简单地说就是搞个新表存冲突的元素。



**链地址法（拉链法）**

​		将所有关键字为同义词的记录存储在同一线性链表中，也就是把冲突位置的元素构造成链表。



##### HashMap 的负载因子初始值为什么是 0.75?

​		比如说当前的容器容量是16，负载因子是0.75,16*0.75=12，也就是说，当容量达到了12的时候就会进行扩容操作。当负载因子是1.0的时候，也就意味着，只有当数组的8个值（这个图表示了8个）全部填充了，才会发生扩容。这就带来了很大的问题，因为Hash冲突时避免不了的。当负载因子是1.0的时候，意味着会出现大量的Hash的冲突，底层的红黑树变得异常复杂。对于查询效率极其不利。这种情况就是牺牲了时间来保证空间的利用率。因此一句话总结就是负载因子过大，虽然空间利用率上去了，但是时间效率降低了。

​		负载因子是0.5的时候，这也就意味着，当数组中的元素达到了一半就开始扩容，既然填充的元素少了，Hash冲突也会减少，那么底层的链表长度或者是红黑树的高度就会降低。查询效率就会增加。但是，这时候空间利用率就会大大的降低，原本存储1M的数据，现在就意味着需要2M的空间。一句话总结就是负载因子太小，虽然时间效率提升了，但是空间利用率降低了。大致意思就是说负载因子是0.75的时候，空间利用率比较高，而且避免了相当多的Hash冲突，使得底层的链表或者是红黑树的高度比较低，提升了空间效率。



##### Comparable **和** Comparator 的区别?

如果在定义类时，就实现了Comparable接口，直接在里面重写compareTo()方法，如果没实现，后面在业务开发中需要有比较排序的功能，就再单独写一个类实现Comparator接口，在里面重写compare()方法，然后这个类需要作为参数传入到工具类Collections.sort和Arrays.sort方法中。最主要的区别就是一个一开始就实现，一个是后期实现。



#### HashMap（数组+链表+红黑树）

​		HashMap 根据键的 HashCode 值存储数据，大多数情况下可以直接定位到它的值，因而具有很快的访问速度，但遍历顺序却是不确定的。 HashMap 最多只允许一条记录的键为 null，允许多条记录的值为 null。HashMap 非线程安全，即任一时刻可以有多个线程同时写 HashMap，可能会导致数据的不一致。如果需要满足线程安全，可以用 Collections 的 synchronizedMap 方法使HashMap 具有线程安全的能力，或者使用 ConcurrentHashMap。



##### java7 的 hashmap 实现

![](D:\documents\notes\solid\Java.assets\5.jpg)



​		大方向上，HashMap 里面是一个数组，然后数组中每个元素是一个单向链表。上图中，每个绿色的实体是嵌套类 Entry 的实例，Entry 包含四个属性：key, value, hash 值和用于单向链表的 next。

\1. capacity：当前数组容量，始终保持 2^n，可以扩容，扩容后数组大小为当前的 2 倍。

\2. loadFactor：负载因子，默认为 0.75。

\3. threshold：扩容的阈值，等于 capacity * loadFactor

##### java8 的 hashmap 实现

​		Java8 对 HashMap 进行了一些修改，最大的不同就是利用了红黑树，所以其由 数组+链表+红黑树 组成。根据 Java7 HashMap 的介绍，我们知道，查找的时候，根据 hash 值我们能够快速定位到数组的具体下标，但是之后的话，需要顺着链表一个个比较下去才能找到我们需要的，时间复杂度取决于链表的长度，为 O(n)。为了降低这部分的开销，在 Java8 中，当链表中的元素超过了 8 个以后，会将链表转换为红黑树，在这些位置进行查找的时候可以降低时间复杂度为 O(logN)。

![](D:\documents\notes\solid\Java.assets\4.jpg)



#### ConcurrentHashMap

##### java7 实现

Segment 段

​		ConcurrentHashMap 和 HashMap 思路是差不多的，但是因为它支持并发操作，所以要复杂一些。整个 ConcurrentHashMap 由一个个 Segment 组成，Segment 代表”部分“或”一段“的

意思，所以很多地方都会将其描述为分段锁。注意，行文中，我很多地方用了“槽”来代表一个segment。

线程安全（Segment 继承 ReentrantLock 加锁）

简单理解就是，ConcurrentHashMap 是一个 Segment 数组，Segment 通过继承ReentrantLock 来进行加锁，所以每次需要加锁的操作锁住的是一个 segment，这样只要保证每个 Segment 是线程安全的，也就实现了全局的线程安全。

![](D:\documents\notes\solid\Java.assets\3.jpg)



并行度（默认 16）

concurrencyLevel：并行级别、并发数、Segment 数，怎么翻译不重要，理解它。默认是 16，也就是说 ConcurrentHashMap 有 16 个 Segments，所以理论上，这个时候，最多可以同时支持 16 个线程并发写，只要它们的操作分别分布在不同的 Segment 上。这个值可以在初始化的时候设置为其他值，但是一旦初始化以后，它是不可以扩容的。再具体到每个 Segment 内部，其实每个 Segment 很像之前介绍的 HashMap，不过它要保证线程安全，所以处理起来要麻烦些。



##### java8 实现

​		Java8 对 ConcurrentHashMap 进行了比较大的改动,Java8 也引入了红黑树。通过对链表的头加锁实现。

![](D:\documents\notes\solid\Java.assets\4.jpg)



#### HashTable（线程安全）

​		Hashtable 是遗留类，很多映射的常用功能与 HashMap 类似，不同的是它承自 Dictionary 类，并且是线程安全的，任一时间只有一个线程能写 Hashtable，并发性不如 ConcurrentHashMap，因为 ConcurrentHashMap 引入了分段锁。Hashtable 不建议在新代码中使用，不需要线程安全的场合可以用 HashMap 替换，需要线程安全的场合可以用 ConcurrentHashMap 替换。



#### TreeMap（可排序）

​		TreeMap 实现 SortedMap 接口，能够把它保存的记录根据键排序，默认是按键值的升序排序，也可以指定排序的比较器，当用 Iterator 遍历 TreeMap 时，得到的记录是排过序的。如果使用排序的映射，建议使用 TreeMap。在使用 TreeMap 时，key 必须实现 Comparable 接口或者在构造 TreeMap 传入自定义的。Comparator，否则会在运行时抛出 java.lang.ClassCastException 类型的异常。



#### LinkHashMap（记录插入顺序）

​		LinkedHashMap 是 HashMap 的一个子类，保存了记录的插入顺序，在用 Iterator 遍历。LinkedHashMap 时，先得到的记录肯定是先插入的，也可以在构造时带参数，按照访问次序排序。





#### 键值对

```java
Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>("key", "value")
```



#### 固定大小，删除旧数据 Map

```java
import java.util.LinkedHashMap;
import java.util.Map;
 
public class FixedSizeOrderMap<K, V> extends LinkedHashMap<K, V> {
    private int maxSize;

    public FixedSizeOrderMap(int maxSize) {
        super(16, 0.75f, true); // 初始容量16，加载因子0.75，访问顺序
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}

public class FixedSizeMapExample {
    public static void main(String[] args) {
        FixedSizeOrderMap<Integer, Integer> fixedSizeMap = new FixedSizeOrderMap<>(5);
 
        // 添加数据
        fixedSizeMap.put(1, 1);
        fixedSizeMap.put(2, 2);
        fixedSizeMap.put(3, 3);
        fixedSizeMap.put(4, 4);
        fixedSizeMap.put(5, 5);
        fixedSizeMap.put(6, 6); // 这时会删除键为1的数据
 
        // 输出当前Map内容
        for (Map.Entry<Integer, Integer> entry : fixedSizeMap.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }
}
```

### 集合操作

#### List 转 Map

```java
public Map<Long, String> getIdNameMap(List<User> users) {
    return accounts.stream().collect(Collectors.toMap(User::getId, User::getUserName));
}
//第二种：将id和实体Bean做为K,V account -> account是一个返回本身的lambda表达式，后面的使用Function接口中的一个默认方法代替，使整个方法更简洁优雅。
public Map<Long, Account> getIdAccountMap(List<Account> accounts) {
    return accounts.stream().collect(Collectors.toMap(Account::getId, account -> account));
}
public Map<Long, Account> getIdAccountMap(List<Account> accounts) {
    return accounts.stream().collect(Collectors.toMap(Account::getId, Function.identity()));
}
//第三种： key存在重复记录时处理,如果使用第一种方法会出错，所以这里只是简单的使用后者覆盖前者来解决key重复问题
public Map<String, Account> getNameAccountMap(List<Account> accounts) {
    return accounts.stream().collect(Collectors.toMap(Account::getUsername, Function.identity(), (key1, key2) -> key2));
}
//第四种： 使用某个具体的Map类来保存，如保存时使用LinkedHashMap
public Map<String, Account> getNameAccountMap(List<Account> accounts) {
    return accounts.stream().collect(Collectors.toMap(Account::getUsername, Function.identity(), (key1, key2) -> key2, LinkedHashMap::new));
}
```



#### List 转 Map 当value值为空时会报空指针异常解决方案

```java
//解决方案一，使用Optional类处理null
public Map<String, String> getNameAccountMap(List<Account> accounts) {
    return accounts.stream().collect(Collectors.toMap(s -> Optional.ofNullable(s.getUsername()).orElse(null), s -> Optional.ofNullable(s.getPassword()).orElse("unknown"), (key1, key2) -> key2, HashMap::new));
}

//解决方案二，直接使用collect()方法进行规约操作
public Map<String, String> getNameAccountMap(List<Account> accounts) {
    return accounts.stream().collect(HashMap::new, (map, item) -> map.put(item.getUsername(), item.getPassword()), HashMap::putAll);
}
```



#### 深层次的集合类型取值

```java
    /**
     * @description: 深层次的集合类型取值,根据keyList获取多层集合的指定值
     * @param: listOrMap
     * @param: keys
     * @return: java.lang.Object
     * @throws 
     * @author yang xiong
     * @date 2024/11/28 14:26
     **/
    public static Object getValue(Object listOrMap, Object ...keys){
        if(null == listOrMap || null == keys || keys.length <= 0){
            return null;
        } else{
            return getValue(listOrMap, 0, keys);
        }
    }

    /**
     * @description: 深层次的集合类型取值,根据keyList获取多层集合的指定值
     * @param: listOrMap
     * @param: keyIndex
     * @param: keys
     * @return: java.lang.Object
     * @throws
     * @author yang xiong
     * @date 2024/11/26 15:07
     **/
    private static Object getValue(Object listOrMap, int keyIndex, Object ...keys){
        if(null == listOrMap || keyIndex > keys.length){
            return null;
        } else if(keyIndex == keys.length){
            return listOrMap;
        }
        Object nextLevelCollection;
        if(listOrMap instanceof List && keys[keyIndex] instanceof Integer){
            int index = (Integer) keys[keyIndex];
            List list = (List)listOrMap;
            if(0 <= index && index < list.size()){
                nextLevelCollection = list.get(index);
            } else {
                nextLevelCollection = null;
            }
        } else if(listOrMap instanceof Map && keys[keyIndex] instanceof String){
            nextLevelCollection = ((Map)listOrMap).get(keys[keyIndex]);
        } else {
            nextLevelCollection = null;
        }
        return getValue(nextLevelCollection, keyIndex+1, keys);
    }
```



```java
public class JavaTest {
    public static void main(String[] args)  {
        Map<String, List<Map<String, List<Map<String, String>>>>> map = CollectionUtils.mapBuilder(10)
                        .put("level_1", CollectionUtils.listBuilder(10)
                                .add(CollectionUtils.mapBuilder(10)
                                        .put("level_3", CollectionUtils.listBuilder(10)
                                                .add((CollectionUtils.mapBuilder(10)
                                                        .put("level_5", "level_5_value")
                                                        .build())
                                                ).build()
                                        ).build()
                                ).build()
                        ).build();
        Object obj;
        obj = CollectionUtils.getValue(null, null); // null
        System.out.println(obj);
        obj = CollectionUtils.getValue(null, "level_1"); // null
        System.out.println(obj);
        obj = CollectionUtils.getValue(new HashSet<>(), "level_1"); // null
        System.out.println(obj);
        obj = CollectionUtils.getValue(map, null); // null
        System.out.println(obj);
        obj = CollectionUtils.getValue(map, new BigDecimal(0)); // null
        System.out.println(obj);
        obj = CollectionUtils.getValue(map); // null
        System.out.println(obj);
        obj = CollectionUtils.getValue(map,  0); // null
        System.out.println(obj);
        obj = CollectionUtils.getValue(map, "level_1"); // [{level_3=[{level_5=level_5_value}]}]
        System.out.println(obj);
        obj = CollectionUtils.getValue(map,  "level_1", 0, "level_3", 0, "level_5"); // level_5_value
        System.out.println(obj);
        obj = CollectionUtils.getValue(map,  "level_1", 0, "level_3", 0, "level_5", 0); // null
        System.out.println(obj);
        obj = CollectionUtils.getValue(map,  "level_1", 0, "level_3", 0, "level_5", "level_6"); // null
        System.out.println(obj);
    }
}
```



## 时间

#### 时间格式

```java
// yyyy-MM-dd HH:mm:ss
// yyyy-MM-dd HH:mm:ss.SSS
```



#### Date

##### 字符串转 Date

```java
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
Date date = sdf.parse("2022-09-01 12:00:00");
```



##### Date 格式化为字符串

```java
Date date = new Date();
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
String dateStr = sdf.format(date);
```



##### 时间戳互转

```java
// 获取13位毫秒时间戳
Date date = new Date();
long timestamp = date.getTime();
// 13位毫秒时间戳转 Date
Date date = new Date(timestamp);
```



#### LocalDate



##### 比较日期大小

```java
LocalDate startDateOfMonth = month.with(TemporalAdjusters.firstDayOfMonth());
LocalDate endDateOfMonth = month.with(TemporalAdjusters.lastDayOfMonth());
// startDateOfMonth > endDateOfMonth 结果大于 0
// startDateOfMonth = endDateOfMonth 结果等于 0
// startDateOfMonth < endDateOfMonth 结果小于 0
startDateOfMonth.compareTo(endDateOfMonth)
```



##### 获取当前一周的开始、结束日期

```java
LocalDate nowDate = LocalDate.now();
LocalDate startDateOfWeek = nowDate.minusDays(nowDate.getDayOfWeek().getValue() - 1);
LocalDate endDateOfWeek = nowDate.plusDays(7 - nowDate.getDayOfWeek().getValue());
```



##### 获取一个月的所有周的开始、结束日期

```java
public static List<PairValueVO<LocalDate, LocalDate>> weeksBoundOfMonth(LocalDate month){
    if(null == month){
        return Collections.emptyList();
    }
    List<PairValueVO<LocalDate, LocalDate>> weeksOfMonthList = new ArrayList<>(6);
    LocalDate startDateOfMonth = month.with(TemporalAdjusters.firstDayOfMonth());
    LocalDate endDateOfMonth = month.with(TemporalAdjusters.lastDayOfMonth());
    LocalDate startDateOfWeek = startDateOfMonth;
    int endDateOfWeekOffset = 7 - startDateOfMonth.getDayOfWeek().getValue();
    while (startDateOfWeek.compareTo(endDateOfMonth) <= 0){
        LocalDate endDateOfWeek = startDateOfWeek.plusDays(endDateOfWeekOffset);
        if(endDateOfWeek.compareTo(endDateOfMonth) > 0){
            endDateOfWeek = endDateOfMonth;
        }
        weeksOfMonthList.add(new PairValueVO(startDateOfWeek, endDateOfWeek));
        startDateOfWeek = endDateOfWeek.plusDays(1);
        endDateOfWeekOffset = 6;
    }
    return weeksOfMonthList;
}
```



##### 获取一个月间隔为7天的开始、结束日期

```java
public static List<PairValueVO<LocalDate, LocalDate>> weeksBoundOfMonth(LocalDate month){
    if(null == month){
        return Collections.emptyList();
    }
    List<PairValueVO<LocalDate, LocalDate>> weeksOfMonthList = new ArrayList<>(6);
    LocalDate startDateOfMonth = month.with(TemporalAdjusters.firstDayOfMonth());
    LocalDate endDateOfMonth = month.with(TemporalAdjusters.lastDayOfMonth());
    LocalDate startDate = startDateOfMonth;
    while (startDate.compareTo(endDateOfMonth) <= 0){
        LocalDate endDate = startDate.plusDays(6);
        if(endDate.compareTo(endDateOfMonth) > 0){
            endDate = endDateOfMonth;
        }
        weeksOfMonthList.add(new PairValueVO(startDate, endDate));
        startDate = endDate.plusDays(1);
    }
    return weeksOfMonthList;
}
```



##### 获取日期范围内的每天日期

```java
public static List<LocalDate> dates(LocalDate startDate, LocalDate endDate){
    if(null == startDate || null == endDate){
        return Collections.emptyList();
    }
    List<LocalDate> dateList = new ArrayList<>();
    LocalDate date = startDate;
    while (date.compareTo(endDate) <= 0){
        dateList.add(date);
        date = date.plusDays(1);
    }
    return dateList;
}
```



#### LocalDateTime



##### 比较时间大小

```java
LocalDateTime startTime = LocalDateTime.now().with(LocalTime.MIN);
LocalDateTime endTime = LocalDateTime.now().with(LocalTime.MAX);
// startTime > endTime 结果大于 0
// startTime = endTime 结果等于 0
// startTime < endTime 结果小于 0
startTime.compareTo(endTime)
```



##### 获取两个时间间隔数

```java
LocalDateTime start = LocalDateTime.of(2022, 5, 7, 1, 1);
LocalDateTime end = LocalDateTime.of(2022, 5, 8, 1, 1);
System.out.println("相差的天数: " + Duration.between(start, end).toDays() + "天");
System.out.println("相差的小时数: " + Duration.between(start, end).toHours() + "小时");
System.out.println("相差的分钟数: " + Duration.between(start, end).toMinutes() + "分钟");
System.out.println("相差的毫秒数: " + Duration.between(start, end).toMillis() + "毫秒");

```


##### 字符串转 LocalDateTime

```java
// 包含日期+时间
DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
LocalDateTime ldt = LocalDateTime.parse("2022-09-01 12:00:00", dtf);

//只包含日期
DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
LocalDateTime ldt = LocalDate.parse("2022-09-01", dtf).atStartOfDay();
```



##### LocalDateTime 格式化为字符串

```java
LocalDateTime ldt = LocalDateTime.now();
DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
String dateTimeStr = dtf.format(ldt);
```



##### 获取一天的开始、结束时间

```java
LocalDateTime now = LocalDateTime.now();
LocalDateTime startTime = now.with(LocalTime.MIN);
LocalDateTime endTime = now.with(LocalTime.MAX);
```



##### 获取一周的开始、结束时间

```java
LocalDateTime now = LocalDateTime.now();
LocalDateTime startTime = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
LocalDateTime endTime = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).with(LocalTime.MAX);
```



##### 获取指定日期是第几周

```java
LocalDateTime now = LocalDateTime.parse("2024-12-31 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

// 2024-12-31 00:00:00 输出53，跨年的周按上一年算
int week = now.get(WeekFields.ISO.weekOfYear());
// 2024-12-31 00:00:00 输出1，跨年的周按下一年算
int week = now.get(WeekFields.ISO.weekOfWeekBasedYear());
// 自定义周的周期，一个周的周期是从周五开始,一个周的最小天数是1
int week = now.get(WeekFields.of(DayOfWeek.FRIDAY,1));
```



##### 获取一个月的开始、结束时间

```java
LocalDateTime now = LocalDateTime.now();

LocalDateTime startTime = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);

LocalDateTime endTime = now.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);

System.out.println("一个月的开始时间："+ startTime + "， 一个月的结束时间："+endTime);
```



##### 设置日期/时间

```java
LocalDateTime now = LocalDateTime.now();
// 指定日期的年
System.out.println(now.withYear(2023));
// 指定日期的月 范围：1-12
System.out.println(now.withMonth(1));
// 指定日期是月中的第几天 范围：1 - 28/31
System.out.println(now.withDayOfMonth(1));
// 指定日期是年中的第几天 范围：1 - 365/366
System.out.println(now.withDayOfYear(1));
// 指定日期的小时 范围：0 - 23
System.out.println(now.withHour(0));
// 指定日期的分钟 范围：0 - 59
System.out.println(now.withMinute(0));
// 指定日期的秒 范围：0 - 59
System.out.println(now.withSecond(0));
// 指定日期中纳秒
System.out.println(now.withNano(0));
```



##### 时间戳互转

```java
LocalDateTime now = LocalDateTime.now();
// 秒-second(s)
System.out.println(now.atOffset(ZoneOffset.ofHours(8)).toEpochSecond());
// 毫秒-millisecond(ms)
System.out.println(now.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
// 微秒-microsecond(μs)
System.out.println(Long.valueOf(now.atOffset(ZoneOffset.ofHours(8)).toEpochSecond() + String.valueOf(now.getNano()).substring(0,6)));
// 纳秒-nanosecond(ns)
System.out.println(Long.valueOf(now.atOffset(ZoneOffset.ofHours(8)).toEpochSecond() + String.valueOf(now.getNano())));

// 13位毫秒时间戳转 LocalDateTime
Instant instant = Instant.ofEpochMilli(timestamp);
System.out.println(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
System.out.println(LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Shanghai")));
```





#### Date、LocalDateTime 互转

```java
// Date 转 LocalDateTime
// 方法一
Date date = new Date();
LocalDateTime ldt = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
//方法二
 Date date = new Date();
LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());

// LocalDateTime 转 Date
LocalDateTime ldt = LocalDateTime.now();
Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
```



#### LocalDateTime、LocalDate 互转

```java
// LocalDateTime 转 LocalDate
LocalDate nowDate = LocalDateTime.now().toLocalDate();

// LocalDate 转 LocalDateTime
LocalDate nowDate = LocalDate.now();
LocalDateTime ldt1 = LocalDateTime.of(nowDate, LocalTime.now());
LocalDateTime ldt2 = LocalDateTime.of(nowDate, LocalTime.MIN);
LocalDateTime ldt3 = LocalDateTime.of(nowDate, LocalTime.MAX);
LocalDateTime ldt4 = LocalDateTime.of(nowDate, LocalTime.of(8,20,33));
```



## 工具/组件

### 异常处理

[为什么不建议用 try catch 处理异常？](https://mp.weixin.qq.com/s/MmovlYs_E8ZZIpFEYSGhIA)

[为什么不建议用 try catch 处理异常？](../files/HTML/Java/SpringBoot/为什么不建议用 try catch 处理异常？.pdf)

#### 基本类

##### 断言接口

```java
/**
 * <b><code>Assert</code></b>
 * <p/>
 * 异常断言接口
 * <p/>
 * <b>Creation Time:</b> 2022/8/24 上午11:37.
 *
 * @author yang xiong
 */
public interface IAssert {

    /**
     * @description: 断言对象为 not null，如果对象为 null，则抛出异常
     * @param obj 待判断对象
     * @return
     * @throws
     * @author yang xiong
     * @date 2022/8/24 上午11:55
     */
    void assertNotNull(Object obj) throws BaseException;

    /**
     * @description:
     * 断言对象为 not null，如果对象为 null，则抛出异常
     * 异常信息支持传递参数方式，避免在判断之前进行字符串拼接操作
     *
     * @param obj 待判断对象
     * @param args message占位符对应的参数列表
     * @return
     * @throws
     * @author yang xiong
     * @date 2022/8/24 上午11:56
     */
    void assertNotNull(Object obj, Object... args) throws BaseException;

    /**
     * @description: 断言对象为 null，如果对象为 not null，则抛出异常
     * @param obj 待判断对象
     * @return
     * @throws
     * @author yang xiong
     * @date 2022/8/24 上午11:55
     */
    void assertNull(Object obj) throws BaseException;

    /**
     * @description:
     * 断言对象为 null，如果对象为 not null，则抛出异常
     * 异常信息支持传递参数方式，避免在判断之前进行字符串拼接操作
     *
     * @param obj 待判断对象
     * @param args message占位符对应的参数列表
     * @return
     * @throws
     * @author yang xiong
     * @date 2022/8/24 上午11:56
     */
    void assertNull(Object obj, Object... args) throws BaseException;

    /**
     * @description: 断言对象为 false，如果对象为空或 true，则抛出异常
     * @param obj 待判断对象
     * @return
     * @throws
     * @author yang xiong
     * @date 2022/8/24 上午11:56
     */
    void assertFalse(Boolean obj) throws BaseException;

    /**
     * @description:
     * 断言对象为 false，如果对象为空或 true，则抛出异常
     * 异常信息支持传递参数方式，避免在判断之前进行字符串拼接操作
     * @param obj 待判断对象
     * @param args message占位符对应的参数列表
     * @return
     * @throws
     * @author yang xiong
     * @date 2022/8/24 上午11:59
     */
    void assertFalse(Boolean obj, Object... args) throws BaseException;

    /**
     * @description: 断言对象为 true，如果对象为空或 false，则抛出异常
     * @param obj 待判断对象
     * @return
     * @throws
     * @author yang xiong
     * @date 2022/8/24 上午11:56
     */
    void assertTrue(Boolean obj) throws BaseException;

    /**
     * @description:
     * 断言对象为 true，如果对象为空或 false，则抛出异常
     * 异常信息支持传递参数方式，避免在判断之前进行字符串拼接操作
     * @param obj 待判断对象
     * @param args message占位符对应的参数列表
     * @return
     * @throws
     * @author yang xiong
     * @date 2022/8/24 上午11:59
     */
    void assertTrue(Boolean obj, Object... args) throws BaseException;

    /**
     * @description: 断言 Collection 对象为 not null 且 not empty ，如果对象为 null 或 empty，则抛出异常
     * @param collection 待判断 Collection 对象
     * @return
     * @throws
     * @author yang xiong
     * @date 2022/8/24 上午11:56
     */
    void assertNotEmpty(Collection<?> collection) throws BaseException;

    /**
     * @description:
     * 断言 Collection 对象为 not null 且 not empty ，如果对象为 null 或 empty，则抛出异常
     * 异常信息支持传递参数方式，避免在判断之前进行字符串拼接操作
     * @param collection 待判断 Collection 对象
     * @param args message占位符对应的参数列表
     * @return
     * @throws
     * @author yang xiong
     * @date 2022/8/24 上午11:59
     */
    void assertNotEmpty(Collection<?> collection, Object... args) throws BaseException;

    /**
     * @description: 断言 Collection 对象为 null 或 empty，如果对象为 not null 且 not empty，则抛出异常
     * @param collection 待判断 Collection 对象
     * @return
     * @throws
     * @author yang xiong
     * @date 2022/8/24 上午11:56
     */
    void assertEmpty(Collection<?> collection) throws BaseException;

    /**
     * @description:
     * 断言 Collection 对象为 null 或 empty，如果对象为 not null 且 not empty，则抛出异常
     * 异常信息支持传递参数方式，避免在判断之前进行字符串拼接操作
     * @param collection 待判断 Collection 对象
     * @param args message占位符对应的参数列表
     * @return
     * @throws
     * @author yang xiong
     * @date 2022/8/24 上午11:59
     */
    void assertEmpty(Collection<?> collection, Object... args) throws BaseException;
}
```



##### 创建异常接口

```java
/**
 * <b><code>IBaseException</code></b>
 * <p/>
 * 创建异常接口
 * <p/>
 * <b>Creation Time:</b> 2022/8/24 上午11:53.
 *
 * @author yang xiong
 */
public interface IExceptionFactory {
    /**
     * @description: 创建异常
     * @param args
     * @return {@link BaseException}
     * @throws
     * @author yang xiong
     * @date 2022/8/24 上午11:38
     */
    BaseException newException(Object... args);

    /**
     * @description: 创建异常
     * @param t
     * @param args
     * @return {@link BaseException}
     * @throws
     * @author yang xiong
     * @date 2022/8/24 上午11:38
     */
    BaseException newException(Throwable t, Object... args);
}
```



##### 断言接口默认实现类

```java
/**
 * <b><code>AbstractAssert</code></b>
 * <p/>
 * 异常断言接口默认实现类， 由于枚举不能继承，所以不能使用抽象类
 * <p/>
 * <b>Creation Time:</b> 2022/8/24 上午11:51.
 *
 * @author yang xiong
 */
public interface IBaseAssert extends IExceptionFactory, IAssert {
    @Override
    default void assertNotNull(Object obj) throws BaseException {
        if (obj == null) {
            throw newException(obj);
        }
    }

    @Override
    default void assertNotNull(Object obj, Object... args) throws BaseException {
        if (obj == null) {
            throw newException(args);
        }
    }
    @Override
    default void assertNull(Object obj) throws BaseException {
        if (obj != null) {
            throw newException(obj);
        }
    }

    @Override
    default void assertNull(Object obj, Object... args) throws BaseException {
        if (obj != null) {
            throw newException(args);
        }
    }

    @Override
    default void assertFalse(Boolean obj) throws BaseException {
        if (!Boolean.FALSE.equals(obj)) {
            throw newException(obj);
        }
    }

    @Override
    default void assertFalse(Boolean obj, Object... args) throws BaseException {
        if (!Boolean.FALSE.equals(obj)) {
            throw newException(args);
        }
    }

    @Override
    default void assertTrue(Boolean obj) throws BaseException {
        if (!Boolean.TRUE.equals(obj)) {
            throw newException(obj);
        }
    }

    @Override
    default void assertTrue(Boolean obj, Object... args) throws BaseException {
        if (!Boolean.TRUE.equals(obj)) {
            throw newException(args);
        }
    }

    @Override
    default void assertNotEmpty(Collection<?> collection) throws BaseException {
        if (null == collection || collection.isEmpty()) {
            throw newException(collection);
        }
    }

    @Override
    default void assertNotEmpty(Collection<?> collection, Object... args) throws BaseException {
        if (null == collection || collection.isEmpty()) {
            throw newException(args);
        }
    }

    @Override
    default void assertEmpty(Collection<?> collection) throws BaseException {
        if (null != collection && !collection.isEmpty()) {
            throw newException(collection);
        }
    }

    @Override
    default void assertEmpty(Collection<?> collection, Object... args) throws BaseException {
        if (null != collection && !collection.isEmpty()) {
            throw newException(args);
        }
    }
}
```



##### 响应枚举值接口

```java
/**
 * <b><code>IResponseEnum</code></b>
 * <p/>
 * 响应枚举值接口
 * <p/>
 * <b>Creation Time:</b> 2022/8/24 下午12:05.
 *
 * @author yang xiong
 */
public interface IExceptionAssertResponseEnum {
    int getCode();
    String getMessage();
}

```



##### 基础异常类

```java
/**
 * <b><code>BaseException</code></b>
 * <p/>
 * 基础异常类
 * <p/>
 * <b>Creation Time:</b> 2022/8/24 下午12:31.
 *
 * @author yang xiong
 */

public abstract class BaseException extends Exception {
    private IExceptionAssertResponseEnum exceptionAssertResponseEnum;
    private Object[] args;
    private String message;
    private Throwable cause;

    public BaseException(IExceptionAssertResponseEnum exceptionAssertResponseEnum, Object[] args, String message){
        this.exceptionAssertResponseEnum = exceptionAssertResponseEnum;
        this.args = args;
        this.message = message;
    }

    public BaseException(IExceptionAssertResponseEnum exceptionAssertResponseEnum, Object[] args, String message, Throwable cause) {
        this.exceptionAssertResponseEnum = exceptionAssertResponseEnum;
        this.args = args;
        this.message = message;
        this.cause = cause;
    }

    public IExceptionAssertResponseEnum getExceptionAssertResponseEnum() {
        return exceptionAssertResponseEnum;
    }

    public Object[] getArgs() {
        return args;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }
}
```



#### 具体业务异常处理



##### 创建异常接口实现类

```java
/**
 * <b><code>IBusinessExceptionFactory</code></b>
 * <p/>
 * 创建异常接口实现类，由于枚举不能继承，所以不能使用抽象类
 * <p/>
 * <b>Creation Time:</b> 2022/8/24 下午12:09.
 *
 * @author yang xiong
 */
public interface IBusinessExceptionFactory extends IExceptionAssertResponseEnum, IBaseAssert {
    @Override
    default BaseException newException(Object... args) {
        String msg = MessageFormat.format(this.getMessage(), args);

        return new BaseBusinessException(this, args, msg);
    }

    @Override
    default BaseException newException(Throwable t, Object... args) {
        String msg = MessageFormat.format(this.getMessage(), args);

        return new BaseBusinessException(this, args, msg, t);
    }
}
```



##### 业务异常类

```java
/**
 * <b><code>BusinessException</code></b>
 * <p/>
 * 业务异常
 * <p/>
 * <b>Creation Time:</b> 2022/8/24 下午12:34.
 *
 * @author yang xiong
 */
public class BusinessException extends BaseException {

    private static final long serialVersionUID = 1L;

    public BusinessException(IExceptionAssertResponseEnum exceptionAssertResponseEnum, Object[] args, String message) {
        super(exceptionAssertResponseEnum, args, message);
    }

    public BusinessException(IExceptionAssertResponseEnum exceptionAssertResponseEnum, Object[] args, String message, Throwable cause) {
        super(exceptionAssertResponseEnum, args, message, cause);
    }
}
```



##### 业务异常断言响应枚举

```java
/**
 * <b><code>BusinessExceptionAssertResponseEnum</code></b>
 * <p/>
 * 业务异常断言响应枚举
 * <p/>
 * <b>Creation Time:</b> 2022/8/24 下午12:19.
 *
 * @author yang xiong
 */
public enum BusinessExceptionAssertResponseEnum implements IBusinessExceptionFactory {

    /**
     * 通用异常
     */
    THROW_EXCEPTION(100001, "{0}"),

    /**
     * 未找到文件
     */
    FILE_NOT_FOUND(200001, "未找到文件:{0}."),

    /**
     * 未找到文件
     */
    FILE_NOT_FOUND_IN_DIR(200002, "在目录:{0},未找到文件:{1}."),

    /**
     * 返回码
     */
    private int code;

    /**
     * 返回消息
     */
    private String message;

    BusinessExceptionAssertResponseEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

```



#### SpringBoot 全局异常处理

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @description: 自定义异常类-业务异常
     * @param e
     * @return {@link ResponseResult< Integer>}
     * @throws
     * @author yang xiong
     * @date 2022/9/8 上午11:30
     */
    @ExceptionHandler({
            BaseException.class,
            BusinessException.class
    })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseResult<Integer> businessException(BaseException e) {
        log.error(e.getMessage(), e);
        return ResponseResult.failed(e.getExceptionAssertResponseEnum().getCode(),e.getMessage());
    }
}
```



#### 使用

```java
// response 为 null时抛出异常
BusinessExceptionAssertResponseEnum.NEW_BUSINESS_EXCEPTION.assertNotNull(response, "response 为 null");
```



### @FieldMark

#### FieldMark 注解

```java
/**
 * <b><code>FieldMark</code></b>
 * <p/>
 * 字段标记注解
 * <p/>
 * <b>Creation Time:</b> 2022/8/23 下午2:42.
 *
 * @author yang xiong
 * @since 
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface FieldMark {

    String[] types() default {};

    String alias() default "";
}

```



#### FieldMarkUtils

```java
/**
 * <b><code>FieldMarkUtils</code></b>
 * <p/>
 * 字段标记注解工具
 * <p/>
 * <b>Creation Time:</b> 2022/8/23 下午3:02.
 *
 * @author yang xiong
 * @since 
 */
@Slf4j
public class FieldMarkUtils {

    /**
     * @description: 获取注解FieldRemark上的types属性作为key,字段名Set作为value的map
     * @param clazz
     * @return {@link Map< String, Set< String>>}
     * @throws
     * @author yang xiong
     * @date 2022/9/21 上午9:05
     */
    public static Map<String, Set<String>> getMarkTypeFieldNameSetMap(Class<?> clazz){
        Map<String, Set<String>> columnMap = new HashMap<>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if(declaredField.isAnnotationPresent(FieldMark.class)) {
                FieldMark annotation = declaredField.getAnnotation(FieldMark.class);
                String[] types = annotation.types();
                if (null != types) {
                    String fieldName = declaredField.getName();
                    for (String dimension : types) {
                        columnMap.computeIfAbsent(dimension, k -> new HashSet<>()).add(fieldName);
                    }
                }
            }
        }
        return columnMap;
    }

    /**
     * @description: 获取注解FieldMark上的types属性作为key,字段作为value的map
     * @param clazz
     * @return {@link Map<String, Field>}
     * @throws
     * @author yang xiong
     * @date 2022/6/8 下午3:42
     */
    public static Map<String, Field> getTypeFieldMap(Class<?> clazz){
        Map<String, Field> fieldListMap = new HashMap<>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if(declaredField.isAnnotationPresent(FieldMark.class)) {
                FieldMark annotation = declaredField.getAnnotation(FieldMark.class);
                String[] types = annotation.types();
                if (null != types) {
                    for (String type: types) {
                        if(StringUtils.hasText(type)){
                            ReflectionUtils.makeAccessible(declaredField);
                            fieldListMap.put(type, declaredField);
                        }
                    }
                }
            }
        }
        return fieldListMap;
    }

    /**
     * @description: 获取注解FieldMark上的types属性作为key,字段List作为value的map
     * @param clazz
     * @return {@link Map<String, List<Field>>}
     * @throws
     * @author yang xiong
     * @date 2022/6/8 下午3:42
     */
    public static Map<String, List<Field>> getTypeFieldListMap(Class<?> clazz){
        Map<String, List<Field>> fieldListMap = new HashMap<>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if(declaredField.isAnnotationPresent(FieldMark.class)) {
                FieldMark annotation = declaredField.getAnnotation(FieldMark.class);
                String[] types = annotation.types();
                if (null != types) {
                    for (String type: types) {
                        if(StringUtils.hasText(type)){
                            ReflectionUtils.makeAccessible(declaredField);
                            fieldListMap.computeIfAbsent(type, k -> new ArrayList<>()).add(declaredField);
                        }
                    }
                }
            }
        }
        return fieldListMap;
    }
}

```



#### 使用

```java
public class Vo implements Serializable {
    @FieldMark(types = {"GAME", "WEB", "VR"})
    private String timeId;
}


private Map<String, Set<String>> typeFieldNameSetMap = FieldMarkUtils.getMarkTypeFieldNameSetMap(Vo.class);
```



### PageInfoUtils

```java
import com.github.pagehelper.PageInfo;

/**
 * <b><code>PageInfoUtils</code></b>
 * <p/>
 * 分页组件
 * <p/>
 * <b>Creation Time:</b> 2021/2/24 上午11:17.
 *
 * @author yang xiong
 * @version 1.0.0
 */
@Slf4j
public final class PageInfoUtils {
    private PageInfoUtils(){}

    /**
     * @description: 复制分页对象的属性
     * @param oldPage
     * @param list
     * @return {@link PageInfo<T>}
     * @throws
     * @author yang xiong
     * @date 2022/9/20 下午4:39
     */
    public static <T> PageInfo<T> clonePage(PageInfo oldPage,List<T> list){
        PageInfo<T> newPage = PageInfo.of(list);
        // 数据总数
        newPage.setTotal(oldPage.getTotal());
        // 当前页数
        newPage.setPageNum(oldPage.getPageNum());
        // 每页大小
        newPage.setPageSize(oldPage.getPageSize());
        // 当前页数据数
        newPage.setSize(oldPage.getSize());
        // 页总数
        newPage.setPages(oldPage.getPages());
        newPage.setPrePage(oldPage.getPrePage());
        newPage.setNextPage(oldPage.getNextPage());
        return newPage;
    }
    
    public static <T> PageInfo<T> emptyPage(PageInfo<T> page, int current, int size) {
        page.setList(Collections.emptyList());
        page.setTotal(0);
        page.setPageNum(current);
        page.setPageSize(size);
        page.setSize(0);
        page.setPages(0);
        return page;
    }
}
```



### FileUtils

```java
import org.apache.commons.io.file.SimplePathVisitor;

public final class FileUtils {
    /**
     * @description: 获取目录下符合条件的文件
     * @param path
     * @param filter
     * @return {@link File[]}
     * @throws
     * @author yang xiong
     * @date 2022/11/10 下午2:39
     */
    public static File[] listFiles(final String dir, final String filter){
        if(null == dir || null == filter){
            return null;
        }
        return new File(dir).listFiles((d, s) -> s.contains(filter));
    }
    
        /**
     * @description: 获取目录及子目录下所有文件和目录
     * @param dir
     * @return
     * @throws
     * @author yang xiong
     * @date 2022/11/10 下午2:46
     */
    public static void listFiles(String dir) throws IOException {
        Path path = Paths.get(dir);
        if(Files.exists(path)){
            Files.walkFileTree(path, new SimplePathVisitor() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                    Files.delete(file);
                    System.out.println("file: " + file.getParent().toString() + File.separator + file.getFileName().toString());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//                    Files.delete(dir);
                    System.out.println("dir: " + dir.getParent().toString() + File.separator + dir.getFileName().toString());
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
```





### DateTimeUtils

```java
    /**
     * @description: 从开始时间到结束时间一个小时执行一次 func
     * @param: startDateTime
     * @param: endDateTime
     * @param: func
     * @return: void
     * @throws
     * @author yang xiong
     * @date 2023/10/11 18:35
     **/
    private void traverseHour(LocalDateTime startDateTime, LocalDateTime endDateTime, Consumer<BigDecimal> func){
        LocalDateTime targetDateTime = startDateTime;
        while(targetDateTime.compareTo(endDateTime) <= 0){
            func.accept(DateUtils.getBigDecimalDate(targetDateTime, TimeGranularityEnum.H));
            targetDateTime = targetDateTime.plusHours(1);
        }
        // 如果小时相等要再执行一次
        if(targetDateTime.getHour() == endDateTime.getHour()){
            func.accept(DateUtils.getBigDecimalDate(targetDateTime, TimeGranularityEnum.H));
        }
    }
```





## 日志

[java日志组件介绍(common-logging,log4j,slf4j,logback)](../files/HTML/Java/SpringBoot/java日志组件介绍(common-logging,log4j,slf4j,logback).pdf)

### Logback 日志

#### 依赖

```xml
<!-- 使用 slf4j 接口 -->
<!-- <dependency> -->
<!--     <groupId>org.slf4j</groupId> -->
<!--     <artifactId>slf4j-api</artifactId> -->
<!--     <version>2.0.9</version> -->
<!--     <optional>true</optional> -->
<!-- </dependency> -->
<!-- 使用 commons-logging 接口 -->
<dependency>
    <groupId>commons-logging</groupId>
    <artifactId>commons-logging</artifactId>
    <version>1.2</version>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.4.11</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <scope>provided</scope>
    <optional>true</optional>
</dependency>
```



#### logback.xml 配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
    status : 这个用于设置log4j2自身内部的信息输出,可以不设置,当设置成trace时,会看到log4j2内部各种详细输出。
    因此我们直接设置成OFF
 -->
<Configuration status="OFF">

    <!-- -Dcommon.log.home=/var/logs/common -->
    <property name="logging.path" value="${common.log.home:-./logs}"/>

    <!-- %d{yyyy-MM-dd HH:mm:ss.SSS} 日志输出时间 -->
    <!-- %thread 输出日志的进程名字，这在Web应用以及异步任务处理中很有用 -->
    <!-- %-5level 日志级别，并且使用5个字符靠左对齐 -->
    <!-- %logger{50} 日志输出者的名字 -->
    <!-- %msg 日志消息 -->
    <!-- %n 平台的换行符 -->
    <property name="CONSOLE_LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{50} - %msg%n"/>
    <property name="FILE_LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{50} - %msg%n"/>
    <!--1. 输出到控制台-->
    <!--此日志appender是为开发使用，只配置最底级别-->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <!-- LevelFilter 过滤器，如果事件的级别等于配置的级别，则根据 onMatch 属性的配置接受或者拒绝该事件-->
        <!-- <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>INFO</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter> -->
        <!-- ThresholdFilter 过滤器，将拒绝级别低于阈值的事件，控制台输出的日志级别是大于或等于此级别的日志信息-->
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>DEBUG</level>
        </filter>
        <!--日志输出格式-->
        <encoder>
            <Pattern>${CONSOLE_LOG_PATTERN}</Pattern>
            <!-- 设置字符集 -->
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <!--2. 输出到文档-->
    <!-- 2.1 level为 DEBUG 日志，时间滚动输出 -->
    <appender name="FULL_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 正在记录的日志文档的路径及文档名 -->
        <file>${logging.path}/CommonJava-current.log</file>
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <!-- 日志记录器的滚动策略，按日期，按大小记录 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- 日志归档 -->
            <fileNamePattern>${logging.path}/CommonJava-%d{yyyy-MM-dd}.%i.log.zip</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>1GB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <!--日志文档保留天数-->
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>

    <logger name="!com.common.java" level="OFF"/>

    <!-- root节点是必选节点，用来指定最基础的日志输出级别 -->
    <!-- level:默认是DEBUG -->
    <!-- -Dcommon.log.level=INFO -->
    <root level="${common.log.level:-DEBUG}">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FULL_FILE"/>
    </root>

</Configuration>
```



#### 输出日志

```java
@Slf4j
public class LogTest {
    private static final Logger logger = LoggerFactory.getLogger(LogTest.class);

    @Test
    public void slf4jApiTest() {
        logger.info("Current Time:" + System.currentTimeMillis());
        logger.info("Current Time: {}", System.currentTimeMillis());
        logger.trace("trace log");
        logger.warn("warn log");
        logger.debug("debug log");
        logger.info("info log");
        logger.error("error log");
    }

    /**
     * 需要导入 lombok 包，以及使用 @Slf4j 注解
     **/
    @Test
    public void lombokLogTest() {
        log.info("Current Time:" + System.currentTimeMillis());
        log.info("Current Time: {}", System.currentTimeMillis());
        log.trace("trace log");
        log.warn("warn log");
        log.debug("debug log");
        log.info("info log");
        log.error("error log");
    }
}
```



## 反射

### 相关概念

#### 反射中，Class.forName 和 ClassLoader 区别

 Class.forName(className)方法，**在loadClass后必须初始化**。比较下我们前面准备jvm加载类的知识，我们可以清晰的看到在执行过此方法后，目标对象的 static块代码已经被执行，static参数也已经被初始化。

ClassLoader.loadClass(className)方法，该参数表示**目标对象被装载后不进行链接**，这就意味这**不会去执行该类静态块中间的内**容。因此2者的区别就显而易见了。



### 获取父类类型

```java
// 获取带泛型的父类
Object.class.getGenericSuperclass();
// 获取不带泛型的父类
Object.class.getSuperclass();
```



### 获取父类泛型

```java
Type[] types = ((ParameterizedType)Object.class.getGenericSuperclass()).getActualTypeArguments();
Class<?> poClazz = (Class)types[1];
```



## JDBC 数据类型与 Java 数据类型映射关系

例：`<result property="a" column="a" jdbcType="DATE"/>`



| Java Type                  | JDBC Type                   | 说明 |
| -------------------------- | --------------------------- | ---- |
| LocalDate                  | DATE                        |      |
| LocalTime                  | TIME                        |      |
| LocalDateTime              | TIMESTAMP                   |      |
| Date                       | DATE 或 TIME DATE TIMESTAMP |      |
| Array                      | ARRAY                       |      |
| boolean                    | BIT                         |      |
| byte                       | TINYINT                     |      |
| short                      | SMALLINT                    |      |
| Integer                    | INTEGER                     |      |
| long                       | BIGINT                      |      |
| double                     | FLOAT                       |      |
| float                      | REAL                        |      |
| double                     | DOUBLE                      |      |
| BigDecimal                 | NUMERIC                     |      |
| BigDecimal                 | DECIMAL                     |      |
| String                     | CHAR                        |      |
| String                     | VARCHAR                     |      |
| String                     | LONGVARCHAR                 |      |
| byte[]                     | BINARY                      |      |
| byte[]                     | VARBINARY                   |      |
| byte[]                     | LONGVARBINARY               |      |
|                            | NULL                        |      |
|                            | OTHER                       |      |
| Blob                       | BLOB                        |      |
| Clob                       | CLOB                        |      |
| boolean                    | BOOLEAN                     |      |
|                            | CURSOR                      |      |
|                            | UNDEFINED                   |      |
|                            | NVARCHAR                    |      |
|                            | NCHAR                       |      |
|                            | NCLOB                       |      |
| Struct                     | STRUCT                      |      |
|                            | JAVA_OBJECT                 |      |
| mapping of underlying type | DISTINCT                    |      |
| Ref                        | REF                         |      |
| URL                        | DATALINK                    |      |
|                            | ROWID                       |      |
|                            | LONGNVARCHAR                |      |
|                            | SQLXML                      |      |
|                            | DATETIMEOFFSET              |      |
|                            | TIME_WITH_TIMEZONE          |      |
|                            | TIMESTAMP_WITH_TIMEZONE     |      |



## euqals、hashCode

### equals

`==` 用于判断类型值是否相等的，比如 int,，float 等，也可以用来判断两个对象的引用地址是否相同（两个引用指向同一个对象）

`equals` 用于判断两个对象的值是否相等

`instanceof` 判断一个类是否是一个类或者该类子类的实例

`this.getClass() == obj.getClass();`比较目标和当前类必须为同一类的时候需要使用

```java
public class MyKey {
    private String name;
    private Boolean sex;
//    省略getter setter toString

//    注意：@Override注解必须加上否则IDE会帮我们检查是否重写了父类方法，否则可能实现的是重载方法。导致后面运行出错找不到问题的所在。
    @Override
    public boolean equals(Object obj){
//        自反性（reflective）：对于任何非null的引用值必须满足x.equals(x)必须为true
        if(this==obj){
            return true;
        }
//        对于任何非null的引用值x，x.equals(null)一定返回false
        if(null==obj){
            return false;
        }
//        对称性（symmetric）：对于任何非null的引用值x,y。当且仅当x.equals(y)为true的时候，y.equals(x)一定为true。
//        传递性（transitive）：对于任何非null的引用值x,y,z。当且仅当x.equals(y)为true的时候且y.equals(z)为true，x.equals(z)一定为true。

//        检查对象类型是否合法、否则后面类型转换可能会出错
        if(!(obj instanceof MyKey)){
            return false;
        }
        MyKey other=(MyKey)obj;
//        return ((this.name==other.name||null!=this.name&&this.name.equals(other.name))&&(this.sex==other.sex||null!=this.sex&&this.sex.equals(other.sex)));
        //使用jdk提供的工具类
        return ((Objects.equals(this.name, other.name))&&(Objects.equals(this.sex, other.sex)));
    }
}
```



### hashCode

hashCode 散列码计算（来自：Effective Java）

1. 把某个非零的常数值，比如 17，保存在一个名为 result 的 int 类型的变量中。
2. 对于对象中每个关键域`f`(指`equals`方法中涉及的每个域)，完成以下步骤：
   1. 为该域计算`int`类型的散列码c：
      1. 如果该域是`boolean`类型，则计算(`f?1:0`)。
      2. 如果该域是`byte`，`char`，`short`或者int类型，则计算`(int)f`。
      3. 如果该域是`long`类型，则计算`(int)(f^(f>>>32))`。
      4. 如果该域是`float`类型，则计算`Float.floatToIntBits(f)`。
      5. 如果该域是`double`类型，则计算`Double.doubleToLongBits(f)`，然后按照步骤 2.1.3，为得到的`long`类型值计算散列值。
      6. 如果该域是一个对象引用，并且该类的`equals`方法通过递归地调用`equals`的方式来比较这个域，则同样为这个域递归地调用`hashCode`。如果需要更复杂的比较，则为这个域计算一个范式`(canonical representation)`，然后针对这个范式调用`hashCode`。如果这个域的值为`null`，则返回`0`(其他常数也行)。
      7. 如果该域是一个数组，则要把每一个元素当做单独的域来处理。也就是说，递归地应用上述规则，对每个重要的元素计算一个散列码，然后根据步骤 2.2 中的做法把这些散列值组合起来。如果数组域中的每个元素都很重要，可以利用发行版本**1.5**中增加的其中一个`Arrays.hashCode`方法。
   2. 按照下面的公式，把步骤2.1中计算得到的散列码`c`合并到`result`中：`result = 31 * result + c`; //此处`31`是个奇素数，并且有个很好的特性，即用移位和减法来代替乘法，可以得到更好的性能：`31*i == (i<<5) - i， 现代JVM能自动完成此优化。
3. 返回`result`
4. 检验并测试该`hashCode`实现是否符合通用约定。

```java
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + mInt;
        result = 31 * result + (mBoolean ? 1 : 0);
        result = 31 * result + Float.floatToIntBits(mFloat);
        result = 31 * result + (int)(mLong ^ (mLong >>> 32));
        long mDoubleTemp = Double.doubleToLongBits(mDouble);
        result =31 * result + (int)(mDoubleTemp ^ (mDoubleTemp >>> 32));
        result = 31 * result + (mString == null ? 0 : mMsgContain.hashCode());
        result = 31 * result + (mObj == null ? 0 : mObj.hashCode());
        return result;
    }
```



## Jackson

```java
// json 字符串转 List
List<Obj> result = objectMapper.readValue(jsonStr, objectMapper.getTypeFactory().constructParametricType(List.class, Obj.class));
```







## 数据库

### 连接数据库

```java
package com.richstonedt.cmgde2eas.commons.util;

import java.sql.*;

/**
 * <b><code>JDBCUtils</code></b>
 * <p/>
 * <p>
 * <p/>
 * <b>Creation Time:</b> 2024/11/29 17:15
 *
 * @author yang xiong
 * @since cmgde2eas-sr-be 0.1.0
 */
public class JDBCUtils {

    private static final String url = "jdbc:oracle:thin:@192.168.108.57:1523:XE";
    private static final String user = "CMGDE2EAS";
    private static final String password = "CMGDE2EAS123!@#";
    private static final String driverClass = "oracle.jdbc.OracleDriver";

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName(driverClass);
        return DriverManager.getConnection(url, user, password);
    }

    public static void closeResource(Connection con, Statement sta, ResultSet rs){
        try {
            if(null != con){
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if(null != sta){
                sta.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if(null != rs){
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```



```java
    public String execSql(String sql) throws Exception {
        String result = "";
        Exception ex = null;
//            con = JDBCUtils.getConnection();
//            ps = con.prepareStatement(sql);
//            rs = ps.executeQuery();
        try(
                Connection con = dataSource.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
                ){
            log.info("exec sql: {}", sql);
            result = printRs(rs);
        } catch (Exception e){
            ex = e;
        } finally {
//            JDBCUtils.closeResource(con, ps, rs);
        }
        if(null != ex){
            throw ex;
        }
        return result;
    }

    private String printRs(ResultSet rs) throws SQLException {
        StringBuilder sb = new StringBuilder();
        // 获取列的数量
        int columns = rs.getMetaData().getColumnCount();
        // 打印列名
        for (int i = 1; i <= columns; i++) {
            sb.append(rs.getMetaData().getColumnName(i) + "\t");
        }
        System.out.println();
        // 打印数据
        while (rs.next()) {
            for (int i = 1; i <= columns; i++) {
                sb.append(rs.getObject(i) + "\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
```





# Spring

## 容器与 bean

### 容器接口







#### BeanFactory

```java
public interface BeanFactory {
    String FACTORY_BEAN_PREFIX = "&";
    Object getBean(String var1) throws BeansException;
    <T> T getBean(String var1, Class<T> var2) throws BeansException;
    Object getBean(String var1, Object... var2) throws BeansException;
    <T> T getBean(Class<T> var1) throws BeansException;
    <T> T getBean(Class<T> var1, Object... var2) throws BeansException;
    <T> ObjectProvider<T> getBeanProvider(Class<T> var1);
    <T> ObjectProvider<T> getBeanProvider(ResolvableType var1);
    boolean containsBean(String var1);
    boolean isSingleton(String var1) throws NoSuchBeanDefinitionException;
    boolean isPrototype(String var1) throws NoSuchBeanDefinitionException;
    boolean isTypeMatch(String var1, ResolvableType var2) throws NoSuchBeanDefinitionException;
    boolean isTypeMatch(String var1, Class<?> var2) throws NoSuchBeanDefinitionException;
    @Nullable
    Class<?> getType(String var1) throws NoSuchBeanDefinitionException;
    @Nullable
    Class<?> getType(String var1, boolean var2) throws NoSuchBeanDefinitionException;
    String[] getAliases(String var1);
}

```



- BeanFactory 是 Spring 的核心容器

- BeanFactory 是 ApplicationContext 的父接口


- 提供 getBean、isSingleton 等基本方法
- 它的实现类提供了控制反转、基本的依赖注入、直至 Bean 的生命周期的各种功能
- 它的成员变量 singletonObjects，内部包含了所有的单例 bean



通过反射获取 singletonObjects

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) throws IllegalAccessException, NoSuchFieldException {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        // 成员变量 singletonObjects 在 DefaultListableBeanFactory 的父类 DefaultSingletonBeanRegistry
        Field singletonObjects = DefaultSingletonBeanRegistry.class.getDeclaredField("singletonObjects");
        singletonObjects.setAccessible(true);
        ConfigurableBeanFactory beanFactory = context.getBeanFactory();
        Map<String, Object> map = (Map<String, Object>) singletonObjects.get(beanFactory);
        map.forEach((k,v)->{
            System.out.println(k + "=" + v);
        });
    }
}
```









##### 实现类

* beanFactory 可以通过 registerBeanDefinition 注册一个 bean definition 对象
  * 我们平时使用的配置类、xml、组件扫描等方式都是生成 bean definition 对象注册到 beanFactory 当中
  * bean definition 描述了这个 bean 的创建蓝图：scope 是什么、用构造还是工厂创建、初始化销毁方法是什么，等等
* beanFactory 需要手动调用 beanFactory 后处理器对它做增强
  * 例如通过解析 @Bean、@ComponentScan 等注解，来补充一些 bean definition
* beanFactory 需要手动添加 bean 后处理器，以便对后续 bean 的创建过程提供增强
  * 例如 @Autowired，@Resource 等注解的解析都是 bean 后处理器完成的
  * bean 后处理的添加顺序会对解析结果有影响，见视频中同时加 @Autowired，@Resource 的例子
* beanFactory 需要手动调用方法来初始化单例
* beanFactory 需要额外设置才能解析 ${} 与 #{}

###### DefaultListableBeanFactory

![image-20230201220656418](D:\documents\notes\solid\Java.assets\334.png)



```java
public class TestBeanFactory {

    public static void main(String[] args){
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        // 添加 bean 的定义(class, scoap, 初始化, 销毁)
        AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(Config.class)
                .setScope("singleton").getBeanDefinition();
        beanFactory.registerBeanDefinition("config" ,beanDefinition);

//        for(String name:beanFactory.getBeanDefinitionNames()){
//            System.out.println(name);
//        }

        // 此时还需要解析注解
        // 接下来给 BeanFactory 注册常用后处理器
        // 后处理器用于解析注解
        AnnotationConfigUtils.registerAnnotationConfigProcessors(beanFactory);

//        for(String name:beanFactory.getBeanDefinitionNames()){
//            System.out.println(name);
//        }

        // internalConfigurationAnnotationProcessor BeanFactory 后处理器，解析 @Configuration、@Bean 等注解的后处理器
        // internalAutowiredAnnotationProcessor Bean 后处理器，解析 @Autowire 注解
        // internalCommonAnnotationProcessor Bean 后处理器，解析 @Resource 等 JavaEE 注解
        // internalEventListenerProcessor
        // internalEventListenerFactory

        // BeanFactoryPostProcessor 为上面 BeanFactory 后处理器的共同父类
        beanFactory.getBeansOfType(BeanFactoryPostProcessor.class).values().forEach(beanFactoryPostProcessor -> {
            // 执行 BeanFactory 后处理器解析注解，并将他们加入 BeanFactory
            beanFactoryPostProcessor.postProcessBeanFactory(beanFactory);
        });
        // BeanPostProcessor 为上面 Bean 后处理器的共同父类
        beanFactory.getBeansOfType(BeanPostProcessor.class).values().forEach(beanPostProcessor -> {
            // 执行 Bean 后处理器解析注解，并将他们加入 BeanFactory
            beanFactory.addBeanPostProcessor(beanPostProcessor);
        });

        for(String name:beanFactory.getBeanDefinitionNames()){
            System.out.println(name);
        }

        // 配置 BeanFactory 提前创建 Bean，不要懒加载 Bean
        beanFactory.preInstantiateSingletons();
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        // null
        System.out.println(beanFactory.getBean(Bean1.class).getBean2());

        /**
         * BeanFactory 不会做的事
         *      1.不会主动调用 BeanFactory 后处理器
         *      2.不会主动添加 Bean 后处理器
         *      3.不会主动初始化单例
         *      4.不会解析 BeanFactory 还不会解析 ${} 与 #{}
         * Bean 后处理器会有排序的逻辑
         */

    }

    @Configuration
    static class Config{
        @Bean
        public Bean1 bean1(){
            return new Bean1();
        }

        @Bean
        public Bean2 bean2(){
            return new Bean2();
        }
    }

    static class Bean1{
        private static final Logger log = LoggerFactory.getLogger(Bean1.class);

        @Autowired
        private Bean2 bean2;

        public Bean1() {
            log.debug("构造 Bean1()");
        }

        public Bean2 getBean2(){
            return bean2;
        }
    }

    static class Bean2{
        private static final Logger log = LoggerFactory.getLogger(Bean2.class);

        public Bean2() {
            log.debug("构造 Bean2()");
        }
    }
}
```



###### 后处理器的顺序

1. 实现了 PriorityOrdered 接口的优先级最高
2. 实现了 Ordered 接口与加了 @Order 注解的平级, 按数字升序
3. 其它的排在最后

```java
public class TestBeanFactory {

    public static void main(String[] args){
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(Config.class)
                .setScope("singleton").getBeanDefinition();
        beanFactory.registerBeanDefinition("config" ,beanDefinition);

        AnnotationConfigUtils.registerAnnotationConfigProcessors(beanFactory);

        beanFactory.getBeansOfType(BeanFactoryPostProcessor.class).values().forEach(beanFactoryPostProcessor -> {
            beanFactoryPostProcessor.postProcessBeanFactory(beanFactory);
        });
        beanFactory.getBeansOfType(BeanPostProcessor.class).values().forEach(beanPostProcessor -> {
            beanFactory.addBeanPostProcessor(beanPostProcessor);
        });

        for(String name:beanFactory.getBeanDefinitionNames()){
            System.out.println(name);
        }

        System.out.println(beanFactory.getBean(Bean1.class).getInter());
    }

    @Configuration
    static class Config{
        @Bean
        public Bean1 bean1(){
            return new Bean1();
        }

        @Bean
        public Bean2 bean2(){
            return new Bean2();
        }

        @Bean
        public Bean3 bean3(){
            return new Bean3();
        }
    }

    interface Inter{}

    static class Bean1{
        private static final Logger log = LoggerFactory.getLogger(Bean1.class);

        // @Autowired 先根据类型匹配，再根据名字匹配
        @Autowired
        @Resource(name = "bean3")
        private Inter bean2;

        public Bean1() {
            log.debug("构造 Bean1()");
        }

        public Inter getInter(){
            return bean2;
        }
    }

    static class Bean2 implements Inter{
        private static final Logger log = LoggerFactory.getLogger(Bean2.class);

        public Bean2() {
            log.debug("构造 Bean2()");
        }
    }

    static class Bean3 implements Inter{
        private static final Logger log = LoggerFactory.getLogger(Bean3.class);

        public Bean3() {
            log.debug("构造 Bean3()");
        }
    }
}

```



在方法 `AnnotationConfigUtils.registerAnnotationConfigProcessors(beanFactory);` 中设置了比较器 `beanFactory.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE);`

在后处理器中会有`getOrder()`方法来返回后处理器的排序值

看一下源码中的 order 可以看到 Autowired 的 order 比 Common 中的 order 大

```java
public class CommonAnnotationBeanPostProcessor
    public CommonAnnotationBeanPostProcessor() {
        this.setOrder(2147483644);
        ...
    }
}
```



```java
public class AutowiredAnnotationBeanPostProcessor {
    ...
    private int order = 2147483645;
    ...
}
```



#### ApplicationContext

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        /**
         * ConfigurableApplicationContext 继承了 ApplicationContext 
         */
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
    }

}
```

![image-20230201220656417](D:\documents\notes\solid\Java.assets\333.png)





##### 组合了 BeanFactory 

```java
public Object getBean(String name) throws BeansException {
    this.assertBeanFactoryActive();
    return this.getBeanFactory().getBean(name);
}
```



##### 扩展了 BeanFactory 

- 国际化（MessageSource）
- 通配符方式获取一组 Resource 资源（ResourcePatternResolver）
- 事件发布与监听（ApplicationEventPublisher）
- 整合 Environment 环境（EnvironmentCapable）



###### MessageSource

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        System.out.println(context.getMessage("hi", null, Locale.CHINA));
        System.out.println(context.getMessage("hi", null, Locale.ENGLISH));
        System.out.println(context.getMessage("hi", null, Locale.JAPANESE));
    }

}
```



messages.properties

```properties

```

messages_zh.properties

```properties
hi=你好
```

messages_en.properties

```properties
hi=Hello
```

messages_ja.properties

```properties
hi=こんにちは
```



###### ResourcePatternResolver

获取资源

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        Resource[] resources = context.getResources("classpath:application*.properties");
        for(Resource resource:resources){
            System.out.println(resource);
        }
    }

}
```



###### ApplicationEventPublisher

可以用于解耦

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        context.publishEvent(new UserRegisteredEvent(context));
    }

}
```

```java
/**
 * 事件发布器
 */
public class UserRegisteredEvent extends ApplicationEvent {
    /**
     * 时间发送者
     */
    public UserRegisteredEvent(Object source) {
        super(source);
    }
}

```

```java
/**
 * 事件监听器
 */
@Component
public class Component1 {

    @EventListener
    public void listener (UserRegisteredEvent event){
        System.out.println(event);
    }
}

```



###### EnvironmentCapable

获取环境变量、配置信息

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        System.out.println(context.getEnvironment().getProperty("java_home"));
        System.out.println(context.getEnvironment().getProperty("server.port"));
    }

}
```



##### 实现类



Spring 的发展历史较为悠久，因此很多资料还在讲解它较旧的实现，这里出于怀旧的原因，把它们都列出来，供大家参考

* DefaultListableBeanFactory，是 BeanFactory 最重要的实现，像**控制反转**和**依赖注入**功能，都是它来实现
* ClassPathXmlApplicationContext，从类路径查找 XML 配置文件，创建容器（旧）
* FileSystemXmlApplicationContext，从磁盘路径查找 XML 配置文件，创建容器（旧）
* XmlWebApplicationContext，传统 SSM 整合时，基于 XML 配置文件的容器（旧）
* AnnotationConfigWebApplicationContext，传统 SSM 整合时，基于 java 配置类的容器（旧）
* AnnotationConfigApplicationContext，Spring boot 中非 web 环境容器（新）
* AnnotationConfigServletWebServerApplicationContext，Spring boot 中 servlet web 环境容器（新）
* AnnotationConfigReactiveWebServerApplicationContext，Spring boot 中 reactive web 环境容器（新）

另外要注意的是，后面这些带有 ApplicationContext 的类都是 ApplicationContext 接口的实现，但它们是**组合**了 DefaultListableBeanFactory 的功能，并非继承而来



###### ClassPathXmlApplicationContext

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        classPathXmlApplicationContext();

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        // 读取配置并将 bean 存入 beanFactory
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        reader.loadBeanDefinitions(new ClassPathResource("test/classPathXml.xml"));
        for(String name:beanFactory.getBeanDefinitionNames()){
            System.out.println(name);
        }
    }

    /**
     * 较为经典的容器，基于 classpath 下 xml 格式的配置文件来创建
     */
    private static void classPathXmlApplicationContext(){
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("test/classPathXml.xml");
        for(String name:context.getBeanDefinitionNames()){
            System.out.println(name);
        }
        System.out.println(context.getBean(Bean2.class).getBean1());
    }
}
```



###### FileSystemXmlApplicationContext

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        fileSystemXmlApplicationContext();

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        // 读取配置并将 bean 存入 beanFactory
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        reader.loadBeanDefinitions(new FileSystemResource("src\\main\\resources\\test\\classPathXml.xml"));
        for(String name:beanFactory.getBeanDefinitionNames()){
            System.out.println(name);
        }
    }

    /**
     * 基于磁盘路径下 xml 格式的配置文件来创建
     */
    private static void fileSystemXmlApplicationContext(){
        // 相对目录 src\main\resources\test\classPathXml.xml
        FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext("D:\\develop\\code\\java\\uums\\src\\main\\resources\\test\\classPathXml.xml");
        for(String name:context.getBeanDefinitionNames()){
            System.out.println(name);
        }
        System.out.println(context.getBean(Bean2.class).getBean1());
    }
}
```



###### AnnotationConfigApplicationContext

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        annotationConfigApplicationContext();
    }
 
        /**
     * 较为经典的容器，基于 java 配置类来创建
     */
    private static void annotationConfigApplicationContext(){
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
        for(String name:context.getBeanDefinitionNames()){
            System.out.println(name);
        }
        System.out.println(context.getBean(Bean2.class).getBean1());
    }
}
```



```java
@Configuration
public class Config {
    @Bean
    public Bean1 bean1(){
        return new Bean1();
    }

    @Bean
    public Bean2 bean2(Bean1 bean1){
        Bean2 bean2 = new Bean2();
        bean2.setBean1(bean1);
        return bean2;
    }
}
```



```java
public class Bean1 {
}
```



```java
public class Bean2 {
    private Bean1 bean1;

    public Bean1 getBean1() {
        return bean1;
    }

    public void setBean1(Bean1 bean1) {
        this.bean1 = bean1;
    }
}
```



###### AnnotationConfigServletWebServerApplicationContext

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        annotationConfigServletWebServerApplicationContext();
    }
    
    /**
     * 较为经典的容器，基于 java 配置类来创建，用于 web 环境
     */
    private static void annotationConfigServletWebServerApplicationContext(){
        AnnotationConfigServletWebServerApplicationContext context = new AnnotationConfigServletWebServerApplicationContext(WebConfig.class);
        for(String name:context.getBeanDefinitionNames()){
            System.out.println(name);
        }
//      http://localhost:8080/hello
    }
}
```



```java
@Configuration
public class WebConfig {
    @Bean
    public ServletWebServerFactory servletWebServerFactory(){
        return new TomcatServletWebServerFactory();
    }

    @Bean
    public DispatcherServlet dispatcherServlet(){
        return new DispatcherServlet();
    }

    /**
     * 注册 servlet 到 tomcat 服务器
     */
    @Bean
    public DispatcherServletRegistrationBean dispatcherServletRegistrationBean(DispatcherServlet dispatcherServlet){
        return new DispatcherServletRegistrationBean(dispatcherServlet, "/");
    }

    /**
     * 控制器
     */
    @Bean("/hello")
    public Controller controller(){
        return (httpServletRequest, httpServletResponse) -> {
            httpServletResponse.getWriter().println("hello world");
            return null;
        };
    }
}
```



#### 事件解耦





### Bean 的生命周期

一个受 Spring 管理的 bean，生命周期主要阶段有

1. 创建：根据 bean 的构造方法或者工厂方法来创建 bean 实例对象
2. 依赖注入：根据 @Autowired，@Value 或其它一些手段，为 bean 的成员变量填充值、建立关系
3. 初始化：回调各种 Aware 接口，调用对象的各种初始化方法
4. 销毁：在容器关闭时，会销毁所有单例对象（即调用它们的销毁方法）
   * prototype 对象也能够销毁，不过需要容器这边主动调用

一些资料会提到，生命周期中还有一类 bean 后处理器：BeanPostProcessor，会在 bean 的初始化的前后，提供一些扩展逻辑。但这种说法是不完整的，见下面的演示



```mermaid
graph LR

创建 --> 依赖注入
依赖注入 --> 初始化
初始化 --> 可用
可用 --> 销毁
```

创建前后的增强

* postProcessBeforeInstantiation
  * 这里返回的对象若不为 null 会替换掉原本的 bean，并且仅会走 postProcessAfterInitialization 流程
* postProcessAfterInstantiation
  * 这里如果返回 false 会跳过依赖注入阶段

依赖注入前的增强

* postProcessProperties
  * 如 @Autowired、@Value、@Resource 

初始化前后的增强

* postProcessBeforeInitialization
  * 这里返回的对象会替换掉原本的 bean
  * 如 @PostConstruct、@ConfigurationProperties
* postProcessAfterInitialization 
  * 这里返回的对象会替换掉原本的 bean
  * 如代理增强

销毁之前的增强

* postProcessBeforeDestruction
  * 如 @PreDestroy 



1. Spring bean 生命周期各个阶段
2. 模板设计模式, 指大流程已经固定好了, 通过接口回调（bean 后处理器）在一些关键点前后提供扩展



#### MyBeanPostProcessor

```java
@Component
public class MyBeanPostProcessor implements InstantiationAwareBeanPostProcessor, DestructionAwareBeanPostProcessor {

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if (beanName.equals("lifeCycleBean"))
            System.out.println("<<<<<< 销毁之前执行, 如 @PreDestroy");
    }

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        if (beanName.equals("lifeCycleBean"))
            System.out.println("<<<<<< 实例化之前执行, 这里返回的对象会替换掉原本的 bean");
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        if (beanName.equals("lifeCycleBean")) {
            System.out.println("<<<<<< 实例化之后执行, 这里如果返回 false 会跳过依赖注入阶段");
//            return false;
        }
        return true;
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        if (beanName.equals("lifeCycleBean"))
            System.out.println("<<<<<< 依赖注入阶段执行, 如 @Autowired、@Value、@Resource");
        return pvs;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.equals("lifeCycleBean"))
            System.out.println("<<<<<< 初始化之前执行, 这里返回的对象会替换掉原本的 bean, 如 @PostConstruct、@ConfigurationProperties");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.equals("lifeCycleBean"))
            System.out.println("<<<<<< 初始化之后执行, 这里返回的对象会替换掉原本的 bean, 如代理增强");
        return bean;
    }
}
```



#### LifeCycleBean

```java
@Component
public class LifeCycleBean {

    public LifeCycleBean(){
        System.out.println("构造");
    }

    /**
     * String 类型使用 @Autowired 不会进行注入，使用 @Value 进行注入
     */
    @Autowired
    public void autowired(@Value("${Path}") String value){
        System.out.printf("依赖注入：%s\n", value);
    }

    @PostConstruct
    public void init(){
        System.out.println("初始化");
    }

    @PreDestroy
    public void destroy(){
        System.out.println("销毁");
    }

}

```



#### Application

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
//        context.addBeanFactoryPostProcessor();
        context.close();
    }


}
```



### Bean 后处理器

#### 作用

为 Bean 生命周期各个阶段提供扩展



#### 常见的后处理器

1. @Autowired 等注解的解析属于 bean 生命周期阶段（依赖注入, 初始化）的扩展功能，这些扩展功能由 bean 后处理器来完成
2. 每个后处理器各自增强什么功能
   * AutowiredAnnotationBeanPostProcessor 解析 @Autowired 与 @Value
   * CommonAnnotationBeanPostProcessor 解析 @Resource、@PostConstruct、@PreDestroy
   * ConfigurationPropertiesBindingPostProcessor 解析 @ConfigurationProperties
3. 另外 ContextAnnotationAutowireCandidateResolver 负责获取 @Value 的值，解析 @Qualifier、泛型、@Lazy 等

##### Bean1

```java
public class Bean1 {
    private static final Logger log = LoggerFactory.getLogger(Bean1.class);

    private Bean2 bean2;

    @Autowired
    public void setBean2(Bean2 bean2) {
        log.debug("@Autowired 生效: {}", bean2);
        this.bean2 = bean2;
    }

    @Autowired
    private Bean3 bean3;

    @Resource
    public void setBean3(Bean3 bean3) {
        log.debug("@Resource 生效: {}", bean3);
        this.bean3 = bean3;
    }

    private String home;

    @Autowired
    public void setHome(@Value("${JAVA_HOME}") String home) {
        log.debug("@Value 生效: {}", home);
        this.home = home;
    }

    @PostConstruct
    public void init() {
        log.debug("@PostConstruct 生效");
    }

    @PreDestroy
    public void destroy() {
        log.debug("@PreDestroy 生效");
    }

    @Override
    public String toString() {
        return "Bean1{" +
               "bean2=" + bean2 +
               ", bean3=" + bean3 +
               ", home='" + home + '\'' +
               '}';
    }
}

```



##### Bean2

```java
public class Bean2 {
}

```



##### Bean3

```java
public class Bean3 {
}

```



##### Bean4

```java
/*
    java.home=
    java.version=
 */
@ConfigurationProperties(prefix = "java")
public class Bean4 {

    private String home;

    private String version;

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Bean4{" +
               "home='" + home + '\'' +
               ", version='" + version + '\'' +
               '}';
    }
}

```



##### Application

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // GenericApplicationContext 是一个【干净】的容器，没有添加额外的处理器
        GenericApplicationContext context = new GenericApplicationContext();

        // 用原始方法注册三个 bean
        context.registerBean("bean1", Bean1.class);
        context.registerBean("bean2", Bean2.class);
        context.registerBean("bean3", Bean3.class);
        context.registerBean("bean4", Bean4.class);

        context.getDefaultListableBeanFactory().setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
        context.registerBean(AutowiredAnnotationBeanPostProcessor.class); // @Autowired @Value

        context.registerBean(CommonAnnotationBeanPostProcessor.class); // @Resource @PostConstruct @PreDestroy

        ConfigurationPropertiesBindingPostProcessor.register(context.getDefaultListableBeanFactory()); // @ConfigurationProperties

        // 初始化容器
        context.refresh(); // 执行beanFactory后处理器, 添加bean后处理器, 初始化所有单例

        System.out.println(context.getBean(Bean1.class));
        System.out.println(context.getBean(Bean4.class));

        // 销毁容器
        context.close();

        /*
            学到了什么
                a. @Autowired 等注解的解析属于 bean 生命周期阶段(依赖注入, 初始化)的扩展功能
                b. 这些扩展功能由 bean 后处理器来完成
         */
    }

}
```



#### @Autowired Bean 后处理器执行分析

1. AutowiredAnnotationBeanPostProcessor.findAutowiringMetadata 用来获取某个 bean 上加了 @Value @Autowired 的成员变量，方法参数的信息，表示为 InjectionMetadata
2. InjectionMetadata 可以完成依赖注入
3. InjectionMetadata 内部根据成员变量，方法参数封装为 DependencyDescriptor 类型
4. 有了 DependencyDescriptor，就可以利用 beanFactory.doResolveDependency 方法进行基于类型的查找



```java
// AutowiredAnnotationBeanPostProcessor 运行分析
public class DigInAutowired {
    public static void main(String[] args) throws Throwable {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("bean2", new Bean2()); // 此方法注册 bean 工厂不会再进行创建过程,依赖注入,初始化
        beanFactory.registerSingleton("bean3", new Bean3());
        beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver()); // @Value
        beanFactory.addEmbeddedValueResolver(new StandardEnvironment()::resolvePlaceholders); // ${} 的解析器

        // 1. 查找哪些属性、方法加了 @Autowired, 这称之为 InjectionMetadata
        AutowiredAnnotationBeanPostProcessor processor = new AutowiredAnnotationBeanPostProcessor();
        processor.setBeanFactory(beanFactory);

        Bean1 bean1 = new Bean1();
//        System.out.println(bean1);
//        processor.postProcessProperties(null, bean1, "bean1"); // 执行依赖注入 @Autowired @Value
//        System.out.println(bean1);

        Method findAutowiringMetadata = AutowiredAnnotationBeanPostProcessor.class.getDeclaredMethod("findAutowiringMetadata", String.class, Class.class, PropertyValues.class);
        findAutowiringMetadata.setAccessible(true);
        InjectionMetadata metadata = (InjectionMetadata) findAutowiringMetadata.invoke(processor, "bean1", Bean1.class, null);// 获取 Bean1 上加了 @Value @Autowired 的成员变量，方法参数信息
        System.out.println(metadata);

//         2. 调用 InjectionMetadata 来进行依赖注入, 注入时按类型查找值
        metadata.inject(bean1, "bean1", null);
        System.out.println(bean1);

        // metadata.inject 执行流程，如何按类型查找值
//        成员变量
//        Field bean3 = Bean1.class.getDeclaredField("bean3");
//        DependencyDescriptor dd1 = new DependencyDescriptor(bean3, false);
//        Object o = beanFactory.doResolveDependency(dd1, null, null, null);
//        System.out.println(o);
//
//        方法参数
//        Method setBean2 = Bean1.class.getDeclaredMethod("setBean2", Bean2.class);
//        DependencyDescriptor dd2 =
//                new DependencyDescriptor(new MethodParameter(setBean2, 0), true);
//        Object o1 = beanFactory.doResolveDependency(dd2, null, null, null);
//        System.out.println(o1);
//
//        值注入
//        Method setHome = Bean1.class.getDeclaredMethod("setHome", String.class);
//        DependencyDescriptor dd3 = new DependencyDescriptor(new MethodParameter(setHome, 0), true);
//        Object o2 = beanFactory.doResolveDependency(dd3, null, null, null);
//        System.out.println(o2);

    }
}

```



### BeanFactory 后处理器

#### 作用

为 BeanFactory 提供扩展



#### 常见的后处理器



* ConfigurationClassPostProcessor 可以解析
  * @ComponentScan
  * @Bean
  * @Import
  * @ImportResource
* MapperScannerConfigurer 可以解析
  * Mapper 接口



1. @ComponentScan, @Bean, @Mapper 等注解的解析属于核心容器（即 BeanFactory）的扩展功能
2. 这些扩展功能由不同的 BeanFactory 后处理器来完成，其实主要就是补充了一些 bean 定义



##### 示例

###### Config

```java
@Configuration
@ComponentScan(basePackages = "cn.example.uums.test.a05.component")
public class Config {
    @Bean
    public Bean1 bean1() {
        return new Bean1();
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean(DataSource dataSource) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean;
    }

    @Bean(initMethod = "init")
    public DruidDataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:mysql://192.168.31.100:3306/test");
        dataSource.setUsername("root");
        dataSource.setPassword("mysql");
        return dataSource;
    }
}
```

###### Bean1

```java
public class Bean1 {

    private static final Logger log = LoggerFactory.getLogger(Bean1.class);

    public Bean1() {
        log.debug("我被 Spring 管理啦");
    }
}

```

###### Bean2

```java
@Component
public class Bean2 {

    private static final Logger log = LoggerFactory.getLogger(Bean2.class);

    public Bean2() {
        log.debug("我被 Spring 管理啦");
    }
}
```

###### Bean3

```java
@Controller
public class Bean3 {

    private static final Logger log = LoggerFactory.getLogger(Bean3.class);

    public Bean3() {
        log.debug("我被 Spring 管理啦");
    }
}
```

###### Mapper1

```java
@Mapper
public interface Mapper1 {
}
```

###### Mapper2

```java
@Mapper
public interface Mapper2 {
}
```

###### Application

```java
public class Application {
    public static void main(String[] args) throws IOException {

        // GenericApplicationContext 是一个【干净】的容器
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("config", Config.class);
        context.registerBean(ConfigurationClassPostProcessor.class); // @ComponentScan @Bean @Import @ImportResource
        context.registerBean(MapperScannerConfigurer.class, bd -> { // @MapperScanner
            bd.getPropertyValues().add("basePackage", "cn.example.uums.test.a05.mapper");
        });

        // 初始化容器
        context.refresh();

        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }

        // 销毁容器
        context.close();
    }
}

```



##### ConfigurationClassPostProcessor

1. Spring 操作元数据的工具类 CachingMetadataReaderFactory
2. 通过注解元数据（AnnotationMetadata）获取直接或间接标注的注解信息
3. 通过类元数据（ClassMetadata）获取类名，AnnotationBeanNameGenerator 生成 bean 名
4. 解析元数据是基于 ASM 技术



###### 实现原理

```java
public class Application {
    public static void main(String[] args) throws IOException {

        // GenericApplicationContext 是一个【干净】的容器
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("config", Config.class);
        // 后处理器 ConfigurationClassPostProcessor 原理示例
        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
        AnnotationBeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();
        if(Config.class.isAnnotationPresent(ComponentScan.class)){
            ComponentScan componentScan = Config.class.getAnnotation(ComponentScan.class);
            for (String basePackage : componentScan.basePackages()) {
                System.out.println(basePackage);
                // cn.example.uums.test.a05.component classpath*:cn/example/uums/test/a05/component/**/*.class
                String path = "classpath*:".concat(basePackage.replace('.','/')).concat("/**/*.class");
                System.out.println(path);
                Resource[] resources = context.getResources(path);
                for(Resource resource:resources){
//                    System.out.println(resource);
                    MetadataReader reader = metadataReaderFactory.getMetadataReader(resource);
                    System.out.println("类名:"+reader.getClassMetadata().getClassName());
                    System.out.println("Component:"+reader.getAnnotationMetadata().hasAnnotation(Component.class.getName()));
                    System.out.println("Component 派生:"+reader.getAnnotationMetadata().hasMetaAnnotation(Component.class.getName()));
                    if(reader.getAnnotationMetadata().hasAnnotation(Component.class.getName())
                    || reader.getAnnotationMetadata().hasMetaAnnotation(Component.class.getName())){
                        AbstractBeanDefinition abd = BeanDefinitionBuilder.genericBeanDefinition(reader.getClassMetadata().getClassName()).getBeanDefinition();
                        DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
                        String beanName = beanNameGenerator.generateBeanName(abd, beanFactory);
                        beanFactory.registerBeanDefinition(beanName, abd);
                    }
                }
            }
        }
        // 初始化容器
        context.refresh();
        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }
        // 销毁容器
        context.close();
    }
}
```



###### 自定义后处理器

```java
public class ComponentScanPostProcessor implements BeanDefinitionRegistryPostProcessor {

    // context.refresh(); 调用时会回调该方法
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanFactory) throws BeansException {
        // 后处理器 ConfigurationClassPostProcessor 原理示例
        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
        AnnotationBeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();
        if(Config.class.isAnnotationPresent(ComponentScan.class)){
            ComponentScan componentScan = Config.class.getAnnotation(ComponentScan.class);
            for (String basePackage : componentScan.basePackages()) {
                System.out.println(basePackage);
                // cn.example.uums.test.a05.component classpath*:cn/example/uums/test/a05/component/**/*.class
                String path = "classpath*:".concat(basePackage.replace('.','/')).concat("/**/*.class");
                System.out.println(path);
                try{
                    Resource[] resources = new PathMatchingResourcePatternResolver().getResources(path);
                    for(Resource resource:resources){
//                    System.out.println(resource);
                        MetadataReader reader = metadataReaderFactory.getMetadataReader(resource);
                        System.out.println("类名:"+reader.getClassMetadata().getClassName());
                        System.out.println("Component:"+reader.getAnnotationMetadata().hasAnnotation(Component.class.getName()));
                        System.out.println("Component 派生:"+reader.getAnnotationMetadata().hasMetaAnnotation(Component.class.getName()));
                        if(reader.getAnnotationMetadata().hasAnnotation(Component.class.getName())
                                || reader.getAnnotationMetadata().hasMetaAnnotation(Component.class.getName())){
                            AbstractBeanDefinition abd = BeanDefinitionBuilder.genericBeanDefinition(reader.getClassMetadata().getClassName()).getBeanDefinition();
                            String beanName = beanNameGenerator.generateBeanName(abd, beanFactory);
                            beanFactory.registerBeanDefinition(beanName, abd);

                        }
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }

            }
        }

    }
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}

```

```java
public class Application {
    public static void main(String[] args) throws IOException {

        // GenericApplicationContext 是一个【干净】的容器
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("config", Config.class);

        context.registerBean(ComponentScanPostProcessor.class); // 解析 @ComponentScan

        // 初始化容器
        context.refresh();

        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }

        // 销毁容器
        context.close();

    }
}
```



#### @Bean 实现

##### 实现原理

```java
public class Application {

    public static void main(String[] args) throws IOException {

        // GenericApplicationContext 是一个【干净】的容器
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("config", Config.class);
        // 后处理器 ConfigurationClassPostProcessor 原理示例
        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
        AnnotationBeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();
        MetadataReader reader = metadataReaderFactory.getMetadataReader(new ClassPathResource("cn/example/uums/test/a05/Config.class"));
        Set<MethodMetadata> methodMetadataSet = reader.getAnnotationMetadata().getAnnotatedMethods(Bean.class.getName());
        for(MethodMetadata methodMetadata:methodMetadataSet){
            System.out.println(methodMetadata.getMethodName());
            String initMethod = methodMetadata.getAnnotationAttributes(Bean.class.getName()).get("initMethod").toString();

            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition()
                    .setFactoryMethodOnBean(methodMetadata.getMethodName(), "config")
                    // 配置自动装配模式，需要参数的方法能自动装配,构造方法/工厂方法选择 AUTOWIRE_CONSTRUCTOR
                    .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
            if(null != initMethod){
                builder.setInitMethodName(initMethod);
            }

            AbstractBeanDefinition abd = builder.getBeanDefinition();
            DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
            String beanName = methodMetadata.getMethodName();
            beanFactory.registerBeanDefinition(beanName, abd);
        }

        // 初始化容器
        context.refresh();

        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }

        // 销毁容器
        context.close();

    }
}
```



##### 自定义后处理器

```java
public class BeanPostProcessor implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanFactory) throws BeansException {
        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
        MetadataReader reader = null;
        try {
            reader = metadataReaderFactory.getMetadataReader(new ClassPathResource("cn/example/uums/test/a05/Config.class"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Set<MethodMetadata> methodMetadataSet = reader.getAnnotationMetadata().getAnnotatedMethods(Bean.class.getName());
        for(MethodMetadata methodMetadata:methodMetadataSet){
            System.out.println(methodMetadata.getMethodName());
            String initMethod = methodMetadata.getAnnotationAttributes(Bean.class.getName()).get("initMethod").toString();

            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition()
                    .setFactoryMethodOnBean(methodMetadata.getMethodName(), "config")
                    // 配置自动装配模式，需要参数的方法能自动装配,构造方法/工厂方法选择 AUTOWIRE_CONSTRUCTOR
                    .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
            if(null != initMethod){
                builder.setInitMethodName(initMethod);
            }

            AbstractBeanDefinition abd = builder.getBeanDefinition();
            String beanName = methodMetadata.getMethodName();
            beanFactory.registerBeanDefinition(beanName, abd);
        }
    }
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}
```

```java
public class Application {
    public static void main(String[] args) throws IOException {

        // GenericApplicationContext 是一个【干净】的容器
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("config", Config.class);
        // 后处理器 ConfigurationClassPostProcessor 原理示例

        context.registerBean(BeanPostProcessor.class); // 解析 @Bean

        // 初始化容器
        context.refresh();

        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }

        // 销毁容器
        context.close();

    }
}
```



#### Mapper 实现



##### 自定义后处理器

1. Mapper 接口被 Spring 管理的本质：实际是被作为 MapperFactoryBean 注册到容器中
2. Spring 的诡异做法，根据接口生成的 BeanDefinition 仅为根据接口名生成 bean 名

```java
public class MapperPostProcessor implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanFactory) throws BeansException {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:cn/example/uums/test/a05/mapper/**/*.class");
            AnnotationBeanNameGenerator generator = new AnnotationBeanNameGenerator();
            CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory();
            for (Resource resource : resources) {
                MetadataReader reader = factory.getMetadataReader(resource);
                ClassMetadata classMetadata = reader.getClassMetadata();
                if (classMetadata.isInterface()) {
                    AbstractBeanDefinition bd = BeanDefinitionBuilder.genericBeanDefinition(MapperFactoryBean.class)
                            .addConstructorArgValue(classMetadata.getClassName())
                            .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE)
                            .getBeanDefinition();
                    String name = generator.generateBeanName(
                            BeanDefinitionBuilder.genericBeanDefinition(classMetadata.getClassName()).getBeanDefinition(),
                            beanFactory);
                    beanFactory.registerBeanDefinition(name, bd);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
```



```java
public class A05 {
    private static final Logger log = LoggerFactory.getLogger(A05.class);

    public static void main(String[] args) throws IOException {

        // GenericApplicationContext 是一个【干净】的容器
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("config", Config.class);
        // 后处理器 MapperScannerConfigurer 原理示例

        context.registerBean(BeanPostProcessor.class); // 解析 @Bean
        context.registerBean(MapperPostProcessor.class); // 解析 Mapper 接口

        // 初始化容器
        context.refresh();

        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }

        // 销毁容器
        context.close();

    }
}

```



### Aware 接口

#### Aware 接口及 InitializingBean 接口

1. Aware 接口提供了一种【内置】 的注入手段，例如
   * BeanNameAware 注入 bean 的名字
   * BeanFactoryAware 注入 BeanFactory 容器
   * ApplicationContextAware 注入 ApplicationContext 容器
   * EmbeddedValueResolverAware 注入 ${} 解析器
2. InitializingBean 接口提供了一种【内置】的初始化手段
3. 对比
   * 内置的注入和初始化不受扩展功能的影响，总会被执行
   * 而扩展功能受某些情况影响可能会失效
   * 因此 Spring 框架内部的类常用内置注入和初始化



MyBean

```java
public class MyBean implements BeanNameAware, ApplicationContextAware, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(MyBean.class);

    @Override
    public void setBeanName(String name) {
        log.debug("当前bean " + this + " 名字叫:" + name);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.debug("当前bean " + this + " 容器是:" + applicationContext);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("当前bean " + this + " 初始化");
    }

    @Autowired
    public void aaa(ApplicationContext applicationContext) {
        log.debug("当前bean " + this + " 使用@Autowired 容器是:" + applicationContext);
    }

    @PostConstruct
    public void init() {
        log.debug("当前bean " + this + " 使用@PostConstruct 初始化");
    }
}
```



Application

```java
/*
    Aware 接口及 InitializingBean 接口
 */
public class Application {
    public static void main(String[] args) {
        /*
            1. Aware 接口用于注入一些与容器相关信息, 例如
                a. BeanNameAware 注入 bean 的名字
                b. BeanFactoryAware 注入 BeanFactory 容器
                c. ApplicationContextAware 注入 ApplicationContext 容器
                d. EmbeddedValueResolverAware ${}
         */
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("myBean", MyBean.class);
        context.registerBean(AutowiredAnnotationBeanPostProcessor.class);

        /*
            2. 有同学说: b、c、d 的功能用 @Autowired 就能实现啊, 为啥还要用 Aware 接口呢
            简单地说:
                a. @Autowired 的解析需要用到 bean 后处理器, 属于扩展功能
                b. 而 Aware 接口属于内置功能, 不加任何扩展, Spring 就能识别
            某些情况下, 扩展功能会失效, 而内置功能不会失效
         */

        context.refresh(); // 1. beanFactory 后处理器,  2. 添加 bean 后处理器, 3. 初始化单例
        context.close();

        /*
            a. Aware 接口提供了一种【内置】 的注入手段, 可以注入 BeanFactory, ApplicationContext
            b. InitializingBean 接口提供了一种【内置】的初始化手段
            c. 内置的注入和初始化不受扩展功能的影响, 总会被执行, 因此 Spring 框架内部的类常用它们
         */
    }
}
```



#### 配置类 @Autowired 失效分析

Java 配置类不包含 BeanFactoryPostProcessor 的情况

```mermaid
sequenceDiagram 
participant ac as ApplicationContext
participant bfpp as BeanFactoryPostProcessor
participant bpp as BeanPostProcessor
participant config as Java配置类
ac ->> bfpp : 1. 执行 BeanFactoryPostProcessor
ac ->> bpp : 2. 注册 BeanPostProcessor
ac ->> +config : 3. 创建和初始化
bpp ->> config : 3.1 依赖注入扩展(如 @Value 和 @Autowired)
bpp ->> config : 3.2 初始化扩展(如 @PostConstruct)
ac ->> config : 3.3 执行 Aware 及 InitializingBean
config -->> -ac : 3.4 创建成功
```

Java 配置类包含 BeanFactoryPostProcessor 的情况，因此要创建其中的 BeanFactoryPostProcessor 必须提前创建 Java 配置类，而此时的 BeanPostProcessor 还未准备好，导致 @Autowired 等注解失效

```mermaid
sequenceDiagram 
participant ac as ApplicationContext
participant bfpp as BeanFactoryPostProcessor
participant bpp as BeanPostProcessor
participant config as Java配置类
ac ->> +config : 3. 创建和初始化
ac ->> config : 3.1 执行 Aware 及 InitializingBean
config -->> -ac : 3.2 创建成功

ac ->> bfpp : 1. 执行 BeanFactoryPostProcessor
ac ->> bpp : 2. 注册 BeanPostProcessor



```

MyConfig1

```java
@Configuration
public class MyConfig1 {

    private static final Logger log = LoggerFactory.getLogger(MyConfig1.class);

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        log.debug("注入 ApplicationContext");
    }

    @PostConstruct
    public void init() {
        log.debug("初始化");
    }

    @Bean //  beanFactory 后处理器
    public BeanFactoryPostProcessor processor1() {
        return beanFactory -> {
            log.debug("执行 processor1");
        };
    }

}
```



Application

```java
/*
    Aware 接口及 InitializingBean 接口
 */
public class Application {
    public static void main(String[] args) {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("myConfig1", MyConfig1.class);
        context.registerBean(AutowiredAnnotationBeanPostProcessor.class);
        context.registerBean(CommonAnnotationBeanPostProcessor.class);
        context.registerBean(ConfigurationClassPostProcessor.class);

        /*
            你会发现用 Aware 注入 ApplicationContext 成功, 而 @Autowired 注入 ApplicationContext 失败
         */

        context.refresh(); // 1. beanFactory 后处理器,  2. 添加 bean 后处理器, 3. 初始化单例
        context.close();
    }
}
```



> ***注意***
>
> 解决方法：
>
> * 用内置依赖注入和初始化取代扩展依赖注入和初始化
> * 用静态工厂方法代替实例工厂方法，避免工厂对象提前被创建



### 初始化与销毁

Bean1

```java
public class Bean1 implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(Bean1.class);

    @PostConstruct
    public void init1() {
        log.debug("初始化1");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("初始化2");
    }

    public void init3() {
        log.debug("初始化3");
    }
}

```

Bean2

```java
public class Bean2 implements DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(Bean2.class);

    @PreDestroy
    public void destroy1() {
        log.debug("销毁1");
    }

    @Override
    public void destroy() throws Exception {
        log.debug("销毁2");
    }

    public void destroy3() {
        log.debug("销毁3");
    }
}
```

Application

```java
/*
    初始化和销毁的执行顺序
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        context.close();
        /*
            学到了什么
                a. Spring 提供了多种初始化和销毁手段
                b. Spring 的面试有多么地卷
         */
    }

    @Bean(initMethod = "init3")
    public Bean1 bean1() {
        return new Bean1();
    }

    @Bean(destroyMethod = "destroy3")
    public Bean2 bean2() {
        return new Bean2();
    }
}
```



#### 多种初始化手段

Spring 提供了多种初始化手段，除了 @PostConstruct，@Bean(initMethod) 之外，还可以实现 InitializingBean 接口来进行初始化，如果同一个 bean 用了以上手段声明了 3 个初始化方法，那么它们的执行顺序是

1. @PostConstruct 标注的初始化方法
2. InitializingBean 接口的初始化方法
3. @Bean(initMethod) 指定的初始化方法

#### 执行顺序



与初始化类似，Spring 也提供了多种销毁手段，执行顺序为

1. @PreDestroy 标注的销毁方法
2. DisposableBean 接口的销毁方法
3. @Bean(destroyMethod) 指定的销毁方法



### Scope 

#### Scope 类型

在当前版本(5.3.10)的 Spring 和 Spring Boot 程序中，支持五种 Scope

* singleton，容器启动时创建（未设置延迟），容器关闭时销毁
* prototype，每次使用时创建，不会自动销毁，需要调用 DefaultListableBeanFactory.destroyBean(bean) 销毁
* request，每次请求用到此 bean 时创建，请求结束时销毁
* session，每个会话用到此 bean 时创建，会话结束时销毁
* application，web 容器用到此 bean 时创建，容器停止时销毁

有些文章提到有 globalSession 这一 Scope，也是陈旧的说法，目前 Spring 中已废弃



但要注意，如果在 singleton 注入其它 scope 都会有问题，解决方法有

* @Lazy
* @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
* ObjectFactory
* ApplicationContext.getBean



BeanForSession

```java
@Scope("session")
@Component
public class BeanForSession {
    private static final Logger log = LoggerFactory.getLogger(BeanForSession.class);

    @PreDestroy
    public void destroy() {
        log.debug("destroy");
    }
}
```

BeanForRequest

```java
@Scope("request")
@Component
public class BeanForRequest {
    private static final Logger log = LoggerFactory.getLogger(BeanForRequest.class);

    @PreDestroy
    public void destroy() {
        log.debug("destroy");
    }

}
```

BeanForApplication

```java
@Scope("application")
@Component
public class BeanForApplication {
    private static final Logger log = LoggerFactory.getLogger(BeanForApplication.class);

    @PreDestroy
    public void destroy() {
        log.debug("destroy");
    }
}
```

MyController

```java
@RestController
public class MyController {

    @Lazy
    @Autowired
    private BeanForRequest beanForRequest;

    @Lazy
    @Autowired
    private BeanForSession beanForSession;

    @Lazy
    @Autowired
    private BeanForApplication beanForApplication;

    @GetMapping(value = "/test", produces = "text/html")
    public String test(HttpServletRequest request, HttpSession session) {
        ServletContext sc = request.getServletContext();
        String sb = "<ul>" +
                    "<li>" + "request scope:" + beanForRequest + "</li>" +
                    "<li>" + "session scope:" + beanForSession + "</li>" +
                    "<li>" + "application scope:" + beanForApplication + "</li>" +
                    "</ul>";
        return sb;
    }

}
```

Application

```java
/*
    singleton, prototype, request, session, application

    jdk >= 9 如果反射调用 jdk 中方法
    jdk <= 8 不会有问题

    演示 request, session, application 作用域
    打开不同的浏览器, 刷新 http://localhost:8080/test 即可查看效果
    如果 jdk > 8, 运行时请添加 --add-opens java.base/java.lang=ALL-UNNAMED
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(A08.class, args);
        /*
            学到了什么
                a. 有几种 scope
                b. 在 singleton 中使用其它几种 scope 的方法
                c. 其它 scope 的销毁
                    1. 可以将通过 server.servlet.session.timeout=10s 观察 session bean 的销毁
                    2. ServletContextScope 销毁机制疑似实现有误
         */
    }
}
```

* 打开不同的浏览器, 刷新 http://localhost:8080/test 即可查看效果
* 如果 jdk > 8, 运行时请添加 --add-opens java.base/java.lang=ALL-UNNAMED



1. 有几种 scope
2. 在 singleton 中使用其它几种 scope 的方法
3. 其它 scope 的销毁时机
   * 可以将通过 server.servlet.session.timeout=30s 观察 session bean 的销毁
   * ServletContextScope 销毁机制疑似实现有误



#### singleton 注入其它 scope 失效

以单例注入多例为例

有一个单例对象 E

```java
@Component
public class E {
    private static final Logger log = LoggerFactory.getLogger(E.class);

    private F f;

    public E() {
        log.info("E()");
    }

    @Autowired
    public void setF(F f) {
        this.f = f;
        log.info("setF(F f) {}", f.getClass());
    }

    public F getF() {
        return f;
    }
}
```

要注入的对象 F 期望是多例

```java
@Component
@Scope("prototype")
public class F {
    private static final Logger log = LoggerFactory.getLogger(F.class);

    public F() {
        log.info("F()");
    }
}
```

测试

```java
E e = context.getBean(E.class);
F f1 = e.getF();
F f2 = e.getF();
System.out.println(f1);
System.out.println(f2);
```

输出

```
com.itheima.demo.cycle.F@6622fc65
com.itheima.demo.cycle.F@6622fc65
```

发现它们是同一个对象，而不是期望的多例对象



对于单例对象来讲，依赖注入仅发生了一次，后续再没有用到多例的 F，因此 E 用的始终是第一次依赖注入的 F

```mermaid
graph LR

e1(e 创建)
e2(e set 注入 f)

f1(f 创建)

e1-->f1-->e2

```

解决

* 仍然使用 @Lazy 生成代理
* 代理对象虽然还是同一个，但当每次**使用代理对象的任意方法**时，由代理创建新的 f 对象

```mermaid
graph LR

e1(e 创建)
e2(e set 注入 f代理)

f1(f 创建)
f2(f 创建)
f3(f 创建)

e1-->e2
e2--使用f方法-->f1
e2--使用f方法-->f2
e2--使用f方法-->f3

```

```java
@Component
public class E {

    @Autowired
    @Lazy
    public void setF(F f) {
        this.f = f;
        log.info("setF(F f) {}", f.getClass());
    }

    // ...
}
```

> ***注意***
>
> * @Lazy 加在也可以加在成员变量上，但加在 set 方法上的目的是可以观察输出，加在成员变量上就不行了
> * @Autowired 加在 set 方法的目的类似

输出

```
E: setF(F f) class com.itheima.demo.cycle.F$$EnhancerBySpringCGLIB$$8b54f2bc
F: F()
com.itheima.demo.cycle.F@3a6f2de3
F: F()
com.itheima.demo.cycle.F@56303b57
```

从输出日志可以看到调用 setF 方法时，f 对象的类型是代理类型



#### 解决失效的4种方法



* 如果 jdk > 8, 运行时请添加 --add-opens java.base/java.lang=ALL-UNNAMED

1. 单例注入其它 scope 的四种解决方法
   * @Lazy
   * @Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
   * ObjectFactory
   * ApplicationContext
2. 解决方法虽然不同，但理念上殊途同归: 都是推迟其它 scope bean 的获取

F1

```java
@Scope("prototype")
@Component
public class F1 {
}
```

F2

```java
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class F2 {
}
```

F3

```java
@Scope("prototype")
@Component
public class F3 {
}
```

F4

```java
@Scope("prototype")
@Component
public class F4 {
}
```

E

```java
@Component
public class E {

    @Lazy
    @Autowired
    private F1 f1;

    @Autowired
    private F2 f2;

    @Autowired
    private ObjectFactory<F3> f3;

    @Autowired
    private ApplicationContext context;

    public F1 getF1() {
        return f1;
    }

    public F2 getF2() {
        return f2;
    }

    public F3 getF3() {
        return f3.getObject();
    }

    public F4 getF4() {
        return context.getBean(F4.class);
    }
}
```

Application

```java
@ComponentScan("cn.example.uums.test.a08.sub")
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(A08_1.class);

        E e = context.getBean(E.class);
        log.debug("{}", e.getF1().getClass());
        log.debug("{}", e.getF1());
        log.debug("{}", e.getF1());
        log.debug("{}", e.getF1());

        log.debug("{}", e.getF2().getClass());
        log.debug("{}", e.getF2());
        log.debug("{}", e.getF2());
        log.debug("{}", e.getF2());

        log.debug("{}", e.getF3());
        log.debug("{}", e.getF3());

        log.debug("{}", e.getF4());
        log.debug("{}", e.getF4());

        context.close();
        /*
            学到了什么
                a. 单例注入其它 scope 的四种解决方法
                b. 解决方法虽然不同, 但理念上殊途同归: 都是推迟其它 scope bean 的获取
         */
    }
}
```



## AOP

AOP 底层实现方式之一是代理，由代理结合通知和目标，提供增强功能

除此以外，aspectj 提供了两种另外的 AOP 底层实现：

* 第一种是通过 ajc 编译器在**编译** class 类文件时，就把通知的增强功能，织入到目标类的字节码中

* 第二种是通过 agent 在**加载**目标类时，修改目标类的字节码，织入增强功能
* 作为对比，之前学习的代理是**运行**时生成新的字节码

简单比较的话：

* aspectj 在编译和加载时，修改目标字节码，性能较高
* aspectj 因为不用代理，能突破一些技术上的限制，例如对构造、对静态方法、对 final 也能增强
* 但 aspectj 侵入性较强，且需要学习新的 aspectj 特有语法，因此没有广泛流行



### AOP - ajc 编译器

1. 编译器也能修改 class 实现增强
2. 编译器增强能突破代理仅能通过方法重写增强的限制：可以对构造方法、静态方法等实现增强

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>cn.example.uums</groupId>
    <artifactId>uums</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>

    <!-- 引入Spring Boot依赖 -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.3.12.RELEASE</version>
    </parent>
    <dependencies>
        <!-- 引入Web场景依赖启动器 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
        </dependency>

        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjrt</artifactId>
        </dependency>

    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>aspectj-maven-plugin</artifactId>
                <version>1.14.0</version>
                <configuration>
                    <complianceLevel>1.8</complianceLevel>
                    <source>8</source>
                    <target>8</target>
                    <showWeaveInfo>true</showWeaveInfo>
                    <verbose>true</verbose>
                    <Xlint>ignore</Xlint>
                    <encoding>UTF-8</encoding>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <!-- use this goal to weave all your main classes -->
                            <goal>compile</goal>
                            <!-- use this goal to weave all your test classes -->
                            <goal>test-compile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

```java
@Service
public class MyService {

    public static void foo() {
        System.out.println("foo()");
    }
}
```

```java
@Aspect // 注意此切面并未被 Spring 管理
public class MyAspect {

    @Before("execution(* cn.example.uums.test.aop_01.service.MyService.foo())")
    public void before() {
        System.out.println("before()");
    }
}
```

```java
/*
    注意几点
    1. 版本选择了 java 8, 因为目前的 aspectj-maven-plugin 1.14.0 最高只支持到 java 16
    2. 一定要用 maven 的 compile 来编译, idea 不会调用 ajc 编译器
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
//        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
//        MyService service = context.getBean(MyService.class);
//
//        System.out.printf("service class: %s \n", service.getClass());
//        service.foo();
//
//        context.close();

        new MyService().foo();

        /*
            学到了什么
            1. aop 的原理并非代理一种, 编译器也能玩出花样
         */
    }
}
```



> ***注意***
>
> * 版本选择了 java 8, 因为目前的 aspectj-maven-plugin 1.14.0 最高只支持到 java 16
> * 一定要用 maven 的 compile 来编译, idea 不会调用 ajc 编译器





### AOP - agent 类加载

1. 类加载时可以通过 agent 修改 class 实现增强

```xml
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
</dependency>

<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjrt</artifactId>
</dependency>
```

```java
@Service
public class MyService {

    final public void foo() {
        System.out.println("foo()");
        this.bar();
    }

    public void bar() {
        System.out.println("bar()");
    }
}
```

```java
@Aspect // 注意此切面并未被 Spring 管理
public class MyAspect {

    @Before("execution(* cn.example.uums.test.aop_02.service.MyService.*())")
    public void before() {
        System.out.println("before()");
    }
}
```

```java
/*
    注意几点
    1. 版本选择了 java 8, 因为目前的 aspectj-maven-plugin 1.14.0 最高只支持到 java 16
    2. 运行时需要在 VM options 里加入 -javaagent:C:/Users/manyh/.m2/repository/org/aspectj/aspectjweaver/1.9.7/aspectjweaver-1.9.7.jar
        把其中 C:/Users/manyh/.m2/repository 改为你自己 maven 仓库起始地址
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        MyService service = context.getBean(MyService.class);

        // MyService 并非代理, 但 foo 方法也被增强了, 做增强的 java agent, 在加载类时, 修改了 class 字节码
        System.out.printf("service class: %s \n", service.getClass());
        service.foo();

//        context.close();

        /*
            学到了什么
            1. aop 的原理并非代理一种, agent 也能, 只要字节码变了, 行为就变了
         */
    }
}
```

通过 arthas 反编译 MyAspect 类可以看到，在类加载阶段修改字节码达到增强的效果



### AOP - proxy

#### JDK 动态代理

##### 示例

```java
public class JdkProxyDemo {

    interface Foo {
        void foo();
    }

    static final class Target implements Foo {
        public void foo() {
            System.out.println("target foo");
        }
    }

    // JDK 只能针对接口代理
    // cglib
    public static void main(String[] param) throws IOException {
        // 目标对象
        Target target = new Target();

        ClassLoader loader = JdkProxyDemo.class.getClassLoader(); // 用来加载在运行期间动态生成的字节码
        Foo proxy = (Foo) Proxy.newProxyInstance(loader, new Class[]{Foo.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("before...");
                // 目标.方法(参数)
                // 方法.invoke(目标, 参数);
                Object result = method.invoke(target, args);
                System.out.println("after....");
                return result; // 让代理也返回目标方法执行的结果
            }
        });

        System.out.println(proxy.getClass());

        proxy.foo();

        System.in.read();
    }
}
```

运行结果

```
proxy before...
target foo
proxy after...
```

* JDK 动态代理要求目标**必须**实现接口，生成的代理类实现相同接口，因此代理与目标之间是平级兄弟关系



##### 原理

代理无非就是利用了多态、反射的知识

1. 方法重写可以增强逻辑，只不过这【增强逻辑】千变万化，不能写死在代理内部
2. 通过接口回调将【增强逻辑】置于代理类之外
3. 配合接口方法反射（是多态调用），就可以再联动调用目标方法
4. 会用 arthas 的 jad 工具反编译代理类
5. 限制：代理增强是借助多态来实现，因此成员变量、静态方法、final 方法均不能通过代理实现

```java
public interface Foo {
    void foo();
    int bar();
}
```

```java
public class Target implements Foo {
    public void foo() {
        System.out.println("target foo");
    }

    @Override
    public int bar() {
        System.out.println("target bar");
        return 100;
    }
}
```

```java
public class $Proxy0 extends Proxy implements Foo {

    public $Proxy0(InvocationHandler h) {
        super(h);
    }
    @Override
    public void foo() {
        try {
            h.invoke(this, foo, new Object[0]);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    @Override
    public int bar() {
        try {
            Object result = h.invoke(this, bar, new Object[0]);
            return (int) result;
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    static Method foo;
    static Method bar;
    static {
        try {
            foo = Foo.class.getMethod("foo");
            bar = Foo.class.getMethod("bar");
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }
}
```

```java
public class A12 {

    /*interface InvocationHandler {
        Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
    }*/

    public static void main(String[] param) {
        Foo proxy = new $Proxy0(new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable{
                // 1. 功能增强
                System.out.println("before...");
                // 2. 调用目标
//                new Target().foo();
                return method.invoke(new Target(), args);
            }
        });
        proxy.foo();
        proxy.bar();
    }
}
```



##### 源码

使用 arthas 反编译示例的代理类字节码

`jad cn.example.uums.test.a11.$Proxy0`

```java
/*
 * Decompiled with CFR.
 *
 * Could not load the following classes:
 *  cn.example.uums.test.a11.JdkProxyDemo$Foo
 */
package cn.example.uums.test.a11;

import cn.example.uums.test.a11.JdkProxyDemo;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

final class $Proxy0
extends Proxy
implements JdkProxyDemo.Foo {
    private static final Method m0;
    private static final Method m1;
    private static final Method m2;
    private static final Method m3;

    public $Proxy0(InvocationHandler invocationHandler) {
        super(invocationHandler);
    }

    static {
        try {
            m0 = Class.forName("java.lang.Object").getMethod("hashCode", new Class[0]);
            m1 = Class.forName("java.lang.Object").getMethod("equals", Class.forName("java.lang.Object"));
            m2 = Class.forName("java.lang.Object").getMethod("toString", new Class[0]);
            m3 = Class.forName("cn.example.uums.test.a11.JdkProxyDemo$Foo").getMethod("foo", new Class[0]);
            return;
        }
        catch (NoSuchMethodException noSuchMethodException) {
            throw new NoSuchMethodError(noSuchMethodException.getMessage());
        }
        catch (ClassNotFoundException classNotFoundException) {
            throw new NoClassDefFoundError(classNotFoundException.getMessage());
        }
    }

    public final boolean equals(Object object) {
        try {
            return (Boolean)this.h.invoke(this, m1, new Object[]{object});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    public final String toString() {
        try {
            return (String)this.h.invoke(this, m2, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    public final int hashCode() {
        try {
            return (Integer)this.h.invoke(this, m0, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    public final void foo() {
        try {
            this.h.invoke(this, m3, null);
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    private static MethodHandles.Lookup proxyClassLookup(MethodHandles.Lookup lookup) throws IllegalAccessException {
        if (lookup.lookupClass() == Proxy.class && lookup.hasFullPrivilegeAccess()) {
            return MethodHandles.lookup();
        }
        throw new IllegalAccessException(lookup.toString());
    }
}
```



##### ASM

JDK 代理是程序运行时直接生成代理类的字节码，生成字节码的技术叫 ASM，在 JDK 代理中，生成代理类字节码使用的是字节码指令生成的。

下面展示的是使用插件生成代理类字节码

idea 安装插件 ASM Bytecode Outline

以原理中的代理类为例，右键 -> show Bytecode outline -> ASMified

就生成了生成代理类字节码的代码。

```java
package cn.example.uums.test.a12;

import java.util.*;

import aj.org.objectweb.asm.*;

public class $Proxy0Dump implements Opcodes {

    public static byte[] dump() throws Exception {

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, "cn/example/uums/test/a12/$Proxy0", null, "java/lang/reflect/Proxy", new String[]{"cn/example/uums/test/a12/Foo"});

        cw.visitSource("$Proxy0.java", null);

        {
            fv = cw.visitField(ACC_STATIC, "foo", "Ljava/lang/reflect/Method;", null, null);
            fv.visitEnd();
        }
        {
            fv = cw.visitField(ACC_STATIC, "bar", "Ljava/lang/reflect/Method;", null, null);
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/reflect/InvocationHandler;)V", null, null);
            mv.visitParameter("h", 0);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(13, l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/reflect/Proxy", "<init>", "(Ljava/lang/reflect/InvocationHandler;)V", false);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(14, l1);
            mv.visitInsn(RETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLocalVariable("this", "Lcn/example/uums/test/a12/$Proxy0;", null, l0, l2, 0);
            mv.visitLocalVariable("h", "Ljava/lang/reflect/InvocationHandler;", null, l0, l2, 1);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "foo", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock(l0, l1, l2, "java/lang/RuntimeException");
            mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Error");
            Label l3 = new Label();
            mv.visitTryCatchBlock(l0, l1, l3, "java/lang/Throwable");
            mv.visitLabel(l0);
            mv.visitLineNumber(18, l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "cn/example/uums/test/a12/$Proxy0", "h", "Ljava/lang/reflect/InvocationHandler;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETSTATIC, "cn/example/uums/test/a12/$Proxy0", "foo", "Ljava/lang/reflect/Method;");
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/reflect/InvocationHandler", "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", true);
            mv.visitInsn(POP);
            mv.visitLabel(l1);
            mv.visitLineNumber(23, l1);
            Label l4 = new Label();
            mv.visitJumpInsn(GOTO, l4);
            mv.visitLabel(l2);
            mv.visitLineNumber(19, l2);
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"});
            mv.visitVarInsn(ASTORE, 1);
            Label l5 = new Label();
            mv.visitLabel(l5);
            mv.visitLineNumber(20, l5);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitInsn(ATHROW);
            mv.visitLabel(l3);
            mv.visitLineNumber(21, l3);
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"});
            mv.visitVarInsn(ASTORE, 1);
            Label l6 = new Label();
            mv.visitLabel(l6);
            mv.visitLineNumber(22, l6);
            mv.visitTypeInsn(NEW, "java/lang/reflect/UndeclaredThrowableException");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>", "(Ljava/lang/Throwable;)V", false);
            mv.visitInsn(ATHROW);
            mv.visitLabel(l4);
            mv.visitLineNumber(24, l4);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitInsn(RETURN);
            Label l7 = new Label();
            mv.visitLabel(l7);
            mv.visitLocalVariable("e", "Ljava/lang/Throwable;", null, l5, l3, 1);
            mv.visitLocalVariable("e", "Ljava/lang/Throwable;", null, l6, l4, 1);
            mv.visitLocalVariable("this", "Lcn/example/uums/test/a12/$Proxy0;", null, l0, l7, 0);
            mv.visitMaxs(4, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "bar", "()I", null, null);
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock(l0, l1, l2, "java/lang/RuntimeException");
            mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Error");
            Label l3 = new Label();
            mv.visitTryCatchBlock(l0, l1, l3, "java/lang/Throwable");
            mv.visitLabel(l0);
            mv.visitLineNumber(29, l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "cn/example/uums/test/a12/$Proxy0", "h", "Ljava/lang/reflect/InvocationHandler;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETSTATIC, "cn/example/uums/test/a12/$Proxy0", "bar", "Ljava/lang/reflect/Method;");
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/reflect/InvocationHandler", "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", true);
            mv.visitVarInsn(ASTORE, 1);
            Label l4 = new Label();
            mv.visitLabel(l4);
            mv.visitLineNumber(30, l4);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
            mv.visitLabel(l1);
            mv.visitInsn(IRETURN);
            mv.visitLabel(l2);
            mv.visitLineNumber(31, l2);
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"});
            mv.visitVarInsn(ASTORE, 1);
            Label l5 = new Label();
            mv.visitLabel(l5);
            mv.visitLineNumber(32, l5);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitInsn(ATHROW);
            mv.visitLabel(l3);
            mv.visitLineNumber(33, l3);
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"});
            mv.visitVarInsn(ASTORE, 1);
            Label l6 = new Label();
            mv.visitLabel(l6);
            mv.visitLineNumber(34, l6);
            mv.visitTypeInsn(NEW, "java/lang/reflect/UndeclaredThrowableException");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>", "(Ljava/lang/Throwable;)V", false);
            mv.visitInsn(ATHROW);
            Label l7 = new Label();
            mv.visitLabel(l7);
            mv.visitLocalVariable("result", "Ljava/lang/Object;", null, l4, l2, 1);
            mv.visitLocalVariable("e", "Ljava/lang/Throwable;", null, l5, l3, 1);
            mv.visitLocalVariable("e", "Ljava/lang/Throwable;", null, l6, l7, 1);
            mv.visitLocalVariable("this", "Lcn/example/uums/test/a12/$Proxy0;", null, l0, l7, 0);
            mv.visitMaxs(4, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock(l0, l1, l2, "java/lang/NoSuchMethodException");
            mv.visitLabel(l0);
            mv.visitLineNumber(42, l0);
            mv.visitLdcInsn(Type.getType("Lcn/example/uums/test/a12/Foo;"));
            mv.visitLdcInsn("foo");
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
            mv.visitFieldInsn(PUTSTATIC, "cn/example/uums/test/a12/$Proxy0", "foo", "Ljava/lang/reflect/Method;");
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitLineNumber(43, l3);
            mv.visitLdcInsn(Type.getType("Lcn/example/uums/test/a12/Foo;"));
            mv.visitLdcInsn("bar");
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
            mv.visitFieldInsn(PUTSTATIC, "cn/example/uums/test/a12/$Proxy0", "bar", "Ljava/lang/reflect/Method;");
            mv.visitLabel(l1);
            mv.visitLineNumber(46, l1);
            Label l4 = new Label();
            mv.visitJumpInsn(GOTO, l4);
            mv.visitLabel(l2);
            mv.visitLineNumber(44, l2);
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/NoSuchMethodException"});
            mv.visitVarInsn(ASTORE, 0);
            Label l5 = new Label();
            mv.visitLabel(l5);
            mv.visitLineNumber(45, l5);
            mv.visitTypeInsn(NEW, "java/lang/NoSuchMethodError");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/NoSuchMethodException", "getMessage", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/NoSuchMethodError", "<init>", "(Ljava/lang/String;)V", false);
            mv.visitInsn(ATHROW);
            mv.visitLabel(l4);
            mv.visitLineNumber(47, l4);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitInsn(RETURN);
            mv.visitLocalVariable("e", "Ljava/lang/NoSuchMethodException;", null, l5, l4, 0);
            mv.visitMaxs(3, 1);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }
}

```

将自己编写的代理类注释掉后执行测试

```java
public class A12 {

    /*interface InvocationHandler {
        Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
    }*/

    public static void main(String[] param) throws Exception {
//        Foo proxy = new $Proxy0(new InvocationHandler() {
//            @Override
//            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable{
//                // 1. 功能增强
//                System.out.println("before...");
//                // 2. 调用目标
////                new Target().foo();
//                return method.invoke(new Target(), args);
//            }
//        });
//        proxy.foo();
//        proxy.bar();
//        /*
//            学到了什么: 代理一点都不难, 无非就是利用了多态、反射的知识
//                1. 方法重写可以增强逻辑, 只不过这【增强逻辑】千变万化, 不能写死在代理内部
//                2. 通过接口回调将【增强逻辑】置于代理类之外
//                3. 配合接口方法反射(也是多态), 就可以再联动调用目标方法
//         */

        byte[] dump = $Proxy0Dump.dump();

        /*FileOutputStream os = new FileOutputStream("$Proxy0.class");
        os.write(dump, 0, dump.length);
        os.close();*/

        ClassLoader loader = new ClassLoader() {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                return super.defineClass(name, dump, 0, dump.length);
            }
        };
        Class<?> proxyClass = loader.loadClass("cn.example.uums.test.a12.$Proxy0");

        Constructor<?> constructor = proxyClass.getConstructor(InvocationHandler.class);
        Foo proxy = (Foo) constructor.newInstance(new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("before...");
                System.out.println("调用目标");
                return null;
            }
        });

        proxy.foo();
    }
}
```



#### cglib 代理

##### 示例

```java
public class CglibProxyDemo {

    static class Target {
        public void foo() {
            System.out.println("target foo");
        }
    }

    // 代理是子类型, 目标是父类型
    public static void main(String[] param) {
//        Target target = new Target();

        Target proxy = (Target) Enhancer.create(Target.class, (MethodInterceptor) (p, method, args, methodProxy) -> {
            System.out.println("before...");
//            Object result = method.invoke(target, args); // 用方法反射调用目标
            // methodProxy 它可以避免反射调用
//            Object result = methodProxy.invoke(target, args); // 内部没有用反射, 需要目标 （spring）
            Object result = methodProxy.invokeSuper(p, args); // 内部没有用反射, 需要代理
            System.out.println("after...");
            return result;
        });

        proxy.foo();

    }
}
```

运行结果与 jdk 动态代理相同



* cglib 不要求目标实现接口，它生成的代理类是目标的子类，因此代理与目标之间是子父关系
* 限制：根据上述分析 final 类无法被 cglib 增强



##### 原理

和 jdk 动态代理原理差不多

1. 回调的接口换了一下，InvocationHandler 改成了 MethodInterceptor
2. 调用目标时有所改进，见下面代码片段
   1. method.invoke 是反射调用，必须调用到足够次数才会进行优化
   2. methodProxy.invoke 是不反射调用，它会正常（间接）调用目标对象的方法（Spring 采用）
   3. methodProxy.invokeSuper 也是不反射调用，它会正常（间接）调用代理对象的方法，可以省略目标对象

```java
public class Target {
    public void save() {
        System.out.println("save()");
    }

    public void save(int i) {
        System.out.println("save(int)");
    }

    public void save(long j) {
        System.out.println("save(long)");
    }
}
```

```java
public class Proxy extends Target {

    private MethodInterceptor methodInterceptor;

    public void setMethodInterceptor(MethodInterceptor methodInterceptor) {
        this.methodInterceptor = methodInterceptor;
    }

    static Method save0;
    static Method save1;
    static Method save2;
    static MethodProxy save0Proxy;
    static MethodProxy save1Proxy;
    static MethodProxy save2Proxy;
    static {
        try {
            save0 = Target.class.getMethod("save");
            save1 = Target.class.getMethod("save", int.class);
            save2 = Target.class.getMethod("save", long.class);
            // name1: 带增强功能的方法名
            // name2: 带原始功能的方法名
            save0Proxy = MethodProxy.create(Target.class, Proxy.class, "()V", "save", "saveSuper");
            save1Proxy = MethodProxy.create(Target.class, Proxy.class, "(I)V", "save", "saveSuper");
            save2Proxy = MethodProxy.create(Target.class, Proxy.class, "(J)V", "save", "saveSuper");
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 带原始功能的方法
    public void saveSuper() {
        super.save();
    }
    public void saveSuper(int i) {
        super.save(i);
    }
    public void saveSuper(long j) {
        super.save(j);
    }
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 带增强功能的方法
    @Override
    public void save() {
        try {
            methodInterceptor.intercept(this, save0, new Object[0], save0Proxy);
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    @Override
    public void save(int i) {
        try {
            methodInterceptor.intercept(this, save1, new Object[]{i}, save1Proxy);
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    @Override
    public void save(long j) {
        try {
            methodInterceptor.intercept(this, save2, new Object[]{j}, save2Proxy);
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }
}
```

```java
public class Application {

    public static void main(String[] args) {
        Proxy proxy = new Proxy();
        Target target = new Target();
        proxy.setMethodInterceptor(new MethodInterceptor() {
            @Override
            public Object intercept(Object p, Method method, Object[] args,
                                    MethodProxy methodProxy) throws Throwable {
                System.out.println("before...");
//                return method.invoke(target, args); // 反射调用
                // MethodProxy 内部使用 FastClass 避免反射调用
//                return methodProxy.invoke(target, args); // 内部无反射, 结合目标用
                return methodProxy.invokeSuper(p, args); // 内部无反射, 结合代理用
            }
        });

        proxy.save();
        proxy.save(1);
        proxy.save(2L);
    }
}
```



> ***注意***
>
> * 调用 Object 的方法, 后两种在 jdk >= 9 时都有问题, 需要 --add-opens java.base/java.lang=ALL-UNNAMED



##### MethodProxy 如何避免反射调用

1. 当调用 MethodProxy 的 invoke 或 invokeSuper 方法时, 会动态生成两个代理类，这两个代理类都继承自 FastClass
   * TargetFastClass 目标类的代理类，配合目标对象使用，直接调用目标类的方法, 避免反射 (Spring 用的这种)
   * ProxyFastClass 代理类的代理类，配合代理对象使用，直接调用代理类的原始功能方法(saveSuper)，避免反射
2. TargetFastClass 记录了 Target 中方法与编号的对应关系
   - save(long) 编号 2
   - save(int) 编号 1
   - save() 编号 0
   - 首先根据方法名和参数个数、类型, 用 switch 和 if 找到这些方法编号
   - 然后再根据编号去调用目标方法, 又用了一大堆 switch 和 if, 但避免了反射
3. ProxyFastClass 记录了 Proxy 中方法与编号的对应关系，不过 Proxy 额外提供了下面几个方法
   * saveSuper(long) 编号 2，不增强，仅是调用 super.save(long)
   * saveSuper(int) 编号 1，不增强, 仅是调用 super.save(int)
   * saveSuper() 编号 0，不增强, 仅是调用 super.save()
   * 查找方式与 TargetFastClass 类似
4. 为什么有这么麻烦的一套东西呢？
   * 避免反射, 提高性能, 代价是一个代理类配两个 FastClass 类, 代理类中还得增加仅调用 super 的一堆方法
   * 用编号处理方法对应关系比较省内存, 另外, 最初获得方法顺序是不确定的, 这个过程没法固定死

```java
package org.springframework.cglib.reflect;

public abstract class FastClass {
    private Class type;

	// ......

    public abstract int getIndex(String var1, Class[] var2);

    public abstract int getIndex(Class[] var1);

    public abstract Object invoke(int var1, Object var2, Object[] var3) throws InvocationTargetException;

    public abstract Object newInstance(int var1, Object[] var2) throws InvocationTargetException;

    public abstract int getIndex(Signature var1);

    public abstract int getMaxIndex();

   // ......
}
```

模拟两个代理类

```java
public class TargetFastClass {
    static Signature s0 = new Signature("save", "()V");
    static Signature s1 = new Signature("save", "(I)V");
    static Signature s2 = new Signature("save", "(J)V");

    // 获取目标方法的编号
    /*
        Target
            save()              0
            save(int)           1
            save(long)          2
        signature 包括方法名字、参数返回值
     */
    public int getIndex(Signature signature) {
        if (s0.equals(signature)) {
            return 0;
        } else if (s1.equals(signature)) {
            return 1;
        } else if (s2.equals(signature)) {
            return 2;
        }
        return -1;
    }

    // 根据方法编号, 正常调用目标对象方法
    public Object invoke(int index, Object target, Object[] args) {
        if (index == 0) {
            ((Target) target).save();
            return null;
        } else if (index == 1) {
            ((Target) target).save((int) args[0]);
            return null;
        } else if (index == 2) {
            ((Target) target).save((long) args[0]);
            return null;
        } else {
            throw new RuntimeException("无此方法");
        }
    }

    public static void main(String[] args) {
        TargetFastClass fastClass = new TargetFastClass();
        int index = fastClass.getIndex(new Signature("save", "(I)V"));
        System.out.println(index);
        fastClass.invoke(index, new Target(), new Object[]{100});
    }
}
```



```java
public class ProxyFastClass {
    static Signature s0 = new Signature("saveSuper", "()V");
    static Signature s1 = new Signature("saveSuper", "(I)V");
    static Signature s2 = new Signature("saveSuper", "(J)V");

    // 获取代理方法的编号
    /*
        Proxy
            saveSuper()              0
            saveSuper(int)           1
            saveSuper(long)          2
        signature 包括方法名字、参数返回值
     */
    public int getIndex(Signature signature) {
        if (s0.equals(signature)) {
            return 0;
        } else if (s1.equals(signature)) {
            return 1;
        } else if (s2.equals(signature)) {
            return 2;
        }
        return -1;
    }

    // 根据方法编号, 正常调用目标对象方法
    public Object invoke(int index, Object proxy, Object[] args) {
        if (index == 0) {
            ((Proxy) proxy).saveSuper();
            return null;
        } else if (index == 1) {
            ((Proxy) proxy).saveSuper((int) args[0]);
            return null;
        } else if (index == 2) {
            ((Proxy) proxy).saveSuper((long) args[0]);
            return null;
        } else {
            throw new RuntimeException("无此方法");
        }
    }

    public static void main(String[] args) {
        ProxyFastClass fastClass = new ProxyFastClass();
        int index = fastClass.getIndex(new Signature("saveSuper", "()V"));
        System.out.println(index);

        fastClass.invoke(index, new Proxy(), new Object[0]);
    }
}
```



##### 与 JDK 动态代理对比



### AOP -Spring

#### 示例

```java
public class Target {
    public void foo(){
        System.out.println("target foo");
    }

    public void bar(){
        System.out.println("target bar");
    }
}
```

```java
@Aspect
public class MyAspect {
    @Before("execution(* foo())")
    public void before() {
        System.out.println("before");
    }

    @After("execution(* foo())")
    public void after() {
        System.out.println("after");
    }
}
```

```java
@Configuration
public class MyConfig {

    // 低级 Advisor 切面
    @Bean
    public Advisor advisor(MethodInterceptor advice){
        // 切点
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* foo())");
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, advice);
        return advisor;
    }

    // 通知
    @Bean
    public MethodInterceptor advice(){
        return invocation -> {
            System.out.println("advice before...");
            Object result = invocation.proceed(); // 调用目标
            System.out.println("advice after...");
            return result;
        };
    }

    @Bean
    public Target tagrget(){
        return new Target();
    }
}
```

```java
public class Application {
    public static void main(String[]args) {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("myConfig", MyConfig.class);
        context.registerBean(ConfigurationClassPostProcessor.class);
        context.registerBean("myAspect", MyAspect.class);
        context.registerBean(AnnotationAwareAspectJAutoProxyCreator.class);
        context.refresh();

        Target proxyTarget = context.getBean(Target.class);
        proxyTarget.foo();
        proxyTarget.bar();
    }
}

// 输出
// advice before...
// before
// target foo
// after
// advice after...
// target bar
```



### JDK 代理 和 cglib 代理在 Spring 中的统一

#### 示例

```java
public interface I1 {
    void foo();

    void bar();
}

public class Target1 implements I1 {
    public void foo() {
        System.out.println("target1 foo");
    }

    public void bar() {
        System.out.println("target1 bar");
    }
}

public class Target2 {
    public void foo() {
        System.out.println("target2 foo");
    }

    public void bar() {
        System.out.println("target2 bar");
    }
}
```

```java
public class Application {
    public static void main(String[] args) {
        /*
            两个切面概念
            aspect =
                通知1(advice) +  切点1(pointcut)
                通知2(advice) +  切点2(pointcut)
                通知3(advice) +  切点3(pointcut)
                ...
            advisor = 更细粒度的切面，包含一个通知和切点
         */

        // 1. 备好切点
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* foo())");
        // 2. 备好通知
        MethodInterceptor advice = invocation -> {
            System.out.println("before...");
            Object result = invocation.proceed(); // 调用目标
            System.out.println("after...");
            return result;
        };
        // 3. 备好切面
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, advice);

        /*
           4. 创建代理
                a. proxyTargetClass = false, 目标实现了接口, 用 jdk 实现
                b. proxyTargetClass = false,  目标没有实现接口, 用 cglib 实现
                c. proxyTargetClass = true, 总是使用 cglib 实现
         */
        ProxyFactory factory = new ProxyFactory();
        factory.addAdvisor(advisor);

        Target1 target1 = new Target1();
        factory.setTarget(target1);
        factory.setInterfaces(target1.getClass().getInterfaces());
        factory.setProxyTargetClass(false);
        I1 proxy1 = (I1) factory.getProxy();
        System.out.println(proxy1.getClass());
        proxy1.foo();
        proxy1.bar();

        factory.setProxyTargetClass(true);
        proxy1 = (I1) factory.getProxy();
        System.out.println(proxy1.getClass());
        proxy1.foo();
        proxy1.bar();

        Target2 target2 = new Target2();
        factory.setTarget(target2);
        factory.setInterfaces(target2.getClass().getInterfaces());
        factory.setProxyTargetClass(false);
        Target2 proxy2 = (Target2) factory.getProxy();
        System.out.println(proxy2.getClass());
        proxy2.foo();
        proxy2.bar();
    }
}
```



#### 切点、通知、切面

Spring 中对切点、通知、切面的抽象如下

* 切点：接口 Pointcut，典型实现 AspectJExpressionPointcut
* 通知：典型接口为 MethodInterceptor 代表环绕通知
* 切面：Advisor，包含一个 Advice 通知，PointcutAdvisor 包含一个 Advice 通知和一个 Pointcut

```mermaid
classDiagram

class Advice
class MethodInterceptor
class Advisor
class PointcutAdvisor

Pointcut <|-- AspectJExpressionPointcut
Advice <|-- MethodInterceptor
Advisor <|-- PointcutAdvisor
PointcutAdvisor o-- "一" Pointcut
PointcutAdvisor o-- "一" Advice

<<interface>> Advice
<<interface>> MethodInterceptor
<<interface>> Pointcut
<<interface>> Advisor
<<interface>> PointcutAdvisor
```

#### 代理相关类图

代理相关类图

* AopProxyFactory 根据 proxyTargetClass 等设置选择 AopProxy 实现
* AopProxy 通过 getProxy 创建代理对象
* 图中 Proxy 都实现了 Advised 接口，能够获得关联的切面集合与目标（其实是从 ProxyFactory 取得）
* 调用代理方法时，会借助 ProxyFactory 将通知统一转为环绕通知：MethodInterceptor

```mermaid
classDiagram

Advised <|-- ProxyFactory
ProxyFactory o-- Target
ProxyFactory o-- "多" Advisor

ProxyFactory --> AopProxyFactory : 使用
AopProxyFactory --> AopProxy
Advised <|-- 基于CGLIB的Proxy
基于CGLIB的Proxy <-- ObjenesisCglibAopProxy : 创建
AopProxy <|-- ObjenesisCglibAopProxy
AopProxy <|-- JdkDynamicAopProxy
基于JDK的Proxy <-- JdkDynamicAopProxy : 创建
Advised <|-- 基于JDK的Proxy

class AopProxy {
   +getProxy() Object
}

class ProxyFactory {
	proxyTargetClass : boolean
}

class ObjenesisCglibAopProxy {
	advised : ProxyFactory
}

class JdkDynamicAopProxy {
	advised : ProxyFactory
}

<<interface>> Advised
<<interface>> AopProxyFactory
<<interface>> AopProxy
```



#### 总结

1. 底层的切点实现
2. 底层的通知实现
3. 底层的切面实现
4. ProxyFactory 用来创建代理
   * 如果指定了接口，且 proxyTargetClass = false，使用 JdkDynamicAopProxy
   * 如果没有指定接口，或者 proxyTargetClass = true，使用 ObjenesisCglibAopProxy
     * 例外：如果目标是接口类型或已经是 Jdk 代理，使用 JdkDynamicAopProxy

> ***注意***
>
> * 要区分本章节提到的 MethodInterceptor(org.aopalliance.intercept)，它与之前 cglib 中用的的 MethodInterceptor 是不同的接口



### 切点匹配

#### 示例

```java
public class T1 {
    @Transactional
    public void foo() {
    }
    public void bar() {
    }
}

@Transactional
public class T2 {
    public void foo() {
    }
}

@Transactional
public interface I3 {
    void foo();
}
public class T3 implements I3 {
    public void foo() {
    }
}
```

```java
public class Application {
    public static void main(String[] args) throws NoSuchMethodException {
        // 根据方法名匹配方法
//        AspectJExpressionPointcut pt1 = new AspectJExpressionPointcut();
//        pt1.setExpression("execution(* bar())");
//        System.out.println(pt1.matches(T1.class.getMethod("foo"), T1.class));
//        System.out.println(pt1.matches(T1.class.getMethod("bar"), T1.class));

        // 根据方法注解匹配方法
//        AspectJExpressionPointcut pt2 = new AspectJExpressionPointcut();
//        pt2.setExpression("@annotation(org.springframework.transaction.annotation.Transactional)");
//        System.out.println(pt2.matches(T1.class.getMethod("foo"), T1.class));
//        System.out.println(pt2.matches(T1.class.getMethod("bar"), T1.class));

        // @Transactional 以上两种方法均不是事务实现的方式。
        // 因为他们只能根据方法名或者方法上的注解匹配，
        // 而 @Transactional 可以加在类或接口上，此时使用切点表达式无法进行匹配
        // 下面模拟事务增强的匹配
        StaticMethodMatcherPointcut pt3 = new StaticMethodMatcherPointcut() {
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                // 检查方法上是否加了 Transactional 注解
                MergedAnnotations annotations = MergedAnnotations.from(method);
                if (annotations.isPresent(Transactional.class)) {
                    return true;
                }
                // 查看类上是否加了 Transactional 注解
                // 默认查找本类上的注解
                // MergedAnnotations.SearchStrategy.TYPE_HIERARCHY 本类、父类上的注解都查找
                annotations = MergedAnnotations.from(targetClass, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY);
                if (annotations.isPresent(Transactional.class)) {
                    return true;
                }
                return false;
            }
        };

        System.out.println(pt3.matches(T1.class.getMethod("foo"), T1.class));
        System.out.println(pt3.matches(T1.class.getMethod("bar"), T1.class));
        System.out.println(pt3.matches(T2.class.getMethod("foo"), T2.class));
        System.out.println(pt3.matches(T3.class.getMethod("foo"), T3.class));
    }
}
```



#### 总结

1. 常见 aspectj 切点用法
2. 底层切点实现是如何匹配的: 调用了 aspectj 的匹配方法
3. 比较关键的是它实现了 MethodMatcher 接口, 用来执行方法的匹配
4. aspectj 切点的局限性，实际的 @Transactional 切点实现



### 代理对象的创建时机

#### 示例

```java
public class Target1 {
    public void foo() {

    }

    public Target1() {
        System.out.println("Target1()");
    }

//    @Autowired
//    public void setTarget1(Target1 target1) {
//        System.out.println("Target2 setTarget1(target1) class is: " + target1.getClass());
//    }

    @PostConstruct
    public void init() {
        System.out.println("Target1 init()");
    }
}

public class Target2 {
    public Target2() {
        System.out.println("Target2()");
    }

    @Autowired
    public void setTarget1(Target1 target1) {
        System.out.println("Target2 setTarget1(target1) class is: " + target1.getClass());
    }

    @PostConstruct
    public void init() {
        System.out.println("Target2 init()");
    }
}
```



```java
@Configuration
public class MyConfig {

    @Bean // 解析 @Aspect、产生代理
    public AnnotationAwareAspectJAutoProxyCreator annotationAwareAspectJAutoProxyCreator() {
        return new AnnotationAwareAspectJAutoProxyCreator();
    }

    @Bean // 解析 @Autowired
    public AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor() {
        return new AutowiredAnnotationBeanPostProcessor();
    }

    @Bean // 解析 @PostConstruct
    public CommonAnnotationBeanPostProcessor commonAnnotationBeanPostProcessor() {
        return new CommonAnnotationBeanPostProcessor();
    }

    @Bean
    public Advisor advisor(MethodInterceptor advice) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* foo())");
        return new DefaultPointcutAdvisor(pointcut, advice);
    }

    @Bean
    public MethodInterceptor advice() {
        return (MethodInvocation invocation) -> {
            System.out.println("before...");
            return invocation.proceed();
        };
    }

    @Bean
    public Target1 target1() {
        return new Target1();
    }

    @Bean
    public Target2 target2() {
        return new Target2();
    }

}
```

```java
public class Application {
    public static void main(String[]args){
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean(ConfigurationClassPostProcessor.class);
        context.registerBean(MyConfig.class);
        context.refresh();
        context.close();
        // 
        /*
            学到了什么
                a. 代理的创建时机
                    1. 初始化之后 (无循环依赖时)
                    2. 实例创建后, 依赖注入前 (有循环依赖时), 并暂存于二级缓存
                b. 依赖注入与初始化不应该被增强, 仍应被施加于原始对象
         */
    }
}
```



#### 总结

创建 -> (`*`) 依赖注入 -> 初始化 (`*`)

1. 代理的创建时机
   * 初始化之后 (无循环依赖时)
   * 实例创建后, 依赖注入前 (有循环依赖时), 并暂存于二级缓存
2. 依赖注入与初始化不应该被增强, 仍应被施加于原始对象



### 切面的顺序控制

#### 示例

```java
public class Target {
    public void foo(){
        System.out.println("target foo");
    }
}

// 高级 @Aspect 切面
@Aspect
@Order(1)
public class Aspect1 {
    @Before("execution(* foo())")
    public void before(){
        System.out.println("Aspect1 before");
    }

    @After("execution(* foo())")
    public void after(){
        System.out.println("Aspect1 after");
    }
}

```

```java
@Configuration
public class MyConfig {

    // 低级 Advisor 切面
    @Bean
    public Advisor advisor(MethodInterceptor advice){
        // 切点
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* foo())");
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, advice);
        advisor.setOrder(2);
        return advisor;
    }

    // 通知
    @Bean
    public MethodInterceptor advice(){
        return invocation -> {
            System.out.println("advice before...");
            Object result = invocation.proceed(); // 调用目标
            System.out.println("advice after...");
            return result;
        };
    }

    @Bean
    public Target tagrget(){
        return new Target();
    }
}
```

```java
public class Application {
    public static void main(String[]args){
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("myConfig", MyConfig.class);
        context.registerBean(ConfigurationClassPostProcessor.class);
        context.registerBean("aspect1", Aspect1.class);
        context.registerBean(AnnotationAwareAspectJAutoProxyCreator.class);
        context.refresh();

        Target proxy1 = context.getBean(Target.class);
        proxy1.foo();
    }
}
```



#### 总结

1.默认情况下低级的 advisor 比高级的 aspect 优先级高

2.可以在切面类上使用 `@Order` 注解指定该切面类所有 aspect  切面的优先级，advisor 切面可以调用 `setOrder` 方法设置。数值越小优先级越高

3.`@Order`加在切面方法上是无效的

### 后处理器 AnnotationAwareAspectJAutoProxyCreator

该后处理器的两个作用

1. 找到 Bean 中的 Advisor 切面和 Aspect 切面，并将找到的 Aspect 切面转换为 Advisor 切面
2. 在合适的时机创建代理



#### 示例

```java
public class Target {
    public void foo(){
        System.out.println("target foo");
    }

    public void bar(){
        System.out.println("target bar");
    }
}
```

```java
@Aspect
public class MyAspect {
    @Before("execution(* foo())")
    public void before() {
        System.out.println("before");
    }

    @After("execution(* foo())")
    public void after() {
        System.out.println("after");
    }
}
```

```java
@Configuration
public class MyConfig {

    // 低级 Advisor 切面
    @Bean
    public Advisor advisor(MethodInterceptor advice){
        // 切点
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* foo())");
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, advice);
        return advisor;
    }

    // 通知
    @Bean
    public MethodInterceptor advice(){
        return invocation -> {
            System.out.println("advice before...");
            Object result = invocation.proceed(); // 调用目标
            System.out.println("advice after...");
            return result;
        };
    }
}
```

```java
// 包名为了方便调用 protected 方法 findEligibleAdvisors、wrapIfNecessary
package org.springframework.aop.framework.autoproxy;

public class Application {
    public static void main(String[]args) {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("myConfig", MyConfig.class);
        context.registerBean(ConfigurationClassPostProcessor.class);
        context.registerBean("myAspect", MyAspect.class);
        context.registerBean(AnnotationAwareAspectJAutoProxyCreator.class);
        context.refresh();

        /**
         * 第一个重要的方法 findEligibleAdvisors 找到有资格的 Advisors
         * 1.有资格的 Advisors 一部分是低级的，可以由自己编写，如例中的 advisor
         * 2.有资格的 Advisors 另一部分是高级的，由 AnnotationAwareAspectJAutoProxyCreator 解析 @Aspect 获得
         */
        AnnotationAwareAspectJAutoProxyCreator creator = context.getBean(AnnotationAwareAspectJAutoProxyCreator.class);
        // 获得和 Target 类方法有资格的 Advisor
        List<Advisor> advisors = creator.findEligibleAdvisors(Target.class, "target");
        /**
         * 获得四个切面
         * 1.Spring 为所有 Bean 创建的切面
         * 2. 两个 @Aspect 切面
         * 3. 一个 低级 Advisor 切面
         */
        for(Advisor advisor:advisors){
            System.out.println(advisor);
        }

        // 第二个重要的方法 wrapIfNecessary 内部调用 findEligibleAdvisors 检查对象是否有切面并创建代理
        Target proxy = (Target) creator.wrapIfNecessary(new Target(), "target", "target");
        System.out.println(proxy.getClass());
        proxy.foo();
    }
}
```



#### 方法一：findEligibleAdvisors

findEligibleAdvisors 找到有资格的 Advisors

* 有资格的 Advisors 一部分是低级的，可以由自己编写，如例中的 advisor
* 有资格的 Advisors 另一部分是高级的，由 AnnotationAwareAspectJAutoProxyCreator 解析 @Aspect 获得

过程如下

1. 找到所有 Advisor 切面
2. 找到所有 Aspect 切面并转换为 Advisor 切面
3. 找到适合当前 Bean 的所有 Advisor 切面



##### 源码

```java
public abstract class AbstractAdvisorAutoProxyCreator extends AbstractAutoProxyCreator {
    protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
        /**
         * 调用子类方法
         * 找到所有 Advisor 切面
         * 找到所有 Aspect 切面并转换为 Advisor 切面
         */
        List<Advisor> candidateAdvisors = this.findCandidateAdvisors();
        // 找到适合当前 Bean 的所有 Advisor 切面
        List<Advisor> eligibleAdvisors = this.findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);
        this.extendAdvisors(eligibleAdvisors);
        if (!eligibleAdvisors.isEmpty()) {
            eligibleAdvisors = this.sortAdvisors(eligibleAdvisors);
        }

        return eligibleAdvisors;
    }
}
```


###### 找到所有 Advisor 切面

该方法从 BeanFactory 中获取所有 Advisor 类型的对象

```java
public class BeanFactoryAdvisorRetrievalHelper {
    public List<Advisor> findAdvisorBeans() {
        String[] advisorNames = this.cachedAdvisorBeanNames;
        if (advisorNames == null) {
            advisorNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this.beanFactory, Advisor.class, true, false);
            this.cachedAdvisorBeanNames = advisorNames;
        }

        if (advisorNames.length == 0) {
            return new ArrayList();
        } else {
            List<Advisor> advisors = new ArrayList();
            String[] var3 = advisorNames;
            int var4 = advisorNames.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String name = var3[var5];
                if (this.isEligibleBean(name)) {
                    if (this.beanFactory.isCurrentlyInCreation(name)) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Skipping currently created advisor '" + name + "'");
                        }
                    } else {
                        try {
                            advisors.add(this.beanFactory.getBean(name, Advisor.class));
                        } catch (BeanCreationException var11) {
                            Throwable rootCause = var11.getMostSpecificCause();
                            if (rootCause instanceof BeanCurrentlyInCreationException) {
                                BeanCreationException bce = (BeanCreationException)rootCause;
                                String bceBeanName = bce.getBeanName();
                                if (bceBeanName != null && this.beanFactory.isCurrentlyInCreation(bceBeanName)) {
                                    if (logger.isTraceEnabled()) {
                                        logger.trace("Skipping advisor '" + name + "' with dependency on currently created bean: " + var11.getMessage());
                                    }
                                    continue;
                                }
                            }

                            throw var11;
                        }
                    }
                }
            }

            return advisors;
        }
    }
}
```



###### 找到所有 Aspect 切面并转换为 Advisor 切面

1. 从 BeanFactory 中获取所有对象
2. 判断是否为 Aspect 切面对象
3. 如果是 Aspect 切面对象就使用 Aspect 切面方法的信息生成 Advisor 切面



```java
public class BeanFactoryAspectJAdvisorsBuilder {
    public List<Advisor> buildAspectJAdvisors() {
        List<String> aspectNames = this.aspectBeanNames;
        if (aspectNames == null) {
            synchronized(this) {
                aspectNames = this.aspectBeanNames;
                if (aspectNames == null) {
                    List<Advisor> advisors = new ArrayList();
                    List<String> aspectNames = new ArrayList();
                    String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this.beanFactory, Object.class, true, false);
                    String[] var18 = beanNames;
                    int var19 = beanNames.length;

                    for(int var7 = 0; var7 < var19; ++var7) {
                        String beanName = var18[var7];
                        if (this.isEligibleBean(beanName)) {
                            Class<?> beanType = this.beanFactory.getType(beanName, false);
                            if (beanType != null && this.advisorFactory.isAspect(beanType)) {
                                aspectNames.add(beanName);
                                AspectMetadata amd = new AspectMetadata(beanType, beanName);
                                if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
                                    MetadataAwareAspectInstanceFactory factory = new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);
                                    List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);
                                    if (this.beanFactory.isSingleton(beanName)) {
                                        this.advisorsCache.put(beanName, classAdvisors);
                                    } else {
                                        this.aspectFactoryCache.put(beanName, factory);
                                    }

                                    advisors.addAll(classAdvisors);
                                } else {
                                    if (this.beanFactory.isSingleton(beanName)) {
                                        throw new IllegalArgumentException("Bean with name '" + beanName + "' is a singleton, but aspect instantiation model is not singleton");
                                    }

                                    MetadataAwareAspectInstanceFactory factory = new PrototypeAspectInstanceFactory(this.beanFactory, beanName);
                                    this.aspectFactoryCache.put(beanName, factory);
                                    advisors.addAll(this.advisorFactory.getAdvisors(factory));
                                }
                            }
                        }
                    }

                    this.aspectBeanNames = aspectNames;
                    return advisors;
                }
            }
        }

        if (aspectNames.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<Advisor> advisors = new ArrayList();
            Iterator var3 = aspectNames.iterator();

            while(var3.hasNext()) {
                String aspectName = (String)var3.next();
                List<Advisor> cachedAdvisors = (List)this.advisorsCache.get(aspectName);
                if (cachedAdvisors != null) {
                    advisors.addAll(cachedAdvisors);
                } else {
                    MetadataAwareAspectInstanceFactory factory = (MetadataAwareAspectInstanceFactory)this.aspectFactoryCache.get(aspectName);
                    advisors.addAll(this.advisorFactory.getAdvisors(factory));
                }
            }

            return advisors;
        }
    }
}
```



###### 找到适合当前 Bean 的所有 Advisor 切面

1. 将所有 Advisor 切面与目标 Bean 进行匹配，获取目标 Bean 的 Advisor 切面



```java
public abstract class AopUtils {
    public static List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> clazz) {
        if (candidateAdvisors.isEmpty()) {
            return candidateAdvisors;
        } else {
            List<Advisor> eligibleAdvisors = new ArrayList();
            Iterator var3 = candidateAdvisors.iterator();

            while(var3.hasNext()) {
                Advisor candidate = (Advisor)var3.next();
                if (candidate instanceof IntroductionAdvisor && canApply(candidate, clazz)) {
                    eligibleAdvisors.add(candidate);
                }
            }

            boolean hasIntroductions = !eligibleAdvisors.isEmpty();
            Iterator var7 = candidateAdvisors.iterator();

            while(var7.hasNext()) {
                Advisor candidate = (Advisor)var7.next();
                if (!(candidate instanceof IntroductionAdvisor) && canApply(candidate, clazz, hasIntroductions)) {
                    eligibleAdvisors.add(candidate);
                }
            }

            return eligibleAdvisors;
        }
    }
}
```



##### 示例

```java
public class Target1 {
    public void foo(){
        System.out.println("target1 foo");
    }
}

public class Target2 {
    public void bar(){
        System.out.println("target2 bar");
    }
}
```

```java
@Aspect
public class MyAspect {
    @Before("execution(* foo())")
    public void fooBefore() {
        System.out.println("foo before");
    }

    @After("execution(* foo())")
    public void fooAfter() {
        System.out.println("foo after");
    }

    @Before("execution(* bar())")
    public void barBefore() {
        System.out.println("bar before");
    }

    @After("execution(* bar())")
    public void barAfter() {
        System.out.println("bar after");
    }
}
```

```java
@Configuration
public class MyConfig {

    // 低级 Advisor 切面
    @Bean
    public Advisor fooAdvisor(MethodInterceptor advice){
        // 切点
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* foo())");
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, advice);
        return advisor;
    }

    // 低级 Advisor 切面
    @Bean
    public Advisor barAdvisor(MethodInterceptor advice){
        // 切点
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* bar())");
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, advice);
        return advisor;
    }

    // 通知
    @Bean
    public MethodInterceptor advice(){
        return invocation -> {
            System.out.println("advice before...");
            Object result = invocation.proceed(); // 调用目标
            System.out.println("advice after...");
            return result;
        };
    }
}
```



```java
public class Application {
    public static void main(String[]args) {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("myConfig", MyConfig.class);
        context.registerBean(ConfigurationClassPostProcessor.class);
        context.registerBean("myAspect", MyAspect.class);
        context.registerBean(AnnotationAwareAspectJAutoProxyCreator.class);
        context.refresh();

        List<Advisor> candidateAdvisors = findAdvisorBeans(context.getDefaultListableBeanFactory());
        candidateAdvisors.addAll(buildAspectJAdvisors());
        List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, Target1.class);

        System.out.println("排序");
        AnnotationAwareOrderComparator.sort(eligibleAdvisors);
        printAdvisor(eligibleAdvisors);
    }

    private static List<Advisor> findAdvisorBeans(DefaultListableBeanFactory beanFactory){
        System.out.println("找到所有 Advisor 切面");
        List<Advisor> candidateAdvisors = new ArrayList<>(beanFactory.getBeansOfType(Advisor.class).values());
        printAdvisor(candidateAdvisors);
        return candidateAdvisors;
    }

    private static List<Advisor> buildAspectJAdvisors(){
        System.out.println("找到所有 Aspect 切面并转换为 Advisor 切面");
        AspectInstanceFactory factory = new SingletonAspectInstanceFactory(new MyAspect());
        List<Advisor> aspectAdvisors = new ArrayList<>();
        for(Method method:MyAspect.class.getDeclaredMethods()){
            /**
             *   @Before 前置通知会被转换为下面原始的 AspectJMethodBeforeAdvice 形式, 该对象包含了如下信息
             *       a. 通知代码从哪儿来
             *       b. 切点是什么
             *       c. 通知对象如何创建, 本例共用同一个 Aspect 对象
             *   类似的通知还有
             *       1. AspectJAroundAdvice (环绕通知)
             *       2. AspectJAfterReturningAdvice
             *       3. AspectJAfterThrowingAdvice (环绕通知)
             *       4. AspectJAfterAdvice (环绕通知)
             */
            if (method.isAnnotationPresent(Before.class)) {
                // 解析切点
                AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
                pointcut.setExpression(method.getAnnotation(Before.class).value());
                // 通知类
                // 切面
                Advisor advisor = new DefaultPointcutAdvisor(pointcut, new AspectJMethodBeforeAdvice(method, pointcut, factory));
                aspectAdvisors.add(advisor);
            } else if (method.isAnnotationPresent(After.class)) {
                AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
                pointcut.setExpression(method.getAnnotation(After.class).value());
                Advisor advisor = new DefaultPointcutAdvisor(pointcut,  new AspectJAfterAdvice(method, pointcut, factory));
                aspectAdvisors.add(advisor);
            } else if (method.isAnnotationPresent(AfterReturning.class)) {
                AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
                pointcut.setExpression(method.getAnnotation(AfterReturning.class).value());
                Advisor advisor = new DefaultPointcutAdvisor(pointcut,  new AspectJAfterReturningAdvice(method, pointcut, factory));
                aspectAdvisors.add(advisor);
            } else if (method.isAnnotationPresent(Around.class)) {
                AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
                pointcut.setExpression(method.getAnnotation(Around.class).value());
                Advisor advisor = new DefaultPointcutAdvisor(pointcut, new AspectJAroundAdvice(method, pointcut, factory));
                aspectAdvisors.add(advisor);
            }
        }
        printAdvisor(aspectAdvisors);
        return aspectAdvisors;
    }

    private static List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> clazz){
        System.out.println("找到适合当前 Bean 的所有 Advisor 切面");
        List<Advisor> eligibleAdvisors = new ArrayList<>();
        for (Advisor advisor : candidateAdvisors) {
            for(Method method:Target1.class.getDeclaredMethods()){
                if(((PointcutAdvisor)advisor).getPointcut().getMethodMatcher().matches(method, clazz)){
                    eligibleAdvisors.add(advisor);
                    break;
                }
            }
        }
        printAdvisor(eligibleAdvisors);
        return eligibleAdvisors;
    }

    private static void printAdvisor(List<Advisor> advisors){
        for(Advisor advisor:advisors){
            System.out.println(advisor);
        }
    }
}
```



##### 总结

1. @Before 前置通知会被转换为原始的 AspectJMethodBeforeAdvice 形式, 该对象包含了如下信息
   1. 通知代码从哪儿来
   2. 切点是什么(这里为啥要切点, 后面解释)
   3. 通知对象如何创建, 本例共用同一个 Aspect 对象
2. 类似的还有
   1. AspectJAroundAdvice (环绕通知)
   2. AspectJAfterReturningAdvice
   3. AspectJAfterThrowingAdvice (环绕通知)
   4. AspectJAfterAdvice (环绕通知)



#### 方法二：wrapIfNecessary

* 它内部调用 findEligibleAdvisors, 只要返回集合不空, 则表示需要创建代理
* 它的调用时机通常在原始对象初始化后执行, 但碰到循环依赖会提前至依赖注入之前执行



##### 源码

```java
public abstract class AbstractAutoProxyCreator extends ProxyProcessorSupport implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware {
    protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
        if (StringUtils.hasLength(beanName) && this.targetSourcedBeans.contains(beanName)) {
            return bean;
        } else if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
            return bean;
        } else if (!this.isInfrastructureClass(bean.getClass()) && !this.shouldSkip(bean.getClass(), beanName)) {
            // 调用 findEligibleAdvisors
            Object[] specificInterceptors = this.getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, (TargetSource)null);
            if (specificInterceptors != DO_NOT_PROXY) {
                this.advisedBeans.put(cacheKey, Boolean.TRUE);
                Object proxy = this.createProxy(bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
                this.proxyTypes.put(cacheKey, proxy.getClass());
                return proxy;
            } else {
                this.advisedBeans.put(cacheKey, Boolean.FALSE);
                return bean;
            }
        } else {
            this.advisedBeans.put(cacheKey, Boolean.FALSE);
            return bean;
        }
    }
    
    protected Object createProxy(Class<?> beanClass, @Nullable String beanName, @Nullable Object[] specificInterceptors, TargetSource targetSource) {
        if (this.beanFactory instanceof ConfigurableListableBeanFactory) {
            AutoProxyUtils.exposeTargetClass((ConfigurableListableBeanFactory)this.beanFactory, beanName, beanClass);
        }

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.copyFrom(this);
        if (!proxyFactory.isProxyTargetClass()) {
            if (this.shouldProxyTargetClass(beanClass, beanName)) {
                proxyFactory.setProxyTargetClass(true);
            } else {
                this.evaluateProxyInterfaces(beanClass, proxyFactory);
            }
        }

        Advisor[] advisors = this.buildAdvisors(beanName, specificInterceptors);
        proxyFactory.addAdvisors(advisors);
        proxyFactory.setTargetSource(targetSource);
        this.customizeProxyFactory(proxyFactory);
        proxyFactory.setFrozen(this.freezeProxy);
        if (this.advisorsPreFiltered()) {
            proxyFactory.setPreFiltered(true);
        }

        return proxyFactory.getProxy(this.getProxyClassLoader());
    }
}
```



##### 示例

```java
public class Target1 {
    public void foo(){
        System.out.println("target1 foo");
    }
}
```

```java
public class Target2 {
    public void bar(){
        System.out.println("target2 bar");
    }
}
```

```java
@Aspect
public class MyAspect {
    @Before("execution(* foo())")
    public void fooBefore() {
        System.out.println("foo before");
    }

    @After("execution(* foo())")
    public void fooAfter() {
        System.out.println("foo after");
    }

    @Before("execution(* bar())")
    public void barBefore() {
        System.out.println("bar before");
    }

    @After("execution(* bar())")
    public void barAfter() {
        System.out.println("bar after");
    }
}
```

```java
@Configuration
public class MyConfig {

    // 低级 Advisor 切面
    @Bean
    public Advisor fooAdvisor(MethodInterceptor advice){
        // 切点
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* foo())");
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, advice);
        return advisor;
    }

    // 低级 Advisor 切面
    @Bean
    public Advisor barAdvisor(MethodInterceptor advice){
        // 切点
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* bar())");
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, advice);
        return advisor;
    }

    // 通知
    @Bean
    public MethodInterceptor advice(){
        return invocation -> {
            System.out.println("advice before...");
            Object result = invocation.proceed(); // 调用目标
            System.out.println("advice after...");
            return result;
        };
    }
}
```

```java
public class Application {
    public static void main(String[]args) {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("myConfig", MyConfig.class);
        context.registerBean(ConfigurationClassPostProcessor.class);
        context.registerBean("myAspect", MyAspect.class);
        context.registerBean(AnnotationAwareAspectJAutoProxyCreator.class);
        context.refresh();
        Target1 proxy1 = (Target1) Application.wrapIfNecessary(new Target1(), context.getDefaultListableBeanFactory());

        proxy1.foo();
    }

    /**
     * 创建代理
     *  a. proxyTargetClass = false, 目标实现了接口, 用 jdk 实现
     *  b. proxyTargetClass = false,  目标没有实现接口, 用 cglib 实现
     *  c. proxyTargetClass = true, 总是使用 cglib 实现
     */
    private static Object wrapIfNecessary(Object bean, DefaultListableBeanFactory beanFactory){
        List<Advisor> eligibleAdvisors = Application.findEligibleAdvisors(bean.getClass(), beanFactory);
        ProxyFactory factory = new ProxyFactory();
        factory.addAdvice(ExposeInvocationInterceptor.INSTANCE);
        factory.addAdvisors(eligibleAdvisors);

        factory.setTarget(bean);
        factory.setInterfaces(bean.getClass().getInterfaces());
        factory.setProxyTargetClass(false);
        return factory.getProxy(Thread.currentThread().getContextClassLoader());
    }

    private static List<Advisor> findEligibleAdvisors(Class<?> clazz, DefaultListableBeanFactory beanFactory){
        List<Advisor> candidateAdvisors = findAdvisorBeans(beanFactory);
        candidateAdvisors.addAll(buildAspectJAdvisors());
        return findAdvisorsThatCanApply(candidateAdvisors, clazz);
    }

    private static List<Advisor> findAdvisorBeans(DefaultListableBeanFactory beanFactory){
        System.out.println("找到所有 Advisor 切面");
        List<Advisor> candidateAdvisors = new ArrayList<>(beanFactory.getBeansOfType(Advisor.class).values());
        printAdvisor(candidateAdvisors);
        return candidateAdvisors;
    }

    private static List<Advisor> buildAspectJAdvisors(){
        System.out.println("找到所有 Aspect 切面并转换为 Advisor 切面");
        AspectInstanceFactory factory = new SingletonAspectInstanceFactory(new MyAspect());
        List<Advisor> aspectAdvisors = new ArrayList<>();
        for(Method method:MyAspect.class.getDeclaredMethods()){
            /**
             *   @Before 前置通知会被转换为下面原始的 AspectJMethodBeforeAdvice 形式, 该对象包含了如下信息
             *       a. 通知代码从哪儿来
             *       b. 切点是什么
             *       c. 通知对象如何创建, 本例共用同一个 Aspect 对象
             *   类似的通知还有
             *       1. AspectJAroundAdvice (环绕通知)
             *       2. AspectJAfterReturningAdvice
             *       3. AspectJAfterThrowingAdvice (环绕通知)
             *       4. AspectJAfterAdvice (环绕通知)
             */
            if (method.isAnnotationPresent(Before.class)) {
                // 解析切点
                AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
                pointcut.setExpression(method.getAnnotation(Before.class).value());
                // 通知类
                // 切面
                Advisor advisor = new DefaultPointcutAdvisor(pointcut, new AspectJMethodBeforeAdvice(method, pointcut, factory));
                aspectAdvisors.add(advisor);
            } else if (method.isAnnotationPresent(After.class)) {
                AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
                pointcut.setExpression(method.getAnnotation(After.class).value());
                Advisor advisor = new DefaultPointcutAdvisor(pointcut,  new AspectJAfterAdvice(method, pointcut, factory));
                aspectAdvisors.add(advisor);
            } else if (method.isAnnotationPresent(AfterReturning.class)) {
                AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
                pointcut.setExpression(method.getAnnotation(AfterReturning.class).value());
                Advisor advisor = new DefaultPointcutAdvisor(pointcut,  new AspectJAfterReturningAdvice(method, pointcut, factory));
                aspectAdvisors.add(advisor);
            } else if (method.isAnnotationPresent(Around.class)) {
                AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
                pointcut.setExpression(method.getAnnotation(Around.class).value());
                Advisor advisor = new DefaultPointcutAdvisor(pointcut, new AspectJAroundAdvice(method, pointcut, factory));
                aspectAdvisors.add(advisor);
            }
        }
        printAdvisor(aspectAdvisors);
        return aspectAdvisors;
    }

    private static List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> clazz){
        System.out.println("找到适合当前 Bean 的所有 Advisor 切面");
        List<Advisor> eligibleAdvisors = new ArrayList<>();
        for (Advisor advisor : candidateAdvisors) {
            for(Method method:Target1.class.getDeclaredMethods()){
                if(((PointcutAdvisor)advisor).getPointcut().getMethodMatcher().matches(method, clazz)){
                    eligibleAdvisors.add(advisor);
                    break;
                }
            }
        }
        printAdvisor(eligibleAdvisors);
        return eligibleAdvisors;
    }

    private static void printAdvisor(List<Advisor> advisors){
        for(Advisor advisor:advisors){
            System.out.println(advisor);
        }
    }
}
```



### 静态通知调用

代理对象调用流程如下（以 JDK 动态代理实现为例）

* 从 ProxyFactory 获得 Target 和环绕通知链，根据他俩创建 MethodInvocation，简称 mi
* 首次执行 mi.proceed() 发现有下一个环绕通知，调用它的 invoke(mi)
* 进入环绕通知1，执行前增强，再次调用 mi.proceed() 发现有下一个环绕通知，调用它的 invoke(mi)
* 进入环绕通知2，执行前增强，调用 mi.proceed() 发现没有环绕通知，调用 mi.invokeJoinPoint() 执行目标方法
* 目标方法执行结束，将结果返回给环绕通知2，执行环绕通知2 的后增强
* 环绕通知2继续将结果返回给环绕通知1，执行环绕通知1 的后增强
* 环绕通知1返回最终的结果

图中不同颜色对应一次环绕通知或目标的调用起始至终结

```mermaid
sequenceDiagram
participant Proxy
participant ih as InvocationHandler
participant mi as MethodInvocation
participant Factory as ProxyFactory
participant mi1 as MethodInterceptor1
participant mi2 as MethodInterceptor2
participant Target

Proxy ->> +ih : invoke()
ih ->> +Factory : 获得 Target
Factory -->> -ih : 
ih ->> +Factory : 获得 MethodInterceptor 链
Factory -->> -ih : 
ih ->> +mi : 创建 mi
mi -->> -ih : 
rect rgb(200, 223, 255)
ih ->> +mi : mi.proceed()
mi ->> +mi1 : invoke(mi)
mi1 ->> mi1 : 前增强
rect rgb(200, 190, 255)
mi1 ->> mi : mi.proceed()
mi ->> +mi2 : invoke(mi)
mi2 ->> mi2 : 前增强
rect rgb(150, 190, 155)
mi2 ->> mi : mi.proceed()
mi ->> +Target : mi.invokeJoinPoint()
Target ->> Target : 
Target -->> -mi2 : 结果
end
mi2 ->> mi2 : 后增强
mi2 -->> -mi1 : 结果
end
mi1 ->> mi1 : 后增强
mi1 -->> -mi : 结果
mi -->> -ih : 
end
ih -->> -Proxy : 
```



#### 通知调用过程

##### 示例

```java
public class Target1{
    public void foo(){
        System.out.println("target1 foo");
    }
}
public class Target2 {
    public void bar(){
        System.out.println("target2 bar");
    }
}

```

```java
@Aspect
public class MyAspect {
    @Before("execution(* foo())")
    public void fooBefore() {
        System.out.println("foo before");
    }

    @After("execution(* foo())")
    public void fooAfter() {
        System.out.println("foo after");
    }

    @Before("execution(* bar())")
    public void barBefore() {
        System.out.println("bar before");
    }

    @After("execution(* bar())")
    public void barAfter() {
        System.out.println("bar after");
    }
}
```

```java
@Configuration
public class MyConfig {

    // 低级 Advisor 切面
    @Bean
    public Advisor fooAdvisor(MethodInterceptor advice){
        // 切点
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* foo())");
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, advice);
        return advisor;
    }

    // 低级 Advisor 切面
    @Bean
    public Advisor barAdvisor(MethodInterceptor advice){
        // 切点
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* bar())");
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, advice);
        return advisor;
    }

    // 通知
    @Bean
    public MethodInterceptor advice(){
        return invocation -> {
            System.out.println("advice before...");
            Object result = invocation.proceed(); // 调用目标
            System.out.println("advice after...");
            return result;
        };
    }
}
```

```java
package org.springframework.aop.framework;

public class Application {
    public static void main(String[]args) throws Throwable {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("myConfig", MyConfig.class);
        context.registerBean(ConfigurationClassPostProcessor.class);
        context.registerBean("myAspect", MyAspect.class);
        context.registerBean(AnnotationAwareAspectJAutoProxyCreator.class);
        context.refresh();
        Target1 target1 = new Target1();

        List<Advisor> eligibleAdvisors = findEligibleAdvisors(target1.getClass(), context.getDefaultListableBeanFactory());

        ProxyFactory factory = new ProxyFactory();
        factory.addAdvice(ExposeInvocationInterceptor.INSTANCE);
        factory.addAdvisors(eligibleAdvisors);

        factory.setTarget(target1);
        factory.setInterfaces(target1.getClass().getInterfaces());
        factory.setProxyTargetClass(false);

        /*

            其实无论 ProxyFactory 基于哪种方式创建代理, 最后干活(调用 advice)的是一个 MethodInvocation 对象
                a. 因为 advisor 有多个, 且一个套一个调用, 因此需要一个调用链对象, 即 MethodInvocation
                b. MethodInvocation 要知道 advice 有哪些, 还要知道目标, 调用次序如下

                将 MethodInvocation 放入当前线程
                    |-> before1 ----------------------------------- 从当前线程获取 MethodInvocation
                    |                                             |
                    |   |-> before2 --------------------          | 从当前线程获取 MethodInvocation
                    |   |                              |          |
                    |   |   |-> target ------ 目标   advice2    advice1
                    |   |                              |          |
                    |   |-> after2 ---------------------          |
                    |                                             |
                    |-> after1 ------------------------------------
                c. 从上图看出, 环绕通知才适合作为 advice, 因此其他 before、afterReturning 都会被转换成环绕通知
                d. 统一转换为环绕通知, 体现的是设计模式中的适配器模式
                    - 对外是为了方便使用要区分 before、afterReturning
                    - 对内统一都是环绕通知, 统一用 MethodInterceptor 表示

            此步获取所有执行时需要的 advice (静态)
                a. 即统一转换为 MethodInterceptor 环绕通知, 这体现在方法名中的 Interceptors 上
                b. 适配如下
                  - MethodBeforeAdviceAdapter 将 @Before AspectJMethodBeforeAdvice 适配为 MethodBeforeAdviceInterceptor
                  - AfterReturningAdviceAdapter 将 @AfterReturning AspectJAfterReturningAdvice 适配为 AfterReturningAdviceInterceptor
         */

        System.out.println("把所有通知转换为环绕通知");
        Method method = target1.getClass().getMethod("foo");
        List<Object> methodInterceptorList = factory.getInterceptorsAndDynamicInterceptionAdvice(method, target1.getClass());
        for (Object o : methodInterceptorList) {
            System.out.println(o);
        }

        System.out.println("创建并执行调用链 (环绕通知s + 目标)");
        MethodInvocation methodInvocation = new ReflectiveMethodInvocation(
                null, target1, method, new Object[0], target1.getClass(), methodInterceptorList
        );
        methodInvocation.proceed();

        /*
            学到了什么
                a. 无参数绑定的通知如何被调用
                b. MethodInvocation 编程技巧: 拦截器、过滤器等等实现都与此类似
                c. 适配器模式在 Spring 中的体现
         */
    }

    /**
     * 创建代理
     *  a. proxyTargetClass = false, 目标实现了接口, 用 jdk 实现
     *  b. proxyTargetClass = false,  目标没有实现接口, 用 cglib 实现
     *  c. proxyTargetClass = true, 总是使用 cglib 实现
     */
    private static Object wrapIfNecessary(Object bean, DefaultListableBeanFactory beanFactory){
        List<Advisor> eligibleAdvisors = Application.findEligibleAdvisors(bean.getClass(), beanFactory);
        ProxyFactory factory = new ProxyFactory();
        // 把这个 Advice 把 MethodInvocation 放入当前线程，因为有些 advice 需要使用到 MethodInvocation
        factory.addAdvice(ExposeInvocationInterceptor.INSTANCE);
        factory.addAdvisors(eligibleAdvisors);

        factory.setTarget(bean);
        factory.setInterfaces(bean.getClass().getInterfaces());
        factory.setProxyTargetClass(false);
        return factory.getProxy(Thread.currentThread().getContextClassLoader());
    }

    private static List<Advisor> findEligibleAdvisors(Class<?> clazz, DefaultListableBeanFactory beanFactory){
        List<Advisor> candidateAdvisors = findAdvisorBeans(beanFactory);
        candidateAdvisors.addAll(buildAspectJAdvisors());
        return findAdvisorsThatCanApply(candidateAdvisors, clazz);
    }

    private static List<Advisor> findAdvisorBeans(DefaultListableBeanFactory beanFactory){
        System.out.println("找到所有 Advisor 切面");
        List<Advisor> candidateAdvisors = new ArrayList<>(beanFactory.getBeansOfType(Advisor.class).values());
        printAdvisor(candidateAdvisors);
        return candidateAdvisors;
    }

    private static List<Advisor> buildAspectJAdvisors(){
        System.out.println("找到所有 Aspect 切面并转换为 Advisor 切面");
        AspectInstanceFactory factory = new SingletonAspectInstanceFactory(new MyAspect());
        List<Advisor> aspectAdvisors = new ArrayList<>();
        for(Method method:MyAspect.class.getDeclaredMethods()){
            /**
             *   @Before 前置通知会被转换为下面原始的 AspectJMethodBeforeAdvice 形式, 该对象包含了如下信息
             *       a. 通知代码从哪儿来
             *       b. 切点是什么
             *       c. 通知对象如何创建, 本例共用同一个 Aspect 对象
             *   类似的通知还有
             *       1. AspectJAroundAdvice (环绕通知)
             *       2. AspectJAfterReturningAdvice
             *       3. AspectJAfterThrowingAdvice (环绕通知)
             *       4. AspectJAfterAdvice (环绕通知)
             */
            if (method.isAnnotationPresent(Before.class)) {
                // 解析切点
                AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
                pointcut.setExpression(method.getAnnotation(Before.class).value());
                // 通知类
                // 切面
                Advisor advisor = new DefaultPointcutAdvisor(pointcut, new AspectJMethodBeforeAdvice(method, pointcut, factory));
                aspectAdvisors.add(advisor);
            } else if (method.isAnnotationPresent(After.class)) {
                AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
                pointcut.setExpression(method.getAnnotation(After.class).value());
                Advisor advisor = new DefaultPointcutAdvisor(pointcut,  new AspectJAfterAdvice(method, pointcut, factory));
                aspectAdvisors.add(advisor);
            } else if (method.isAnnotationPresent(AfterReturning.class)) {
                AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
                pointcut.setExpression(method.getAnnotation(AfterReturning.class).value());
                Advisor advisor = new DefaultPointcutAdvisor(pointcut,  new AspectJAfterReturningAdvice(method, pointcut, factory));
                aspectAdvisors.add(advisor);
            } else if (method.isAnnotationPresent(Around.class)) {
                AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
                pointcut.setExpression(method.getAnnotation(Around.class).value());
                Advisor advisor = new DefaultPointcutAdvisor(pointcut, new AspectJAroundAdvice(method, pointcut, factory));
                aspectAdvisors.add(advisor);
            }
        }
        printAdvisor(aspectAdvisors);
        return aspectAdvisors;
    }

    private static List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> clazz){
        System.out.println("找到适合当前 Bean 的所有 Advisor 切面");
        List<Advisor> eligibleAdvisors = new ArrayList<>();
        for (Advisor advisor : candidateAdvisors) {
            for(Method method:Target1.class.getDeclaredMethods()){
                if(((PointcutAdvisor)advisor).getPointcut().getMethodMatcher().matches(method, clazz)){
                    eligibleAdvisors.add(advisor);
                    break;
                }
            }
        }
        printAdvisor(eligibleAdvisors);
        return eligibleAdvisors;
    }

    private static void printAdvisor(List<Advisor> advisors){
        for(Advisor advisor:advisors){
            System.out.println(advisor);
        }
    }
}
```



##### 总结

代理方法执行时会做如下工作

1. 通过 proxyFactory 的 getInterceptorsAndDynamicInterceptionAdvice() 将其他通知统一转换为 MethodInterceptor 环绕通知
   - MethodBeforeAdviceAdapter 将 @Before AspectJMethodBeforeAdvice 适配为 MethodBeforeAdviceInterceptor
   - AfterReturningAdviceAdapter 将 @AfterReturning AspectJAfterReturningAdvice 适配为 AfterReturningAdviceInterceptor
   - 这体现的是适配器设计模式
2. 所谓静态通知，体现在上面方法的 Interceptors 部分，这些通知调用时无需再次检查切点，直接调用即可
3. 结合目标与环绕通知链，创建 MethodInvocation 对象，通过它完成整个调用



#### 模拟 MethodInvocation

##### 示例

```java
static class Target {
    public void foo() {
        System.out.println("Target.foo()");
    }
}
```

```java
static class Advice1 implements MethodInterceptor {
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("Advice1.before()");
        Object result = invocation.proceed();// 调用下一个通知或目标
        System.out.println("Advice1.after()");
        return result;
    }
}

static class Advice2 implements MethodInterceptor {
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("Advice2.before()");
        Object result = invocation.proceed();// 调用下一个通知或目标
        System.out.println("Advice2.after()");
        return result;
    }
}
```

```java
static class MyInvocation implements MethodInvocation {
    private Object target;  // 1
    private Method method;
    private Object[] args;
    List<MethodInterceptor> methodInterceptorList; // 2
    private int count = 1; // 调用次数

    public MyInvocation(Object target, Method method, Object[] args, List<MethodInterceptor> methodInterceptorList) {
        this.target = target;
        this.method = method;
        this.args = args;
        this.methodInterceptorList = methodInterceptorList;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArguments() {
        return args;
    }

    @Override
    public Object proceed() throws Throwable { // 调用每一个环绕通知, 调用目标
        if (count > methodInterceptorList.size()) {
            // 调用目标， 返回并结束递归
            return method.invoke(target, args);
        }
        // 逐一调用通知, count + 1
        MethodInterceptor methodInterceptor = methodInterceptorList.get(count++ - 1);
        return methodInterceptor.invoke(this);
    }

    @Override
    public Object getThis() {
        return target;
    }

    @Override
    public AccessibleObject getStaticPart() {
        return method;
    }
}
```

```java
public static void main(String[] args) throws Throwable {
    Target target = new Target();
    List<MethodInterceptor> list = List.of(
            new Advice1(),
            new Advice2()
    );
    MyInvocation invocation = new MyInvocation(target, Target.class.getMethod("foo"), new Object[0], list);
    invocation.proceed();
}
```



##### 总结

1. proceed() 方法调用链中下一个环绕通知
2. 每个环绕通知内部继续调用 proceed()
3. 调用到没有更多通知了, 就调用目标方法

MethodInvocation 的编程技巧在实现拦截器、过滤器时能用上



### 动态通知调用

#### 示例

```java
public class Target{
    public void foo(){
        System.out.println("target1 foo");
    }

    public void foo(int x) {
        System.out.printf("target1 foo(%d)%n", x);
    }
}
```

```java
@Aspect
public class MyAspect {
    @Before("execution(* foo(..))") // 静态通知调用，不带参数绑定，执行时不需要切点
    public void before1() {
        System.out.println("before1");
    }

    @Before("execution(* foo(..)) && args(x)") // 动态通知调用，需要参数绑定，执行时还需要切点对象
    public void before2(int x) {
        System.out.printf("before2(%d)%n", x);
    }
}
```

```java
public class Application {
    public static void main(String[]args) throws Throwable {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean(ConfigurationClassPostProcessor.class);
        context.registerBean("myAspect", MyAspect.class);
        context.registerBean(AnnotationAwareAspectJAutoProxyCreator.class);
        context.refresh();

        AnnotationAwareAspectJAutoProxyCreator creator = context.getBean(AnnotationAwareAspectJAutoProxyCreator.class);
        List<Advisor> advisors = creator.findEligibleAdvisors(Target.class, "target");

        Target target = new Target();
        ProxyFactory factory = new ProxyFactory();
        factory.setTarget(target);
        factory.addAdvisors(advisors);
        Object proxy = factory.getProxy(); // 获取代理

        List<Object> interceptorList = factory.getInterceptorsAndDynamicInterceptionAdvice(Target.class.getMethod("foo", int.class), Target.class);
        for (Object o : interceptorList) {
            showDetail(o);
        }

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>");
        ReflectiveMethodInvocation invocation = new ReflectiveMethodInvocation(
                proxy, target, Target.class.getMethod("foo", int.class), new Object[]{100}, Target.class, interceptorList
        ) {};

        invocation.proceed();

        /*
            学到了什么
                a. 有参数绑定的通知调用时还需要切点，对参数进行匹配及绑定
                b. 复杂程度高, 性能比无参数绑定的通知调用低
         */
    }

    public static void showDetail(Object o) {
        try {
            Class<?> clazz = Class.forName("org.springframework.aop.framework.InterceptorAndDynamicMethodMatcher");
            if (clazz.isInstance(o)) {
                Field methodMatcher = clazz.getDeclaredField("methodMatcher");
                methodMatcher.setAccessible(true);
                Field methodInterceptor = clazz.getDeclaredField("interceptor");
                methodInterceptor.setAccessible(true);
                System.out.println("环绕通知和切点：" + o);
                System.out.println("\t切点为：" + methodMatcher.get(o));
                System.out.println("\t通知为：" + methodInterceptor.get(o));
            } else {
                System.out.println("普通环绕通知：" + o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```



#### 总结

1. 通过 proxyFactory 的 getInterceptorsAndDynamicInterceptionAdvice() 将其他通知统一转换为 MethodInterceptor 环绕通知
2. 所谓动态通知，体现在上面方法的 DynamicInterceptionAdvice 部分，这些通知调用时因为要为通知方法绑定参数，还需再次利用切点表达式
3. 动态通知调用复杂程度高，性能较低



## Spring MVC

### RequestMappingHandlerMapping 与 RequestMappingHandlerAdapter

RequestMappingHandlerMapping 与 RequestMappingHandlerAdapter 俩是一对，分别用来

* 处理 @RequestMapping 映射
* 调用控制器方法、并处理方法参数与方法返回值



#### 依赖配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>cn.example</groupId>
    <artifactId>example</artifactId>
    <version>1.0-SNAPSHOT</version>


    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>

    <!-- 引入Spring Boot依赖 -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.3.12.RELEASE</version>
    </parent>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <!-- 引入Web场景依赖启动器 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-test</artifactId>
        </dependency>

    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

```properties
server.port=8081
spring.mvc.servlet.load-on-startup=1
```



#### DispatcherServlet 初始化



##### 示例



```java
@Configuration
@ComponentScan
@PropertySource("classpath:application.properties")
@EnableConfigurationProperties({WebMvcProperties.class, ServerProperties.class})
/**
 * WebMvcProperties 以 spring.mvc 开头的配置
 * 类似的还有
 * WebMvcProperties 以 spring.web 开头的配置
 * ServerProperties 以 server 开头的配置
 */
public class WebConfig {
    /**
     * 内嵌 web 容器工厂
     */
    @Bean
    public TomcatServletWebServerFactory tomcatServletWebServerFactory(ServerProperties serverProperties) {
        return new TomcatServletWebServerFactory(serverProperties.getPort());
    }

    /**
     * 创建 DispatcherServlet
     */
    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

    /**
     * 注册 DispatcherServlet, Spring MVC 的入口
     */
    @Bean
    public DispatcherServletRegistrationBean dispatcherServletRegistrationBean(
            DispatcherServlet dispatcherServlet, WebMvcProperties webMvcProperties) {
        DispatcherServletRegistrationBean registrationBean = new DispatcherServletRegistrationBean(dispatcherServlet, "/");
        /**
         * 设置 Servlet 一启动时就初始化，参数对应不用 Servlet 启动优先级，优先级从小到大逐渐降低
         * 或者在 Spring 的配置文件配置 spring.mvc.servlet.load-on-startup=1
         * registrationBean.setLoadOnStartup(1);
         */
        registrationBean.setLoadOnStartup(webMvcProperties.getServlet().getLoadOnStartup());
        return registrationBean;
    }

    /**
     * 如果用 DispatcherServlet 初始化时默认添加的组件, 并不会作为 bean 添加到 Spring 容器中, 给测试带来困扰
     * 1. 加入 RequestMappingHandlerMapping
     */
    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping();
    }

    /**
     * 2. 继续加入RequestMappingHandlerAdapter, 会替换掉 DispatcherServlet 默认的 4 个 HandlerAdapter
     */
    @Bean
    public MyRequestMappingHandlerAdapter requestMappingHandlerAdapter() {
        MyRequestMappingHandlerAdapter handlerAdapter = new MyRequestMappingHandlerAdapter();
        return handlerAdapter;
    }
}
```

```java
@Controller
public class MyController {

    private static final Logger log = LoggerFactory.getLogger(MyController.class);

    @GetMapping("/test1")
    public ModelAndView test1() throws Exception {
        log.debug("test1()");
        return null;
    }
    
    @PostMapping("/test2")
    public ModelAndView test2(@RequestParam("name") String name) {
        log.debug("test2({})", name);
        return null;
    }
}
```

```java
/**
 * 为了调用父类的受保护方法，所以写一个子类重写为 public
 */
public class MyRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {
    @Override
    public ModelAndView invokeHandlerMethod(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
        return super.invokeHandlerMethod(request, response, handlerMethod);
    }
}
```

```java
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        AnnotationConfigServletWebServerApplicationContext context =
                new AnnotationConfigServletWebServerApplicationContext(WebConfig.class);

        // 作用 解析 @RequestMapping 以及派生注解，生成路径与控制器方法的映射关系, 在初始化时就生成
        RequestMappingHandlerMapping handlerMapping = context.getBean(RequestMappingHandlerMapping.class);

        // 获取映射结果
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
        handlerMethods.forEach((k, v) -> {
            System.out.println(k + "=" + v);
        });

        // 请求来了，获取控制器方法  返回处理器执行链对象
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test1");
        request.setLocalPort(8081);
        // request.setParameter("name", "张三");
        // request.addHeader("token", "某个令牌");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerExecutionChain chain = handlerMapping.getHandler(request);
        System.out.println(chain);

        System.out.println(">>>>>>>>>>>>>>>>>>>>>");
        // HandlerAdapter 作用: 调用控制器方法
        MyRequestMappingHandlerAdapter handlerAdapter = context.getBean(MyRequestMappingHandlerAdapter.class);
        handlerAdapter.invokeHandlerMethod(request, response, (HandlerMethod) chain.getHandler());

        // 检查响应
        byte[] content = response.getContentAsByteArray();
        System.out.println("结果：" + new String(content, StandardCharsets.UTF_8));
    }
}
```



##### 源码

```java
protected void onRefresh(ApplicationContext context) {
    this.initStrategies(context);
}

protected void initStrategies(ApplicationContext context) {
    // 初始化文件解析器
    this.initMultipartResolver(context);
    // 初始化本地化解析器
    this.initLocaleResolver(context);
    // 初始化模板解析器
    this.initThemeResolver(context);
    // 初始化路径映射器
    this.initHandlerMappings(context);
    // 初始化控制器适配器
    this.initHandlerAdapters(context);
    // 初始化异常解析器
    this.initHandlerExceptionResolvers(context);
    this.initRequestToViewNameTranslator(context);
    this.initViewResolvers(context);
    this.initFlashMapManager(context);
}

private void initHandlerMappings(ApplicationContext context) {
    this.handlerMappings = null;
    // 是否检测包括父子容器的所有 HandlerMapping
    if (this.detectAllHandlerMappings) {
        Map<String, HandlerMapping> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
        if (!matchingBeans.isEmpty()) {
            this.handlerMappings = new ArrayList(matchingBeans.values());
            AnnotationAwareOrderComparator.sort(this.handlerMappings);
        }
    } else {
        try {
            HandlerMapping hm = (HandlerMapping)context.getBean("handlerMapping", HandlerMapping.class);
            this.handlerMappings = Collections.singletonList(hm);
        } catch (NoSuchBeanDefinitionException var3) {
        }
    }

    // 容器中没有找到 HandlerMapping，则获取默认的 HandlerMapping
    if (this.handlerMappings == null) {
        this.handlerMappings = this.getDefaultStrategies(context, HandlerMapping.class);
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("No HandlerMappings declared for servlet '" + this.getServletName() + "': using default strategies from DispatcherServlet.properties");
        }
    }

}
```



#### 参数与返回值处理器

##### 示例

```java
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        AnnotationConfigServletWebServerApplicationContext context =
                new AnnotationConfigServletWebServerApplicationContext(WebConfig.class);

        // HandlerAdapter 作用: 调用控制器方法
        MyRequestMappingHandlerAdapter handlerAdapter = context.getBean(MyRequestMappingHandlerAdapter.class);
		/**
		 * 不同的参数由不同的参数解析器来解析
		 */
        System.out.println(">>>>>>>>>>>>>>>>>>>>> 所有参数解析器");
        for (HandlerMethodArgumentResolver resolver : handlerAdapter.getArgumentResolvers()) {
            System.out.println(resolver);
        }

		/**
		 * 不同的返回值由不同的返回值解析器来解析
		 */
        System.out.println(">>>>>>>>>>>>>>>>>>>>> 所有返回值解析器");
        for (HandlerMethodReturnValueHandler handler : handlerAdapter.getReturnValueHandlers()) {
            System.out.println(handler);
        }
    }
}
```



##### 自定义解析器

```java
/**
 * 例如经常需要用到请求头中的 token 信息, 用下面注解来标注由哪个参数来获取它
 * token=令牌
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Token {
}
```

```java
/**
 * 自定义参数解析器
 */
public class TokenArgumentResolver implements HandlerMethodArgumentResolver {
    /**
     * 是否支持某个参数
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Token token = parameter.getParameterAnnotation(Token.class);
        return token != null;
    }

    /**
     * 解析参数
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return webRequest.getHeader("token");
    }
}
```

```java
/**
 * 自定义返回值解析器
 */
public class YmlReturnValueHandler implements HandlerMethodReturnValueHandler {
    /**
     * 是否支持某个返回值
     */
    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        Yml yml = returnType.getMethodAnnotation(Yml.class);
        return yml != null;
    }

    /**
     * 解析返回值
     */
    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        // 1. 转换返回结果为 yaml 字符串
        String str = new Yaml().dump(returnValue);

        // 2. 将 yaml 字符串写入响应
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        response.setContentType("text/plain;charset=utf-8");
        response.getWriter().print(str);

        // 3. 设置请求已经处理完毕
        mavContainer.setRequestHandled(true);
    }
}
```

```java
@Configuration
@ComponentScan
@PropertySource("classpath:application.properties")
@EnableConfigurationProperties({WebMvcProperties.class, ServerProperties.class})
/**
 * WebMvcProperties 以 spring.mvc 开头的配置
 * 类似的还有
 * WebMvcProperties 以 spring.web 开头的配置
 * ServerProperties 以 server 开头的配置
 */
public class WebConfig {
    /**
     * 内嵌 web 容器工厂
     */
    @Bean
    public TomcatServletWebServerFactory tomcatServletWebServerFactory(ServerProperties serverProperties) {
        return new TomcatServletWebServerFactory(serverProperties.getPort());
    }

    /**
     * 创建 DispatcherServlet
     */
    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

    /**
     * 注册 DispatcherServlet, Spring MVC 的入口
     */
    @Bean
    public DispatcherServletRegistrationBean dispatcherServletRegistrationBean(
            DispatcherServlet dispatcherServlet, WebMvcProperties webMvcProperties) {
        DispatcherServletRegistrationBean registrationBean = new DispatcherServletRegistrationBean(dispatcherServlet, "/");
        /**
         * 设置 Servlet 一启动时就初始化，参数对应不用 Servlet 启动优先级，优先级从小到大逐渐降低
         * 或者在 Spring 的配置文件配置 spring.mvc.servlet.load-on-startup=1
         * registrationBean.setLoadOnStartup(1);
         */
        registrationBean.setLoadOnStartup(webMvcProperties.getServlet().getLoadOnStartup());
        return registrationBean;
    }

    /**
     * 如果用 DispatcherServlet 初始化时默认添加的组件, 并不会作为 bean 添加到 Spring 容器中, 给测试带来困扰
     * 1. 加入 RequestMappingHandlerMapping
     */
    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping();
    }

    /**
     * 2. 继续加入RequestMappingHandlerAdapter, 会替换掉 DispatcherServlet 默认的 4 个 HandlerAdapter
     */
    @Bean
    public MyRequestMappingHandlerAdapter requestMappingHandlerAdapter() {
        TokenArgumentResolver tokenArgumentResolver = new TokenArgumentResolver();
        YmlReturnValueHandler ymlReturnValueHandler = new YmlReturnValueHandler();
        MyRequestMappingHandlerAdapter handlerAdapter = new MyRequestMappingHandlerAdapter();
        /**
         * 加入自定义参数解析器和返回值解析器
         */
        handlerAdapter.setCustomArgumentResolvers(Collections.singletonList(tokenArgumentResolver));
        handlerAdapter.setCustomReturnValueHandlers(Collections.singletonList(ymlReturnValueHandler));
        return handlerAdapter;
    }
}
```

```java
@Controller
public class MyController {

    private static final Logger log = LoggerFactory.getLogger(MyController.class);
    @PutMapping("/test3")
    @Yml
    public User test3(@Token String token) {
        log.debug("test3({})", token);
        return new User("张三", 18);
    }

    public static class User {
        private String name;
        private int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
```

```java
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        AnnotationConfigServletWebServerApplicationContext context =
                new AnnotationConfigServletWebServerApplicationContext(WebConfig.class);


        // 作用 解析 @RequestMapping 以及派生注解，生成路径与控制器方法的映射关系, 在初始化时就生成
        RequestMappingHandlerMapping handlerMapping = context.getBean(RequestMappingHandlerMapping.class);

        // HandlerAdapter 作用: 调用控制器方法
        MyRequestMappingHandlerAdapter handlerAdapter = context.getBean(MyRequestMappingHandlerAdapter.class);

        // 请求来了，获取控制器方法  返回处理器执行链对象
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/test3");
        request.setParameter("token", "token");
        request.addHeader("token", "某个令牌");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerExecutionChain chain = handlerMapping.getHandler(request);
        System.out.println(chain);

        System.out.println(">>>>>>>>>>>>>>>>>>>>>");
        handlerAdapter.invokeHandlerMethod(request, response, (HandlerMethod) chain.getHandler());

        // 检查响应
        byte[] content = response.getContentAsByteArray();
        System.out.println("结果：" + new String(content, StandardCharsets.UTF_8));
    }
}
```



#### 总结

1. DispatcherServlet 是在第一次被访问时执行初始化, 也可以通过配置修改为 Tomcat 启动后就初始化
2. 在初始化时会从 Spring 容器中找一些 Web 需要的组件, 如 HandlerMapping、HandlerAdapter 等，并逐一调用它们的初始化
3. RequestMappingHandlerMapping 初始化时，会收集所有 @RequestMapping 映射信息，封装为 Map，其中
   * key 是 RequestMappingInfo 类型，包括请求路径、请求方法等信息
   * value 是 HandlerMethod 类型，包括控制器方法对象、控制器对象
   * 有了这个 Map，就可以在请求到达时，快速完成映射，找到 HandlerMethod 并与匹配的拦截器一起返回给 DispatcherServlet
4. RequestMappingHandlerAdapter 初始化时，会准备 HandlerMethod 调用时需要的各个组件，如：
   * HandlerMethodArgumentResolver 解析控制器方法参数
   * HandlerMethodReturnValueHandler 处理控制器方法返回值



### 参数解析器

#### 常见参数解析器

```java
org.springframework.web.method.annotation.RequestParamMethodArgumentResolver@abbc908
org.springframework.web.method.annotation.RequestParamMapMethodArgumentResolver@44afefd5
org.springframework.web.servlet.mvc.method.annotation.PathVariableMethodArgumentResolver@9a7a808
org.springframework.web.servlet.mvc.method.annotation.PathVariableMapMethodArgumentResolver@72209d93
org.springframework.web.servlet.mvc.method.annotation.MatrixVariableMethodArgumentResolver@2687f956
org.springframework.web.servlet.mvc.method.annotation.MatrixVariableMapMethodArgumentResolver@1ded7b14
org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor@29be7749
org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor@5f84abe8
org.springframework.web.servlet.mvc.method.annotation.RequestPartMethodArgumentResolver@4650a407
org.springframework.web.method.annotation.RequestHeaderMethodArgumentResolver@30135202
org.springframework.web.method.annotation.RequestHeaderMapMethodArgumentResolver@6a4d7f76
org.springframework.web.servlet.mvc.method.annotation.ServletCookieValueMethodArgumentResolver@10ec523c
org.springframework.web.method.annotation.ExpressionValueMethodArgumentResolver@53dfacba
org.springframework.web.servlet.mvc.method.annotation.SessionAttributeMethodArgumentResolver@79767781
org.springframework.web.servlet.mvc.method.annotation.RequestAttributeMethodArgumentResolver@78411116
org.springframework.web.servlet.mvc.method.annotation.ServletRequestMethodArgumentResolver@aced190
org.springframework.web.servlet.mvc.method.annotation.ServletResponseMethodArgumentResolver@245a060f
org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor@6edaa77a
org.springframework.web.servlet.mvc.method.annotation.RedirectAttributesMethodArgumentResolver@1e63d216
org.springframework.web.method.annotation.ModelMethodProcessor@62ddd21b
org.springframework.web.method.annotation.MapMethodProcessor@16c3ca31
org.springframework.web.method.annotation.ErrorsMethodArgumentResolver@2d195ee4
org.springframework.web.method.annotation.SessionStatusMethodArgumentResolver@2d6aca33
org.springframework.web.servlet.mvc.method.annotation.UriComponentsBuilderMethodArgumentResolver@21ab988f
org.springframework.web.servlet.mvc.method.annotation.PrincipalMethodArgumentResolver@29314cc9
org.springframework.web.method.annotation.RequestParamMethodArgumentResolver@4e38d975
org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor@35f8a9d3
```



#### 示例

```java
public class User {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
```

```java
public class MyController {
    public void test(
            @RequestParam("name1") String name1, // name1=张三
            String name2,                        // name2=李四
            @RequestParam("age") int age,        // age=18
            @RequestParam(name = "home", defaultValue = "${JAVA_HOME}") String home1, // spring 获取数据
            @RequestParam("file") MultipartFile file, // 上传文件
            @PathVariable("id") int id,               //  /test/124   /test/{id}
            @RequestHeader("Content-Type") String header,
            @CookieValue("token") String token,
            @Value("${JAVA_HOME}") String home2, // spring 获取数据  ${} #{}
            HttpServletRequest request,          // request, response, session ...
            @ModelAttribute("abc") User user1,          // name=zhang&age=18
            User user2,                          // name=zhang&age=18
            @RequestBody User user3              // json
    ) {
    }
}
```

```java
public class Application {

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(WebConfig.class);
        DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
        // 准备测试 Request
        HttpServletRequest request = mockRequest();

        // 要点1. 控制器方法被封装为 HandlerMethod
        HandlerMethod handlerMethod = new HandlerMethod(new MyController(), MyController.class.getMethod("test", String.class, String.class, int.class, String.class, MultipartFile.class, int.class, String.class, String.class, String.class, HttpServletRequest.class, User.class, User.class, User.class));

        // 要点2. 准备对象绑定与类型转换
        ServletRequestDataBinderFactory factory = new ServletRequestDataBinderFactory(null, null);

        // 要点3. 准备 ModelAndViewContainer 用来存储中间 Model 结果
        ModelAndViewContainer container = new ModelAndViewContainer();

        // 要点4. 解析每个参数值
        for (MethodParameter parameter : handlerMethod.getMethodParameters()) {
            // 多个解析器组合
            HandlerMethodArgumentResolverComposite composite = new HandlerMethodArgumentResolverComposite();
            composite.addResolvers(
                    //                                          false 表示必须有 @RequestParam
                    new RequestParamMethodArgumentResolver(beanFactory, false),
                    new PathVariableMethodArgumentResolver(),
                    new RequestHeaderMethodArgumentResolver(beanFactory),
                    new ServletCookieValueMethodArgumentResolver(beanFactory),
                    new ExpressionValueMethodArgumentResolver(beanFactory),
                    new ServletRequestMethodArgumentResolver(),
                    new ServletModelAttributeMethodProcessor(false), // 必须有 @ModelAttribute
                    new RequestResponseBodyMethodProcessor(Collections.singletonList(new MappingJackson2HttpMessageConverter())),
                    new ServletModelAttributeMethodProcessor(true), // 省略了 @ModelAttribute
                    new RequestParamMethodArgumentResolver(beanFactory, true) // 省略 @RequestParam
            );

            String annotations = Arrays.stream(parameter.getParameterAnnotations()).map(a -> a.annotationType().getSimpleName()).collect(Collectors.joining());
            String str = annotations.length() > 0 ? " @" + annotations + " " : " ";
            parameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());

            if (composite.supportsParameter(parameter)) {
                // 支持此参数
                Object v = composite.resolveArgument(parameter, container, new ServletWebRequest(request), factory);
//                System.out.println(v.getClass());
                System.out.println(String.format("[%d] %s %s %s -> %s", parameter.getParameterIndex(), str, parameter.getParameterType().getSimpleName(), parameter.getParameterName(), v));
                System.out.println("模型数据为：" + container.getModel());
            } else {
                System.out.println(String.format("[%d] %s %s %s", parameter.getParameterIndex(), str, parameter.getParameterType().getSimpleName(), parameter.getParameterName()));
            }
        }
    }

    private static HttpServletRequest mockRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("name1", "zhangsan");
        request.setParameter("name2", "lisi");
        request.addPart(new MockPart("file", "abc", "hello".getBytes(StandardCharsets.UTF_8)));
        Map<String, String> map = new AntPathMatcher().extractUriTemplateVariables("/test/{id}", "/test/123");
        System.out.println(map);
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, map);
        request.setContentType("application/json");
        request.setCookies(new Cookie("token", "123456"));
        request.setParameter("name", "张三");
        request.setParameter("age", "18");
        request.setContent("{\"nam\":\"李四\",\"ag\":20}".getBytes(StandardCharsets.UTF_8));

        return new StandardServletMultipartResolver().resolveMultipart(request);
    }
}
```



#### 总结

1. 初步了解 RequestMappingHandlerAdapter 的调用过程
   1. 控制器方法被封装为 HandlerMethod
   2. 准备对象绑定与类型转换
   3. 准备 ModelAndViewContainer 用来存储中间 Model 结果
   4. 解析每个参数值
2. 解析参数依赖的就是各种参数解析器，它们都有两个重要方法
   * supportsParameter 判断是否支持方法参数
   * resolveArgument 解析方法参数
3. 常见参数的解析
   * @RequestParam
   * 省略 @RequestParam
   * @RequestParam(defaultValue)
   * MultipartFile
   * @PathVariable
   * @RequestHeader
   * @CookieValue
   * @Value
   * HttpServletRequest 等
   * @ModelAttribute
   * 省略 @ModelAttribute
   * @RequestBody
4. 组合模式在 Spring 中的体现
5. @RequestParam, @CookieValue 等注解中的参数名、默认值, 都可以写成活的, 即从 ${ } #{ }中获取



### 参数名解析

#### 问题

方法的参数经过编译器编译之后不再是源码中的参数名

```java
// 源码
public class Bean{
    public void foo(String name, int age){

    }
}

// javac ./Bean.java 编译
// 使用 idea 查看 class 文件
public class Bean {
    public Bean() {
    }

    public void foo(String var1, int var2) {
    }
}

// javap -c -v ./Bean.class 反编译查看字节码
public void foo(java.lang.String, int);
	descriptor: (Ljava/lang/String;I)V
    flags: ACC_PUBLIC
    Code:
        stack=0, locals=3, args_size=3
            0: return
        LineNumberTable:
            line 6: 0
```



#### 解决方法

##### 使用参数 `-parameters`编译

```java
// javac -parameters ./Bean.java 编译
// 使用 idea 查看 class 文件
public class Bean {
    public Bean() {
    }

    public void foo(String name, int age) {
    }
}

// javap -c -v ./Bean.class 反编译查看字节码
public void foo(java.lang.String, int);
	descriptor: (Ljava/lang/String;I)V
    flags: ACC_PUBLIC
    Code:
        stack=0, locals=3, args_size=3
            0: return
        LineNumberTable:
            line 6: 0
        MethodParameters:
            Name                           Flags
            name
            age
```

##### 使用参数 `-g`编译

```java
// javac -parameters ./Bean.java 编译
// 使用 idea 查看 class 文件
public class Bean {
    public Bean() {
    }

    public void foo(String name, int age) {
    }
}

// javap -c -v ./Bean.class 反编译查看字节码
public void foo(java.lang.String, int);
	descriptor: (Ljava/lang/String;I)V
    flags: ACC_PUBLIC
    Code:
        stack=0, locals=3, args_size=3
            0: return
        LineNumberTable:
            line 6: 0
        LocalVariableTable:
            Start  Length  Slot  Name   Signature
                0       1     0  this   Lcom/example/vo/Bean;
                0       1     1  name   Ljava/lang/String;
                0       1     2   age   I
```



##### 区别

 `-parameters`与`-g`的区别

- `-parameters`方法参数名存在方法参数表中，可以通过反射获取方法参数名
- `-g`方法参数名存在本地变量表中，不可以通过反射获取方法参数名，但是能通过 ASM 技术获取
- Spring 中同时支持这两个方式获取变量名



#### Spring 的参数名获取

- Spring Boot 会在编译时加入 `-parameters`
- 大部分 IDE 会在编译时加入 `-g`



1. 在 Spring 项目的 src 同级目录添加下面的源码（避免 Idea 自动编译）

```java
public class MyBean{
    public void foo(String name, int age){

    }
}
```

2. File -> Project Structure -> Project Settings -> Modules -> 选择当前项目 -> Dependencies -> 添加 JARs or Directories -> 添加 MyBean 目录

3. 使用不同编译方式编译 MyBean 之后运行测试代码查看运行结果

```java
public class Application {
    public static void main(String[] args) throws NoSuchMethodException {
        System.out.println("反射获取的变量名：");
        Method foo = MyBean.class.getMethod("foo", String.class, int.class);
        for(Parameter parameter:foo.getParameters()){
            System.out.println(parameter.getName());
        }

        System.out.println("本地变量表的变量名：");
        LocalVariableTableParameterNameDiscoverer nameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
        String[] parameterNames = nameDiscoverer.getParameterNames(foo);
        if(null != parameterNames){
            for(String parameterName:parameterNames){
                System.out.println(parameterName);
            }
        }

        System.out.println("Spring 获取的的变量名：");
        DefaultParameterNameDiscoverer defaultNameDiscoverer = new DefaultParameterNameDiscoverer();
        parameterNames = defaultNameDiscoverer.getParameterNames(foo);
        if(null != parameterNames){
            for(String parameterName:parameterNames){
                System.out.println(parameterName);
            }
        }
    }
}
```



**DefaultParameterNameDiscoverer 结合了方法参数变量表和本地变量表参数名的获取方法**

```java
public class DefaultParameterNameDiscoverer extends PrioritizedParameterNameDiscoverer {
    public DefaultParameterNameDiscoverer() {
        if (KotlinDetector.isKotlinReflectPresent() && !GraalDetector.inImageCode()) {
            this.addDiscoverer(new KotlinReflectionParameterNameDiscoverer());
        }
		
        // 根据反射获取方法参数变量名
        this.addDiscoverer(new StandardReflectionParameterNameDiscoverer());
        // 根据本地变量表获取方法参数名
        this.addDiscoverer(new LocalVariableTableParameterNameDiscoverer());
    }
}
```



#### 总结

1. 如果编译时添加了 -parameters 可以生成参数表, 反射时就可以拿到参数名
2. 如果编译时添加了 -g 可以生成调试信息, 但分为两种情况
   * 普通类, 会包含局部变量表, 用 asm 可以拿到参数名
   * 接口, 不会包含局部变量表, 无法获得参数名
     * 这也是 MyBatis 在实现 Mapper 接口时为何要提供 @Param 注解来辅助获得参数名



### 对象绑定与类型转换

#### 底层第一套转换接口与实现

```mermaid
classDiagram

Formatter --|> Printer
Formatter --|> Parser

class Converters {
   Set~GenericConverter~
}
class Converter

class ConversionService
class FormattingConversionService

ConversionService <|-- FormattingConversionService
FormattingConversionService o-- Converters

Printer --> Adapter1
Adapter1 --> Converters
Parser --> Adapter2
Adapter2 --> Converters
Converter --> Adapter3
Adapter3 --> Converters

<<interface>> Formatter
<<interface>> Printer
<<interface>> Parser
<<interface>> Converter
<<interface>> ConversionService
```

* Printer 把其它类型转为 String
* Parser 把 String 转为其它类型
* Formatter 综合 Printer 与 Parser 功能
* Converter 把类型 S 转为类型 T
* Printer、Parser、Converter 经过适配转换成 GenericConverter 放入 Converters 集合
* FormattingConversionService 利用其它们实现转换



#### 底层第二套转换接口

```mermaid
classDiagram

PropertyEditorRegistry o-- "多" PropertyEditor

<<interface>> PropertyEditorRegistry
<<interface>> PropertyEditor
```

* PropertyEditor 把 String 与其它类型相互转换
* PropertyEditorRegistry 可以注册多个 PropertyEditor 对象
* 与第一套接口直接可以通过 FormatterPropertyEditorAdapter 来进行适配



#### 高层接口与实现

```mermaid
classDiagram
TypeConverter <|-- SimpleTypeConverter
TypeConverter <|-- BeanWrapperImpl
TypeConverter <|-- DirectFieldAccessor
TypeConverter <|-- ServletRequestDataBinder

SimpleTypeConverter --> TypeConverterDelegate
BeanWrapperImpl --> TypeConverterDelegate
DirectFieldAccessor --> TypeConverterDelegate
ServletRequestDataBinder --> TypeConverterDelegate

TypeConverterDelegate --> ConversionService
TypeConverterDelegate --> PropertyEditorRegistry

<<interface>> TypeConverter
<<interface>> ConversionService
<<interface>> PropertyEditorRegistry
```

* 它们都实现了 TypeConverter 这个高层转换接口，在转换时，会用到 TypeConverter Delegate 委派ConversionService 与 PropertyEditorRegistry 真正执行转换（Facade 门面模式）
  * 首先看是否有自定义转换器, @InitBinder 添加的即属于这种 (用了适配器模式把 Formatter 转为需要的 PropertyEditor)
  * 再看有没有 ConversionService 转换
  * 再利用默认的 PropertyEditor 转换
  * 最后有一些特殊处理
* SimpleTypeConverter 仅做类型转换
* BeanWrapperImpl 为 bean 的属性赋值，当需要时做类型转换，走 Property
* DirectFieldAccessor 为 bean 的属性赋值，当需要时做类型转换，走 Field
* ServletRequestDataBinder 为 bean 的属性执行绑定，当需要时做类型转换，根据 directFieldAccess 选择走 Property 还是 Field，具备校验与获取校验结果功能



#### 类型转换与数据绑定

##### 示例

###### SimpleTypeConverter

```java
public class TestSimpleConverter {
    public static void main(String[] args) {
        // 仅有类型转换的功能
        SimpleTypeConverter typeConverter = new SimpleTypeConverter();
        Integer number = typeConverter.convertIfNecessary("13", int.class);
        Date date = typeConverter.convertIfNecessary("1999/03/04", Date.class);
        System.out.println(number);
        System.out.println(date);
    }
}
```

###### BeanWrapperImpl

```java
public class TestBeanWrapper {
    public static void main(String[] args) {
        // 利用反射原理, 为 bean 的属性赋值
        MyBean target = new MyBean();
        BeanWrapperImpl wrapper = new BeanWrapperImpl(target);
        wrapper.setPropertyValue("a", "10");
        wrapper.setPropertyValue("b", "hello");
        wrapper.setPropertyValue("c", "1999/03/04");
        System.out.println(target);
    }

    static class MyBean {
        private int a;
        private String b;
        private Date c;

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public String getB() {
            return b;
        }

        public void setB(String b) {
            this.b = b;
        }

        public Date getC() {
            return c;
        }

        public void setC(Date c) {
            this.c = c;
        }

        @Override
        public String toString() {
            return "MyBean{" +
                   "a=" + a +
                   ", b='" + b + '\'' +
                   ", c=" + c +
                   '}';
        }
    }
}
```

###### DirectFieldAccessor

```java
public class TestFieldAccessor {
    public static void main(String[] args) {
        // 利用反射原理, 为 bean 的属性赋值
        MyBean target = new MyBean();
        DirectFieldAccessor accessor = new DirectFieldAccessor(target);
        accessor.setPropertyValue("a", "10");
        accessor.setPropertyValue("b", "hello");
        accessor.setPropertyValue("c", "1999/03/04");
        System.out.println(target);
    }

    static class MyBean {
        private int a;
        private String b;
        private Date c;
        @Override
        public String toString() {
            return "MyBean{" +
                   "a=" + a +
                   ", b='" + b + '\'' +
                   ", c=" + c +
                   '}';
        }
    }
}
```

###### DataBinder

```java
public class TestDataBinder {

    public static void main(String[] args) {
        // 执行数据绑定
        MyBean target = new MyBean();
        DataBinder dataBinder = new DataBinder(target);
        // 设置走 Property 还是 Field
        dataBinder.initDirectFieldAccess();
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.add("a", "10");
        pvs.add("b", "hello");
        pvs.add("c", "1999/03/04");
        dataBinder.bind(pvs);
        System.out.println(target);
    }

    static class MyBean {
        private int a;
        private String b;
        private Date c;

        @Override
        public String toString() {
            return "MyBean{" +
                   "a=" + a +
                   ", b='" + b + '\'' +
                   ", c=" + c +
                   '}';
        }
    }
}
```

###### ServletRequestDataBinder

```java
public class TestServletDataBinder {

    public static void main(String[] args) {
        // web 环境下数据绑定
        MyBean target = new MyBean();
        ServletRequestDataBinder dataBinder = new ServletRequestDataBinder(target);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("a", "10");
        request.setParameter("b", "hello");
        request.setParameter("c", "1999/03/04");

        dataBinder.bind(new ServletRequestParameterPropertyValues(request));

        System.out.println(target);
    }

    static class MyBean {
        private int a;
        private String b;
        private Date c;

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public String getB() {
            return b;
        }

        public void setB(String b) {
            this.b = b;
        }

        public Date getC() {
            return c;
        }

        public void setC(Date c) {
            this.c = c;
        }

        @Override
        public String toString() {
            return "MyBean{" +
                   "a=" + a +
                   ", b='" + b + '\'' +
                   ", c=" + c +
                   '}';
        }
    }
}
```



#### 数据绑定工厂

##### 示例

```java
public class MyDateFormatter implements Formatter<Date> {
    private static final Logger log = LoggerFactory.getLogger(MyDateFormatter.class);
    private final String desc;

    public MyDateFormatter(String desc) {
        this.desc = desc;
    }

    @Override
    public String print(Date date, Locale locale) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy|MM|dd");
        return sdf.format(date);
    }

    @Override
    public Date parse(String text, Locale locale) throws ParseException {
        log.debug(">>>>>> 进入了: {}", desc);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy|MM|dd");
        return sdf.parse(text);
    }
}
```

```java
public class TestServletDataBinderFactory {
    public static void main(String[] args) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("birthday", "1999|01|02");
        request.setParameter("address.name", "西安");

        User target = new User();
//         "1. 用工厂, 无转换功能"
//        ServletRequestDataBinderFactory factory = new ServletRequestDataBinderFactory(null, null);

//         "2. 用 @InitBinder 转换"          PropertyEditorRegistry PropertyEditor
//        InvocableHandlerMethod method = new InvocableHandlerMethod(new MyController(), MyController.class.getMethod("testInitBinder", WebDataBinder.class));
//        ServletRequestDataBinderFactory factory = new ServletRequestDataBinderFactory(Collections.singletonList(method), null);

//         "3. 用 ConversionService 转换"    ConversionService Formatter
//        FormattingConversionService service = new FormattingConversionService();
//        service.addFormatter(new MyDateFormatter("用 ConversionService 方式扩展转换功能"));
//        ConfigurableWebBindingInitializer initializer = new ConfigurableWebBindingInitializer();
//        initializer.setConversionService(service);
//        ServletRequestDataBinderFactory factory = new ServletRequestDataBinderFactory(null, initializer);

//         "4. 同时加了 @InitBinder 和 ConversionService" 使用了 @InitBinder
//        ServletRequestDataBinderFactory factory = new ServletRequestDataBinderFactory(Collections.singletonList(method), initializer);

//         "5. 使用默认 ConversionService 转换" 需要配合 Spring 的容器格式化注解 @DateTimeFormat
//        非 Spring Boot 程序可以使用 DefaultFormattingConversionService，Spring Boot 程序可以使用 ApplicationConversionService，
        DefaultFormattingConversionService service = new DefaultFormattingConversionService();
//        ApplicationConversionService service = new ApplicationConversionService();
        ConfigurableWebBindingInitializer initializer = new ConfigurableWebBindingInitializer();
        initializer.setConversionService(service);
        ServletRequestDataBinderFactory factory = new ServletRequestDataBinderFactory(null, initializer);

        WebDataBinder dataBinder = factory.createBinder(new ServletWebRequest(request), target, "user");
        dataBinder.bind(new ServletRequestParameterPropertyValues(request));
        System.out.println(target);
    }

    static class MyController {
        @InitBinder
        public void testInitBinder(WebDataBinder dataBinder) {
            // 扩展 dataBinder 的转换器
            dataBinder.addCustomFormatter(new MyDateFormatter("用 @InitBinder 方式扩展的"));
        }
    }

    public static class User {
        @DateTimeFormat(pattern = "yyyy|MM|dd")
        private Date birthday;
        private Address address;

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Date getBirthday() {
            return birthday;
        }

        public void setBirthday(Date birthday) {
            this.birthday = birthday;
        }

        @Override
        public String toString() {
            return "User{" +
                   "birthday=" + birthday +
                   ", address=" + address +
                   '}';
        }
    }

    public static class Address {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Address{" +
                   "name='" + name + '\'' +
                   '}';
        }
    }
}
```



##### 总结

ServletRequestDataBinderFactory 的用法和扩展点

1. 可以解析控制器的 @InitBinder 标注方法作为扩展点，添加自定义转换器
   * 控制器私有范围
2. 可以通过 ConfigurableWebBindingInitializer 配置 ConversionService 作为扩展点，添加自定义转换器
   * 公共范围
3. 同时加了 @InitBinder 和 ConversionService 的转换优先级
   1. 优先采用 @InitBinder 的转换器
   2. 其次使用 ConversionService 的转换器
   3. 使用默认转换器
   4. 特殊处理（例如有参构造）



#### 获取泛型参数

##### 示例

```java
public class TestGenericType {
    public static void main(String[] args) {
//         小技巧
//         1. java api
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
        Type type = TeacherDao.class.getGenericSuperclass();
        System.out.println(type);

//        判断父类型是否是泛型
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            System.out.println(parameterizedType.getActualTypeArguments()[0]);
        }

//         2. spring api 1
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
        Class<?> t = GenericTypeResolver.resolveTypeArgument(TeacherDao.class, BaseDao.class);
        System.out.println(t);

//         3. spring api 2
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(ResolvableType.forClass(TeacherDao.class).getSuperType().getGeneric().resolve());
    }

}
```



### @ControllerAdvice 

对 Controller 提供增强功能

- @InitBinder 补充自定义参数转换器

- @ModelAttribute 方法的返回值会作为模型数据补充到控制器的执行过程中
- @ExceptionHandler 统一处理异样



#### @InitBinder

- 可以加在 `@ControllerAdvice` 的方法上，作用范围是全局的，对所有 `@Controller` 都生效
- 可以加在 `@Controller` 的方法上，作用范围是当前 `@Controller`，只对当前 `@Controller` 生效



##### 示例

**准备 @InitBinder** 在整个 HandlerAdapter 调用过程中所处的位置

```mermaid
sequenceDiagram
participant adapter as HandlerAdapter
participant bf as WebDataBinderFactory
participant mf as ModelFactory
participant ihm as ServletInvocableHandlerMethod
participant ar as ArgumentResolvers 
participant rh as ReturnValueHandlers
participant container as ModelAndViewContainer
rect rgb(200, 150, 255)
adapter ->> +bf: 准备 @InitBinder
bf -->> -adapter: 
end
adapter ->> +mf: 准备 @ModelAttribute
mf ->> +container: 添加Model数据
container -->> -mf: 
mf -->> -adapter: 

adapter ->> +ihm: invokeAndHandle
ihm ->> +ar: 获取 args
ar ->> ar: 有的解析器涉及 RequestBodyAdvice
ar ->> container: 有的解析器涉及数据绑定生成Model数据
ar -->> -ihm: args
ihm ->> ihm: method.invoke(bean,args) 得到 returnValue
ihm ->> +rh: 处理 returnValue
rh ->> rh: 有的处理器涉及 ResponseBodyAdvice
rh ->> +container: 添加Model数据,处理视图名,是否渲染等
container -->> -rh: 
rh -->> -ihm: 
ihm -->> -adapter: 
adapter ->> +container: 获取 ModelAndView
container -->> -adapter: 
```

* RequestMappingHandlerAdapter 在图中缩写为 HandlerAdapter
* HandlerMethodArgumentResolverComposite 在图中缩写为 ArgumentResolvers
* HandlerMethodReturnValueHandlerComposite 在图中缩写为 ReturnValueHandlers



```java
public class MyDateFormatter implements Formatter<Date> {
    private static final Logger log = LoggerFactory.getLogger(MyDateFormatter.class);
    private final String desc;

    public MyDateFormatter(String desc) {
        this.desc = desc;
    }

    @Override
    public String print(Date date, Locale locale) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy|MM|dd");
        return sdf.format(date);
    }

    @Override
    public Date parse(String text, Locale locale) throws ParseException {
        log.debug(">>>>>> 进入了: {}", desc);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy|MM|dd");
        return sdf.parse(text);
    }
}
```

```java
@Configuration
public class WebConfig {

    @ControllerAdvice
    static class MyControllerAdvice {
        @InitBinder
        public void binder3(WebDataBinder webDataBinder) {
            webDataBinder.addCustomFormatter(new MyDateFormatter("binder3 转换器"));
        }
    }

    @Controller
    static class Controller1 {
        @InitBinder
        public void binder1(WebDataBinder webDataBinder) {
            webDataBinder.addCustomFormatter(new MyDateFormatter("binder1 转换器"));
        }

        public void foo() {

        }
    }

    @Controller
    static class Controller2 {
        @InitBinder
        public void binder21(WebDataBinder webDataBinder) {
            webDataBinder.addCustomFormatter(new MyDateFormatter("binder21 转换器"));
        }

        @InitBinder
        public void binder22(WebDataBinder webDataBinder) {
            webDataBinder.addCustomFormatter(new MyDateFormatter("binder22 转换器"));
        }

        public void bar() {

        }
    }

}
```

```java
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        /*
            @InitBinder 的来源有两个
            1. @ControllerAdvice 中 @InitBinder 标注的方法，由 RequestMappingHandlerAdapter 在初始化时解析并记录
            2. @Controller 中 @InitBinder 标注的方法，由 RequestMappingHandlerAdapter 会在控制器方法首次执行时解析并记录
         */
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(WebConfig.class);

        RequestMappingHandlerAdapter handlerAdapter = new RequestMappingHandlerAdapter();
        handlerAdapter.setApplicationContext(context);
        handlerAdapter.afterPropertiesSet();

        log.debug("1. 刚开始...");
        showBindMethods(handlerAdapter);

        Method getDataBinderFactory = RequestMappingHandlerAdapter.class.getDeclaredMethod("getDataBinderFactory", HandlerMethod.class);
        getDataBinderFactory.setAccessible(true);

        log.debug("2. 模拟调用 Controller1 的 foo 方法时 ...");
        getDataBinderFactory.invoke(handlerAdapter, new HandlerMethod(new WebConfig.Controller1(), WebConfig.Controller1.class.getMethod("foo")));
        showBindMethods(handlerAdapter);

        log.debug("3. 模拟调用 Controller2 的 bar 方法时 ...");
        getDataBinderFactory.invoke(handlerAdapter, new HandlerMethod(new WebConfig.Controller2(), WebConfig.Controller2.class.getMethod("bar")));
        showBindMethods(handlerAdapter);

        context.close();

        /*
            a. Method 对象的获取利用了缓存来进行加速
            b. 绑定器工厂的扩展点(advice 之一), 通过 @InitBinder 扩展类型转换器
         */
    }

    @SuppressWarnings("all")
    private static void showBindMethods(RequestMappingHandlerAdapter handlerAdapter) throws NoSuchFieldException, IllegalAccessException {
        Field initBinderAdviceCache = RequestMappingHandlerAdapter.class.getDeclaredField("initBinderAdviceCache");
        initBinderAdviceCache.setAccessible(true);
        Map<ControllerAdviceBean, Set<Method>> globalMap = (Map<ControllerAdviceBean, Set<Method>>) initBinderAdviceCache.get(handlerAdapter);
        log.debug("全局的 @InitBinder 方法 {}",
                globalMap.values().stream()
                        .flatMap(ms -> ms.stream().map(m -> m.getName()))
                        .collect(Collectors.toList())
        );

        Field initBinderCache = RequestMappingHandlerAdapter.class.getDeclaredField("initBinderCache");
        initBinderCache.setAccessible(true);
        Map<Class<?>, Set<Method>> controllerMap = (Map<Class<?>, Set<Method>>) initBinderCache.get(handlerAdapter);
        log.debug("控制器的 @InitBinder 方法 {}",
                controllerMap.entrySet().stream()
                        .flatMap(e -> e.getValue().stream().map(v -> e.getKey().getSimpleName() + "." + v.getName()))
                        .collect(Collectors.toList())
        );
    }
}
```



##### 总结

1. RequestMappingHandlerAdapter 初始化时会解析 @ControllerAdvice 中的 @InitBinder 方法
2. RequestMappingHandlerAdapter 会以类为单位，在该类首次使用时，解析此类的 @InitBinder 方法
3. 以上两种 @InitBinder 的解析结果都会缓存来避免重复解析
4. 控制器方法调用时，会综合利用本类的 @InitBinder 方法和 @ControllerAdvice 中的 @InitBinder 方法创建绑定工厂



#### 控制器方法执行流程

##### 图1

```mermaid
classDiagram
class ServletInvocableHandlerMethod {
	+invokeAndHandle(ServletWebRequest,ModelAndViewContainer)
}
HandlerMethod <|-- ServletInvocableHandlerMethod
HandlerMethod o-- bean
HandlerMethod o-- method
ServletInvocableHandlerMethod o-- WebDataBinderFactory
ServletInvocableHandlerMethod o-- ParameterNameDiscoverer
ServletInvocableHandlerMethod o-- HandlerMethodArgumentResolverComposite
ServletInvocableHandlerMethod o-- HandlerMethodReturnValueHandlerComposite
```

HandlerMethod 需要

* bean 即是哪个 Controller
* method 即是 Controller 中的哪个方法

ServletInvocableHandlerMethod 需要

* WebDataBinderFactory 负责对象绑定、类型转换
* ParameterNameDiscoverer 负责参数名解析
* HandlerMethodArgumentResolverComposite 负责解析参数
* HandlerMethodReturnValueHandlerComposite 负责处理返回值



##### 图2

```mermaid
sequenceDiagram
participant adapter as RequestMappingHandlerAdapter
participant bf as WebDataBinderFactory
participant mf as ModelFactory
participant container as ModelAndViewContainer
adapter ->> +bf: 准备 @InitBinder
bf -->> -adapter: 
adapter ->> +mf: 准备 @ModelAttribute
mf ->> +container: 添加Model数据
container -->> -mf: 
mf -->> -adapter: 
```

- WebDataBinderFactory 数据绑定工厂
- ModelFactory 模型工厂，用来创建模型数据
- ModelAndViewContainer 存放临时模型数据，`@ControllerAdvice` 的 `@ModelAttribute` 产生的临时数据



##### 图3

```mermaid
sequenceDiagram
participant adapter as RequestMappingHandlerAdapter
participant ihm as ServletInvocableHandlerMethod
participant ar as ArgumentResolvers
participant rh as ReturnValueHandlers
participant container as ModelAndViewContainer

adapter ->> +ihm: invokeAndHandle
ihm ->> +ar: 获取 args
ar ->> ar: 有的解析器涉及 RequestBodyAdvice
ar ->> container: 有的解析器涉及数据绑定生成模型数据
container -->> ar: 
ar -->> -ihm: args
ihm ->> ihm: method.invoke(bean,args) 得到 returnValue
ihm ->> +rh: 处理 returnValue
rh ->> rh: 有的处理器涉及 ResponseBodyAdvice
rh ->> +container: 添加Model数据,处理视图名,是否渲染等
container -->> -rh: 
rh -->> -ihm: 
ihm -->> -adapter: 
adapter ->> +container: 获取 ModelAndView
container -->> -adapter: 
```



- RequestMappingHandlerAdapter
- ServletInvocableHandlerMethod 反射调用控制器方法
- ArgumentResolvers HandlerMethodArgumentResolverComposite 参数解析器，准备参数
- ReturnValueHandlers HandlerMethodReturnValueHandlerComposite  返回值处理器，处理返回值
- ModelAndViewContainer





#### @ModelAttribute

**准备 @ModelAttribute** 在整个 HandlerAdapter 调用过程中所处的位置

```mermaid
sequenceDiagram
participant adapter as HandlerAdapter
participant bf as WebDataBinderFactory
participant mf as ModelFactory
participant ihm as ServletInvocableHandlerMethod
participant ar as ArgumentResolvers 
participant rh as ReturnValueHandlers
participant container as ModelAndViewContainer

adapter ->> +bf: 准备 @InitBinder
bf -->> -adapter: 
rect rgb(200, 150, 255)
adapter ->> +mf: 准备 @ModelAttribute
mf ->> +container: 添加Model数据
container -->> -mf: 
mf -->> -adapter: 
end
adapter ->> +ihm: invokeAndHandle
ihm ->> +ar: 获取 args
ar ->> ar: 有的解析器涉及 RequestBodyAdvice
ar ->> container: 有的解析器涉及数据绑定生成Model数据
ar -->> -ihm: args
ihm ->> ihm: method.invoke(bean,args) 得到 returnValue
ihm ->> +rh: 处理 returnValue
rh ->> rh: 有的处理器涉及 ResponseBodyAdvice
rh ->> +container: 添加Model数据,处理视图名,是否渲染等
container -->> -rh: 
rh -->> -ihm: 
ihm -->> -adapter: 
adapter ->> +container: 获取 ModelAndView
container -->> -adapter: 
```



##### 示例

```java
@Configuration
public class WebConfig {

    @ControllerAdvice
    static class MyControllerAdvice {
        @ModelAttribute("a")
        public String aa() {
            return "aa";
        }
    }

    @Controller
    static class Controller1 {
        @ModelAttribute("b")
        public String aa() {
            return "bb";
        }

        // 使用 @ResponseStatus 为了方便测试，不需要实现 HandlerMethodReturnValueHandlerComposite
        @ResponseStatus(HttpStatus.OK)
        public ModelAndView foo(@ModelAttribute("u") User user) {
            System.out.println("foo");
            return null;
        }
    }

    static class User {
        private String name;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {

            return name;
        }

        @Override
        public String toString() {
            return "User{" +
                   "name='" + name + '\'' +
                   '}';
        }
    }
}
```

```java
public class Application {

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(WebConfig.class);

        RequestMappingHandlerAdapter adapter = new RequestMappingHandlerAdapter();
        adapter.setApplicationContext(context);
        adapter.afterPropertiesSet();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("name", "张三");
        /*
            现在可以通过 ServletInvocableHandlerMethod 把这些整合在一起, 并完成控制器方法的调用, 如下
         */
        ServletInvocableHandlerMethod handlerMethod = new ServletInvocableHandlerMethod(
                new WebConfig.Controller1(), WebConfig.Controller1.class.getMethod("foo", WebConfig.User.class));

        ServletRequestDataBinderFactory factory = new ServletRequestDataBinderFactory(null, null);

        handlerMethod.setDataBinderFactory(factory);
        handlerMethod.setParameterNameDiscoverer(new DefaultParameterNameDiscoverer());
        handlerMethod.setHandlerMethodArgumentResolvers(getArgumentResolvers(context));

        ModelAndViewContainer container = new ModelAndViewContainer();

        // 获取模型工厂方法
        Method getModelFactory = RequestMappingHandlerAdapter.class.getDeclaredMethod("getModelFactory", HandlerMethod.class, WebDataBinderFactory.class);
        getModelFactory.setAccessible(true);
        ModelFactory modelFactory = (ModelFactory) getModelFactory.invoke(adapter, handlerMethod, factory);

        // 初始化模型数据
        modelFactory.initModel(new ServletWebRequest(request), container, handlerMethod);

        handlerMethod.invokeAndHandle(new ServletWebRequest(request), container);

        System.out.println(container.getModel());

        context.close();

        /*
            a. 控制器方法是如何调用的
            b. 模型数据如何产生
            c. advice 之二, @ModelAttribute 补充模型数据
         */
    }

    public static HandlerMethodArgumentResolverComposite getArgumentResolvers(AnnotationConfigApplicationContext context) {
        HandlerMethodArgumentResolverComposite composite = new HandlerMethodArgumentResolverComposite();
        composite.addResolvers(
                new RequestParamMethodArgumentResolver(context.getDefaultListableBeanFactory(), false),
                new PathVariableMethodArgumentResolver(),
                new RequestHeaderMethodArgumentResolver(context.getDefaultListableBeanFactory()),
                new ServletCookieValueMethodArgumentResolver(context.getDefaultListableBeanFactory()),
                new ExpressionValueMethodArgumentResolver(context.getDefaultListableBeanFactory()),
                new ServletRequestMethodArgumentResolver(),
                new ServletModelAttributeMethodProcessor(false),
                new RequestResponseBodyMethodProcessor(Collections.singletonList(new MappingJackson2HttpMessageConverter())),
                new ServletModelAttributeMethodProcessor(true),
                new RequestParamMethodArgumentResolver(context.getDefaultListableBeanFactory(), true)
        );
        return composite;
    }

}
```





##### 总结

1. RequestMappingHandlerAdapter 初始化时会解析 @ControllerAdvice 中的 @ModelAttribute 方法
2. RequestMappingHandlerAdapter 会以类为单位，在该类首次使用时，解析此类的 @ModelAttribute 方法
3. 以上两种 @ModelAttribute 的解析结果都会缓存来避免重复解析
4. 控制器方法调用时，会综合利用本类的 @ModelAttribute 方法和 @ControllerAdvice 中的 @ModelAttribute 方法创建模型工厂



#### ResponseBodyAdvice

##### 示例

**ResponseBodyAdvice 增强** 在整个 HandlerAdapter 调用过程中所处的位置

```mermaid
sequenceDiagram
participant adapter as HandlerAdapter
participant bf as WebDataBinderFactory
participant mf as ModelFactory
participant ihm as ServletInvocableHandlerMethod
participant ar as ArgumentResolvers 
participant rh as ReturnValueHandlers
participant container as ModelAndViewContainer

adapter ->> +bf: 准备 @InitBinder
bf -->> -adapter: 
adapter ->> +mf: 准备 @ModelAttribute
mf ->> +container: 添加Model数据
container -->> -mf: 
mf -->> -adapter: 
adapter ->> +ihm: invokeAndHandle
ihm ->> +ar: 获取 args
ar ->> ar: 有的解析器涉及 RequestBodyAdvice
ar ->> container: 有的解析器涉及数据绑定生成Model数据
ar -->> -ihm: args
ihm ->> ihm: method.invoke(bean,args) 得到 returnValue
ihm ->> +rh: 处理 returnValue
rect rgb(200, 150, 255)
rh ->> rh: 有的处理器涉及 ResponseBodyAdvice
end
rh ->> +container: 添加Model数据,处理视图名,是否渲染等
container -->> -rh: 
rh -->> -ihm: 
ihm -->> -adapter: 
adapter ->> +container: 获取 ModelAndView
container -->> -adapter: 
```

```java
@Configuration
public class WebConfig {

    @ControllerAdvice
    static class MyControllerAdvice implements ResponseBodyAdvice<Object> {
        // 满足条件才转换
        public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
            /**
             * @ResponseBody
             * @RestController
             */
            if (returnType.getMethodAnnotation(ResponseBody.class) != null ||
                AnnotationUtils.findAnnotation(returnType.getContainingClass(), ResponseBody.class) != null) {
                return true;
            }
            return false;
        }

        // 将 User 或其它类型统一为 Result 类型
        public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
            if (body instanceof Result) {
                return body;
            }
            return Result.ok(body);
        }
    }

    // @Controller
    // @ResponseBody
    @RestController
    public static class MyController {
        public User user() {
            return new User("王五", 18);
        }
    }

    public static class User {
        private String name;
        private int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
```

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result {
    private int code;
    private String msg;
    private Object data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @JsonCreator
    private Result(@JsonProperty("code") int code, @JsonProperty("data") Object data) {
        this.code = code;
        this.data = data;
    }

    private Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static Result ok() {
        return new Result(200, null);
    }

    public static Result ok(Object data) {
        return new Result(200, data);
    }

    public static Result error(String msg) {
        return new Result(500, "服务器内部错误:" + msg);
    }
}
```

```java
public class Application {

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(WebConfig.class);

        ServletInvocableHandlerMethod handlerMethod = new ServletInvocableHandlerMethod(
                context.getBean(WebConfig.MyController.class),
                WebConfig.MyController.class.getMethod("user")
        );
        handlerMethod.setDataBinderFactory(new ServletRequestDataBinderFactory(Collections.emptyList(), null));
        handlerMethod.setParameterNameDiscoverer(new DefaultParameterNameDiscoverer());
        handlerMethod.setHandlerMethodArgumentResolvers(getArgumentResolvers(context));
        handlerMethod.setHandlerMethodReturnValueHandlers(getReturnValueHandlers(context));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndViewContainer container = new ModelAndViewContainer();
        handlerMethod.invokeAndHandle(new ServletWebRequest(request, response), container);

        System.out.println(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8));
    }

    public static HandlerMethodArgumentResolverComposite getArgumentResolvers(AnnotationConfigApplicationContext context) {
        HandlerMethodArgumentResolverComposite composite = new HandlerMethodArgumentResolverComposite();
        composite.addResolvers(
                new RequestParamMethodArgumentResolver(context.getDefaultListableBeanFactory(), false),
                new PathVariableMethodArgumentResolver(),
                new RequestHeaderMethodArgumentResolver(context.getDefaultListableBeanFactory()),
                new ServletCookieValueMethodArgumentResolver(context.getDefaultListableBeanFactory()),
                new ExpressionValueMethodArgumentResolver(context.getDefaultListableBeanFactory()),
                new ServletRequestMethodArgumentResolver(),
                new ServletModelAttributeMethodProcessor(false),
                new RequestResponseBodyMethodProcessor(Collections.singletonList(new MappingJackson2HttpMessageConverter())),
                new ServletModelAttributeMethodProcessor(true),
                new RequestParamMethodArgumentResolver(context.getDefaultListableBeanFactory(), true)
        );
        return composite;
    }

    public static HandlerMethodReturnValueHandlerComposite getReturnValueHandlers(AnnotationConfigApplicationContext context) {
        // 添加 advice
        List<ControllerAdviceBean> annotatedBeans = ControllerAdviceBean.findAnnotatedBeans(context);
        List<Object> collect = annotatedBeans.stream().filter(b -> ResponseBodyAdvice.class.isAssignableFrom(b.getBeanType()))
                .collect(Collectors.toList());

        HandlerMethodReturnValueHandlerComposite composite = new HandlerMethodReturnValueHandlerComposite();
        composite.addHandler(new ModelAndViewMethodReturnValueHandler());
        composite.addHandler(new ViewNameMethodReturnValueHandler());
        composite.addHandler(new ServletModelAttributeMethodProcessor(false));
        composite.addHandler(new HttpEntityMethodProcessor(Collections.singletonList(new MappingJackson2HttpMessageConverter())));
        composite.addHandler(new HttpHeadersReturnValueHandler());
        composite.addHandler(new RequestResponseBodyMethodProcessor(Collections.singletonList(new MappingJackson2HttpMessageConverter()), collect));
        composite.addHandler(new ServletModelAttributeMethodProcessor(true));
        return composite;
    }
}
```



##### 总结

1. ResponseBodyAdvice 返回响应体前包装



#### 异常处理

##### @ExceptionHandler

ExceptionHandlerExceptionResolver

###### 异常处理器调用过程

```java
public class DispatcherServlet extends FrameworkServlet {
       protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpServletRequest processedRequest = request;
        HandlerExecutionChain mappedHandler = null;
        try {
            try {
                try {
                    processedRequest = this.checkMultipart(request);
                    mappedHandler = this.getHandler(processedRequest);
                    HandlerAdapter ha = this.getHandlerAdapter(mappedHandler.getHandler());
                    // 控制器方法调用
                    mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
                } catch (Exception var20) {
                    dispatchException = var20;
                } catch (Throwable var21) {
                    dispatchException = new NestedServletException("Handler dispatch failed", var21);
                }
				// 处理调用结果
                this.processDispatchResult(processedRequest, response, mappedHandler, mv, (Exception)dispatchException);
            } catch (Exception var22) {
			// ...
            } catch (Throwable var23) {
			// ...
            }

        } finally {
			// ...
        }
    }
    
    private void processDispatchResult(HttpServletRequest request, HttpServletResponse response, @Nullable HandlerExecutionChain mappedHandler, @Nullable ModelAndView mv, @Nullable Exception exception) throws Exception {
        boolean errorView = false;
        // 发生异常，进行异常处理
        if (exception != null) {
            if (exception instanceof ModelAndViewDefiningException) {
                this.logger.debug("ModelAndViewDefiningException encountered", exception);
                mv = ((ModelAndViewDefiningException)exception).getModelAndView();
            } else {
                Object handler = mappedHandler != null ? mappedHandler.getHandler() : null;
                mv = this.processHandlerException(request, response, handler, exception);
                errorView = mv != null;
            }
        }

        // ...
    }
    
    @Nullable
    protected ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex) throws Exception {
        request.removeAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);
        ModelAndView exMv = null;
        // 查找异常处理器来处理异常
        if (this.handlerExceptionResolvers != null) {
            Iterator var6 = this.handlerExceptionResolvers.iterator();

            while(var6.hasNext()) {
                HandlerExceptionResolver resolver = (HandlerExceptionResolver)var6.next();
                exMv = resolver.resolveException(request, response, handler, ex);
                if (exMv != null) {
                    break;
                }
            }
        }
		// ...
    }
}
```



###### 示例

异常处理器 ExceptionHandlerExceptionResolver

该异常处理器用来处理 `@ExceptionHandler` 的异常方法

```java
public class Application {
    public static void main(String[] args) throws NoSuchMethodException {
        ExceptionHandlerExceptionResolver resolver = new ExceptionHandlerExceptionResolver();
        resolver.setMessageConverters(Collections.singletonList(new MappingJackson2HttpMessageConverter()));
        // 添加参数、返回值处理器
        resolver.afterPropertiesSet();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        System.out.println("1.测试 json");
        HandlerMethod handlerMethod = new HandlerMethod(new Controller1(), Controller1.class.getMethod("foo"));
        Exception e = new ArithmeticException("被零除");
        resolver.resolveException(request, response, handlerMethod, e);
        System.out.println(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8));

        System.out.println("2.测试 mav");
        handlerMethod = new HandlerMethod(new Controller2(), Controller2.class.getMethod("foo"));
        e = new ArithmeticException("被零除");
        ModelAndView mav = resolver.resolveException(request, response, handlerMethod, e);
        System.out.println(mav.getModel());
        System.out.println(mav.getViewName());

        System.out.println("3.测试嵌套异常");
        handlerMethod = new HandlerMethod(new Controller3(), Controller3.class.getMethod("foo"));
        e = new Exception("e1", new RuntimeException("e2", new IOException("e3")));
        resolver.resolveException(request, response, handlerMethod, e);
        System.out.println(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8));

        System.out.println("4.测试异常处理方法参数解析");

        handlerMethod = new HandlerMethod(new Controller4(), Controller4.class.getMethod("foo"));
        e = new Exception("e1");
        resolver.resolveException(request, response, handlerMethod, e);
        System.out.println(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8));
    }

    static class Controller1 {
        public void foo() {

        }
        @ExceptionHandler
        @ResponseBody
        public Map<String, Object> handle(ArithmeticException e) {
            Map<String, Object> map = new HashMap<>(1);
            map.put("error", e.getMessage());
            return map;
        }
    }

    static class Controller2 {
        public void foo() {

        }
        @ExceptionHandler
        public ModelAndView handle(ArithmeticException e) {
            Map<String, Object> map = new HashMap<>(1);
            map.put("error", e.getMessage());
            return new ModelAndView("test2", map);
        }
    }

    static class Controller3 {
        public void foo() {

        }
        @ExceptionHandler
        @ResponseBody
        public Map<String, Object> handle(IOException e) {
            Map<String, Object> map = new HashMap<>(1);
            map.put("error", e.getMessage());
            return map;
        }
    }

    static class Controller4 {
        public void foo() {}
        @ExceptionHandler
        @ResponseBody
        public Map<String, Object> handler(Exception e, HttpServletRequest request) {
            System.out.println(request);
            Map<String, Object> map = new HashMap<>(1);
            map.put("error", e.getMessage());
            return map;
        }
    }
}
```



###### afterPropertiesSet()

Spring 初始化时调用

```java
public void afterPropertiesSet() {
    // 缓存解析器
    this.initExceptionHandlerAdviceCache();
    List handlers;
    // 添加常用参数解析器
    if (this.argumentResolvers == null) {
        handlers = this.getDefaultArgumentResolvers();
        this.argumentResolvers = (new HandlerMethodArgumentResolverComposite()).addResolvers(handlers);
    }

    // 添加常用返回值处理器
    if (this.returnValueHandlers == null) {
        handlers = this.getDefaultReturnValueHandlers();
        this.returnValueHandlers = (new HandlerMethodReturnValueHandlerComposite()).addHandlers(handlers);
    }

}

private void initExceptionHandlerAdviceCache() {
    if (this.getApplicationContext() != null) {
        List<ControllerAdviceBean> adviceBeans = ControllerAdviceBean.findAnnotatedBeans(this.getApplicationContext());
        Iterator var2 = adviceBeans.iterator();

        while(var2.hasNext()) {
            ControllerAdviceBean adviceBean = (ControllerAdviceBean)var2.next();
            Class<?> beanType = adviceBean.getBeanType();
            // ... 
            ExceptionHandlerMethodResolver resolver = new ExceptionHandlerMethodResolver(beanType);
            if (resolver.hasExceptionMappings()) {
                this.exceptionHandlerAdviceCache.put(adviceBean, resolver);
            }

            if (ResponseBodyAdvice.class.isAssignableFrom(beanType)) {
                this.responseBodyAdvice.add(adviceBean);
            }
            // ...
        }
    }
}
```



###### 总结

1. ExceptionHandlerExceptionResolver 能够重用参数解析器、返回值处理器，实现组件重用
2. 能够支持嵌套异常，在解析异常时会将嵌套的异常展开为数组



##### 全局@ExceptionHandler

当我们没有在 Controller 中使用 `@ExceptionHandler` 进行异常处理时，希望有一个可以进行全局处理异常的 `@ExceptionHandler`

###### 示例

```java
@Configuration
public class WebConfig {
    @ControllerAdvice
    static class MyControllerAdvice {
        @ExceptionHandler
        @ResponseBody
        public Map<String, Object> handle(Exception e) {
            Map<String, Object> map = new HashMap<>();
            map.put("error", e.getMessage());
            return map;
        }
    }

    @Bean
    public ExceptionHandlerExceptionResolver resolver() {
        List list = new ArrayList();
        list.add(new MappingJackson2HttpMessageConverter());
        ExceptionHandlerExceptionResolver resolver = new ExceptionHandlerExceptionResolver();
        resolver.setMessageConverters(list);
        return resolver;
    }
}
```

```java
public class Application {
    public static void main(String[] args) throws NoSuchMethodException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

//        ExceptionHandlerExceptionResolver resolver = new ExceptionHandlerExceptionResolver();
//        resolver.setMessageConverters(List.of(new MappingJackson2HttpMessageConverter()));
//        resolver.afterPropertiesSet();

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(WebConfig.class);
        ExceptionHandlerExceptionResolver resolver = context.getBean(ExceptionHandlerExceptionResolver.class);

        HandlerMethod handlerMethod = new HandlerMethod(new Controller5(), Controller5.class.getMethod("foo"));
        Exception e = new Exception("e1");
        resolver.resolveException(request, response, handlerMethod, e);
        System.out.println(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8));
    }

    static class Controller5 {
        public void foo() {

        }
    }
}
```



###### 总结

1. ExceptionHandlerExceptionResolver 初始化时会解析 @ControllerAdvice 中的 @ExceptionHandler 方法
2. ExceptionHandlerExceptionResolver 会以类为单位，在该类首次处理异常时，解析此类的 @ExceptionHandler 方法
3. 以上两种 @ExceptionHandler 的解析结果都会缓存来避免重复解析



### 返回值处理器

#### 常见返回值处理器

```java
org.springframework.web.servlet.mvc.method.annotation.ModelAndViewMethodReturnValueHandler@4c9e38
org.springframework.web.method.annotation.ModelMethodProcessor@5d1e09bc
org.springframework.web.servlet.mvc.method.annotation.ViewMethodReturnValueHandler@4bdc8b5d
org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitterReturnValueHandler@3bcd426c
org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBodyReturnValueHandler@5f14a673
org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor@726a17c4
org.springframework.web.servlet.mvc.method.annotation.HttpHeadersReturnValueHandler@5dc3fcb7
org.springframework.web.servlet.mvc.method.annotation.CallableMethodReturnValueHandler@c4c0b41
org.springframework.web.servlet.mvc.method.annotation.DeferredResultMethodReturnValueHandler@76911385
org.springframework.web.servlet.mvc.method.annotation.AsyncTaskMethodReturnValueHandler@5467eea4
org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor@160396db
org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor@7a799159
org.springframework.web.servlet.mvc.method.annotation.ViewNameMethodReturnValueHandler@40ab8a8
org.springframework.web.method.annotation.MapMethodProcessor@6ff37443
org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor@65cc8228
```



#### 示例

> `Spring Boot` 版本为 `2.5.5`

```java
@Configuration
public class WebConfig {

    @Bean
    public FreeMarkerConfigurer freeMarkerConfigurer() throws IOException {
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setDefaultEncoding("utf-8");
        configurer.setTemplateLoaderPath("classpath:templates");
        return configurer;
    }

    @Bean // FreeMarkerView 在借助 Spring 初始化时，会要求 web 环境才会走 setConfiguration, 这里想办法去掉了 web 环境的约束
    public FreeMarkerViewResolver viewResolver(FreeMarkerConfigurer configurer) {
        FreeMarkerViewResolver resolver = new FreeMarkerViewResolver(){
//            @Override
            protected AbstractUrlBasedView instantiateView() {
                FreeMarkerView view = new FreeMarkerView() {
                    @Override
                    protected boolean isContextRequired() {
                        return false;
                    }
                };
                view.setConfiguration(configurer.getConfiguration());
                return view;
            }
        };
        resolver.setContentType("text/html;charset=utf-8");
        resolver.setPrefix("/");
        resolver.setSuffix(".ftl");
        resolver.setExposeSpringMacroHelpers(false);
        return resolver;
    }

}
```

```java
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(WebConfig.class);
        // 1. 测试返回值类型为 ModelAndView
        test(context, "test1", null);

        // 2. 测试返回值类型为 String 时, 把它当做视图名
        test(context, "test2", null);

        // 3. 测试返回值添加了 @ModelAttribute 注解时, 此时需找到默认视图名
        test(context, "test3", "/test3");

        // 4. 测试返回值不加 @ModelAttribute 注解且返回非简单类型时, 此时需找到默认视图名
        test(context, "test4", "/test4");

        // 5. 测试返回值类型为 ResponseEntity 时, 此时不走视图流程
        test(context, "test5", null);

        // 6. 测试返回值类型为 HttpHeaders 时, 此时不走视图流程
        test(context, "test6", null);

        // 7. 测试返回值添加了 @ResponseBody 注解时, 此时不走视图流程
        test(context, "test7", null);
    }

    private static void test(AnnotationConfigApplicationContext context, String methodName, String uri) throws Exception {
        Method method = Controller.class.getMethod(methodName);
        Controller controller = new Controller();
        Object returnValue = method.invoke(controller); // 获取返回值

        HandlerMethod methodHandle = new HandlerMethod(controller, method);

        ModelAndViewContainer container = new ModelAndViewContainer();
        HandlerMethodReturnValueHandlerComposite composite = getReturnValueHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        if(null != uri){
            request.setRequestURI(uri);
            // 将请求 uri 存入 request 域，当没有找到视图名时将获取 uri 作为视图名
            UrlPathHelper.defaultInstance.resolveAndCacheLookupPath(request);
        }
        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        if (composite.supportsReturnType(methodHandle.getReturnType())) { // 检查是否支持此类型的返回值
            composite.handleReturnValue(returnValue, methodHandle.getReturnType(), container, webRequest);
            System.out.println(container.getModel());
            System.out.println(container.getViewName());
            // 返回值为 ResponseEntity、HttpHeaders、@ResponseBody 的处理器中 `handleReturnValue` 方法内 `mavContainer.setRequestHandled(true);` 设置了不需要视图解析
            if (!container.isRequestHandled()) {
                renderView(context, container, webRequest); // 渲染视图
            } else {
                for (String name : response.getHeaderNames()) {
                    System.out.println(name + "=" + response.getHeader(name));
                }
                System.out.println(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8));
            }
        }
    }

    public static HandlerMethodReturnValueHandlerComposite getReturnValueHandler() {
        HandlerMethodReturnValueHandlerComposite composite = new HandlerMethodReturnValueHandlerComposite();
        composite.addHandler(new ModelAndViewMethodReturnValueHandler());
        composite.addHandler(new ViewNameMethodReturnValueHandler());
        composite.addHandler(new ServletModelAttributeMethodProcessor(false));
        composite.addHandler(new HttpEntityMethodProcessor(Collections.singletonList(new MappingJackson2HttpMessageConverter())));
        composite.addHandler(new HttpHeadersReturnValueHandler());
        composite.addHandler(new RequestResponseBodyMethodProcessor(Collections.singletonList(new MappingJackson2HttpMessageConverter())));
        composite.addHandler(new ServletModelAttributeMethodProcessor(true));
        return composite;
    }

    @SuppressWarnings("all")
    private static void renderView(AnnotationConfigApplicationContext context, ModelAndViewContainer container,
                                   ServletWebRequest webRequest) throws Exception {
        log.debug(">>>>>> 渲染视图");
        FreeMarkerViewResolver resolver = context.getBean(FreeMarkerViewResolver.class);
        String viewName = container.getViewName() != null ? container.getViewName() : new DefaultRequestToViewNameTranslator().getViewName(webRequest.getRequest());
        log.debug("没有获取到视图名, 采用默认视图名: {}", viewName);
        // 每次渲染时, 会产生新的视图对象, 它并非被 Spring 所管理, 但确实借助了 Spring 容器来执行初始化
        View view = resolver.resolveViewName(viewName, Locale.getDefault());
        view.render(container.getModel(), webRequest.getRequest(), webRequest.getResponse());
        System.out.println(new String(((MockHttpServletResponse) webRequest.getResponse()).getContentAsByteArray(), StandardCharsets.UTF_8));
    }

    static class Controller {
        private static final Logger log = LoggerFactory.getLogger(Controller.class);

        public ModelAndView test1() {
            log.debug("test1()");
            ModelAndView mav = new ModelAndView("view1");
            mav.addObject("name", "张三");
            return mav;
        }

        public String test2() {
            log.debug("test2()");
            return "view2";
        }

        @ModelAttribute
//        @RequestMapping("/test3")
        // 返回值缺少视图名，找不到视图名时会使用路径作为视图名
        public User test3() {
            log.debug("test3()");
            return new User("李四", 20);
        }

        public User test4() {
            log.debug("test4()");
            return new User("王五", 30);
        }

        public HttpEntity<User> test5() {
            log.debug("test5()");
            return new HttpEntity<>(new User("赵六", 40));
        }

        public HttpHeaders test6() {
            log.debug("test6()");
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "text/html");
            return headers;
        }

        @ResponseBody
        public User test7() {
            log.debug("test7()");
            return new User("钱七", 50);
        }
    }

    // 必须用 public 修饰, 否则 freemarker 渲染其 name, age 属性时失败
    public static class User {
        private String name;
        private int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "User{" +
                   "name='" + name + '\'' +
                   ", age=" + age +
                   '}';
        }
    }
}
```



#### 总结

1. 常见的返回值处理器
   * ModelAndView，分别获取其模型和视图名，放入 ModelAndViewContainer
   * 返回值类型为 String 时，把它当做视图名，放入 ModelAndViewContainer
   * 返回值添加了 @ModelAttribute 注解时，将返回值作为模型，放入 ModelAndViewContainer
     * 此时需找到默认视图名（使用路径作为视图名）
   * 返回值省略 @ModelAttribute 注解且返回非简单类型时，将返回值作为模型，放入 ModelAndViewContainer
     * 此时需找到默认视图名（使用路径作为视图名）
   * 返回值类型为 ResponseEntity 时
     * 不走视图相应流程，此时走 MessageConverter，并设置 ModelAndViewContainer.requestHandled 为 true
     * 处理器中 `handleReturnValue` 方法内 `mavContainer.setRequestHandled(true);` 设置了不需要视图解析
   * 返回值类型为 HttpHeaders 时
     * 不走视图相应流程，会设置 ModelAndViewContainer.requestHandled 为 true
     * 处理器中 `handleReturnValue` 方法内 `mavContainer.setRequestHandled(true);` 设置了不需要视图解析
   * 返回值添加了 @ResponseBody 注解时
     * 不走视图相应流程，此时走 MessageConverter，并设置 ModelAndViewContainer.requestHandled 为 true
     * 处理器中 `handleReturnValue` 方法内 `mavContainer.setRequestHandled(true);` 设置了不需要视图解析
2. 组合模式在 Spring 中的体现 + 1



### MessageConverter

#### 示例

```java
public class Application {
    public static void main(String[] args) throws IOException, NoSuchMethodException, HttpMediaTypeNotAcceptableException {
        test1();
        test2();
        test3();
        test4();
    }

    private static void test4() throws IOException, HttpMediaTypeNotAcceptableException, NoSuchMethodException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletWebRequest webRequest = new ServletWebRequest(request, response);

        request.addHeader("Accept", "application/xml");
        response.setContentType("application/json");
        List messageConverterList = new ArrayList<>(2);
        messageConverterList.add(new MappingJackson2HttpMessageConverter());
        messageConverterList.add(new MappingJackson2XmlHttpMessageConverter());
        RequestResponseBodyMethodProcessor processor = new RequestResponseBodyMethodProcessor(messageConverterList);
        processor.handleReturnValue(
                new User("张三", 18),
                new MethodParameter(Application.class.getMethod("user"), -1),
                new ModelAndViewContainer(),
                webRequest
        );
        System.out.println(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8));
    }

    @ResponseBody
    @RequestMapping(produces = "application/json")
    public User user() {
        return null;
    }

    /**
     * json 字符串转 java 对象
     */
    private static void test3() throws IOException {
        MockHttpInputMessage message = new MockHttpInputMessage("{\"name\":\"李四\",\"age\":20}".getBytes(StandardCharsets.UTF_8));
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        if (converter.canRead(User.class, MediaType.APPLICATION_JSON)) {
            Object read = converter.read(User.class, message);
            System.out.println(read);
        }
    }

    /**
     * java 对象转 xml 格式字符串
     */
    private static void test2() throws IOException {
        MockHttpOutputMessage message = new MockHttpOutputMessage();
        MappingJackson2XmlHttpMessageConverter converter = new MappingJackson2XmlHttpMessageConverter();
        if (converter.canWrite(User.class, MediaType.APPLICATION_XML)) {
            converter.write(new User("李四", 20), MediaType.APPLICATION_XML, message);
            System.out.println(message.getBodyAsString());
        }
    }

    /**
     * java 对象转 json 格式字符串
     */
    public static void test1() throws IOException {
        MockHttpOutputMessage message = new MockHttpOutputMessage();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        if (converter.canWrite(User.class, MediaType.APPLICATION_JSON)) {
            converter.write(new User("张三", 18), MediaType.APPLICATION_JSON, message);
            System.out.println(message.getBodyAsString());
        }
    }

    public static class User {
        private String name;
        private int age;

        @JsonCreator
        public User(@JsonProperty("name") String name, @JsonProperty("age") int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "User{" +
                   "name='" + name + '\'' +
                   ", age=" + age +
                   '}';
        }
    }
}
```







#### 总结

1. MessageConverter 的作用
   * @ResponseBody 是返回值处理器解析的
   * 但具体转换工作是 MessageConverter 做的
2. 如何选择 MediaType
   * 首先看 @RequestMapping 上有没有指定
   * 其次看 request 的 Accept 头有没有指定
   * 最后按 MessageConverter 的顺序, 谁能谁先转换





### Tomcat 异常处理

* 我们知道 @ExceptionHandler 只能处理发生在 mvc 流程中的异常，例如控制器内、拦截器内，那么如果是 Filter 出现了异常，如何进行处理呢？

* 在 Spring Boot 中，是这么实现的：
  1. 因为内嵌了 Tomcat 容器，因此可以配置 Tomcat 的错误页面，Filter 与 错误页面之间是通过请求转发跳转的，可以在这里做手脚
  2. 先通过 ErrorPageRegistrarBeanPostProcessor 这个后处理器配置错误页面地址，默认为 `/error` 也可以通过 `${server.error.path}` 进行配置
  3. 当 Filter 发生异常时，不会走 Spring 流程，但会走 Tomcat 的错误处理，于是就希望转发至 `/error` 这个地址
     * 当然，如果没有 @ExceptionHandler，那么最终也会走到 Tomcat 的错误处理
  4. Spring Boot 又提供了一个 BasicErrorController，它就是一个标准 @Controller，@RequestMapping 配置为 `/error`，所以处理异常的职责就又回到了 Spring
  5. 异常信息由于会被 Tomcat 放入 request 作用域，因此 BasicErrorController 里也能获取到
  6. 具体异常信息会由 DefaultErrorAttributes 封装好
  7. BasicErrorController 通过 Accept 头判断需要生成哪种 MediaType 的响应
     * 如果要的不是 text/html，走 MessageConverter 流程
     * 如果需要 text/html，走 mvc 流程，此时又分两种情况
       * 配置了 ErrorViewResolver，根据状态码去找 View
       * 没配置或没找到，用 BeanNameViewResolver 根据一个固定为 error 的名字找到 View，即所谓的 WhitelabelErrorView



#### 示例基础代码

```java
@Configuration
public class WebConfig {
    @Bean
    public TomcatServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory();
    }

    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

    @Bean
    public DispatcherServletRegistrationBean servletRegistrationBean(DispatcherServlet dispatcherServlet) {
        DispatcherServletRegistrationBean registrationBean = new DispatcherServletRegistrationBean(dispatcherServlet, "/");
        registrationBean.setLoadOnStartup(1);
        return registrationBean;
    }

    @Bean // @RequestMapping
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping();
    }

    @Bean // 注意默认的 RequestMappingHandlerAdapter 不会带 jackson 转换器
    public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
        RequestMappingHandlerAdapter handlerAdapter = new RequestMappingHandlerAdapter();
        List list = new ArrayList();
        list.add(new MappingJackson2HttpMessageConverter());
        handlerAdapter.setMessageConverters(list);
        return handlerAdapter;
    }

    @Controller
    public static class MyController {
        @RequestMapping("test")
        public ModelAndView test() {
            int i = 1 / 0;
            return null;
        }
}
```

```java
public class Application {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        AnnotationConfigServletWebServerApplicationContext context =
                new AnnotationConfigServletWebServerApplicationContext(WebConfig.class);
        RequestMappingHandlerMapping handlerMapping = context.getBean(RequestMappingHandlerMapping.class);
        handlerMapping.getHandlerMethods().forEach((RequestMappingInfo k, HandlerMethod v) -> {
            System.out.println("映射路径:" + k + "\t方法信息:" + v);
        });
    }
}
```



#### 示例 - Tomcat 的错误页处理

```java
// 自定义错误处理 1.修改了 Tomcat 服务器默认错误地址
// ErrorPageRegistrar 是 TomcatServletWebServerFactory 的父接口
@Bean
public ErrorPageRegistrar errorPageRegistrar() {
    // 出现错误，会使用请求转发 forward 跳转到 error 地址
    // error 地址可以是 servlet 地址、静态页面地址、Controller 地址
    return webServerFactory -> webServerFactory.addErrorPages(new ErrorPage("/error"));
}

// 自定义错误处理 2.添加后处理器
// TomcatServletWebServerFactory 初始化前调用用它增强，注册所有 ErrorPageRegistrar
@Bean
public ErrorPageRegistrarBeanPostProcessor errorPageRegistrarBeanPostProcessor() {
    return new ErrorPageRegistrarBeanPostProcessor();
}

@Controller
public static class MyController {
//   自定义错误处理 3.异常展示
    @RequestMapping("/error")
    @ResponseBody
    public Map<String, Object> error(HttpServletRequest request) {
        // 出现异常时 tomcat 会把异常存储到 request 作用域中
        Throwable e = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Map<String, Object> map = new HashMap<>();
        map.put("error", e.getMessage());
        return map;
    }
}
```



#### 示例 - Spring Boot 的 BasicErrorController

```java
// 自定义错误处理 1.修改了 Tomcat 服务器默认错误地址
// ErrorPageRegistrar 是 TomcatServletWebServerFactory 的父接口
@Bean
public ErrorPageRegistrar errorPageRegistrar() {
    // 出现错误，会使用请求转发 forward 跳转到 error 地址
    // error 地址可以是 servlet 地址、静态页面地址、Controller 地址
    return webServerFactory -> webServerFactory.addErrorPages(new ErrorPage("/error"));
}

// 自定义错误处理 2.添加后处理器
// TomcatServletWebServerFactory 初始化前调用用它增强，注册所有 ErrorPageRegistrar
@Bean
public ErrorPageRegistrarBeanPostProcessor errorPageRegistrarBeanPostProcessor() {
    return new ErrorPageRegistrarBeanPostProcessor();
}

//    自定义错误处理 3.SpringBoot 中的 BasicErrorController
@Bean
public BasicErrorController basicErrorController() {
    ErrorProperties errorProperties = new ErrorProperties();
    errorProperties.setIncludeException(true);
    return new BasicErrorController(new DefaultErrorAttributes(), errorProperties);
}

//    提供视图
@Bean
public View error() {
    return new View() {
        @Override
        public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
            System.out.println(model);
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print("<h3>服务器内部错误</h3>");
        }
    };
}

//    视图解析器
@Bean
public ViewResolver viewResolver() {
    // 按照视图名找到对应名字的视图对象
    return new BeanNameViewResolver();
}
```



### BeanNameUrlHandlerMapping 与 SimpleControllerHandlerAdapter

路径映射和控制器方法调用，属于 Spring MVC 早期的实现

#### 示例

```java
@Configuration
public class WebConfig {
    @Bean // 内嵌 web 容器工厂
    public TomcatServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory(8080);
    }

    @Bean // 创建 DispatcherServlet
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

    @Bean // 注册 DispatcherServlet, Spring MVC 的入口
    public DispatcherServletRegistrationBean servletRegistrationBean(DispatcherServlet dispatcherServlet) {
        return new DispatcherServletRegistrationBean(dispatcherServlet, "/");
    }

    // /c1  -->  /c1
    // /c2  -->  /c2
    @Bean
    public BeanNameUrlHandlerMapping beanNameUrlHandlerMapping() {
        return new BeanNameUrlHandlerMapping();
    }

    @Bean
    public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
        return new SimpleControllerHandlerAdapter();
    }

    @Component("/c1")
    public static class Controller1 implements Controller {
        @Override
        public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
            response.getWriter().print("this is c1");
            return null;
        }
    }

    @Component("/c2")
    public static class Controller2 implements Controller {
        @Override
        public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
            response.getWriter().print("this is c2");
            return null;
        }
    }

    @Bean("/c3")
    public Controller controller3() {
        return (request, response) -> {
            response.getWriter().print("this is c3");
            return null;
        };
    }
}
```

```java
public class Application {
    public static void main(String[] args) {
        AnnotationConfigServletWebServerApplicationContext context
                = new AnnotationConfigServletWebServerApplicationContext(WebConfig.class);
    }
}
```



#### 自定义 HandlerMapping 和 HandlerAdapter

将上例中创建 HandlerMapping 和 HandlerAdapter 的方法改为如下

```java
// /c1  -->  /c1
// /c2  -->  /c2
@Component
static class MyHandlerMapping implements HandlerMapping {
    @Override
    public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        String key = request.getRequestURI();
        Controller controller = collect.get(key);
        if (controller == null) {
            return null;
        }
        return new HandlerExecutionChain(controller);
    }

    @Autowired
    private ApplicationContext context;
    private Map<String, Controller> collect;

    @PostConstruct
    public void init() {
        collect = context.getBeansOfType(Controller.class).entrySet()
                .stream().filter(e -> e.getKey().startsWith("/"))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        System.out.println(collect);
    }
}

@Component
static class MyHandlerAdapter implements HandlerAdapter {

    @Override
    public boolean supports(Object handler) {
        return handler instanceof Controller;
    }

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof Controller) {
            Controller controller = (Controller) handler;
            controller.handleRequest(request, response);
        }
        return null;
    }

    @Override
    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1;
    }
}
```



#### 总结

1. BeanNameUrlHandlerMapping，以  `/` 开头的 bean 的名字会被当作映射路径
2. 这些 bean 本身当作 handler，要求实现 Controller 接口
3. SimpleControllerHandlerAdapter，调用 handler
4. 模拟实现这组映射器和适配器
5. 对比 RequestMappingHandlerMapping 与 RequestMappingHandlerAdapter
   - RequestMappingHandlerAdapter, 以 @RequestMapping 作为映射路径
   - 控制器的具体方法会被当作 handler
   - RequestMappingHandlerAdapter, 调用 handler





### RouterFunctionMapping 与 HandlerFunctionAdapter

Spring 5.2 开始才引入

#### 示例

```java
@Configuration
public class WebConfig {
    @Bean // 内嵌 web 容器工厂
    public TomcatServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory(8080);
    }

    @Bean // 创建 DispatcherServlet
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

    @Bean // 注册 DispatcherServlet, Spring MVC 的入口
    public DispatcherServletRegistrationBean servletRegistrationBean(DispatcherServlet dispatcherServlet) {
        return new DispatcherServletRegistrationBean(dispatcherServlet, "/");
    }

    // RouterFunctionMapping 初始化时会收集所有 RouterFunction 建立映射
    // 请求到达之后根据请求信息找到对应 HandlerFunction
    @Bean
    public RouterFunctionMapping routerFunctionMapping() {
        return new RouterFunctionMapping();
    }

    // 找到 HandlerFunction 之后调用 HandlerFunction
    @Bean
    public HandlerFunctionAdapter handlerFunctionAdapter() {
        return new HandlerFunctionAdapter();
    }

    @Bean
    public RouterFunction<ServerResponse> r1() {
        return RouterFunctions.route(RequestPredicates.GET("/r1"), new HandlerFunction<ServerResponse>() {
            @Override
            public ServerResponse handle(ServerRequest serverRequest) throws Exception {
                return ServerResponse.ok().body("this is r1");
            }
        });
    }

    @Bean
    public RouterFunction<ServerResponse> r2() {
        return route(GET("/r2"), request -> ok().body("this is r2"));
    }

}
```

```java
public class Application {
    public static void main(String[] args) {
        AnnotationConfigServletWebServerApplicationContext context =
                new AnnotationConfigServletWebServerApplicationContext(WebConfig.class);
    }
}
```



#### 总结

1. RouterFunctionMapping, 通过 RequestPredicate 条件映射
2. handler 要实现 HandlerFunction 接口
3. HandlerFunctionAdapter, 调用 handler
4. 对比 RequestMappingHandlerMapping 与 RequestMappingHandlerAdapter
   - RequestMappingHandlerAdapter, 以 @RequestMapping 作为映射路径
   - 控制器的具体方法会被当作 handler
   - RequestMappingHandlerAdapter, 调用 handler



### SimpleUrlHandlerMapping 与 HttpRequestHandlerAdapter

静态资源处理

#### 示例

```java
@Configuration
public class WebConfig {
    @Bean // 内嵌 web 容器工厂
    public TomcatServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory(8080);
    }

    @Bean // 创建 DispatcherServlet
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

    @Bean // 注册 DispatcherServlet, Spring MVC 的入口
    public DispatcherServletRegistrationBean servletRegistrationBean(DispatcherServlet dispatcherServlet) {
        return new DispatcherServletRegistrationBean(dispatcherServlet, "/");
    }

    /**
     * 映射路径
     */
    @Bean
    public SimpleUrlHandlerMapping simpleUrlHandlerMapping(ApplicationContext context) {
        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        Map<String, ResourceHttpRequestHandler> map = context.getBeansOfType(ResourceHttpRequestHandler.class);
        handlerMapping.setUrlMap(map);
        System.out.println(map);
        return handlerMapping;
    }

    /**
     * 调用 handler
     */
    @Bean
    public HttpRequestHandlerAdapter httpRequestHandlerAdapter() {
        return new HttpRequestHandlerAdapter();
    }

    /**
     * 作为静态资源 handler
     *
     * /** ->
     *       /index.html
     *       /r1.html
     *       /r2.html
     */
    @Bean("/**")
    public ResourceHttpRequestHandler handler1() {
        ResourceHttpRequestHandler handler = new ResourceHttpRequestHandler();
        List classPathResourceList = new ArrayList();
        List resourceResolver = new ArrayList();
        classPathResourceList.add(new ClassPathResource("static/"));
        // 用于缓存静态资源，第一次从磁盘读取静态资源后缓存下来
        resourceResolver.add(new CachingResourceResolver(new ConcurrentMapCache("cache1")));
        // 用于读取压缩静态资源，需要先将静态资源压缩，例：initGzip
        resourceResolver.add(new EncodedResourceResolver());
        resourceResolver.add(new PathResourceResolver());
        handler.setLocations(classPathResourceList);
        handler.setResourceResolvers(resourceResolver);
        return handler;
    }

    /**
     * /img/** ->
     *          /img/1.jpg
     *          /img/2.jpg
     *          /img/3.jpg
     */
    @Bean("/img/**")
    public ResourceHttpRequestHandler handler2() {
        ResourceHttpRequestHandler handler = new ResourceHttpRequestHandler();
        List classPathResourceList = new ArrayList();
        classPathResourceList.add(new ClassPathResource("images/"));
        handler.setLocations(classPathResourceList);
        return handler;
    }

    /**
     * 欢迎页
     * WelcomePageHandlerMapping 由 Spring Boot 提供
     * 可以映射到静态资源和 Controller
     *
     * a. WelcomePageHandlerMapping, 映射欢迎页(即只映射 '/')
     *     - 它内置了 handler ParameterizableViewController 作用是不执行逻辑, 仅根据视图名找视图
     *     - 视图名固定为 forward:index.html       /**
     * b. SimpleControllerHandlerAdapter, 调用 handler
     *     - 转发至 /index.html
     *     - 处理 /index.html 又会走上面的静态资源处理流程
     */
    @Bean
    public WelcomePageHandlerMapping welcomePageHandlerMapping(ApplicationContext context) {
        Resource resource = context.getResource("classpath:static/index.html");
        return new WelcomePageHandlerMapping(null, context, resource, "/**");
        // 生成实现了 Controller 接口的处理器
    }

    /**
     * 识别调用 Controller 接口的处理器
     */
    @Bean
    public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
        return new SimpleControllerHandlerAdapter();
    }

    /**
     * 压缩静态资源
     */
    @PostConstruct
    @SuppressWarnings("all")
    public void initGzip() throws IOException {
        Resource resource = new ClassPathResource("static");
        File dir = resource.getFile();
        for (File file : dir.listFiles(pathname -> pathname.getName().endsWith(".html"))) {
            System.out.println(file);
            try (FileInputStream fis = new FileInputStream(file); GZIPOutputStream fos = new GZIPOutputStream(new FileOutputStream(file.getAbsoluteFile() + ".gz"))) {
                byte[] bytes = new byte[8 * 1024];
                int len;
                while ((len = fis.read(bytes)) != -1) {
                    fos.write(bytes, 0, len);
                }
            }
        }
    }
}
```

```java
public class Application {
    public static void main(String[] args) {
        AnnotationConfigServletWebServerApplicationContext context
                = new AnnotationConfigServletWebServerApplicationContext(WebConfig.class);
    }
}
```



#### 总结

##### 静态资源处理

1. SimpleUrlHandlerMapping 不会在初始化时收集映射信息，需要手动收集
2. SimpleUrlHandlerMapping 映射路径
3. ResourceHttpRequestHandler 作为静态资源 handler
4. HttpRequestHandlerAdapter, 调用 handler



##### 静态资源解析优化

1. 责任链模式体现
2. 压缩文件需要手动生成



##### 欢迎页

1. 欢迎页支持静态欢迎页与动态欢迎页
2. WelcomePageHandlerMapping 映射欢迎页（即只映射 '/'）
   * 它内置的 handler ParameterizableViewController 作用是不执行逻辑，仅根据视图名找视图
   * 视图名固定为 forward:index.html
3. SimpleControllerHandlerAdapter, 调用 handler
   * 转发至 /index.html
   * 处理 /index.html 又会走上面的静态资源处理流程



#### 映射器与适配器小结

1. HandlerMapping 负责建立请求与控制器之间的映射关系
   * RequestMappingHandlerMapping (与 @RequestMapping 匹配)
   * WelcomePageHandlerMapping    (/)
   * BeanNameUrlHandlerMapping    (与 bean 的名字匹配 以 / 开头)
   * RouterFunctionMapping        (函数式 RequestPredicate, HandlerFunction)
   * SimpleUrlHandlerMapping      (静态资源 通配符 /** /img/**)
   * 之间也会有顺序问题, boot 中默认顺序如上
2. HandlerAdapter 负责实现对各种各样的 handler 的适配调用
   * RequestMappingHandlerAdapter 处理：@RequestMapping 方法
     * 参数解析器、返回值处理器体现了组合模式
   * SimpleControllerHandlerAdapter 处理：Controller 接口
   * HandlerFunctionAdapter 处理：HandlerFunction 函数式接口
   * HttpRequestHandlerAdapter 处理：HttpRequestHandler 接口 (静态资源处理)
   * 这也是典型适配器模式体现



### MVC 处理流程

当浏览器发送一个请求 `http://localhost:8080/hello` 后，请求到达服务器，其处理流程是：

1. 服务器提供了 DispatcherServlet，它使用的是标准 Servlet 技术

   * 路径：默认映射路径为 `/`，即会匹配到所有请求 URL，可作为请求的统一入口，也被称之为**前控制器**
     * jsp 不会匹配到 DispatcherServlet
     * 其它有路径的 Servlet 匹配优先级也高于 DispatcherServlet
   * 创建：在 Boot 中，由 DispatcherServletAutoConfiguration 这个自动配置类提供 DispatcherServlet 的 bean
   * 初始化：DispatcherServlet 初始化时会优先到容器里寻找各种组件，作为它的成员变量
     * HandlerMapping，初始化时记录映射关系
     * HandlerAdapter，初始化时准备参数解析器、返回值处理器、消息转换器
     * HandlerExceptionResolver，初始化时准备参数解析器、返回值处理器、消息转换器
     * ViewResolver
2. DispatcherServlet 会利用 RequestMappingHandlerMapping 查找控制器方法

   * 例如根据 /hello 路径找到 @RequestMapping("/hello") 对应的控制器方法

   * 控制器方法会被封装为 HandlerMethod 对象，并结合匹配到的拦截器一起返回给 DispatcherServlet 

   * HandlerMethod 和拦截器合在一起称为 HandlerExecutionChain（调用链）对象
3. DispatcherServlet 接下来会：

   1. 调用拦截器的 preHandle 方法
   2. RequestMappingHandlerAdapter 调用 handle 方法，准备数据绑定工厂、模型工厂、ModelAndViewContainer、将 HandlerMethod 完善为 ServletInvocableHandlerMethod
      * @ControllerAdvice 全局增强点1️⃣：补充模型数据
      * @ControllerAdvice 全局增强点2️⃣：补充自定义类型转换器
      * 使用 HandlerMethodArgumentResolver 准备参数
        * @ControllerAdvice 全局增强点3️⃣：RequestBody 增强
      * 调用 ServletInvocableHandlerMethod 
      * 使用 HandlerMethodReturnValueHandler 处理返回值
        * @ControllerAdvice 全局增强点4️⃣：ResponseBody 增强
      * 根据 ModelAndViewContainer 获取 ModelAndView
        * 如果返回的 ModelAndView 为 null，不走第 4 步视图解析及渲染流程
          * 例如，有的返回值处理器调用了 HttpMessageConverter 来将结果转换为 JSON，这时 ModelAndView 就为 null
        * 如果返回的 ModelAndView 不为 null，会在第 4 步走视图解析及渲染流程
   3. 调用拦截器的 postHandle 方法
   4. 处理异常或视图渲染
      * 如果 1~3 出现异常，走 ExceptionHandlerExceptionResolver 处理异常流程
        * @ControllerAdvice 全局增强点5️⃣：@ExceptionHandler 异常处理
      * 正常，走视图解析及渲染流程
   5. 调用拦截器的 afterCompletion 方法



## Spring Boot

### Boot War项目

步骤1：创建模块，区别在于打包方式选择 war

<img src="D:\documents\notes\solid\Java.assets\001-178892492371984.png" alt="image-20211021160145072" style="zoom: 50%;" />

接下来勾选 Spring Web 支持

<img src="D:\documents\notes\solid\Java.assets\002-178892492371985.png" alt="image-20211021162416525" style="zoom:50%;" />

步骤2：编写控制器

```java
@Controller
public class MyController {

    @RequestMapping("/hello")
    public String abc() {
        System.out.println("进入了控制器");
        return "hello";
    }
}
```

步骤3：编写 jsp 视图，新建 webapp 目录和一个 hello.jsp 文件，注意文件名与控制器方法返回的视图逻辑名一致

```
src
	|- main
		|- java
		|- resources
		|- webapp
			|- hello.jsp
```

步骤4：配置视图路径，打开 application.properties 文件

```properties
spring.mvc.view.prefix=/
spring.mvc.view.suffix=.jsp
```

> 将来 prefix + 控制器方法返回值 + suffix 即为视图完整路径



#### 测试

如果用 mvn 插件 `mvn spring-boot:run` 或 main 方法测试

* 必须添加如下依赖，因为此时用的还是内嵌 tomcat，而内嵌 tomcat 默认不带 jasper（用来解析 jsp）

```xml
<dependency>
    <groupId>org.apache.tomcat.embed</groupId>
    <artifactId>tomcat-embed-jasper</artifactId>
    <scope>provided</scope>
</dependency>
```

也可以使用 Idea 配置 tomcat 来测试，此时用的是外置 tomcat

* 骨架生成的代码中，多了一个 ServletInitializer，它的作用就是配置外置 Tomcat 使用的，在外置 Tomcat 启动后，去调用它创建和运行 SpringApplication



#### 启示

对于 jar 项目，若要支持 jsp，也可以在加入 jasper 依赖的前提下，把 jsp 文件置入 `META-INF/resources` 



### Boot 启动过程



#### 启动过程

##### 阶段一：SpringApplication 构造

1. 记录 BeanDefinition 源
2. 推断应用类型
3. 记录 ApplicationContext 初始化器
4. 记录监听器
5. 推断主启动类



###### 源码

```java
public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
    this.sources = new LinkedHashSet();
    this.bannerMode = Mode.CONSOLE;
    this.logStartupInfo = true;
    this.addCommandLineProperties = true;
    this.addConversionService = true;
    this.headless = true;
    this.registerShutdownHook = true;
    this.additionalProfiles = new HashSet();
    this.isCustomEnvironment = false;
    this.lazyInitialization = false;
    this.resourceLoader = resourceLoader;
    Assert.notNull(primarySources, "PrimarySources must not be null");
    // 获取 Bean Definition 源，Bean 可以来自配置类，配置文件...
    this.primarySources = new LinkedHashSet(Arrays.asList(primarySources));
    // 推断应用类型，Spring Boot 支持三种应用类型。非 WEB 应用程序，基于 Servlet 的应用程序，基于 reactive 的应用程序
    // 后续需要根据不同应用类型创建不同类型的 ApplicationConetxt
    this.webApplicationType = WebApplicationType.deduceFromClasspath();
    // 添加 ApplicationContext 初始化器
    // 		作用
    // 		调用 SpringApplication.run 的时候创建 ApplicationContext
    // 		调用初始化器，对 ApplicationContext 进行扩展
    // 		调用 ApplicationContext.reflush 完成 ApplicationContext 初始化
    this.setInitializers(this.getSpringFactoriesInstances(ApplicationContextInitializer.class));
    // 监听器与事件
    //		作用
    //		Spring Boot 程序启动和运行过程中会发布一些事件，监听器就是用来相应这些事件并且做出处理
    this.setListeners(this.getSpringFactoriesInstances(ApplicationListener.class));
    // 主类推断。获取 main 方法所在类
    this.mainApplicationClass = this.deduceMainApplicationClass();
}
```



###### 演示

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="bean1" class="cn.example.uums.test.a39.Application.Bean1"/>

</beans>
```



```java
@Configuration
public class Application {

    public static void main(String[] args) throws Exception {
        System.out.println("1. 演示获取 Bean Definition 源");
        SpringApplication spring = new SpringApplication(Application.class);
        Set sourcesSet = new HashSet();
        sourcesSet.add("classpath:b01.xml");
        spring.setSources(sourcesSet);

        System.out.println("2. 演示推断应用类型");
        Method deduceFromClasspath = WebApplicationType.class.getDeclaredMethod("deduceFromClasspath");
        deduceFromClasspath.setAccessible(true);
        System.out.println("\t应用类型为:"+deduceFromClasspath.invoke(null));

        System.out.println("3. 演示 ApplicationContext 初始化器");
        spring.addInitializers(new ApplicationContextInitializer<ConfigurableApplicationContext>() {
            @Override
            public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
                if (configurableApplicationContext instanceof GenericApplicationContext) {
                    GenericApplicationContext gac = (GenericApplicationContext) configurableApplicationContext;
                    gac.registerBean("bean3", Bean3.class);
                }
            }
        });
        System.out.println("4. 演示监听器与事件");
        spring.addListeners(event -> System.out.println("\t事件为:" + event.getClass()));
        System.out.println("5. 演示主类推断");
        Method deduceMainApplicationClass = SpringApplication.class.getDeclaredMethod("deduceMainApplicationClass");
        deduceMainApplicationClass.setAccessible(true);
        System.out.println("\t主类是："+deduceMainApplicationClass.invoke(spring));

        ConfigurableApplicationContext context = spring.run(args);

        // 创建 ApplicationContext
        // 调用初始化器 对 ApplicationContext 做扩展
        // ApplicationContext.refresh
        for (String name : context.getBeanDefinitionNames()) {
            System.out.println("name: " + name + " 来源：" + context.getBeanFactory().getBeanDefinition(name).getResourceDescription());
        }
        context.close();
    }

    static class Bean1 {

    }

    static class Bean2 {

    }

    static class Bean3 {

    }

    @Bean
    public Bean2 bean2() {
        return new Bean2();
    }

    @Bean
    public TomcatServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory();
    }
}
```



##### 阶段二：执行 run 方法

12 个步骤，7个事件

1. 得到 SpringApplicationRunListeners，名字取得不好，实际是事件发布器

   * 发布 application starting 事件1️⃣

2. 封装启动 args

3. 准备 Environment 添加命令行参数（*）

4. ConfigurationPropertySources 处理（*）

   * 发布 application environment 已准备事件2️⃣

5. 通过 EnvironmentPostProcessorApplicationListener 进行 env 后处理（*）
   * application.properties，由 StandardConfigDataLocationResolver 解析
   * spring.application.json

6. 绑定 spring.main 到 SpringApplication 对象（*）

7. 打印 banner（*）

8. 创建容器

9. 准备容器

   * 发布 application context 已初始化事件3️⃣

10. 加载 bean 定义

    * 发布 application prepared 事件4️⃣

11. refresh 容器

    * 发布 application started 事件5️⃣

12. 执行 runner

    * 发布 application ready 事件6️⃣

    * 这其中有异常，发布 application failed 事件7️⃣

> 带 * 的有独立的示例



###### 源码

```java
public class SpringApplication {
	public ConfigurableApplicationContext run(String... args) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ConfigurableApplicationContext context = null;
        this.configureHeadlessProperty();
        // s1.
        SpringApplicationRunListeners listeners = this.getRunListeners(args);
        listeners.starting();

        try {
            ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
            ConfigurableEnvironment environment = this.prepareEnvironment(listeners, applicationArguments);
            this.configureIgnoreBeanInfo(environment);
            Banner printedBanner = this.printBanner(environment);
            // s8.
            context = this.createApplicationContext();
            this.prepareContext(context, environment, listeners, applicationArguments, printedBanner);
            this.refreshContext(context);
            this.afterRefresh(context, applicationArguments);
            stopWatch.stop();
            if (this.logStartupInfo) {
                (new StartupInfoLogger(this.mainApplicationClass)).logStarted(this.getApplicationLog(), stopWatch);
            }

            listeners.started(context);
            this.callRunners(context, applicationArguments);
        } catch (Throwable var9) {
            this.handleRunFailure(context, var9, listeners);
            throw new IllegalStateException(var9);
        }

        try {
            listeners.running(context);
            return context;
        } catch (Throwable var8) {
            this.handleRunFailure(context, var8, (SpringApplicationRunListeners)null);
            throw new IllegalStateException(var8);
        }
    }
}
```



###### 第1步，7 个事件

```java
public class Application {
    public static void main(String[] args) throws Exception{

        System.out.println("----------------step 1 添加 app 监听器--------------------");
        SpringApplication app = new SpringApplication();
        app.addListeners(e -> System.out.println(e.getClass()));

        // 获取事件发送器实现类名
        // 如何读取 spring.factories 中的配置
        List<String> names = SpringFactoriesLoader.loadFactoryNames(SpringApplicationRunListener.class, Application.class.getClassLoader());
        for (String name : names) {
            System.out.println(name);
            Class<?> clazz = Class.forName(name);
            Constructor<?> constructor = clazz.getConstructor(SpringApplication.class, String[].class);
            SpringApplicationRunListener publisher = (SpringApplicationRunListener) constructor.newInstance(app, args);

            // 发布事件
            // DefaultBootstrapContext 需要 Spring Boot 版本 2.5.5
            DefaultBootstrapContext bootstrapContext = new DefaultBootstrapContext();
            // spring boot 开始启动
        	System.out.println("----------------e1.发布 application starting 事件--------------------");
            publisher.starting(bootstrapContext);
            // 环境信息准备完毕
        	System.out.println("----------------e2.发布 application environment 已准备事件--------------------");
            publisher.environmentPrepared(bootstrapContext, new StandardEnvironment());
            // 在 spring 容器创建，并调用初始化器之后，发送此事件
        	System.out.println("----------------e3.发布 application context 已初始化事件--------------------");
            GenericApplicationContext context = new GenericApplicationContext();
            publisher.contextPrepared(context);
            // 所有 bean definition 加载完毕
        	System.out.println("----------------e4.发布 application prepared 事件--------------------");
            publisher.contextLoaded(context);
            context.refresh();
            // spring 容器初始化完成(refresh 方法调用完毕)
        	System.out.println("----------------e5.发布 application started 事件--------------------");
            publisher.started(context);
            // spring boot 启动完毕
            System.out.println("----------------e6.发布 application ready 事件--------------------");
            publisher.running(context);
            // spring boot 启动出错
            System.out.println("----------------e7.这其中有异常，发布 application failed 事件--------------------");
            publisher.failed(context, new Exception("出错了"));
        }
    }
}
```



###### 第2、8到12步

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="bean4" class="cn.example.uums.test.a39.Application.Bean4"/>

</beans>
```



```java
@Component
public class Bean7 {
}
```



```java
// 运行时请添加运行参数 --server.port=8080 debug
public class Application {
    @SuppressWarnings("all")
    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication();
        app.addInitializers(new ApplicationContextInitializer<ConfigurableApplicationContext>() {
            @Override
            public void initialize(ConfigurableApplicationContext applicationContext) {
                System.out.println("执行初始化器增强...");
            }
        });

        System.out.println("----------------step 2 封装启动 args--------------------");
        DefaultApplicationArguments arguments = new DefaultApplicationArguments(args);

        System.out.println("----------------step 8 创建容器--------------------");
        GenericApplicationContext context = createApplicationContext(WebApplicationType.SERVLET);

        System.out.println("----------------step 9 准备容器--------------------");
        for (ApplicationContextInitializer initializer : app.getInitializers()) {
            initializer.initialize(context);
        }

        System.out.println("----------------step 10 加载 bean definition--------------------");
        DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
        AnnotatedBeanDefinitionReader reader1 = new AnnotatedBeanDefinitionReader(beanFactory);
        XmlBeanDefinitionReader reader2 = new XmlBeanDefinitionReader(beanFactory);
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(beanFactory);

        reader1.register(Config.class);
        reader2.loadBeanDefinitions(new ClassPathResource("b03.xml"));
        scanner.scan("cn.example.uums.test.a39.sub");

        System.out.println("----------------step 11 refresh 容器--------------------");
        context.refresh();

        for (String name : context.getBeanDefinitionNames()) {
            System.out.println("name:" + name + " 来源：" + beanFactory.getBeanDefinition(name).getResourceDescription());
        }

        System.out.println("----------------step 12 执行 runner--------------------");
        // runner 是实现了特定接口的 Bean
        for (CommandLineRunner runner : context.getBeansOfType(CommandLineRunner.class).values()) {
            runner.run(args);
        }

        for (ApplicationRunner runner : context.getBeansOfType(ApplicationRunner.class).values()) {
            runner.run(arguments);
        }

        /*
            学到了什么
            a. 创建容器、加载 bean 定义、refresh, 对应的步骤

         */
    }

    /**
     * SpringApplication 构造时已经推断出了应用类型，需要根据不同应用类型创建不同类型的 ApplicationConetxt
     */
    private static GenericApplicationContext createApplicationContext(WebApplicationType type) {
        GenericApplicationContext context = null;
        switch (type) {
            case SERVLET:
                context = new AnnotationConfigServletWebServerApplicationContext();
            case REACTIVE:
                context = new AnnotationConfigReactiveWebServerApplicationContext();
            case NONE:
                context = new AnnotationConfigApplicationContext();
        }
        return context;
    }

    static class Bean4 {

    }

    static class Bean5 {

    }

    static class Bean6 {

    }

    @Configuration
    static class Config {
        @Bean
        public Bean5 bean5() {
            return new Bean5();
        }

        @Bean
        public ServletWebServerFactory servletWebServerFactory() {
            return new TomcatServletWebServerFactory();
        }

        @Bean
        public CommandLineRunner commandLineRunner() {
            return new CommandLineRunner() {
                @Override
                public void run(String... args) throws Exception {
                    System.out.println("commandLineRunner()..." + Arrays.toString(args));
                }
            };
        }

        @Bean
        public ApplicationRunner applicationRunner() {
            return new ApplicationRunner() {
                @Override
                public void run(ApplicationArguments args) throws Exception {
                    System.out.println("applicationRunner()..." + Arrays.toString(args.getSourceArgs()));
                    System.out.println(args.getOptionNames());
                    System.out.println(args.getOptionValues("server.port"));
                    System.out.println(args.getNonOptionArgs());
                }
            };
        }
    }
}
```



###### 第3到7步

step3.properties

```properties
server.port=8080
```

step4.properties

```properties
user.first-name=George
user.middle_name=Walker
user.lastName=Bush
```

step6.properties

```properties
spring.main.banner-mode=off
spring.main.lazy-initialization=true
```

banner1.txt

```txt
  _____            _             ____              _
 / ____|          (_)           |  _ \            | |
| (___  _ __  _ __ _ _ __   __ _| |_) | ___   ___ | |_
 \___ \| '_ \| '__| | '_ \ / _` |  _ < / _ \ / _ \| __|
 ____) | |_) | |  | | | | | (_| | |_) | (_) | (_) | |_
|_____/| .__/|_|  |_|_| |_|\__, |____/ \___/ \___/ \__|
       | |                  __/ |
       |_|                 |___/
```

[banner2.png](../pictures/20230507/003.png)

```java
public class Application {
    public static void main(String[] args) throws IOException {
        step3(args);
        step4();
        step5(args);
        step6();
        step7();
    }

    private static void step3(String[] args) throws IOException {
        System.out.println("----------------step 3 准备 Environment 添加命令行参数--------------------");
        ApplicationEnvironment env = new ApplicationEnvironment(); // 系统环境变量, properties, yaml
        env.getPropertySources().addLast(new ResourcePropertySource(new ClassPathResource("step3.properties")));
        env.getPropertySources().addFirst(new SimpleCommandLinePropertySource(args));
        // 环境变量来源
        for (PropertySource<?> ps : env.getPropertySources()) {
            System.out.println(ps);
        }
        System.out.println(env.getProperty("JAVA_HOME"));
        System.out.println(env.getProperty("server.port"));
    }

    private static void step4() throws IOException {
        System.out.println("----------------step 4 ConfigurationPropertySources 处理--------------------");
        ApplicationEnvironment env = new ApplicationEnvironment();
        env.getPropertySources().addLast(
                new ResourcePropertySource("step4", new ClassPathResource("step4.properties"))
        );
        ConfigurationPropertySources.attach(env);
        for (PropertySource<?> ps : env.getPropertySources()) {
            System.out.println(ps);
        }

        System.out.println(env.getProperty("user.first-name"));
        System.out.println(env.getProperty("user.middle-name"));
        System.out.println(env.getProperty("user.last-name"));
    }

    private static void step5(String[] args){
        System.out.println("----------------step 5 通过 EnvironmentPostProcessorApplicationListener 进行 env 后处理--------------------");
        // application.properties 在这步被添加到环境
        ApplicationEnvironment env = new ApplicationEnvironment();
        SpringApplication app = new SpringApplication();
        app.addListeners(new EnvironmentPostProcessorApplicationListener());

        /*List<String> names = SpringFactoriesLoader.loadFactoryNames(EnvironmentPostProcessor.class, Application.class.getClassLoader());
        for (String name : names) {
            System.out.println(name);
        }*/

        EventPublishingRunListener publisher = new EventPublishingRunListener(app, args);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 增强前");
        for (PropertySource<?> ps : env.getPropertySources()) {
            System.out.println(ps);
        }
        publisher.environmentPrepared(new DefaultBootstrapContext(), env);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 增强后");
        for (PropertySource<?> ps : env.getPropertySources()) {
            System.out.println(ps);
        }
        step5Test();
    }

    private static void step6() throws IOException {
        System.out.println("----------------step 6 绑定 spring.main 前缀的 key value 至 SpringApplication--------------------");
        SpringApplication application = new SpringApplication();
        ApplicationEnvironment env = new ApplicationEnvironment();
        env.getPropertySources().addLast(new ResourcePropertySource("step4", new ClassPathResource("step4.properties")));
        env.getPropertySources().addLast(new ResourcePropertySource("step6", new ClassPathResource("step6.properties")));

//        User user = Binder.get(env).bind("user", User.class).get();
//        System.out.println(user);

//        User user = new User();
//        Binder.get(env).bind("user", Bindable.ofInstance(user));
//        System.out.println(user);

        // 属性为 SpringApplication 的私有属性, 请通过 debug 查看
        System.out.println(application);
        Binder.get(env).bind("spring.main", Bindable.ofInstance(application));
        System.out.println(application);
    }

    private static void step7(){
        System.out.println("----------------step 7 打印 banner--------------------");
        ApplicationEnvironment env = new ApplicationEnvironment();
        SpringApplicationBannerPrinter printer = new SpringApplicationBannerPrinter(
                new DefaultResourceLoader(),
                new SpringBootBanner()
        );
        // 测试文字 banner
//        env.getPropertySources().addLast(new MapPropertySource("custom", Map.of("spring.banner.location","banner1.txt")));
        // 测试图片 banner
//        env.getPropertySources().addLast(new MapPropertySource("custom", Map.of("spring.banner.image.location","banner2.png")));
        // 版本号的获取
        System.out.println(SpringBootVersion.getVersion());
        printer.print(env, Application.class, System.out);
    }

    private static void step5Test() {
        SpringApplication app = new SpringApplication();
        ApplicationEnvironment env = new ApplicationEnvironment();

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 增强前");
        for (PropertySource<?> ps : env.getPropertySources()) {
            System.out.println(ps);
        }
        ConfigDataEnvironmentPostProcessor postProcessor1 = new ConfigDataEnvironmentPostProcessor(new DeferredLogs(), new DefaultBootstrapContext());
        postProcessor1.postProcessEnvironment(env, app);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 增强后");
        for (PropertySource<?> ps : env.getPropertySources()) {
            System.out.println(ps);
        }
        RandomValuePropertySourceEnvironmentPostProcessor postProcessor2 = new RandomValuePropertySourceEnvironmentPostProcessor(new DeferredLog());
        postProcessor2.postProcessEnvironment(env, app);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 增强后");
        for (PropertySource<?> ps : env.getPropertySources()) {
            System.out.println(ps);
        }
        System.out.println(env.getProperty("server.port"));
        System.out.println(env.getProperty("random.int"));
        System.out.println(env.getProperty("random.int"));
        System.out.println(env.getProperty("random.int"));
        System.out.println(env.getProperty("random.uuid"));
        System.out.println(env.getProperty("random.uuid"));
        System.out.println(env.getProperty("random.uuid"));
    }

    static class User {
        private String firstName;
        private String middleName;
        private String lastName;
        public String getFirstName() {
            return firstName;
        }
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        public String getMiddleName() {
            return middleName;
        }
        public void setMiddleName(String middleName) {
            this.middleName = middleName;
        }
        public String getLastName() {
            return lastName;
        }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        @Override
        public String toString() {
            return "User{" +
                    "firstName='" + firstName + '\'' +
                    ", middleName='" + middleName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    '}';
        }
    }
}
```



#### 收获

1. SpringApplication 构造方法中所做的操作
   * 可以有多种源用来加载 bean 定义
   * 应用类型推断
   * 添加容器初始化器
   * 添加监听器
   * 演示主类推断
2. 如何读取 spring.factories 中的配置
3. 从配置中获取重要的事件发布器：SpringApplicationRunListeners
4. 容器的创建、初始化器增强、加载 bean 定义等
5. CommandLineRunner、ApplicationRunner 的作用
6. 环境对象
   1. 命令行 PropertySource
   2. ConfigurationPropertySources 规范环境键名称
   3. EnvironmentPostProcessor 后处理增强
      * 由 EventPublishingRunListener 通过监听事件2️⃣来调用
   4. 绑定 spring.main 前缀的 key value 至 SpringApplication
7. Banner 



### Tomcat 内嵌容器

Tomcat 基本结构

```
Server
└───Service
    ├───Connector (协议, 端口)
    └───Engine
        └───Host(虚拟主机 localhost)
            ├───Context1 (应用1, 可以设置虚拟路径, / 即 url 起始路径; 项目磁盘路径, 即 docBase )
            │   │   index.html
            │   └───WEB-INF
            │       │   web.xml (servlet, filter, listener) 3.0
            │       ├───classes (servlet, controller, service ...)
            │       ├───jsp
            │       └───lib (第三方 jar 包)
            └───Context2 (应用2)
                │   index.html
                └───WEB-INF
                        web.xml
```

#### Tomcat 内嵌容器



```java
public class TestTomcat {
    @SuppressWarnings("all")
    public static void main(String[] args) throws LifecycleException, IOException {
        // 1.创建 Tomcat 对象
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir("tomcat");

        // 2.创建项目文件夹, 即 docBase 文件夹
        File docBase = Files.createTempDirectory("boot.").toFile();
        docBase.deleteOnExit();

        // 3.创建 Tomcat 项目, 在 Tomcat 中称为 Context
        Context context = tomcat.addContext("test/", docBase.getAbsolutePath());
        
        // 4.编程添加 Servlet
        context.addServletContainerInitializer(new ServletContainerInitializer() {
            @Override
            public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
                HelloServlet helloServlet = new HelloServlet();
                ctx.addServlet("aaa", helloServlet).addMapping("/hello");
            }
        }, Collections.emptySet());

        // 5.启动 Tomcat
        tomcat.start();

        // 6.创建连接器, 设置监听端口
        Connector connector = new Connector(new Http11Nio2Protocol());
        connector.setPort(8080);
        tomcat.setConnector(connector);
    }
}
```



```java
public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=utf-8");
        resp.getWriter().print("<h3>hello</h3>");
    }
}
```



#### 集成 Spring 容器

##### Tomcat 在 Spring 容器中的初始化

创建 Spring 容器时执行 refresh 方法会初始化 Tomcat

```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    public void refresh() throws BeansException, IllegalStateException {
        synchronized(this.startupShutdownMonitor) {
            StartupStep contextRefresh = this.applicationStartup.start("spring.context.refresh");
            this.prepareRefresh();
            ConfigurableListableBeanFactory beanFactory = this.obtainFreshBeanFactory();
            this.prepareBeanFactory(beanFactory);

            try {
                this.postProcessBeanFactory(beanFactory);
                StartupStep beanPostProcess = this.applicationStartup.start("spring.context.beans.post-process");
                this.invokeBeanFactoryPostProcessors(beanFactory);
                this.registerBeanPostProcessors(beanFactory);
                beanPostProcess.end();
                this.initMessageSource();
                this.initApplicationEventMulticaster();
                // onRefresh 创建 Tomcat 步骤 1、2、3、4
                this.onRefresh();
                this.registerListeners();
                this.finishBeanFactoryInitialization(beanFactory);
                // 启动 Tomcat 步骤 5、6
                this.finishRefresh();
            } catch (BeansException var10) {
                if (this.logger.isWarnEnabled()) {
                    this.logger.warn("Exception encountered during context initialization - cancelling refresh attempt: " + var10);
                }

                this.destroyBeans();
                this.cancelRefresh(var10);
                throw var10;
            } finally {
                this.resetCommonCaches();
                contextRefresh.end();
            }

        }
    }
}
```



##### 示例

```java
@Configuration
static class Config {
    @Bean
    public DispatcherServletRegistrationBean registrationBean(DispatcherServlet dispatcherServlet) {
        return new DispatcherServletRegistrationBean(dispatcherServlet, "/");
    }

    @Bean
    // 这个例子中必须为 DispatcherServlet 提供 AnnotationConfigWebApplicationContext, 否则会选择 XmlWebApplicationContext 实现
    public DispatcherServlet dispatcherServlet(WebApplicationContext applicationContext) {
        return new DispatcherServlet(applicationContext);
    }

    @Bean
    public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
        RequestMappingHandlerAdapter handlerAdapter = new RequestMappingHandlerAdapter();
        List list = new ArrayList();
        list.add(new MappingJackson2HttpMessageConverter());
        handlerAdapter.setMessageConverters(list);
        return handlerAdapter;
    }

    @RestController
    static class MyController {
        @GetMapping("hello2")
        public Map<String,Object> hello() {
            Map<String, Object> map = new HashMap<>();
            map.put("hello2", "hello2, spring!");
            return map;
        }
    }
}
```



```java
public class TestTomcat {
    @SuppressWarnings("all")
    public static void main(String[] args) throws LifecycleException, IOException {
        // 1.创建 Tomcat 对象
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir("tomcat");

        // 2.创建项目文件夹, 即 docBase 文件夹
        File docBase = Files.createTempDirectory("boot.").toFile();
        docBase.deleteOnExit();

        // 3.创建 Tomcat 项目, 在 Tomcat 中称为 Context
        Context context = tomcat.addContext("test/", docBase.getAbsolutePath());

        WebApplicationContext springContext = getApplicationContext();

        // 4.编程添加 Servlet
        context.addServletContainerInitializer(new ServletContainerInitializer() {
            @Override
            public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
                HelloServlet helloServlet = new HelloServlet();
                ctx.addServlet("aaa", helloServlet).addMapping("/hello");

//                DispatcherServlet dispatcherServlet = springContext.getBean(DispatcherServlet.class);
//                ctx.addServlet("dispatcherServlet", dispatcherServlet).addMapping("/");
                for (ServletRegistrationBean registrationBean : springContext.getBeansOfType(ServletRegistrationBean.class).values()) {
                    registrationBean.onStartup(ctx);
                }
            }
        }, Collections.emptySet());

        // 5.启动 Tomcat
        tomcat.start();

        // 6.创建连接器, 设置监听端口
        Connector connector = new Connector(new Http11Nio2Protocol());
        connector.setPort(8080);
        tomcat.setConnector(connector);
    }

    public static WebApplicationContext getApplicationContext() {
//        AnnotationConfigServletWebServerApplicationContext
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(Config.class);
        context.refresh();
        return context;
    }
}
```





### Boot 自动配置

#### AopAutoConfiguration

Spring Boot 是利用了自动配置类来简化了 aop 相关配置

* AOP 自动配置类为 `org.springframework.boot.autoconfigure.aop.AopAutoConfiguration`
* 可以通过 `spring.aop.auto=false` 禁用 aop 自动配置
* AOP 自动配置的本质是通过 `@EnableAspectJAutoProxy` 来开启了自动代理，如果在引导类上自己添加了 `@EnableAspectJAutoProxy` 那么以自己添加的为准
* `@EnableAspectJAutoProxy` 的本质是向容器中添加了 `AnnotationAwareAspectJAutoProxyCreator` 这个 bean 后处理器，它能够找到容器中所有切面，并为匹配切点的目标类创建代理，创建代理的工作一般是在 bean 的初始化阶段完成的



##### 源码

```java
@Configuration(
    proxyBeanMethods = false
)
@ConditionalOnProperty(
    prefix = "spring.aop",
    name = {"auto"},
    havingValue = "true",
    matchIfMissing = true
)
public class AopAutoConfiguration {
    public AopAutoConfiguration() {
    }

    @Configuration(
        proxyBeanMethods = false
    )
    @ConditionalOnMissingClass({"org.aspectj.weaver.Advice"})
    @ConditionalOnProperty(
        prefix = "spring.aop",
        name = {"proxy-target-class"},
        havingValue = "true",
        matchIfMissing = true
    )
    static class ClassProxyingConfiguration {
        ClassProxyingConfiguration() {
        }

        @Bean
        static BeanFactoryPostProcessor forceAutoProxyCreatorToUseClassProxying() {
            return (beanFactory) -> {
                if (beanFactory instanceof BeanDefinitionRegistry) {
                    BeanDefinitionRegistry registry = (BeanDefinitionRegistry)beanFactory;
                    AopConfigUtils.registerAutoProxyCreatorIfNecessary(registry);
                    AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
                }

            };
        }
    }

    @Configuration(
        proxyBeanMethods = false
    )
    @ConditionalOnClass({Advice.class})
    static class AspectJAutoProxyingConfiguration {
        AspectJAutoProxyingConfiguration() {
        }

        @Configuration(
            proxyBeanMethods = false
        )
        @EnableAspectJAutoProxy(
            proxyTargetClass = true
        )
        @ConditionalOnProperty(
            prefix = "spring.aop",
            name = {"proxy-target-class"},
            havingValue = "true",
            matchIfMissing = true
        )
        static class CglibAutoProxyConfiguration {
            CglibAutoProxyConfiguration() {
            }
        }

        @Configuration(
            proxyBeanMethods = false
        )
        @EnableAspectJAutoProxy(
            proxyTargetClass = false
        )
        @ConditionalOnProperty(
            prefix = "spring.aop",
            name = {"proxy-target-class"},
            havingValue = "false"
        )
        static class JdkDynamicAutoProxyConfiguration {
            JdkDynamicAutoProxyConfiguration() {
            }
        }
    }
}
```



##### 示例

```java
public class TestAopAuto {
    public static void main(String[] args) {
        GenericApplicationContext context = new GenericApplicationContext();
        StandardEnvironment env = new StandardEnvironment();
//        env.getPropertySources().addLast(new SimpleCommandLinePropertySource("--spring.aop.auto=false"));
        context.setEnvironment(env);
        // 注册常见的后处理器
        AnnotationConfigUtils.registerAnnotationConfigProcessors(context.getDefaultListableBeanFactory());
        context.registerBean(Config.class);
        context.refresh();
        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }
        System.out.println(">>>>>>>>>>>>>>>");
        AnnotationAwareAspectJAutoProxyCreator creator = context.getBean(
                "org.springframework.aop.config.internalAutoProxyCreator", AnnotationAwareAspectJAutoProxyCreator.class);
        System.out.println(creator.isProxyTargetClass());

    }

    @Configuration
    @Import(MyImportSelector.class)
    static class Config {

    }

    static class MyImportSelector implements DeferredImportSelector {
        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {
            return new String[]{AopAutoConfiguration.class.getName()};
        }
    }
}
```



#### DataSourceAutoConfiguration

* 对应的自动配置类为：org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
* 它内部采用了条件装配，通过检查容器的 bean，以及类路径下的 class，来决定该 @Bean 是否生效

简单说明一下，Spring Boot 支持两大类数据源：

* EmbeddedDatabase - 内嵌数据库连接池
* PooledDataSource - 非内嵌数据库连接池

PooledDataSource 又支持如下数据源

* hikari 提供的 HikariDataSource
* tomcat-jdbc 提供的 DataSource
* dbcp2 提供的 BasicDataSource
* oracle 提供的 PoolDataSourceImpl

如果知道数据源的实现类类型，即指定了 `spring.datasource.type`，理论上可以支持所有数据源，但这样做的一个最大问题是无法订制每种数据源的详细配置（如最大、最小连接数等）



##### 源码

```java
@Configuration(
    proxyBeanMethods = false
)
@ConditionalOnClass({DataSource.class, EmbeddedDatabaseType.class})
@ConditionalOnMissingBean(
    type = {"io.r2dbc.spi.ConnectionFactory"}
)
@EnableConfigurationProperties({DataSourceProperties.class})
@Import({DataSourcePoolMetadataProvidersConfiguration.class, InitializationSpecificCredentialsDataSourceInitializationConfiguration.class, SharedCredentialsDataSourceInitializationConfiguration.class})
public class DataSourceAutoConfiguration {
    public DataSourceAutoConfiguration() {
    }

    static class EmbeddedDatabaseCondition extends SpringBootCondition {
        private static final String DATASOURCE_URL_PROPERTY = "spring.datasource.url";
        private final SpringBootCondition pooledCondition = new DataSourceAutoConfiguration.PooledDataSourceCondition();

        EmbeddedDatabaseCondition() {
        }

        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Builder message = ConditionMessage.forCondition("EmbeddedDataSource", new Object[0]);
            if (this.hasDataSourceUrlProperty(context)) {
                return ConditionOutcome.noMatch(message.because("spring.datasource.url is set"));
            } else if (this.anyMatches(context, metadata, new Condition[]{this.pooledCondition})) {
                return ConditionOutcome.noMatch(message.foundExactly("supported pooled data source"));
            } else {
                EmbeddedDatabaseType type = EmbeddedDatabaseConnection.get(context.getClassLoader()).getType();
                return type == null ? ConditionOutcome.noMatch(message.didNotFind("embedded database").atAll()) : ConditionOutcome.match(message.found("embedded database").items(new Object[]{type}));
            }
        }

        private boolean hasDataSourceUrlProperty(ConditionContext context) {
            Environment environment = context.getEnvironment();
            if (environment.containsProperty("spring.datasource.url")) {
                try {
                    return StringUtils.hasText(environment.getProperty("spring.datasource.url"));
                } catch (IllegalArgumentException var4) {
                }
            }

            return false;
        }
    }

    static class PooledDataSourceAvailableCondition extends SpringBootCondition {
        PooledDataSourceAvailableCondition() {
        }

        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Builder message = ConditionMessage.forCondition("PooledDataSource", new Object[0]);
            return DataSourceBuilder.findType(context.getClassLoader()) != null ? ConditionOutcome.match(message.foundExactly("supported DataSource")) : ConditionOutcome.noMatch(message.didNotFind("supported DataSource").atAll());
        }
    }

    static class PooledDataSourceCondition extends AnyNestedCondition {
        PooledDataSourceCondition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @Conditional({DataSourceAutoConfiguration.PooledDataSourceAvailableCondition.class})
        static class PooledDataSourceAvailable {
            PooledDataSourceAvailable() {
            }
        }

        @ConditionalOnProperty(
            prefix = "spring.datasource",
            name = {"type"}
        )
        static class ExplicitType {
            ExplicitType() {
            }
        }
    }

    @Configuration(
        proxyBeanMethods = false
    )
    @Conditional({DataSourceAutoConfiguration.PooledDataSourceCondition.class})
    @ConditionalOnMissingBean({DataSource.class, XADataSource.class})
    @Import({Hikari.class, Tomcat.class, Dbcp2.class, OracleUcp.class, Generic.class, DataSourceJmxConfiguration.class})
    protected static class PooledDataSourceConfiguration {
        protected PooledDataSourceConfiguration() {
        }
    }

    @Configuration(
        proxyBeanMethods = false
    )
    @Conditional({DataSourceAutoConfiguration.EmbeddedDatabaseCondition.class})
    @ConditionalOnMissingBean({DataSource.class, XADataSource.class})
    @Import({EmbeddedDataSourceConfiguration.class})
    protected static class EmbeddedDatabaseConfiguration {
        protected EmbeddedDatabaseConfiguration() {
        }
    }
}
```



##### 示例

```java
public class TestDataSourceAuto {
    @SuppressWarnings("all")
    public static void main(String[] args) {
        GenericApplicationContext context = new GenericApplicationContext();
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().addLast(new SimpleCommandLinePropertySource(
                "--spring.datasource.url=jdbc:mysql://localhost:3306/test",
                "--spring.datasource.username=root",
                "--spring.datasource.password=root"
        ));
        context.setEnvironment(env);
        AnnotationConfigUtils.registerAnnotationConfigProcessors(context.getDefaultListableBeanFactory());
        context.registerBean(Config.class);

        context.refresh();
        for (String name : context.getBeanDefinitionNames()) {
            String resourceDescription = context.getBeanDefinition(name).getResourceDescription();
            if (resourceDescription != null)
                System.out.println(name + " 来源:" + resourceDescription);
        }
    }

    @Configuration
    @Import(MyImportSelector.class)
    static class Config {

    }

    static class MyImportSelector implements DeferredImportSelector {
        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {
            return new String[]{
                    DataSourceAutoConfiguration.class.getName()
            };
        }
    }
}
```



#### MybatisAutoConfiguration

* MyBatis 自动配置类为 `org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration`
* 它主要配置了两个 bean
  * SqlSessionFactory - MyBatis 核心对象，用来创建 SqlSession
  * SqlSessionTemplate - SqlSession 的实现，此实现会与当前线程绑定
  * 用 ImportBeanDefinitionRegistrar 的方式扫描所有标注了 @Mapper 注解的接口
  * 用 AutoConfigurationPackages 来确定扫描的包
* 还有一个相关的 bean：MybatisProperties，它会读取配置文件中带 `mybatis.` 前缀的配置项进行定制配置

@MapperScan 注解的作用与 MybatisAutoConfiguration 类似，会注册 MapperScannerConfigurer 有如下区别

* @MapperScan 扫描具体包（当然也可以配置关注哪个注解）
* @MapperScan 如果不指定扫描具体包，则会把引导类范围内，所有接口当做 Mapper 接口
* MybatisAutoConfiguration 关注的是所有标注 @Mapper 注解的接口，会忽略掉非 @Mapper 标注的接口

这里有同学有疑问，之前介绍的都是将具体类交给 Spring 管理，怎么到了 MyBatis 这儿，接口就可以被管理呢？

* 其实并非将接口交给 Spring 管理，而是每个接口会对应一个 MapperFactoryBean，是后者被 Spring 所管理，接口只是作为 MapperFactoryBean 的一个属性来配置



##### 源码

```java
@Configuration
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties({MybatisProperties.class})
@AutoConfigureAfter({DataSourceAutoConfiguration.class, MybatisLanguageDriverAutoConfiguration.class})
public class MybatisAutoConfiguration implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(MybatisAutoConfiguration.class);
    private final MybatisProperties properties;
    private final Interceptor[] interceptors;
    private final TypeHandler[] typeHandlers;
    private final LanguageDriver[] languageDrivers;
    private final ResourceLoader resourceLoader;
    private final DatabaseIdProvider databaseIdProvider;
    private final List<ConfigurationCustomizer> configurationCustomizers;

    public MybatisAutoConfiguration(MybatisProperties properties, ObjectProvider<Interceptor[]> interceptorsProvider, ObjectProvider<TypeHandler[]> typeHandlersProvider, ObjectProvider<LanguageDriver[]> languageDriversProvider, ResourceLoader resourceLoader, ObjectProvider<DatabaseIdProvider> databaseIdProvider, ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider) {
        this.properties = properties;
        this.interceptors = (Interceptor[])interceptorsProvider.getIfAvailable();
        this.typeHandlers = (TypeHandler[])typeHandlersProvider.getIfAvailable();
        this.languageDrivers = (LanguageDriver[])languageDriversProvider.getIfAvailable();
        this.resourceLoader = resourceLoader;
        this.databaseIdProvider = (DatabaseIdProvider)databaseIdProvider.getIfAvailable();
        this.configurationCustomizers = (List)configurationCustomizersProvider.getIfAvailable();
    }

    public void afterPropertiesSet() {
        this.checkConfigFileExists();
    }

    private void checkConfigFileExists() {
        if (this.properties.isCheckConfigLocation() && StringUtils.hasText(this.properties.getConfigLocation())) {
            Resource resource = this.resourceLoader.getResource(this.properties.getConfigLocation());
            Assert.state(resource.exists(), "Cannot find config location: " + resource + " (please add config file or check your Mybatis configuration)");
        }

    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        if (StringUtils.hasText(this.properties.getConfigLocation())) {
            factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
        }

        this.applyConfiguration(factory);
        if (this.properties.getConfigurationProperties() != null) {
            factory.setConfigurationProperties(this.properties.getConfigurationProperties());
        }

        if (!ObjectUtils.isEmpty(this.interceptors)) {
            factory.setPlugins(this.interceptors);
        }

        if (this.databaseIdProvider != null) {
            factory.setDatabaseIdProvider(this.databaseIdProvider);
        }

        if (StringUtils.hasLength(this.properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
        }

        if (this.properties.getTypeAliasesSuperType() != null) {
            factory.setTypeAliasesSuperType(this.properties.getTypeAliasesSuperType());
        }

        if (StringUtils.hasLength(this.properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
        }

        if (!ObjectUtils.isEmpty(this.typeHandlers)) {
            factory.setTypeHandlers(this.typeHandlers);
        }

        if (!ObjectUtils.isEmpty(this.properties.resolveMapperLocations())) {
            factory.setMapperLocations(this.properties.resolveMapperLocations());
        }

        Set<String> factoryPropertyNames = (Set)Stream.of((new BeanWrapperImpl(SqlSessionFactoryBean.class)).getPropertyDescriptors()).map(FeatureDescriptor::getName).collect(Collectors.toSet());
        Class<? extends LanguageDriver> defaultLanguageDriver = this.properties.getDefaultScriptingLanguageDriver();
        if (factoryPropertyNames.contains("scriptingLanguageDrivers") && !ObjectUtils.isEmpty(this.languageDrivers)) {
            factory.setScriptingLanguageDrivers(this.languageDrivers);
            if (defaultLanguageDriver == null && this.languageDrivers.length == 1) {
                defaultLanguageDriver = this.languageDrivers[0].getClass();
            }
        }

        if (factoryPropertyNames.contains("defaultScriptingLanguageDriver")) {
            factory.setDefaultScriptingLanguageDriver(defaultLanguageDriver);
        }

        return factory.getObject();
    }

    private void applyConfiguration(SqlSessionFactoryBean factory) {
        org.apache.ibatis.session.Configuration configuration = this.properties.getConfiguration();
        if (configuration == null && !StringUtils.hasText(this.properties.getConfigLocation())) {
            configuration = new org.apache.ibatis.session.Configuration();
        }

        if (configuration != null && !CollectionUtils.isEmpty(this.configurationCustomizers)) {
            Iterator var3 = this.configurationCustomizers.iterator();

            while(var3.hasNext()) {
                ConfigurationCustomizer customizer = (ConfigurationCustomizer)var3.next();
                customizer.customize(configuration);
            }
        }

        factory.setConfiguration(configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        ExecutorType executorType = this.properties.getExecutorType();
        return executorType != null ? new SqlSessionTemplate(sqlSessionFactory, executorType) : new SqlSessionTemplate(sqlSessionFactory);
    }

    @Configuration
    @Import({MybatisAutoConfiguration.AutoConfiguredMapperScannerRegistrar.class})
    @ConditionalOnMissingBean({MapperFactoryBean.class, MapperScannerConfigurer.class})
    public static class MapperScannerRegistrarNotFoundConfiguration implements InitializingBean {
        public MapperScannerRegistrarNotFoundConfiguration() {
        }

        public void afterPropertiesSet() {
            MybatisAutoConfiguration.logger.debug("Not found configuration for registering mapper bean using @MapperScan, MapperFactoryBean and MapperScannerConfigurer.");
        }
    }

    public static class AutoConfiguredMapperScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar {
        private BeanFactory beanFactory;

        public AutoConfiguredMapperScannerRegistrar() {
        }

        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            if (!AutoConfigurationPackages.has(this.beanFactory)) {
                MybatisAutoConfiguration.logger.debug("Could not determine auto-configuration package, automatic mapper scanning disabled.");
            } else {
                MybatisAutoConfiguration.logger.debug("Searching for mappers annotated with @Mapper");
                List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
                if (MybatisAutoConfiguration.logger.isDebugEnabled()) {
                    packages.forEach((pkg) -> {
                        MybatisAutoConfiguration.logger.debug("Using auto-configuration base package '{}'", pkg);
                    });
                }

                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
                builder.addPropertyValue("processPropertyPlaceHolders", true);
                builder.addPropertyValue("annotationClass", Mapper.class);
                builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(packages));
                BeanWrapper beanWrapper = new BeanWrapperImpl(MapperScannerConfigurer.class);
                Set<String> propertyNames = (Set)Stream.of(beanWrapper.getPropertyDescriptors()).map(FeatureDescriptor::getName).collect(Collectors.toSet());
                if (propertyNames.contains("lazyInitialization")) {
                    builder.addPropertyValue("lazyInitialization", "${mybatis.lazy-initialization:false}");
                }

                if (propertyNames.contains("defaultScope")) {
                    builder.addPropertyValue("defaultScope", "${mybatis.mapper-default-scope:}");
                }

                registry.registerBeanDefinition(MapperScannerConfigurer.class.getName(), builder.getBeanDefinition());
            }
        }

        public void setBeanFactory(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }
    }
}
```



##### 示例

```java
public class TestDataSourceAuto {
    @SuppressWarnings("all")
    public static void main(String[] args) {
        GenericApplicationContext context = new GenericApplicationContext();
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().addLast(new SimpleCommandLinePropertySource(
                "--spring.datasource.url=jdbc:mysql://localhost:3306/test",
                "--spring.datasource.username=root",
                "--spring.datasource.password=root"
        ));
        context.setEnvironment(env);
        AnnotationConfigUtils.registerAnnotationConfigProcessors(context.getDefaultListableBeanFactory());
        context.registerBean(Config.class);

        // 注册要扫描的包范围
        String packageName = TestDataSourceAuto.class.getPackage().getName();
        System.out.println("当前包名:" + packageName);
        AutoConfigurationPackages.register(context.getDefaultListableBeanFactory(),
                packageName);

        context.refresh();
        for (String name : context.getBeanDefinitionNames()) {
            String resourceDescription = context.getBeanDefinition(name).getResourceDescription();
            if (resourceDescription != null)
                System.out.println(name + " 来源:" + resourceDescription);
        }
    }

    @Configuration
    @Import(MyImportSelector.class)
    static class Config {

    }

    static class MyImportSelector implements DeferredImportSelector {
        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {
            return new String[]{
                    DataSourceAutoConfiguration.class.getName(),
                    MybatisAutoConfiguration.class.getName()
            };
        }
    }
}
```



#### TransactionAutoConfiguration

##### 源码

```java
@Configuration(
    proxyBeanMethods = false
)
@Role(2)
public class ProxyTransactionManagementConfiguration extends AbstractTransactionManagementConfiguration {
    public ProxyTransactionManagementConfiguration() {
    }

    @Bean(
        name = {"org.springframework.transaction.config.internalTransactionAdvisor"}
    )
    @Role(2)
    public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor(TransactionAttributeSource transactionAttributeSource, TransactionInterceptor transactionInterceptor) {
        BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
        advisor.setTransactionAttributeSource(transactionAttributeSource);
        advisor.setAdvice(transactionInterceptor);
        if (this.enableTx != null) {
            advisor.setOrder((Integer)this.enableTx.getNumber("order"));
        }

        return advisor;
    }

    @Bean
    @Role(2)
    public TransactionAttributeSource transactionAttributeSource() {
        return new AnnotationTransactionAttributeSource();
    }

    @Bean
    @Role(2)
    public TransactionInterceptor transactionInterceptor(TransactionAttributeSource transactionAttributeSource) {
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionAttributeSource(transactionAttributeSource);
        if (this.txManager != null) {
            interceptor.setTransactionManager(this.txManager);
        }

        return interceptor;
    }
}
```



##### 示例

```java
public class TestDataSourceAuto {
    @SuppressWarnings("all")
    public static void main(String[] args) {
        GenericApplicationContext context = new GenericApplicationContext();
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().addLast(new SimpleCommandLinePropertySource(
                "--spring.datasource.url=jdbc:mysql://localhost:3306/test",
                "--spring.datasource.username=root",
                "--spring.datasource.password=root"
        ));
        context.setEnvironment(env);
        AnnotationConfigUtils.registerAnnotationConfigProcessors(context.getDefaultListableBeanFactory());
        context.registerBean(Config.class);

        context.refresh();
        for (String name : context.getBeanDefinitionNames()) {
            String resourceDescription = context.getBeanDefinition(name).getResourceDescription();
            if (resourceDescription != null)
                System.out.println(name + " 来源:" + resourceDescription);
        }
    }

    @Configuration
    @Import(MyImportSelector.class)
    static class Config {

    }

    static class MyImportSelector implements DeferredImportSelector {
        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {
            return new String[]{
                    DataSourceAutoConfiguration.class.getName(),
                    DataSourceTransactionManagerAutoConfiguration.class.getName(),
                    TransactionAutoConfiguration.class.getName()
            };
        }
    }
}
```



* 事务自动配置类有两个：
  * `org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration`
  * `org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration`

* 前者配置了 DataSourceTransactionManager 用来执行事务的提交、回滚操作
* 后者功能上对标 @EnableTransactionManagement，包含以下三个 bean
  * BeanFactoryTransactionAttributeSourceAdvisor 事务切面类，包含通知和切点
  * TransactionInterceptor 事务通知类，由它在目标方法调用前后加入事务操作
  * AnnotationTransactionAttributeSource 会解析 @Transactional 及事务属性，也包含了切点功能
* 如果自己配置了 DataSourceTransactionManager 或是在引导类加了 @EnableTransactionManagement，则以自己配置的为准



#### ServletWebServerFactoryAutoConfiguration

* 提供 ServletWebServerFactory



#### DispatcherServletAutoConfiguration

* 提供 DispatcherServlet
* 提供 DispatcherServletRegistrationBean



#### WebMvcAutoConfiguration

* 配置 DispatcherServlet 的各项组件，提供的 bean 见过的有
  * 多项 HandlerMapping
  * 多项 HandlerAdapter
  * HandlerExceptionResolver



#### ErrorMvcAutoConfiguration

* 提供的 bean 有 BasicErrorController



#### MultipartAutoConfiguration

* 它提供了 org.springframework.web.multipart.support.StandardServletMultipartResolver
* 该 bean 用来解析 multipart/form-data 格式的数据



#### HttpEncodingAutoConfiguration

* POST 请求参数如果有中文，无需特殊设置，这是因为 Spring Boot 已经配置了 org.springframework.boot.web.servlet.filter.OrderedCharacterEncodingFilter
* 对应配置 server.servlet.encoding.charset=UTF-8，默认就是 UTF-8
* 当然，它只影响非 json 格式的数据



#### 自动配置类原理

##### 示例

假设已有第三方的两个自动配置类

```java
// @Configuration // 第三方的配置类
public class AutoConfiguration1 {
    @Bean
    @ConditionalOnMissingBean
    public Bean1 bean1() {
        return new Bean1("第三方");
    }
}

// @Configuration // 第三方的配置类
public class AutoConfiguration2 {
    @Bean
    public Bean2 bean2() {
        return new Bean2();
    }
}
```

提供一个配置文件 META-INF/spring.factories，key 为导入器类名，值为多个自动配置类名，用逗号分隔

```properties
cn.example.uums.test.a41.MyImportSelector=\
cn.example.uums.test.a41.AutoConfiguration1,\
cn.example.uums.test.a41.AutoConfiguration2
```



引入自动配置

```java
@Configuration // 本项目的配置类
// @Import({AutoConfiguration1.class, AutoConfiguration2.class})
@Import(MyImportSelector.class)
public class Config {
    @Bean
    public Bean1 bean1() {
        return new Bean1("本项目");
    }
}

public class MyImportSelector implements DeferredImportSelector { // ImportSelector
    // 该方法从 META-INF/spring.factories 读取自动配置类名，返回的 String[] 即为要导入的配置类
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        // return new String[]{AutoConfiguration1.class.getName(), AutoConfiguration2.class.getName()};

        System.out.println(">>>>>>>>>>>>>>>>>>>>> 自动配置类 >>>>>>>>>>>>>>>>>>>>>>>");
        for (String name : SpringFactoriesLoader.loadFactoryNames(EnableAutoConfiguration.class, null)) {
            System.out.println(name);
        }
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        List<String> names = SpringFactoriesLoader.loadFactoryNames(MyImportSelector.class, null);
        return names.toArray(new String[0]);
    }
}
```



```java
public class Application {

    @SuppressWarnings("all")
    public static void main(String[] args) throws IOException {
        GenericApplicationContext context = new GenericApplicationContext();
        context.getDefaultListableBeanFactory().setAllowBeanDefinitionOverriding(false);
        context.registerBean("config", Config.class);
        context.registerBean(ConfigurationClassPostProcessor.class);
        context.refresh();

        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(context.getBean(Bean1.class));
    }
}
```



#### 总结

1. 自动配置类本质上就是一个配置类而已，只是用 META-INF/spring.factories 管理，与应用配置类解耦
2. @Enable 打头的注解本质是利用了 @Import
3. @Import 配合 DeferredImportSelector 即可实现导入，selectImports 方法的返回值即为要导入的配置类名
4. DeferredImportSelector 的导入会在最后执行，为的是让其它配置优先解析



### 条件装配底层

#### 基础

条件装配的底层是本质上是 @Conditional 与 @Condition，这两个注解。引入自动配置类时，期望满足一定条件才能被 Spring 管理，不满足则不管理，怎么做呢？

比如条件是【类路径下必须有 dataSource】这个 bean ，怎么做呢？

首先编写条件判断类，它实现 Condition 接口，编写条件判断逻辑

其次，在要导入的自动配置类上添加 `@Conditional(MyCondition1.class)`，将来此类被导入时就会做条件检查

```java
@Configuration // 第三方的配置类
@Conditional(MyCondition1.class)
public class AutoConfiguration1 {
    @Bean
    public Bean1 bean1() {
        return new Bean1();
    }
}

@Configuration // 第三方的配置类
@Conditional(MyCondition2.class)
public class AutoConfiguration2 {
    @Bean
    public Bean2 bean2() {
        return new Bean2();
    }
}

public class MyCondition1 implements Condition { // 存在 Druid 依赖
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return ClassUtils.isPresent("com.alibaba.druid.pool.DruidDataSource", null);
    }
}

public class MyCondition2 implements Condition { // 不存在 Druid 依赖
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return !ClassUtils.isPresent("com.alibaba.druid.pool.DruidDataSource", null);
    }
}

public class Bean1 {}
public class Bean2 {}
```



```java
@Configuration // 本项目的配置类
@Import(MyImportSelector.class)
public class Config {
}

public class MyImportSelector implements DeferredImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{AutoConfiguration1.class.getName(), AutoConfiguration2.class.getName()};
    }
}
```



```java
public class Application {

    @SuppressWarnings("all")
    public static void main(String[] args) throws IOException {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("config", Config.class);
        context.registerBean(ConfigurationClassPostProcessor.class);
        context.refresh();

        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }
    }
}
```

分别测试加入和去除 druid 依赖，观察 bean1 是否存在于容器

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.1.17</version>
</dependency>
```

##### 总结

1. 学习一种特殊的 if - else



#### 进阶

```java
@Configuration // 第三方的配置类
@ConditionalOnClass(className = "com.alibaba.druid.pool.DruidDataSource", exists = false)
public class AutoConfiguration1 {
    @Bean
    public Bean1 bean1() {
        return new Bean1();
    }
}

@Configuration // 第三方的配置类
@ConditionalOnClass(className = "com.alibaba.druid.pool.DruidDataSource", exists = true)
public class AutoConfiguration2 {
    @Bean
    public Bean2 bean2() {
        return new Bean2();
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Conditional(MyCondition.class)
@interface ConditionalOnClass {
    boolean exists(); // true 判断存在 false 判断不存在

    String className(); // 要判断的类名
}

public class MyCondition implements Condition { // 存在 Druid 依赖
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnClass.class.getName());
        String className = attributes.get("className").toString();
        boolean exists = (boolean) attributes.get("exists");
        boolean present = ClassUtils.isPresent(className, null);
        return exists ? present : !present;
    }
}

public class Bean1 {}
public class Bean2 {}
```



```java
@Configuration // 本项目的配置类
@Import(MyImportSelector.class)
public class Config {
}

public class MyImportSelector implements DeferredImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{AutoConfiguration1.class.getName(), AutoConfiguration2.class.getName()};
    }
}
```



```java
public class Application {

    @SuppressWarnings("all")
    public static void main(String[] args) throws IOException {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("config", Config.class);
        context.registerBean(ConfigurationClassPostProcessor.class);
        context.refresh();

        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }
    }
}
```



## 其它

### FactoryBean

#### 示例



```java
@Component
public class Bean2 {
}

public class Bean1 implements BeanFactoryAware {
    private static final Logger log = LoggerFactory.getLogger(Bean1.class);

    private Bean2 bean2;

    @Autowired
    public void setBean2(Bean2 bean2) {
        log.debug("setBean2({})", bean2);
        this.bean2 = bean2;
    }

    public Bean2 getBean2() {
        return bean2;
    }

    @PostConstruct
    public void init() {
        log.debug("init");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        log.debug("setBeanFactory({})", beanFactory);
    }
}
```



```java
@Component("bean1")
public class Bean1FactoryBean implements FactoryBean<Bean1> {

    private static final Logger log = LoggerFactory.getLogger(Bean1FactoryBean.class);

    // 决定了根据【类型】获取或依赖注入能否成功
    @Override
    public Class<?> getObjectType() {
        return Bean1.class;
    }

    // 决定了 getObject() 方法被调用一次还是多次
    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public Bean1 getObject() throws Exception {
        Bean1 bean1 = new Bean1();
        log.debug("create bean: {}", bean1);
        return bean1;
    }
}
```



```java
@Component
public class Bean1PostProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(Bean1PostProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.equals("bean1") && bean instanceof Bean1) {
            log.debug("before [{}] init", beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.equals("bean1") && bean instanceof Bean1) {
            log.debug("after [{}] init", beanName);
        }
        return bean;
    }
}
```



```java
@ComponentScan
public class Application {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Application.class);

        Bean1 bean1 = (Bean1) context.getBean("bean1");
        Bean1 bean2 = (Bean1) context.getBean("bean1");
        Bean1 bean3 = (Bean1) context.getBean("bean1");
        System.out.println(bean1);
        System.out.println(bean2);
        System.out.println(bean3);

        System.out.println(context.getBean(Bean1.class));

        System.out.println(context.getBean(Bean1FactoryBean.class));
        System.out.println(context.getBean("&bean1"));

        context.close();
    }

}
```



#### 总结

一个在 Spring 发展阶段中重要, 但目前已经很鸡肋的接口 FactoryBean 的使用要点
说它鸡肋有两点:

1. 它的作用是用制造创建过程较为复杂的产品, 如 SqlSessionFactory, 但 @Bean 已具备等价功能
2. 使用上较为古怪, 一不留神就会用错
   1. 被 FactoryBean 创建的产品
      * 会认为创建、依赖注入、Aware 接口回调、前初始化这些都是 FactoryBean 的职责, 这些流程都不会走
      * 唯有后初始化的流程会走, 也就是产品可以被代理增强
      * 单例的产品不会存储于 BeanFactory 的 singletonObjects 成员中, 而是另一个 factoryBeanObjectCache 成员中
   2. 按名字去获取时, 拿到的是产品对象, 名字前面加 & 获取的是工厂对象

但目前此接口的实现仍被大量使用, 想被全面废弃很难



### @Indexed 原理

真实项目中，只需要加入以下依赖即可

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context-indexer</artifactId>
    <optional>true</optional>
</dependency>
```



#### 示例

```java
@Component
public class Bean1 {
}
@Component
public class Bean2 {
}
@Component
public class Bean3 {
}

```



```java
public class Application {
    public static void main(String[] args) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        // 组件扫描的核心类
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(beanFactory);

        scanner.scan(A44.class.getPackage().getName());

        for (String name : beanFactory.getBeanDefinitionNames()) {
            System.out.println(name);
        }
    }
}
```



#### 总结

1. 在编译时就根据 @Indexed 生成 META-INF/spring.components 文件
2. 扫描时
   * 如果发现 META-INF/spring.components 存在, 以它为准加载 bean definition
   * 否则, 会遍历包下所有 class 资源 (包括 jar 内的)
3. 解决的问题，在编译期就找到 @Component 组件，节省运行期间扫描 @Component 的时间



### 代理进一步理解

#### 示例

```java
@Component
public class Bean2 {
}

@Component
public class Bean1 {

    private static final Logger log = LoggerFactory.getLogger(Bean1.class);

    protected Bean2 bean2;

    protected boolean initialized;

    @Autowired
    public void setBean2(Bean2 bean2) {
        log.debug("setBean2(Bean2 bean2)");
        this.bean2 = bean2;
    }

    @PostConstruct
    public void init() {
        log.debug("init");
        initialized = true;
    }

    public Bean2 getBean2() {
        log.debug("getBean2()");
        return bean2;
    }

    public boolean isInitialized() {
        log.debug("isInitialized()");
        return initialized;
    }

    public void m1() {
        System.out.println("m1() 成员方法");
    }

    final public void m2() {
        System.out.println("m2() final 方法");
    }

    static public void m3() {
        System.out.println("m3() static 方法");
    }

    private void m4() {
        System.out.println("m4() private 方法");
    }

}
```



```java
@Aspect
@Component
public class MyAspect {

    // 故意对所有方法增强
    @Before("execution(* cn.example.uums.test.a45.Bean1.*(..))")
    public void before() {
        System.out.println("before");
    }
}
```



```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        Bean1 proxy = context.getBean(Bean1.class);



        /*
            1.演示 spring 代理的设计特点
                依赖注入和初始化影响的是原始对象
                代理与目标是两个对象，二者成员变量并不共用数据
         */
        showProxyAndTarget(proxy);

        System.out.println(">>>>>>>>>>>>>>>>>>>");
        System.out.println(proxy.getBean2());
        System.out.println(proxy.isInitialized());

        /*
            2.演示 static 方法、final 方法、private 方法均无法增强
         */

        proxy.m1();
        proxy.m2();
        proxy.m3();
        Method m4 = Bean1.class.getDeclaredMethod("m4");
        m4.setAccessible(true);
        m4.invoke(proxy);

        context.close();
    }


    public static void showProxyAndTarget(Bean1 proxy) throws Exception {
        System.out.println(">>>>> 代理中的成员变量");
        System.out.println("\tinitialized=" + proxy.initialized);
        System.out.println("\tbean2=" + proxy.bean2);

        if (proxy instanceof Advised) {
            Advised advised = (Advised) proxy;
            System.out.println(">>>>> 目标中的成员变量");
            Bean1 target = (Bean1) advised.getTargetSource().getTarget();
            System.out.println("\tinitialized=" + target.initialized);
            System.out.println("\tbean2=" + target.bean2);
        }
    }

}
```



#### 总结

1. spring 代理的设计特点

   * 依赖注入和初始化影响的是原始对象
     * 因此 cglib 不能用 MethodProxy.invokeSuper()

   * 代理与目标是两个对象，二者成员变量并不共用数据

2. static 方法、final 方法、private 方法均无法增强

   * 进一步理解代理增强基于方法重写



### @Value 装配底层

#### 按类型装配的步骤

1. 查看需要的类型是否为 Optional，是，则进行封装（非延迟），否则向下走
2. 查看需要的类型是否为 ObjectFactory 或 ObjectProvider，是，则进行封装（延迟），否则向下走
3. 查看需要的类型（成员或参数）上是否用 @Lazy 修饰，是，则返回代理，否则向下走
4. 解析 @Value 的值
   1. 如果需要的值是字符串，先解析 ${ }，再解析 #{ }
   2. 不是字符串，需要用 TypeConverter 转换
5. 看需要的类型是否为 Stream、Array、Collection、Map，是，则按集合处理，否则向下走
6. 在 BeanFactory 的 resolvableDependencies 中找有没有类型合适的对象注入，没有向下走
7. 在 BeanFactory 及父工厂中找类型匹配的 bean 进行筛选，筛选时会考虑 @Qualifier 及泛型
8. 结果个数为 0 抛出 NoSuchBeanDefinitionException 异常 
9. 如果结果 > 1，再根据 @Primary 进行筛选
10. 如果结果仍 > 1，再根据成员名或变量名进行筛选
11. 结果仍 > 1，抛出 NoUniqueBeanDefinitionException 异常



#### 示例

```java
@Configuration
@SuppressWarnings("all")
public class Application {

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
        DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();

        ContextAnnotationAutowireCandidateResolver resolver = new ContextAnnotationAutowireCandidateResolver();
        resolver.setBeanFactory(beanFactory);

        System.out.println(">>>>>>>>>>>>>>>@Value(\"${MAVEN_HOME}\")");
        test1(context, resolver, Bean1.class.getDeclaredField("home"));
        System.out.println(">>>>>>>>>>>>>>>@Value(\"18\")");
        test2(context, resolver, Bean1.class.getDeclaredField("age"));
        System.out.println(">>>>>>>>>>>>>>>@Value(\"#{@bean3}\")");
        test3(context, resolver, Bean2.class.getDeclaredField("bean3"));
        System.out.println(">>>>>>>>>>>>>>>@Value(\"#{'hello, ' + '${MAVEN_HOME}'}\")");
        test3(context, resolver, Bean4.class.getDeclaredField("value"));
    }

    private static void test3(AnnotationConfigApplicationContext context, ContextAnnotationAutowireCandidateResolver resolver, Field field) {
        DependencyDescriptor dd1 = new DependencyDescriptor(field, false);
        // 获取 @Value 的内容
        String value = resolver.getSuggestedValue(dd1).toString();
        System.out.println(value);

        // 解析 ${}
        value = context.getEnvironment().resolvePlaceholders(value);
        System.out.println(value);
        System.out.println(value.getClass());

        // 解析 #{} @bean3
        Object bean3 = context.getBeanFactory().getBeanExpressionResolver().evaluate(value, new BeanExpressionContext(context.getBeanFactory(), null));

        // 类型转换
        Object result = context.getBeanFactory().getTypeConverter().convertIfNecessary(bean3, dd1.getDependencyType());
        System.out.println(result);
    }

    private static void test2(AnnotationConfigApplicationContext context, ContextAnnotationAutowireCandidateResolver resolver, Field field) {
        DependencyDescriptor dd1 = new DependencyDescriptor(field, false);
        // 获取 @Value 的内容
        String value = resolver.getSuggestedValue(dd1).toString();
        System.out.println(value);

        // 解析 ${}
        value = context.getEnvironment().resolvePlaceholders(value);
        System.out.println(value);
        System.out.println(value.getClass());
        Object age = context.getBeanFactory().getTypeConverter().convertIfNecessary(value, dd1.getDependencyType());
        System.out.println(age.getClass());
    }

    private static void test1(AnnotationConfigApplicationContext context, ContextAnnotationAutowireCandidateResolver resolver, Field field) {
        DependencyDescriptor dd1 = new DependencyDescriptor(field, false);
        // 获取 @Value 的内容
        String value = resolver.getSuggestedValue(dd1).toString();
        System.out.println(value);

        // 解析 ${}
        value = context.getEnvironment().resolvePlaceholders(value);
        System.out.println(value);
    }

    public class Bean1 {
        @Value("${MAVEN_HOME}")
        private String home;
        @Value("18")
        private int age;
    }

    public class Bean2 {
        @Value("#{@bean3}") // SpringEL       #{SpEL}
        private Bean3 bean3;
    }

    @Component("bean3")
    public class Bean3 {
    }

    static class Bean4 {
        @Value("#{'hello, ' + '${MAVEN_HOME}'}")
        private String value;
    }
}
```



#### 总结

1. ContextAnnotationAutowireCandidateResolver 作用之一，获取 @Value 的值
2. 了解 ${ } 对应的解析器
3. 了解 #{ } 对应的解析器
4. TypeConvert 的一项体现



### @Autowired 装配底层

#### 示例

```java
@Configuration
public class Application {
    public static void main(String[] args) throws NoSuchFieldException, NoSuchMethodException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
        DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        // 1. 根据成员变量的类型注入
        DependencyDescriptor dd1 = new DependencyDescriptor(Bean1.class.getDeclaredField("bean2"), false);
        System.out.println(beanFactory.doResolveDependency(dd1, "bean1", null, null));
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        // 2. 根据参数的类型注入
        Method setBean2 = Bean1.class.getDeclaredMethod("setBean2", Bean2.class);
        DependencyDescriptor dd2 = new DependencyDescriptor(new MethodParameter(setBean2, 0), false);
        System.out.println(beanFactory.doResolveDependency(dd2, "bean1", null, null));
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        // 3. 结果包装为 Optional<Bean2>
        DependencyDescriptor dd3 = new DependencyDescriptor(Bean1.class.getDeclaredField("bean3"), false);
        if (dd3.getDependencyType() == Optional.class) {
            dd3.increaseNestingLevel();
            Object result = beanFactory.doResolveDependency(dd3, "bean1", null, null);
            System.out.println(Optional.ofNullable(result));
        }
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        // 4. 结果包装为 ObjectProvider,ObjectFactory
        DependencyDescriptor dd4 = new DependencyDescriptor(Bean1.class.getDeclaredField("bean4"), false);
        if (dd4.getDependencyType() == ObjectFactory.class) {
            dd4.increaseNestingLevel();
            ObjectFactory objectFactory = new ObjectFactory() {
                @Override
                public Object getObject() throws BeansException {
                    return beanFactory.doResolveDependency(dd4, "bean1", null, null);
                }
            };
            System.out.println(objectFactory.getObject());
        }
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        // 5. 对 @Lazy 的处理
        DependencyDescriptor dd5 = new DependencyDescriptor(Bean1.class.getDeclaredField("bean5"), false);
        ContextAnnotationAutowireCandidateResolver resolver = new ContextAnnotationAutowireCandidateResolver();
        resolver.setBeanFactory(beanFactory);
        Object proxy = resolver.getLazyResolutionProxyIfNecessary(dd5, "bean1");
        // module java.base does not open java.lang to unnamed module
        // jdk 1.8+ VM Options --add-opens java.base/java.lang=ALL-UNNAMED
        // 或者被注入的类型重写 toString 方法
        System.out.println(proxy);
        System.out.println(proxy.getClass());
        /*
            1. Optional 及 ObjectFactory 对于内嵌类型的处理, 源码参考 ResolvableType
            2. ObjectFactory 懒惰的思想
            3. @Lazy 懒惰的思想
         */
    }

    static class Bean1 {
        @Autowired private Bean2 bean2;
        @Autowired public void setBean2(Bean2 bean2) {
            this.bean2 = bean2;
        }
        @Autowired private Optional<Bean2> bean3;
        @Autowired private ObjectFactory<Bean2> bean4;
        @Autowired @Lazy private Bean2 bean5;
    }

    @Component("bean2")
    static class Bean2 {
//        @Override
//        public String toString() {
//            return super.toString();
//        }
    }
}
```



#### doResolveDependency

##### 例一



```java
@SuppressWarnings("all")
@Configuration
public class Application {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
        DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 1. 数组类型");
        testArray(beanFactory);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 2. List 类型");
        testList(beanFactory);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 3. applicationContext");
        testApplicationContext(beanFactory);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 4. 泛型");
        testGeneric(beanFactory);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 5. @Qualifier");
        testQualifier(beanFactory);
        /*
            学到了什么
                1. 如何获取数组元素类型
                2. Spring 如何获取泛型中的类型
                3. 特殊对象的处理, 如 ApplicationContext, 并注意 Map 取值时的类型匹配问题 (另见  TestMap)
                4. 谁来进行泛型匹配 (另见 TestGeneric)
                5. 谁来处理 @Qualifier
                6. 刚开始都只是按名字处理, 等候选者确定了, 才会创建实例
         */
    }

    private static void testQualifier(DefaultListableBeanFactory beanFactory) throws NoSuchFieldException {
        DependencyDescriptor dd5 = new DependencyDescriptor(Target.class.getDeclaredField("service"), true);
        Class<?> type = dd5.getDependencyType();
        ContextAnnotationAutowireCandidateResolver resolver = new ContextAnnotationAutowireCandidateResolver();
        resolver.setBeanFactory(beanFactory);
        for (String name : BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, type)) {
            BeanDefinition bd = beanFactory.getMergedBeanDefinition(name);
            //                                                             @Qualifier("service2")
            if (resolver.isAutowireCandidate(new BeanDefinitionHolder(bd,name), dd5)) {
                System.out.println(name);
                System.out.println(dd5.resolveCandidate(name, type, beanFactory));
                break;
            }
        }
    }
    private static void testGeneric(DefaultListableBeanFactory beanFactory) throws NoSuchFieldException {
        DependencyDescriptor dd4 = new DependencyDescriptor(Target.class.getDeclaredField("dao"), true);
        Class<?> type = dd4.getDependencyType();
        ContextAnnotationAutowireCandidateResolver resolver = new ContextAnnotationAutowireCandidateResolver();
        resolver.setBeanFactory(beanFactory);
        for (String name : BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, type)) {
            BeanDefinition bd = beanFactory.getMergedBeanDefinition(name);
            // 对比 BeanDefinition 与 DependencyDescriptor 的泛型是否匹配
            if (resolver.isAutowireCandidate(new BeanDefinitionHolder(bd,name), dd4)) {
                System.out.println(name);
                System.out.println(dd4.resolveCandidate(name, type, beanFactory));
                break;
            }
        }
    }
    private static void testApplicationContext(DefaultListableBeanFactory beanFactory) throws NoSuchFieldException, IllegalAccessException {
        DependencyDescriptor dd3 = new DependencyDescriptor(Target.class.getDeclaredField("applicationContext"), true);

        Field resolvableDependencies = DefaultListableBeanFactory.class.getDeclaredField("resolvableDependencies");
        resolvableDependencies.setAccessible(true);
        Map<Class<?>, Object> dependencies = (Map<Class<?>, Object>) resolvableDependencies.get(beanFactory);
//        dependencies.forEach((k, v) -> {
//            System.out.println("key:" + k + " value: " + v);
//        });
        for (Map.Entry<Class<?>, Object> entry : dependencies.entrySet()) {
            // 左边类型           是否是           右边类型 的父类
            if (entry.getKey().isAssignableFrom(dd3.getDependencyType())) {
                System.out.println(entry.getValue());
                break;
            }
        }
    }
    private static void testList(DefaultListableBeanFactory beanFactory) throws NoSuchFieldException {
        DependencyDescriptor dd2 = new DependencyDescriptor(Target.class.getDeclaredField("serviceList"), true);
        if (dd2.getDependencyType() == List.class) {
            Class<?> resolve = dd2.getResolvableType().getGeneric().resolve();
            System.out.println(resolve);
            List<Object> list = new ArrayList<>();
            // 根据类型找到所有 bean 的名字
            String[] names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, resolve);
            for (String name : names) {
                Object bean = dd2.resolveCandidate(name, resolve, beanFactory);
                list.add(bean);
            }
            System.out.println(list);
        }
    }
    private static void testArray(DefaultListableBeanFactory beanFactory) throws NoSuchFieldException {
        DependencyDescriptor dd1 = new DependencyDescriptor(Target.class.getDeclaredField("serviceArray"), true);
        if (dd1.getDependencyType().isArray()) {
            Class<?> componentType = dd1.getDependencyType().getComponentType();
            System.out.println(componentType);
            String[] names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, componentType);
            List<Object> beans = new ArrayList<>();
            for (String name : names) {
                System.out.println(name);
                Object bean = dd1.resolveCandidate(name, componentType, beanFactory);
                beans.add(bean);
            }
            Object array = beanFactory.getTypeConverter().convertIfNecessary(beans, dd1.getDependencyType());
            System.out.println(array);
        }
    }
    static class Target {
        @Autowired private Service[] serviceArray;
        @Autowired private List<Service> serviceList;
        @Autowired private ConfigurableApplicationContext applicationContext;
        @Autowired private Dao<Teacher> dao;
        @Autowired @Qualifier("service2") private Service service;
    }
    
    interface Dao<T> {}
    @Component("dao1") static class Dao1 implements Dao<Student> {}
    @Component("dao2") static class Dao2 implements Dao<Teacher> {}

    static class Student {}

    static class Teacher {}

    interface Service {}

    @Component("service1")
    static class Service1 implements Service {}

    @Component("service2")
    static class Service2 implements Service {}

    @Component("service3")
    static class Service3 implements Service {}
}
```



##### 例二

```java
@Configuration
public class Application {
    public static void main(String[] args) throws NoSuchFieldException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
        DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
        testPrimary(beanFactory);
        testDefault(beanFactory);

        /*
            学到了什么
                1. @Primary 的处理, 其中 @Primary 会在 @Bean 解析或组件扫描时被解析 (另见 TestPrimary)
                2. 最后的防线, 通过属性或参数名匹配
         */
    }

    private static void testDefault(DefaultListableBeanFactory beanFactory) throws NoSuchFieldException {
        DependencyDescriptor dd = new DependencyDescriptor(Target2.class.getDeclaredField("service3"), false);
        Class<?> type = dd.getDependencyType();
        for (String name : BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, type)) {
            if(name.equals(dd.getDependencyName())) {
                System.out.println(name);
                break;
            }
        }
    }

    private static void testPrimary(DefaultListableBeanFactory beanFactory) throws NoSuchFieldException {
        DependencyDescriptor dd = new DependencyDescriptor(Target1.class.getDeclaredField("service"), false);
        Class<?> type = dd.getDependencyType();
        for (String name : BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, type)) {
            if (beanFactory.getMergedBeanDefinition(name).isPrimary()) {
                System.out.println(name);
                break;
            }
        }
    }

    static class Target1 {
        @Autowired private Service service;
    }

    static class Target2 {
        @Autowired private Service service3;
    }

    interface Service {}
    @Component("service1") static class Service1 implements Service {}
    @Component("service2") @Primary static class Service2 implements Service {}
    @Component("service3") static class Service3 implements Service {}
}
```



#### 总结

1. @Autowired 本质上是根据成员变量或方法参数的类型进行装配
2. 如果待装配类型是 Optional，需要根据 Optional 泛型找到 bean，再封装为 Optional 对象装配
3. 如果待装配的类型是 ObjectFactory，需要根据 ObjectFactory 泛型创建 ObjectFactory 对象装配
   * 此方法可以延迟 bean 的获取
4. 如果待装配的成员变量或方法参数上用 @Lazy 标注，会创建代理对象装配
   * 此方法可以延迟真实 bean 的获取
   * 被装配的代理不作为 bean
5. 如果待装配类型是数组，需要获取数组元素类型，根据此类型找到多个 bean 进行装配
6. 如果待装配类型是 Collection 或其子接口，需要获取 Collection 泛型，根据此类型找到多个 bean
7. 如果待装配类型是 ApplicationContext 等特殊类型
   * 会在 BeanFactory 的 resolvableDependencies 成员按类型查找装配
   * resolvableDependencies 是 map 集合，key 是特殊类型，value 是其对应对象
   * 不能直接根据 key 进行查找，而是用 isAssignableFrom 逐一尝试右边类型是否可以被赋值给左边的 key 类型
8. 如果待装配类型有泛型参数
   * 需要利用 ContextAnnotationAutowireCandidateResolver 按泛型参数类型筛选
9. 如果待装配类型有 @Qualifier
   * 需要利用 ContextAnnotationAutowireCandidateResolver 按注解提供的 bean 名称筛选
10. 有 @Primary 标注的 @Component 或 @Bean 的处理
11. 与成员变量名或方法参数名同名 bean 的处理



### 事件监听器

#### 示例一

事件监听示例

```java
// 事件解耦例子
@Configuration
public class Application {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
        context.getBean(MyService.class).doBusiness();
        context.close();
    }

}

public class MyEvent extends ApplicationEvent {
    public MyEvent(Object source) {
        super(source);
    }
}

@Component
public class MyService {
    private static final Logger log = LoggerFactory.getLogger(MyService.class);
    @Autowired
    private ApplicationEventPublisher publisher; // applicationContext
    public void doBusiness() {
        log.debug("主线业务");
        // 主线业务完成后需要做一些支线业务，下面是问题代码
        publisher.publishEvent(new MyEvent("MyService.doBusiness()"));
    }
}

// @Component
public class SmsApplicationListener implements ApplicationListener<MyEvent> {
    private static final Logger log = LoggerFactory.getLogger(SmsApplicationListener.class);
    @Override
    public void onApplicationEvent(MyEvent event) {
        log.debug("发送短信");
    }
}

@Component
public class EmailApplicationListener implements ApplicationListener<MyEvent> {
    private static final Logger log = LoggerFactory.getLogger(EmailApplicationListener.class);
    @Override
    public void onApplicationEvent(MyEvent event) {
        log.debug("发送邮件");
    }
}
```



#### 示例二

事件监听注解

```java
@Configuration
public class Application {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
        context.getBean(MyService.class).doBusiness();
        context.close();
    }

    @Bean
    public ThreadPoolTaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        return executor;
    }

    @Bean
    public SimpleApplicationEventMulticaster applicationEventMulticaster(ThreadPoolTaskExecutor executor) {
        SimpleApplicationEventMulticaster multicaster = new SimpleApplicationEventMulticaster();
        multicaster.setTaskExecutor(executor);
        return multicaster;
    }
}

public class MyEvent extends ApplicationEvent {
    public MyEvent(Object source) {
        super(source);
    }
}

@Component
public class MyService {
    private static final Logger log = LoggerFactory.getLogger(MyService.class);
    @Autowired
    private ApplicationEventPublisher publisher; // applicationContext
    public void doBusiness() {
        log.debug("主线业务");
        // 主线业务完成后需要做一些支线业务，下面是问题代码
        publisher.publishEvent(new MyEvent("MyService.doBusiness()"));
    }
}

@Component
public class SmsService {
    private static final Logger log = LoggerFactory.getLogger(SmsService.class);
    @EventListener
    public void listener(MyEvent myEvent) {
        log.debug("发送短信");
    }
}

@Component
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    @EventListener
    public void listener(MyEvent myEvent) {
        log.debug("发送邮件");
    }
}
```



#### 示例三

自定义事件监听注解

```java
@Configuration
public class Application {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
        context.getBean(MyService.class).doBusiness();
        context.close();
    }

    @Bean
    public SmartInitializingSingleton smartInitializingSingleton(ConfigurableApplicationContext context) {
        return () -> {
            for (String name : context.getBeanDefinitionNames()) {
                Object bean = context.getBean(name);
                for (Method method : bean.getClass().getMethods()) {
                    if (method.isAnnotationPresent(MyListener.class)) {
                        context.addApplicationListener((event) -> {
                            System.out.println(event);
                            // 判断传的事件对象与接收的事件对象是否一致
                            Class<?> eventType = method.getParameterTypes()[0];
                            if (eventType.isAssignableFrom(event.getClass())) {
                                try {
                                    method.invoke(bean, event);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        };
    }
}

@Component
public class MyService {
    private static final Logger log = LoggerFactory.getLogger(MyService.class);
    @Autowired
    private ApplicationEventPublisher publisher; // applicationContext

    public void doBusiness() {
        log.debug("主线业务");
        // 主线业务完成后需要做一些支线业务，下面是问题代码
        publisher.publishEvent(new MyEvent("MyService.doBusiness()"));
    }
}

@Component
public class SmsService {
    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @MyListener
    public void listener(MyEvent myEvent) {
        log.debug("发送短信");
    }
}

@Component
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @MyListener
    public void listener(MyEvent myEvent) {
        log.debug("发送邮件");
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface MyListener {
}

public class MyEvent extends ApplicationEvent {
    public MyEvent(Object source) {
        super(source);
    }
}
```



#### 总结

事件监听器的两种方式

1. 实现 ApplicationListener 接口
   * 根据接口泛型确定事件类型
2. @EventListener 标注监听方法
   * 根据监听器方法参数确定事件类型
   * 解析时机：在 SmartInitializingSingleton（所有单例初始化完成后），解析每个单例 bean



### 49) 事件发布器

#### 示例

```java
@Configuration
public class Application {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
        context.getBean(MyService.class).doBusiness();
        context.close();
    }

    @Bean
    public ThreadPoolTaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        return executor;
    }
}

@Bean
public ApplicationEventMulticaster applicationEventMulticaster(ConfigurableApplicationContext context, ThreadPoolTaskExecutor executor) {
    return new AbstractApplicationEventMulticaster() {
        private List<GenericApplicationListener> listeners = new ArrayList<>();

        // 收集监听器
        public void addApplicationListenerBean(String name) {
            ApplicationListener listener = context.getBean(name, ApplicationListener.class);
            System.out.println(listener);
            // 获取该监听器支持的事件类型
            ResolvableType type = ResolvableType.forClass(listener.getClass()).getInterfaces()[0].getGeneric();
            System.out.println(type);

            // 将原始的 listener 封装为支持事件类型检查的 listener
            GenericApplicationListener genericApplicationListener = new GenericApplicationListener() {
                // 是否支持某事件类型                真实的事件类型
                public boolean supportsEventType(ResolvableType eventType) {
                    return type.isAssignableFrom(eventType);
                }

                public void onApplicationEvent(ApplicationEvent event) {
                    executor.submit(() -> listener.onApplicationEvent(event));
                }
            };

            listeners.add(genericApplicationListener);
        }

        // 发布事件
        public void multicastEvent(ApplicationEvent event, ResolvableType eventType) {
            for (GenericApplicationListener listener : listeners) {
                if (listener.supportsEventType(ResolvableType.forClass(event.getClass()))) {
                    listener.onApplicationEvent(event);
                }
            }
        }
    };
}

public static class AbstractApplicationEventMulticaster implements ApplicationEventMulticaster {

    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {

    }

    @Override
    public void addApplicationListenerBean(String listenerBeanName) {

    }

    @Override
    public void removeApplicationListener(ApplicationListener<?> listener) {

    }

    @Override
    public void removeApplicationListenerBean(String listenerBeanName) {

    }

    @Override
    public void removeApplicationListeners(Predicate<ApplicationListener<?>> predicate) {

    }

    @Override
    public void removeApplicationListenerBeans(Predicate<String> predicate) {

    }

    @Override
    public void removeAllListeners() {

    }

    @Override
    public void multicastEvent(ApplicationEvent event) {

    }

    @Override
    public void multicastEvent(ApplicationEvent event, ResolvableType eventType) {

    }
}

public class MyEvent extends ApplicationEvent {
    public MyEvent(Object source) {
        super(source);
    }
}

@Component
public class MyService {
    private static final Logger log = LoggerFactory.getLogger(MyService.class);
    @Autowired
    private ApplicationEventPublisher publisher; // applicationContext

    public void doBusiness() {
        log.debug("主线业务");
        // 主线业务完成后需要做一些支线业务，下面是问题代码
        publisher.publishEvent(new MyEvent("MyService.doBusiness()"));
    }
}

@Component
public class SmsApplicationListener implements ApplicationListener<MyEvent> {
    private static final Logger log = LoggerFactory.getLogger(SmsApplicationListener.class);

    @Override
    public void onApplicationEvent(MyEvent event) {
        log.debug("发送短信");
    }
}

@Component
public class EmailApplicationListener implements ApplicationListener<MyEvent> {
    private static final Logger log = LoggerFactory.getLogger(EmailApplicationListener.class);

    @Override
    public void onApplicationEvent(MyEvent event) {
        log.debug("发送邮件");
    }
}
```



#### 总结

事件发布器模拟实现

1. addApplicationListenerBean 负责收集容器中的监听器
   * 监听器会统一转换为 GenericApplicationListener 对象，以支持判断事件类型
2. multicastEvent 遍历监听器集合，发布事件
   * 发布前先通过 GenericApplicationListener.supportsEventType 判断支持该事件类型才发事件
   * 可以利用线程池进行异步发事件优化
3. 如果发送的事件对象不是 ApplicationEvent 类型，Spring 会把它包装为 PayloadApplicationEvent 并用泛型技术解析事件对象的原始类型
   * 视频中未讲解







# SpringBoot

## 创建 Spring Boot 项目

### 使用 Maven 项目创建

#### pom

如果是 linux 环境，用以下命令即可获取 spring boot 的骨架 pom.xml

```shell
curl -G https://start.spring.io/pom.xml -d dependencies=web,mysql,mybatis -o pom.xml
```

也可以使用 Postman 等工具实现

若想获取更多用法，请参考

```shell
curl https://start.spring.io
```



[pom.xml.example](../files/config/Java/SpringBoot/pom.xml.example)



#### 启动类

[ExampleApplication.java](../files/config/Java/SpringBoot/ExampleApplication.java)



#### 配置

[application.yaml](../files/config/Java/SpringBoot/application.yaml.example)

[application-dev.yaml](../files/config/Java/SpringBoot/application-dev.yaml.example)



### 使用 IDEA 创建



## 配置日志

### 简介

[java日志组件介绍(common-logging,log4j,slf4j,logback)](../files/HTML/Java/SpringBoot/java日志组件介绍(common-logging,log4j,slf4j,logback).pdf)

​        SpringBoot 内部日志系统使用的是 Commons Logging 并且 SpringBoot 给 JDKLogging，Log4j2(Log4j 也是支持的)，Logback 都提供了默认配置，并且如果使用了 Starters，那么默认使用 Logback。Slf4j(Simple Logging Facade for Java)则是一个日志门面框架，提供了日志系统中常用的接口，logback 和 log4j 则对slf4j 进行了实现。

官方文档中有提到，SpringBoot 的 Logging 配置的级别有7个：
     TRACE , DEBUG , INFO , WARN , ERROR , FATAL , OFF



### 依赖

```xml
<!-- 实际开发中我们不需要直接添加该依赖，spring-boot-starter 中包含了 spring-boot-starter-logging，该依赖内容就是 Spring Boot 默认的日志框架 Logback+SLF4J。而 spring-boot-starter-web 包含了spring-boot-starter，所以我们只需要引入web组件即可 -->
<!--    <dependency>-->
<!--        <groupId>org.springframework.boot</groupId>-->
<!--        <artifactId>spring-boot-starter-logging</artifactId>-->
<!--    </dependency>-->

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!--引入日志 @Slf4j 注解-->
<dependency>
	<groupId>org.projectlombok</groupId>
	<artifactId>lombok</artifactId>
</dependency>
```



### 配置日志输出

#### 在 yaml 中简单配置 Logback

application.yaml

```yaml
logging:
  # 记录异常时使用的转换字
  # exception-conversion-word:
  file:
    # 可以是绝对路径或者相对路径
    # path: /var/logs/
    # 指定输出日志的文件
    # path 和 name 不能同时使用，如若同时使用，则只有 name 生效
    name: logs/Product.log
    # 日志文件的最长保存时间（仅默认的 Logback 设置受支持。）
    max-history: 30
    # 日志文件的最大容量（仅默认的 Logback 设置受支持。）
    max-size: 20MB
    # 日志归档的总大小，当日志存档的总大小超过该阈值时，将删除备份（仅默认的 Logback 设置受支持。）
    total-size-cap: 1GB
    # 是否在启动时清除存档日志文件（如果启用了 LOG_FILE ）（仅默认的 Logback 设置受支持。）
    # clean-history-on-start: true
  
  level:
    # root：即项目所有包的日志
    root: DEBUG
    # 包名: 指定包下的日志级别
    com.richstonedt.cmgde2eas.cs.mapper: DEBUG

  # 使用控制台输出的日志模式
  pattern:
    # 呈现日志级别时使用的格式（默认%5p）。（仅默认的Logback设置受支持。）
    level: DEBUG
    console: ${CONSOLE_LOG_PATTERN:-%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}}
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} ${LOG_LEVEL_PATTERN:-%5p} ${PID:- } --- [%15.15t] %-40.40logger{39} : %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}"
    # 日志时间格式（仅默认的Logback设置受支持。）
    # dateformat=
    # 日志文件中日志的模式
    # 过渡日志文件名的模式（默认${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz）。（仅默认的Logback设置受支持。）
    # rolling-file-name:
    # register-shutdown-hook:
```



#### 使用 Logback 自定义配置实现高级日志

application.yaml

```yaml
logging:
  # 指定 lohback 配置文件默认 logback-spring.xml
  config: classpath:logback-spring.xml
```



[logback-spring.xml](../files/config/logback-spring.xml.example)



### 使用 Slf4j API 输出日志

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class LoggingTest {
	private static final Logger logger = LoggerFactory.getLogger(LoggingTest.class);

	@Test
	public void loggerTest() {
		logger.info("Current Time:" + System.currentTimeMillis());
		logger.info("Current Time: {}", System.currentTimeMillis());
		logger.trace("trace log");
		logger.warn("warn log");
		logger.debug("debug log");
		logger.info("info log");
		logger.error("error log");
	}
}
```

### 使用 @Slf4j 注解

在类上写上@Slf4j注解，在方法中直接使用 log对象  

```java
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class LoggingTest {

	@Test
	public void loggerTest() {
		log.info("Current Time:" + System.currentTimeMillis());
		log.info("Current Time: {}", System.currentTimeMillis());
		log.trace("trace log");
		log.warn("warn log");
		log.debug("debug log");
		log.info("info log");
		log.error("error log");
	}
}
```

如果使用eclipse，使用@slf4j注解找不到log变量

原因是缺少插件Lomboz

Lomboz是一个基于LGPL的开源J2EE综合开发环境的Eclipse插件，对编码，发布，测试，以及debug等各个软件开发的生命周期提供支持，支持JSP,EJB等。Lomboz是Eclipse的一个J2EE的插件，它将很多Java应用服务器、J2EE组件和Web应用开发集成到Eclipse中，可以帮助Java开发者使用Eclipse建立、测试、部署J2EE应用。

安装Lombo方法：

到 https://projectlombok.org/download.html 下载jar包，然后将lombok.jar复制到myeclipse.ini / eclipse.ini /sts.ini所在的文件夹目录下，打开eclipse.ini / myeclipse.ini/sts.ini 添加下面两行并保存

​        -Xbootclasspath/a:lombok.jar  

​        -javaagent:lombok.jar  

 重启

常用的 lombok 注解：
@Data  ：注解在类上；提供类所有属性的 getting 和 setting 方法，此外还提供了equals、canEqual、hashCode、toString 方法
@Setter：注解在属性上；为属性提供 setting 方法
@Getter：注解在属性上；为属性提供 getting 方法
@Log4j ：注解在类上；为类提供一个 属性名为log 的 log4j 日志对象
@NoArgsConstructor：注解在类上；为类提供一个无参的构造方法
@AllArgsConstructor：注解在类上；为类提供一个全参的构造方法  



### 日志级别

日志级别从低到高分为 TRACE < DEBUG < INFO < WARN < ERROR < FATAL，如果设置为 WARN，则低于 WARN 的信息都不会输出。

trace： 追踪，就是程序推进以下，你就可以写个trace输出，所以trace应该会特别多，不过没关系，我们可以设置最低日志级别不让他输出。

debug： 调试，一般就只用这个作为最低级别，trace压根不用。是在没办法就用eclipse或者idea的debug功能就好了。

info： 输出一下你感兴趣的或者重要的信息，这个用的最多了。

warn： 有些信息不是错误信息，但是也要给程序员的一些提示，类似于eclipse中代码的验证不是有error 和warn（不算错误但是也请注意，比如以下depressed的方法）。

error： 错误信息。用的也比较多。

fatal： 级别比较高了。重大错误，这种级别你可以直接停止程序了，是不应该出现的错误么！不用那么紧张，其实就是一个程度的问题。



### slf4j 规范输出

**使用slf4j的格式化功能**

```java
Object entry = new SomeObject();
logger.debug("The entry is {}.", entry);

//SLF4J使用自己的格式化语法{}，同时提供了适合不同参数个数的方法重载：
logger.debug(String format, Object param); //支持一个参数
logger.debug(String format, Object param1, Object param2); //支持两个参数
logger.debug(String format, Object... param); //任意数量参数，构造参数数组具有一定的性能损耗

//连续的{}才被认为是格式化占位符，所以：
logger.debug("Set {1,2} differs from {}", "3"); //output:Set {1,2} differs from 3
logger.debug("Set {1,2} differs from {{}}", "3"); //output:Set {1,2} differs from {3}

//用”\”转义{}占位符
logger.debug("Set \\{} differs from {}", "3"); //output:Set {} differs from 3

//用“\”本身转义“{}”中的”\”
logger.debug("File name is C:\\\\{}.", "file.zip"); //output:File name is C:\file.zip
```



### 扩展阅读

[springBoot完美配置log4j2](../files/HTML/Java/SpringBoot/springBoot完美配置log4j2.pdf)

[Spring AOP实现后台管理系统日志管理](../files/HTML/Java/SpringBoot/Spring AOP实现后台管理系统日志管理.pdf)

[Spring Boot 日志配置(超详细)](../files/HTML/Java/SpringBoot/Spring Boot 日志配置(超详细).pdf)



### 配置 Redis

#### 依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```



RedisConfig

```java
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * <b><code>RedisConfig</code></b>
 * <p/>
 * redis配置
 * <p/>
 * <b>Creation Time:</b> 2024/6/5 22:52
 *
 * @author yang xiong
 * @since CommonAuth 1.0
 */
@EnableCaching
@Configuration
public class RedisConfig extends CachingConfigurerSupport {

    @Bean
    public KeyGenerator keyGenerator() {
        return (Object target, Method method, Object... params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object obj : params) {
                sb.append(obj.toString());
            }
            return sb.toString();
        };
    }

    /**
     * SpringBoot配置redis作为默认缓存工具
     * SpringBoot 2.0 以上版本的配置
     * @param: redisTemplate - [RedisTemplate]
     * @return: org.springframework.cache.CacheManager
     * @throws 
     * @author yang xiong
     * @date 2024/6/5 23:07
     **/
    @Bean
    public CacheManager cacheManager(RedisTemplate redisTemplate) {
        RedisCacheConfiguration defaultCacheConfiguration = RedisCacheConfiguration
                        .defaultCacheConfig()
                        // 设置key为String
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisTemplate.getStringSerializer()))
                        // 设置value 为自动转Json的Object
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisTemplate.getValueSerializer()))
                        // 不缓存null
                        .disableCachingNullValues()
                        // 缓存数据保存1小时
                        .entryTtl(Duration.ofHours(1));
        RedisCacheManager cacheManager = RedisCacheManager.RedisCacheManagerBuilder
                        // Redis 连接工厂
                        .fromConnectionFactory(redisTemplate.getConnectionFactory())
                        // 缓存配置
                        .cacheDefaults(defaultCacheConfiguration)
                        // 配置同步修改或删除 put/evict
                        .transactionAware()
                        .build();
        return cacheManager;
    }

    /**
     * @description: 配置一个 RedisTemplate<String,Object> 的 Bean
     * @param: factory - [RedisConnectionFactory]
     * @return: org.springframework.data.redis.core.RedisTemplate<java.lang.String,java.lang.Object>
     * @throws
     * @author yang xiong
     * @date 2024/6/5 23:17
     **/
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 创建RedisTemplate<String, Object>对象
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 配置连接工厂
        template.setConnectionFactory(factory);
        RedisSerializer keySerializer = getKeySerializer();
        RedisSerializer valueSerializer = getValueSerializer();
        // redis key 序列化方式使用stringSerial
        template.setKeySerializer(keySerializer);
        // redis value 序列化方式使用jackson
        template.setValueSerializer(valueSerializer);
        // redis hash key 序列化方式使用stringSerial
        template.setHashKeySerializer(keySerializer);
        // redis hash value 序列化方式使用jackson
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }

    private RedisSerializer getKeySerializer(){
        return new StringRedisSerializer();
    }

    /**
     * @description: 定义 Jackson2JsonRedisSerializer 序列化对象
     * @param:
     * @return: org.springframework.data.redis.serializer.RedisSerializer
     * @throws
     * @author yang xiong
     * @date 2024/6/5 23:15
     **/
    private RedisSerializer getValueSerializer() {
        ObjectMapper om = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会报异常
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer(Object.class);
        serializer.setObjectMapper(om);

        return serializer;
    }

}
```



#### RedisOperationsConfig

配置常用 Operations 到容器中

```java
/**
 * <b><code>RedisOperationsConfig</code></b>
 * <p/>
 * Operations配置
 * <p/>
 * <b>Creation Time:</b> 2024/6/5 23:20
 *
 * @author yang xiong
 * @since CommonAuth 1.0
 */
@Configuration
public class RedisOperationsConfig {
    /**
     * redis string
     */
    @Bean
    public ValueOperations<String, Object> valueOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForValue();
    }

    /**
     * redis hash
     */
    @Bean
    public HashOperations<String, String, Object> hashOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForHash();
    }

    /**
     * redis list
     */
    @Bean
    public ListOperations<String, Object> listOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForList();
    }

    /**
     * redis set
     */
    @Bean
    public SetOperations<String, Object> setOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForSet();
    }

    /**
     * redis zset
     */
    @Bean
    public ZSetOperations<String, Object> zSetOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForZSet();
    }
}
```



#### 配置

```yaml
spring:
  redis:
    # Redis数据库索引（默认为0）
    database: 0
    host: 192.168.31.101
    port: 6379
    password: Senna&redis@P1
    # 连接超时时间（毫秒）
    timeout: 1000
    jedis:
      pool:
        # 连接池最大连接数（使用负值表示没有限制）
        max-active=20: 20
        # 连接池最大阻塞等待时间（使用负值表示没有限制）
        max-wait: -1
        # 连接池中的最大空闲连接
        max-idle: 10
        # 连接池中的最小空闲连接
        min-idle: 0
```



## 国际化

[springboot国际化配置 - 知乎 (zhihu.com)](https://zhuanlan.zhihu.com/p/602254731)



## 全局异常处理

[为什么不建议用 try catch 处理异常？](https://mp.weixin.qq.com/s/MmovlYs_E8ZZIpFEYSGhIA)

[为什么不建议用 try catch 处理异常？](../files/HTML/Java/SpringBoot/为什么不建议用 try catch 处理异常？.pdf)

```java
/**
 * <b><code>GlobalExceptionHandler</code></b>
 * <p/>
 * 全局异常处理
 * 要在 Application 包下，若没有则在 Spplication 启动类上使用 scanBasePackages 指定包名
 * <p/>
 * <b>Creation Time:</b> 2023/11/9 22:16
 *
 * @author yang xiong
 * @since CommonSpring 1.0
 */

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 获取国际化消息
     *
     * @param e 异常
     * @return
     */
    public String getMessage(BaseException e) {
//        String code = "response." + e.getExceptionAssertResponseEnum().toString();
//        String message = unifiedMessageSource.getMessage(code, e.getArgs());
//        if (message == null || message.isEmpty()) {
//            return e.getMessage();
//        }

        return e.getMessage();
    }

    /**
     * 业务异常
     *
     * @param e 异常
     * @return 异常结果
     */
//    @ExceptionHandler(value = BusinessException.class)
//    @ResponseBody
//    public BaseResponse handleBusinessException(BaseException e) {
//        log.error("{}", e.getMessage());
//
//        return new ER(e.getExceptionAssertResponseEnum().getCode(), getMessage(e));
//    }

    /**
     * 自定义异常
     *
     * @param e 异常
     * @return 异常结果
     */
    @ExceptionHandler(value = BaseException.class)
    @ResponseBody
    public BaseResponse handleBaseException(BaseException e) {
        log.error("GlobalExceptionHandler#handleBaseException: {}", e);
        return new ER(e.getExceptionAssertResponseEnum().getCode(), getMessage(e));
    }

    /**
     * Controller上一层相关异常
     *
     * @param e 异常
     * @return 异常结果
     */
    @ExceptionHandler({
            NoHandlerFoundException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            MissingPathVariableException.class,
            MissingServletRequestParameterException.class,
            TypeMismatchException.class,
            HttpMessageNotReadableException.class,
            HttpMessageNotWritableException.class,
            HttpMediaTypeNotAcceptableException.class,
            ServletRequestBindingException.class,
            ConversionNotSupportedException.class,
            MissingServletRequestPartException.class,
            AsyncRequestTimeoutException.class
    })
    @ResponseBody
    public BaseResponse handleServletException(Exception e) {
        log.error("GlobalExceptionHandler#handleServletException: {}", e);
        CommonBusinessResponseEnum commonBusinessResponseEnum = CommonBusinessResponseEnum.SERVLET_EXCEPTION;
        try {
            commonBusinessResponseEnum = CommonBusinessResponseEnum.valueOf(StringsUtils.upperCamelCase2allCapsCase(e.getClass().getSimpleName()));
        } catch (IllegalArgumentException e1) {
            log.error("class [{}] not defined in enum {}", e.getClass().getName(), CommonBusinessResponseEnum.class.getName());
        }
        return new ER(commonBusinessResponseEnum.getCode(), commonBusinessResponseEnum.toString());
    }


    /**
     * 参数绑定/校验异常，将校验失败的所有异常组合成一条错误信息
     *
     * @param e 异常
     * @return 异常结果
     */
    @ExceptionHandler({
            BindException.class,
            MethodArgumentNotValidException.class
    })
    @ResponseBody
    public BaseResponse handleBindException(BindException e) {
        log.error("GlobalExceptionHandler#handleBindException: {}", e);
        StringBuilder msg = new StringBuilder();
        for (ObjectError error : e.getBindingResult().getAllErrors()) {
            msg.append(", ");
            if (error instanceof FieldError) {
                msg.append(((FieldError) error).getField()).append(": ");
            }
            msg.append(error.getDefaultMessage() == null ? "" : error.getDefaultMessage());
        }
        return new ER(CommonResponseEnum.ARGUE_ERROR.getCode(), msg.substring(2));
    }

    /**
     * 未定义异常
     *
     * @param e 异常
     * @return 异常结果
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public BaseResponse handleException(Exception e) {
        log.error("GlobalExceptionHandler#handleException: {}", e);
        return new ER(CommonResponseEnum.INTERNAL_SERVER_ERROR.getCode(), CommonResponseEnum.INTERNAL_SERVER_ERROR.toString());
    }

}

```



# SpringCloud

[Spring Cloud Alibaba](https://spring-cloud-alibaba-group.github.io/zh-cn/docs/next/overview/what-is-sca)



## CAP

Consistency（一致性）：数据一致更新，所有数据的变化都是同步的 

Availability（可用性）：在集群中一部分节点故障后，集群整体是否还能响应客户端的读写请求

Partition tolerance（分区容忍性）：某个节点的故障，并不影响整个系统的运行 

| 选择 | 说明                                                         |
| ---- | ------------------------------------------------------------ |
| CA   | 放弃分区容错性，加强一致性和可用性，其实就是传统的关系型数据库的选择 |
| AP   | 放弃一致性（这里说的一致性是强一致性），追求分区容错性和可用性，这是很多分布式 系统设计时的选择，例如很多NoSQL系统就是如此 |
| CP   | 放弃可用性，追求一致性和分区容错性，基本不会选择，网络问题会直接让整个系统不可用 |





## 概述

Spring Cloud的本质是在 Spring Boot 的基础上，增加了一堆微服务相关的规范，并对应用上下文 （Application Context）进行了功能增强。既然 Spring Cloud 是规范，那么就需要去实现，目前 Spring Cloud 规范已有 Spring官方，Spring Cloud Netﬂix，Spring Cloud Alibaba等实现。通过组件 化的方式，Spring Cloud将这些实现整合到一起构成全家桶式的微服务技术栈。 

Spring Cloud Netﬂix组件

| 组件名称 | 作用           |
| -------- | -------------- |
| Eureka   | 服务注册中心   |
| Ribbon   | 客户端负载均衡 |
| Feign    | 声明式服务调用 |
| Hystrix  | 客户端容错保护 |
| Zuul     | API服务网关    |

Spring Cloud Alibaba组件

| 组件名称 | 作用           |
| -------- | -------------- |
| Nacos    | 服务注册中心   |
| Sentinel | 客户端容错保护 |

Spring Cloud原生及其他组件

| 组件名称      | 作用           |
| ------------- | -------------- |
| Consul        | 服务注册中心   |
| Conﬁg         | 分布式配置中心 |
| Gateway       | API服务网关    |
| Sleuth/Zipkin | 分布式链路追踪 |



![image-20231030233544758](../pictures/202310/001.jpg)



## 环境搭建

### 数据库表

```sql
CREATE TABLE `tb_user` (  
    `id` bigint NOT NULL AUTO_INCREMENT,  
    `username` varchar(40) DEFAULT NULL COMMENT '用户名',  
    `password` varchar(40) DEFAULT NULL COMMENT '密码',  
    `age` int(3) DEFAULT NULL COMMENT '年龄',  
    `balance` decimal(10,2) DEFAULT NULL COMMENT '余额',  
    `address` varchar(80) DEFAULT NULL COMMENT '地址',  
    PRIMARY KEY (`id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `tb_product` (  
    `id` bigint NOT NULL AUTO_INCREMENT,  
    `product_name` varchar(40) DEFAULT NULL COMMENT '名称',  
    `status` int(2) DEFAULT NULL COMMENT '状态',  
    `price` decimal(10,2) DEFAULT NULL COMMENT '单价',  
    `product_desc` varchar(255) DEFAULT NULL COMMENT '描述',  
    `caption` varchar(255) DEFAULT NULL COMMENT '标题',  
    `inventory` int(11) DEFAULT NULL COMMENT '库存',  
    PRIMARY KEY (`id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `tb_order` (  
    `id` bigint NOT NULL AUTO_INCREMENT,  
    `user_id` bigint NOT NULL COMMENT '用户id',  
    `product_id` bigint NOT NULL COMMENT '商品id',  
    `number` int(11) DEFAULT NULL COMMENT '数量',  
    `price` decimal(10,2) DEFAULT NULL COMMENT '单价',  
    `amount` decimal(10,2) DEFAULT NULL COMMENT '总额',
    PRIMARY KEY (`id`),
    FOREIGN KEY (user_id) REFERENCES tb_user(id),
    FOREIGN KEY (product_id) REFERENCES tb_product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 

### 父项目

创建一个SpringBoot父项目shop_service

子项目

shop_service_common

shop_service_user

shop_service_order

shop_service_product

#### 相关依赖

#### shop_service

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project
	xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.3.0.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.shop</groupId>
	<artifactId>shop_service</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>pom</packaging>
	<name>shop_service</name>
	<description>Demo project for Spring Boot</description>

	<properties>
		<java.version>1.8</java.version>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-logging</artifactId>
		</dependency>	
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-tomcat</artifactId>
			<scope>provided</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-devtools</artifactId>
			<scope>runtime</scope>
			<optional>true</optional>
		</dependency>
		<dependency>
		    <groupId>junit</groupId>
		    <artifactId>junit</artifactId>
		    <!-- <version>4.13</version> -->
		    <scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
			<exclusions>
				<exclusion>
					<groupId>org.junit.vintage</groupId>
					<artifactId>junit-vintage-engine</artifactId>
				</exclusion>
			</exclusions>
		</dependency>
	</dependencies>
	<dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>Hoxton.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
    <modules>
		<module>shop_service_user</module>
		<module>shop_service_product</module>
		<module>shop_service_order</module>
		<module>shop_service_common</module>
	</modules>
</project>

```

### 子模块

使用eclipse开发时一个子模块引用另一个子模块的内容

右键项目→Properties→Java Build Path→Projects→Add

添加需要引用的子模块即可

#### shop_service_common

##### pom.xml

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.demo</groupId>
    <artifactId>shop_service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
  </parent>
  <artifactId>shop_service_common</artifactId>
  <packaging>war</packaging>
   	<dependencies>
		<!-- https://mvnrepository.com/artifact/com.baomidou/mybatis-plus-boot-starter -->
		<dependency>
		    <groupId>com.baomidou</groupId>
		    <artifactId>mybatis-plus-boot-starter</artifactId>
		    <version>3.3.2</version>
		</dependency>
	</dependencies>
</project>
```

##### entity

```java
@TableName("tb_user")
public class User {
	@TableId(type=IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private Integer age;
    private Double balance;//余额
    private String address;
 	//省略getter、setter   
}
@TableName("tb_order")
public class Order {
	@TableId(type=IdType.AUTO)
	private Long id;
	private Long userId;
	private Long productId;
	private Integer number;
	private Double price;
	private Double amount;//总额
    //省略getter、setter   
}
@TableName("tb_product")
public class Product {
	@TableId(type=IdType.AUTO)
	private Long id;
	private String productName;
	private Integer status;
	private Double price;
	private String productDesc;
	private String caption;//标题
	private Integer inventory;//库存 	
    //省略getter、setter   
}
```



#### shop_service_user

##### application.properties

```properties
spring.http.encoding.force=true
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
server.tomcat.uri-encoding=UTF-8
server.port=8002
spring.application.name=shop-service-user

#mysql datasource
spring.datasource.url=jdbc:mysql://localhost:3306/shop_service?useSSL=false&serverTimezone=Hongkong&useUnicode=true&characterEncoding=utf-8&autoReconnect=true
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
```

##### pom.xml

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.demo</groupId>
    <artifactId>shop_service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
  </parent>
  <artifactId>shop_service_user</artifactId>
  <packaging>war</packaging>
  	<dependencies>
		<!-- https://mvnrepository.com/artifact/com.baomidou/mybatis-plus-boot-starter -->
		<dependency>
		    <groupId>com.baomidou</groupId>
		    <artifactId>mybatis-plus-boot-starter</artifactId>
		    <version>3.3.2</version>
		</dependency>
		<!-- https://mvnrepository.com/artifact/mysql/mysql-connector-java -->
		<dependency>
		    <groupId>mysql</groupId>
		    <artifactId>mysql-connector-java</artifactId>
		   <!--  <version>8.0.21</version> -->
		</dependency>
	</dependencies>
</project>
```

##### mapper

```java
@Mapper
public interface IUserMapper extends BaseMapper<User>{
	
}

```

##### service

```java
@Service
public class UserService implements IUserService {
	@Autowired
	private IUserMapper userMapper;
	public int insert(User user) {
		return userMapper.insert(user);
	}
	public int deleteById(Long id) {
		return userMapper.deleteById(id);
	}
	public void modify(User user) {
		userMapper.updateById(user);
	}
	public User findById(Long id) {
		return userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getId, id));
	}
}
```



##### web

```java
@RestController
@RequestMapping("/user")
public class UserController {
	@Autowired
	private IUserService userService;
	@GetMapping("/{id}")
	public User findById(@PathVariable("id") Long id) {
		return userService.findById(id);
	}
}
```



#### shop_service_order

##### application.properties

```properties
spring.http.encoding.force=true
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
server.tomcat.uri-encoding=UTF-8
server.port=8000
spring.application.name=shop-service-order

#mysql datasource
spring.datasource.url=jdbc:mysql://localhost:3306/shop_service?useSSL=false&serverTimezone=Hongkong&useUnicode=true&characterEncoding=utf-8&autoReconnect=true
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
```

##### pom.xml

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.demo</groupId>
    <artifactId>shop_service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
  </parent>
  <artifactId>shop_service_order</artifactId>
  <packaging>war</packaging>
  	<dependencies>
		<!-- https://mvnrepository.com/artifact/com.baomidou/mybatis-plus-boot-starter -->
		<dependency>
		    <groupId>com.baomidou</groupId>
		    <artifactId>mybatis-plus-boot-starter</artifactId>
		    <version>3.3.2</version>
		</dependency>

		<!-- https://mvnrepository.com/artifact/mysql/mysql-connector-java -->
		<dependency>
		    <groupId>mysql</groupId>
		    <artifactId>mysql-connector-java</artifactId>
		    <!-- <version>8.0.21</version> -->
		</dependency>
	</dependencies>
</project>
```

##### mapper

```java
@Mapper
public interface IOrderMapper extends BaseMapper<Order>{
	
}
```

##### service

```java
@Service
public class OrderService implements IOrderService {
	@Autowired
	private IOrderMapper orderMapper;
	public int insert(Order order) {
		return orderMapper.insert(order);
	}
	public int deleteById(Long id) {
		return orderMapper.deleteById(id);
	}
	public void modify(Order order) {
		orderMapper.updateById(order);
	}
	public Order findById(Long id) {
		return orderMapper.selectOne(Wrappers.<Order>lambdaQuery().eq(Order::getId,id));
	}
}
```



##### web

```java
@RestController
@RequestMapping("/order")
public class OrderController {
	@Autowired
	private IOrderService orderService;
	@GetMapping("/{id}")
	public Order findById(@PathVariable("id") Long id) {
		return orderService.findById(id);
	}
	@GetMapping("/buy")
	public Order buy(String uid,String pid,Integer number) {
		//通过restTemplate调用商品微服务
	    Product product=restTemplate.getForObject("http://127.0.0.1:8001/product/"+pid, Product.class);
	    User user=restTemplate.getForObject("http://127.0.0.1:8002/user/"+uid, User.class);
	    Order order=null;
	    if(null!=product&&null!=user&&null!=number) {
	    	order=new Order();
	    	order.setAmount(number*product.getPrice());
	    	order.setNumber(number);
	    	order.setPrice(product.getPrice());
	    	order.setProductId(product.getId());
	    	order.setUserId(user.getId());
	    	orderService.insert(order);
	    	order=orderService.findById(order.getId());
	    }
	    System.out.println(order);
		return order;
	}
}

```



#### shop_service_product

##### application.properties

```properties
spring.http.encoding.force=true
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
server.tomcat.uri-encoding=UTF-8
server.port=8001
spring.application.name=shop-service-product

#mysql datasource
spring.datasource.url=jdbc:mysql://localhost:3306/shop_service?useSSL=false&serverTimezone=Hongkong&useUnicode=true&characterEncoding=utf-8&autoReconnect=true
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
```

##### pom.xml

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.demo</groupId>
    <artifactId>shop_service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
  </parent>
  <artifactId>shop_service_product</artifactId>
  <packaging>war</packaging>
    <dependencies>
		<!-- https://mvnrepository.com/artifact/com.baomidou/mybatis-plus-boot-starter -->
		<dependency>
		    <groupId>com.baomidou</groupId>
		    <artifactId>mybatis-plus-boot-starter</artifactId>
		    <version>3.3.2</version>
		</dependency>
		<!-- https://mvnrepository.com/artifact/mysql/mysql-connector-java -->
		<dependency>
		    <groupId>mysql</groupId>
		    <artifactId>mysql-connector-java</artifactId>
		    <!-- <version>8.0.21</version> -->
		</dependency>
	</dependencies>
</project>
```

##### mapper

```java
@Mapper
public interface IProductMapper extends BaseMapper<Product>{
	
}

```

##### service

```java
@Service
public class ProductService implements IProductService {
	@Autowired
	private IProductMapper productMapper;
	public int insert(Product product) {
		return productMapper.insert(product);
	}
	public int deleteById(Long id) {
		return productMapper.deleteById(id);
	}
	public void modify(Product product) {
		productMapper.updateById(product);
	}
	public Product findById(Long id) {
		return productMapper.selectOne(Wrappers.<Product>lambdaQuery().eq(Product::getId,id));
	}
}
```



##### web

```java
@RestController
@RequestMapping("/product")
public class ProductController {
	@Autowired
	private IProductService productService;
	@GetMapping("/{id}")
	public Product findById(@PathVariable("id") Long id) {
		return productService.findById(id);
	}
}
```

## 服务调用基础

### 概述

前文已经编写了三个基础的微服务，在用户下单时需要调用商品微服务获取商品数据。那应该怎么做 呢？总人皆知商品微服务提供了供人调用的HTTP接口。所以可以再下定单的时候使用http请求的相关 工具类完成，如常见的HttpClient，OkHttp，当然也可以使用Spring提供的RestTemplate。

### RestTemplate方法介绍

| HTTP method | RestTemplate method                                          |
| ----------- | ------------------------------------------------------------ |
| GET         | getForObject(String,Class<T>,Object...)                      |
| GET         | getForEntity(String,Class<T>,Object...)                      |
| POST        | postForLocation(String,Object,Object...)                     |
| POST        | postForObject(String,Object,,Class<T>Object...)              |
| PUT         | put(String,Object,Object..)                                  |
| DELETE      | delete(String,Object...)                                     |
| HEAD        | headForHeaders(String,Object...)                             |
| OPTIONS     | optionsForAllow(String,Object...)                            |
| any         | exchange(String,HttpMethod,HttpEntity<?>,Class<T>,Object...) |
| any         | excute(String,HttpMethod,RequestCallback,ResponseExtractor<T>,Object...) |

### 通过RestTemplate调用微服务

在启动类中配置RestTemplate

```java
 //配置RestTemplate交给spring管理
 @Bean
 public RestTemplate getRestTemplate() {
 	return new RestTemplate();
 }

```

服务调用

```java
 @PostMapping("/{id}")
 public String order(Integer num) {
     //通过restTemplate调用商品微服务
     Product object =
     restTemplate.getForObject("http://127.0.0.1:9002/product/1", Product.class);
     System.out.println(object);
     return "操作成功";
 }
```

### 存在硬编码问题

至此已经可以通过RestTemplate调用商品微服务的RESTFul API接口。但是我们把提供者的网络地址 （ip，端口）等硬编码到了代码中，这种做法存在许多问题： 

- 应用场景有局限 

- 无法动态调整

那么应该怎么解决呢，就需要通过注册中心动态的对服务注册和服务发现



## 服务注册/发现

| 组件     | 语言 | CAP  | 一致性算法 | 服务健康检查 | 对外暴露接口 |
| -------- | ---- | ---- | ---------- | ------------ | ------------ |
| Eureka   | Java | AP   | 无         | 可配支持     | HTTP         |
| Nacos    | Java | AP   | Raft       | 支持         | HTTP         |
| Consul   | Go   | CP   | Raft       | 支持         | HTTP/DNS     |
| Zookeepr | Java | CP   | Paxos      | 支持         | 客户端       |



### Eureka

- Eureka是Netflix开发的服务发现框架，SpringCloud将它集成在自己的子项目spring-cloud-netflix中， 实现SpringCloud的服务发现功能。

- 在Euraka的GitHub上，宣布Eureka 2.x闭源。近这意味着如果开发者继续使用作为 2.x 分支上现有工作 repo 一部分发布的代码库和工件，则将自负风险。

####  创建服务发现模块

##### 创建shop_server_eureka子模块

##### 引入依赖

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.demo</groupId>
    <artifactId>shop_service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
  </parent>
  <artifactId>shop_server_eureka</artifactId>
  <packaging>war</packaging>
  	<dependencies>
  		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
		</dependency>
  	</dependencies>
</project>
```

##### 配置application.properties

```properties
spring.http.encoding.force=true
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
server.tomcat.uri-encoding=UTF-8
server.port=9000
spring.application.name=shop-server-eureka

# 配置eureka
eureka.instance.hostname=localhost
# registerWithEureka:  是否将自己注册到Eureka服务中，本身就是所有无需注册
eureka.client.registerWithEureka=false
# fetchRegistry : 是否从Eureka中获取注册信息
eureka.client.fetchRegistry=false
#serviceUrlEureka: 客户端与Eureka服务端进行交互的地址
eureka.client.serviceUrl.defaultZone=http://${eureka.instance.hostname}:${server.port}/eureka/
# 为了更好的对所有服务进行定位，微服务注册到Eureka Server的时候可以手动配置示例ID
eureka.instance.instanceId=${spring.cloud.client.ip-address}:${server.port}
```

##### 配置启动类

在启动类上添加注解 @EnableEurekaServer

```java
@SpringBootApplication
@EnableEurekaServer
public class ShopServerEurekaAApplication {
	public static void main(String[] args) {
		SpringApplication.run(ShopServerEurekaAApplication.class, args);
	}
}
```

#####  服务注册中心管理后台

打开浏览器访问http://localhost:9000即可进入EurekaServer内置的管理控制台



#### 服务注册到Eureka注册中心

- 需要注册到服务注册中心的模块按以下配置

##### 在服务模块中添加如下依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-commons</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
</dependencies>
```

##### 服务模块添加配置

```properties
#eureka
# 配置eureka server的路径
eureka.client.serviceUrl.defaultZone=http://localhost:9000/eureka/
# 使用ip注册
eureka.instance.preferIpAddress=true
# 指定本服务请求ip,不指定时为本机ip
eureka.instance.ipAddress=127.0.0.1
# 为了更好的对所有服务进行定位，微服务注册到Eureka Server的时候可以手动配置示例ID
eureka.instance.instanceId=${spring.cloud.client.ip-address}:${server.port}
```

##### 修改服务模块启动类

- 启动类上添加@EnableDiscoveryClient注解或@EnableEurekaClient注解

  *从Spring Cloud Edgware版本开始， @EnableDiscoveryClient 或 @EnableEurekaClient 可省略。只需加上相关依赖，并进行相应配置，即可将微服务注册到服务发现组件上。*

```java
@SpringBootApplication
//@EnableDiscoveryClient
//@EnableEurekaClient
public class UserApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserApplication.class, args);
	}
}
```

启动子模块后在EurekaServer内置的管理控制台可以看见已启动的服务

##### Eureka中的元数据

Eureka的元数据有两种：标准元数据和自定义元数据。

- 标准元数据：主机名、IP地址、端口号、状态页和健康检查等信息，这些信息都会被发布在服务注 册表中，用于服务之间的调用。 

- 自定义元数据：可以使用eureka.instance.metadata-map配置，符合KEY/VALUE的存储格式。这 些元数据可以在远程客户端中访问。 在程序中可以使用DiscoveryClient 获取指定微服务的所有元数据信息

```java
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class RestTemplateTest {
	@Autowired
	private DiscoveryClient discoveryClient;
	@Test
	public void test() {
		//根据微服务名称从注册中心获取相关的元数据信息
		List<ServiceInstance> instances = discoveryClient.getInstances("shopservice-product");
		for (ServiceInstance instance : instances) {
			System.out.println(instance);
		}
	}
}
```



#### Eureka 高可用集群

##### 创建多个服务注册发现模块

创建模块与引入依赖和单个注册发现模块相同

配置文件修改为如下

- eureka.client.registerWithEureka=配置为true
- eureka.client.fetchRegistry=配置为true

- server.port=每个eureka服务配置为不同端口
- spring.application.name=每个eureka服务配置为不同名称
- eureka.client.serviceUrl.defaultZone=每个eureka服务配置为不包含自己的所有其它eureka url

```properties
spring.http.encoding.force=true
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
server.tomcat.uri-encoding=UTF-8
server.port=9002
spring.application.name=shop-server-eureka

# 配置eureka
eureka.instance.hostname=localhost
# registerWithEureka:  是否将自己注册到Eureka服务中，本身就是所有无需注册
eureka.client.registerWithEureka=true
# fetchRegistry : 是否从Eureka中获取注册信息
eureka.client.fetchRegistry=true
#serviceUrlEureka: 客户端与Eureka服务端进行交互的地址
eureka.client.serviceUrl.defaultZone=http://${eureka.instance.hostname}:9000/eureka/
```



##### 服务注册到Eureka Server集群

修改需要注册到服务注册中心模块的注册文件

- eureka.client.serviceUrl.defaultZone=所有eureka模块的url

```properties
# 将eureka集群的url配置到服务模块
eureka.client.serviceUrl.defaultZone=http://localhost:9000/eureka/,http://localhost:9002/eureka/
```



#### Eureka中的常见问题

##### 服务节点剔除问题

默认情况下，由于Eureka Server剔除失效服务间隔时间为90s且存在自我保护的机制。所以不能有效而 迅速的剔除失效节点，这对开发或测试会造成困扰。解决方案如下： 

Eureka Server：

 配置关闭自我保护，设置剔除无效节点的时间间隔

```properties
# 关闭自我保护
eureka.server.enableSelfPreservation=false
# 剔除时间间隔,单位:毫秒
eureka.server.evictionIntervalTimerInMs=4000
```

Eureka Client： 

配置开启健康检查，并设置续约时间

```properties
# 开启健康检查(依赖spring-boot-actuator)
eureka.client.healthcheck.enabled=true
# eureka client发送心跳给server端后，续约到期时间（默认90秒）
eureka.instance.lease-expiration-duration-in-seconds=10
# 发送心跳续约间隔
eureka.instance.lease-renewal-interval-in-seconds=5
```

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-actuator</artifactId>
    <!-- <version>2.3.3.RELEASE</version> -->
</dependency>
```



#####  监控页面显示ip

在Eureka Server的管控台中，显示的服务实例名称默认情况下是微服务定义的名称和端口。为了更好 的对所有服务进行定位，微服务注册到Eureka Server的时候可以手动配置示例ID。配置方式如下

```properties
eureka.instance.instanceId=${spring.cloud.client.ip-address}:${server.port}
```



### Nacos

[Nacos](https://spring-cloud-alibaba-group.github.io/zh-cn/docs/next/user-guide/nacos/quick-start)



#### 安装配置部署管理

示例版本 `2.2.3`

##### 安装

[GitHub 的 Release 下载页](https://github.com/alibaba/nacos/releases)，下载完成后，解压文件，启动 bin/startup 脚本



##### 配置

###### conf/application.properties

```properties
# 配置为使用 mysql 数据源
# 默认使用嵌入式数据库，0.7版本以后增加支持mysql数据源能力，目前只支持 Mysql
# 需要数据库执行初始化表 conf/mysql-schema.sql
spring.datasource.platform=mysql
spring.sql.init.platform=mysql

db.num=1
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true
db.user.0=user
db.password.0=password

# tomcat 文件目录
server.tomcat.basedir=/var/log/nacos

# 开启鉴权
nacos.core.auth.system.type=nacos
nacos.core.auth.enabled=true

## 开启服务身份识别功能
### 关闭使用user-agent判断服务端请求并放行鉴权的功能
### Since 1.4.1, Turn on/off white auth for user-agent: nacos-server, only for upgrade from old version.
nacos.core.auth.enable.userAgentAuthWhite=false

### 配置自定义身份识别的key（不可为空）和value（不可为空）
### 所有集群均需要配置相同的server.identity信息，否则可能导致服务端之间数据不一致或无法删除实例等问题。
### Since 1.4.1, worked when nacos.core.auth.enabled=true and nacos.core.auth.enable.userAgentAuthWhite=false.
### The two properties is the white list for auth and used by identity the request from other server.
nacos.core.auth.server.identity.key=example
nacos.core.auth.server.identity.value=example

# 自定义用于生成JWT令牌的密钥
# 密钥需要保持节点间一致，长时间不一致可能导致403 invalid token错误
### The default token (Base64 String):
# Base64 SecretKey987654321098765432109876543210987654321098765432109876543210
nacos.core.auth.plugin.nacos.token.secret.key=U2VjcmV0S2V5OTg3NjU0MzIxMDk4NzY1NDMyMTA5ODc2NTQzMjEwOTg3NjU0MzIxMDk4NzY1NDMyMTA5ODc2NTQzMjEw
```



###### conf/nacos-logback.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright 1999-2018 Alibaba Group Holding Ltd.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<configuration scan="true" scanPeriod="10 seconds">

	<!-- defaultValue 由 ${nacos.home}/logs 改为 /var/log/nacos -->
    <springProperty scope="context" name="logPath" source="nacos.logs.path" defaultValue="/var/log/nacos"/>
 
    <property name="LOG_HOME" value="${logPath}"/>

    <appender name="cmdb-main"
              class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- ${nacos.home}/logs 改为 ${LOG_HOME} -->
        <file>${LOG_HOME}/cmdb-main.log</file>
        <append>true</append>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <!-- ${nacos.home}/logs 改为 ${LOG_HOME} -->
            <fileNamePattern>${LOG_HOME}/logs/cmdb-main.log.%d{yyyy-MM-dd}.%i</fileNamePattern>
            <maxFileSize>2GB</maxFileSize>
            <maxHistory>7</maxHistory>
            <totalSizeCap>7GB</totalSizeCap>
            <cleanHistoryOnStart>true</cleanHistoryOnStart>
        </rollingPolicy>
        <encoder>
            <Pattern>%date %level %msg%n%n</Pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <Pattern>%date %level %msg%n%n</Pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>
    
<!--
    省略......
-->
</configuration>
```



###### bin/startup.sh

```shell
# 脚本开头加上
LOG_DIR="/var/log/nacos"

#===========================================================================================
# JVM Configuration
#===========================================================================================
if [[ "${MODE}" == "standalone" ]]; then
    JAVA_OPT="${JAVA_OPT} -Xms512m -Xmx512m -Xmn256m"
    JAVA_OPT="${JAVA_OPT} -Dnacos.standalone=true"
else
    if [[ "${EMBEDDED_STORAGE}" == "embedded" ]]; then
        JAVA_OPT="${JAVA_OPT} -DembeddedStorage=true"
    fi
    # 这里修改一下堆大小
    # JAVA_OPT="${JAVA_OPT} -server -Xms2g -Xmx2g -Xmn1g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=320m"
    JAVA_OPT="${JAVA_OPT} -server -Xms1g -Xmx1g -Xmn512m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m"
    # 这里的 ${BASE_DIR}/logs -> ${LOG_DIR}    
    JAVA_OPT="${JAVA_OPT} -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_DIR}/java_heapdump.hprof"
    JAVA_OPT="${JAVA_OPT} -XX:-UseLargePages"

fi

# 省略......

JAVA_MAJOR_VERSION=$($JAVA -version 2>&1 | sed -E -n 's/.* version "([0-9]*).*$/\1/p')
if [[ "$JAVA_MAJOR_VERSION" -ge "9" ]] ; then
  # 这里的 ${BASE_DIR}/logs -> ${LOG_DIR}
  JAVA_OPT="${JAVA_OPT} -Xlog:gc*:file=${LOG_DIR}/nacos_gc.log:time,tags:filecount=10,filesize=100m"
else
  JAVA_OPT_EXT_FIX="-Djava.ext.dirs=${JAVA_HOME}/jre/lib/ext:${JAVA_HOME}/lib/ext"
  # 这里的 ${BASE_DIR}/logs -> ${LOG_DIR}
  JAVA_OPT="${JAVA_OPT} -Xloggc:${LOG_DIR}/nacos_gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M"
fi


# 省略......

# 结尾这部分
# 增加变量
START_LOG_FILE="${LOG_DIR}/start.out"
# 这里的 ${BASE_DIR}/logs/start.out -> ${START_LOG_FILE}
if [ ! -f "${START_LOG_FILE}" ]; then
# 这里的 ${BASE_DIR}/logs/start.out -> ${START_LOG_FILE}
  touch "${START_LOG_FILE}"
fi
# start
# 这里的 ${BASE_DIR}/logs/start.out -> ${START_LOG_FILE}
echo "$JAVA $JAVA_OPT_EXT_FIX ${JAVA_OPT}" > ${START_LOG_FILE} 2>&1 &

if [[ "$JAVA_OPT_EXT_FIX" == "" ]]; then
# 这里的 ${BASE_DIR}/logs/start.out -> ${START_LOG_FILE}
  nohup "$JAVA" ${JAVA_OPT} nacos.nacos >> ${START_LOG_FILE} 2>&1 &
else
# 这里的 ${BASE_DIR}/logs/start.out -> ${START_LOG_FILE}
  nohup "$JAVA" "$JAVA_OPT_EXT_FIX" ${JAVA_OPT} nacos.nacos >> ${START_LOG_FILE} 2>&1 &
fi

# 这里的 ${BASE_DIR}/logs/start.out -> ${START_LOG_FILE}
echo "nacos is starting，you can check the ${START_LOG_FILE}"
```



##### 创建用户（组）

```shell
sudo groupadd -r apps
sudo useradd -r -g apps -s /bin/false nacos

sudo mkdir /var/log/nacos/
sudo chown -R nacos:root /opt/apps/nacos
sudo chown -R nacos:root /var/log/nacos
```



##### 服务管理

手动

```shell
# cd /opt/apps/nacos
# 集群模式启动
sudo -u nacos /opt/apps/nacos/bin/startup.sh -m cluster
# 单机模式启动
sudo -u nacos /opt/apps/nacos/bin/startup.sh -m standalone
# 停止
sudo /opt/apps/nacos/bin/shutdown.sh

ps aux | grep 'nacos'
sudo kill -9 PID
```



自动

编写[脚本](../files/scripts/server/nacos.server)文件放在 base_dir 目录下 `/opt/apps/nacos/nacos.server`

配置参考 Linux -> 服务管理  -> 系统启动时自动运行服务



启动之后在浏览器打开[链接](http://localhost:8848/nacos)，默认用户名/密码是 nacos/nacos 。

端口号和访问链接的前缀可以在配置文件 **conf/application.properties** 中修改



##### Nacos 高可用集群

注意：3个或3个以上Nacos节点才能构成集群，仅支持linux/unix/mac

1.在 nacos 的解压目录 **conf** 目录下，有配置文件 **cluster.conf**（若无则手动创建），每行配置成 **ip:port**。（配置3个或3个以上节点）

`conf/cluster.conf`

```properties
192.168.31.100:8840
192.168.31.100:8844
192.168.31.100:8842
```

2.配置后在各个节点服务器输入命令启动所有服务

3.配置 nginx 负载均衡

```properties
http {
    upstream nacoscluster {
        server 192.168.31.100:8840;
        server 192.168.31.100:8842;
        server 192.168.31.100:8844;
    }

    # another virtual host using mix of IP-, name-, and port-based configuration
    #
    server {
        listen       8848;
        server_name  localhost;

        location / {
            proxy_pass http://nacoscluster/;
        }
    }
}

stream {

    upstream nacoscluster_grpc {
        server 192.168.31.100:9840 max_fails=3 fail_timeout=10s;
        server 192.168.31.100:9842 max_fails=3 fail_timeout=10s;
		server 192.168.31.100:9844 max_fails=3 fail_timeout=10s;
    }

    server {
        listen 9848;
        proxy_connect_timeout 20s;
        proxy_timeout 5m;
        proxy_pass nacoscluster_grpc;
    }

}
```



##### 注意

Nacos2.0 版本相比 1.X 新增了 gRPC 的通信方式，因此需要增加 2 个端口。新增端口是在配置的主端口(server.port)基础上，进行一定偏移量自动生成。

| 端口 | 与主端口的偏移量 | 描述                                                       |
| ---- | ---------------- | ---------------------------------------------------------- |
| 9848 | 1000             | 客户端gRPC请求服务端端口，用于客户端向服务端发起连接和请求 |
| 9849 | 1001             | 服务端gRPC请求服务端端口，用于服务间同步等                 |

也就是在同一台机器上，如果搭建集群，建议使用8848，8850，8852，这种有间隔的端口，否则可能导致端口冲突。

如果使用的是 Docker 或存在端口转发方式启动，需要把这两个端口进行配置。

如果存在防火墙或者 Nginx 端口转发问题，需要进行相应的端口暴露配置。如在 Nginx 中，在已经暴露 8848(x) 的基础上，需要额外暴露9848（x+1000)。



#### 服务注册到 Nacos 注册中心

##### 导包

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```



##### 配置

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        file-extension: yaml
        # 需要在 Nacos 的页面创建好对应的 namespace
        namespace: ed2069f3-cc54-4a62-b044-95061643ae84
        # 随便填,注意服务之间进行调用需要在同一组
        group: MY_GROUP
        username: nacos
        password: xx
```



##### 开启服务发现

```java
@SpringBootApplication
@EnableDiscoveryClient
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```



### Consul

Consul 是 HashiCorp 公司推出的开源工具，用于实现分布式系统的服务发现与配置。与其它分布式服 务注册与发现的方案，Consul 的方案更“一站式”，内置了服务注册与发现框 架、分布一致性协议实 现、健康检查、Key/Value 存储、多数据中心方案，不再需要依赖其它工具（比如 ZooKeeper 等）。 使用起来也较 为简单。Consul 使用 Go 语言编写，因此具有天然可移植性(支持Linux、windows和 Mac OS X)；安装包仅包含一个可执行文件，方便部署，与 Docker 等轻量级容器可无缝配合。

Consul 的优势： 

- 使用 Raft 算法来保证一致性, 比复杂的 Paxos 算法更直接. 相比较而言, zookeeper 采用的是 Paxos, 而 etcd 使用的则是 Raft。 
- 支持多数据中心，内外网的服务采用不同的端口进行监听。 多数据中心集群可以避免单数据中心 的单点故障,而其部署则需要考虑网络延迟, 分片等情况等。 zookeeper 和 etcd 均不提供多数据中 心功能的支持。
- 支持健康检查。 etcd 不提供此功能。 
- 支持 http 和 dns 协议接口。 zookeeper 的集成较为复杂, etcd 只支持 http 协议。 
- 官方提供 web 管理界面, etcd 无此功能。 
- 综合比较, Consul 作为服务注册和配置管理的新星, 比较值得关注和研究。 特性： 服务发现 健康检查 Key/Value 存储 多数据中心

特性：

- 服务发现 
- 健康检查 
- Key/Value 存储 
- 多数据中心

Consul与Eureka的区别 

（1）一致性 

Consul强一致性（CP）

- 服务注册相比Eureka会稍慢一些。因为Consul的raft协议要求必须过半数的节点都写入成功才认 为注册成功 
- Leader挂掉时，重新选举期间整个consul不可用。保证了强一致性但牺牲了可用性。 

Eureka保证高可用和最终一致性（AP） 

- 服务注册相对要快，因为不需要等注册信息replicate到其他节点，也不保证注册信息是否 replicate成功 
- 当数据出现不一致时，虽然A, B上的注册信息不完全相同，但每个Eureka节点依然能够正常对外提 供服务，这会出现查询服务信息时如果请求A查不到，但请求B就能查到。如此保证了可用性但牺 牲了一致性。 

（2）开发语言和使用 

- Eureka就是个servlet程序，跑在servlet容器中 
- Consul则是go编写而成，安装启动即可

#### 安装Consul

访问Consul [官网下载](https://www.consul.io/downloads.html) Consul 的最新版本，根据不同的系统类型选择不同的安装包

##### 在Linux中安装Consul服务

```shell
# 从官网下载最新版本的Consul服务
wget -c https://releases.hashicorp.com/consul/1.8.3/consul_1.8.3_linux_amd64.zip
# 使用unzip命令解压
unzip consul_1.8.3_linux_amd64.zip -d /opt/consul/
# 解压到/opt/install/目录下并创建软连接
ln -s /opt/consul/consul-1.8.3 /opt/consul/consul
# 配置/etc/profile
export CONSUL_HOME="/opt/consul"
export PATH=$PATH:$CONSUL_HOME
source /etc/profile
# 测试一下
consul -version

#添加appgroup用户组
sudo groupadd -r appgroup
#添加consul用户
sudo useradd -r -g appgroup -s /bin/bash consul
sudo passwd consul
sudo chown -R consul:appgroup /opt/consul
```



##### 在Window中安装Consul服务

- 解压consul_1.8.3_windows_amd64.zip
- 将consul.exe配置到环境变量中
- consul -version

#### 启动Consul

```shell
su consul
# 以开发者模式快速启动，-client指定客户端可以访问的ip地址
consul agent -dev -client=0.0.0.0 -data-dir /opt/consul/data/ -bind=59.110.23.195
==> Starting Consul agent...
           Version: '1.8.3'
           Node ID: '32b6954b-b83b-21ca-e0ad-a7706842943b'
         Node name: 'DESKTOP-0EH02L0'
        Datacenter: 'dc1' (Segment: '<all>')
            Server: true (Bootstrap: false)
       Client Addr: [0.0.0.0] (HTTP: 8500, HTTPS: -1, gRPC: 8502, DNS: 8600)
      Cluster Addr: 127.0.0.1 (LAN: 8301, WAN: 8302)
           Encrypt: Gossip: false, TLS-Outgoing: false, TLS-Incoming: false, Auto-Encrypt-TLS: false
```

启动成功之后访问： http://IP:8500 ，可以看到 Consul 的管理界面

####  创建服务发现模块



#### 服务注册到Consul

- 需要注册到服务注册中心的模块按以下配置

##### 在服务模块中添加如下依赖

```xml
<!--SpringCloud提供的基于Consul的服务发现-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul-discovery</artifactId>
</dependency>
<!--actuator用于心跳检查-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

##### 服务模块添加配置

```properties
# Consul
# ConsulServer请求地址
spring.cloud.consul.host:192.168.31.87
# ConsulServer端口
spring.cloud.consul.port=8500
# 是否注册
spring.cloud.consul.discovery.register=true
# 实例的唯一id
spring.cloud.consul.discovery.instanceId=${spring.application.name}:${spring.cloud.client.ip-address}
# 服务实例名称
spring.cloud.consul.discovery.serviceName=${spring.application.name}
# 服务实例端口
spring.cloud.consul.discovery.port=${server.port}
# 开启健康检查
spring.cloud.consul.discovery.heartbeat.enabled=true
# 健康检查路径
spring.cloud.consul.discovery.healthCheckPath=/actuator/health
# 健康检查时间间隔
spring.cloud.consul.discovery.healthCheckInterval=15s
# 开启ip地址注册
spring.cloud.consul.discovery.preferIpAddress=true
# 当前微服务的请求ip
spring.cloud.consul.discovery.ipAddress=${spring.cloud.client.ip-address}
```

打开ConsulServer的[管理控制台](http://192.168.31.87:8500/)，可以发现所有微服务已经全部注册到Consul中了

##### Consul中的元数据

由于SpringCloud对Consul进行了封装。对于在消费者端获取服务提供者信息和Eureka是一致的。同样 使用 DiscoveryClient 完成调用获取微服务实例信息



#### Consul高可用集群

##### 准备环境

| 服务器ip       | consul类型 | Node（节点名称） |
| -------------- | ---------- | ---------------- |
| 192.168.31.158 | server     | server-1         |
| 192.168.31.6   | server     | server-2         |
| 192.168.31.166 | server     | server-3         |
| 192.168.31.87  | client     | client-1         |

##### 启动每个consul server节点

```shell
##登录s1虚拟机，以server形式运行
consul agent -server -bootstrap-expect 2 -data-dir /etc/consul.d -node=server-1 -bind=192.168.31.158 -ui -client 0.0.0.0 &
##登录s2 虚拟机，以server形式运行
consul agent -server -bootstrap-expect 2 -data-dir /etc/consul.d -node=server-2 -bind=192.168.31.6 -ui -client 0.0.0.0 &
##登录s3 虚拟机，以server形式运行
consul agent -server -bootstrap-expect 2 -data-dir /etc/consul.d -node=server-3 -bind=192.168.31.166 -ui -client 0.0.0.0 & 
##在本地电脑中使用client形式启动consul
consul agent -client=0.0.0.0  -data-dir D:/data/consul/consul.d -node=client-1
```

-server： 以server身份启动。

-bootstrap-expect：集群要求的最少server数量，当低于这个数量，集群即失效。 

-data-dir：data存放的目录，更多信息请参阅consul数据同步机制 

-node：节点id，在同一集群不能重复。

-bind：监听的ip地址。 

-client：客户端的ip地址(0.0.0.0表示不限制) 

& ：在后台运行，此为linux脚本语法



##### 每个节点加入集群

在server-2，server-3，client-1 服务其上通过consul join 命令加入 server-1中的consul集群中

```shell
##加入consul集群
consul join 192.168.31.158
```

##### 测试

在任意一台服务器中输入 consul members查看集群中的所有节点信息

```shell
young@server-1:~$ consul members
Node      Address              Status  Type    Build  Protocol  DC   Segment
server-1  192.168.31.158:8301  alive   server  1.8.3  2         dc1  <all>
server-2  192.168.31.6:8301    alive   server  1.8.3  2         dc1  <all>
server-3  192.168.31.166:8301  alive   server  1.8.3  2         dc1  <all>
client-1  192.168.31.87:8301   alive   client  1.8.3  2         dc1  <default>
```

打开任意ConsulServer的[管理控制台](http://192.168.56.1:8500/)，可以查看集群中的所有节点信息

##### 将微服务注册到本机 consul client

将微服务注册到本机 consul client后，打开任意ConsulServer的[管理控制台](http://192.168.31.87:8500/)，可以发现所有微服务已经全部注册到Consul集群中了



#### Consul中的常见问题

**节点和服务注销** 

当服务或者节点失效，Consul不会对注册的信息进行剔除处理，仅仅标记已状态进行标记（并且不可使 用）。如果担心失效节点和失效服务过多影响监控。可以通过调用HTTP API的形式进行处理 节点和服务的注销可以使用HTTP API: 

注销任意节点和服务：/catalog/deregister 

注销当前节点的服务：/agent/service/deregister/:service_id 

如果某个节点不继续使用了，也可以在本机使用consul leave命令，或者在其它节点使用consul forceleave 节点Id。 

**健康检查与故障转移** 

在集群环境下，健康检查是由服务注册到的Agent来处理的，那么如果这个Agent挂掉了，那么此节点 的健康检查就处于无人管理的状态。 

从实际应用看，节点上的服务可能既要被发现，又要发现别的服务，如果节点挂掉了，仅提供被发现的 功能实际上服务还是不可用的。当然发现别的服务也可以不使用本机节点，可以通过访问一个Nginx实 现的若干Consul节点的负载均衡来实现。

## 服务调用

当启动某个服务的时候，可以通过HTTP的形式 将信息注册到注册中心，并且可以通过SpringCloud提供的工具获取注册中心的服务列表。但是服务之 间的调用还存在很多的问题，如何更加方便的调用微服务，多个微服务的提供者如何选择，如何负载均 衡等。

### Ribbon

#### 概述

在 SpringCloud 中， Eureka一般配合Ribbon进行使用，Ribbon提供了客户端负载均衡的功能，Ribbon利用从Eureka中读 取到的服务信息，在调用服务节点提供的服务时，会合理的进行负载。

##### 简介

Ribbon是 Netflixfa 发布的一个负载均衡器，有助于控制 HTTP 和 TCP客户端行为。在 SpringCloud 中， Eureka一般配合Ribbon进行使用，Ribbon提供了客户端负载均衡的功能，Ribbon利用从Eureka中读 取到的服务信息，在调用服务节点提供的服务时，会合理的进行负载。

**服务调用** 

基于Ribbon实现服务调用， 是通过拉取到的所有服务列表组成（服务名-请求路径的）映射关系。借助 RestTemplate 最终进行调用 

**负载均衡** 

当有多个服务提供者时，Ribbon可以根据负载均衡的算法自动的选择需要调用的服务地址

#### 集成Ribbon

准备多个相同的服务提供者，这里准备了两个product提供者

##### pom.xml

Eureka的依赖包含了Ribbon，所以这里不需要导入任何额外的坐标

##### application.properties

在服务消费者添加如下配置，进行负载均衡策略设置

```properties
# 配置负载均衡策略 shop-service-product为需要调用的服务名称
# 不使用服务名则为全局设置
shop-service-product.ribbon.NFLoadBalancerRuleClassName=com.netflix.loadbalancer.RandomRule
```

##### 消费者启动类

在服务消费者的启动类添加基于Ribbon的服务调用与负载均衡的注解

```java
@SpringBootApplication
public class ShopServiceOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopServiceOrderApplication.class, args);
	}
	
	 //配置RestTemplate交给spring管理
	 @Bean
	 //基于Ribbon的服务调用与负载均衡
	 @LoadBalanced
	 public RestTemplate getRestTemplate() {
	 	return new RestTemplate();
	 }
}
```

##### 服务提供者

```java
@RestController
@RequestMapping("/product")
public class ProductController {
	@Autowired
	private IProductService productService;
	@Value("${server.port}")
	private String port;
	@Value("${spring.cloud.client.ip-address}")
	private String ip;
	@GetMapping("/{id}")
	public Product findById(@PathVariable("id") Long id) {
		Product product=productService.findById(id);
		product.setProductName(product.getProductName()+"from ip: "+ip+":"+port);
		return product;
	}
}
```

##### 服务消费者

```java
	@GetMapping("/buy")
	public Order buy(String uid,String pid,Integer number) {
		//通过restTemplate调用商品微服务
	    //Product product=restTemplate.getForObject("http://127.0.0.1:8001/product/"+pid, Product.class);
		Product product=restTemplate.getForObject("http://shop-service-product/product/"+pid, Product.class);
		//User user=restTemplate.getForObject("http://127.0.0.1:8002/user/"+uid, User.class);
	    User user=restTemplate.getForObject("http://shop-service-user/user/"+uid, User.class);
	    Order order=null;
	    if(null!=product&&null!=user&&null!=number) {
	    	order=new Order();
	    	order.setAmount(number*product.getPrice());
	    	order.setNumber(number);
	    	order.setPrice(product.getPrice());
	    	order.setProductId(product.getId());
	    	order.setUserId(user.getId());
	    	orderService.insert(order);
	    	order=orderService.findById(order.getId());
	    }
	    System.out.println(product);
	    System.out.println(user);
	    System.out.println(order);
		return order;
	}
```

通过多次访问order可以发现product的端口每次基本不一样

#### 负载均衡策略

- com.netflix.loadbalancer.RoundRobinRule ：以轮询的方式进行负载均衡
- com.netflix.loadbalancer.RandomRule ：随机策略 
- com.netflix.loadbalancer.RetryRule ：重试策略
- com.netflix.loadbalancer.WeightedResponseTimeRule ：权重策略。会计算每个服务的权 重，越高的被调用的可能性越大
- com.netflix.loadbalancer.BestAvailableRule ：最佳策略。遍历所有的服务实例，过滤掉 故障实例，并返回请求数最小的实例返回
- com.netflix.loadbalancer.AvailabilityFilteringRule ：可用过滤策略。过滤掉故障和请 求数超过阈值的服务实例，再从剩下的实力中轮询调用

### Feign

#### 概述

使用Ribbon实现REST API调用，是使用拼接字符串的方式构造URL的，该URL只有一个参数。但是，在现实中，URL 中往往含有多个参数。这时候我们如果还用这种方式构造URL，那么就会非常痛苦。

##### Feign简介

Feign是Netflix开发的声明式，模板化的HTTP客户端，其灵感来自Retrofit,JAXRS-2.0以及WebSocket. Feign可帮助我们更加便捷，优雅的调用HTTP API。 在SpringCloud中，使用Feign非常简单——创建一个接口，并在接口上添加一些注解，代码就完 成了。 Feign支持多种注解，例如Feign自带的注解或者JAX-RS注解等。 SpringCloud对Feign进行了增强，使Feign支持了SpringMVC注解，并整合了Ribbon和Eureka， 从而让Feign的使用更加方便。

#### Eureka集成Feign

##### pom.xml

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

##### application.properties

```properties
# Ribbon
# 配置负载均衡策略 shop-service-product为需要调用的服务名称
shop-service-product.ribbon.NFLoadBalancerRuleClassName=com.netflix.loadbalancer.RandomRule

# Feign
# shop-service-product为定义FeginClient的名称
# 建立链接的超时时长,相当于Request.Options
feign.client.config.shop-service-product.connectTimeout=5000
# 读取超时时长,相当于Request.Options
feign.client.config.shop-service-product.readTimeout=5000
# 配置Feign的日志级别，相当于代码配置方式中的Logger
# NONE【性能最佳，适用于生产】：不记录任何日志（默认值）
# BASIC【适用于生产环境追踪问题】：仅记录请求方法、URL、响应状态代码以及执行时间
# HEADERS：记录BASIC级别的基础上，记录请求和响应的header。
# FULL【比较适用于开发及测试环境定位问题】：记录请求和响应的header、body和元数据。
feign.client.config.shop-service-product.loggerLevel=full
# Feign日志只会对日志级别为debug的做出响应
logging.level.com.demo.order.component.IProductFeginClient=debug
# Feign的错误解码器，相当于代码配置方式中的ErrorDecoder
# feign.client.config.shop-service-product.errorDecoder= com.example.SimpleErrorDecoder
# 配置重试，相当于代码配置方式中的Retryer
# feign.client.config.shop-service-product.retryer=com.example.SimpleRetryer
# 添加请求拦截器，相当于代码配置方式中的RequestInterceptor
#feign.client.config.shop-service-product.requestInterceptors=com.example.FooRequestInterceptor,com.example.BarRequestInterceptor
# 配置熔断不处理404异常
feign.client.config.shop-service-product.decode404=false
# 请求压缩
# 开启请求压缩
feign.compression.request.enabled=true
# 设置压缩的数据类型
feign.compression.request.mime-types=text/html,application/xml,application/json
# 设置触发压缩的大小下限
feign.compression.request.min-request-size=2048
# 开启响应压缩
feign.compression.response.enabled=true
```

##### 消费者启动类

通过在消费者的启动类@EnableFeignClients注解开启Spring Cloud Feign的支持功能

Feign中本身已经集成了Ribbon依赖和自动配置，因此我们不需要额外引入依赖，也不需要再注册 RestTemplate 对象

```java
@SpringBootApplication
@EnableFeignClients
public class ShopServiceOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopServiceOrderApplication.class, args);
	}
}
```

##### Feign接口

此接口是在Feign中调用微服务的核心接口 

确保该接口能被扫描进Spring容器中

在服务消费者 shop_service_order 添加一个 IProductFeginClient 接口

```java
//指定需要调用的微服务名称
@FeignClient(name="shop-service-product")
@Component
public interface IProductFeignClient {
	//调用的请求路径
	@GetMapping("/product/get/{id}")
	public Product findByIdWithGet(@PathVariable("id") Long id);
	@GetMapping("/product/find")
	public Product findByIdWithFind(@RequestParam("id") Long id);
}
```

在服务消费者 shop_service_order 添加一个 IUserFeginClient 接口

```java
@FeignClient(name="shop-service-user")
@Component
public interface IUserFeignClient {
	//调用的请求路径
	@GetMapping("/user/{id}")
	public User findById(@PathVariable("id") Long id);
}
```

- 定义各参数绑定时，@PathVariable、@RequestParam、@RequestHeader等可以指定参数属 性，
- @FeignClient：注解通过name指定需要调用的微服务的名称，用于创建Ribbon的负载均衡器。 所以Ribbon会把 shop-service-product 解析为注册中心的服务。

##### 服务消费者调用接口

添加IProductFeginClient 、IUserFeginClient 的自动注入，并在order方法中使用 IProductFeginClient 、IUserFeginClient 完成微服务调用。

```java
@RestController
@RequestMapping("/order")
public class OrderController {
	@Autowired
	private IOrderService orderService;
	@Autowired
	private IProductFeignClient productFeignClient;
	@Autowired
	private IUserFeignClient userFeignClient;

	@GetMapping("/{id}")
	public Order findById(@PathVariable("id") Long id) {
		return orderService.findById(id);
	}
	@GetMapping("/buy")
	public Order buy(long uid,Long pid,Integer number) {
		Product product=productFeignClient.findByIdWithFind(pid);
		User user=userFeignClient.findById(uid);
	    Order order=null;
	    if(null!=product&&null!=user&&null!=number) {
	    	order=new Order();
	    	order.setAmount(number*product.getPrice());
	    	order.setNumber(number);
	    	order.setPrice(product.getPrice());
	    	order.setProductId(product.getId());
	    	order.setUserId(user.getId());
	    	orderService.insert(order);
	    	order=orderService.findById(order.getId());
	    }
	    System.out.println(product);
	    System.out.println(user);
	    System.out.println(order);
		return order;
	}
}
```

通过多次访问order可以发现product的端口每次基本不一样

##### Feign和Ribbon的联系

 Ribbon是一个基于 HTTP 和 TCP 客户端 的负载均衡的工具。它可以 在客户端 配置 RibbonServerList（服务端列表），使用 HttpClient 或 RestTemplate 模拟http请求，步骤相当繁琐。

Feign 是在 Ribbon的基础上进行了一次改进，是一个使用起来更加方便的 HTTP 客户端。采用接口的 方式，只需要创建一个接口，然后在上面添加注解即可，将需要调用的其他服务的方法定义成抽象方 法即可，不需要自己构建http请求。然后就像是调用自身工程的方法调用，而感觉不到是调用远程方 法，使得编写客户端变得非常容易 。

##### 负载均衡

Feign中本身已经集成了Ribbon依赖和自动配置，因此我们不需要额外引入依赖，也不需要再注册 RestTemplate 对象。另外，我们可以像Ribbon一样去配置Ribbon，可以通过 ribbon.xx 来进 行全局配置。也可以通过 服务名.ribbon.xx 来对指定服务配置。



### OpenFeign

#### 导包

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    <version>3.1.6</version>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
    <version>3.1.7</version>
</dependency>
```



#### 开启 OpenFeign

```java
@SpringBootApplication
@EnableFeignClients
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```



#### Nacos 集成 OpenFeign

##### 调用服务

```java
@Component
// name 为服务名
@FeignClient(name = "Product", path = "/product/product")
public interface ProductFeignService {
    @GetMapping("/{id}")
    ProductVO findById(@PathVariable("id") Long id);
}
```



##### 配置日志

###### 局部配置

```yaml
feign:
  client:
    config:
      # NONE: 不记录任何日志，是OpenFeign默认日志级别（性能最佳，适用于生产环境）。
      # BASIC: 仅记录请求方法、URL、响应状态码、执行时间（适用于生产环境追踪问题）。
      # HEADERS: 在记录BASIC级别的基础上，记录请求和响应的header头部信息。
      # FULL: 记录请求响应的header、body 和 元数据（适用于开发和测试环境定位问题）。
      Product:
        loggerLevel: full
      Order:
        loggerLevel: full
logging:
  level:
    # Feign 调试日志是 debug 级别输出，SpringBoot 默认日志级别是 info，所以 Feign 的 debug 日志级别就不会输出
    com.example.order.ms: debug
```



###### 全局配置

对所有服务配置日志记录

```java
import feign.Logger;
import  org.springframework.context.annotation.Bean;
/**
 * 全局配置：加了 @Configuration 注解表示全局配置，对所有服务起作用
 * 局部配置：不加 @Configuration 注解表示局部配置，只针对指定的一个服务起作用
 */
public class FeignConfig {
    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }
}

/**
 * 局部配置
 */
@Component
@FeignClient(name = "Product", path = "/product/product" configuration = FeignConfig.class)
// name 为服务名
public interface ProductFeignService {
    @GetMapping("/{id}")
    ProductVO findById(@PathVariable("id") Long id);
}
```





## 服务熔断

### 概述

#### 雪崩效应

在微服务架构中，一个请求需要调用多个服务是非常常见的。如客户端访问A服务，而A服务需要调用B 服务，B服务需要调用C服务，由于网络原因或者自身的原因，如果B服务或者C服务不能及时响应，A服 务将处于阻塞状态，直到B服务C服务响应。此时若有大量的请求涌入，容器的线程资源会被消耗完毕， 导致服务瘫痪。服务与服务之间的依赖性，故障会传播，造成连锁反应，会对整个微服务系统造成灾难 性的严重后果，这就是服务故障的“雪崩”效应。

雪崩是系统中的蝴蝶效应导致其发生的原因多种多样，有不合理的容量设计，或者是高并发下某一个方 法响应变慢，亦或是某台机器的资源耗尽。从源头上我们无法完全杜绝雪崩源头的发生，但是雪崩的根 本原因来源于服务之间的强依赖，所以我们可以提前评估，做好熔断，隔离，限流。

#### 服务隔离 

顾名思义，它是指将系统按照一定的原则划分为若干个服务模块，各个模块之间相对独立，无强依赖。 当有故障发生时，能将问题和影响隔离在某个模块内部，而不扩散风险，不波及其它模块，不影响整体 的系统服务。 5.1.3 

#### 熔断降级

 熔断这一概念来源于电子工程中的断路器（Circuit Breaker）。在互联网系统中，当下游服务因访问压 力过大而响应变慢或失败，上游服务为了保护系统整体的可用性，可以暂时切断对下游服务的调用。这 种牺牲局部，保全整体的措施就叫做熔断。

所谓降级，就是当某个服务熔断之后，服务器将不再被调用，此时客户端可以自己准备一个本地的 fallback回调，返回一个缺省值。 也可以理解为兜底

#### 服务限流 

限流可以认为服务降级的一种，限流就是限制系统的输入和输出流量已达到保护系统的目的。一般来说 系统的吞吐量是可以被测算的，为了保证系统的稳固运行，一旦达到的需要限制的阈值，就需要限制流 量并采取少量措施以完成限制流量的目的。比方：推迟解决，拒绝解决，或者者部分拒绝解决等等。

#### Hystrix、Sentinel、Resilience4J

|                | Hystrix                 | Sentinel                                                    | Resilience4J                      |
| -------------- | ----------------------- | ----------------------------------------------------------- | --------------------------------- |
| 隔离策略       | 线程池隔离/信 号量隔离  | 信号量隔离（并发线程数限流）                                | 信号量隔离                        |
| 熔断降级策略   | 基于异常比率            | 基于响应时间、异常比率、异常数                              | 基于异常比率、响应 时间           |
| 实时统计实现   | 滑动窗口（基于 RxJava） | 滑动窗口（LeapArray）                                       | Ring Bit Buffer                   |
| 动态规则配置   | 支持多种数据源          | 支持多种数据源                                              | 有限支持                          |
| 扩展性         | 插件的形式              | 多个扩展点                                                  | 接口的形式                        |
| 基于注解的支持 | 支持                    | 支持                                                        | 支持                              |
| 限流           | 有限的支持              | 基于 QPS，支持基于调用关系的限流                            | Rate Limiter                      |
| 流量整形       | 不支持                  | 支持预热模式、匀速器模式、预热排队模式                      | 简单的 Rate Limiter 模式          |
| 系统自适应保护 | 不支持                  | 支持                                                        | 不支持                            |
| 控制台         | 简单的监控查 看         | 提供开箱即用的控制台，可配置规则、 查看秒级监控、机器发现等 | 不提供控制台，可对 接其它监控系统 |



### Hystrix

#### 简介

Hystrix是由Netflix开源的一个延迟和容错库，用于隔离访问远程系统、服务或者第三方库，防止级联失 败，从而提升系统的可用性与容错性。Hystrix主要通过以下几点实现延迟和容错。 

- 包裹请求：使用HystrixCommand包裹对依赖的调用逻辑，每个命令在独立线程中执行。这使用 了设计模式中的“命令模式”。 

- 跳闸机制：当某服务的错误率超过一定的阈值时，Hystrix可以自动或手动跳闸，停止请求该服务 一段时间。 

- 资源隔离：Hystrix为每个依赖都维护了一个小型的线程池（或者信号量）。如果该线程池已满， 发往该依赖的请求就被立即拒绝，而不是排队等待，从而加速失败判定。 

- 监控：Hystrix可以近乎实时地监控运行指标和配置的变化，例如成功、失败、超时、以及被拒绝 的请求等。 

- 回退机制：当请求失败、超时、被拒绝，或当断路器打开时，执行回退逻辑。回退逻辑由开发人员 自行提供，例如返回一个缺省值。 

- 自我修复：断路器打开一段时间后，会自动进入“半开”状态。

18年底Netflix官方宣布Hystrix 已经足够稳定，不再积极开发 Hystrix，该项目将处于维护模式。就目前 来看Hystrix是比较稳定的，并且Hystrix只是停止开发新的版本，并不是完全停止维护，Bug什么的依 然会维护的。因此短期内，Hystrix依然是继续使用的。

#### RestTemplate整合Hystrix

在服务消费者配置如下

##### pom.xml

```xml
	    <dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
		</dependency>
```

##### papplication.properties

在服务消费者中添加配置

```properties
# Hystrix
# 熔断器设置
# 触发熔断的最小请求次数，默认20
circuitBreaker.requestVolumeThreshold=5
# errorThresholdPercentage：触发熔断的失败请求最小占比，默认50%
circuitBreaker.sleepWindowInMilliseconds=10000
# sleepWindowInMilliseconds：熔断多少秒后去尝试请求
circuitBreaker.errorThresholdPercentage=50
# 配置隔离策略 
# 信号量隔离 
hystrix.command.default.execution.isolation.strategy=ExecutionIsolationStrategy.SEMAPHORE
# 线程池隔离
hystrix.command.default.execution.isolation.strategy=ExecutionIsolationStrategy.THREAD 
# 最大信号量上限
hystrix.command.default.execution.isolation.maxConcurrentRequests=20
```

##### 启动类

在服务消费者的启动类 OrderApplication 中添加 @EnableCircuitBreaker 注解开启对熔断器的支持。

类上的注解越来越多，在微服务中，经常会引入上面的三个注解，于是Spring就提供了 一个组合注解：@SpringCloudApplication

```java
//@SpringBootApplication
//@EnableCircuitBreaker
@SpringCloudApplication
public class ShopServiceOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopServiceOrderApplication.class, args);
	}
	 //配置RestTemplate交给spring管理
	 @Bean
	 //基于Ribbon的服务调用与负载均衡
	 @LoadBalanced
	 public RestTemplate getRestTemplate() {
	 	return new RestTemplate();
	 }
}

```

##### 配置服务消费者的熔断降级业务逻辑

使用注解@HystrixCommand的fallbackMethod属性，指定熔断触发的降级方法

使用注解@DefaultProperties配置加在类上同时使用@HystrixCommand注解在方法上，实现默认fallback

```java
@RestController
@RequestMapping("/order")
@DefaultProperties(defaultFallback="buyDefaultFallBack")//默认降级方法没有参数
public class OrderController {
	@Autowired
	private IOrderService orderService;
	@Autowired
	private RestTemplate restTemplate;

	@GetMapping("/buy")
	@HystrixCommand(fallbackMethod = "buyFallBack")
	public Order buy(Long uid,Long pid,Integer number) {
		Product product=restTemplate.getForObject("http://shop-service-product/product/get/"+pid, Product.class);
	    User user=restTemplate.getForObject("http://shop-service-user/user/"+uid, User.class);
	    Order order=null;
	    if(null!=product&&null!=user&&null!=number) {
	    	order=new Order();
	    	order.setAmount(number*product.getPrice());
	    	order.setNumber(number);
	    	order.setPrice(product.getPrice());
	    	order.setProductId(product.getId());
	    	order.setUserId(user.getId());
	    	orderService.insert(order);
	    	order=orderService.findById(order.getId());
	    }
	    System.out.println(product);
	    System.out.println(user);
	    System.out.println(order);
		return order;
	}
	
	@GetMapping("/buy/defalut")
	public Order buyDefault(Long uid,Long pid,Integer number) {
		Product product=restTemplate.getForObject("http://shop-service-product/product/get/"+pid, Product.class);
	    User user=restTemplate.getForObject("http://shop-service-user/user/"+uid, User.class);
	    Order order=null;
	    System.out.println(product);
	    System.out.println(user);
	    System.out.println(order);
		return order;
	}
	
	public Order buyFallBack(Long uid,Long pid,Integer number) {
		Order order=new Order();
		order.setAmount(0.0);
		order.setNumber(number);
		order.setPrice(0.0);
		order.setProductId(pid);
		order.setUserId(uid);
		order.setId(null);
		return order;
	}
	public Order buyDefaultFallBack() {
		Order order=new Order();
		order.setAmount(null);
		order.setNumber(null);
		order.setPrice(null);
		order.setProductId(null);
		order.setUserId(null);
		order.setId(null);
		return order;
	}

```

- 熔断的降级逻辑方法必须跟正常逻辑方法保证：相同的参数列表和返回值声明。 

- 在 buy方法上 HystrixCommand(fallbackMethod = "buyFallBack") 用来 声明一个降级逻辑的方法
- 默认降级方法没有参数



启动所有服务能正常访问，关闭所有product服务，触发降级方法



#### Feign整合Hystrix

##### pom.xml

SpringCloud Fegin默认已为Feign整合了hystrix，所以添加Feign依赖后就不用在添加hystrix的依赖了

##### application.properties

在服务消费者的配置中增加

```properties
# Hystrix
# 开启hystrix
feign.hystrix.enabled=true
# 熔断器设置
# 触发熔断的最小请求次数，默认20
circuitBreaker.requestVolumeThreshold=5
# errorThresholdPercentage：触发熔断的失败请求最小占比，默认50%
circuitBreaker.sleepWindowInMilliseconds=10000
# sleepWindowInMilliseconds：熔断多少秒后去尝试请求
circuitBreaker.errorThresholdPercentage=50
## 配置隔离策略 
## 信号量隔离 
#hystrix.command.default.execution.isolation.strategy=ExecutionIsolationStrategy.SEMAPHORE
## 线程池隔离
#hystrix.command.default.execution.isolation.strategy=ExecutionIsolationStrategy.THREAD 
## 最大信号量上限
#hystrix.command.default.execution.isolation.maxConcurrentRequests=20
```

##### 启动类

在服务消费者的启动类 OrderApplication 中添加 @EnableCircuitBreaker 注解开启对熔断器的支持。

类上的注解越来越多，在微服务中，经常会引入上面的三个注解，于是Spring就提供了 一个组合注解：@SpringCloudApplication

```java
//@SpringBootApplication
//@EnableCircuitBreaker
@SpringCloudApplication
@EnableFeignClients
public class ShopServiceOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopServiceOrderApplication.class, args);
	}
}


```

##### Feign接口配置Hystrix

在服务消费者的Feign接口的实现类中配置熔断方法

基于Feign实现熔断降级，降级方法需要配置到FeignClient接口的实现类中

- ProductFeignClient的实现类，在里面编写对应调用的降级并确保该类能被扫描进Spring容器

```java
@Component
public class ProductFeignClient implements IProductFeignClient{
	// 服务降级类
	public Product findByIdWithGet(Long id) {
		Product product=new Product();
		product.setProductName("服务降级");
        product.setPrice(0.0);
		return product;
	}
	// 服务降级类
	public Product findByIdWithFind(Long id) {
		Product product=new Product();
		product.setProductName("服务降级");
        product.setPrice(0.0);
		return product;
	}
}
```

- Feign接口ProductFeignClient中，去掉注解@Component，并在注解@FeignClient中添加fallback指定降级类

```java
@FeignClient(name="shop-service-product",fallback =ProductFeignClient.class)
public interface IProductFeignClient {
	//调用的请求路径
	@GetMapping("/product/get/{id}")
	public Product findByIdWithGet(@PathVariable("id") Long id);
	@GetMapping("/product/find")
	public Product findByIdWithFind(@RequestParam("id") Long id);
}
```

##### 服务调用

在product的服务中让线程休眠Thread.sleep(10000);后访问order微服务可以在控制台看见调用product时一被降级

```java
@RestController
@RequestMapping("/order")
public class OrderController {
	@Autowired
	private IOrderService orderService;
	@Autowired
	private IProductFeignClient productFeignClient;
	@Autowired
	private IUserFeignClient userFeignClient;

	@GetMapping("/buy")
	public Order buy(Long uid,Long pid,Integer number) {
		Product product=productFeignClient.findByIdWithFind(pid);
		User user=userFeignClient.findById(uid);
	    Order order=null;
	    if(null!=product&&null!=user&&null!=number&&null!=product.getId()&&null!=user.getId()) {
	    	order=new Order();
	    	order.setAmount(number*product.getPrice());
	    	order.setNumber(number);
	    	order.setPrice(product.getPrice());
	    	order.setProductId(product.getId());
	    	order.setUserId(user.getId());
	    	orderService.insert(order);
	    	order=orderService.findById(order.getId());
	    }
	    System.out.println(product);
	    System.out.println(user);
	    System.out.println(order);
		return order;
	}
}
```



#### 熔断器

被@HystrixCommand注解的方法

##### 熔断器的状态

熔断器有三个状态 CLOSED 、 OPEN 、 HALF_OPEN 熔断器默认关闭状态，当触发熔断后状态变更为 OPEN ,在等待到指定的时间，Hystrix会放请求检测服务是否开启，这期间熔断器会变为 HALF_OPEN 半 开启状态，熔断探测服务可用则继续变更为 CLOSED 关闭熔断器。

- Closed：关闭状态（断路器关闭），所有请求都正常访问。代理类维护了最近调用失败的次数， 如果某次调用失败，则使失败次数加1。如果最近失败次数超过了在给定时间内允许失败的阈值， 则代理类切换到断开(Open)状态。此时代理开启了一个超时时钟，当该时钟超过了该时间，则切 换到半断开（Half-Open）状态。该超时时间的设定是给了系统一次机会来修正导致调用失败的错 误。 
- Open：打开状态（断路器打开），所有请求都会被降级。Hystix会对请求情况计数，当一定时间 内失败请求百分比达到阈值，则触发熔断，断路器会完全关闭。默认失败比例的阈值是50%，请求 次数最少不低于20次。
- Half Open：半开状态，open状态不是永久的，打开后会进入休眠时间（默认是5S）。随后断路 器会自动进入半开状态。此时会释放1次请求通过，若这个请求是健康的，则会关闭断路器，否则 继续保持打开，再次进行5秒休眠计时。

```properties
# 触发熔断的最小请求次数，默认20
circuitBreaker.requestVolumeThreshold=5
# errorThresholdPercentage：触发熔断的失败请求最小占比，默认50%
circuitBreaker.sleepWindowInMilliseconds=10000
# sleepWindowInMilliseconds：熔断多少秒后去尝试请求
circuitBreaker.errorThresholdPercentage=50
```

为了能够精确控制请求的成功或失败，我们在 shop_service_product 的调用业务中加入一段逻辑：

```java
@GetMapping("/{id}")
public Product findById(@PathVariable Long id) {
	if(id !=1 ) {
		throw new RuntimeException("太忙了");
	}
	return productService.findById(id);
}
```

这样如果参数是id为1，一定失败，其它情况都成功。

 我们准备两个请求窗口：

 一个请求：http://localhost:8080/consumer/1，注定失败 

一个请求：http://localhost:8080/consumer/2，肯定成功

当我们疯狂访问id为1的请求时（超过10次），就会触发熔断。断路器会端口，一切请求都会被降级处 理。 此时你访问id为2的请求，会发现返回的也是失败，而且失败时间很短。

##### 熔断器的隔离策略

```properties
# 配置隔离策略 
# 信号量隔离 
hystrix.command.default.execution.isolation.strategy=ExecutionIsolationStrategy.SEMAPHORE
# 线程池隔离
hystrix.command.default.execution.isolation.strategy=ExecutionIsolationStrategy.THREAD 
# 最大信号量上限
hystrix.command.default.execution.isolation.maxConcurrentRequests=20
```



微服务使用Hystrix熔断器实现了服务的自动降级，让微服务具备自我保护的能力，提升了系统的稳定 性，也较好的解决雪崩效应。其使用方式目前支持两种策略：

- **线程池隔离策略**：使用一个线程池来存储当前的请求，线程池对请求作处理，设置任务返回处理超 时时间，堆积的请求堆积入线程池队列。这种方式需要为每个依赖的服务申请线程池，有一定的资 源消耗，好处是可以应对突发流量（流量洪峰来临时，处理不完可将数据存储到线程池队里慢慢处 理） 

- **信号量隔离策略**：使用一个原子计数器（或信号量）来记录当前有多少个线程在运行，请求来先判 断计数器的数值，若超过设置的最大线程个数则丢弃改类型的新请求，若不超过则执行计数操作请 求来计数器+1，请求返回计数器-1。这种方式是严格的控制线程且立即返回模式，无法应对突发 流量（流量洪峰来临时，处理的线程超过数量，其他的请求会直接返回，不继续去请求依赖的服 务） 线程池和型号量两种策略功能支持对比如下：



| 功能     | 线程池隔离             | 信号量隔离                |
| -------- | ---------------------- | ------------------------- |
| 线程     | 与调用线程非相同线程   | 与线程调用相同(Jetty线程) |
| 开销     | 排队、调度、上下文开销 | 无线程切换，开销低        |
| 异步     | 支持                   | 不支持                    |
| 并发支持 | 支持（最大线程池大小） | 支持（最大信号量上限）    |



##### Hystrix的监控平台

###### 搭建Hystrix DashBoard监控

Hystrix DashBoard可以显示每个熔断器（被@HystrixCommand注解的方法）的状态

pom.xml

```xml
		<dependency>
		    <groupId>org.springframework.boot</groupId>
		    <artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<dependency>
		    <groupId>org.springframework.cloud</groupId>
		    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
		</dependency>
		<dependency>
		    <groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
		</dependency>
```

application.properties

在配置文件增加端口暴露配置

```properties
# dashboard暴露端点
management.endpoints.web.exposure.include=*
```

启动类

在启动类使用@EnableHystrixDashboard注解激活仪表盘项目

```java
@SpringCloudApplication
@EnableFeignClients
@EnableHystrixDashboard
public class ShopServiceOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopServiceOrderApplication.class, args);
	}
}
```

测试

http://localhost:8000/hystrix后在输入框输入下面的链接

http://localhost:8000/actuator/hystrix.stream

不断访问order则可以在HystrixDashboard看见服务状态

###### 熔断器聚合监控Turbine

**需要被监控的微服务要按照HystrixDashboard配置好**

在微服务架构体系中，每个服务都需要配置Hystrix DashBoard监控。如果每次只能查看单个实例的监 控数据，就需要不断切换监控地址，这显然很不方便。要想看这个系统的Hystrix Dashboard数据就需 要用到Hystrix Turbine。Turbine是一个聚合Hystrix 监控数据的工具，他可以将所有相关微服务的 Hystrix 监控数据聚合到一起，方便使用。

新建一个微服务模块shop_server_hystrix_turbine用来监控多个Hystrix微服务

pom.xml

在HystrixDashboard的依赖上添加如下依赖

```xml
	    <dependency>
	        <groupId>org.springframework.cloud</groupId>
	        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
	    </dependency>		
		<dependency>
		    <groupId>org.springframework.cloud</groupId>
		    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
		</dependency>
		<dependency>
		    <groupId>org.springframework.boot</groupId>
		    <artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<dependency>
		    <groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-turbine</artifactId>
		</dependency>
```

application.properties

配置多个微服务的hystrix监控

在需要使用hystrix监控的微服务增加如下配置

```properties
spring.http.encoding.force=true
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
server.tomcat.uri-encoding=UTF-8
server.port=8004
spring.application.name=shop-service-hystrix-turbine

# eureka
# 将eureka集群的url配置到服务模块
eureka.client.serviceUrl.defaultZone=http://localhost:9000/eureka/,http://localhost:9002/eureka/
# 使用ip注册
eureka.instance.preferIpAddress=true
# 在Eureka Server的管控台中，显示的服务实例名称
eureka.instance.instanceId=${spring.cloud.client.ip-address}:${server.port}

# dashboard暴露端点
#management.endpoints.web.exposure.include=*,turbine.stream

# turbine
# 要监控的微服务列表，多个用,分隔
turbine.appConfig=shop-service-order
turbine.clusterNameExpression='default'
```

启动类

在启动类使用@EnableTurbine注解激活Turbine

测试

http://localhost:8004/hystrix后在输入框输入下面的链接

http://localhost:8004/turbine.stream

不断访问order则可以在HystrixDashboard看见服务shop-service-order的状态



### Resilience4J



### Sentinel

[Sentinal](https://spring-cloud-alibaba-group.github.io/zh-cn/docs/next/user-guide/sentinel/quick-start)

[Sentinel Wiki (github.com)](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel)

[Sentinel (sentinelguard.io)](https://sentinelguard.io/zh-cn/docs/dashboard.html)



#### 简介

Sentinel 是阿里巴巴开源的一款断路器实现，目前在Spring Cloud的孵化器项目Spring Cloud Alibaba 中的一员Sentinel本身在阿里内部已经被大规模采用，非常稳定。因此可以作为一个较好的替代品。

Sentinel 具有以下特征: 

- 丰富的应用场景：Sentinel 承接了阿里巴巴近 10 年的双十一大促流量的核心场景，例如秒杀（即 突发流量控制在系统容量可以承受的范围）、消息削峰填谷、集群流量控制、实时熔断下游不可用 应用等。 

- 完备的实时监控：Sentinel 同时提供实时的监控功能。您可以在控制台中看到接入应用的单台机器秒级数据，甚至 500 台以下规模的集群的汇总运行情况。 

- 广泛的开源生态：Sentinel 提供开箱即用的与其它开源框架/库的整合模块，例如与 Spring Cloud、Dubbo、gRPC 的整合。您只需要引入相应的依赖并进行简单的配置即可快速地接入 Sentinel。 

- 完善的 SPI 扩展点：Sentinel 提供简单易用、完善的 SPI  扩展接口。您可以通过实现扩展接口来快 速地定制逻辑。例如定制规则管理、适配动态数据源等。

##### 名词解释

Sentinel 可以简单的分为 Sentinel 核心库和 Dashboard。核心库不依赖 Dashboard，但是结合 Dashboard 可以取得最好的效果。 

使用 Sentinel 来进行熔断保护，主要分为几个步骤: 

1. 定义资源
2. 定义规则 
3. 检验规则是否生效 

**资源**：可以是任何东西，一个服务，服务里的方法，甚至是一段代码。

**规则**：Sentinel 支持以下几种规则：流量控制规则、熔断降级规则、系统保护规则、来源访问控制规则和热点参数规则。Sentinel 的所有规则都可以在内存态中动态地查询及修改，修改之后立即生效 先把可能需要保护的资源定义好，之后再配置规则。也可以理解为，只要有了资源，我们就可以在任何 时候灵活地定义各种流量控制规则。在编码的时候，只需要考虑这个代码是否需要保护，如果需要保护，就将之定义为一个资源。



#### 安装配置部署管理

示例版本 `1.8.6`

##### 安装

从[官方网站](https://github.com/alibaba/Sentinel/releases)中下载最新版本的控制台 jar 包

**启动 Sentinel 控制台需要 JDK 版本为 1.8 及以上版本。**



##### 配置

[更多启动配置项](https://sentinelguard.io/zh-cn/docs/general-configuration.html)

[配置](../files/config/sentinel.all.properties)

- Sentinel 提供 JVM -D 参数方式\properties 文件方式（1.7.0 版本开始支持）配置方式
- 优先级 JVM -D 参数 > properties 文件参数
- 其中，project.name 参数只能通过 JVM -D 参数方式配置（since 1.8.0 取消该限制），其它参数支持所有的配置方式。



JVM -D 参数方式

```properties
# 配置文件路径
-Dcsp.sentinel.config.file=/opt/apps/sentinal/conf/application.properties
# 指定控制台的端口为 8050
-Dserver.port=8050 
# 指定要被哪个控制台监控（这里指定的是自己监控自己）
-Dcsp.sentinel.dashboard.server=localhost:8050
# 客户端启动 HTTP API Server 的端口号。默认值：8719，若端口冲突会自动从8719开始依次+1扫描,直至找到未被占用的端口
# 端口配置会在应用对应的机器上启动一个 Http Server，该 Server 会与 Sentinel 控制台做交互。比如 Sentinel 控制台添加了一个限流规则，会把规则数据 push 给这个 Http Server 接收，Http Server 再将规则注册到 Sentinel 中。
-Dcsp.sentinel.api.port=8719
# 指定实例名称（名称会在控制台左侧以菜单显示）
-Dproject.name=sentinel-dashboard
# 设置登录的帐号为：sentinel，默认为 sentinel
-Dsentinel.dashboard.auth.username=sentinel
# 设置登录的密码为：sentinel，默认为 sentinel
-Dsentinel.dashboard.auth.password=sentinel
# Sentinel 日志文件目录。默认值：${user.home}/logs/csp/
-Dcsp.sentinel.log.dir=/var/log/sentinal
# 日志文件名中是否加入进程号，用于单机部署多个应用的情况。默认值：false
-Dcsp.sentinel.log.use.pid=false

# JVM 最小堆大小
-Xms1g
# JVM 最大堆大小
-Xmx1g
```



properties 文件方式

```properties
server.port=8050 
csp.sentinel.dashboard.server=localhost:8050
project.name=sentinel-dashboard
sentinel.dashboard.auth.username=sentinel
sentinel.dashboard.auth.password=sentinel
csp.sentinel.log.dir=/var/log/sentinal
csp.sentinel.log.use.pid=false
csp.sentinel.api.port=8719
```



##### 创建用户（组）

```shell
sudo groupadd -r apps
sudo useradd -r -g apps -s /bin/false sentinel

sudo mkdir /var/log/sentinel/
sudo chown -R sentinel:root /opt/apps/sentinel
sudo chown -R sentinel:root /var/log/sentinel
```



##### 服务管理

手动

```shell
# 启动
sudo -u sentinel nohup java -Dcsp.sentinel.config.file=/opt/apps/sentinel/conf/application.properties -jar /opt/apps/sentinel/sentinel-dashboard.jar > /var/log/sentinel/sentinel.nohup.log 2>&1 &

sudo -u sentinel nohup java -Dserver.port=8050 -Dcsp.sentinel.dashboard.server=localhost:8050 -Dproject.name=sentinel-dashboard -Dsentinel.dashboard.auth.username=sentinel -Dsentinel.dashboard.auth.password=sentinel -Dcsp.sentinel.log.dir=/var/log/sentinel -Dcsp.sentinel.log.use.pid=false -Dcsp.sentinel.api.port=8719 -Xms1g -Xmx1g -jar /opt/apps/sentinel/sentinel-dashboard.jar > /var/log/sentinel/sentinel.nohup.log 2>&1 &

# 停止
ps aux | grep 'sentinel'
sudo kill -9 PID
```



自动

编写[脚本](../files/scripts/server/sentinel.server)文件放在 base_dir 目录下 `/opt/apps/sentinel/sentinel.server`

配置参考 Linux -> 服务管理  -> 系统启动时自动运行服务



启动之后在浏览器访问 http://localhost:8050/ 进入 Sentinel 管理控制台



#### SpringCloud 整合 Sentinel



参考[鉴权模块文档](https://github.com/alibaba/Sentinel/wiki/%E6%8E%A7%E5%88%B6%E5%8F%B0#%E9%89%B4%E6%9D%83) 



##### 导包

父工程增加依赖

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-dependencies</artifactId>
            <version>2021.0.5.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

服务模块增加依赖

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```



##### 配置

```yaml
spring:
  cloud:
    sentinel:
      transport:
        # 配置 Sentinal dashboard 地址
        dashboard: 192.168.31.100:8050
        # 默认8719端口，假如被占用会自动从8719开始依次+1扫描,直至找到未被占用的端口
        # 端口配置会在应用对应的机器上启动一个 Http Server，该 Server 会与 Sentinel 控制台做交互。比如 Sentinel 控制台添加了一个限流规则，会把规则数据 push 给这个 Http Server 接收，Http Server 再将规则注册到 Sentinel 中。
        port: 8719
      # 默认为true，表示将调用链路收敛，会导致链路流控效果无效，需要改成false
      web-context-unify: false
```





##### @SentinelResource

[basic-api-resource-rule | Sentinel (sentinelguard.io)](https://sentinelguard.io/zh-cn/docs/basic-api-resource-rule.html)

`@SentinelResource` 用于定义资源，并提供可选的异常处理和 fallback 配置项。`@SentinelResource` 解包含以下属性：

注意：注解方式埋点不支持 private 方法。

| 属性                        | 作用                                                         | 是否必须  |
| --------------------------- | ------------------------------------------------------------ | --------- |
| `value`                     | 资源名称                                                     | 是        |
| `entryType`                 | entry 类型，标记流量的方向，取值 `EntryType.IN`/`EntryType.OUT`，默 认是 `EntryType.OUT` | 否        |
| `blockHandler`              | 处理 BlockException 的函数名称。函数要求： 1. 必须是 public 2.返回类型与原方法一致 3. 参数类型需要和原方法相匹配，并在最后加 BlockException 类型的参数。 4. 默认需和原方 法在同一个类中。若希望使用其他类的函数，可配置 blockHandlerClass ，并指定 blockHandlerClass 里面的方法。 | 否        |
| `blockHandlerClass`         | 存放 blockHandler 的类。对应的处理函数必须 static修饰，否则无法解析，其他要求：同 blockHandler。 | 否        |
| `fallback`                  | 用于在抛出异常的时候提供 fallback 处理逻辑。 fallback 函数可以针对所有类型的异常（除了 exceptionsToIgnore 里面排除掉的异常类型） 进行处理。函数要求： 1. 返回类型与原方法一致 2. 参数类型需要和原方法相匹配，Sentinel 1.6 开始，也可在方法最后加 Throwable 类型的参数。 3.默认需和原方法在同一个类中。若希望使 用其他类的函数，可配置 fallbackClass ，并指定 fallbackClass 里面的方法。 | 否        |
| `fallbackClass`【1.6】      | 存放 fallback 的类。对应的处理函数必须 static 修饰，否则无法解析，其他要求：同fallback。 | 否        |
| `defaultFallback`【1.6】    | 用于通用的 fallback 逻辑。默认 fallback 函数可 以针对所有类型的异常（除了 exceptionsToIgnore 里面排除掉的异常类型） 进行处理。若同时配置了 fallback 和 defaultFallback，以fallback为准。函数要求： 1. 返回类型与原方法一致 2. 方法参数列表为空， 或者有一个 Throwable 类型的参数。 3. 默认需 要和原方法在同一个类中。若希望使用其他类的函数，可配置 fallbackClass ，并指定 fallbackClass 里面的方法。 | 否        |
| `exceptionsToIgnore`【1.6】 | 指定排除掉哪些异常。排除的异常不会计入异常统计，也不会进入fallback逻辑，而是原样抛出。 | 否        |
| `exceptionsToTrace`         | 需要 trace 的异常                                            | Throwable |



###### 示例

```java
@RequestMapping("/sentinel")
@RestController
public class SentinelDemoController {
    /**
     * 单机阈值：5
     */
    @GetMapping("/sentinelResource")
    @SentinelResource(value = "sentinelResource", blockHandler = "sentinelResourceBlockHandler")
    public String sentinelResource(Integer id){
        return "sentinelResource,快速失败，id：" + id;
    }
    public String sentinelResourceBlockHandler(Integer id, BlockException blockException){
        return "sentinelResource,被限流，id：" + id;
    }


    /**
     * 单机阈值：5
     */
    @GetMapping("/sentinelResource2")
    @SentinelResource(value = "sentinelResource2", blockHandlerClass = SentinelResourceBlockHandlerClass.class,blockHandler = "sentinelResource2BlockHandler")
    public String sentinelResource2(Integer id){
        return "sentinelResource2,快速失败，id：" + id;
    }

}
```



```java
public class SentinelResourceBlockHandlerClass {

    public static String sentinelResource2BlockHandler(Integer id, BlockException blockException){
        return "sentinelResource2,被限流，id：" + id;
    }
}
```





##### RestTemplate 整合 Sentinel



###### 服务消费者配置降级方法

在需要被保护的方法上使用@SentinelResource注解进行熔断配置。与Hystrix不同的是，Sentinel对抛 出异常和熔断降级做了更加细致的区分，通过 blockHandler 指定熔断降级方法，通过 fallback 指定 触发异常执行的降级方法。

```java
@RestController
@RequestMapping("/order")
public class OrderController {
	@Autowired
	private IOrderService orderService;
	@Autowired
	private RestTemplate restTemplate;
	@GetMapping("/buy")
	@SentinelResource(value="buy",blockHandler = "buyBlockHandler",fallback="buyFallBack")
	public Order buy(String uid,String pid,Integer number) {
		//通过restTemplate调用商品微服务
		Product product=restTemplate.getForObject("http://shop-service-product/product/"+pid, Product.class);
	    User user=restTemplate.getForObject("http://shop-service-user/user/"+uid, User.class);
	    Order order=null;
	    if(null!=product&&null!=user&&null!=number&&null!=product.getId()&&null!=user.getId()) {
	    	order=new Order();
	    	order.setAmount(number*product.getPrice());
	    	order.setNumber(number);
	    	order.setPrice(product.getPrice());
	    	order.setProductId(product.getId());
	    	order.setUserId(user.getId());
	    	orderService.insert(order);
	    	order=orderService.findById(order.getId());
	    }
	    System.out.println(product);
	    System.out.println(user);
	    System.out.println(order);
		return order;
	}
	//blockHandler 指定熔断降级方法
	public Order buyBlockHandler(String uid,String pid,Integer number){
		Order order=new Order();
		order.setAmount(0.0);
		order.setId(0L);
		order.setNumber(0);
		order.setPrice(0.0);
		order.setProductId(0L);
		order.setUserId(0L);
		return order;
	}
	//fallback 指定触发异常执行的降级方法
	public Order buyFallBack(String uid,String pid,Integer number) {
		return new Order();
	}
}
```

*注：1.6.0 之前的版本 fallback 函数只针对降级异常（ DegradeException ）进行处理，不能针 对业务异常进行处理。*

特别地，若 blockHandler 和 fallback 都进行了配置，则被限流降级而抛出 BlockException 时只会 进入 blockHandler 处理逻辑。若未配置 blockHandler 、 fallback 和 defaultFallback ，则被 限流降级时会将 BlockException 直接抛出。

###### 启动类配置 Sentinel

```java
@SpringBootApplication
public class ShopServiceOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopServiceOrderApplication.class, args);
	}
	
	 //配置RestTemplate交给spring管理
	 @Bean
	 //基于Ribbon的服务调用与负载均衡
	 @LoadBalanced
	 @SentinelRestTemplate(
			 fallback = "handleFallback",fallbackClass=FallBackUtil.class,
			 blockHandler="handleBlock",blockHandlerClass=FallBackUtil.class)
	 public RestTemplate getRestTemplate() {
	 	return new RestTemplate();
	 }
}
```

- @SentinelRestTemplate 注解的属性支持限流( blockHandler , blockHandlerClass )和降级 ( fallback , fallbackClass )的处理。

- 其中 blockHandler 或 fallback 属性对应的方法必须是对应 blockHandlerClass 或 fallbackClass 属性中的静态方法。 

- 该方法的参数跟返回值跟 org.springframework.http.client.ClientHttpRequestInterceptor#interceptor 方法一 致，其中参数多出了一个 BlockException 参数用于获取 Sentinel 捕获的异常

```java
public class FallBackUtil {
	//限流熔断业务逻辑
	public static SentinelClientHttpResponse handleBlock(HttpRequest request,byte[] body,ClientHttpRequestExecution execution, BlockException ex) {
		System.err.println("Oops: " + ex.getClass().getCanonicalName());
		return new SentinelClientHttpResponse("限流熔断降级");
	}
	//异常熔断业务逻辑
	public static SentinelClientHttpResponse handleFallback(HttpRequest request, byte[] body,ClientHttpRequestExecution execution, BlockException ex) {
		System.err.println("fallback: " + ex.getClass().getCanonicalName());
		return new SentinelClientHttpResponse("异常熔断降级");
	}

}
```

Sentinel RestTemplate 限流的资源规则提供两种粒度： httpmethod:schema://host:port/path ：协议、主机、端口和路径 httpmethod:schema://host:port ：协议、主机和端口



##### OpenFeign 整合 Sentinel



###### application.properties

配置增加如下，开启 feign 对 sentinel 的支持

```properties
# 开启 feign 对 sentinel 的支持
feign.sentinel.enabled=true
```

###### Feign 接口配置 Sentinel

在服务消费者的 Feign 接口的实现类中配置熔断方法

基于 Feign 实现熔断降级，降级方法需要配置到 FeignClient 接口的实现类中

- IProductFeignClient 的实现类，在里面编写对应调用的降级并确保该类能被扫描进 Spring 容器


```java
// 实现 UserFeignService Feign 微服务调用接口 在接口实现类中编写熔断降级方法
@Component
public class UserFeignServiceFallback implements UserFeignService {
    @Override
    public UserVO findById(Long id) {
        System.out.println("UserFeignServiceFallback");
        return null;
    }
}
```

- Feign接口IProductFeignClient中，去掉注解@Component，并在注解@FeignClient中添加fallback指定降级类

```java
@Component
@FeignClient(name = "User", path = "/user/user", fallback = UserFeignServiceFallbackHandler.class)
public interface UserFeignService {
    @GetMapping("/{id}")
    UserVO findById(@PathVariable("id") Long id);
}
```

Feign 对应的接口中的资源名策略定义：httpmethod:protocol://requesturl

@FeignClient 注解中的所有属性，Sentinel 都做了兼容。 

UserFeignService 接口中方法 findById 对应的资源名  GET:http://User/user/user/{id}



##### Endpoint 支持

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

在使用 Endpoint 特性之前需要在 Maven 中添加 `spring-boot-starter-actuator` 依赖，并在配置中允许 Endpoints 的访问。

- Spring Boot 1.x 中添加配置 `management.security.enabled=false`。暴露的 endpoint 路径为 `/sentinel`
- Spring Boot 2.x 中添加配置 `management.endpoints.web.exposure.include=*`。暴露的 endpoint 路径为 `/actuator/sentinel`

Sentinel Endpoint 里暴露的信息非常有用。包括当前应用的所有规则信息、日志目录、当前实例的 IP，Sentinel Dashboard 地址，Block Page，应用与 Sentinel Dashboard 的心跳频率等等信息。



#### 使用

##### 实时监控



##### 簇点链路



##### 流控规则

[流量控制](https://github.com/alibaba/Sentinel/wiki/流量控制)

[流量控制](https://sentinelguard.io/zh-cn/docs/flow-control.html)



- 资源名：唯一名称，默认请求路径
- 针对来源：Sentinel 可以针对调用者进行限流，填写微服务名，默认default（不区分来源）
- 阈值类型/单机阈值：
	- QPS（每秒钟的请求数量）：当调用该 api 的 QPS 达到阈值的时候，进行限流
	- 线程数：当调用该 api 的线程数达到阈值的时候，进行限流
- 是否集群：不需要集群
- 流控模式：
	- 直接：api 达到限流条件时，直接限流
	- 关联：当关联的资源达到阈值时，就限流自己
	- 链路：只记录指定链路上的流量（指定资源从入口进来的流量，如果达到阈值，就进行限流）【api 级别的针对来源】
- 流控效果：
	- 快速失败：直接失败，抛异常 `com.alibaba.csp.sentinel.slots.block.flow.controller.DefaultController`
	- Warm Up：根据 codeFactor（冷加载因子，默认3）的值，QPS 从 `阈值 / codeFactor` 开始，经设定的预热时长后逐渐以每秒 `阈值 / codeFactor` 的增量升至设定的 QPS 值 `com.alibaba.csp.sentinel.slots.block.flow.controller.WarmUpController`

	- 排队等待：匀速排队，让请求以匀速的速度通过，阈值类型必须设置为 QPS，否则无效 `com.alibaba.csp.sentinel.slots.block.flow.controller.RateLimiterController`



##### 熔断规则

[熔断降级](https://github.com/alibaba/Sentinel/wiki/熔断降级)

[熔断降级](https://sentinelguard.io/zh-cn/docs/circuit-breaking.html)



Sentinel 熔断降级会在调用链路中某个资源出现不稳定状态时（例如调用超时或异常比例升高），对这个资源的调用进行限制，让请求快速失败，避免影响到其它的资源而导致级联错误。
当资源被降级后，在接下来的降级时间窗口之内，对该资源的调用都自动熔断（默认行为是抛出 DegradeException）

- 资源名：唯一名称，默认请求路径
- 熔断策略：

  - 平均响应时间 （DEGRADE_GRADE_RT）：当 1s 内持续进入 5 个请求，对应时刻的平均响应时间（秒级）均超过阈值（count ，以 ms 为单位），那么在接下的时间窗口（DegradeRule 中的 timeWindow，以 s 为单位）之内，对这个方法的调用都会自动地熔断（抛出 DegradeException）。注意 Sentinel 默认统计的 RT 上限是 4900 ms，超出此值的都会算作 4900ms，若需要变更此上限可以通过启动配置项 `-Dcsp.sentinel.statistic.max.rt=xxx` 来配置。
  - 异常比例（DEGRADE_GRADE_EXCEPTION_RATIO）：当资源的每秒请求量 >= 5，并且每秒异常总数占通过量的比值超过阈值中的（DegradeRule 中的 count）之后，资源进入降级状态，即在接下的时间窗口（DegradeRule 中的 timeWindow，以 s 为单位）之内，对这个方法的调用都会自动地返回。异常比率的阈值范围是[0.0, 1.0]，代表 0% - 100%。
  - 异常数（DEGRADE_GRADE_EXCEPTION_COUNT）：当资源近 1 分钟的异常数目超过阈值之后会进行熔断。注意由于统计时间窗口是分钟级别的，若 timeWindow 小于 60s，则结束熔断状态后仍可能再进入熔断状态。
- 统计时长：
- 最小请求数：

- 熔断时长：

- 最大 RT：
- 比例阈值：
- 异常数：



##### 热点规则

[热点参数限流](https://github.com/alibaba/Sentinel/wiki/热点参数限流)

[热点参数限流](https://sentinelguard.io/zh-cn/docs/parameter-flow-control.html?spm=sentinel-github.wiki.0.0.0)



何为热点? 热点即经常访问的数据。很多时候我们希望统计某个热点数据中访问频次最高的 Top K 数据，并对其访问进行限制。比如:

- 商品 ID 为参数，统计一段时间内最常购买的商品 ID 并进行限制
- 用户 ID 为参数，针对一段时间内频繁访问的用户 ID 进行限制

热点参数限流会统计传入参数中的热点参数，并根据配置的限流阈值与模式，对包含热点参数的资源调用进行限流。热点参数限流可以看

做是一种特殊的流量控制，仅对包含热点参数的资源调用生效。

Sentinel 利用 LRU 策略统计最近最常访问的热点参数，结合令牌桶算法来进行参数级别的流控。热点参数限流支持集群模式。



###### 导包

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-parameter-flow-control</artifactId>
</dependency>
```



###### 在需要热点限流的方法添加 @SentinelResource 注解



###### 新增热点限流规则





##### 系统规则

[系统自适应限流](https://github.com/alibaba/Sentinel/wiki/系统自适应限流)

[系统自适应限流](https://sentinelguard.io/zh-cn/docs/system-adaptive-protection.html?spm=sentinel-github.wiki.0.0.0)



##### 授权规则

##### 集群流控

##### 机器列表



##### 规则持久化

**注意：规则配置文件添加注释可能会导致规则不生效**

###### 导包

```xml
<!-- sentinel 规则持久化 -->
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-nacos</artifactId>
</dependency>
```



###### 应用配置

```yaml
spring:
  cloud:
    sentinel:
      # 添加Nacos数据源配置
      datasource:
        # 流控规则
        flow:
          nacos:
            server-addr: 192.168.31.101:8848
            namespace: ed2069f3-cc54-4a62-b044-95061643ae84
            data-id: ${spring.application.name}-flow-rules
            group-id: MY_SENTINEL_GROUP
            # 还可以是：degrade、authority、param-flow
            rule-type: flow
            data-type: json
            username: app
            password: Senna&nacos-app@P1
        # 降级规则
        degrade:
          nacos:
            server-addr: 192.168.31.101:8848
            namespace: ed2069f3-cc54-4a62-b044-95061643ae84
            data-id: ${spring.application.name}-degrade-rules
            group-id: MY_SENTINEL_GROUP
            rule-type: degrade
            data-type: json
            username: app
            password: Senna&nacos-app@P1
        # 系统规则
        system:
          nacos:
            server-addr: 192.168.31.101:8848
            namespace: ed2069f3-cc54-4a62-b044-95061643ae84
            data-id: ${spring.application.name}-system-rules
            group-id: MY_SENTINEL_GROUP
            rule-type: system
            data-type: json
            username: app
            password: Senna&nacos-app@P1
        # 授权规则
        authority:
          nacos:
            server-addr: 192.168.31.101:8848
            namespace: ed2069f3-cc54-4a62-b044-95061643ae84
            data-id: ${spring.application.name}-authority-rules
            group-id: MY_SENTINEL_GROUP
            rule-type: authority
            data-type: json
            username: app
            password: Senna&nacos-app@P1
        # 热点规则
        param-flow:
          nacos:
            server-addr: 192.168.31.101:8848
            namespace: ed2069f3-cc54-4a62-b044-95061643ae84
            data-id: ${spring.application.name}-param-flow-rules
            group-id: MY_SENTINEL_GROUP
            rule-type: param-flow
            data-type: json
            username: app
            password: Senna&nacos-app@P1
```



###### 流控规则配置

[Order-flow-rules](../files/config/Sentinel/SpringCloudAlibaba/Order-flow-rules.json)

```json
[
  {
    // 资源名
    "resource":"/sentinel/flow/fast",
    // 针对来源，若为 default 则不区分调用来源
    "limitApp":"default",
    // 限流阈值类型(1:QPS;0:并发线程数）
    "grade":1,
    // 阈值
    "count":5,
    // 是否是集群模式
    "clusterMode":false,
    // 流控模式(0:直接；1:关联;2:链路)
    "strategy":0,
    // 流控效果(0:快速失败;1:Warm Up(预热模式);2:排队等待)
    "controlBehavior":0
    // 预热时长（秒，预热模式需要此参数）
    // "warmUpPeriodSec":10,
    // 超时时间（排队等待模式需要此参数）
    // "maxQueueingTimeMs":500,
    // 关联资源、入口资源(关联、链路模式)
    // "refResource":"rrr"
  }
]
```



###### 降级规则配置

[Order-degrade-rules](../files/config/Sentinel/SpringCloudAlibaba/Order-degrade-rules.json)

```json
[
    {
      // 资源名
      "resource": "/sentinel/meltdown/rt",
      "limitApp": "default",
      // 熔断策略（0:慢调用比例，1:异常比率，2:异常计数）
      "grade": 0,
      // 最大RT、比例阈值、异常数
      "count": 500,
      // 慢调用比例阈值，仅慢调用比例模式有效（1.8.0 引入）
      "slowRatioThreshold": 0.5,
      // 最小请求数
      "minRequestAmount": 5,
      // 当单位统计时长(类中默认1000)
      "statIntervalMs": 1000,
      // 熔断时长
      "timeWindow": 1000
    }
]
```



###### 系统规则配置

[Order-system-rules](../files/config/Sentinel/SpringCloudAlibaba/Order-system-rules.json)

```json
[
    {
      // RT
      "avgRt": 1,
      // CPU 使用率
      "highestCpuUsage": -1,
      // LOAD
      "highestSystemLoad": -1,
      // 线程数
      "maxThread": -1,
      // 入口 QPS
      "qps": -1
    }
  ]
  
```



###### 授权规则配置

[Order-authority-rules](../files/config/Sentinel/SpringCloudAlibaba/Order-authority-rules.json)

```json
[
    {
      // 资源名
      "resource": "sentinel_spring_web_context",
      // 流控应用
      "limitApp": "/test",
      // 授权类型(0代表白名单；1代表黑名单。)
      "strategy": 0
    }
  ]
  
```



###### 热点规则配置

[Order-param-flow-rules](../files/config/Sentinel/SpringCloudAlibaba/Order-param-flow-rules.json)

```json
[
    {
      // 资源名
      "resource": "sentinelHotKey",
      // 限流模式（QPS 模式，不可更改）
      "grade": 1,
      // 参数索引
      "paramIdx": 0,
      // 单机阈值
      "count": 1,
      // 统计窗口时长（秒）
      "durationInSec": 1,
      // 是否集群 默认false
      "clusterMode": false,
      // 均摊阈值/集群阈值
      "burstCount": 0,
      // 集群模式配置
      "clusterConfig": {
        // 
        "fallbackToLocalWhenFail": true,
        // 
        "flowId": 2,
        // 
        "sampleCount": 10,
        // 
        "thresholdType": 0,
        // 
        "windowIntervalMs": 1000
      },
      // 流控效果（支持快速失败和匀速排队模式）
      "controlBehavior": 0,
      // 
      "limitApp": "default",
      // 
      "maxQueueingTimeMs": 0,
      // 高级选项
      "paramFlowItemList": [
        {
           // 参数类型
          "classType": "int",
           // 参数值
          "object": "1",
           // 限流阈值
          "count": 2
        }
      ]
    }
  ]
```





## 微服务网关

### 概述

不同的微服务一般会有不同的网络地址，客户端在访问这些微服务时必须记住几十甚至几百个地址，这对于客户端方来说太复杂也难以维护。

如果让客户端直接与各个微服务通讯，可能会有很多问题： 

- 客户端会请求多个不同的服务，需要维护不同的请求地址，增加开发难度 

- 在某些场景下存在跨域请求的问题 

- 加大身份认证的难度，每个微服务需要独立认证 

因此，我们需要一个微服务网关，介于客户端与服务器之间的中间层，所有的外部请求都会先经过微服 务网关。客户端只需要与网关交互，只知道一个网关地址即可，这样简化了开发还有以下优点： 

1、易于监控 

2、易于认证

3、减少了客户端与各个微服务之间的交互次数

#### 概念

##### 什么是微服务网关 

API网关是一个服务器，是系统对外的唯一入口。API网关封装了系统内部架构，为每个客户端提供 一个定制的API。API网关方式的核心要点是，所有的客户端和消费端都通过统一的网关接入微服务，在 网关层处理所有的非业务功能。通常，网关也是提供REST/HTTP的访问API。服务端通过API-GW注册和 管理服务。

##### 作用和应用场景 

网关具有的职责，如身份验证、监控、负载均衡、缓存、请求分片与管理、静态响应处理。当然，最主 要的职责还是与“外界联系”。

##### 常见的API网关实现方式 

Kong 

基于Nginx+Lua开发，性能高，稳定，有多个可用的插件(限流、鉴权等等)可以开箱即用。 问题：只支持Http协议；二次开发，自由扩展困难；提供管理API，缺乏更易用的管控、配置方 式。 

Zuul 

Netflix开源，功能丰富，使用JAVA开发，易于二次开发；需要运行在web容器中，如Tomcat。 问题：缺乏管控，无法动态配置；依赖组件较多；处理Http请求依赖的是Web容器，性能不如 Nginx； 

Traefik 

Go语言开发；轻量易用；提供大多数的功能：服务路由，负载均衡等等；提供WebUI 问题：二进制文件部署，二次开发难度大；UI更多的是监控，缺乏配置、管理能力； 

Spring Cloud Gateway 

SpringCloud提供的网关服务 

Nginx+lua实现 

使用Nginx的反向代理和负载均衡可实现对api服务器的负载均衡及高可用 问题：自注册的问题和网关本身的扩展性

### Zuul(1.x)

https://github.com/Netflix/zuul/wiki

#### 简介

ZUUL是Netflix开源的微服务网关，它可以和Eureka、Ribbon、Hystrix等组件配合使用，Zuul组件的核心是一系列的过滤器，这些过滤器可以完成以下功能： 动态路由：动态将请求路由到不同后端集群 压力测试：逐渐增加指向集群的流量，以了解性能负载分配：为每一种负载类型分配对应容量，并弃用超出限定值的请求静态响应处理：边缘位置进行响应，避免转发到内部集群身份认证和安全: 识别每一个资源的验证要求，并拒绝那些不符的请求。Spring Cloud对Zuul进行 了整合和增强。

**存在的问题**

性能问题 

Zuul1x版本本质上就是一个同步Servlet，采用多线程阻塞模型进行请求转发。简单讲，每来 一个请求，Servlet容器要为该请求分配一个线程专门负责处理这个请求，直到响应返回客户 端这个线程才会被释放返回容器线程池。如果后台服务调用比较耗时，那么这个线程就会被阻塞，阻塞期间线程资源被占用，不能干其它事情。我们知道Servlet容器线程池的大小是有限制的，当前端请求量大，而后台慢服务比较多时，很容易耗尽容器线程池内的线程，造成容器无法接受新的请求。 

不支持任何长连接，如websocket

**Zuul网关的替换方案**

- Zuul2.x版本 
- SpringCloud Gateway

#### 搭建Zuul网关服务器

创建一个SpringBoot子模块shop_server_zuul

##### pom.xml

```xml
  <dependencies>
  		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-zuul</artifactId>
		</dependency>
  </dependencies>
```

##### application.properties

```properties
spring.http.encoding.force=true
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
server.tomcat.uri-encoding=UTF-8
server.port=9100
spring.application.name=api-gateway-zuul

# zuul
# order-service 这里是路由id,可以随意取名
# 这里是映射路径,这里将所有请求前缀为/order-service/的请求，转发到http://127.0.0.1:8000
zuul.routes.order-service.path=/order-service/**
# 映射路径对应的实际url地址
zuul.routes.order-service.url=http://127.0.0.1:8000
# 默认zuul会屏蔽cookie，cookie不会传到下游服务，这里设置为空则取消默认的黑名单，如果设置了具体的头信息则不会传到下游服务
zuul.routes.order-service.sensitiveHeaders=
```

##### 配置启动类

```java
@SpringCloudApplication
@EnableZuulProxy // 开启Zuul的网关功能
public class ShopServerZuulApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopServerZuulApplication.class, args);
	}
}
```

配置好Zuul路由之后启动服务，在浏览器中输入http://localhost:9100/order-service/order/buy?uid=1&pid=2&number=3，即可访问到订单微服务

#### 动态路由(Eureka)

微服务一般是由几十、上百个服务组成，对于一个URL请求，最终会确认一个服务实例进行处理。如果 对每个服务实例手动指定一个唯一访问地址，然后根据URL去手动实现请求匹配，这样做显然就不合理。 

Zuul支持与Eureka整合开发，根据ServiceID自动的从注册中心中获取服务地址并转发请求，这样做的 好处不仅可以通过单个端点来访问应用的所有服务，而且在添加或移除服务实例的时候不用修改Zuul的路由配置。

##### pom.xml

增加以下依赖

```xml
	    <dependency>
	        <groupId>org.springframework.cloud</groupId>
	        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
	    </dependency>
```

##### application.properties

zuul配置修改为如下并增加Eureka配置

```properties
# zuul
## order-service 这里是路由id,可以随意取名
## 这里是映射路径,这里将所有请求前缀为/order-service/的请求，转发到http://127.0.0.1:8000
#zuul.routes.order-service.path=/order-service/**

# 路由配置方法1
## 映射路径对应的实际url地址
## 加入Eureka之前的写法
##zuul.routes.order-service.url=http://127.0.0.1:8000
## 加入Eureka之后的写法, 指定需要转发的微服务实例名称
#zuul.routes.order-service.serviceId=shop-service-order

## 默认zuul会屏蔽cookie，cookie不会传到下游服务，这里设置为空则取消默认的黑名单，如果设置了具体的头信息则不会传到下游服务
#zuul.routes.order-service.sensitiveHeaders=

# 路由配置方法2
## 简化写法 zuul.routes.<serviceId>=<path>
#zuul.routes.shop-service-order=/order-service/**
#zuul.routes.shop-service-order.sensitiveHeaders=

# 路由配置方法3
# 默认路由配置,默认情况下，一切服务的映射路径就是服务名本身
zuul.routes.shop-service-order.sensitiveHeaders=

# eureka
# 将eureka集群的url配置到服务模块
eureka.client.serviceUrl.defaultZone=http://localhost:9000/eureka/
# 使用ip注册
eureka.instance.preferIpAddress=true
# 在Eureka Server的管控台中，显示的服务实例名称
eureka.instance.instanceId=${spring.cloud.client.ip-address}:${server.port}
# 指定本服务请求ip,不指定时为本机ip
eureka.instance.ipAddress=127.0.0.1
```

##### 启动类

较新版本SpringCloud可以不用加@EnableDiscoveryClien，详情请看Eureka

```java
@SpringCloudApplication
//@EnableDiscoveryClient
@EnableZuulProxy // 开启Zuul的网关功能
public class ShopServerZuulApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopServerZuulApplication.class, args);
	}
}
```

配置好Zuul路由之后启动服务，在浏览器中输入http://localhost:9100/order-service/order/buy?uid=1&pid=2&number=3，即可访问到订单微服务

默认路由

http://localhost:9100/shop-service-order/order/buy?uid=1&pid=2&number=3



####  ZuulFilter

##### 类型

Zuul 中的过滤器跟我们之前使用的 javax.servlet.Filter 不一样，javax.servlet.Filter 只有一种类型，可以通过配置 urlPatterns 来拦截对应的请求。而 Zuul 中的过滤器总共有 4 种类型，且每种类型都有对应的使用场景。

- PRE：这种过滤器在请求被路由之前调用。我们可利用这种过滤器实现身份验证、在集群中选择请求的微服务、记录调试信息等。 

- ROUTING：这种过滤器将请求路由到微服务。这种过滤器用于构建发送给微服务的请求，并使用 Apache HttpClient或Netfilx Ribbon请求微服务。 


- POST：这种过滤器在路由到微服务以后执行。这种过滤器可用来为响应添加标准的HTTP Header、收集统计信息和指标、将响应从微服务发送给客户端等。 


- ERROR：在其他阶段发生错误时执行该过滤器。 



##### 生命周期





​			|-------------------------------------------------------------------------------------<<----response----<<-------------|

HTTP Request 		|------------------------------------------------------------------------------------------------------|	  |

​			|-request->-|---------> "pre" filters -------------> "routing" filters -------------> "post" filters -|--> |

​								  |------------------------------------------------|-----------|-----------------------------------|---|	  |

​																								|	  		|								   |	  |

​																						      Origin Server			"error" filters ---|

- 正常流程： 

  请求到达首先会经过pre类型过滤器，而后到达routing类型，进行路由，请求就到达真正的 服务提供者，执行请求，返回结果后，会到达post过滤器。而后返回响应。 

- 异常流程： 

  整个过程中，pre或者routing过滤器出现异常，都会直接进入error过滤器，再error处理完毕 后，会将请求交给POST过滤器，最后返回给用户。 

  如果是error过滤器自己出现异常，最终也会进入POST过滤器，而后返回。 

  如果是POST过滤器出现异常，会跳转到error过滤器，但是与pre和routing不同的时，请求 不会再到达POST过滤器了。 

- 不同过滤器的场景： 

  请求鉴权：一般放在pre类型，如果发现没有访问权限，直接就拦截了 

  异常处理：一般会在error类型和post类型过滤器中结合来处理。 

  服务调用时长统计：pre和post结合使用。

##### 例子

Zuul提供了自定义过滤器的功能实现起来也十分简单，只需要编写一个类去实现zuul提供的接口

```java
@Component
public class ZuulFilterImpl extends ZuulFilter{

	public boolean shouldFilter() {
		return true;
	}
	public Object run() throws ZuulException {
		System.out.println("登录校验逻辑");
		// 1）获取Zuul提供的请求上下文对象
		RequestContext ctx = RequestContext.getCurrentContext();
		// 2) 从上下文中获取request对象
		HttpServletRequest req = ctx.getRequest();
		// 3) 从请求中获取token
		String token = req.getParameter("token");
		// 4) 判断
		if(token == null || "".equals(token.trim())){
            // 没有token，登录校验失败，拦截
            ctx.setSendZuulResponse(false);
            // 返回401状态码。也可以考虑重定向到登录页。
            ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
		}
		// 校验通过，可以考虑把用户信息放入上下文，继续向后执行
		return null;
	}
	public String filterType() {
		return "pre";
	}
	public int filterOrder() {
		return 1;
	}
}
```

- ZuulFilter是过滤器的顶级父类。在这里我们看一下其中定义的4个最重要的方法 

- shouldFilter ：返回一个 Boolean 值，判断该过滤器是否需要执行。返回true执行，返回false 不执行。 

- run ：过滤器的具体业务逻辑。 

- filterType ：返回字符串，代表过滤器的类型。包含以下4种： pre ：请求在被路由之前执行 routing ：在路由请求时调用 post ：在routing和errror过滤器之后调用 error ：处理请求时发生错误调用 

- filterOrder ：通过返回的int值来定义过滤器的执行顺序，数字越小优先级越高。
- RequestContext：用于在过滤器之间传递消息。它的数据保存在每个请求的ThreadLocal中。它 用于存储请求路由到哪里、错误、HttpServletRequest、HttpServletResponse都存储在 RequestContext中。RequestContext扩展了ConcurrentHashMap，所以，任何数据都可以存储 在上下文中



### SpringCloud Gateway

#### 简介

| 组件                 | RPS(request per second) |
| -------------------- | ----------------------- |
| Spring Cloud Gateway | Requests/sec: 32213.38  |
| Zuul1X               | Requests/sec: 20800.13  |



##### Spring Cloud Gateway 是什么

https://cloud.spring.io/spring-cloud-static/spring-cloud-gateway/2.2.1.RELEASE/reference/html/

​		Spring Cloud Gateway 是 Spring Cloud 的一个全新项目,基于 Spring 5.0+Spring Boot 2.0 和 Project Reactor 等技术开发的网关，它旨在为微服务架构提供一种简单有效的统一的 API 路由管理方式。

​		Sprina Cloud Gateway 作为 Spring Cloud 生态系统中的网关，目标是替代 Zuul，在 Spring Cloud 2.0 以上版本中，没有对新版本的 Zuul 2.0 以上最新高性能版本进行集成，仍然还是使用的 Zuul 1.x 非 Reactor 模式的老版本，而为了提升网关的性能，Spring Cloud Gateway 是基于 WebFlux 框架实现的，而 WebFlux 框架底层则使用了高性能的 Reactor 模式通信框架 Netty。

​		Spring Cloud Gateway 的目标提供统一的路由方式且基于 Filter 链的方式提供了网关基本的功能，例如: 安全，监控/指标，和限流。



##### Spring Cloud Gateway 具有如下特性

- 基于 Spring Framework 5, Project Reactor 和 Spring Boot 2.0 进行构建动态路由
- 能够匹配任何请求属性
- 可以对路由指定 Predicate (断言) 和 Filter (过滤器)
- 集成 Hystrix 的断路器功能
- 集成 Spring Cloud 服务发现功能
- 易于编写的 Predicate (断言) 和 Filter (过滤器)
- 请求限流功能
- 支持路径重写



##### Spring Cloud Gateway 与 Zuul 的区别

在SpringCloud Finchley 正式版之前，Spring Cloud 推荐的网关是 Netflix 提供的Zuul

Zuul 1.x 是一个基于阻塞 I/ O的 API Gateway

Zuul 1.x 基于 Servlet 2.5 使用阻塞架构它不支持任何长连接(如 WebSocket) Zuul 的设计模和 Nginx 较像，每次 I/O 操作都是从工作线程中选择一个执行，请求线程被阻塞到工作线程完成，但是差别是 Nginx 用 C++ 实现，Zuul 用 Java 实现，而 JVM 本身会有第一次加载较慢的情况，使得 Zuul 的性能相对较差

Zuul 2.x 理念先进，想基于 Netty 非阻塞和支持长连接，Spring Cloud 目前还没有整合。Zuul 2.x 的性能较 Zuul 1.x 有较大提升。在性能方面，根据官方提供的基准测试，Spring Cloud Gateway 的 RPS (每秒请求数) 是 Zuul 的 1.6 倍

Spring Cloud Gateway 建立在 Spring Framework 5、Project Reactor 和 Spring Boot 2 之上，使用非阳塞API

Spring Cloud Gateway 还支持 WebSocket，并且与 Spring 紧密集成拥有更好的开发体验



##### WebFlux 是什么

https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactor/html



传统的 Web 框架，比如说 Struts2，Spring MVC 等都是基于 Servlet API 与 Servlet 容器基础之上运行的

但是在 Servlet3.1 之后有了异步非阻塞的支持。而 WebFlux 是一个典型异步非阻塞的框架，它的核心是基于 Reactor 的相关 API 实现的。相对于传统的 web 框架来说，它可以运行在诸如 Netty，Undertow 及支持 Servlet3.1 的容器上，非阻塞式+函数式编程(Spring5 必须让你使用 Java8)

Spring WebFlux 是 Spring 5.0 引入的新的响应式框架，区别于 Spring MVC，它不需要依赖 Servlet APl，它是完全异步非阻塞的，并且基于 Reactor 来实现响应式流规范



#### **核心概念**

##### Route(路由)

路由是构建网关的基本模块，它由 ID，目标 URI，一系列的断言和过滤器组成，如果断言为 true 则匹配该路由



##### Predicate(断言)

参考的是 Java8 的 iava.util.function.Predicate
开发人员可以匹配 HTTP 请求中的所有内容(例如请求头或请求参数)，如果请求与断言相匹配则进行路由



##### Filter(过滤)

指的是 Spring 框架中 GatewayFilter 的实例，使用过滤器，可以在请求被路由前或者之后对请求进行修改



#### 工作流程

路由转发+执行过滤器链

![](D:\documents\notes\pictures\202311\001.png)

​		客户端向 Spring Cloud Gateway 发出请求。然后在 Gateway Handler Mapping 中找到与请求相匹配的路由，将其发送到 Gateway
Web Handler

​		Handler 再通过指定的过滤器链来将请求发送到我们实际的服务执行业务逻辑，然后返回。过滤器之间用虚线分开是因为过滤器可能会在发送代理请求之前(“pre”)或之后(“post”)执行业务逻辑 

​		Filter 在“pre”类型的过滤器可以做参数校验、权限校验、流量监控、日志输出、协议转换等，在“post”类型的过滤器中可以做响应内容、响应头的修改，日志的输出，流量监控等有着非常重要的作用





#### 搭建SCG网关服务器(Eureka)

创建新的子模块 SCG

##### 依赖

```xml
  <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
            <version>3.1.9</version>
        </dependency>
      <!-- 
注意 SpringCloud Gateway 使用的 web 框架为 webflux，和 SpringMVC 不兼容。引入的限流组件是 hystrix。redis 底层不再使用jedis，而是lettuce。
所以 SCG 不需要下面两个依赖，并把他们从父工程移除，移到需要的子工程 
-->
        <dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
			<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-tomcat</artifactId>
			<scope>provided</scope>
		</dependency>
  </dependencies>
```

##### 配置

###### 配置方法一

在 yaml 配置文件配置

同 nacos



###### 配置方法二

同 nacos



##### 负载均衡配置

###### pom.xml

增加下列依赖项

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

###### application.properties

增加Eureka Client配置，修改路由uri为微服务的名称

```properties
# eureka
# 将eureka集群的url配置到服务模块
eureka.client.serviceUrl.defaultZone=http://localhost:9000/eureka/
# 使用ip注册
eureka.instance.preferIpAddress=true
# 在Eureka Server的管控台中，显示的服务实例名称
eureka.instance.instanceId=${spring.cloud.client.ip-address}:${server.port}
# 指定本服务请求ip,不指定时为本机ip
eureka.instance.ipAddress=127.0.0.1

# Spring Cloud Gateway
# 目标服务地址
#spring.cloud.gateway.routes[0].uri=http://127.0.0.1:8000
# 动态路由
spring.cloud.gateway.routes[0].uri=lb://shop-service-order
```



##### 启动类

```java
@SpringBootApplication
public class SCGApplication {
    public static void main(String[] args) {
        SpringApplication.run(SCGApplication.class, args);
    }
}
```

配置好 SCG 路由之后启动服务，在浏览器中输入 http://localhost:9009/demo_spring_cloud_alibaba/order/1，即可经过 SCG 访问到订单微服务



#### 搭建SCG网关服务器(Nacos)

创建新的子模块 SCG

##### 依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
        <version>3.1.9</version>
    </dependency>
    <!-- 服务发现 -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
    <!-- 服务配置 -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-bootstrap</artifactId>
        <version>3.1.6</version>
    </dependency>
    <!--
            注意 SpringCloud Gateway 使用的 web 框架为 webflux，和 SpringMVC 不兼容。引入的限流组件是 hystrix。redis 底层不再使用jedis，而是lettuce。
            所以 SCG 不需要下面两个依赖，并把他们从父工程移除，移到需要的子工程
        -->
    <!-- <dependency> -->
    <!--     <groupId>org.springframework.boot</groupId> -->
    <!--     <artifactId>spring-boot-starter-web</artifactId> -->
    <!-- </dependency> -->
    <!-- <dependency> -->
    <!--     <groupId>org.springframework.boot</groupId> -->
    <!--     <artifactId>spring-boot-starter-tomcat</artifactId> -->
    <!--     <scope>provided</scope> -->
    <!-- </dependency> -->
    <!--open feign-->
</dependencies>
```

##### 配置

[Spring Cloud Gateway Nacos 实现动态路由_nacos 动态路由_hundan_520520的博客-CSDN博客](https://blog.csdn.net/hundan_520520/article/details/129158544)



bootstrap

```yaml
spring:
  application:
    name: SCG
  profiles:
    active: dev
  cloud:
    nacos:
      config:
        server-addr: 192.168.31.90:18848
        file-extension: yaml
        namespace: ed2069f3-cc54-4a62-b044-95061643ae84
        group: MY_GROUP
        username: app
        password: Senna&app@P1
```



```yaml
server:
  port: 9009
  servlet:
    context-path: /demo_spring_cloud_alibaba/scg
mybatis-plus:
  mapper-location: classpath*:mapper/**/*.xml
logging:
  config: classpath:logback-spring.xml

spring:
  datasource:
    url: jdbc:mysql://192.168.31.101:3306/demo_spring_cloud_alibaba?useSSL=false&serverTimezone=Hongkong&useUnicode=true&characterEncoding=utf-8&autoReconnect=true
    username: young
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
  cloud:
    nacos:
      discovery:
        server-addr: 192.168.31.90:18848
        namespace: ed2069f3-cc54-4a62-b044-95061643ae84
        group: MY_GROUP
        username: app
        password: Senna&app@P1
```



###### 配置方法一

在 yaml 配置文件配置

```yaml
spring:
  cloud:
    # Spring Cloud Gateway
    gateway:
      enabled: true
      # 定义路由规则
      routes:
        # 我们自定义的路由 ID，保持唯一
      - id: order_route
        # 匹配后提供服务的路由地址
        uri: http://127.0.0.1:9006
        # 路由条件，Predicate 接受一个输入参数，返回一个布尔值结果。该接口包含多种默认方法来将 Predicate 组合成其他复杂的逻辑（比如：与，或，非）
        predicates:
          # 断言，路径相匹配的进行路由
        - Path=/demo_spring_cloud_alibaba/order/**
          # 更多路由规则
		  # 路由断言之后匹配，在改时间之后当前路由才生效
		- After=2023-11-30T23:30:00.000+08:00
		  # 路由断言之前匹配，类似 After
		- Before=xxxxx
		  # 路由断言之间
		- Between=xxxxx,xxx
		  # 路由断言Cookie匹配,此predicate匹配给定名称(chocolate)和正则表达式(ch.p)
		- Cookie=chocolate, ch.p
		  # 路由断言 Header 匹配，header 名称匹配 X-Request-Id,header 值且正则表达式匹配\d+
		- Header=X-Request-Id, \d+
		  # 路由断言匹配Host匹配，匹配下面Host主机列表,**代表可变参数
		- Host=**.somehost.org,**.anotherhost.org
		  # 路由断言Method匹配，匹配的是请求的HTTP方法
		- Method=GET
		  # 路由断言匹配，{segment}为可变参数
		- Path=/foo/{segment},/bar/{segment}
		  # 路由断言Query匹配，将请求的参数param(baz)进行匹配，也可以进行regexp正则表达式匹配 (参数包含foo,并且foo的值匹配ba.)
		- Query=baz 或 Query=foo,ba.
		  # 路由断言RemoteAddr匹配，将匹配192.168.1.1~192.168.1.254之间的ip地址，其中24为子网掩码位数即255.255.255.0
		- RemoteAddr=192.168.1.1/24
        filters:
          # 过滤器工厂会在匹的请求头加上一对请求头，名为X-Reouest-Id值为1024
        - AddRequestParameter=X-Reuest-Id,1024
          # 路由转发是直接将匹配的路由path直接拼接到映射路径（URI）之后
          # 重写转发路径,通过RewritePath配置重写转发的url，将order-service/(?<segment>.*),重写为{segment}
        - RewritePath=/order-service/(?<segment>.*), /$\{segment}

```



###### 配置方法二

代码中注入 RouteLocator 的 Bean

```java
@Configuration
public class GatewayConfig {
    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("order_route",
                        r -> r.path("/demo_spring_cloud_alibaba/order/**")
//                .filters(
//                        f -> f.circuitBreaker(c -> c.name("myCircuitBreaker").fallbackUri("forward:/inCaseOfFailureUseThis"))
//                                .rewritePath("/consumingServiceEndpoint", "/backingServiceEndpoint")
//                )
                .uri("http://127.0.0.1:9006"))
        .build();
    }
}
```



##### 负载均衡配置

###### 依赖

```xml
<!--open feign-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    <version>3.1.6</version>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
    <version>3.1.7</version>
</dependency>
```



###### 配置

```yaml
spring:
  cloud:
    gateway:
      enabled: true
      routes:
        # 更改 uri 配置，根据注册中心服务进行负载均衡
        # uri: http://127.0.0.1:9006
        uri: lb://Order
```



##### 启动类

```java
@SpringBootApplication
@EnableDiscoveryClient
public class SCGApplication {
    public static void main(String[] args) {
        SpringApplication.run(SCGApplication.class, args);
    }
}
```

配置好 SCG 路由之后启动服务，在浏览器中输入 http://localhost:9009/demo_spring_cloud_alibaba/order/1，即可经过 SCG 访问到订单微服务



#### 过滤器

##### 类型

Spring Cloud Gateway 的 Filter 的生命周期不像 Zuul 的那么丰富，它只有两个：“pre” 和 “post”。

-  PRE： 这种过滤器在请求被路由之前调用。我们可利用这种过滤器实现身份验证、在集群中选择 请求的微服务、记录调试信息等。 


- POST：这种过滤器在路由到微服务以后执行。这种过滤器可用来为响应添加标准的 HTTP Header、收集统计信息和指标、将响应从微服务发送给客户端等。


Spring Cloud Gateway 的 Filter 从作用范围可分为另外两种GatewayFilter 与 GlobalFilter。 

- GatewayFilter：应用到单个路由或者一个分组的路由上。 


- GlobalFilter：应用到所有的路由上。




##### 生命周期

HTTP Response <------- Filter <----------- Filter <---------- Filter <---------- Proxy Filter

​																																|

​																													  MicroServices

​																																|

HTTP Request ----------> Filter ----------> Filter ----------> Filter ----------> Proxy Filter



##### GatewayFilter

[Spring Cloud Gateway](https://cloud.spring.io/spring-cloud-static/spring-cloud-gateway/2.2.1.RELEASE/reference/html/#gatewayfilter-factories)

局部过滤器（GatewayFilter），是针对单个路由的过滤器。可以对访问的URL过滤，进行切面处理。在 Spring Cloud Gateway中通过GatewayFilter的形式内置了很多不同类型的局部过滤器。这里简单将 Spring Cloud Gateway内置的所有过滤器工厂整理成了一张表格，虽然不是很详细，但能作为速览使 用。如下：

| 过滤器工厂                  | 作用                                                         | 参数                                                         |
| --------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| AddRequestHeader            | 为原始请求添加Header                                         | Header的名称及值                                             |
| AddRequestParameter         | 为原始请求添加请求参数                                       | 参数名称及值                                                 |
| AddResponseHeader           | 为原始响应添加Header                                         | Header的名称及值                                             |
| DedupeResponseHeader        | 剔除响应头中重复的值                                         | 需要去重的Header名称及去重策略                               |
| Hystrix                     | 为路由引入Hystrix的断路器保护                                | HystrixCommand的名 称                                        |
| FallbackHeaders             | 为fallbackUri的请求头中添加具 体的异常信息                   | Header的名称                                                 |
| PrefixPath                  | 为原始请求路径添加前缀                                       | 前缀路径                                                     |
| PreserveHostHeader          | 为请求添加一个 preserveHostHeader=true的属 性，路由过滤器会检查该属性以 决定是否要发送原始的Host | 无                                                           |
| RequestRateLimiter          | 用于对请求限流，限流算法为令 牌桶                            | keyResolver、 rateLimiter、 statusCode、 denyEmptyKey、 emptyKeyStatus |
| RedirectTo                  | 将原始请求重定向到指定的URL                                  | http状态码及重定向的 url                                     |
| RemoveHopByHopHeadersFilter | 为原始请求删除IETF组织规定的 一系列Header                    | 默认就会启用，可以通过配置指定仅删除哪些 Header              |
| RemoveRequestHeader         | 为原始请求删除某个Header                                     | Header名称                                                   |
| RemoveResponseHeader        | 为原始响应删除某个Header                                     | Header名称                                                   |
| RewritePath                 | 重写原始的请求路径                                           | 原始路径正则表达式以及重写后路径的正则表达式                 |
| RewriteResponseHeader       | 重写原始响应中的某个Header                                   | Header名称，值的正则表达式，重写后的值                       |
| SaveSession                 | 在转发请求之前，强制执行 WebSession::save操作                | 无                                                           |
| secureHeaders               | 为原始响应添加一系列起安全作用的响应头                       | 无，支持修改这些安全响应头的值                               |
| SetPath                     | 修改原始的请求路径                                           | 修改后的路径                                                 |
| SetResponseHeader           | 修改原始响应中某个Header的值                                 | Header名称，修改后的值                                       |
| SetStatus                   | 修改原始响应的状态码                                         | HTTP 状态码，可以是数字，也可以是字符串                      |
| StripPrefix                 | 用于截断原始请求的路径                                       | 使用数字表示要截断的路径的数量                               |
| Retry                       | 针对不同的响应进行重试                                       | retries、statuses、 methods、series                          |
| RequestSize                 | 设置允许接收最大请求包的大 小。如果请求包大小超过设置的 值，则返回 413 Payload Too Large | 请求包大小，单位为字 节，默认值为5M                          |
| ModifyRequestBody           | 在转发请求之前修改原始请求体 内容                            | 修改后的请求体内容                                           |
| ModifyResponseBody          | 修改原始响应体的内容                                         | 修改后的响应体内容                                           |

每个过滤器工厂都对应一个实现类，并且这些类的名称必须以 GatewayFilterFactory 结尾，这是 Spring Cloud Gateway的一个约定，例如 AddRequestHeader 对应的实现类为 AddRequestHeaderGatewayFilterFactory 。对于这些过滤器的使用方式可以参考官方文档.

##### GlobalFilter

[Spring Cloud Gateway](https://cloud.spring.io/spring-cloud-static/spring-cloud-gateway/2.2.1.RELEASE/reference/html/#global-filters)

全局过滤器（GlobalFilter）作用于所有路由，Spring Cloud Gateway 定义了Global Filter接口，用户 可以自定义实现自己的Global Filter。通过全局过滤器可以实现对权限的统一校验，安全性验证等功 能，并且全局过滤器也是程序员使用比较多的过滤器。 

Spring Cloud Gateway内部也是通过一系列的内置全局过滤器对整个路由转发进行处理如下：

​																					  			  通过负载均衡客户端根据路由的URL

​										负载均衡客户端相关过滤器  			解析转换成真实的请求URL

​							  | ----- LoadBalancer  -------------------------- LoadBalancerClientFilter

​							  |

​							  |											  					  通过HttpClient客户端被转发请求真

​							  |		http客户端相关过滤器		  			实的URL并响应写入到当前的请求响应中

​							  | ----- HttpClient  ------------------------- | ---- NettyRoutingFilter

​							  |															| ---- NettyWriteResponseFilter

​							  |

​							  |        Websocket相关过滤器					  负责处理Websocket类型的请求响应信息

​							  | ----- Websocket -------------------------------- WebsocketRoutingFilter

GlobalFilter ------ |      

​							  |		路径转发相关过滤器						  解析路径，并将路径转发

​							  | ----- ForwardPath ---------------------------- ForwardPathFilter

​							  |

​							  |		路由url相关过滤器							转换路由中的URI

​							  | ----- RouteToRequestUrl ------------------ RouteToRequestUrlFilter

​							  |

​							  |																  通过WebClient客户端转发请求真实的

​							  |		WebClient相关过滤器					URL并将响应写入到当前的请求响应中

​							  | ----- WebClient  ---------------------- | ---- WebClientHttpRoutingFilter

​																						 | ---- WebClientWriteResponseFilter



###### 自定义过滤器

```java
@Component
public class SCGGlobalFilter implements GlobalFilter, Ordered{

	public int getOrder() {
		return 0;
	}

	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		//String url = exchange.getRequest().getURI().getPath();
		//忽略以下url请求
		//if(url.indexOf("/login") >= 0){
		// return chain.filter(exchange);
		// }
		String token = exchange.getRequest().getQueryParams().getFirst("token");
		if (StringUtils.isBlank(token)) {
			System.out.println("token is empty ...");
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}
		return chain.filter(exchange);
	}
}
```

- 自定义全局过滤器需要实现GlobalFilter和Ordered接口。 

- 在filter方法中完成过滤器的逻辑判断处理 

- 在getOrder方法指定此过滤器的优先级，返回值越大级别越低 

- ServerWebExchange 就相当于当前请求和响应的上下文，存放着重要的请求-响应属性、请求实 例和响应实例等等。一个请求中的request，

- response都可以通过 ServerWebExchange 获取 调用 chain.filter 继续向下游执行

配置好过滤器之后启动服务，在浏览器中输入带token的链接http://localhost:9100/order-service/order/buy?uid=1&pid=2&number=3&token=token，即可访问到订单微服务

#### 网关限流

##### 常见限流算法

###### 计数器

计数器限流算法是最简单的一种限流实现方式。其本质是通过维护一个单位时间内的计数器，每次请求 计数器加1，当单位时间内计数器累加到大于设定的阈值，则之后的请求都被拒绝，直到单位时间已经 过去，再将计数器重置为零



###### 漏桶算法

漏桶算法可以很好地限制容量池的大小，从而防止流量暴增。漏桶可以看作是一个带有常量服务时间的 单服务器队列，如果漏桶（包缓存）溢出，那么数据包会被丢弃。 在网络中，漏桶算法可以控制端口的 流量输出速率，平滑网络上的突发流量，实现流量整形，从而为网络提供一个稳定的流量。

为了更好的控制流量，漏桶算法需要通过两个变量进行控制：一个是桶的大小，支持流量突发增多时可 以存多少的水（burst），另一个是水桶漏洞的大小（rate）。

###### 令牌桶算法

令牌桶算法是对漏桶算法的一种改进，桶算法能够限制请求调用的速率，而令牌桶算法能够在限制调用 的平均速率的同时还允许一定程度的突发调用。在令牌桶算法中，存在一个桶，用来存放固定数量的令 牌。算法中存在一种机制，以一定的速率往桶中放令牌。每次请求调用需要先获取令牌，只有拿到令 牌，才有机会继续执行，否则选择选择等待可用的令牌、或者直接拒绝。放令牌这个动作是持续不断的 进行，如果桶中令牌数达到上限，就丢弃令牌，所以就存在这种情况，桶中一直有大量的可用令牌，这 时进来的请求就可以直接拿到令牌执行，比如设置qps为100，那么限流器初始化完成一秒后，桶中就 已经有100个令牌了，这时服务还没完全启动好，等启动完成对外提供服务时，该限流器可以抵挡瞬时 的100个请求。所以，只有桶中没有令牌时，请求才会进行等待，最后相当于以一定的速率执行。



##### 基于Filter的限流

SpringCloudGateway官方就提供了基于令牌桶的限流支持。基于其内置的过滤器工厂 RequestRateLimiterGatewayFilterFactory 实现。在过滤器工厂中是通过Redis和lua脚本结合的方 式进行流量控制。

###### pom.xml

在scg模块新增以下依赖

```xml
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-redis-reactive</artifactId>
		</dependency>
```

###### application.properties

在配置文件中加入限流的配置并配置redis

```properties
# Spring Cloud Gateway
# 基于Filter的限流
# 配置了RequestRateLimiter的限流过滤器
spring.cloud.gateway.routes[0].filters[1].name=RequestRateLimiter
# 用于限流的键的解析器的 Bean 对象的名字。它使用 SpEL 表达式根据#{@beanName}从 Spring 容器中获取 Bean 对象。
spring.cloud.gateway.routes[0].filters[1].args.keyResolver=#{@pathKeyResolver}
# 令牌桶每秒填充平均速率
spring.cloud.gateway.routes[0].filters[1].args.redis-rate-limiter.replenishRate=1
# 令牌桶的总容量
spring.cloud.gateway.routes[0].filters[1].args.redis-rate-limiter.burstCapacity=3

# Redis
spring.redis.host=localhost
spring.redis.port=6379
```

###### 配置KeyResolver

为了达到不同的限流效果和规则，可以通过实现 KeyResolver 接口，定义不同请求类型的限流键。

```java
@Configuration
public class KeyResolverConfiguration {
	//基于请求路径的限流
	@Bean
	public KeyResolver pathKeyResolver() {
		return exchange -> Mono.just(
			exchange.getRequest().getPath().toString()
		);	
	}
//	//基于请求ip地址的限流
//	@Bean
//	public KeyResolver ipKeyResolver() {
//		return exchange -> Mono.just(
//			exchange.getRequest().getHeaders().getFirst("X-Forwarded-For")
//		);
//	}
//	//编写基于请求参数的限流规则
//	@Bean
//	public KeyResolver paramsKeyResolver() {
//		return exchange -> Mono.just(
//				exchange.getRequest().getQueryParams().getFirst("uid")
//		);
//	}
}

```

使用Jmetter模拟10组线程访问，会发现当达到令牌桶的总容量3时，其他的请求会返回429错误。

通过reids的MONITOR可以监听(redis-cli.exe -h 127.0.0.1 -p 6379 -> MONITOR)redis的执行过程。这时候Redis中会有对应的数据。



##### 基于Sentinel的限流

Sentinel 支持对 Spring Cloud Gateway、Zuul 等主流的 API Gateway 进行限流。

###### pom.xml

在scg模块新增以下依赖

```xml
		<dependency>
			<groupId>com.alibaba.csp</groupId>
			<artifactId>sentinel-spring-cloud-gateway-adapter</artifactId>
			<version>1.8.0</version>
		</dependency>
```

###### 编写配置类

```java
@Configuration
public class SentinelGatewayConfiguration {
	private final List<ViewResolver> viewResolvers;
	private final ServerCodecConfigurer serverCodecConfigurer;
	public SentinelGatewayConfiguration(ObjectProvider<List<ViewResolver>> viewResolversProvider, ServerCodecConfigurer serverCodecConfigurer) {
		this.viewResolvers=viewResolversProvider.getIfAvailable(Collections::emptyList);
		this.serverCodecConfigurer=serverCodecConfigurer;
	}

	/**
	 * 配置限流的异常处理器:SentinelGatewayBlockExceptionHandler
	 */
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
		return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
	}

	/**
	 * 配置限流过滤器
	 */
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public GlobalFilter sentinelGatewayFilter() {
		return new SentinelGatewayFilter();
	}

	/**
	 * 配置初始化的限流参数
	 */
	@PostConstruct
	public void initGatewayRules() {
		Set<GatewayFlowRule> rules = new HashSet<>();
		rules.add(new GatewayFlowRule("order-service-id") // 资源名称
				.setCount(1) // 限流阈值
				.setIntervalSec(1) // 统计时间窗口，单位是秒，默认是 1 秒
		);
		GatewayRuleManager.loadRules(rules);
	}
}
```

- 基于Sentinel 的Gateway限流是通过其提供的Filter来完成的，使用时只需注入对应的 SentinelGatewayFilter 实例以及 SentinelGatewayBlockExceptionHandler 实例即可。 

- @PostConstruct定义初始化的加载方法，用于指定资源的限流规则。这里资源的名称为 orderservice ，统计时间是1秒内，限流阈值是1。表示每秒只能访问一个请求。

###### 自定义异常提示

当触发限流后页面显示的是Blocked by Sentinel: FlowException。为了展示更加友好的限流提示， Sentinel支持自定义异常处理。

您可以在 GatewayCallbackManager 注册回调进行定制： 

setBlockHandler ：注册函数用于实现自定义的逻辑处理被限流的请求，对应接口为 BlockRequestHandler 。默认实现为 DefaultBlockRequestHandler ，当被限流时会返回类似 于下面的错误信息： Blocked by Sentinel: FlowException 。

```java
	/**
	 * 配置初始化的限流参数
	 */
	@PostConstruct
	public void initGatewayRules() {
		Set<GatewayFlowRule> rules = new HashSet<>();
		rules.add(new GatewayFlowRule("order-service-id") // 资源名称
				.setCount(1) // 限流阈值
				.setIntervalSec(1) // 统计时间窗口，单位是秒，默认是 1 秒
				//参数限流
				.setParamItem(new GatewayParamFlowItem().setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM).setFieldName("uid"))
		);
		GatewayRuleManager.loadRules(rules);
		//自定义异常提示
		BlockRequestHandler blockRequestHandler = new BlockRequestHandler() {
			public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
				Map<String,Object> map = new HashMap<>();
				map.put("code", 001);
				map.put("message", "对不起,接口限流了");
				return ServerResponse.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(BodyInserters.fromValue(map));
			}
		};
		GatewayCallbackManager.setBlockHandler(blockRequestHandler);
	}
```

###### 参数限流

通过指定PARAM_PARSE_STRATEGY_URL_PARAM表示从url中获取参数，setFieldName指定参数名称

```java
	/**
	 * 配置初始化的限流参数
	 */
	@PostConstruct
	public void initGatewayRules() {
		Set<GatewayFlowRule> rules = new HashSet<>();
		rules.add(new GatewayFlowRule("order-service-id") // 资源名称
				.setCount(1) // 限流阈值
				.setIntervalSec(1) // 统计时间窗口，单位是秒，默认是 1 秒
				//参数限流
				.setParamItem(new GatewayParamFlowItem().setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM).setFieldName("uid"))
		);
		GatewayRuleManager.loadRules(rules);
    }
```

###### 自定义API分组

```java
	@PostConstruct
	private void initCustomizedApis() {
		Set<ApiDefinition> definitions = new HashSet<>();
		ApiDefinition api1 = new ApiDefinition("product_api").setPredicateItems(new HashSet<ApiPredicateItem>() {
			{
				// 以/product-service/product 开头的请求
				add(new ApiPathPredicateItem().setPattern("/productservice/product/**")
						.setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
			}
		});
		ApiDefinition api2 = new ApiDefinition("order_api").setPredicateItems(new HashSet<ApiPredicateItem>() {
			{
				/// order-service/order 完成的url路径匹配
				add(new ApiPathPredicateItem().setPattern("/order-service/order"));
			}
		});
		definitions.add(api1);
		definitions.add(api2);
		GatewayApiDefinitionManager.loadApiDefinitions(definitions);
	}
```



#### SCG高可用集群

高可用HA（High Availability）是分布式系统架构设计中必须考虑的因素之一，它通常是指，通过设计 减少系统不能提供服务的时间。我们都知道，单点是系统高可用的大敌，单点往往是系统高可用最大的 风险和敌人，应该尽量在系统设计的过程中避免单点。方法论上，高可用保证的原则是“集群化”，或者 叫“冗余”：只有一个单点，挂了服务会受影响；如果有冗余备份，挂了还有其他backup能够顶上。

不同的客户端使用不同的负载将请求分发到后端 的 Gateway，Gateway 再通过HTTP调用后端服务，最后对外输出。因此为了保证 Gateway 的高可用 性，前端可以同时启动多个 Gateway 实例进行负载，在 Gateway 的前端使用 Nginx 或者 F5 进行负载 转发以达到高可用性。

（1） 准备多个GateWay工程 修改 

shop_gateway_server 的application.yml。添加如下配置

```yml
spring:
	application:
		name: api-gateway #指定服务名
	cloud:
		gateway:
            routes:
			- id: product-service
			uri: lb://shop-service-product
			predicates:
			- Path=/product-service/**
			filters:
			- RewritePath=/product-service/(?<segment>.*), /$\{segment}
eureka:
	client:
		serviceUrl:
			defaultZone: http://eureka1:8761/eureka/
			registry-fetch-interval-seconds: 5 # 获取服务列表的周期：5s
	instance:
		preferIpAddress: true
	ip-address: 127.0.0.1
---
spring:
    profiles: gateway01
server:
	port: 8080 #服务端口
---
spring:
	profiles: gateway02
server:
	port: 8081 #服务端口
```

通过不同的profiles配置启动两个网关服务，请求端口分别为8080和8081。浏览器验证发现效果是一致 的。

（2） 配置ngnix 找到ngnix添加负载均衡配置

```
#配置多台服务器（这里只在一台服务器上的不同端口）
upstream gateway {
	server 127.0.0.1:8081;
	server 127.0.0.1:8080;
}
#请求转向mysvr 定义的服务器列表
location / {
	proxy_pass http://gateway;
}
```

在浏览器上通过访问http://localhost/order-service/order/buy/1请求的效果和之前是一样的。这次关 闭一台网关服务器，还是可以支持部分请求的访问。

## 链路追踪

在大型系统的微服务化构建中，一个系统会被拆分成许多模块。这些模块负责不同的功能，组合成系 统，最终可以提供丰富的功能。在这种架构中，一次请求往往需要涉及到多个服务。互联网应用构建在 不同的软件模块集上，这些软件模块，有可能是由不同的团队开发、可能使用不同的编程语言来实现、 有可能布在了几千台服务器，横跨多个不同的数据中心，也就意味着这种架构形式也会存在一些问题：

- 如何快速发现问题？

- 如何判断故障影响范围？ 

- 如何梳理服务依赖以及依赖的合理性？ 

- 如何分析链路性能问题以及实时容量规划？

分布式链路追踪（Distributed Tracing），就是将一次分布式请求还原成调用链路，进行日志记录，性 能监控并将 一次分布式请求的调用情况集中展示。比如各个服务节点上的耗时、请求具体到达哪台机器 上、每个服务节点的请求状态等等。 目前业界比较流行的链路追踪系统如：Twitter的Zipkin，阿里的鹰眼，美团的Mtrace，大众点评的 cat等，大部分都是基于google发表的Dapper。Dapper阐述了分布式系统，特别是微服务架构中链路 追踪的概念、数据表示、埋点、传递、收集、存储与展示等技术细节。

### Sleuth+Zipkin

#### Sleuth

##### 简介

Spring Cloud Sleuth 主要功能就是在分布式系统中提供追踪解决方案，并且兼容支持了 zipkin，你只 需要在 pom 文件中引入相应的依赖即可。

##### 相关概念

Spring Cloud Sleuth 为 Spring Cloud 提供了分布式根据的解决方案。它大量借用了 Google Dapper 的 设计。先来了解一下 Sleuth 中的术语和相关概念。 

Spring Cloud Sleuth 采用的是 Google 的开源项目 Dapper 的专业术语。

- Span：基本工作单元，例如，在一个新建的 span 中发送一个 RPC 等同于发送一个回应请求给 RPC，span 通过一个 64 位 ID 唯一标识，trace 以另一个 64 位 ID 表示，span 还有其他数据信息，比 如摘要、时间戳事件、关键值注释(tags)、span 的 ID、以及进度ID（通常是 IP 地址） span 在不断的启动和停止，同时记录了时间信息，当你创建了一个 span，你必须在未来的某个时 刻停止它。

- Trace：一系列 spans 组成的一个树状结构，例如，如果你正在跑一个分布式大数据工程，你可能 需要创建一个 trace。

- Annotation：用来及时记录一个事件的存在，一些核心 annotations 用来定义一个请求的开始和结 束

  - cs - Client Sent -客户端发起一个请求，这个 annotion 描述了这个 span 的开始

  - sr - Server Received -服务端获得请求并准备开始处理它，如果将其 sr 减去 cs 时间戳便可得到网络延迟

  - ss - Server Sent -注解表明请求处理的完成(当请求返回客户端)，如果ss减去sr时间戳便可得 到服务端需要的处理请求时间

  - cr - Client Received -表明 span 的结束，客户端成功接收到服务端的回复，如果 cr 减去 cs 时间戳便可得到客户端从服务端获取回复的所有所需时间



##### pom.xml

被追踪的微服务添加如下依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
    <version>3.1.5</version>
</dependency>
```

##### application.properties

被追踪的微服务添加如下配置

```properties
# Sleuth
spring:
  sleuth:
    enabled: true
    sampler:
      # 采样的百分比,默认为0.1，即10%，这里配置1，是记录全部的sleuth信息，是为了收集到更多的数据（仅供测试用）。
      # 在分布式系统中，过于频繁的采样会影响系统性能，所以这里配置需要采用一个合适的值
      probability: 1.0
logging:
  level:
    root: INFO
    org.springframework.web.servlet.RequestDispatcher: DEBUG
    org.springframework.cloud.sleuth: DEBUG
```

每个微服务都需要添加如上的配置。启动微服务，调用之后，我们可以在控制台观察到sleuth的日志输出。

```shell
2020-09-11 00:09:47.638  INFO [shop-server-eureka,e20e504efe80c112,e20e504efe80c112,false]
2020-09-11 00:09:52.549  INFO [shop-server-eureka,19856ec89ac02e15,19856ec89ac02e15,false]
2020-09-11 00:09:53.182  INFO [shop-server-eureka,78dd64bef79a5409,78dd64bef79a5409,false]
2020-09-11 00:09:55.875  INFO [shop-server-eureka,64b8d39648deb8c5,64b8d39648deb8c5,false]
2020-09-11 00:09:56.434  INFO [shop-server-eureka,1e9ddb1299334bee,1e9ddb1299334bee,false]
2020-09-11 00:09:59.753  INFO [shop-server-eureka,bf52a267a31cbf1b,bf52a267a31cbf1b,false]
2020-09-11 00:10:02.654  INFO [shop-server-eureka,be3377c783653c4f,be3377c783653c4f,false]
2020-09-11 00:10:08.202  INFO [shop-server-eureka,c58c5d7b28a70f8a,c58c5d7b28a70f8a,false]
```

服务名称后面是 TraceId，后面跟着的是 SpanId，依次调用有一个全局的 TraceId，将调用链路串起来。

仔细分析每个微服务的日志，不难看出请求的具体过程。 查看日志文件并不是一个很好的方法，当微服务越来越多日志文件也会越来越多，通过 Zipkin 可以将日志聚合，并进行可视化展示和全文检索。

####  Zipkin

##### 简介

Zipkin 是 Twitter 的一个开源项目，它基于 Google Dapper 实现，它致力于收集服务的定时数据，以 解决微服务架构中的延迟问题，包括数据的收集、存储、查找和展现。 我们可以使用它来收集各个服务 器上请求链路的跟踪数据，并通过它提供的 REST API 接口来辅助我们查询跟踪数据以实现对分布式系统的监控程序，从而及时地发现系统中出现的延迟升高问题并找出系统性能瓶颈的根源。除了面向开发的 API 接口之外，它也提供了方便的 UI 组件来帮助我们直观的搜索跟踪信息和分析请求链路明细，比 如：可以查询某段时间内各用户请求的处理时间等。 Zipkin 提供了可插拔数据存储方式：InMemory、MySql、Cassandra 以及 Elasticsearch。

Zipkin 主要由 4 个核心组件构成： 

- Collector：收集器组件，它主要用于处理从外部系统发送过来的跟踪信息，将这些信息转换为 Zipkin 内部处理的 Span 格式，以支持后续的存储、分析、展示等功能。
- Storage：存储组件，它主要对处理收集器接收到的跟踪信息，默认会将这些信息存储在内存中， 我们也可以修改此存储策略，通过使用其他存储组件将跟踪信息存储到数据库中。 
- RESTful API：API 组件，它主要用来提供外部访问接口。比如给客户端展示跟踪信息，或是外接系统访问以实现监控等。 
- Web UI：UI 组件，基于 API 组件实现的上层应用。通过 UI 组件用户可以方便而有直观地查询和分析跟踪信息。 

Zipkin 分为两端，一个是 Zipkin 服务端，一个是 Zipkin 客户端，客户端也就是微服务的应用。 客户端会配置服务端的 URL 地址，一旦发生服务间的调用的时候，会被配置在微服务里面的 Sleuth 的 监听器监听，并生成相应的 Trace 和 Span 信息发送给服务端。 发送的方式主要有两种，一种是 HTTP 报文的方式，还有一种是消息总线的方式如 RabbitMQ。 不论哪种方式，我们都需要： 一个 Eureka 服务注册中心，这里我们就用之前的 eureka 项目来当注册中心。 一个 Zipkin 服务端。 多个微服务，这些微服务中配置Zipkin 客户端。



[OpenZipkin](https://zipkin.io/)

[Zipkin服务端搭建使用教程_zipkin-server_思月行云的博客-CSDN博客](https://blog.csdn.net/kenkao/article/details/127844321)

[Kubnernetes 集群部署 Zipkin+Kafka+ElasticSearch 实现链路追踪_wx6086232c36932的技术博客_51CTO博客](https://blog.51cto.com/u_15181572/6172583)

#### Zipkin Server

##### 安装配置部署管理

###### **安装**

从 SpringBoot 2.0 开始，官方就不再支持使用自建 Zipkin Server 的方式进行服务链路追踪，而是直接提供了编译好的 jar 包来给我们使用 

```shell
# https://github.com/openzipkin/zipkin/releases
# https://search.maven.org/remote_content?g=io.zipkin.java&a=zipkin-server&v=LATEST&c=exec
# https://repo1.maven.org/maven2/io/zipkin/zipkin-server/2.24.3/zipkin-server-2.24.3-exec.jar
curl -sSL https://zipkin.io/quickstart.sh | bash -s
```



使用源码

```shell
# get the latest source
git clone https://github.com/openzipkin/zipkin
cd zipkin
# Build the server and also make its dependencies
./mvnw -DskipTests --also-make -pl zipkin-server clean install
# Run the server
java -jar ./zipkin-server/target/zipkin-server-*exec.jar
```



```shell
sudo mkdir /opt/apps/zipkin/
sudo mkdir /opt/apps/zipkin/conf
sudo mkdir /var/log/zipkin/
sudo cp zipkin-server-2.24.3-exec.jar /opt/apps/zipkin/zipkin.jar
sudo chmod +x /opt/apps/zipkin/zipkin.jar
```



###### **配置**

Zipkin Server 的启动参数可以通过官方提供的 yml 配置文件查找 

[zipkin-server-shared-github.yml](https://github.com/openzipkin/zipkin/blob/master/zipkin-server/src/main/resources/zipkin-server-shared.yml)

[zipkin-server-shared.yml](../files/config/Java/SpringCloud/zipkin-server-shared.yml)

[zipkin-server-my.yml](../files/config/Java/SpringCloud/zipkin-server-my.yml)



**使用启动参数配置**

```shell
# 例如配置端口号，rabbitmq，根据 yml 内的配置项进行配置
java -Xms1g -Xmx1g -jar zipkin.jar
    --server.port=9411
    --zipkin.collector.rabbitmq.addresses=ip:port
    --zipkin.collector.rabbitmq.username=username
    --zipkin.collector.rabbitmq.password=password
```



**使用配置文件配置**

```yaml
server:
  port: 9411
zipkin:
  collector:
    rabbitmq:
      addresses: 192.168.31.90:10574
      username: app
      password: Hello&App@918
```

```shell
# 启动时附加配置文件中的配置
java -Xms1g -Xmx1g -jar -Dspring.config.additional-location=zipkin-server-my.yml zipkin.jar 
```



###### **创建用户**

```shell
sudo groupadd -r apps
sudo useradd -r -g apps -s /bin/false zipkin

sudo chown -R zipkin:root /opt/apps/zipkin
sudo chown -R zipkin:root /var/log/zipkin
```



###### **服务管理**

手动

```shell
# 启动
sudo bash -c 'sudo -u zipkin nohup java -Xms1g -Xmx1g -jar -Dspring.config.additional-location=/opt/apps/zipkin/conf/zipkin-server-my.yml /opt/apps/zipkin/zipkin.jar > /var/log/zipkin/zipkin.nohup.log 2>&1 &'

# 停止
ps aux | grep 'zipkin'
sudo kill -9 PID
```



自动

编写[/opt/apps/zipkin/zipkin.server](../files/scripts/server/zipkin.server)

配置参考 Linux -> 服务管理  -> 系统启动时自动运行服务



- 默认 Zipkin Server 的请求端口为 9411
- 在浏览器输入 http://127.0.0.1:9411 即可进入到 Zipkin Server 的管理后台



###### **集群**

直接使用 `HAProxy` 或 `Niginx` 代理，`Keepalived` 配置虚拟 IP 进行路由转发

 

#### Zipkin Client

通过 Sleuth 查看日志分析微服务的调用链路并不是一个很直观的方案，结合 zipkin 可以很直观地显示微服务之间的调用关系。

##### pom.xml

被追踪的微服务添加如下依赖

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-zipkin</artifactId>
    <version>3.1.5</version>
</dependency>
```

##### application.properties

被追踪的微服务添加如下配置

```properties
# Zipkin
spring:
  zipkin:
    enabled: true
    # zipkin server的请求地址
    base-url: http://127.0.0.1:9411/
    # 请求方式,默认以http的方式向zipkin server发送追踪数据
    sender:
      type: web
```

##### 测试

通过浏览器发送一次微服务请求。打开 Zipkin Server 控制台，我们可以根据条件查询追踪每次请求调用过程



#### 基于 RabbitMQ 收集数据

加了 MQ 之后，通信过程为

Zipkin client -------> RabbitMQ ---------> Zipkin Server



#####  Zipkin Server

###### 配置

```yml
server:
  port: 9411
zipkin:
  collector:
    rabbitmq:
      addresses: 127.0.0.1:9411
      username: guest
      password: guest
```

启动 Zipkin Server 之后，我们打开 RabbitMQ 的控制台可以看到多了一个名为 Zipkin 的 Queue



##### Zipkin Client

###### pom.xml

被追踪的微服务添加如下依赖

```xml
<dependency>
    <groupId>org.springframework.amqp</groupId>
    <artifactId>spring-rabbit</artifactId>
</dependency>
```

导入 spring-rabbit 依赖，是 Spring 提供的对 rabbit 的封装，客户端会根据配置自动的生产消息并发送到目标队列中

###### application.properties

被追踪的微服务添加如下配置

```properties
# Zipkin 更改为如下
spring:
  zipkin:
    enabled=true
    sender:
      type: rabbit

  # 加入 RabbitMQ 配置
  rabbitmq:
    host: 192.168.31.158
    port: 5672
    username: admin
    password: 123456
    # 这里配置了重试策略
    listener:
      direct:
        retry:
          enabled: true
      simple:
        retry:
          enabled: true
```



###### 测试

关闭 Zipkin Server，并随意请求连接。打开 rabbitmq 管理后台可以看到，消息已经推送到 rabbitmq。 当 Zipkin Server 启动时，会自动的从 rabbitmq 获取消息并消费，展示追踪数据 

可以看到如下效果： 

- 请求的耗时时间不会出现突然耗时特长的情况 

- 当 ZipkinServer 不可用时（比如关闭、网络不通等），追踪信息不会丢失，因为这些信息会保存在 Rabbitmq 服务器上，直到 Zipkin 服务器可用时，再从 Rabbitmq 中取出这段时间的信息



#### MySql 存储跟踪数据

Zipkin Server 默认时间追踪数据信息保存到内存，这种方式不适合生产环境。因为一旦 Service 关闭重 启或者服务崩溃，就会导致历史数据消失。Zipkin 支持将追踪数据持久化到 MySql 数据库或者存储到 elasticsearch 中。这里已 MySql 为例。

##### 准备数据库 

可以从官网找到 Zipkin Server 持久 MySql 的[数据库脚本](https://github.com/openzipkin/zipkin/blob/master/zipkin-storage/mysql-v1/src/main/resources/mysql.sql)。

[mysql.sql](../files/config/Java/SpringCloud/zipkin-server-mysql.sql)



##### 配置数据源

```yaml
zipkin:
  storage:
    # 存储类型 mem\mysql\elasticsearch
    type: mysql
    mysql:
      jdbc-url:
      host: localhost
      port: 3306
      username: username
      password: password
      db: zipkin
      max-active: 10
      use-ssl: false
```



配置好服务端之后，可以在浏览器请求几次。回到数据库查看会发现数据已经持久化到 mysql 中



#### ES 存储跟踪数据

##### 配置

新增以下 elasticsearch 配置

```yaml
zipkin:
  storage:
    # 存储类型 mem\mysql\elasticsearch
    type: elasticsearch
    elasticsearch:
      # host is left unset intentionally, to defer the decision
      hosts: localhost:9200
      index: zipkin
```



##  Spring Cloud Stream

### 概述 

Spring Cloud Stream 由一个中间件中立的核组成。应用通过 Spring Cloud Stream 插入的 input(相当于 消费者consumer，它是从队列中接收消息的)和output(相当于生产者producer，它是从队列中发送消 息的。)通道与外界交流。通道通过指定中间件的Binder实现与外部代理连接。业务开发者不再关注具 体消息中间件，只需关注Binder对应用程序提供的抽象概念来使用消息中间件实现业务即可。

### 核心概念

#### 绑定器

Binder 绑定器是Spring Cloud Stream中一个非常重要的概念。在没有绑定器这个概念的情况下，我们 的Spring Boot应用要直接与消息中间件进行信息交互的时候，由于各消息中间件构建的初衷不同，它 们的实现细节上会有较大的差异性，这使得我们实现的消息交互逻辑就会非常笨重，因为对具体的中间 件实现细节有太重的依赖，当中间件有较大的变动升级、或是更换中间件的时候，我们就需要付出非常 大的代价来实施。 通过定义绑定器作为中间层，实现了应用程序与消息中间件(Middleware)细节之间的隔离。通过向应用 程序暴露统一的Channel通过，使得应用程序不需要再考虑各种不同的消息中间件的实现。当需要升级 消息中间件，或者是更换其他消息中间件产品时，我们需要做的就是更换对应的Binder绑定器而不需要 修改任何应用逻辑 。甚至可以任意的改变中间件的类型而不需要修改一行代码。 Spring Cloud Stream支持各种binder实现，下表包含GitHub项目的链接。

[Apache Kafka](https://github.com/spring-cloud/spring-cloud-stream-binder-kafka)

[Amazon Kinesis](https://github.com/spring-cloud/spring-cloud-stream-binder-aws-kinesis)

[Google PubSub (partner maintained)](https://github.com/spring-cloud/spring-cloud-gcp/tree/master/spring-cloud-gcp-pubsub-stream-binder)

[Solace PubSub+ (partner maintained)](https://github.com/SolaceProducts/spring-cloud-stream-binder-solace)

[Azure Event Hubs (partner maintained)](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-cloud-eventhubs-stream-binder)

通过配置把应用和spring cloud stream 的 binder 绑定在一起，之后我们只需要修改 binder 的配置来 达到动态修改topic、exchange、type等一系列信息而不需要修改一行代码。

#### 发布/订阅模型

在Spring Cloud Stream中的消息通信方式遵循了发布-订阅模式，当一条消息被投递到消息中间件之 后，它会通过共享的 Topic 主题进行广播，消息消费者在订阅的主题中收到它并触发自身的业务逻辑处 理。这里所提到的 Topic 主题是Spring Cloud Stream中的一个抽象概念，用来代表发布共享消息给消 费者的地方。在不同的消息中间件中， Topic 可能对应着不同的概念，比如：在RabbitMQ中的它对应 了Exchange、而在Kakfa中则对应了Kafka中的Topic。

### RabbitMQ

#### 生产者

##### pom.xml

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
</dependency>
```

##### application.properties

```properties
# rabbitmq
spring.rabbitmq.host=192.168.31.158
spring.rabbitmq.port=5672
spring.rabbitmq.username=admin
spring.rabbitmq.password=123456

# spring cloud stream
# 指定了消息发送的目的地，对应 RabbitMQ，会发送到 exchange 是 test-exchange 的所有消息队列中
spring.cloud.stream.bindings.output.destination=test-exchange
# 用于指定消息的类型。具体可以参考https://docs.spring.io/spring-cloud-stream/docs/Chelsea.SR2/reference/htmlsingle/index.html
spring.cloud.stream.bindings.output.contentType=text/plain
```

##### bingding

发送消息时需要定义一个接口，不同的是接口方法的返回对象是 MessageChannel，下面是 Spring Cloud Stream 内置的接口

```java
public interface Source {
    String OUTPUT = "output";
    @Output("output")
    MessageChannel output();
}
```




##### controller

```java
@RestController
@RequestMapping("/producer")
@EnableBinding(Source.class)
public class ProductController {
	private final MessageChannel output;
	public ProductController(MessageChannel output) {//@Qualifier("output") 
		this.output=output;
	}
	@GetMapping("/{msg}")
	public void send(@PathVariable("msg") String msg) {
		//发送MQ消息
        output.send(MessageBuilder.withPayload(msg).build());
	}
}
```

#### 消费者

##### pom.xml

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
</dependency>
```

##### application.properties

```properties
# rabbitmq
spring.rabbitmq.host=192.168.31.158
spring.rabbitmq.port=5672
spring.rabbitmq.username=admin
spring.rabbitmq.password=123456

# spring cloud stream
# 指定了消息获取的目的地，对应于MQ就是 exchange，这里的exchange就是 test-exchange
spring.cloud.stream.bindings.input.destination=test-exchange
```

##### bingding

需要定义一个接口，如下是内置的一个接口

```java
public interface Sink {
    String INPUT = "input";
    @Input("input")
    SubscribableChannel input();
}
```



##### controller

```java
@RestController
@RequestMapping("/consumer")
@EnableBinding(Sink.class)
public class ConsumerController {
	// 监听 binding 为 Sink.INPUT 的消息
    @StreamListener(ISink.INPUT)
    public void input(Message<String> message) {
        System.out.println("监听收到：" + message.getPayload());
   }
}
```

#### 自定义消息通道

##### 定义一个接口

```java
public interface OrderProcessor {
    String INPUT_ORDER = "inputOrder";
    String OUTPUT_ORDER = "outputOrder";
    @Input(INPUT_ORDER)
    SubscribableChannel inputOrder();
    @Output(OUTPUT_ORDER)
    MessageChannel outputOrder();
}
```

##### application.properties

增加如下配置

```properties
spring.cloud.stream.bindings.inputOrder.destination=test-exchange-2
spring.cloud.stream.bindings.outputOrder.destination=test-exchange-2
```

##### 使用

使用时，需要在 @EnableBinding 注解中，添加自定义的接口。

使用 @StreamListener 做监听的时候，需要指定 OrderProcessor.INPUT_ORDER

#### 消息分组

当同一个服务启动多个实例的时候，这些实例都会绑定到同一个消息通道的目标主题（Topic）上。默认情况下，当生产者发出一 条消息到绑定通道上，这条消息会产生多个副本被每个消费者实例接收和处理，但是有些业务场景之 下，我们希望生产者产生的消息只被其中一个实例消费，这个时候我们需要为这些消费者设置消费组来实现这样的功能。

我们只需要在服务消费者端设置 spring.cloud.stream.bindings.input.group 属性即可

##### application.properties

```properties
spring.cloud.stream.bindings.inputOrder.destination=test-exchange
spring.cloud.stream.bindings.inputOrder.group=group-2
#spring.cloud.stream.binders.defaultRabbit.type=rabbit
```

在同一个 group 中的多个消费者只有一个可以获取到消息并消费

#### 消息分区

##### 消费者

###### application.properties

在服务消费者端增加配置

```properties
# 通过该参数开启消费者分区功能
spring.cloud.stream.bindings.inputOrder.consumer.partitioned=true
# 该参数指定了当前消费者的总实例数量
spring.cloud.stream.instanceCount=2
# 该参数设置当前实例的索引号，从0开始，最大值为
# spring.cloud.stream.instanceCount 参数 - 1。我们试验的时候需要启动多个实例，可以通过
# 运行参数来为不同实例设置不同的索引值。
spring.cloud.stream.instanceIndex=0
```

生产者

###### application.properties

在服务生产者端增加配置

```properties
# 通过该参数指定了分区键的表达式规则，我们可以根据实际的输出消息规则来配置SpEL来生成合适的分区键
spring.cloud.stream.bindings.outputOrder.producer.partitionKeyExpression=payload
# 该参数指定了消息分区的数量
spring.cloud.stream.bindings.outputOrder.producer.partitionCount=2
```

这样当同一个消息被发给消费组时，我们可以发现只有一个消费实例在接收和处理这些相同的消息。



### Kafka





## 配置中心

### Spring Cloud Conﬁg

#### 概述

##### 简介

Spring Cloud Config项目是一个解决分布式系统的配置管理方案。它包含了Client和Server两个部分， server提供配置文件的存储、以接口的形式将配置文件的内容提供出去，client通过接口获取数据、并 依据此数据初始化自己的应用。

Spring Cloud Config为分布式系统中的外部配置提供服务器和客户端支持。使用Config Server，您可 以为所有环境中的应用程序管理其外部属性。它非常适合spring应用，也可以使用在其他语言的应用 上。随着应用程序通过从开发到测试和生产的部署流程，您可以管理这些环境之间的配置，并确定应用 程序具有迁移时需要运行的一切。服务器存储后端的默认实现使用git，因此它轻松支持标签版本的配置 环境，以及可以访问用于管理内容的各种工具。 

Spring Cloud Config服务端特性： 

- HTTP，为外部配置提供基于资源的API（键值对，或者等价的YAML内容） 

- 属性值的加密和解密（对称加密和非对称加密）

- 通过使用@EnableConfigServer在Spring boot应用中非常简单的嵌入。 

Config客户端的特性（特指Spring应用）

-  绑定Config服务端，并使用远程的属性源初始化Spring环境。 

-  属性值的加密和解密（对称加密和非对称加密）

#### 准备工作

##### 在github上创建一个项目

shop-service-config-repo

##### 并将配置文件上传

shop-service-order-dev.properties

shop-service-product-dev.properties

shop-service-user-dev.properties

##### 文件命名规则：

{application}-{profile}.yml 

{application}-{profile}.properties 

application为应用名称 

profile指的开发环境（用于区分开发环境，测试环境、生产环境等）

#### 创建配置中心服务端

##### pom.xml

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

##### application.properties

```properties
spring.http.encoding.force=true
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
server.tomcat.uri-encoding=UTF-8
server.port=9002
spring.application.name=shop-server-config

# spring cloud config
# 配置git服务地址
spring.cloud.config.server.git.uri=https://github.com/imyxiong/shop-service-config-repo.git
# 配置git用户名
spring.cloud.config.server.git.username=imyxiong
# 配置git密码
spring.cloud.config.server.git.password=p159357.
```

##### 配置启动类

```java
@SpringBootApplication
@EnableConfigServer //开启配置中心服务端功能
public class ShopServerConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopServerConfigApplication.class, args);
	}
}

```

##### 测试

启动此微服务，可以在浏览器上，通过访问http://localhost:9002/配置文件名(如:shop-service-user-dev.properties)访问到git服务器上的文件。



#### 修改使用配置中心的微服务

##### pom.xml

增加依赖包

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

##### application.properties

将原有的application.properties配置文件上传至github并删除

##### boostrap.properties

新建加载级别更高的boostrap.properties配置文件并配置如下

```properties
spring.cloud.config.name=shop-service-user
spring.cloud.config.profile=dev
spring.cloud.config.label=master
spring.cloud.config.uri=http://localhost:9002
```



#### 配置中心服务端高可用

##### 服务端改造

创建多个spring.application.name相同的配置中心

###### application.properties

改造如下,增加eureka配置

```properties
spring.http.encoding.force=true
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
server.tomcat.uri-encoding=UTF-8
server.port=9003
spring.application.name=shop-server-config

# spring cloud config
# 配置git服务地址
spring.cloud.config.server.git.uri=https://github.com/imyxiong/shop-service-config-repo.git
# 配置git用户名
spring.cloud.config.server.git.username=imyxiong
# 配置git密码
spring.cloud.config.server.git.password=p159357.

#eureka
# 配置eureka server的路径
eureka.client.serviceUrl.defaultZone=http://localhost:9000/eureka/
# 使用ip注册
eureka.instance.preferIpAddress=true
# 指定本服务请求ip,不指定时为本机ip
eureka.instance.ipAddress=127.0.0.1
# 为了更好的对所有服务进行定位，微服务注册到Eureka Server的时候可以手动配置示例ID
eureka.instance.instanceId=${spring.cloud.client.ip-address}:${server.port}
```



##### 客户端改造

###### bootstrap.properties

改造如下,增加eureka配置和从eureka中获取配置中心信息

```properties
# eureka
# 将eureka集群的url配置到服务模块
eureka.client.serviceUrl.defaultZone=http://localhost:9000/eureka/
# spring cloud config
spring.cloud.config.name=shop-service-user
spring.cloud.config.profile=dev
spring.cloud.config.label=master
spring.cloud.config.uri=http://localhost:9002
# 从eureka中获取配置中心信息
spring.cloud.config.discovery.enabled=true 
spring.cloud.config.discovery.serviceId=shop-server-config
```

#### 消息总线bus



### Apollo



### Disconf



### Nacos

[Nacos](https://spring-cloud-alibaba-group.github.io/zh-cn/docs/next/user-guide/nacos/quick-start)



#### 导包

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```



#### 配置

创建配置文件 boostrap.yaml

只保留以下配置

```yaml
spring:
  application:
    name: User
  profiles:
    active: dev
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        file-extension: yaml
        namespace: ed2069f3-cc54-4a62-b044-95061643ae84
        group: MY_GROUP
        username: nacos
        password: xx
```



在 Nacos -> 配置管理 -> 配置列表 中新建对应的主配置和环境配置

DataId 的格式如下

${prefix}-${spring.profile.active}.${file-extension]

prefix:  默认为 spring.application.name 的值，也可以通过配置项 spring.cloud.nacos.config.prefix 来配置

spring.profile.active: 为当前环境对应的 profile

file-extension: 配置内容的数据格式，目前只支持 properties 和 yaml 类型。要与 boostrap.yaml 中的一致



Group

要与 boostrap.yaml 中的一致



## 分布式事务

[Spring Cloud Alibaba-----整合Seata_springcloud整合seata_皆是梦的博客-CSDN博客](https://blog.csdn.net/weixin_43994761/article/details/131533629)

一次业务操作需要跨多个数据源或需要跨多个系统进行远程调用，就会产生分布式事务问题。



### 分布式事务的理论基础

​		解决分布式事务，也有相应的规范和协议。分布式事务相关的协议有 2PC、3PC。由于三阶段提交协议3PC非常难实现，目前市面主流的分布式事务解决方案都是 2PC 协议。



#### 2PC 两阶段提交协议

2PC(两阶段提交，Two-Phase Commit)，顾名思义，分为两个阶段：Prepare 和 Commit。



##### Prepare

提交事务请求

1. 询问：协调者向所有参与者发送事务请求，询问是否可执行事务操作，然后等待各个参与者的响应。
2. 执行：各个参与者接收到协调者事务请求后，执行事务操作（例如更新一个关系型数据库表中的记录），并将 Undo 和 Redo 信息记录事务日志中。
3. 响应：如果参与者成功执行了事务并写入 Undo 和 Redo 信息，则向协调者返回 YES 响应，否则返回 NO 响应。
    当然，参与者也可能宕机，从而不会返回响应。



##### Commit

执行事务提交，执行事务提交分为正常提交和回滚两种情况。



###### 正常提交事务

1. Commit 请求：协调者向所有参与者发送 Commit 请求。
2. 事务提交：参与者收到 Commit 请求后，执行事务提交，提交完成后释放事务执行期占用的所有资源。
3. 反馈结果：参与者执行事务提交后向协调者发送 Ack 响应。
4. 完成事务：接收到所有参与者的 Ack 响应后，完成事务提交。



###### 回滚事务

​		在执行 Prepare 步骤过程中，如果某些参与者执行事务失败、宕机或与协调者之间的网络中断，那么协调者就无法收到所有参与者的 YES 响应，或者某个参与者返回了 No 响应，此时，协调者就会进入回滚流程，对事务进行回滚。

1. Rollback 请求：协调者向所有参与者发送 Rollback 请求。
2. 事务回滚：参与者收到 Rollback 后，使用 Prepare 阶段的 Undo 日志执行事务回滚，完成后释放事务执行期占用的所有资源。
3. 反馈结果：参与者执行事务回滚后向协调者发送 Ack 响应。
4. 中断事务：接收到所有参与者的 Ack 响应后，完成事务中断。



##### 2PC 的问题

1. 同步阻塞：参与者在等待协调者的指令时，其实是在等待其他参与者的响应，在此过程中，参与者是无法进行其他操作的，也就是阻塞了其运行。 倘若参与者与协调者之间网络异常导致参与者一直收不到协调者信息，那么会导致参与者一直阻塞下去。
2. 单点：在 2PC 中，一切请求都来自协调者，所以协调者的地位是至关重要的，如果协调者宕机，那么就会使参与者一直阻塞并一直占用事务资源。如果协调者也是分布式，使用选主方式提供服务，那么在一个协调者挂掉后，可以选取另一个协调者继续后续的服务，可以解决单点问题。但是，新协调者无法知道上一个事务的全部状态信息（例如已等待 Prepare 响应的时长等），所以也无法顺利处理上一个事务。
3. 数据不一致：Commit 事务过程中 Commit /Rollback 请求可能因为协调者宕机或协调者与参与者网络问题丢失，那么就导致了部分参与者没有收到 Commit/Rollback 请求，而其他参与者则正常收到执行了 Commit/Rollback 操作，没有收到请求的参与者则继续阻塞。这时，参与者之间的数据就不再一致了。当参与者执行 Commit/Rollback 后会向协调者发送 Ack，然而协调者不论是否收到所有的参与者的 Ack，该事务也不会再有其他补救措施了，协调者能做的也就是等待超时后像事务发起者返回一个“我不确定该事务是否成功”。
4. 环境可靠性依赖：协调者 Prepare 请求发出后，等待响应，然而如果有参与者宕机或与协调者之间的网络中断，都会导致协调者无法收到所有参与者的响应，那么在 2PC 中，协调者会等待一定时间，然后超时后，会触发事务中断，在这个过程中，协调者和所有其他参与者都是出于阻塞的。这种机制对网络问题常见的现实环境来说太苛刻了。



### 常见分布式事务解决方案

1. AT（Seata）
2. TCC（消息队列）
3. Saga
4. XA

​		他们都是两阶段(2PC)。两阶段是指完成整个分布式事务，划分成两个步骤完成。实际上，这四种常见的分布式事务解决方案，分别对应着分布式事务的四种模式：AT、TCC、Saga、XA；四种分布式事务模式，都有各自的理论基础，分别在不同的时间被提出；每种模式都有它的适用场景，同样每个模式也都诞生有各自的代表产品；而这些代表产品，可能就是我们常见的（全局事务、基于可靠消息、最大努力通知、TCC）。

#### AT 模式(auto transcation)

AT 模式是一种无侵入的分布式事务解决方案，Seata 实现了该模式。
在 AT 模式下，用户只需关注自己的业务 SQL，用户的业务 SQL 作为一阶段，Seata 框架会自动生成事务的二阶段提交和回滚操作。

##### 一阶段

1. Seata 会拦截业务 SQL，解析 SQL 语义，找到业务 SQL 要更新的业务数据，在业务数据被更新前，将其保存成 before image
2. 执行业务 SQL 更新业务数据，在业务数据更新之后
3. 其保存成 after image，最后生成行锁

以上操作全部在一个数据库事务内完成，这样保证了一阶段操作的原子性



##### 二阶段

提交

如果顺利提交的话，因为业务 SQL 在一阶段已经提交至数据库，所以 Seata 框架只需将一阶段保存的快照数据和行锁删掉，完成数据清理即可

回滚

Seata 就需要回滚一阶段已经执行的业务 SQL，还原业务数据。回滚方式便是用 before image 还原业务数据；但在还原前要首先要校验脏写，对比 数据库当前业务数据 和 after image，如果两份数据完全一致就说明没有脏写，可以还原业务数据，如果不一致就说明有脏写，出现脏写就需要转人工处理。



#### TCC 模式

1. 侵入性比较强， 并且得自己实现相关事务控制逻辑
2. 在整个过程基本没有锁，性能更强
   TCC 模式需要用户根据自己的业务场景实现 Try、Confirm 和 Cancel 三个操作；事务发起方在一阶段执行 Try 方式，在二阶段提交执行 Confirm 方法，二阶段回滚执行 Cancel 方法。



TCC 三个方法描述：

- Try：资源的检测和预留；
- Confirm：执行的业务操作提交；要求 Try 成功 Confirm 一定要能成功；
- Cancel：预留资源释放；



##### 两阶段设计

​		用户接入 TCC ，最重要的是考虑如何将自己的业务模型拆成两阶段来实现。以扣钱场景为例，在接入 TCC 前，对 A 账户的扣钱，只需一条更新账户余额的 SQL 便能完成；但是在接入 TCC 之后，用户就需要考虑如何将原来一步就能完成的扣钱操作，拆成两阶段，实
现成三个方法，并且保证一阶段 Try 成功的话 二阶段 Confirm 一定能成功。

​		Try 方法作为一阶段准备方法，需要做资源的检查和预留。在扣钱场景下，Try 要做的事情是就是检查账户余额是否充足，预留转账资金，预留的方式就是冻结 A 账户的转账资金。Try 方法执行之后，账号 A 余额虽然还是 100，但是其中 30 元已经被冻结了，不能被其他事务使用。

​		二阶段 Confirm 方法执行真正的扣钱操作。Confirm 会使用 Try 阶段冻结的资金，执行账号扣款。Confirm 方法执行之后，账号 A 在一阶段中冻结的 30 元已经被扣除，账号 A 余额变成 70 元 。

​		如果二阶段是回滚的话，就需要在 Cancel 方法内释放一阶段 Try 冻结的 30 元，使账号 A 的回到初始状态，100 元全部可用。用户接入 TCC 模式，最重要的事情就是考虑如何将业务模型拆成 2 阶段，实现成 TCC 的 3 个方
法，并且保证 Try 成功 Confirm 一定能成功。

相对于 AT 模式，TCC 模式对业务代码有一定的侵入性，但是 TCC 模式无 AT 模式的全局行锁，TCC 性能会比 AT 模式高很多。



##### 并发控制



##### 允许空回滚

​		Cancel 接口设计时需要允许空回滚。在 Try 接口因为丢包时没有收到，事务管理器会触发回滚，这时会触发 Cancel 接口，这时 Cancel 执行时发现没有对应的事务 xid 或主键时，需要返回回滚成功。让事务服务管理器认为已回滚，否则会不断重试，而 Cancel 又没有对应的业务数据可以进行回滚。



##### 防悬挂控制

​		悬挂的意思是：Cancel 比 Try 接口先执行，出现的原因是 Try 由于网络拥堵而超时，事务管理器生成回滚，触发 Cancel 接口，而最终又收到了 Try 接口调用，但是 Cancel 比 Try 先到。按照前面允许空回滚的逻辑，回滚会返回成功，事务管理器认为事务已回滚成功，则此时的 Try 接口不应该执行，否则会产生数据不一致，所以我们在 Cancel 空回滚返回成功之前先记录该条事务 xid 或业务主键，标识这条记录已经回滚过，Try 接口先检查这条事务xid或业务主键如果已经标记为回滚成功过，则不执行 Try 的业务操作。



##### 幂等控制

​		幂等性的意思是：对同一个系统，使用同样的条件，一次请求和重复的多次请求对系统资源的影响是一致的。因为网络抖动或拥堵可能会超时，事务管理器会对资源进行重试操作，所以很可能一个业务操作会被重复调用，为了不因为重复调用而多次占用资源，需要对服务设计时进行幂等控制，通常我们可以用事务 xid 或业务主键判重来控制。



#### Saga 模式

Saga 理论出自 Hector & Kenneth 1987发表的论文 Sagas。
Saga 模式的实现，是长事务解决方案。
Saga 是一种补偿协议，在 Saga 模式下，分布式事务内有多个参与者，每一个参与者都是一个冲正补偿服务，需要用户根据业务场景实现其正向操作和逆向回滚操作。

分布式事务执行过程中，依次执行各参与者的正向操作，如果所有正向操作均执行成功，那么分布式
事务提交。如果任何一个正向操作执行失败，那么分布式事务会退回去执行前面各参与者的逆向回滚操作，回滚已提交的参与者，使分布式事务回到初始状态。
Saga 正向服务与补偿服务也需要业务开发者实现。因此是业务入侵的。
Saga 模式下分布式事务通常是由事件驱动的，各个参与者之间是异步执行的，Saga 模式是一种长事务解决方案。



##### 使用场景

Saga 模式适用于业务流程长且需要保证事务最终一致性的业务系统，Saga 模式一阶段就会提交本地事务，无锁、长流程情况下可以保证性能。

事务参与者可能是其它公司的服务或者是遗留系统的服务，无法进行改造和提供 TCC 要求的接口，可以使用 Saga 模式。



##### 优点

- 一阶段提交本地数据库事务，无锁，高性能；
- 参与者可以采用事务驱动异步执行，高吞吐；
- 补偿服务即正向服务的“反向”，易于理解，易于实现；



##### 缺点

Saga 模式由于一阶段已经提交本地数据库事务，且没有进行预留动作，所以不能保证隔离性。
与 TCC 实践经验相同的是，Saga 模式中，每个事务参与者的冲正、逆向操作，需要支持：

- 空补偿：逆向操作早于正向操作时；
- 防悬挂控制：空补偿后要拒绝正向操作
- 幂等



#### XA 模式

​		XA 是 X/Open DTP 组织（X/Open DTP group）定义的两阶段提交协议，XA 被许多数据库（如 Oracle、DB2、SQL Server、MySQL）和中间件等工具（如 CICS 和 Tuxedo）本地支持 。

X/Open DTP 模型（1994）包括应用程序（AP）、事务管理器（TM）、资源管理器（RM）。
XA 接口函数由数据库厂商提供。XA 规范的基础是两阶段提交协议 2PC。
JTA（Java Transaction API）是 Java 实现的 XA 规范的增强版接口。

​		在 XA 模式下，需要有一个[全局]协调器，每一个数据库事务完成后，进行第一阶段预提交，并通知协调器，把结果给协调器。协调器等所有分支事务操作完成、都预提交后，进行第二步；

​		第二步：协调器通知每个数据库进行逐个 commit/rollback。
其中，这个全局协调器就是 XA 模型中的 TM 角色，每个分支事务各自的数据库就是 RM。

MySQL 提供的XA实现（https://dev.mysql.com/doc/refman/5.7/en/xa.html）
XA 模式下的 开源框架有 atomikos，其开发公司也有商业版本。
XA 模式缺点：事务粒度大。高并发下，系统可用性低。因此很少使用。





#### 总结

四种分布式事务模式，分别在不同的时间被提出，每种模式都有它的适用场景

- AT 模式是无侵入的分布式事务解决方案，适用于不希望对业务进行改造的场景，几乎 0 学习成本。
- TCC 模式是高性能分布式事务解决方案，适用于核心系统等对性能有很高要求的场景。
- Saga 模式是长事务解决方案，适用于业务流程长且需要保证事务最终一致性的业务系统，Saga 模式一阶段就会提交本地事务，无锁，长流程情况下可以保证性能，多用于渠道层、集成层业务系统。事务参与者可能是其它公司的服务或者是遗留系统的服务，无法进行改造和提供 TCC 要求的接口，也可以使用 Saga 模式。
- XA 模式是分布式强一致性的解决方案，但性能低而使用较少



### Seata

[Seata](https://seata.io/zh-cn/index.html)

#### 概念

TC (Transaction Coordinator) - 事务协调者

维护全局和分支事务的状态，驱动全局事务提交或回滚。

TM (Transaction Manager) - 事务管理器

定义全局事务的范围：开始全局事务、提交或回滚全局事务。

RM (Resource Manager) - 资源管理器

管理分支事务处理的资源，与TC交谈以注册分支事务和报告分支事务的状态，并驱动分支事务提交或回滚。



#### 架构图

1.TM 向 TC 申请开启一个全局事务，全局事务创建成功并生成一个全局唯一的 XID

2.XID 在微服务调用链路的上下文中传播

3.RM 向 TC 注册分支事务，将其纳入 XID 对应全局事务的管辖

4.TM 向 TC 发起针对 XID 的全局提交或回滚决议（事务第一阶段结束）

5.TC 调度 XID 下管辖的全部分支事务完成提交或回滚请求（事务第二阶段结束）



![img](..\pictures\20230726\001.png)







#### 安装配置部署管理

示例版本 `1.7.0`

[SEATA1.6.1的配置与使用-阿里云开发者社区 (aliyun.com)](https://developer.aliyun.com/article/1223677)



##### 安装

下载的 [Seata](https://github.com/seata/seata/releases) 并解压缩



##### 配置

###### 应用配置 

conf/application.yml

```yaml
#  Copyright 1999-2019 Seata.io Group.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

server:
  port: 7091

spring:
  application:
    name: seata-server

logging:
  config: classpath:logback-spring.xml
  file:
    path: /var/log/seata
  extend:
    logstash-appender:
      destination: 127.0.0.1:4560
    kafka-appender:
      bootstrap-servers: 127.0.0.1:9092
      topic: logback_to_logstash

console:
  user:
    username: seata
    password: seata
seata:
  config:
    # support: nacos, consul, apollo, zk, etcd3
    type: nacos
    nacos:
      server-addr: 192.168.31.90:10668
      namespace: e7f09330-5809-43ba-bcc9-31f46580b9e9
      group: SEATA_GROUP
      # #if use MSE Nacos with auth, mutex with username/password attribute
      # access-key: ""
      # secret-key: ""
      # nacos dashboard 的账号密码
      username: nacos
      password: xx
      data-id: SeataServer.properties
      cluster: default
  registry:
    # support: nacos, eureka, redis, zk, consul, etcd3, sofa
    type: nacos
    nacos:
      # application=seata-tc-server
      server-addr: 192.168.31.90:10668
      namespace: e7f09330-5809-43ba-bcc9-31f46580b9e9
      group: SEATA_GROUP
      # nacos dashboard 的账号密码
      username: nacos
      password: xx
  # store:
    # support: file 、 db 、 redis
    # mode: db
  # server:
    # service-port: 8091 #If not configured, the default is '${server.port} + 1000'
  security:
    secretKey: SeataSecretKey0c382ef121d778043159209298fd40bf3850a017
    tokenValidityInMilliseconds: 1800000
    ignore:
      urls: /,/**/*.css,/**/*.js,/**/*.html,/**/*.map,/**/*.svg,/**/*.png,/**/*.jpeg,/**/*.ico,/api/v1/auth/login

```



###### 配置中心

[Seata-Seata的高可用异地容灾架构搭建](../files/HTML/Java/SpringCloud/Seata-Seata的高可用异地容灾架构搭建.pdf)

SeataServer.properties

```properties
# service.vgroupMapping.demo_spring_cloud_alibaba_tx_group=default
# 对应 seata server 的 seata.seaver.server-port
# service.default.grouplist=192.168.31.90:10535
# service.enableDegrade=false
# service.disableGlobalTransaction=false
# support: file 、 db 、 redis
store.mode=db
store.db.datasource=druid
store.db.dbType=mysql
store.db.driverClassName=com.mysql.cj.jdbc.Driver
store.db.url=jdbc:mysql://192.168.31.101:3306/seata?useSSL=false&serverTimezone=Hongkong&useUnicode=true&characterEncoding=utf-8&autoReconnect=true
store.db.user=young
store.db.password=123456
store.db.minConn=10
store.db.maxConn=100
store.db.globalTable=global_table
store.db.branchTable=branch_table
store.db.lockTable=lock_table
store.db.distributedLockTable=distributed_lock
store.db.queryLimit=1000
store.db.maxWait=5000

# 事务、日志等配置
server.recoverycommittingRetryPeriod=3000
server.recoveryasynCommittingRetryPeriod=3000
server.recoveryrollbackingRetryPeriod=3000
server.recoverytimeoutRetryPeriod=3000
server.maxCommitRetryTimeout=-1
server.maxRollbackRetryTimeout=-1
server.rollbackRetryTimeoutUnlockEnable=false
server.undologSaveDays=7
server.undologDeletePeriod=86400000

security.secretKey=SeataSecretKey0c382ef121d778043159209298fd40bf3850a017
security.tokenValidityInMilliseconds=1800000
security.ignore.urls=/,/**/*.css,/**/*.js,/**/*.html,/**/*.map,/**/*.svg,/**/*.png,/**/*.jpeg,/**/*.ico,/api/v1/auth/login

# 客户端与服务端传输方式
transport.serialization=seata
transport.compressor=none

# RM配置
client.rm.asyncCommitBufferLimit=10000
client.rm.lock.retryInterval=10
client.rm.lock.retryTimes=30
client.rm.lock.retryPolicyBranchRollbackOnConflict=true
client.rm.reportRetryCount=5
client.rm.tableMetaCheckEnable=false
client.rm.tableMetaCheckerInterval=60000
client.rm.sqlParserType=druid
client.rm.reportSuccessEnable=false
client.rm.sagaBranchRegisterEnable=false
# TM配置
client.tm.commitRetryCount=5
client.tm.rollbackRetryCount=5
client.tm.defaultGlobalTransactionTimeout=60000
client.tm.degradeCheck=false
client.tm.degradeCheckAllowTimes=10
client.tm.degradeCheckPeriod=2000

# undo日志配置
client.undo.dataValidation=true
client.undo.logSerialization=jackson
client.undo.onlyCareUpdateColumns=true
client.undo.logTable=undo_log
client.undo.compress.enable=true
client.undo.compress.type=zip
client.undo.compress.threshold=64k
client.log.exceptionRate=100

# 关闭metrics功能，提高性能
metrics.enabled=false
metrics.registryType=compact
metrics.exporterList=prometheus
metrics.exporterPrometheusPort=9898
```



###### 数据库配置

使用 [script/server/db/mysql.sql](../files/sql/seata.server.init.sql) 初始化数据库 spring_cloud_alibaba_seata



###### 启动脚本配置修改

bin/seata-server.sh

```shell
# 前面加上
LOG_DIR="/var/log/seata"

# 此处参数：-Dskywalking.logging.dir 由 ${BASEDIR}/logs 改为 ${LOG_DIR}
SKYWALKING_OPTS="-javaagent:${BASEDIR}/ext/apm-skywalking/skywalking-agent.jar -Dskywalking_config=${BASEDIR}/ext/apm-skywalking/config/agent.config -Dskywalking.logging.dir=${LOG_DIR}"

# 这里修改一下堆大小
JAVA_OPT="${JAVA_OPT} -server -Dloader.path=${LOADER_PATH:="$BASEDIR/lib"} -Xmx${JVM_XMX:="1024m"} -Xms${JVM_XMS:="1024m"} -Xss${JVM_XSS:="512k"} -XX:SurvivorRatio=10 -XX:MetaspaceSize=${JVM_MetaspaceSize:="256m"} -XX:MaxMetaspaceSize=${JVM_MaxMetaspaceSize:="256m"} -XX:MaxDirectMemorySize=${JVM_MaxDirectMemorySize:=1024m} -XX:-OmitStackTraceInFastThrow -XX:-UseAdaptiveSizePolicy"

# 此处参数：-XX:HeapDumpPath 由 ${BASEDIR}/logs 改为 ${LOG_DIR}
JAVA_OPT="${JAVA_OPT} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${BASEDIR}/logs/java_heapdump.hprof -XX:+DisableExplicitGC"

# 此处参数：-Xlog:gc*:file、-Xlog:gc=trace:file、-Xloggc 由 ${BASEDIR}/logs 改为 ${LOG_DIR}
if [[ "$JAVA_MAJOR_VERSION" -ge "9" ]] ; then
  JAVA_OPT="${JAVA_OPT} -Xlog:gc*:file=${LOG_DIR}/seata_gc.log:time,tags:filecount=10,filesize=102400"
elif [[ "$JAVA_MAJOR_VERSION" -ge "17" ]] ; then
  JAVA_OPT="${JAVA_OPT} -Xlog:gc=trace:file=${LOG_DIR}/seata_gc.log:time,tags:filecount=10,filesize=10M"
else
  JAVA_OPT="${JAVA_OPT} -Xloggc:${LOG_DIR}/seata_gc.log -verbose:gc -XX:+PrintGCDetails  -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC"
fi

# 注释掉这几句
# if [ ! -x "$BASEDIR"/logs ]; then
#   mkdir "$BASEDIR"/logs
# fi

CMD_LINE_ARGS=$@

# 此处后台输出日志文件目录 由 ${BASEDIR}/logs 改为 ${LOG_DIR}
# start
echo "$JAVACMD ${JAVA_OPT} ${CMD_LINE_ARGS}" > ${LOG_DIR}/start.out 2>&1 &
nohup $JAVACMD ${JAVA_OPT} ${CMD_LINE_ARGS} >> ${LOG_DIR}/start.out 2>&1 &
echo "seata-server is starting, you can check the ${LOG_DIR}/start.out"
```



##### 创建用户（组）

```shell
sudo groupadd -r apps
sudo useradd -r -g apps -s /bin/false seata

sudo mkdir /var/log/seata/
sudo chown -R seata:root /opt/apps/seata
sudo chown -R seata:root /var/log/seata
```



##### 服务管理

手动

```shell
# cd /opt/apps/seata
sudo -u seata /opt/apps/seata/bin/seata-server.sh
# 停止
ps aux | grep 'seata'
sudo kill -9 PID
```



自动

编写[脚本](../files/scripts/server/seata.server)文件放在 base_dir 目录下 `/opt/apps/seata/seata.server`

配置参考 Linux -> 服务管理  -> 系统启动时自动运行服务



启动之后在浏览器打开[链接](http://localhost:7091)。

端口号和访问链接的前缀可以在配置文件 **conf/application.properties** 中修改



##### Seata 高可用集群

1.修改应用端口为 7090

2.配置后在各个节点服务器输入命令启动所有服务

3.配置 nginx 负载均衡

```properties
http {
    upstream seatacluster {
        server 192.168.31.101:7090;
        server 192.168.31.102:7090;
        server 192.168.31.103:7090;
    }

    # another virtual host using mix of IP-, name-, and port-based configuration
    #
    server {
        listen       7091;
        server_name  localhost;

        location / {
            proxy_pass http://seatacluster/;
        }
    }
}

stream {

    upstream seatacluster_server {
        server 192.168.31.101:8090 max_fails=3 fail_timeout=10s;
        server 192.168.31.102:8090 max_fails=3 fail_timeout=10s;
		server 192.168.31.103:8090 max_fails=3 fail_timeout=10s;
    } 

    server {
        listen 8091;
        proxy_connect_timeout 20s;
        proxy_timeout 5m;
        proxy_pass seatacluster_server;
    }

}
```



#### SpringCloud 整合 Seata

##### 配置

```yaml
seata:
  tx-service-group: demo_spring_cloud_alibaba_tx_group
  service:
    vgroup-mapping:
      demo_spring_cloud_alibaba_tx_group: default
    default:
      # 对应 TC 服务的 IP 及 seata.server.server-port 配置的端口
      grouplist: 192.168.31.101:8091
```



##### Seata 代理数据源

导包

```xml
<!-- seata 版本与 seata server 版本一致-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
    <exclusions>
        <exclusion>
            <groupId>io.seata</groupId>
            <artifactId>seata-spring-boot-starter</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>io.seata</groupId>
    <artifactId>seata-spring-boot-starter</artifactId>
    <version>1.7.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
</dependency>
```



###### MybatisPlusConfig

```java
@Configuration
@MapperScan({"com.example.order.mapper"})
public class MybatisPlusConfig {
}
```



###### DataSourceConfig

```java
import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import io.seata.rm.datasource.DataSourceProxy;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
public class DataSourceConfig {

    @Value("${mybatis-plus.mapper-location}")
    private String mapperLocations;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource druidDataSource(){
        return new DruidDataSource();
    }

    @Bean
    public DataSourceProxy dataSourceProxy(DataSource dataSource){
        return new DataSourceProxy(dataSource);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSourceProxy") DataSource dataSourceProxy) throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSourceProxy);
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(mapperLocations));
        sqlSessionFactoryBean.setTransactionFactory(new SpringManagedTransactionFactory());
        return sqlSessionFactoryBean.getObject();
    }
}
```



##### undo_log

在服务所在数据库创建 undo_log 表

```sql
-- for AT mode you must to init this sql for you business database. the seata server not need it.
CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id`     BIGINT(20)   NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(100) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
AUTO_INCREMENT = 1
DEFAULT CHARSET = utf8 COMMENT ='AT transaction mode undo table';
```



https://blog.csdn.net/weixin_43994761/article/details/131533629



## 服务性能

### Jmetter

Apache JMeter是Apache组织开发的基于Java的压力测试工具。用于对软件做压力测试，它最初被设计 用于Web应用测试，但后来扩展到其他测试领域。 它可以用于测试静态和动态资源，例如静态文件、 Java 小服务程序、CGI 脚本、Java 对象、数据库、FTP 服务器， 等等。JMeter 可以用于对服务器、网 络或对象模拟巨大的负载，来自不同压力类别下测试它们的强度和分析整体性能。另外JMeter能够对应 用程序做功能/回归测试，通过创建带有断言的脚本来验证你的程序返回了你期望的结果。为了最大限 度的灵活性，JMeter允许使用正则表达式创建断言。

#### 下载安装

打开[Jmetter下载页面](https://downloads.apache.org/jmeter/binaries/)进行下载，解压后找到安装目录下 bin/jmeter.bat以管理员身份启动即可。

#### 使用Jmetter

1、File -> New 创建一个测试计划

2、对测试计划右键 Add -> Threads(Users) -> Thread Group

3、设置Thread Group -> Thread Properties

Number of Threads(users) :线程数

Ramp-up period(seconds):决定多长时间启动所有线程,如果ramp-up period 为零， JMeter 将立即建立所有线程

3、对Thread Group右键 Add -> Simpler -> HTTP Request

4、配置接口信息

5、对 HTTP Request 右键 Add -> Lisenter 添加（View Result Tree、Aggregate Report、Graph Results)

6、点击Start按钮开始发起请求



# 鉴权

## SSO(单点登录)

### 简介

多系统，单一位置登录，实现多系统访问的一种技术。



### Session

#### Session 跨域

​		所谓 Session 跨域就是摒弃了系统提供的 Session，而使用自定义的类似 Session 的机制来保存客户端数据的一种解决方案。

​		如：通过设置 cookie 的 domain 来实现 cookie 的跨域传递。在 cookie 中传递一个自定义的 session_id。这个session_id 是客户端的唯一标记。将这个标记作为 key，将客户端需要保存的数据作为 value,在服务端进行保存（数据库保存或 NoSQL 保存）。这种机制就是 Session 的跨域解决。

​		什么是域：在应用模型，一个完整的，有独立访问路径的功能集合称为一个域。如：百度称为一个应用或系统。百度下有若干的域，如：搜索引擎 (www.baidu.com)，百度贴吧(tieba.baidu.com)，百度知道 (zhidao.baidu.com)，百度地图 (map.baidu.com)等。域信息有时也称为多级域名。域的划分: 以IP，端口，域名，主机名为标准，实现划分。

​		什么是跨域：客户端请求的时候，请求的服务器，不是同一个 IP，端口，域名，主机名的时候，都称为跨域。



#### Spring Session 共享

​		spring-session 技术是 spring 提供的用于处理集群会话共享的解决方案。spring-session 技术是将用户 session 数据保存到三方存储容器中，如：mysql，redis 等。

​		spring-session 技术是解决同域名下的多服务器集群 session 共享问题的。不能解决跨域 session 共享问题。

​		使用：配置一个 Spring 提供的 Filter，实现数据的拦截保存，并转换为 spring-session 需要的会话对象。必须提供一个数据库的表格信息（由 spring-session 提供，找 spring-session-jdbc.jar/org/springframework/session/jdbc/*.sql，根据具体的数据库找对应的SQL 文件，做表格的创建）。

​		spring-session 表：保存客户端 session 对象的表格。

​		spring-session-attributes 表：保存客户端 session 中的 attributes 属性数据的表格。

​		spring-session 框架，是结合 Servlet 技术中的 HTTPSession 完成的会话共享机制。在代码中是直接操作 HttpSession 对象的。

![](../pictures/202312/001.png)



#### Nginx Session 共享

​		nginx 中的 ip_hash 技术能够将某个 ip 的请求定向到同一台后端，这样一来这个 ip 下的某个客户端和某个后端就能建立起稳固的 session，ip_hash 是在 upstream 配置中定义的，具体如下：

```properties
http {
    ...
    upstream nacoscluster {
        server 192.168.31.100:8840;
        server 192.168.31.100:8842;
        server 192.168.31.100:8844;
        ip_hash;
    }

    server {
        listen       8848;
        server_name  localhost;

        location / {
            proxy_pass http://nacoscluster/;
            proxy_set_header Host $http_host;
            proxy_set_header Cookie $http_cookie;
            proxy_set_header X-Real-lP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            client_max_body_size 100m;
        }
    }
    
    ...

}

```



​		ip_hash 是容易理解的，但是因为仅仅能用 ip 这个因子来分配后端，因此 ip_hash 是有缺陷的，不能在一些情况下使用：

​		nginx 不是最前端的服务器。

​		ip_hash 要求 nginx 一定是最前端的服务器，否则 nginx 得不到正确 ip，就不能根据 ip 作 hash。警如使用的是 squid 为最前端，那么 nginx 取 ip 时只能得到 squid 的服务器 ip 地址，用这个地址来作分流是肯定错乱的。

​		假如 nginx 后端又有其它负载均衡，将请求又通过另外的方式分流了，那么某个客户端的请求肯定不能定位到同一台 session 应用服务器上。



### Token 机制

#### 传统身份认证

​		HTTP 是一种没有状态的协议，也就是它并不知道是谁是访问应用。这里我们把用户看成是客户端，客户端使用用户名还有密码通过了身份验证，不过下回这个客户端再发送请求的时候，还得再验证一下。

​		解决的方法就是，当用户请求登录的时候，如果没有问题，我们在服务端生成一条记录，这个记录里可以说明一下登录的用户是谁，然后把这条记录的 ID 号发送给客户端，客户端收到以后把这个 ID 号存储在 Cookie 里，下次这个用户再向服务端发送请求的时候，可以带着这个 Cookie ，这样服务端会验证一个这个 Cookie 里的信息，看看能不能在服务端这里找到对应的记录，如果可以，说明用户已经通过了身份验证，就把用户请求的数据返回给客户端。

​		上面说的就是 Session，我们需要在服务端存储为登录的用户生成的 Session ，这些 Session 可能会存储在内存，磁盘，或者数据库里。我们可能需要在服务端定期的去清理过期的 Session 。

​		这种认证中出现的问题是：

​		Session：每次认证用户发起请求时，服务器需要去创建一个记录来存储信息。当越来越多的用户发请求时，内存的开销也会不断增加。

​		可扩展性：在服务端的内存中使用 Session 存储登录信息，伴随而来的是可扩展性问题。

​		CORS（跨域资源共享）：当我们需要让数据跨多台移动设备上使用时，跨域资源的共享会是一个让人头疼的问题。在使用 Ajax 抓取另一个域的资源，就可以会出现禁止请求的情况。

​		CSRF（跨站请求伪造）：用户在访问银行网站时，他们很容易受到跨站请求伪造的攻击，并且能够被利用其访问其他的网站。

​		在这些问题中，可扩展性是最突出的。因此我们有必要去寻求一种更有行之有效的方法。



#### Token 身份认证

使用基于 Token 的身份验证方法，在服务端不需要存储用户的登录记录。大概的流程是这样的：

1. 客户端使用用户名、密码请求登录
2. 服务端收到请求，去验证用户名、密码
3. 验证成功后，服务端会签发一个 Token，再把这个 Token 发送给客户端
4. 客户端收到 Token 以后可以把它存储起来，比如放在 Cookie（可以实现跨域传递数据） 里或者 Local Storage（私有的本地存储，无法实现跨域） 里
5. 客户端每次向服务端请求资源的时候需要带着服务端签发的 Token 
6. 服务端收到请求，然后去验证客户端请求里面带着的 Token，如果验证成功，就向客户端返回请求的数据

使用 Token 验证的优势

- 无状态、可扩展：在客户端存储的 Token 是无状态的，并且能够被扩展。基于这种无状态和不存储 Session 信息，负载均衡器能够将用户信息从一个服务传到其他服务器上。
- 安全性：请求中发送 token 而不再是发送 cookie 能够防止 CSRF（跨站请求伪造）。即使在客户端使用 cookie 存储 token，cookie 也仅仅是一个存储机制而不是用于认证。不将信息存储在 Session 中，让我们少了对 Session 操作。



### JWT（Json Web Token）机制

https://jwt.io

​		JSON Web Token (JWT) 是一个开放的行业标准(RFC 7519)，它定义了一种简介的、自包合的协议格式，用于在通信双方传递 JSON 对象，传递的信息经过数字签名可以被验证和信任。JWT 可以使用 HMAC 算法或使用 RSA 的公钥/私钥对来签名，防止被算改。

​		紧凑：数据小，可以通过 URL，POST 参数，请求头发送。且数据小代表传输速度快.

​		自包含：使用 payload 数据块记录用户必要且不隐私的数据，可以有效的减少数据库访问次数，提高代码性能。

​		JWT 一般用于处理用户身份验证或数据信息交换

​		用户身份验证，一旦用户登录，每个后续请求都将包含 JWT，允许用户访问该令牌允许的路由，服务和资源。单点登录是当今广泛使用 JWT 的一项功能，因为它的开销很小，并且能够轻松地跨不同域使用。

​		数据信息交换：JWT 是一种非常方便的多方传递数据的载体，因为其可以使用数据签名来保证数据的有效性和安全性。

#### JWT 数据结构

JWT 的数据结构是：header.payload.signature。 由字符点`.`来分隔三部分数据。

- header: 头信息
- payload: 有效荷载
- signature: 签名



##### header

数据结构：

```json
{
    "alg":"加密算法名称",
    "typ":"JWT"
}
```

- alg 是加密算法定义内容，如：HMAC、SHA256 或 RSA
- typ 是 token 类型，这里固定为 JWT



##### payload

​		在 payload 数据块中一般用于记录实体（通常为用户信息）或其他数据的。主要分为三个部分，分别是：已注册信息（registered claims），公开数据（public claims），私有数据（private claims） 。

标准中注册的声明（建议但不强制使用）

```
iss:jwt签发者
sub:jwt所面向的用户
aud:接收iwt的一方
exp:jwt的过期时间，这个过期时间必须要大于签发时间
nbf:定义在什么时间之前，该jwt都是不可用的.
iat:jwt的签发时间
jti:jwt的唯一身份标识，主要用来作为一次性token,从而回避重放攻击
```

公共的声明

​		公共的声明可以添加任何的信息，一般添加用户的相关信息或其他业务需要的必要信息但不建议添加敏感信息，因为该部分在客户端可解密。

私有的声明

​		私有声明是提供者和消费者所共同定义的声明，一般不建议存放敏感信息，因为 base64 是对称解密的，意味着该部分信息可以归类为明文信息。

​		这个指的就是自定义的claim。比如下面那个举例中的name都属于自定的claim，这些claim跟JWT标准规定的claim区别在于:JWT规定的claim，JWT的接收方在拿到JWT之后，都知道怎么对这些标准的claim进行验证(还不知道是否能够验证);而private claims不会验证，除非明确告诉接收方要对这些claim进行验证以及规则才行。

```
{
    "sub":“1234567898",
    "name":"John Doe",
    "1at": 1516239022
}
```

其中 sub 是标准的声明，name 是自定义的声明（公共的或私有的）

然后将其进行 base64 编码得到 jwt 的第二部分：eyJzdWIi0iIxMjMONTY30DkwIiwibmFtZSI6IkphbWVzliwiYWRtaW4i0nRydwV9

> 即使 JWT 有签名加密机制，但是 payload 内容都是明文记，除非记录的是加密，否则不排除泄露隐私最据的可能。不在payload 中记录任何敏感数据。



##### signature

​		签名信息。这是一个由开发者提供的信息。是服务器验证的传递的数据是否有效安全的标准。在生成 JWT 最终数据的之前。先使用 header 中定义的加密算法，将 header 和 payload 进行加密，并使用点进行连接。如：加密后的 head，加密后的 payload。再使用相同的加密算法，对`密后的 head.加密后的 payload`进行加密。得到最终结果。



#### JWT 执行流程

![](..//pictures/202312/002.png)



#### 注意事项

使用 JWT 实现单点登录时，需要注意 token 时效性。token 是保存在客户端的令牌数据如果永久有效，则有被劫持的可能。token 在设计的时候，可以考虑一次性有效或一段时间内有效。如果设置有效时长，则需要考虑是否需要刷新 token 有效期问题。



##### token 保存位置

使用 JWT 技术生成的 token,客户端在保存的时候可以考虑 Cookie 或 Local Storage。Cookie 保存方式，可以实现跨域传递数据。localStorage ,是域私有的本地存储，无法实现跨域。



##### Web Storage

​		Web Storage 可保存的数据容量为 5M。且只能存储符串数据。

​		Web Storage 分为 Local Storage 和 Session Storage。

​		Local Storage 的生命周期是永久的，关闭页面或览器之后，Local Storage 中的数据也不会消失。Local Storage 除非主动删除数据，否则数据永远不会消失。

​		Session Storage 是会话相关的本地存储单元，生命周期是在在当前会话下有效。Session Storage 引入了一个“浏览器窗口”的概念，Session Storage 是在同源的窗口中始终存在的数据。只要这个浏览器窗口没有关闭，即使刷新页面或者进入同源另一个页面，数据依然存在。但是 Session Storage 在关闭了浏览器窗口后就会被销毁。同时独立的打开同一个窗口同一个页面，Session Storage 也是不一样的。



#### JJWT

https://github.com/jwtk/jjwt

[JJWT使用完全指南 (xjx100.cn)](http://lihuaxi.xjx100.cn/news/1475247.html?action=onClick)

##### 简介

JJWT 是一个提供端到端的 JWT 创建和验证的 Java 库。永远免费和开源(Apache License，版本2.0)，JJWT 很容易使用和理解。它被设计成一个以建筑为中心的流畅界面，隐藏了它的大部分复杂性。



###### JJWT 签名算法

| 标示符 | 签名算法                                          |
| ------ | ------------------------------------------------- |
| HS256  | 使用SHA-256的HMAC                                 |
| HS384  | 使用SHA-384的HMAC                                 |
| HS512  | 使用SHA-512的HMAC                                 |
| ES256  | 使用P-256 和SHA-256的ECDSA                        |
| ES384  | 使用P-384 和SHA-384的ECDSA                        |
| ES512  | 使用P-512 和SHA-512的ECDSA                        |
| RS256  | 使用SHA-256的RSASSA-PKCS-v1_5                     |
| RS384  | 使用SHA-384的RSASSA-PKCS-v1_5                     |
| RS512  | 使用SHA-512的RSASSA-PKCS-v1_5                     |
| PS256  | RSASSA-PSS using SHA-256 and MGF1 with SHA-256    |
| PS384  | RSASSA-PSS using SHA-384 and MGF1 with SHA-384    |
| PS512  | RSASSA-PSS using SHA-512 and MGF1 with SHA-512    |
| EdDSA  | Edwards-Curve Digital Signature Algorithm (EdDSA) |

如果有以下使用场景，需要额外导入 bcprov-jdk15on 的库

- JDK 10或更早的版本， 需要使用RSASSA-PSS (PS256, PS384, PS512) 签名算法
- JDK 10或更早的版本， 需要使用EdECDH（X25519 或 X448）椭圆曲线 Diffie-Hellman 加密。
- JDK14或更早版本， 需要使用EdDSA (Ed25519 or Ed448) 椭圆曲线签名算法
  注意：如果是JDK15及之后的版本， 则不需要添加

```xml
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk15on</artifactId>
    <version>1.70</version>
    <scope>runtime</scope>
</dependency>
```



##### 依赖

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.12.3</version>
</dependency>
```



```java
MacAlgorithm alg = Jwts.SIG.HS256; //or HS384 or HS512
SecretKey key = alg.key().build();
```



##### 生成 JWT

```java
private String genToken(SecretKey key){

    JwtBuilder jwtBuilder = Jwts.builder()
        .header()
        .add("aHeaderName", "aHeaderValue")
        .add(new HashMap<>())
        .and()
        .id("888")
        .subject("Rose")
        .issuedAt(new Date())
        .signWith(key)
        .expiration(new Date(System.currentTimeMillis() + 60 * 1000)) // token 过期时间，不设置则不会过期
        .claim("logo", "logo.jpg") //自定 claim
        .claims(new HashMap<>());

    String token = jwtBuilder.compact();
    System.out.println(token);

    String[] split = token.split("\\.");
    System.out.println(Base64.getDecoder().decode(split[0]));
    System.out.println(Base64.getDecoder().decode(split[1]));
    // System.out.println(Base64.getDecoder().decode(split[2]));
    return token;
}
```



##### 解析 JWT

```java
public void parseToken(String token, SecretKey key){
    System.out.println("------------------------------>parseToken");
    Jws<Claims> jwt = Jwts.parser()
            .verifyWith(key) // <---- publicKey, not privateKey
            .build()
            .parseSignedClaims(token);
    System.out.println("header:alg -> " + jwt.getHeader().get("alg"));
    System.out.println("header:aHeaderName -> " + jwt.getHeader().get("aHeaderName"));
    System.out.println("payload:id -> " + jwt.getPayload().getId());
    System.out.println("payload:subject -> " + jwt.getPayload().getSubject());
    System.out.println("payload:issuedAt -> " + jwt.getPayload().getIssuedAt());
    System.out.println("payload:logo -> " + jwt.getPayload().get("logo"));

}
```



##### 密钥生成方式

```java
// 1.使用原始密码生成 key
Password key = Keys.password(password.toCharArray());
.signWith(key)
.verifyWith(key)

// 2.生成一个足够安全的 SecretKey 
SecretKey key = Jwts.SIG.HS256.key().build();
.signWith(key)
.verifyWith(key)
// 将 SecretKey 转为字符串以保存
String secretString = Encoders.BASE64.encode(key.getEncoded());
// 字符串转为 SecretKey
SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretString));

// 3.生成一对非对称密钥
// or RS384, RS512, PS256, etc...
KeyPair keyPair = Jwts.SIG.RS256.keyPair().build();
.signWith(keyPair.getPrivate())
.verifyWith(keyPair.getPublic())
```



### 加密算法

#### 单向加密（签名算法）

在理论上，从明文加密到密文后，不可反向解密的。可以从迭代和加盐的方式尽可能保证加密数据不可反向解密。

传递敏感数据的时候使用的。如：密码。

在金融相关交易中，用户密码是敏感数据，其他数据是非敏感数据。所有的金融相关的应用中，客户端都有一个独立的密码输入控件。这个控件就是做单向加密的。

使用单向加密的时候，传递的数据只有密文，没有明文，也没有密钥。



##### MD5





#### 双向加密

是可以实现加密和解密双向运算的算法。需要通过密钥实现加解密计算的。

密钥种类

- 公钥：可以对外公开的，就是可以在网络中传递的
- 私钥：必须保密的，绝对不会对外暴露的

在传递安全数据的时候使用。所谓安全数据，就是不可篡改的数据。如:金融交易中的收款人卡号，转账的金额，货币的种类等。

使用双向加密的时候，传递的有明文，密文，公钥。



##### 对称加密

公钥和私钥是同一个密钥的加密方式



###### DES

DES 的密文是非定长密文。根据明文数据和 key 数据动态伸缩的。



###### AES

AES 的 key 要求长度为 16。



##### 非对称加密

公钥和私钥不是同一个密钥的加密方式



###### RSA





## OAuth2.0

### 简介

​		第三方认证技术方案最主要是解决认证协议的通用标准问题，因为要实现跨系统认证，各系统之间要遵循一定的接口协议。

​		OAuth 协议为用户资源的授权提供了一个安全的、开放而又简易的标准。同时，任何第三方都可以使用 OAuth 认证服务，任何服务提供商都可以实现自身的 OAUTH 认证服务，因而 OAuth 是开放的。业界提供了 OAuth 的多种实现如 PHP、JavaScript，Java，Ruby 等各种语言开发包，大大节约了程序员的时间，因而 OAuth 是简易的。互联网很多服务如 Open API，很多大公司如 Google，Yahoo，Microsoft 等都提供了 OAuth 认证服务，这些都足以说明 OAuth 标准逐渐成为开放资源授权的标准。

​		OAuth（开放授权Open Authorization）是一个开放的授权标准，允许用户让第三方应用访问该用户在某一web服务上存储的私密的资源（如照片，视频，联系人列表），而无需将用户名和密码提供给第三方应用。如网站通过微信、微博登录等，主要用于第三方登录。

​		OAuth 允许用户提供一个令牌，而不是用户名和密码来访问他们存放在特定服务提供者的数据。每一个令牌授权个特定的第三方系统（例如，视频编辑网站）在特定时段（例如，接下来的2小时内）内访问特定的资源（例如仅仅是某一相册中的视频）。这样，OAuth 让用户可以授权第三方网站访问他们存储在另外服务提供者的某些特定信息，而非所有内容。

​		OAuth 协议目前发展到 2.0 版本，1.0 版本过于复杂，2.0 版本已得到广泛应用。

参考: https://baike.baidu.com/item/oAuth/7153134

OAuth 协议: https://tools.ietf.org/html/rfc6749



### 认证流程

OAuth2.0 认证流程如下

引自 OAuth2.0 协议 rfc6749 https://tools.ietf.org/html/rfc6749

```
     +--------+                               +---------------+
     |        |--(A)- Authorization Request ->|   Resource    |
     |        |                               |     Owner     |
     |        |<-(B)-- Authorization Grant ---|               |
     |        |                               +---------------+
     |        |
     |        |                               +---------------+
     |        |--(C)-- Authorization Grant -->| Authorization |
     | Client |                               |     Server    |
     |        |<-(D)----- Access Token -------|               |
     |        |                               +---------------+
     |        |
     |        |                               +---------------+
     |        |--(E)----- Access Token ------>|    Resource   |
     |        |                               |     Server    |
     |        |<-(F)--- Protected Resource ---|               |
     +--------+                               +---------------+
```

#### 客户端

​		本身不存储资源，需要通过资源拥有者的授权去请求资源服务器的资源，比如: Android 客户端、Web 客户端（浏览器端）、微信客户端等。

#### 资源拥有者

​		通常为用户，也可以是应用程序，即该资源的拥有者。

#### 授权服务器（也称认证服务器）

​		用来对资源拥有的身份进行认证、对访问资源进行授权。客户端要想访问资源需要通过认证服务器由资源拥有者授权后方可访问。

#### 资源服务器

​		存储资源的服务器，比如，网站用户管理服务器存储了网站用户信息，网站相册服务器存储了用户的相册信息，微信的资源服务存储了微信的用户信息等。客户端最终访问资源服务器获取资源信息。

> 资源服务器和认证服务器可以是一个服务也可以分开的服务，如果是分开的服务资源服务器通堂要请求认证服务器来校验令牌的合法性。



#### 例子

下边分析一个 OAuth2.0 认证的例子，网站使用微信认证的过程

```mermaid
sequenceDiagram
participant 用户
participant 网站
participant 微信认证
participant 微信用户
用户 ->> 网站: 访问登陆页面
网站 ->> 微信认证: 请求认证
微信认证 ->> 用户: 授权页面
用户 ->> 微信认证: 用户授权
网站 ->> 微信认证: 申请令牌
微信认证 ->> 网站: 返回令牌
网站 ->> 微信用户: 请求获取用户信息
微信用户 ->> 微信用户: 校验令牌的合法性
微信用户 ->> 网站: 响应用户信息
网站 ->> 用户: 显示用户信息
```





### 常用术语

- 客户凭证（client Credentials）：客户端的 clientld 和密码用于认证客户
- 令牌（tokens）：授权服务器在接收到客户请求后，颁发的访问令牌
- 作用域（scopes）：客户请求访问令牌时，由资源拥有者额外指定的细分权限（permission）



### 令牌类型

- 授权码：仅用于授权码授权类型，用于交换获取访问令牌和刷新令牌
- 访问令牌：用于代表一个用户或服务直接去访问受保护的资源
- 刷新令牌：用于去授权服务器获取一个刷新访问令牌
- BearerToken：不管谁拿到Token都可以访问资源，类似现金
- Proof of Possesson(PoP) Token：可以校验 client 是否对 Token 有明确的拥有权



### 特点

​		这种基于 OAuth 的认证机制适用于个人消费者类的互联网产品，如社交类APP等应用，但是不太适合拥有自有认证权限管理的企业应用。

#### 优点

- 更安全，客户端不接触用户密码，服务器端更易集中保护
- 广泛传播并被持续采用
- 短寿命和封装的 token
- 资源服务器和授权服务器解耦
- 集中式授权，简化客户端
- HTTP/JSON 友好，易于请求和传递 token
- 考虑多种客户端架构场景
- 客户可以具有不同的信任级别



#### 缺点

- 协议框架太宽泛，造成各种实现的兼容性和互操作性差
- 不是一个认证协议，本身并不能告诉你任何用户信息



### 授权模式

#### 授权码模式（Authorization Code）

常用

```
 +----------+
     | Resource |
     |   Owner  |
     |          |
     +----------+
          ^
          |
         (B)
     +----|-----+          Client Identifier      +---------------+
     |         -+----(A)-- & Redirection URI ---->|               |
     |  User-   |                                 | Authorization |
     |  Agent  -+----(B)-- User authenticates --->|     Server    |
     |          |                                 |               |
     |         -+----(C)-- Authorization Code ---<|               |
     +-|----|---+                                 +---------------+
       |    |                                         ^      v
      (A)  (C)                                        |      |
       |    |                                         |      |
       ^    v                                         |      |
     +---------+                                      |      |
     |         |>---(D)-- Authorization Code ---------'      |
     |  Client |          & Redirection URI                  |
     |         |                                             |
     |         |<---(E)----- Access Token -------------------'
     +---------+       (w/ Optional Refresh Token)
```



#### 简化授权模式（Implicit）

```
     +----------+
     | Resource |
     |  Owner   |
     |          |
     +----------+
          ^
          |
         (B)
     +----|-----+          Client Identifier     +---------------+
     |         -+----(A)-- & Redirection URI --->|               |
     |  User-   |                                | Authorization |
     |  Agent  -|----(B)-- User authenticates -->|     Server    |
     |          |                                |               |
     |          |<---(C)--- Redirection URI ----<|               |
     |          |          with Access Token     +---------------+
     |          |            in Fragment
     |          |                                +---------------+
     |          |----(D)--- Redirection URI ---->|   Web-Hosted  |
     |          |          without Fragment      |     Client    |
     |          |                                |    Resource   |
     |     (F)  |<---(E)------- Script ---------<|               |
     |          |                                +---------------+
     +-|--------+
       |    |
      (A)  (G) Access Token
       |    |
       ^    v
     +---------+
     |         |
     |  Client |
     |         |
     +---------+
```



#### 密码模式（Resource Owner Password Credentials）

```
  +----------+
     | Resource |
     |  Owner   |
     |          |
     +----------+
          v
          |    Resource Owner
         (A) Password Credentials
          |
          v
     +---------+                                  +---------------+
     |         |>--(B)---- Resource Owner ------->|               |
     |         |         Password Credentials     | Authorization |
     | Client  |                                  |     Server    |
     |         |<--(C)---- Access Token ---------<|               |
     |         |    (w/ Optional Refresh Token)   |               |
     +---------+                                  +---------------+
```



#### 客户端模式（Client Credentials）

```
     +---------+                                  +---------------+
     |         |                                  |               |
     |         |>--(A)- Client Authentication --->| Authorization |
     | Client  |                                  |     Server    |
     |         |<--(B)---- Access Token ---------<|               |
     |         |                                  |               |
     +---------+                                  +---------------+
```



#### 刷新令牌（Refresh Tokens）

当 Access Token 过期之后需要使用 Access Token 去 Authorization Server 获取新的 Access Token。不需要重新走一遍授权流程。

```
  +--------+                                           +---------------+
  |        |--(A)------- Authorization Grant --------->|               |
  |        |                                           |               |
  |        |<-(B)----------- Access Token -------------|               |
  |        |               & Refresh Token             |               |
  |        |                                           |               |
  |        |                            +----------+   |               |
  |        |--(C)---- Access Token ---->|          |   |               |
  |        |                            |          |   |               |
  |        |<-(D)- Protected Resource --| Resource |   | Authorization |
  | Client |                            |  Server  |   |     Server    |
  |        |--(E)---- Access Token ---->|          |   |               |
  |        |                            |          |   |               |
  |        |<-(F)- Invalid Token Error -|          |   |               |
  |        |                            +----------+   |               |
  |        |                                           |               |
  |        |--(G)----------- Refresh Token ----------->|               |
  |        |                                           |               |
  |        |<-(H)----------- Access Token -------------|               |
  +--------+           & Optional Refresh Token        +---------------+
```





## 安全框架

### 概述

​	什么是安全框架？解决系统安全问题的框架。如果没有安全框架，我们需要手动处理每个资源的访问控制，非常麻烦。使用安全框架，我们可以通过配置的方式实现对资源的访问限制。



### 常用安全框架

- Spring Security：Spring 家族一员。是一个能够为基于 Spring 的企业应用系统提供声明式的安全访问控制解决方案的安全框架。它提供了一组可以在 Spring 应用上下文中配置的 Bean，充分利用了 Spring IoC，DI（控制反转Inversion of Control,DI:Dependency Injection 依赖注入）和AOP（面向切面编程）功能，为应用系统提供声明式的安全访问控制功能，减少了为企业系统安全控制编写大量重复代码的工作。

  

- Apache Shiro：一个功能强大且易于使用的 Java 安全框架,提供了认证、授权、加密和会话管理。



### SpringSecurity

#### 简介

​		Spring Security 是一个高度自定义的安全框架。利用 Spring oC/DI 和 AOP 功能，为系统提供了声明式安全访问控制功能，减少了为系统安全而编写大量重复代码的工作。使用 Spring Secruity 的原因有很多，但大部分都是发现了 JavaEE 的 Servlet 规范或 EJ 规范中的安全功能缺乏典型企业应用场景。同时认识到他们在 WAR 或 EAR 级别无法称植。因此如果你更换服务器环境，还有大量工作去重新配置你的应用程序。使用 Spring Security 解决了这些问题也为你提供许多其他有用的、可定制的安全功能。正如你可能知道的两个应用程序的两个主要区域是“认证“和“授权”（或者访问控制）。这两点也是 Spring Security 重要核心功能。“认证”，是建立一个他声明的主体的过程（一个“主体”一般是指用户，设备或一些可以在你的应用程序中执行动作的其他系统），通俗点说就是系统认为用户是否能登录。“授权”指确定一个主体是否允许在你的应用程序执行一个动作的过程。通俗点进就是系统判断用户是否有权限去做某些事情。

​		Spring Security 以“The Acegi Secutity System for Spring”的名字始于 2003 年年底。其前身为 acegi 项目。起因是 Spring 开发者邮件列表中一个问题，有人提问是否考虑提供一个基于 Spring 的安全实现。限制于时间问题，开发出了一个简单的安全实现，但是并没有深入研究。几周后，Spring 杜区中其他成员同样询问了安全问题，代码提供给了这些人。2004 年 01 月份已经有 20 人左右使用这个项目。随着更多人的加入，在 2004 年 03 月左右在 sourceforge 中建立了一个项目，在最开始并没有认证模块，所有的认证功能都是依赖容器完成的，而 acei 则注重授权。但是随着更多人的使用，基于容器的认证就显现出了不足。acegi 中也加入了认证功能。大约 1 年后 acegi 成为 Spring 子项目在 2006 年 05 月发布了 acegi 1.0.0 版本。2007 年底 acegi 更名为 Spring Security。



#### 依赖

```xml
<!-- Spring Security组件-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<!--web组件-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```



#### 自定义登录逻辑



##### UserDetailsService

使用 username 调用 UserDetailsService 的 loadUserByUsername 返回 UserDetails(impl by User)，

loadUserByUsername 根据用户名查找用户信息

找到用户信息之后使用 PasswordEncoder(impl by BCryptPasswordEncoder) 对登陆密码进行编码和匹配数据库密码，匹配得上则创建 User 并返回，否则抛出异常





##### PasswordEncoder

Spring Security 要求容器中必须有 PasswordEncoder 实例。所以当自定义登录逻辑时要求必须给容器注入PaswordEncoder 的 bean 对象。

在Spring Security 中内置了很多解析器。推荐使用 `BCryptPasswordEncoder` 实现类。

###### 接口介绍

BCryptPasswordEncoder 是 Spring Security 官方推荐的密码解析器，平时多使用这个解析器 BCryptPasswordEncoder 是对 bcrypt 强散列方法的具体实现。是基于 Hash 算法实现的单向加密。可以通过 strength 控制加密强度，默认 10。

**encode**

`String encode(CharSequence rawPassword);`

对登陆密码按照特定的编码规则进行编码，推荐使用SHA-A 或者 Hash 加 8 byte 盐/随机盐。



**matches**

`boolean matches(CharSequence rawPassword, String encodedPassword);`

验证从存储中获取的编码密码与登录的原始密码是否匹配。如果密码匹配，则返回
`true` 如果不匹配，则返回 `false`。第一个参数表示登录的原始密码。第二个参数表示存储的密码。



**upgradeEncoding**

`default boolean upgradeEncoding(String encodedPassword)`

对登陆密码进行二次加密

如果编码之后的密码能够再次进行编码且达到更安全的结果则返回 `true`，否则返回 `false`。
默认返回 `false`。



##### 编码

###### 配置

```yaml
server:
  port: 9009
  servlet:
    context-path: /common/auth
```



###### 配置类

​		当进行自定义登录逻辑时需要用到 UserDetailsService 和 PasswordEncoder。但是 Spring Security 要求：当进行自定义登录逻辑时容器内必须有 PasswordEncoder 实例。所以不能直接 new 对象。

```java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 表单提交
        http.formLogin()
                // 自定义登灵页面
                .loginPage("/login.html")
                // UsernamePasswordAuthenticationFilter
                // 自定义表单账号参数名
                // .usernameParameter("username")
                // 自定义表单密码参数名
                // .passwordParameter("password")
                // 必须和表单提交的接口一样，会去执行自定义登灵逻辑
                .loginProcessingUrl("/login")
                // 登录成功后跳转的页面，POST 请求
//                .successForwardUrl("/login/success_forward")
                // 自定义登陆成功处理器
                .successHandler(new MyAuthenticationSuccessHandler("/common/auth/main.html"))
                // 登录失败后跳转的页面，POST 请求
//                .failureForwardUrl("/login/failure_forward");
                // 自定义登录失败处理器
                .failureHandler(new MyAuthenticationFailureHandler("/common/auth/error.html"));

        // 授权
        http.authorizeRequests()
                // 放行 /login.html，不需要认证
                .antMatchers("/login.html").permitAll()
                // 放行 /error.html，不需要认证
                .antMatchers("/error.html").permitAll()
                // 所有请求都必须认证才能访问，必须登录，anyRequest 要放在最后
                .anyRequest().authenticated();

        // 关闭 csrf 防护
        http.csrf().disable();
    }
}

```



```java
@Configuration
public class EncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
```



###### 登录逻辑

```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //1.根据用户名去数据库查询，如果不存在抛UsernameNotFoundException异常
        if (!"admin".equals(username)) {
            throw new UsernameNotFoundException("用户名不存在");
        }
        //2.比较密码(注册时已经加密过)，如果匹配成功返回UserDetails
        String rawPassword = "1234";
        String passwordFromStorage = passwordEncoder.encode(rawPassword);
        System.out.println("自定义登录");
        System.out.println(passwordFromStorage);
        return new User(username, passwordFromStorage,AuthorityUtils.commaSeparatedStringToAuthorityList("admin,normal"));
    }
}

```



###### 页面转跳

```java
@Controller
@RequestMapping("/login")
public class LoginController {

    @PostMapping("/success_forward")
//    @ResponseBody
    public String successForward(){
        return "redirect:/main.html";
    }

    @PostMapping("/failure_forward")
//    @ResponseBody
    public String failureForward(){
        return "redirect:/error.html";
    }
}
```



###### 登录页面

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
    <body>
        <form action="/common/auth/login" method="post">
            用户名: <input type="text" name="username"/><br/>
            密码: <input type="password" name="password"/><br/>
            <input type="submit" value="登录"/>
        </form>
    </body>
</html>
```



###### 错误页面

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
    <body>
        操作失败，请重新登录 <a href=“/login.html“>跳转</a>
    </body>
</html>
```



###### 主页面

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
    <body>
        登陆成功！！！
    </body>
</html>
```



###### 自定义登陆成功处理器

```java
public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private String url;

    public MyAuthenticationSuccessHandler(String url) {
        this.url = url;
    }

    @Override
    public void onAuthenticationSuccess (HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        System.out.println("自定义登陆成功处理器");
        User user = (User) authentication.getPrincipal();
        System.out.println(user.getUsername());
        // 输出nul1
        System.out.println(user.getPassword());
        System.out.println(user.getAuthorities());
        response.sendRedirect(url);
    }
}
```



###### 自定义登录失败处理器

```java
public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private String url;

    public MyAuthenticationFailureHandler(String url) {
        this.url = url;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest reguest, HttpServletResponse response, AuthenticationException exception) throws IOException {
        System.out.println("自定义登录失败处理器");
        response.sendRedirect(url);
    }
}
```



#### 访问控制 url 匹配

​		认证中所有常用配置，主要是对 `http.formLogin()` 进行操作。而在配置类中 `http.authorizeRequests()` 主要是对 url 进行控制，也就是我们所说的授权（访问控制）。`http,authorizeRequests()` 也支持连缀写法，总体公式为：

url 匹配规则.权限控制方法

​		通过上面的公式可以有很多 url 匹配规则和很多权限控制方法。这些内容进行各种组合就形成了 Spring Security 中的授权。

​		在所有匹配规则中取所有规则的交集。配置顺序影响了之后授权效果，范围越小的应该放在前面，范围越大的应该放到后面。



##### anyRequest()

​		 `anyRequest()`，表示匹配所有的请求。一般情况下此方法都会使用，设置全部内容都需要进行认证。

```java
.anyRequest().authenticated();
```



##### antMatcher()

方法定义如下

```java
public C antMatchers(String ...antPatterns);
public C antMatchers(HttpMethod method, String ...antPatterns);
```

参数是不定向参数，每个参数是一个 ant 表达式，用于匹配 url 规则

规则如下:

- `?`：匹配一个字符
- `*`：匹配 0 个或多个字符
- `**`：匹配 0 个或多个目录

在实际项目中经常需要放行所有静态资源，

例如放行 `js` 文件夹下所有文件。

```java
.antMatchers("/js/**"，"/css/**").permitAll()
```

还有一种配置方式是只要是 `.js` 文件都放行

```java
.antMatchers("/**/*.js").permitAll()
```



##### regexMatcher()

方法定义如下

```java
public C regexMatcher(String ...regexPatterns);
public C regexMatcher(HttpMethod method, String ...regexPatterns);
```

​		使用正则表达式进行匹配。和 `antMatohers()` 主要的区别就是参数，`antMatchers()` 参数是 ant 表达式 `regexMatchers()` 参数是正则表达式。

例如所有以  `.js` 结尾的文件都被放行

```java
regexMatchers(".+[.]js").permitAll();
```



##### 两个参数时使用方式

​		无论是 `antMatchers()` 还是 `regexMatchers()` 都具有两个参数的方法，其中第一个参数都是 `HttpMethod` 表示请求方式，当设置了 `HttpMethod` 后表示只有特定的请求方式才执行对应的权限设置。

枚举类型 `HttpMethod` 内置属性如下：

```java
public enum HttpMethod {
    GET,
    HEAD,
    POST,
    PUT,
    PATCH,
    DELETE,
    OPTIONS,
    TRACE;
}
```



##### mvcMatcher()





#### 内置权限控制

permitAll

denyAll

anonymous

authenticated

fullyAuthenticated

rememberMe



#### 角色权限判断

​		除了上面的内置权限控制。Spring Security 中还支持很多其他权限控制，这些方法一般都用于用户已经被认证后，判断用户是否具有特定的要求。

##### hasAuthority(String)

​		判断用户是否具有特定的权限，用户的权限是在自定义登录逻辑中创建 User 对象时指定的。下图中 admin 和 normal 就是用户的权限。admin 和 normal 严格区分大小写。

```java
return new User(username, passwordFromStorage, AuthorityUtils.commaSeparatedStringToAuthorityList("admin,normal"));
```

在配置类中通过 `hasAuthority("admin")` 设置具有 admin 权限时才能访问。

```java
.antMatchers("/main1.html").hasAuthority("admin")
```



##### hasAnyAuthority(String ...)

​		如果用户具备给定权限中某一个，就允许访问。

下面代码中由于大小写和用户的权限不相同，所以用户无权访问

```java
.antMatchers("/main1.html").hasAuthority("adMin”“admin")
```



##### hasRole(String)

​		如果用户具备给定角色就允许访问。否则出现 403。

参数取值来源于自定义登录逻辑 UserDetailsService 实现类中创建 User 对象时给 User 赋予的授权，角色名称以 `ROLE_`开头的才为角色。

示例

```java
.antMatchers("/main1.html").hasRole("admin")
```



##### hasAnyRole(String)

​		如果用户具备给定角色中某一个，就允许访问。

示例

```java
.antMatchers("/main1.html").hasAnyRole("adMin”“admin")
```



#### IP 权限判断



##### hasIpAddress(String)



#### 自定义 403 处理

##### handler

```java
@Component
public class MyAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // 响应状态
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        // 返回 json 格式
        response.setHeader("Content-Type","application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.write("{\"status\":\"error\",\"msg\":\"没有权限\"}");
        writer.flush();
        writer.close();
    }
}

```



##### 配置

```java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AccessDeniedHandler accessDeniedHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 异常处理
        http.exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler);

        // 关闭 csrf 防护
        http.csrf().disable();
    }
}
```



#### 基于表达式的访问控制

之前的用户权限判断实际上底层实现都是调用 access (表达式)

可以通过 access() 实现和之前的权限控制完成相同的功能。

以 `hasRole` 和 `permitAll` 举例

```java
// login.html不需要被认证
// .antMatchers("/Login.html").permitAll()
.antMatchers("/login.html").access("permitAll")
// error.htmL不需要被认证
.antMatchers("/error.html").permitAl1()
// .antMatchers("/main.html").hasRole(“abc");
// 不能带上前缀 ROLE_
.antMatchers("/main.html").access("hasRole('abc')");
```



##### 自定义 access

```java
.anyRequest().access("@myServiceImpl.hasPermission(request, authentication)");
```



```java
public interface MyService{
    boolean hasPermission(HttpServletRequest request，Authenticationauthentication);
}

@Service
public class MyServiceImpl implements MyService {
    @Override
    public boolean hasPermission(HttpServletRequest request，Authenticationauthentication){
        // 获取主体
        Object obj= authentication.getPrincipal();
    	// 判断主体是否属于 UserDetails
        if (obj instanceof UserDetails){
            // 获取权限
            UserDetails userDetails = (UserDetails) obj;
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
            // 判断请求的URI是否在权限里
            return authorities.contains(new SimpleGrantedAuthority(request.getRequestURI()));
        }
        return false;
    }
}
```



#### 基于注解的访问控制

​		在 Spring Security 中提供了一些访问控制的注解。这些注解都是默认是都不可用的，需要通过 @EnableGlobalMethodSecurity 进行开启后使用。

​		如果设置的条件允许，程序正常执行。如果不允许会报 500

`org.springframework.security.access.AccessDeniedException` 不允许访问

​		这些注解可以写到 Service 接口或方法上，也可以写到 Controller 或 Controller 的方法上。通常情况下都是写在控制器方法上的，控制接口 URL 是否允许被访问。



##### @Secured

`@Secured` 是专门用于判断是否具有角色的。能写在方法或类上。参数要以 `ROLE_` 开头。

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Secured {
    /**
     *
     * Returns the list of security configuration attributes (e.g. ROLE_USER, ROLE_ADMIN)
     *
     * @return String[] The secure method attributes
     */
    public String[] value();
}
```



###### 开启`Secured`

在启动类（也可以在配置类等能够扫描的类上）上添加 `@EnableGlobalMethodSecurity(securedEnabled = true)`

```java
@EnableGlobalMethodSecurity(securedEnabled = true)
@SpringBootApplication
public class CommonAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonAuthApplication.class, args);
    }

}
```



###### 使用`Secured`

在控制器方法上添加 `@Secured` 注解

```java
/**
 * 成功后跳转页面
 */
@Secured("ROLE_abc")
@PostMapping("/success_forward")
public String successForward(){
    return "redirect:/main.html";
}
```



###### 配置类

```java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 表单提交
        http.formLogin()
                // 自定义登灵页面
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                // 登录成功后跳转的页面，POST 请求
                .successForwardUrl("/login/success_forward")
                // 登录失败后跳转的页面，POST 请求
                .failureForwardUrl("/login/failure_forward");
        ;

        // 授权
        http.authorizeRequests()
                // 放行 /login.html，不需要认证
                .antMatchers("/login.html").permitAll()
                // 放行 /error.html，不需要认证
                .antMatchers("/error.html").permitAll()
                // 所有请求都必须认证才能访问，必须登录，anyRequest 要放在最后
                .anyRequest().authenticated();

        // 关闭 csrf 防护
        http.csrf().disable();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
```



##### @PreAuthorize/@PostAuthorize

@PreAuthorize 和@PostAuthorize 都是方法或类级别注解。

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface PreAuthorize (
    /**
     * @returm the Spring-EL expression to be evaluated before invoking the protected
     * method
     */
    String value();
}
```

- `@PreAuthorize` 表示访问方法或类在执行之前先判断权限，大多情况下都是使用这个注解，注解的参数和 `access()` 方法参数取值相同，都是权限表达式。
- `@PostAuthorize` 表示方法或类执行结束后判断权限，此注解很少被便用到。



###### 开启注解

```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
@SpringBootApplication
public class CommonAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonAuthApplication.class, args);
    }

}

```



###### 使用注解

```java
/**
 * 成功后跳转页面
 */
// PreAuthorize 允许角色以 ROLE_ 开头，也可以不以 ROLE_ 开头，但是配置类不允许以 ROLE_ 开头
@PreAuthorize("ROLE_abc")
@PostMapping("/success_forward")
public String successForward(){
    return "redirect:/main.html";
}
```



###### 配置类

```java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 表单提交
        http.formLogin()
                // 自定义登灵页面
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                // 登录成功后跳转的页面，POST 请求
                .successForwardUrl("/login/success_forward")
                // 登录失败后跳转的页面，POST 请求
                .failureForwardUrl("/login/failure_forward");
        ;

        // 授权
        http.authorizeRequests()
                // 放行 /login.html，不需要认证
                .antMatchers("/login.html").permitAll()
                // 放行 /error.html，不需要认证
                .antMatchers("/error.html").permitAll()
                // 所有请求都必须认证才能访问，必须登录，anyRequest 要放在最后
                .anyRequest().authenticated();

        // 关闭 csrf 防护
        http.csrf().disable();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
```



#### RememberMe

​		Spring Security 中 Remember Me 为“记住我”功能，用户只需要在登录时勾选 remember-me 复选框，取值为 true。Spring Security 会自动把用户信息存储到数据源中，以后就可以不登录进行访问。



##### 依赖

​		Spring Security 实现 Remember Me 功能时底层实现依赖 Spring-JDBC，所以需要导入 Spring-JDBC。以后多使用 MyBatis 框架而很少直接导入 spring-JDBC，所以此处导入 mybatis 启动器同时还需要添加 MySQL 驱动。

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.3</version>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.31</version>
</dependency>
```



##### 数据源配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://192.168.31.101:3306/common_auth?useSSL=false&serverTimezone=Hongkong&useUnicode=true&characterEncoding=utf-8&autoReconnect=true
    username: young
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
```



##### 配置类配置

```java
@Configuration
public class RememberMeConfig {
    @Autowired
    private DataSource dataSource;

    @Bean
    public PersistentTokenRepository persistentTokenRepository(){
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        // 自动建表，第一次启动时需要，第二次启动时注释掉
        // jdbcTokenRepository.setCreateTableOnStartup(true);
        return jdbcTokenRepository;
    }
}

```



```java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private PersistentTokenRepository persistentTokenRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // .....
        // 记住我
        http.rememberMe()
                // 数据源
                .tokenRepository(persistentTokenRepository)
                // 参数
                // .rememberMeParameter()
                // 过期时间 60s(默认两周)
                 .tokenValiditySeconds(60)
                // 自定义登录逻辑
                 .userDetailsService(userDetailsService)
        ;

        // ......
    }
}
```



##### 页面

```html
记住我: <input type="checkbox" name="remember-me"/><br/>
```



#### Thymeleaf 中 SpringSecurity 的使用

​		Spring Security 可以在一些视图技术中进行控制显示效果。例如: JSP 或 Thymeleaf。在非前后端分离且使用 Spring Boot 的项目中多使用 Thymeleaf 作为视图展示技术。



##### 依赖

​		Thymeleaf 对 Spring Security 的支持都放在 thymeleaf-extras-springsecurityX 中，目前最新版本为5。
所以需要在项目中添加此 jar 包的依赖和 thymeleaf 的依赖。

```xml
<!-- thymeleaf springsecurity5 依赖 -->
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity5</artifactId>
</dependency>
<!-- thymeleaf依赖 -->
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

在 html 页面中引入 thymeleaf 命名空间和 security 命名空间

```xml
<html xmIns="http://www.w3.org/1999/xhtml"
      xmlns:th="http ://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity5">
```



##### 配置

```yaml
spring:
  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
    mode: HTML5
    encoding: UTF-8
    servlet:
      content-type: text/html
    # 启用模板缓存（开发时建议关闭）
    cache: false
```



##### 获取属性

可以在 html 页面中通过 sec:authentication=”“ 获取

UsernamePasswordAuthenticationToken 中所有 getXXX 的内容，包含父类中的 getXXX 的内容。

根据源码得出下面属性：

- `name`：登录账号名称
- `principal`：登录主体，在自定义登录逻辑中是 UserDetails
- `credentials`：凭证
- `authorities`：权限和角色
- `details`：实际上是 WebAuthenticationDetails 的实例。可以获取 remoteAddress（客户端 ip）和 sessionId（当前 sessionld）



##### demo.html

在项目 resources 中新建 templates 文件夹，在 templates 中新建 demo.html 页面

```html
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity5">
    <head>
        <meta charset="UTF-8">
        <title>Title</title>
    </head>
    <body>
        登录账号:<span sec:authentication="name"></span><br/>
        登录账号:<span sec:authentication="principal.username"></span><br/>
        凭证: <span sec:authentication="credentials"></span><br/>
        权限和角色:<span sec:authentication="authorities"></span><br/>
        客户端地址: <span sec:authentication="details.remoteAddress"></span><br/>
        brsessionId: <span sec:authentication="details.sessionId"></span><br/>

        通过权限判断:
        <button sec:authorize="hasAuthority('/insert')">新增</button>
        <button sec:authorize="hasAuthority('/delete')">删除</button>
        <button sec:authorize="hasAuthority('/update')">修改</button>
        <button sec:authorize="hasAuthority('/select')">查看</button>
        <br/>
        通过角色判断:
        <button sec:authorize="hasRole('abc')">新增</button>
        <button sec:authorize="hasRole('abc')">删除</button>
        <button sec:authorize="hasRole('abc')">修改</button>
        <button sec:authorize="hasRole('abc')">查看</button>
    </body>
</html>
```



##### Controller

```java
@Controller
@RequestMapping("/hello_world")
public class HelloWorldController {
    @GetMapping("/demo")
    public String demo(){
        return "demo";
    }
}
```



#### 退出登录

实现退出非常简单，只要在页面中添加 `/上下文路径/logout`  的超链接即可。

```html
<a href="/common/auth/logout">退出登录</a>
```

为了实现更好的效果，通常添加退出的配置。默认的退出 url 为 `/logout`，退出成功后重定向到 `/login?logout`

LogoutConfigurer

```java
public final class LogoutConfigurer<H extends HttpSecurityBuilder<H>> extends AbstractHttpConfigurer<LogoutConfigurer<H>, H> {
    private List<LogoutHandler> logoutHandlers = new ArrayList();
    private SecurityContextLogoutHandler contextLogoutHandler = new SecurityContextLogoutHandler();
    private String logoutSuccessUrl = "/login?logout";
    private LogoutSuccessHandler logoutSuccessHandler;
    private String logoutUrl = "/logout";
    private RequestMatcher logoutRequestMatcher;
    private boolean permitAll;
    private boolean customLogoutSuccess;
    private LinkedHashMap<RequestMatcher, LogoutSuccessHandler> defaultLogoutSuccessHandlerMappings = new LinkedHashMap();
    // ......
}
```



```java
// 退出登录
// 退出登录时
// 1.使 Session 无效
// 2.清除授权信息
http.logout()
        // 修改退出登录 url
        .logoutUrl("/user/logout")
        // 退出成功后重定的页面
        .logoutSuccessUrl("/login")
;
```



​		也可以自己进行定义退出成功处理器。需要实现 LogoutSuccessHandler 接口。与登录成功处理器和登录失败处理器类似。



#### CSRF

##### 什么是CSRF

​		CSRF (Cross-site request forgery) 跨站请求伪造，也被称为 OneClick Attack 或者 Session Riding，通过伪造用户请求访问受信任站点的非法请求访问。

​		跨域：只要网络协议，ip 地址，端口中任何一个不相同就是跨域请求。

​		客户端与服务进行交互时，由于 http 协议本身是无状态协议，所以引入了 cookie 进行记录客户端身份。在 cookie 中会存放session id 用来识别客户端身份的。在跨域的情况下，session id 可能被第三方恶意劫持，通过这个 session id 向服务端发起请求时，服务端会认为这个请求是合法的，可能发生很多意想不到的事情。



##### Spring Security 中的 CSRF

​		从 Spring Security4 开始 CSRF 防护默认开启。默认会拦截请求。进行 CSRF 处理，CSRF 为了保证不是其他第三方网站访问，要求访问时携带参数名为 `_csrf` 值为token（token 在服务端产生）的内容，如果 token 和服务端的 token 匹配成功，则正常访问。



##### 示例

###### 页面

loginWithCsrf.html

```html
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
    <form action="/common/auth/login" method="post">
        <input type="hidden" th:value="${_csrf.token}" name="_csrf" th:if="${_csrf}"/>
        用户名: <input type="text" name="username" title="username"/><br/>
        密码: <input type="password" name="password" title="password"/><br/>
        <input type="submit" value="登录" />
    </form>
</body>
</html>
```

###### 配置

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    // 表单提交
    http.formLogin()
        // 自定义登灵页面
        // .loginPage("/login.html")
        .loginPage("/login/loginWithCsrf")
    // 授权
	http.authorizeRequests()
        // 放行登录，不需要认证
        .antMatchers("/login/loginWithCsrf").permitAll()

	// 关闭 csrf 防护
	// http.csrf().disable();
}
```

###### Controller

```java
@Controller
@RequestMapping("/login")
public class LoginController {
    @GetMapping("/loginWithCsrf")
    public String loginWithCsrf(){
        return "loginWithCsrf";
    }
}
```

登录之后查看 login 接口会发现表单中多了一个 `_csrf` 参数



### SpringSecurityOAuth2

授权服务器

![](..\pictures\20240101\001.png)

Authorize Endpoint：授权端点，进行授权

Token Endpoint：令牌端点，经过授权拿到对应的 Token

Introspection Endpoint：校验端点，校验 Token 的合法性

Revocation Endpoint：撤销端点，撤销授权



架构

![](..\pictures\20240101\002.png)



流程

1. 用户访问，此时没有 Token。Oauth2RestTemplate 会报错，这个报错信息会被 Oauth2ClientContextFilter 捕获并重定向到认证服务器
2. 认证服务器通过 Authorization Endpoint 进行授权，并通过 AuthorizationServerTokenServices 生成授权码并返回给客户端
3. 客户端拿到授权码去认证服务器通过 Token Endpoint 调用 AuthorizationServerTokenServices 生成 Token 并返回给客户端
4. 客户端拿到 Token 去资源服务器访问资源，一般会通过 Oauth2AuthenticationManager 调用ResourceServerTokenServices 进行校验。校验通过可以获取资源



#### 授权码模式



##### 依赖

```xml
<!-- Spring Security Oauth2组件 -->
<!-- 包含了spring-cloud-starter-security，所以不需要在单独进行引入 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-oauth2</artifactId>
    <version>2.2.5.RELEASE</version>
</dependency>
<!--web组件-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
</dependency>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2021.0.9</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```



##### EncoderConfig

```java
@Configuration
public class EncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
```



##### UserDetailsService

```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //1.根据用户名去数据库查询，如果不存在抛UsernameNotFoundException异常
        if (!"admin".equals(username)) {
            throw new UsernameNotFoundException("用户名不存在");
        }
        //2.比较密码(注册时已经加密过)，如果匹配成功返回UserDetails
        // 123
        String rawPassword = "1234";
        String passwordFromStorage = passwordEncoder.encode(rawPassword);
        System.out.println("自定义登录");
        System.out.println(passwordFromStorage);
        return new User(username, passwordFromStorage, AuthorityUtils.commaSeparatedStringToAuthorityList("admin,normal"));
    }
}
```



##### SecurityConfig

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/oauth/**", "/login/**", "logout/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().permitAll()
                .and()
                .csrf().disable()
        ;
    }
}
```



##### AuthorizationServerConfig

```java
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                // 客户端 ID
                .withClient("client")
                // 密钥
                .secret(passwordEncoder.encode("111"))
                // 重定向地址
                .redirectUris("http://www.baidu.com")
                // 授权范围
                .scopes("all")
                /**
                 * 授权类型
                 * authorization_code: 授权码模式
                 */
                .authorizedGrantTypes("authorization_code");
    }
}
```



##### ResourceServerConfig

```java
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .requestMatchers()
                .antMatchers("/user/**")
                ;
    }
}
```



##### UserController

```
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping
    public Object userInfo(Authentication authentication){
        return authentication.getPrincipal();
    }
}
```



##### 测试

1.获取授权码

GET http://localhost:9009/common/auth/oauth/authorize?response_type=code&client_id=client&redirect_uri=http://www.baidu.com&scope=all

return https://www.baidu.com/?code=qSG3xy



2.使用授权码获取 Token

POST http://localhost:9009/common/auth/oauth/token

Authorization(Basic Auth)

Username: 客户端标识

Password: 客户端密钥

Body(x-www-form-unlencoded)

grant_type: authorization_code
code: qSG3xy
client_id: client
redirect_uri: http://www.baidu.com
scopes: all



return

{ "access_token": "o5KRPWUFx34c5M1ODO6oO6VlyIk", "token_type": "bearer", "expires_in": 43199, "scope": "all"}



3.通过 Token 获取用户资源

GET http://localhost:9009/common/auth/user

Authorization(Bearer Token)

Token: o5KRPWUFx34c5M1ODO6oO6VlyIk

return: 返回用户信息



#### 密码模式



##### 依赖

与授权码模式一致。



##### EncoderConfig

与授权码模式一致。



##### UserDetailsService

与授权码模式一致。



##### SecurityConfig

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/oauth/**", "/login/**", "logout/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().permitAll()
                .and()
                .csrf().disable()
        ;
    }

    // 新增
    @Bean
    public AuthenticationManager authorizationManager() throws Exception {
        return super.authenticationManager();
    }
}
```



##### AuthorizationServerConfig

```java
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authorizationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                // 客户端 ID
                .withClient("client")
                // 密钥
                .secret(passwordEncoder.encode("111"))
                // 重定向地址
                .redirectUris("http://www.baidu.com")
                // 授权范围
                .scopes("all")
                /**
                 * 授权类型
                 * authorization_code: 授权码模式
                 * password: 密码模式
                 */
                .authorizedGrantTypes("authorization_code", "password");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authorizationManager)
                .userDetailsService(userDetailsService);
    }

}
```



##### ResourceServerConfig

与授权码模式一致。



##### UserController

与授权码模式一致。



##### 测试

1.密码模式不需要授权码，直接使用账号密码请求 Token

2.获取 Token

POST http://localhost:9009/common/auth/oauth/token

Authorization(Basic Auth)

Username: 客户端标识

Password: 客户端密钥

Body(x-www-form-unlencoded)

grant_type: password
scopes: all
username: admin
password: 1234



return

{"access_token":"X4o2hZfJSUfL9n8jllPp0XudwNM","token_type":"bearer","expires_in":43199,"scope":"all"}



3.通过 Token 获取用户资源

GET http://localhost:9009/common/auth/user

Authorization(Bearer Token)

Token: o5KRPWUFx34c5M1ODO6oO6VlyIk

return: 返回用户信息



#### 在 Redis 中存储 Token

将 Token 直接存在内存中，这在生产环境中是不合理的，下面将其改造成存储在 Redis 中。



##### 依赖及配置

```xml
<!--redis 依赖-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
    <version>3.2.1</version>
</dependency>
<!-- commons-poo12 对象池依赖 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```



```yaml
spring:
  redis:
    host: 192.168.31.101
    port: 6379
    password: 123456
```



##### Redis 配置类

```java
@Configuration
public class RedisConfig {
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public RedisTokenStore redisTokenStore(){
        return new RedisTokenStore(redisConnectionFactory);
    }
}
```



##### AuthorizationServerConfig

```java
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

	// ..... 
    
    @Resource
    private TokenStore redisTokenStore;

	// ..... 

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authorizationManager)
                .userDetailsService(userDetailsService)
            // 增加配置
                .tokenStore(redisTokenStore);
    }

}
```



#### 集成 JWT

##### 集成 JWT

###### 依赖

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.12.3</version>
</dependency>
```



###### TokenStoreConfig

```java
@Configuration
public class TokenStoreConfig {
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public TokenStore jwtTokenStore(JwtAccessTokenConverter jwtAccessTokenConverter){
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter(){
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setSigningKey("key");
        return jwtAccessTokenConverter;
    }
}
```



###### AuthorizationServerConfig

```java
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

	// ..... 
    
    @Autowired
    @Qualifier("jwtTokenStore")
    private TokenStore jwtTokenStore;

    @Autowired
    private JwtAccessTokenConverter jwtAccessTokenConverter;

	// ..... 

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authorizationManager)
                .userDetailsService(userDetailsService)
            	// 增加配置
                .tokenStore(jwtTokenStore)
                .accessTokenConverter(jwtAccessTokenConverter);
    }

}
```



##### 扩展 JWT 中存储的内容



###### Token 扩展增强

```java
@Component
public class JwtTokenEnhancer implements TokenEnhancer {
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken oAuth2AccessToken, OAuth2Authentication oAuth2Authentication) {
        Map<String,Object> info = new HashMap<>();
        info.put("enhance", "enhance info");
        ((DefaultOAuth2AccessToken)oAuth2AccessToken).setAdditionalInformation(info);
        return oAuth2AccessToken;
    }
}
```



###### AuthorizationServerConfig

```java
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

	// ..... 
    @Autowired
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    @Autowired
    @Qualifier("jwtTokenEnhancer")
    private TokenEnhancer jwtTokenEnhancer;

	// ..... 

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        // ...
            
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        List<TokenEnhancer> deletates = new ArrayList<>();
        deletates.add(jwtTokenEnhancer);
        deletates.add(jwtAccessTokenConverter);
        
        tokenEnhancerChain.setTokenEnhancers(deletates);
        endpoints.authenticationManager(authorizationManager)
                // ...
                .tokenEnhancer(tokenEnhancerChain);
    }

}
```



新增的扩展将会保存在 JWT 中



##### 刷新令牌

```java
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    // ..

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                // ...
                /**
                 * 授权类型
                 * authorization_code: 授权码模式
                 * password: 密码模式
                 * refresh_token: 刷新令牌
                 */
                .authorizedGrantTypes("authorization_code", "password", "refresh_token")
                /**
                 * token 失效时间
                 */
                .accessTokenValiditySeconds(60)
                /**
                 * 刷新令牌失效时间
                 */
                .refreshTokenValiditySeconds(3600);
    }

	// ...

}
```



令牌失效后，向获取 Token 的接口发送刷新令牌即可获取新令牌

grant_type:refresh_token
refresh_token:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJhZG1pbiIsInNjb3BlIjpbImFsbCJdLCJhdGkiOiJueUt0NWhOX0JRQU5SYm1MaU5lRkx2LUNTVUEiLCJleHAiOjE3MDU1MDcxMDksImF1dGhvcml0aWVzIjpbImFkbWluIiwibm9ybWFsIl0sImp0aSI6Ii1hR3pxSzlPU180UHpweFdKLTFQSVlUd2FPQSIsImNsaWVudF9pZCI6ImNsaWVudCIsImVuaGFuY2UiOiJlbmhhbmNlIGluZm8ifQ.GBRbPMniHr1Wlne2jvlw9l_guodSbfuEvNZxJVnMuQ3OBlcKFl7_GL1FtUlyhy3gH4WSoHgY-mxd8exGnbzONhxJRQmsbnCgLeU2xEy4PmmwC2lpJTEDVFrIzp9Qfvc_vytFrmdI1jjjeWVXsm73aO5FzE1goFr5uFE5Xv6qjISc1ZirPsI4Vy0VZtLGxWMjo8P0k-dFv0Jmbi2hdT8uYVjj7GXK7i-wtfmeO62PxK57Fh5HMPBBxeQkSQmyIjUWuQROt7JuYUUqP9_spFtG1ANdLj4UQ_1jH5Dh-KrS2vneHxQGbN4IoTt8hqjuV_wC0MHh080nVwC7pGp3Fr6QwA



#### 集成 SSO



# EasyExcel

## Converter

### Converter

```java
public class ExcelLocalDateTimeConverter implements Converter<LocalDateTime> {

    private String defaultFormat = DateConstants.FORMAT;

    @Override
    public Class supportJavaTypeKey() {
        return LocalDateTime.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public LocalDateTime convertToJavaData(CellData cellData, ExcelContentProperty excelContentProperty, GlobalConfiguration globalConfiguration) {
        DateTimeFormat annotation = excelContentProperty.getField().getAnnotation(DateTimeFormat.class);
        String pattern = null == annotation ? defaultFormat : annotation.pattern();
        return LocalDateTime.parse(cellData.getStringValue(), DateTimeFormatter.ofPattern(StringUtils.hasText(pattern) ? pattern : defaultFormat));
    }

    @Override
    public CellData convertToExcelData(LocalDateTime localDateTime, ExcelContentProperty excelContentProperty, GlobalConfiguration globalConfiguration) {
        JsonFormat annotation = excelContentProperty.getField().getAnnotation(JsonFormat.class);
        String pattern = null == annotation ? defaultFormat : annotation.pattern();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(StringUtils.hasText(pattern) ? pattern : defaultFormat);
        String format = formatter.format(localDateTime);
        return new CellData<LocalDateTime>(format);
    }
}
```



### 用法一

```java
public class ReserveExcelVO {

    @ExcelProperty(value = {"日期"}, converter = ExcelLocalDateTimeConverter.class)
    private LocalDateTime createTime;
 
}
```



### 用法二

```java
ExcelWriter writer = EasyExcel.write(response.getOutputStream()).needHead(true).excelType(ExcelTypeEnum.XLSX)
          .registerConverter(new ExcelLocalDateTimeConverter())
          .build();
```



## 导入Excel文件并校验

### 依赖

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>easyexcel</artifactId>
    <version>${easyexcel.version}</version>
</dependency>
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-validator</artifactId>
    <version>${hibernate-validator.version}</version>
</dependency>
```



### ValidExcelListener

```java
@Slf4j
@Data
public abstract class ValidExcelListener<T> extends AnalysisEventListener<T> {

    private List<T> dataList = new ArrayList<>();

    private StringBuilder errorMsgs = new StringBuilder();

    private boolean error = false;

    private ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 这个每一条数据解析都会来调用
     *
     * @param data    one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param context
     */
    @Override
    public void invoke(T data, AnalysisContext context) {
        log.info("invoke data={}",data);
        validation(data,context);
        this.dataList.add(data);
    }

    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("Excel数据解析完成！");
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        // 如果是某一个单元格的转换异常 能获取到具体行号
        // 如果要获取头的信息 配合invokeHeadMap使用
        if (exception instanceof ExcelDataConvertException) {
            ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException)exception;
            String tempErrorMsg=String.format("第%s行，第%s列解析异常，数据为:%s",excelDataConvertException.getRowIndex()+1,
                    excelDataConvertException.getColumnIndex()+1, excelDataConvertException.getCellData());
            this.errorMsgs.append(tempErrorMsg);
            log.error(tempErrorMsg);
            this.error = true;
        }
    }

    private void validation(T data, AnalysisContext context){
        ValidationUtil.ValidResult validResult = ValidationUtil.allCheckValidate(data);
        if(null!=validResult && !validResult.isSuccess()){
            this.error = true;
            this.errorMsgs.append("行:").append(context.readRowHolder().getRowIndex()).append(",").append(validResult.getMessages());
            try {
                log.error("excel valid result={}",objectMapper.writeValueAsString(validResult));
            } catch (JsonProcessingException e) {
                log.error("{}", e);
            }
        }
    }

}
```



### ValidationUtil

```java
public class ValidationUtil {

    @Data
    public static class ValidResult {
        private boolean success;
        private List<String> messages;

        public static ValidResult fail(List<String> messages){
            ValidResult result=new ValidResult();
            result.setSuccess(false);
            result.setMessages(messages);
            return result;
        }

        public static ValidResult success(){
            ValidResult result=new ValidResult();
            result.setSuccess(true);
            result.setMessages(Collections.emptyList());
            return result;
        }
    }

    /**
     * 全部校验
     */
    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


    private ValidationUtil() {
    }

    /**
     * 注解验证参数(全部校验)
     *
     * @param obj
     */
    public static <T> ValidResult allCheckValidate(T obj) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(obj);
        //返回异常result
        if (!constraintViolations.isEmpty()) {
            List<String> errorMessages = new LinkedList<>();
            Iterator<ConstraintViolation<T>> iterator = constraintViolations.iterator();
            while (iterator.hasNext()) {
                ConstraintViolation<T> violation = iterator.next();
                errorMessages.add(String.format("%s:%s", violation.getPropertyPath().toString(), violation.getMessage()));
            }
            return ValidResult.fail(errorMessages);
        }
        return ValidResult.success();
    }
}
```



### ExcelListener

```java
public class ExcelListener extends ValidExcelListener<LsuaTaskNumEO> {
}

import org.hibernate.validator.constraints.Length;
@Data
public class LsuaTaskNumEO {

    @ExcelProperty(index = 0)
    @Length(min = 1, max = 255)
    private String tag;

    @ExcelProperty(index = 1)
    @Length(min = 1, max = 255)
    private String numId;
}
```



### 使用

```java
ExcelListener excelListener = new ExcelListener();
EasyExcelFactory.read(filePath, LsuaTaskNumEO.class, excelListener).sheet().doRead();
if(excelListener.isError()){
    String msg = String.format("文件内容校验失败：%s", excelListener.getErrorMsgs().toString());
    log.error("{}", msg);
}

List<LsuaTaskNumEO> recordsEOList = excelListener.getDataList();
```



# Nginx

## 安装配置部署管理



### 安装

在[官网](http://nginx.org/en/download.html)上下载 Stable version

```shell
tar -zxvf nginx.tar.gz

# 安装 nginx
sudo wget http://nginx.org/download/nginx-1.24.0.tar.gz
sudo tar zxvf nginx-1.24.0.tar.gz -C /opt/apps/
cd /opt/apps/nginx-1.24.0
# sudo apt install libpcre3 libpcre3-dev libssl-dev zlib1g-dev
# PCRE 作用是让 Nginx 支持 Rewrite 功能。
# --with-stream : 支持 TCP 转发
sudo ./configure --prefix=/opt/apps/nginx --with-stream --with-http_stub_status_module --with-http_ssl_module

# 参数说明
# 安装目录位置
--prefix=/usr/local/nginx
# 启动的时候进程描述文件保存位置和进程文件名
--pid-path=/var/run/nginx/nginxpid
# 锁定文件
--lock-path=/var/lock/nginx.lock
# 错误日志
--error-log-path=/var/log/nginx/error.log
# 访问日志
--http-log-path=/var/log/nginx/access.log
# 解压压缩模式
--with-http_gzip_static_module
# 客户端定义
--http-client-body-temp-path=/var/temp/nginx/client
# 代理临时目录位置
--http-proxy-temp-path=/var/temp/nginx/proxy
# 快速访问临时目录
--http-fastcgi-temp-path=/var/temp/nginx/fastcgi
# sgi访问目录
--http-uwsgi-temp-path=/var/temp/nginx/uwsgi
# scgi访问临时目录
--http-scgi-temp-path=/var/temp/nginx/scgi

sudo make && sudo make install
cd .. && sudo rm -r /opt/apps/nginx-1.24.0
/opt/apps/nginx/sbin/nginx -v
```



### 配置

`nginx/conf/nginx.conf`

检查配置文件是否正确 `nginx/sbin/nginx -t`



#### 配置大全

[配置大全](https://www.cnblogs.com/54chensongxia/p/12938929.html#:~:text=Nginx的主配置文件,间不存在次序关系。)

[配置大全](../files/config/nginx.all.conf)



#### 参考配置

[参考配置](../files/config/nginx.some.conf)



#### 配置 HTTP 转发

```properties
http {
    include       mime.types;
    default_type  application/octet-stream;

    upstream nacoscluster {
        server 192.168.31.100:8840;
        server 192.168.31.100:8842;
        server 192.168.31.100:8844;
    }

    server {
        listen       8848;
        server_name  localhost;

        location / {
            proxy_pass http://nacoscluster/;
        }
    }
}

```



#### 配置 TCP 转发

```properties
stream {

    upstream nacoscluster_grpc {
        server 192.168.31.100:9840 max_fails=3 fail_timeout=10s;
        server 192.168.31.100:9842 max_fails=3 fail_timeout=10s;
		server 192.168.31.100:9844 max_fails=3 fail_timeout=10s;
    }

    server {
        listen 9848;
        proxy_connect_timeout 20s;
        proxy_timeout 5m;
        proxy_pass nacoscluster_grpc;
    }

}

```



### 创建用户（组）

```shell
sudo groupadd -r apps
sudo useradd -r -g apps -s /bin/false www

sudo mkdir /opt/temp/nginx
sudo mkdir /var/log/nginx
#修改当前目录拥有者为新建的 mysql 用户
sudo chown -R www:root /opt/apps/nginx
sudo chown -R www:root /opt/temp/nginx
sudo chown -R www:root /var/log/nginx
```



### 服务管理

手动

```shell
# 启动 Nginx
sudo /opt/apps/nginx/sbin/nginx
# 重新载入配置文件
sudo /opt/apps/nginx/sbin/nginx -s reload
# 重启 Nginx
sudo /opt/apps/nginx/sbin/nginx -s reopen
# 停止接受新的请求并处理完当前请求后停止 Nginx
sudo /opt/apps/nginx/sbin/nginx -s quit
# 停止 Nginx
sudo /opt/apps/nginx/sbin/nginx -s stop
# Nginx 有哪些模块
sudo /opt/apps/nginx/sbin/nginx -V

ps aux | grep 'nginx'
sudo kill -9 PID
```



自动

编写[脚本](../files/scripts/server/nginx.server)文件放在 base_dir 目录下 `/opt/apps/nginx/nginx.server`

配置参考 Linux -> 服务管理  -> 系统启动时自动运行服务



## 高可用集群

使用 Keepalive 进行负载均衡

[Nginx 部署与集群-腾讯云开发者社区-腾讯云 (tencent.com)](https://cloud.tencent.com/developer/article/2199087?from=15425)
