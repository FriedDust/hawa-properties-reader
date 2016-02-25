#Hawa Properties Reader

A simple library to read key value paired text files and inject the values automatically into the bean properties.

####A sample properties file (config.properties)
name=Fiona
age=16

The file is located under src/main/resources/

####Pojo to store the properties (Person.java)

```java
@PropertySource("classpath:config.properties")
public class Person {
  @PropertyValue("name")
  private String name;
  
  @PropertyValue("age")
  private Integer age;

  // getters and setters
}
```

####A class where you want to use Person bean

```java
public class TestHawa {
  @Inject
  private Person person;

  public void test() {
    // should print Fiona
    System.out.println(person.getName());
  }
}
```