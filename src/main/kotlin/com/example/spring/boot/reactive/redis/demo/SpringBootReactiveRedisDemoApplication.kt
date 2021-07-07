package com.example.spring.boot.reactive.redis.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.util.*
import javax.annotation.PostConstruct

@SpringBootApplication
class SpringBootReactiveRedisDemoApplication

fun main(args: Array<String>) {
    runApplication<SpringBootReactiveRedisDemoApplication>(*args)
}

@JvmRecord
data class Coffee(val id: String, val name: String)

@Configuration
class CoffeeConfiguration {

    @Bean
    fun redisOperations(factory: ReactiveRedisConnectionFactory): ReactiveRedisOperations<String, Coffee> {
        val serializer = Jackson2JsonRedisSerializer(Coffee::class.java)
        val builder = RedisSerializationContext.newSerializationContext<String, Coffee>(StringRedisSerializer())
        val context = builder.value(serializer).build()
        return ReactiveRedisTemplate(factory, context)
    }
}

@Component
class CoffeLoader(
    private val factory: ReactiveRedisConnectionFactory,
    private val coffeeOps: ReactiveRedisOperations<String, Coffee>
) {
    @PostConstruct
    fun loadData() {
        factory.reactiveConnection.serverCommands().flushAll().thenMany(
            Flux.just("Jet Black Redis", "Darth Redis", "Black Alert Redis")
                .map { Coffee(UUID.randomUUID().toString(), it) }
                .flatMap { coffeeOps.opsForValue().set(it.id, it) })
            .thenMany(coffeeOps.keys("*"))
            .flatMap(coffeeOps.opsForValue()::get)
            .subscribe(System.out::println)
    }
}

@RestController
class CoffeeController(private val coffeeOps: ReactiveRedisOperations<String, Coffee>) {
    @GetMapping("/coffees")
    fun all(): Flux<Coffee> {
        return coffeeOps.keys("*").flatMap(coffeeOps.opsForValue()::get)
    }
}
