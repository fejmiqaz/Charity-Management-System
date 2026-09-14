-- Manual migration; existing events remain private until HEAD approves publication.
BEGIN;
ALTER TABLE event ADD COLUMN IF NOT EXISTS public_visible boolean NOT NULL DEFAULT false;
UPDATE event SET public_visible = false WHERE public_visible IS NULL;
ALTER TABLE event ALTER COLUMN public_visible SET DEFAULT false;
ALTER TABLE event ALTER COLUMN public_visible SET NOT NULL;
COMMIT;
