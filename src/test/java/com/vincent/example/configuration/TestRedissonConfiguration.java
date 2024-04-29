package com.vincent.example.configuration;

import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class TestRedissonConfiguration {
    @MockBean
    private RedissonClient redissonClient;
}
