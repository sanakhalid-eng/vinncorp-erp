/**
 * Business modules of the ERP. Each module is a vertical slice: it owns its
 * entities, services, controllers, DTOs, and events.
 *
 * <p>Build order (see {@code docs/architecture/ERP_ROADMAP.md}):
 * <ol>
 *   <li>{@code projects}  - PM tool (existing code, will be refactored in)</li>
 *   <li>{@code hr}        - Employees, departments, attendance, leave, payroll</li>
 *   <li>{@code crm}       - Leads, customers, deals, activities</li>
 *   <li>{@code finance}   - Invoices, payments, expenses, budgets</li>
 * </ol>
 *
 * <p><b>Dependency rule:</b> modules MAY import from {@code com.vinncorp.erp.platform}
 * and {@code com.vinncorp.erp.shared}. They MUST NOT import from sibling modules
 * directly — they go through public service interfaces or events.
 *
 * <p>See {@code docs/architecture/DEPENDENCY_RULES.md} for the full rules.
 */
package com.vinncorp.erp.modules;
