package com.see.realview.token.service;

import com.see.realview._core.exception.ExceptionStatus;
import com.see.realview._core.exception.client.NotFoundException;
import com.see.realview._core.security.JwtProvider;
import com.see.realview.token.entity.Token;
import com.see.realview.token.repository.TokenRedisRepository;
import com.see.realview.token.repository.TokenRedisRepositoryImpl;
import com.see.realview.user.entity.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService {

    private final TokenRedisRepository tokenRedisRepository;

    private final JwtProvider jwtProvider;


    public TokenServiceImpl(@Autowired TokenRedisRepository tokenRedisRepository,
                            @Autowired JwtProvider jwtProvider) {
        this.tokenRedisRepository = tokenRedisRepository;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Token findTokenByEmail(String email) {
        return tokenRedisRepository.findTokenByEmail(email)
                .orElseThrow(() -> new NotFoundException(ExceptionStatus.TOKEN_NOT_FOUND));
    }

    @Override
    public void save(String email, Token token) {
        if (tokenRedisRepository.isTokenExists(email)) {
            deleteByEmail(email);
        }

        tokenRedisRepository.save(email, token);
    }

    @Override
    public Token refresh(UserAccount userAccount) {
        String accessToken = jwtProvider.createAccessToken(userAccount);
        String refreshToken = jwtProvider.createRefreshToken(userAccount);

        Token token = new Token(accessToken, refreshToken);
        save(userAccount.getEmail(), token);

        return token;
    }

    @Override
    public void deleteByEmail(String email) {
        tokenRedisRepository.deleteById(email);
    }
}
