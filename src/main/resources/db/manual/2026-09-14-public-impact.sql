-- Manual migration; existing projects remain private until HEAD approves publication.
BEGIN;
ALTER TABLE project ADD COLUMN IF NOT EXISTS public_impact boolean NOT NULL DEFAULT false;
UPDATE project SET public_impact = false WHERE public_impact IS NULL;
ALTER TABLE project ALTER COLUMN public_impact SET DEFAULT false;
ALTER TABLE project ALTER COLUMN public_impact SET NOT NULL;
COMMIT;
