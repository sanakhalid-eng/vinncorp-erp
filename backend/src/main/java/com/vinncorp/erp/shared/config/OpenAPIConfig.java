package com.vinncorp.erp.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PMT-SK API")
                        .description("Project Management Toolkit - SaaS Platform API. Supports workspace management, task tracking, sprint planning, time tracking, and team collaboration.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PMT-SK Support")
                                .email("support@pmt-sk.com")
                                .url("https://pmt-sk.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://pmt-sk.com/license")))
                .externalDocs(new ExternalDocumentation()
                        .description("PMT-SK Documentation")
                        .url("https://docs.pmt-sk.com"))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local development"),
                        new Server().url("https://api.pmt-sk.com").description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .name("bearer-jwt")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT access token obtained from /api/auth/login")))
                .tags(List.of(
                        new Tag().name("Authentication").description("Login, register, refresh tokens, 2FA"),
                        new Tag().name("Projects").description("Project CRUD, members, roles"),
                        new Tag().name("Tasks").description("Task CRUD, filtering, sorting, states"),
                        new Tag().name("Sprints").description("Sprint planning, active sprints, completion"),
                        new Tag().name("Time Tracking").description("Timer, time logs, reports"),
                        new Tag().name("Notifications").description("User notifications, read/unread"),
                        new Tag().name("Activity Log").description("Audit trail and activity history"),
                        new Tag().name("Webhooks").description("Outgoing webhook configuration and delivery"),
                        new Tag().name("Slack").description("Slack integration and commands"),
                        new Tag().name("System").description("System settings, health, ownership transfer"),
                        new Tag().name("Roles & Permissions").description("Role management and permission assignments"),
                        new Tag().name("Reports & Analytics").description("Project reports, burndown charts, analytics"),
                        new Tag().name("Workspaces").description("Workspace management, members, invitations and settings")
                ));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .displayName("Public APIs")
                .pathsToMatch("/api/auth/**", "/api/webhooks/**", "/api/slack/events")
                .build();
    }

    @Bean
    public GroupedOpenApi coreApi() {
        return GroupedOpenApi.builder()
                .group("core")
                .displayName("Core APIs")
                .pathsToMatch("/api/projects/**", "/api/tasks/**", "/api/sprints/**")
                .build();
    }

    @Bean
    public GroupedOpenApi collaborationApi() {
        return GroupedOpenApi.builder()
                .group("collaboration")
                .displayName("Collaboration APIs")
                .pathsToMatch("/api/notifications/**", "/api/activities/**", "/api/invitations/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .displayName("Admin & System APIs")
                .pathsToMatch("/api/system/**", "/api/roles/**", "/api/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi workspaceApi() {
        return GroupedOpenApi.builder()
                .group("workspaces")
                .displayName("Workspace APIs")
                .pathsToMatch("/api/workspaces/**", "/api/workspace-invitations/**")
                .build();
    }

    @Bean
    public GroupedOpenApi integrationApi() {
        return GroupedOpenApi.builder()
                .group("integrations")
                .displayName("Integration APIs")
                .pathsToMatch("/api/webhooks/**", "/api/slack/**", "/api/time-tracking/**")
                .build();
    }
}

