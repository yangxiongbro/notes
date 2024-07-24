# Java

[java启动jar包引入外部配置文件_jar包加载外部配置文件_飘零未归人的博客-CSDN博客](https://blog.csdn.net/qq_34484062/article/details/128021066)

```shell
java -jar ./test.jar -Dspring.config.location=../config/application-my.yaml
java -jar ./test.jar -Dspring.config.additional-location=../config/application-my.yaml
# spring.config.location：会覆盖内部配置参数
# spring.config.additional-location：会和内部配置参数互补
```



## 集合



### List



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



#### 取对象 List 中的某个属性为属性 List

```java
List<String> names=list.stream().map(PersonData::getName).collect(Collectors.toList());
```



#### List 按某个字段分组

```java
Map<String, List<DataVO>> map = list.stream().collect(Collectors.groupingBy(item -> item.getName()));
// 或
Map<String, List<DataVO>> map = list.stream().collect(Collectors.groupingBy(DataVO::getName));
```



#### 字符串集合合并为一个字符串

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



### Map

#### 键值对

```java
Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>("key", "value")
```



## 时间

#### 时间格式

```java
// yyyy-MM-dd HH:mm:ss
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



##### 获取时间戳

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
 * <b><code>AbstractBusinessExceptionAssert</code></b>
 * <p/>
 * 创建异常接口实现类，由于枚举不能继承，所以不能使用抽象类
 * <p/>
 * <b>Creation Time:</b> 2022/8/24 下午12:09.
 *
 * @author yang xiong
 */
public interface IBaseBusinessExceptionFactory extends IExceptionAssertResponseEnum, IBaseAssert {
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
public class BaseBusinessException extends BaseException {

    private static final long serialVersionUID = 1L;

    public BaseBusinessException(IExceptionAssertResponseEnum exceptionAssertResponseEnum, Object[] args, String message) {
        super(exceptionAssertResponseEnum, args, message);
    }

    public BaseBusinessException(IExceptionAssertResponseEnum exceptionAssertResponseEnum, Object[] args, String message, Throwable cause) {
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
public enum BaseBusinessExceptionAssertResponseEnum implements IBaseBusinessExceptionFactory {

    /**
     * 通用异常
     */
    EXCEPTION(100001, "{0}"),

    /**
     * 未找到文件
     */
    FILE_NOT_FOUND(200001, "未找到文件:{0}."),

    /**
     * 未找到文件
     */
    FILE_NOT_FOUND_IN_DIR(200002, "在目录:{0},未找到文件:{1}.");

    /**
     * 返回码
     */
    private int code;

    /**
     * 返回消息
     */
    private String message;

    BaseBusinessExceptionAssertResponseEnum(int code, String message) {
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





# Spring



# SpringMVC



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

​		Try 方法作为一阶段准备方法，需要做资源的检查和预留。在扣钱场景下，Try 要做的事情是就是检查账户余额是否充足，预留转账资金，预留的方式就是冻结 A 账户的 转账资金。Try 方法执行之后，账号 A 余额虽然还是 100，但是其中 30 元已经被冻结了，不能被其他事务使用。

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
