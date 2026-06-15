package com.vinncorp.erp.modules.projects.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRequest {
    private String url;
    private String secret;
    private List<String> events;
    private Boolean isActive;
}



