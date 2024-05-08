package com.see.realview.user.service;

import com.see.realview._core.exception.ExceptionStatus;
import com.see.realview._core.exception.client.BadRequestException;
import com.see.realview._core.exception.client.NotFoundException;
import com.see.realview._core.security.JwtProvider;
import com.see.realview.code.entity.EmailCode;
import com.see.realview.code.repository.EmailCodeRedisRepository;
import com.see.realview.token.entity.Token;
import com.see.realview.token.service.TokenService;
import com.see.realview.user.dto.request.LoginRequest;
import com.see.realview.user.dto.request.RegisterRequest;
import com.see.realview.user.entity.UserAccount;
import com.see.realview.user.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserAccountRepository userAccountRepository;

    private final TokenService tokenService;

    private final EmailCodeRedisRepository emailCodeRedisRepository;

    private final JwtProvider jwtProvider;

    private final PasswordEncoder passwordEncoder;


    public UserServiceImpl(@Autowired UserAccountRepository userAccountRepository,
                           @Autowired TokenService tokenService,
                           @Autowired EmailCodeRedisRepository emailCodeRedisRepository,
                           @Autowired JwtProvider jwtProvider,
                           @Autowired PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.tokenService = tokenService;
        this.emailCodeRedisRepository = emailCodeRedisRepository;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        checkEmailAlreadyExist(request);
        checkEqualPassword(request);
        checkEmailAuthenticated(request);

        UserAccount userAccount = UserAccount.builder()
                .email(request.email())
                .name(request.username())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userAccountRepository.save(userAccount);
        emailCodeRedisRepository.delete(request.email());
    }

    @Override
    public Token login(LoginRequest request) {
        UserAccount userAccount = findUserAccountByEmail(request);

        checkPassword(request, userAccount);

        String accessToken = jwtProvider.createAccessToken(userAccount);
        String refreshToken = jwtProvider.createRefreshToken(userAccount);
        Token token = new Token(accessToken, refreshToken);

        tokenService.save(userAccount.getEmail(), token);
        return new Token(accessToken, refreshToken);
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return userAccountRepository.findUserAccountByEmail(email);
    }

    private void checkEmailAlreadyExist(RegisterRequest request) {
        userAccountRepository.findUserAccountByEmail(request.email())
                .ifPresent(user -> {
                    throw new BadRequestException(ExceptionStatus.EMAIL_ALREADY_EXIST);
                });
    }

    private static void checkEqualPassword(RegisterRequest request) {
        if (!request.password().equals(request.password2())) {
            throw new BadRequestException(ExceptionStatus.PASSWORD_NOT_EQUALS);
        }
    }

    private void checkEmailAuthenticated(RegisterRequest request) {
        boolean isAuthenticated = isAuthenticatedEmail(request.email());

        if (!isAuthenticated) {
            throw new BadRequestException(ExceptionStatus.EMAIL_NOT_AUTHENTICATED);
        }
    }

    private Boolean isAuthenticatedEmail(String email) {
        EmailCode emailCode = emailCodeRedisRepository.findCodeByEmail(email)
                .orElseThrow(() -> new NotFoundException(ExceptionStatus.EMAIL_AUTHENTICATION_CODE_NOT_FOUND));

        return emailCode.authenticated();
    }

    private void checkPassword(LoginRequest request, UserAccount userAccount) {
        if (!passwordEncoder.matches(request.password(), userAccount.getPassword())) {
            throw new BadRequestException(ExceptionStatus.INVALID_PASSWORD);
        }
    }

    private UserAccount findUserAccountByEmail(LoginRequest request) {
        return userAccountRepository.findUserAccountByEmail(request.email())
                .orElseThrow(() -> new BadRequestException(ExceptionStatus.EMAIL_NOT_FOUND));
    }
}
