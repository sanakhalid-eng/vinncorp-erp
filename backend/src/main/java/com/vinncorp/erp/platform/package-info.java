/**
 * Core domain: identity, tenancy, and cross-module plumbing.
 *
 * <p>Everything every business module needs to exist:
 * <ul>
 *   <li>{@code auth}            - Login, registration, JWT, 2FA, password reset, refresh tokens</li>
 *   <li>{@code user}            - The {@code User} entity and RBAC user-role mapping</li>
 *   <li>{@code workspace}       - The {@code Workspace} tenant root, members, invitations, notes</li>
 *   <li>{@code notification}    - In-app notification feed, preferences, async event processors</li>
 *   <li>{@code audit}           - Activity log, AI summaries, cross-module audit trail</li>
 *   <li>{@code integrations}    - Webhooks, Slack, feature flags</li>
 *   <li>{@code automation}      - Generic SLA policies and escalation rules (v2)</li>
 *   <li>{@code shared}          - Convenience re-exports / cross-core helpers</li>
 * </ul>
 *
 * <p><b>Dependency rule:</b> this package MAY import from {@code com.vinncorp.erp.shared}.
 * It MUST NOT import from {@code com.vinncorp.erp.modules}.
 */
package com.vinncorp.erp.core;

