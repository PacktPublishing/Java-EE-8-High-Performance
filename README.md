


# Java EE 8 High Performance
This is the code repository for [Java EE 8 High Performance](https://www.packtpub.com/application-development/java-ee-8-high-performance?utm_source=github&utm_medium=repository&utm_campaign=9781788473064), published by [Packt](https://www.packtpub.com/?utm_source=github). It contains all the supporting project files necessary to work through the book from start to finish.
## About the Book
The ease with which we write applications has been increasing, but with this comes the need to address their performance. A balancing act between easily implementing complex applications and keeping their performance optimal is a present-day need. In this book, we explore how to achieve this crucial balance while developing and deploying applications with Java EE 8.


## Instructions and Navigation
All of the code is organized into folders. Each folder starts with a number followed by the application name. For example, Chapter02.



The code will look like the following:
```
private String getLoggerName(InvocationContext context) {
return ofNullable(context.getMethod().getAnnotation(Log.class))
.orElseGet(() ->
context.getTarget().getClass().getAnnotation(Log.class))
.value();
}
```



## Related Products
* [Java EE 8 and Angular](https://www.packtpub.com/application-development/java-ee-8-and-angular?utm_source=github&utm_medium=repository&utm_campaign=9781788291200)

* [Java EE 8 Application Development](https://www.packtpub.com/application-development/java-ee-8-application-development?utm_source=github&utm_medium=repository&utm_campaign=9781788293679)

* [Java EE 8 Microservices [Video]](https://www.packtpub.com/application-development/java-ee-8-microservices-video?utm_source=github&utm_medium=repository&utm_campaign=9781788470377)

