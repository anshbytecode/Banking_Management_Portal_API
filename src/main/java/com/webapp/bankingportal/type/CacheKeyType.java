package com.webapp.bankingportal.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CacheKeyType {
    IDEMPOTENCY("IDPT:%s:%s:%s", 86400, CacheValueType.JSON),
    ;

    private final String prefix;
    private final long ttlSeconds;
    private final CacheValueType cacheValueType;

    public String generateKey() {
        return prefix;
    }

    public String generateKey(String... arguments) {
        return String.format(prefix, (Object[]) arguments);
    }
}
