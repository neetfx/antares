# Antares

Antares is an SpringBoot extension used to simplify the process of dynamically registering beans to Spring context.

**Maven Dependency (Java 8)**

```xml
<dependency>
    <groupId>com.github.prchen</groupId>
    <artifactId>spring-boot-starter-antares</artifactId>
    <version>0.0.1</version>
</dependency>
```

## Getting Start

Firstly, define an ```AntaresManifestAdvice``` to tell Antares how to collect passengers and which **FactoryBean** class should be used to create the bean instance.

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@AntaresManifestAdvice(factoryClass = FooFactoryBean.class)
public @interface Foo {
    // Annotation methods
}
```

Secondly, implement your own **FactoryBean** which will create the bean instance.

```java
public class FooFactoryBean extends AntaresFactoryBean {
    @Override
    public Object getObject() throws Exception {
        // Create the bean instance here
        return getObjectType().newInstance();
    }
}
```

Thirdly, use your own annotation in your code.

```java
@Foo
public class MyBean { }
``` 

Put it together.
```java
@AntaresScan
@SpringBootApplication
public class MyApp {
    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    }
}

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@AntaresManifestAdvice(factoryClass = FooFactoryBean.class)
public @interface Foo {
    // Annotation methods
}

class FooFactoryBean extends AntaresFactoryBean {
    @Override
    public Object getObject() throws Exception {
        // Create the bean instance here
        return getObjectType().newInstance();
    }
}

@Foo
public class MyBean { }

@Service
class MyService {
    @Autowired
    private MyBean myBean;
}
```
