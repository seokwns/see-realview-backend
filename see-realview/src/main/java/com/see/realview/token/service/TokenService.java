package com.see.realview.token.service;

import com.see.realview.token.entity.Token;
import com.see.realview.user.entity.UserAccount;

public interface TokenService {

    Token findTokenByEmail(String email);

    void save(String email, Token token);

    Token refresh(UserAccount userAccount);

    void deleteByEmail(String email);
}
