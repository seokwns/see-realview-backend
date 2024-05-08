package com.see.realview.token.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.see.realview._core.exception.ExceptionStatus;
import com.see.realview._core.exception.server.ServerException;
import com.see.realview.token.entity.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
public class TokenRedisRepositoryImpl implements TokenRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private final ValueOperations<String, String> valueOperations;

    private final ObjectMapper objectMapper;

    private final static String TOKEN_PREFIX = "token_";

    private final static Duration TOKEN_EXP = Duration.ofDays(3);

    public TokenRedisRepositoryImpl(@Autowired RedisTemplate<String, String> redisTemplate,
                                    @Autowired ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Token> findTokenByEmail(String email) {
        String key = getKeyById(email);
        String value = valueOperations.get(key);

        if (value == null) {
            return Optional.empty();
        }

        Token token = getToken(value);
        return Optional.ofNullable(token);
    }

    @Override
    public void save(String email, Token token) {
        String key = getKeyById(email);
        String value = getValue(token);
        valueOperations.set(key, value, TOKEN_EXP);
    }

    @Override
    public void deleteById(String email) {
        String key = getKeyById(email);
        redisTemplate.delete(key);
    }

    @Override
    public boolean isTokenExists(String email) {
        String key = getKeyById(email);
        return valueOperations.get(key) != null;
    }

    private static String getKeyById(String email) {
        return TOKEN_PREFIX + email;
    }

    private String getValue(Token token) {
        try {
            return objectMapper.writeValueAsString(token);
        }
        catch (JsonProcessingException exception) {
            throw new ServerException(ExceptionStatus.DATA_CONVERSION_ERROR);
        }
    }

    private Token getToken(String value) {
        try {
            return objectMapper.readValue(value, Token.class);
        }
        catch (JsonProcessingException exception) {
            throw new ServerException(ExceptionStatus.DATA_CONVERSION_ERROR);
        }
    }
}
