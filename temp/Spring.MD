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

![image-20230201220656418](../pictures/20230315/334.png)



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

![image-20230201220656417](../pictures/20230315/333.png)





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

和 jdk 动态代理原理查不多

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



## WEB MVC

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



### 24) @ControllerAdvice 之 @InitBinder

#### 演示 - 准备 @InitBinder

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

#### 收获💡

1. RequestMappingHandlerAdapter 初始化时会解析 @ControllerAdvice 中的 @InitBinder 方法
2. RequestMappingHandlerAdapter 会以类为单位，在该类首次使用时，解析此类的 @InitBinder 方法
3. 以上两种 @InitBinder 的解析结果都会缓存来避免重复解析
4. 控制器方法调用时，会综合利用本类的 @InitBinder 方法和 @ControllerAdvice 中的 @InitBinder 方法创建绑定工厂



### 25) 控制器方法执行流程

#### 图1

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



#### 图2

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

#### 图3

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



### 26) @ControllerAdvice 之 @ModelAttribute

#### 演示 - 准备 @ModelAttribute

##### 代码参考

**com.itheima.a26** 包

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

#### 收获💡

1. RequestMappingHandlerAdapter 初始化时会解析 @ControllerAdvice 中的 @ModelAttribute 方法
2. RequestMappingHandlerAdapter 会以类为单位，在该类首次使用时，解析此类的 @ModelAttribute 方法
3. 以上两种 @ModelAttribute 的解析结果都会缓存来避免重复解析
4. 控制器方法调用时，会综合利用本类的 @ModelAttribute 方法和 @ControllerAdvice 中的 @ModelAttribute 方法创建模型工厂



### 27) 返回值处理器

#### 演示 - 常见返回值处理器

##### 代码参考

**com.itheima.a27** 包

#### 收获💡

1. 常见的返回值处理器
   * ModelAndView，分别获取其模型和视图名，放入 ModelAndViewContainer
   * 返回值类型为 String 时，把它当做视图名，放入 ModelAndViewContainer
   * 返回值添加了 @ModelAttribute 注解时，将返回值作为模型，放入 ModelAndViewContainer
     * 此时需找到默认视图名
   * 返回值省略 @ModelAttribute 注解且返回非简单类型时，将返回值作为模型，放入 ModelAndViewContainer
     * 此时需找到默认视图名
   * 返回值类型为 ResponseEntity 时
     * 此时走 MessageConverter，并设置 ModelAndViewContainer.requestHandled 为 true
   * 返回值类型为 HttpHeaders 时
     * 会设置 ModelAndViewContainer.requestHandled 为 true
   * 返回值添加了 @ResponseBody 注解时
     * 此时走 MessageConverter，并设置 ModelAndViewContainer.requestHandled 为 true
2. 组合模式在 Spring 中的体现 + 1



### 28) MessageConverter

#### 演示 - MessageConverter 的作用

##### 代码参考

**com.itheima.a28.A28**

#### 收获💡

1. MessageConverter 的作用
   * @ResponseBody 是返回值处理器解析的
   * 但具体转换工作是 MessageConverter 做的
2. 如何选择 MediaType
   * 首先看 @RequestMapping 上有没有指定
   * 其次看 request 的 Accept 头有没有指定
   * 最后按 MessageConverter 的顺序, 谁能谁先转换



### 29) @ControllerAdvice 之 ResponseBodyAdvice

#### 演示 - ResponseBodyAdvice 增强

##### 代码参考

**com.itheima.a29** 包

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

#### 收获💡

1. ResponseBodyAdvice 返回响应体前包装



### 30) 异常解析器

#### 演示 - ExceptionHandlerExceptionResolver

##### 代码参考

**com.itheima.a30.A30**

#### 收获💡

1. 它能够重用参数解析器、返回值处理器，实现组件重用
2. 它能够支持嵌套异常



### 31) @ControllerAdvice 之 @ExceptionHandler

#### 演示 - 准备 @ExceptionHandler

##### 代码参考

**com.itheima.a31** 包

#### 收获💡

1. ExceptionHandlerExceptionResolver 初始化时会解析 @ControllerAdvice 中的 @ExceptionHandler 方法
2. ExceptionHandlerExceptionResolver 会以类为单位，在该类首次处理异常时，解析此类的 @ExceptionHandler 方法
3. 以上两种 @ExceptionHandler 的解析结果都会缓存来避免重复解析



### 32) Tomcat 异常处理

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

> ***评价***
>
> * 一个错误处理搞得这么复杂，就问恶心不？



#### 演示1 - 错误页处理

##### 关键代码

```java
@Bean // ⬅️修改了 Tomcat 服务器默认错误地址, 出错时使用请求转发方式跳转
public ErrorPageRegistrar errorPageRegistrar() {
    return webServerFactory -> webServerFactory.addErrorPages(new ErrorPage("/error"));
}

@Bean // ⬅️TomcatServletWebServerFactory 初始化前用它增强, 注册所有 ErrorPageRegistrar
public ErrorPageRegistrarBeanPostProcessor errorPageRegistrarBeanPostProcessor() {
    return new ErrorPageRegistrarBeanPostProcessor();
}
```

#### 收获💡

1. Tomcat 的错误页处理手段



#### 演示2 - BasicErrorController

##### 关键代码

```java
@Bean // ⬅️ErrorProperties 封装环境键值, ErrorAttributes 控制有哪些错误信息
public BasicErrorController basicErrorController() {
    ErrorProperties errorProperties = new ErrorProperties();
    errorProperties.setIncludeException(true);
    return new BasicErrorController(new DefaultErrorAttributes(), errorProperties);
}

@Bean // ⬅️名称为 error 的视图, 作为 BasicErrorController 的 text/html 响应结果
public View error() {
    return new View() {
        @Override
        public void render(
            Map<String, ?> model, 
            HttpServletRequest request, 
            HttpServletResponse response
        ) throws Exception {
            System.out.println(model);
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print("""
                    <h3>服务器内部错误</h3>
                    """);
        }
    };
}

@Bean // ⬅️收集容器中所有 View 对象, bean 的名字作为视图名
public ViewResolver viewResolver() {
    return new BeanNameViewResolver();
}
```

#### 收获💡

1. Spring Boot 中 BasicErrorController 如何工作



### 33) BeanNameUrlHandlerMapping 与 SimpleControllerHandlerAdapter

#### 演示 - 本组映射器和适配器

##### 关键代码

```java
@Bean
public BeanNameUrlHandlerMapping beanNameUrlHandlerMapping() {
    return new BeanNameUrlHandlerMapping();
}

@Bean
public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
    return new SimpleControllerHandlerAdapter();
}

@Bean("/c3")
public Controller controller3() {
    return (request, response) -> {
        response.getWriter().print("this is c3");
        return null;
    };
}
```

#### 收获💡

1. BeanNameUrlHandlerMapping，以 / 开头的 bean 的名字会被当作映射路径
2. 这些 bean 本身当作 handler，要求实现 Controller 接口
3. SimpleControllerHandlerAdapter，调用 handler
4. 模拟实现这组映射器和适配器



### 34) RouterFunctionMapping 与 HandlerFunctionAdapter

#### 演示 - 本组映射器和适配器

##### 关键代码

```java
@Bean
public RouterFunctionMapping routerFunctionMapping() {
    return new RouterFunctionMapping();
}

@Bean
public HandlerFunctionAdapter handlerFunctionAdapter() {
    return new HandlerFunctionAdapter();
}

@Bean
public RouterFunction<ServerResponse> r1() {
    //           ⬇️映射条件   ⬇️handler
    return route(GET("/r1"), request -> ok().body("this is r1"));
}
```

#### 收获💡

1. RouterFunctionMapping, 通过 RequestPredicate 条件映射
2. handler 要实现 HandlerFunction 接口
3. HandlerFunctionAdapter, 调用 handler



### 35) SimpleUrlHandlerMapping 与 HttpRequestHandlerAdapter

#### 演示1 - 本组映射器和适配器

##### 代码参考

**org.springframework.boot.autoconfigure.web.servlet.A35**

##### 关键代码

```java
@Bean
public SimpleUrlHandlerMapping simpleUrlHandlerMapping(ApplicationContext context) {
    SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
    Map<String, ResourceHttpRequestHandler> map 
        = context.getBeansOfType(ResourceHttpRequestHandler.class);
    handlerMapping.setUrlMap(map);
    return handlerMapping;
}

@Bean
public HttpRequestHandlerAdapter httpRequestHandlerAdapter() {
    return new HttpRequestHandlerAdapter();
}

@Bean("/**")
public ResourceHttpRequestHandler handler1() {
    ResourceHttpRequestHandler handler = new ResourceHttpRequestHandler();
    handler.setLocations(List.of(new ClassPathResource("static/")));
    return handler;
}

@Bean("/img/**")
public ResourceHttpRequestHandler handler2() {
    ResourceHttpRequestHandler handler = new ResourceHttpRequestHandler();
    handler.setLocations(List.of(new ClassPathResource("images/")));
    return handler;
}
```

#### 收获💡

1. SimpleUrlHandlerMapping 不会在初始化时收集映射信息，需要手动收集
2. SimpleUrlHandlerMapping 映射路径
3. ResourceHttpRequestHandler 作为静态资源 handler
4. HttpRequestHandlerAdapter, 调用此 handler



#### 演示2 - 静态资源解析优化

##### 关键代码

```java
@Bean("/**")
public ResourceHttpRequestHandler handler1() {
    ResourceHttpRequestHandler handler = new ResourceHttpRequestHandler();
    handler.setLocations(List.of(new ClassPathResource("static/")));
    handler.setResourceResolvers(List.of(
        	// ⬇️缓存优化
            new CachingResourceResolver(new ConcurrentMapCache("cache1")),
        	// ⬇️压缩优化
            new EncodedResourceResolver(),
        	// ⬇️原始资源解析
            new PathResourceResolver()
    ));
    return handler;
}
```

#### 收获💡

1. 责任链模式体现
2. 压缩文件需要手动生成



#### 演示3 - 欢迎页

##### 关键代码

```java
@Bean
public WelcomePageHandlerMapping welcomePageHandlerMapping(ApplicationContext context) {
    Resource resource = context.getResource("classpath:static/index.html");
    return new WelcomePageHandlerMapping(null, context, resource, "/**");
}

@Bean
public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
    return new SimpleControllerHandlerAdapter();
}
```

#### 收获💡

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



### 36) mvc 处理流程

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

### 37) Boot 骨架项目

如果是 linux 环境，用以下命令即可获取 spring boot 的骨架 pom.xml

```shell
curl -G https://start.spring.io/pom.xml -d dependencies=web,mysql,mybatis -o pom.xml
```

也可以使用 Postman 等工具实现

若想获取更多用法，请参考

```shell
curl https://start.spring.io
```



### 38) Boot War项目

步骤1：创建模块，区别在于打包方式选择 war

<img src="D:/documents/资料/Spring/文档/文档/img/image-20211021160145072.png" alt="image-20211021160145072" style="zoom: 50%;" />

接下来勾选 Spring Web 支持

<img src="D:/documents/资料/Spring/文档/文档/img/image-20211021162416525.png" alt="image-20211021162416525" style="zoom:50%;" />

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



### 39) Boot 启动过程

阶段一：SpringApplication 构造

1. 记录 BeanDefinition 源
2. 推断应用类型
3. 记录 ApplicationContext 初始化器
4. 记录监听器
5. 推断主启动类

阶段二：执行 run 方法

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

#### 演示 - 启动过程

**com.itheima.a39.A39_1** 对应 SpringApplication 构造

**com.itheima.a39.A39_2** 对应第1步，并演示 7 个事件

**com.itheima.a39.A39_3** 对应第2、8到12步

**org.springframework.boot.Step3**

**org.springframework.boot.Step4**

**org.springframework.boot.Step5**

**org.springframework.boot.Step6**

**org.springframework.boot.Step7**

#### 收获💡

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



### 40) Tomcat 内嵌容器

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

#### 演示1 - Tomcat 内嵌容器

##### 关键代码

```java
public static void main(String[] args) throws LifecycleException, IOException {
    // 1.创建 Tomcat 对象
    Tomcat tomcat = new Tomcat();
    tomcat.setBaseDir("tomcat");

    // 2.创建项目文件夹, 即 docBase 文件夹
    File docBase = Files.createTempDirectory("boot.").toFile();
    docBase.deleteOnExit();

    // 3.创建 Tomcat 项目, 在 Tomcat 中称为 Context
    Context context = tomcat.addContext("", docBase.getAbsolutePath());

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
```



#### 演示2 - 集成 Spring 容器

##### 关键代码

```java
WebApplicationContext springContext = getApplicationContext();

// 4.编程添加 Servlet
context.addServletContainerInitializer(new ServletContainerInitializer() {
    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        // ⬇️通过 ServletRegistrationBean 添加 DispatcherServlet 等
        for (ServletRegistrationBean registrationBean : 
             springContext.getBeansOfType(ServletRegistrationBean.class).values()) {
            registrationBean.onStartup(ctx);
        }
    }
}, Collections.emptySet());
```



### 41) Boot 自动配置

#### AopAutoConfiguration

Spring Boot 是利用了自动配置类来简化了 aop 相关配置

* AOP 自动配置类为 `org.springframework.boot.autoconfigure.aop.AopAutoConfiguration`
* 可以通过 `spring.aop.auto=false` 禁用 aop 自动配置
* AOP 自动配置的本质是通过 `@EnableAspectJAutoProxy` 来开启了自动代理，如果在引导类上自己添加了 `@EnableAspectJAutoProxy` 那么以自己添加的为准
* `@EnableAspectJAutoProxy` 的本质是向容器中添加了 `AnnotationAwareAspectJAutoProxyCreator` 这个 bean 后处理器，它能够找到容器中所有切面，并为匹配切点的目标类创建代理，创建代理的工作一般是在 bean 的初始化阶段完成的



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



#### TransactionAutoConfiguration

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



#### 演示 - 自动配置类原理

##### 关键代码

假设已有第三方的两个自动配置类

```java
@Configuration // ⬅️第三方的配置类
static class AutoConfiguration1 {
    @Bean
    public Bean1 bean1() {
        return new Bean1();
    }
}

@Configuration // ⬅️第三方的配置类
static class AutoConfiguration2 {
    @Bean
    public Bean2 bean2() {
        return new Bean2();
    }
}
```

提供一个配置文件 META-INF/spring.factories，key 为导入器类名，值为多个自动配置类名，用逗号分隔

```properties
MyImportSelector=\
AutoConfiguration1,\
AutoConfiguration2
```

> ***注意***
>
> * 上述配置文件中 MyImportSelector 与 AutoConfiguration1，AutoConfiguration2 为简洁均省略了包名，自己测试时请将包名根据情况补全

引入自动配置

```java
@Configuration // ⬅️本项目的配置类
@Import(MyImportSelector.class)
static class Config { }

static class MyImportSelector implements DeferredImportSelector {
    // ⬇️该方法从 META-INF/spring.factories 读取自动配置类名，返回的 String[] 即为要导入的配置类
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return SpringFactoriesLoader
            .loadFactoryNames(MyImportSelector.class, null).toArray(new String[0]);
    }
}
```

#### 收获💡

1. 自动配置类本质上就是一个配置类而已，只是用 META-INF/spring.factories 管理，与应用配置类解耦
2. @Enable 打头的注解本质是利用了 @Import
3. @Import 配合 DeferredImportSelector 即可实现导入，selectImports 方法的返回值即为要导入的配置类名
4. DeferredImportSelector 的导入会在最后执行，为的是让其它配置优先解析



### 42) 条件装配底层

条件装配的底层是本质上是 @Conditional 与 Condition，这两个注解。引入自动配置类时，期望满足一定条件才能被 Spring 管理，不满足则不管理，怎么做呢？

比如条件是【类路径下必须有 dataSource】这个 bean ，怎么做呢？

首先编写条件判断类，它实现 Condition 接口，编写条件判断逻辑

```java
static class MyCondition1 implements Condition { 
    // ⬇️如果存在 Druid 依赖，条件成立
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return ClassUtils.isPresent("com.alibaba.druid.pool.DruidDataSource", null);
    }
}
```

其次，在要导入的自动配置类上添加 `@Conditional(MyCondition1.class)`，将来此类被导入时就会做条件检查

```java
@Configuration // 第三方的配置类
@Conditional(MyCondition1.class) // ⬅️加入条件
static class AutoConfiguration1 {
    @Bean
    public Bean1 bean1() {
        return new Bean1();
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

#### 收获💡

1. 学习一种特殊的 if - else



## 其它

### 43) FactoryBean

#### 演示 - FactoryBean

##### 代码参考

**com.itheima.a43** 包

#### 收获💡

1. 它的作用是用制造创建过程较为复杂的产品, 如 SqlSessionFactory, 但 @Bean 已具备等价功能
2. 使用上较为古怪, 一不留神就会用错
   1. 被 FactoryBean 创建的产品
      * 会认为创建、依赖注入、Aware 接口回调、前初始化这些都是 FactoryBean 的职责, 这些流程都不会走
      * 唯有后初始化的流程会走, 也就是产品可以被代理增强
      * 单例的产品不会存储于 BeanFactory 的 singletonObjects 成员中, 而是另一个 factoryBeanObjectCache 成员中
   2. 按名字去获取时, 拿到的是产品对象, 名字前面加 & 获取的是工厂对象



### 44) @Indexed 原理

真实项目中，只需要加入以下依赖即可

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context-indexer</artifactId>
    <optional>true</optional>
</dependency>
```



#### 演示 - @Indexed

##### 代码参考

**com.itheima.a44** 包

#### 收获💡

1. 在编译时就根据 @Indexed 生成 META-INF/spring.components 文件
2. 扫描时
   * 如果发现 META-INF/spring.components 存在, 以它为准加载 bean definition
   * 否则, 会遍历包下所有 class 资源 (包括 jar 内的)
3. 解决的问题，在编译期就找到 @Component 组件，节省运行期间扫描 @Component 的时间



### 45) 代理进一步理解

#### 演示 - 代理

##### 代码参考

**com.itheima.a45** 包

#### 收获💡

1. spring 代理的设计特点

   * 依赖注入和初始化影响的是原始对象
     * 因此 cglib 不能用 MethodProxy.invokeSuper()

   * 代理与目标是两个对象，二者成员变量并不共用数据

2. static 方法、final 方法、private 方法均无法增强

   * 进一步理解代理增强基于方法重写



### 46) @Value 装配底层

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



#### 演示 - @Value 装配过程

##### 代码参考

**com.itheima.a46** 包

#### 收获💡

1. ContextAnnotationAutowireCandidateResolver 作用之一，获取 @Value 的值
2. 了解 ${ } 对应的解析器
3. 了解 #{ } 对应的解析器
4. TypeConvert 的一项体现



### 47) @Autowired 装配底层

#### 演示 - @Autowired 装配过程

##### 代码参考

**com.itheima.a47** 包

#### 收获💡

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



### 48) 事件监听器

#### 演示 - 事件监听器

##### 代码参考

**com.itheima.a48** 包

#### 收获💡

事件监听器的两种方式

1. 实现 ApplicationListener 接口
   * 根据接口泛型确定事件类型
2. @EventListener 标注监听方法
   * 根据监听器方法参数确定事件类型
   * 解析时机：在 SmartInitializingSingleton（所有单例初始化完成后），解析每个单例 bean



### 49) 事件发布器

#### 演示 - 事件发布器

##### 代码参考

**com.itheima.a49** 包

#### 收获💡

事件发布器模拟实现

1. addApplicationListenerBean 负责收集容器中的监听器
   * 监听器会统一转换为 GenericApplicationListener 对象，以支持判断事件类型
2. multicastEvent 遍历监听器集合，发布事件
   * 发布前先通过 GenericApplicationListener.supportsEventType 判断支持该事件类型才发事件
   * 可以利用线程池进行异步发事件优化
3. 如果发送的事件对象不是 ApplicationEvent 类型，Spring 会把它包装为 PayloadApplicationEvent 并用泛型技术解析事件对象的原始类型
   * 视频中未讲解

