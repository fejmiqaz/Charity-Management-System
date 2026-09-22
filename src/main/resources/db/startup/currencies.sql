-- Run before Hibernate schema update. Existing amounts were entered as EUR.
-- The block is safe on both existing and empty PostgreSQL databases.
DO $$
DECLARE finance_table text;
BEGIN
    FOREACH finance_table IN ARRAY ARRAY['donation', 'project_revenue', 'task_payment', 'membership_payment'] LOOP
        IF to_regclass('public.' || finance_table) IS NOT NULL THEN
            EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS currency varchar(3) DEFAULT %L', finance_table, 'EUR');
            EXECUTE format('UPDATE %I SET currency = %L WHERE currency IS NULL', finance_table, 'EUR');
            EXECUTE format('ALTER TABLE %I ALTER COLUMN currency SET DEFAULT %L', finance_table, 'EUR');
            EXECUTE format('ALTER TABLE %I ALTER COLUMN currency SET NOT NULL', finance_table);
        END IF;
    END LOOP;
END $$@@

DO $$
BEGIN
    IF to_regclass('public.event') IS NOT NULL THEN
        ALTER TABLE event ADD COLUMN IF NOT EXISTS public_visible boolean DEFAULT false;
        ALTER TABLE event ADD COLUMN IF NOT EXISTS event_type varchar(32) DEFAULT 'NORMAL';
        UPDATE event SET public_visible = false WHERE public_visible IS NULL;
        UPDATE event SET event_type = 'NORMAL' WHERE event_type IS NULL;
        ALTER TABLE event ALTER COLUMN public_visible SET NOT NULL;
        ALTER TABLE event ALTER COLUMN event_type SET NOT NULL;
    END IF;
    IF to_regclass('public.project') IS NOT NULL THEN
        ALTER TABLE project ADD COLUMN IF NOT EXISTS public_impact boolean DEFAULT false;
        ALTER TABLE project ADD COLUMN IF NOT EXISTS project_type varchar(32) DEFAULT 'STANDARD';
        UPDATE project SET public_impact = false WHERE public_impact IS NULL;
        UPDATE project SET project_type = 'STANDARD' WHERE project_type IS NULL;
        ALTER TABLE project ALTER COLUMN public_impact SET NOT NULL;
        ALTER TABLE project ALTER COLUMN project_type SET NOT NULL;
    END IF;
END $$@@
