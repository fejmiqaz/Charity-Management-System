# Activity and Security

Head accounts see Activity and Security beneath Profile in the account menu, with tabs at `/profile/activity` and `/profile/security`. Non-Heads cannot load these pages or their backend endpoints. Backend checks use the current account role, not only session authorities.

Activity is stored in the new `audit_entries` database table. The existing `spring.jpa.hibernate.ddl-auto=update` creates it on backend startup. It records successful API changes to organization records, password and role changes, sign-ins, failed password sign-ins, sign-outs, and session revocations. Entries contain an actor snapshot, action, resource, record ID when available, timestamp, and endpoint reference. Entries survive member deletion. There is no history backfill, request-body capture, password capture, or before/after field-value comparison. Failed validation and denied mutations are excluded. Legacy web form mutations are excluded because a returned form with HTTP 200 does not establish that a change succeeded.

Security provides active authenticated sessions, failed sign-ins in the last 24 hours, enabled/password/Google status for accounts, and confirmed session revocation. Session references are SHA-256 hashes, never usable session cookies. Revocation invalidates the servlet session; the next request must authenticate again. The Head cannot revoke their current session from this page. Sessions are tracked in memory on one backend instance, removed by servlet destruction/ID-change events, and checked for inactivity expiry. This is a session inventory, not live online presence. A multi-instance deployment would need a shared session registry before presenting an organization-wide session inventory.

Audit endpoints are read-only and paginated (maximum 100 records per page). Retention follows the database lifecycle; no automatic audit deletion is configured. Audit writes use a separate transaction and report database failures to server logs without changing the result of an already-completed business operation.

Existing API payloads remain unchanged. New endpoints:

- `GET /api/management/activity?page=0&size=20&action=&actorId=`
- `GET /api/management/security`
- `POST /api/management/security/sessions/{reference}/revoke` (CSRF required)

Profile edits use a router navigation blocker for links and browser Back, plus the native browser unload warning for reloads or closing the tab. Google status distinguishes an unconnected account from a provider disabled by the organization. Notifications use the existing `POST /api/notifications/read-all` endpoint and refresh the unread count immediately.

Validation: frontend build/lint/localization/API tests; backend `ManagementTests`, `ApiTests`, `GoogleAuthenticationTests`, `ProfileTests`, and `RequestRateLimitFilterTests`; live isolated H2 HTTP checks for tracking, revocation and auditing; browser checks for menu placement/closing, Head-only UI, Activity/Security rendering, marking all notifications read, and unsaved Cancel/Back navigation.
