# Accessing relational databases with Spring Framework and Kotlin with KotlinPrimavera

## Introduction

This is a Kotlin version of [Accessing Relational Data using JDBC with Spring](https://spring.io/guides/gs/relational-data-access/) guide.

We'll use Spring Boot and Kotlin with some features from [KotlinPrimavera](https://github.com/MarioAriasC/KotlinPrimavera)

The theory is explained in the aforementioned guide. We'll focus on the main differences and perks of using Kotlin

## Kotlin Data classes instead of Java Beans

Your normal Java customer class will look like this:

```java
package hello;

public class Customer {
    private long id;
    private String firstName, lastName;

    public Customer(long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return String.format(
                "Customer[id=%d, firstName='%s', lastName='%s']",
                id, firstName, lastName);
    }

    // getters & setters omitted for brevity
}
```

The Kotlin version is just one line:

```kotlin
data class Customer(val id: Long, val firstName: String, val lastName: String)
```

That's including ```toString()``` and getters and setters 'cause there is no need to omit them for brevity :smile:

## ```main```

```main``` methods in Java should static methods inside a class

```java
public static void main(String args[]) {
  SpringApplication.run(Application.class, args);
}
```

In Kotlin could be outside any class

```kotlin
fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
```

## Spring Boot Application class

```java
@SpringBootApplication
public class Application implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
```
Kotlin's version is a little bit shorter due to the absence of ```implements``` keyword and explicit typing


```kotin
@SpringBootApplication
open class Application : CommandLineRunner {

    private val log = LoggerFactory.getLogger(Application::class.java)
```


### Autowiring

```java
@Autowired
JdbcTemplate jdbcTemplate;
```
Kotlin's version is longer due to ```lateinit``` keyword (Remember Kotlin's [null safety features](https://kotlinlang.org/docs/reference/null-safety.html))

```kotlin
@Autowired lateinit var jdbcTemplate: JdbcTemplate
```

### Executing sql statements

```java
jdbcTemplate.execute("DROP TABLE customers IF EXISTS");
jdbcTemplate.execute("CREATE TABLE customers(" +
  "id SERIAL, first_name VARCHAR(255), last_name VARCHAR(255))");
```

Multiline String are a nice feature in Kotlin and ```with``` function could save you a lot of repetitive keystrokes

```kotlin
with(jdbcTemplate) {
  execute("DROP TABLE customers IF EXISTS")
  execute("""
    CREATE TABLE customers (
        id SERIAL,
        first_name VARCHAR(255),
        last_name VARCHAR(255)
    )""")
}
```

### Working with collections

```java
List<Object[]> splitUpNames = Arrays.asList("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long").stream()
        .map(name -> name.split(" "))
        .collect(Collectors.toList());

splitUpNames.forEach(name -> log.info(String.format("Inserting customer record for %s %s", name[0], name[1])));
```

Working with collections in Kotlin is a lot more cleaner than java, and ```format``` is just and extension function which is really convenient 

```kotlin
val splitUpNames = arrayOf("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long").map { name -> name.split(" ").toTypedArray() }

splitUpNames.forEach { name -> log.info("Inserting customer record for %s %s".format(name[0], name[1])) }
```

### Querying with JDBC

```java
jdbcTemplate.query(
        "SELECT id, first_name, last_name FROM customers WHERE first_name = ?", new Object[] { "Josh" },
        (rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name"))
).forEach(customer -> log.info(customer.toString()));
```

Kotlin's version is a little bit easier to the eyes

```kotlin
jdbcTemplate.query("SELECT id, first_name, last_name FROM customers WHERE first_name = ?", arrayOf("Josh")) { rs, rowNum ->
    Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name"))
}.forEach { customer -> log.info(customer.toString()) }
```

### KotlinPrimavera's extensions

In this case we use a KotlinPrimavera extension function that have more convenient parameters, ```fun <T> JdbcOperations.query(sql: String, vararg args: Any, rowMapper: (ResultSet, Int) -> T): List<T>``` so we don't need to wrap the second parameter into an array 

```kotlin
jdbcTemplate.query("SELECT id, first_name, last_name FROM customers WHERE first_name = ?", "Josh") { rs, rowNum ->
    rs.extract { Customer(long["id"]!!, string["first_name"]!!, string["last_name"]!!) }
}.forEach { customer -> log.info(customer.toString()) }
```

Also we're using KotlinPrimavera ```extract``` function for a DLSly, more natural and type safe access to our ```ResultSet```

## Summary

Kotlin seems to be designed for Spring, it just feel natural. And KotlinPrimavera could help you to reduce the amount of code, but you could survive without it 


