package com.see.realview.user.entity.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    @Getter
    private final String roleName;
}
