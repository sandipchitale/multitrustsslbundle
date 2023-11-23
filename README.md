# Description

A simple Spring Boot App to show use of a composite TrustManager built from SsslBundle with RestTemplate.

It uses two trust stores 

- `server1-truststore.jks` to trust server at https://server1:8081 which uses self-signed certificate.
- `server2-truststore.jks` to trust server at https://server2:8082 which uses self-signed certificate.

which are wrapped in SslBundles named:

- `server1`
- `server2`

See [serversslbundle](https://github.com/sandipchitale/serversslbundle) git repo to run the two server instances.

Make sure to add these entries to /etc/hosts :

127.0.0.1 server1 server2 ...

It also registers third SslBundle named `JAVA_CACERTS_BUNDLE` that wraps Java's default cacerts trust store. 
This is used to trust public API which uses signed certificate. 

- https://jsonplaceholder.typicode.com/todos/1



### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.1.5/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.1.5/gradle-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.1.5/reference/htmlsingle/index.html#web)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

