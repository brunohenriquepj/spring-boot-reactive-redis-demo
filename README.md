# Spring Boot Reactive Redis Demo

## Setup Java with [SDKMAN!](https://github.com/sdkman/sdkman-cli)

```console
sdk env install
```
---

## Setup Redis

```console
docker-compose up -d
```

---

## Build

```console
./gradlew build --exclude-task test
```

---

## Run api

```console
./gradlew bootRun
```

- Get data from Redis: http://localhost:8080/coffees

## Links
- [Accessing Data Reactively with Redis](https://spring.io/guides/gs/spring-data-reactive-redis/)
