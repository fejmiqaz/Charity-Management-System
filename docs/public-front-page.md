# Public front page

The supplied `charity-management-system.html` design is now the Thymeleaf `index.html` at `/`.
The previous `/impact` controller and template have been removed. The protected dashboard is
at `/dashboard` (also `/home`); login and workspace navigation now use that destination.

## Public content

- Only FINISHED projects approved by HEAD: title and assigned year. Existing project approvals are preserved.
- Only events approved by HEAD with a date at or after the current time: purpose and scheduled date,
  sorted nearest first. Past and undated events stay off the front page.
- Charts count published completed projects by assigned year and upcoming public events over the next
  12 calendar months. These are activity counts, not a claimed success rate or financial performance.
- No donation amounts or identities, membership information, budgets, participants, private project
  descriptions, account data, or attachments are passed to the public template.

HEAD can publish/unpublish projects and events on their respective details pages. Approve only titles
and event purposes suitable for public readers. Editing a public event's purpose or date clears its
approval. Existing project reapproval rules are unchanged.

## Setup

1. Run `src/main/resources/db/manual/2026-09-14-public-events.sql` on your PostgreSQL database before
   starting this version. This adds a default-false event flag; all existing events remain private.
   The manual script is repeatable and has not been run against Neon.
2. If the earlier project publication migration has not been applied, also run
   `src/main/resources/db/manual/2026-09-14-public-impact.sql`.
3. Rebuild and restart. Use a clean build so deleted controller/template artifacts do not remain.
4. Check event dates before publication. Event creation/editing now saves the date chosen in the form;
   the previous implementation overwrote it with the save time. Existing dates are not rewritten.
5. `PUBLIC_TIME_ZONE` defaults to `Europe/Skopje`. Event times are stored as local date/time;
   use that same zone when entering the schedule. The front page displays the zone explicitly.

Use the isolated H2 build command in `exports-budget-impact.md` with `clean package` for verification.
The original download is unchanged. No extra chart library is required: charts render on the server,
remain readable without JavaScript, and expose their labels and counts to assistive technology.
