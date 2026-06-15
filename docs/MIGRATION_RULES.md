# Flyway Migration Rules

These rules **must** be followed by every developer to prevent environment corruption.

## DOs

- Add a **new** versioned migration file for every schema change.
- Name files sequentially: `V<next_number>__<descriptive_name>.sql`
- Keep migrations **immutable** after they have been applied to any environment.
- Use **only forward** migrations — never roll back via editing.

## NEVER

- **Edit** an old migration that has already been merged / applied.
- **Delete** an old migration that exists in version control.
- **Rename** an old migration (even the `__description` part — Flyway uses the checksum).
- **Reuse** a version number.

## Why

Flyway computes a **checksum** of every applied migration. If the file content changes, Flyway
refuses to boot with:

> `Migration checksum mismatch for migration version X`

This is by design — it prevents silent schema drift.

If you need to change the schema:

1. Understand the current state by looking at existing migrations.
2. Add a new migration on top:

```sql
-- V5__fix_something.sql
ALTER TABLE ...
```

## Local Development

- Use `spring.flyway.clean-disabled=false` only in throwaway local databases.
- **Never** run `flyway clean` on shared / staging / production databases.

## Baseline

If you need to introduce Flyway to an existing database that already has the schema:

```properties
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
```

This tells Flyway: "treat all existing migrations as already applied" by inserting
baseline entries into the `flyway_schema_history` table.
