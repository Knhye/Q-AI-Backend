package com.example.qnai.repository.adapter;

import com.example.qnai.dto.refreshToken.RefreshDto;
import com.example.qnai.entity.RefreshToken;
import com.example.qnai.entity.Users;
import com.example.qnai.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@Profile("redis")
@RequiredArgsConstructor
public class RedisRefreshTokenRepositoryAdapter implements RefreshTokenRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String TOKEN_PREFIX = "refresh_token:";

    @Override
    public void save(String token, Users user, long ttl) {
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(
                tokenKey,
                user.getId().toString(),
                ttl,
                TimeUnit.SECONDS
        );
    }

    @Override
    public Optional<RefreshDto> findByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;

        long ttl = redisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
        if (ttl < 0) {
            return Optional.empty();
        }

        String userId = redisTemplate.opsForValue().get(tokenKey);
        if (userId == null) {
            return Optional.empty();
        }

        LocalDateTime expiryDatetime = LocalDateTime.now().plusSeconds(ttl);

        return Optional.of(RefreshDto.builder()
                .token(token)
                .userId(Long.valueOf(userId))
                .expiryDatetime(expiryDatetime)
                .build());
    }

    @Override
    public void deleteByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.delete(tokenKey);
    }

    @Override
    public RefreshToken findByUser(Users user) {
        String targetUserId = user.getId().toString();

        ScanOptions options = ScanOptions.scanOptions()
                .match(TOKEN_PREFIX + "*")
                .count(100)
                .build();

        try (Cursor<byte[]> cursor = redisTemplate
                .getConnectionFactory()
                .getConnection()
                .scan(options)) {

            while (cursor.hasNext()) {
                String key = new String(cursor.next());

                String userId = redisTemplate.opsForValue().get(key);
                if (targetUserId.equals(userId)) {

                    long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                    if (ttl < 0) {
                        return null;
                    }

                    String token = key.replace(TOKEN_PREFIX, "");
                    LocalDateTime expiryDatetime = LocalDateTime.now().plusSeconds(ttl);

                    return RefreshToken.builder()
                            .token(token)
                            .user(user)
                            .expiryDatetime(expiryDatetime)
                            .build();
                }
            }
        }

        return null;
    }

}
