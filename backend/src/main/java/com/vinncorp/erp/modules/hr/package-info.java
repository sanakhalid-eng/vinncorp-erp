/**
 * HR (Human Resources) module - the first domain module of the VinnCorp ERP.
 *
 * <p>Scope:
 * <ul>
 *   <li>Employee lifecycle: hire, profile, status, offboard.</li>
 *   <li>Organisational structure: departments and reporting hierarchy.</li>
 *   <li>Job catalogue: designations and employment type.</li>
 * </ul>
 *
 * <p>Identity model:
 * <ul>
 *   <li>{@code User} (in {@code core.user}) is the auth identity - email, password, roles, login.</li>
 *   <li>{@code Employee} is the HR business identity - profile, employment details, payroll data.</li>
 *   <li>{@code Employee} has a 1:1 link to {@code User} via {@code user_id} when an employee has a portal login.</li>
 *   <li>An Employee may exist without a User (e.g. paper records, contractors) - {@code user_id} is nullable.</li>
 * </ul>
 *
 * <p>Boundary rules (see {@code docs/architecture/DEPENDENCY_RULES.md}):
 * <ul>
 *   <li>This module can depend on {@code core.*} and {@code shared.*}.</li>
 *   <li>This module must NOT depend on {@code modules.projects}, {@code modules.crm}, or {@code modules.finance}.</li>
 *   <li>{@code core.*} and {@code modules.projects} must NOT depend on this module.</li>
 * </ul>
 */
package com.vinncorp.erp.modules.hr;
