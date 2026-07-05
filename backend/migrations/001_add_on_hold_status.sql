-- Adds ON_HOLD to the set of allowed todos.status values.
--
-- Why this is needed: Hibernate's `ddl-auto=update` only adds missing tables/
-- columns — it never alters a CHECK constraint that already exists on a
-- running database. Databases created before the ON_HOLD status was
-- introduced still have the old 3-value constraint and will reject any
-- attempt to set a task to ON_HOLD until this is run.
--
-- Safe to run multiple times and safe on a fresh database that has never
-- had the old constraint (the DROP is a no-op there).
--
-- Usage:
--   psql -U <username> -h <host> -d todo_db -f backend/migrations/001_add_on_hold_status.sql

ALTER TABLE todos DROP CONSTRAINT IF EXISTS todos_status_check;
ALTER TABLE todos ADD CONSTRAINT todos_status_check
    CHECK (status::text = ANY (ARRAY['PENDING', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED']::text[]));
