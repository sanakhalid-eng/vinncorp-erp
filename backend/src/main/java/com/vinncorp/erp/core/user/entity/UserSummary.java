package com.vinncorp.erp.core.user.entity;

import lombok.Data;

@Data
public class UserSummary {

    private Long id;
    private String name;
    private String email;
    private String avatarUrl;
}
