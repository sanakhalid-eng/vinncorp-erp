package com.vinncorp.erp.core.user.mapper;

import com.vinncorp.erp.core.user.response.UserResponse;
import com.vinncorp.erp.core.user.entity.User;

import java.util.Collections;

import java.util.stream.Collectors;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roles(
                        user.getUserRoles() == null
                        ? Collections.emptySet()
                        : user.getUserRoles().stream()
                        .map(ur -> ur.getRole().getName())
                        .collect(Collectors.toSet())
                )
                .createdAt(user.getCreatedAt())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}

