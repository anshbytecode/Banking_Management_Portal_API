package com.webapp.bankingportal;

import com.webapp.bankingportal.service.CacheService;
import com.webapp.bankingportal.service.CacheServiceImpl;
import com.webapp.bankingportal.type.CacheKeyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheServiceTests {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new CacheServiceImpl(redisTemplate);
    }

    @Test
    void testExists_WithIdempotencyKey_ReturnsTrue() {
        String userId = "user123";
        String endpoint = "/api/account/deposit";
        String payloadHash = "payload123";
        String expectedKey = "IDPT:user123:/api/account/deposit:payload123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn("cached-response");

        boolean exists = cacheService.exists(CacheKeyType.IDEMPOTENCY, userId, endpoint, payloadHash);

        assertTrue(exists);
        verify(valueOperations).get(expectedKey);
    }

    @Test
    void testExists_WithIdempotencyKey_ReturnsFalse() {
        String userId = "user123";
        String endpoint = "/api/account/deposit";
        String payloadHash = "payload123";
        String expectedKey = "IDPT:user123:/api/account/deposit:payload123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(null);

        boolean exists = cacheService.exists(CacheKeyType.IDEMPOTENCY, userId, endpoint, payloadHash);

        assertFalse(exists);
        verify(valueOperations).get(expectedKey);
    }

    @Test
    void testGet_WithIdempotencyKey_ReturnsValue() {
        String userId = "user123";
        String endpoint = "/api/account/deposit";
        String payloadHash = "payload123";
        String expectedKey = "IDPT:user123:/api/account/deposit:payload123";
        String expectedValue = "test-value";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(expectedValue);

        Optional<String> value = cacheService.get(CacheKeyType.IDEMPOTENCY, userId, endpoint, payloadHash);

        assertTrue(value.isPresent());
        assertEquals(expectedValue, value.get());
        verify(valueOperations).get(expectedKey);
    }

    @Test
    void testGet_WithIdempotencyKey_ReturnsEmpty() {
        String userId = "user123";
        String endpoint = "/api/account/deposit";
        String payloadHash = "payload123";
        String expectedKey = "IDPT:user123:/api/account/deposit:payload123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(null);

        Optional<String> value = cacheService.get(CacheKeyType.IDEMPOTENCY, userId, endpoint, payloadHash);

        assertTrue(value.isEmpty());
        verify(valueOperations).get(expectedKey);
    }

    @Test
    void testGet_WithClass_WhenValueMatchesType_ReturnsValue() {
        String userId = "user123";
        String endpoint = "/api/account/deposit";
        String payloadHash = "payload123";
        String expectedKey = "IDPT:user123:/api/account/deposit:payload123";
        String expectedValue = "test-value";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(expectedValue);

        Optional<String> value = cacheService.get(CacheKeyType.IDEMPOTENCY, String.class, userId, endpoint, payloadHash);

        assertTrue(value.isPresent());
        assertEquals(expectedValue, value.get());
        verify(valueOperations).get(expectedKey);
    }

    @Test
    void testGet_WithClass_WhenValueDoesNotMatchType_ReturnsEmpty() {
        String userId = "user123";
        String endpoint = "/api/account/deposit";
        String payloadHash = "payload123";
        String expectedKey = "IDPT:user123:/api/account/deposit:payload123";
        Integer wrongTypeValue = 123;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(wrongTypeValue);

        Optional<String> value = cacheService.get(CacheKeyType.IDEMPOTENCY, String.class, userId, endpoint, payloadHash);

        assertTrue(value.isEmpty());
        verify(valueOperations).get(expectedKey);
    }

    @Test
    void testPut_WithIdempotencyKey_StoresValue() {
        String userId = "user123";
        String endpoint = "/api/account/deposit";
        String payloadHash = "payload123";
        String expectedKey = "IDPT:user123:/api/account/deposit:payload123";
        String value = "test-value";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        cacheService.put(CacheKeyType.IDEMPOTENCY, value, userId, endpoint, payloadHash);

        verify(valueOperations).set(expectedKey, value, 86400, java.util.concurrent.TimeUnit.SECONDS);
    }

    @Test
    void testPut_WithCustomTtl_StoresValueWithCustomTtl() {
        String userId = "user123";
        String endpoint = "/api/account/deposit";
        String payloadHash = "payload123";
        String expectedKey = "IDPT:user123:/api/account/deposit:payload123";
        String value = "test-value";
        long customTtl = 3600;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        cacheService.put(CacheKeyType.IDEMPOTENCY, value, customTtl, userId, endpoint, payloadHash);

        verify(valueOperations).set(expectedKey, value, customTtl, java.util.concurrent.TimeUnit.SECONDS);
    }

    @Test
    void testDelete_WithIdempotencyKey_DeletesCorrectKey() {
        String userId = "user123";
        String endpoint = "/api/account/deposit";
        String payloadHash = "payload123";
        String expectedKey = "IDPT:user123:/api/account/deposit:payload123";

        cacheService.delete(CacheKeyType.IDEMPOTENCY, userId, endpoint, payloadHash);

        verify(redisTemplate).delete(expectedKey);
    }
}