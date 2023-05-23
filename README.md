# Pansy
A backend api for a simple social network.

### Features:
* Feed (implemented using "fan-out on write" model)
* Create/Edit/Delete posts
* Follow/Unfollow users
* Search in users by name
* Like posts
* Add/Remove comment on posts
* Notifications

### Architecture:
The app is divided into three layers ([read more](https://martinfowler.com/bliki/PresentationDomainDataLayering.html) about this architecture):\
\
![img.png](/Users/mostafa/Documents/Projects/Pansy/arch.png)
### Tools & Technologies Used:
* [Spring Boot](https://spring.io/)
* [Hibernate](https://hibernate.org/)
* [Hibernate Search](https://hibernate.org/search/) (with [Elasticsearch](https://www.elastic.co/))
* [PostgreSQL](https://www.postgresql.org/)
* [Redis](https://redis.io/)
* [Lombok](https://projectlombok.org/)
* [springdoc](https://springdoc.org/) (provides [Swagger](https://swagger.io/))
* [Java JWT](https://github.com/jwtk/jjwt)
* [Mockito](https://site.mockito.org/)
* [JUnit](https://junit.org/)