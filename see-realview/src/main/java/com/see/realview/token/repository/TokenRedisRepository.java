package com.see.realview.token.repository;

import com.see.realview.token.entity.Token;

import java.util.Optional;

public interface TokenRedisRepository {

    Optional<Token> findTokenByEmail(String email);

    void save(String email, Token token);

    void deleteById(String email);

    boolean isTokenExists(String email);
}
