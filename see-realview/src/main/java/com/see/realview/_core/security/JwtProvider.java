package com.see.realview._core.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.see.realview.user.entity.UserAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class JwtProvider {

    public final Long ACCESS_TOKEN_EXP = 1000L * 60 * 15;

    public final Long REFRESH_TOKEN_EXP = 1000L * 60 * 60 * 24 * 3;

    public final static String TOKEN_PREFIX = "Bearer ";

    @Value("${security.jwt.secret.access}")
    public String ACCESS_TOKEN_SECRET;

    @Value("${security.jwt.secret.refresh}")
    private String REFRESH_TOKEN_SECRET;


    public String createAccessToken(UserAccount user) {
        return create(user, ACCESS_TOKEN_EXP, ACCESS_TOKEN_SECRET);
    }

    public String createRefreshToken(UserAccount user) {
        return create(user, REFRESH_TOKEN_EXP, REFRESH_TOKEN_SECRET);
    }

    private String create(UserAccount user, Long expire, String secret) {
        return JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + expire))
                .withClaim("email", user.getEmail())
                .sign(Algorithm.HMAC512(secret));
    }

    public DecodedJWT verifyAccessToken(String token) {
        return verify(token, ACCESS_TOKEN_SECRET);
    }

    public DecodedJWT verifyRefreshToken(String token) {
        return verify(token, REFRESH_TOKEN_SECRET);
    }

    private DecodedJWT verify(String token, String secret) {
        return JWT
                .require(Algorithm.HMAC512(secret))
                .build()
                .verify(token);
    }

    public boolean isValidAccessToken(String token) {
        try {
            verify(token, ACCESS_TOKEN_SECRET);
            return true;
        }
        catch (JWTVerificationException exception) {
            return false;
        }
    }
}
