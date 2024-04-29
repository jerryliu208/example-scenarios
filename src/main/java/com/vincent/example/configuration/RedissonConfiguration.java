package com.vincent.example.configuration;

import java.io.IOException;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfiguration {

    @Value("${spring.redis.single.address}")
    private String host;

    @Value("${spring.redis.single.database}")
    private int database;

    /**
     * create a RedissonClient instance with a single Redis server.
     *
     * @return
     * @throws IOException
     */
    @Bean
    public RedissonClient singleRedisson() throws IOException {
        // create configuration
        Config config = new Config();
        config.useSingleServer()
                .setAddress(this.host)
                .setDatabase(this.database);

        return Redisson.create(config);
    }
}
