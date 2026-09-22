-- Run once before deploying the currency columns to an existing PostgreSQL database.
-- All historical amounts were recorded and displayed as EUR.
ALTER TABLE donation ADD COLUMN IF NOT EXISTS currency varchar(3) NOT NULL DEFAULT 'EUR';
ALTER TABLE project_revenue ADD COLUMN IF NOT EXISTS currency varchar(3) NOT NULL DEFAULT 'EUR';
ALTER TABLE task_payment ADD COLUMN IF NOT EXISTS currency varchar(3) NOT NULL DEFAULT 'EUR';
ALTER TABLE membership_payment ADD COLUMN IF NOT EXISTS currency varchar(3) NOT NULL DEFAULT 'EUR';
UPDATE donation SET currency = 'EUR' WHERE currency IS NULL;
UPDATE project_revenue SET currency = 'EUR' WHERE currency IS NULL;
UPDATE task_payment SET currency = 'EUR' WHERE currency IS NULL;
UPDATE membership_payment SET currency = 'EUR' WHERE currency IS NULL;
