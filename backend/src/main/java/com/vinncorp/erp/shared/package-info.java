/**
 * Shared infrastructure for the entire ERP.
 *
 * <p>Cross-cutting concerns that every module needs:
 * <ul>
 *   <li>{@code security}     - JWT, encryption, OAuth2, permission resolvers</li>
 *   <li>{@code storage}      - File upload abstraction (local + Cloudinary)</li>
 *   <li>{@code email}        - Email sending and Thymeleaf templates</li>
 *   <li>{@code exception}    - Global exception handler, error codes, custom exceptions</li>
 *   <li>{@code filter}       - Servlet filters (rate limit, timing, security headers)</li>
 *   <li>{@code cache}        - Cache abstraction (in-memory + Redis)</li>
 *   <li>{@code tenant}       - Tenant context resolvers and access validators</li>
 *   <li>{@code mapper}       - Generic mappers (pagination, etc.)</li>
 *   <li>{@code config}       - Spring configuration (security, async, Jackson, etc.)</li>
 *   <li>{@code scheduling}   - Quartz/cron jobs and background workers</li>
 *   <li>{@code websocket}    - WebSocket/STOMP configuration and dispatchers</li>
 *   <li>{@code util}         - Generic utilities (no business logic)</li>
 * </ul>
 *
 * <p><b>Dependency rule:</b> this package MUST NOT import from {@code com.vinncorp.erp.core}
 * or {@code com.vinncorp.erp.modules}. It is a leaf in the dependency graph.
 */
package com.vinncorp.erp.shared;
