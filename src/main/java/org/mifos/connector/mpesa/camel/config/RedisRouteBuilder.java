package org.mifos.connector.mpesa.camel.config;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisRouteBuilder extends RouteBuilder {
    private JedisConnectionFactory jedisConnectionFactory;
    private RedisTemplate<String, String> redisTemplate;
    @Value("${redis.host}")
    private String redisHost;
    @Value("${redis.port}")
    private String redisPort;

    public void configure() throws Exception {
        jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName(redisHost);
        jedisConnectionFactory.setPort(Integer.parseInt(redisPort));
        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        redisTemplate.afterPropertiesSet();
    }
}
