# Notifications and email setup

The authenticated header contains a bell with the unread count and five most recent
notifications. `/notifications` displays the user's own inbox, 20 items per page.
Read and mark-all-read actions require POST and CSRF protection and are scoped to
the signed-in account, regardless of any supplied notification ID. Notifications
remain available across logins. Counts refresh on page navigation/reload.

## What is sent

- Assigning members when creating an event task creates an in-app notification for
  each enabled linked account and queues an email to each assigned member.
  Legacy members without an account still receive assignment email. Duplicate
  member IDs do not create duplicate messages. No historical assignments are emailed
  merely by installing this feature.
- Every enabled app account, including donors and sponsors, receives upcoming-event
  reminders both in-app and by email at 21, 7 and 1 calendar days before the event.
  Disabled accounts are excluded. Full event links are only shown for roles already
  permitted to access event details; every recipient can read their own alert.
- Calendar calculations use `PUBLIC_TIME_ZONE` (default `Europe/Skopje`). A reminder
  scan runs 60 seconds after startup and every 15 minutes afterward. The app must be
  running for scheduling and delivery to occur.
- A late-created event or restart after downtime catches up **only the current
  milestone**: 8–21 days remaining gets the 21-day reminder, 2–7 gets the one-week
  reminder, and 1 gets the day-before reminder. It does not send all missed reminders
  together. There are no catch-up reminders on or after the event day.
- Deduplication is per account, event date/time and milestone. Rescheduling allows
  reminders for the new date. Previous in-app alerts remain as dated history.
  Email delivery cancels old reminders if the event is deleted/rescheduled or the
  milestone has passed. Task emails are cancelled if the member is removed from the
  task, the task is completed/deleted, the recipient is disabled/deleted, or the
  event has already happened.

## Enable real email

In-app notifications and durable email queuing work without SMTP. Actual delivery
is disabled by default. Configure these environment variables on the application
server (do not commit real credentials):

```text
NOTIFICATIONS_EMAIL_ENABLED=true
NOTIFICATIONS_FROM=Charity <notifications@your-domain.example>
APP_BASE_URL=https://your-app.example
MAIL_HOST=smtp.your-provider.example
MAIL_PORT=587
MAIL_USERNAME=your-smtp-user
MAIL_PASSWORD=your-smtp-password-or-app-password
MAIL_SMTP_AUTH=true
MAIL_STARTTLS=true
```

Use a sender address/domain authorized by your mail provider. `APP_BASE_URL` is the
externally accessible app URL, including a context path if applicable. A missing
SMTP host, sender, or HTTP(S) app URL leaves messages queued with a warning.
For a provider requiring implicit TLS on port 465, set `MAIL_STARTTLS=false` and
`SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_ENABLE=true` with the provider's port.

An email worker runs every 30 seconds and processes up to 50 due messages per pass.
SMTP connection/read/write timeouts are 5 seconds. Failed attempts retry with an
increasing delay (2, 4, 8, 16, 32, 64 and 128 minutes); after 8 failed attempts the
row is marked `FAILED`. Logs identify the delivery ID and attempt without printing
message contents, credentials or recipient addresses. Inspect `email_deliveries`
for `PENDING`, `SENT`, `CANCELLED`, or `FAILED` status; after correcting a permanent
mail configuration failure, an operator can reset failed rows to `PENDING`, reset
`attempts` to 0 and set `next_attempt_at` to the current timestamp.

The outbox is committed in the same database transaction as the task/notification,
so rolled-back assignments cannot generate emails. Event and delivery row locks
prevent concurrent workers from generating/sending the same record simultaneously.
As with ordinary SMTP, a process crash after the mail server accepts a message but
before the database records success can result in a duplicate on retry; this is
at-least-once delivery, not an exactly-once guarantee.

Set `NOTIFICATIONS_SCHEDULING_ENABLED=false` to disable background workers (for
tests or maintenance). This does not remove persisted notifications or queued mail.
The existing JPA `ddl-auto=update` configuration creates `notifications` and
`email_deliveries` and their indexes/constraints on startup. Neither table is
automatically purged; define a retention policy before long-term large-scale use.

## Verification

`./mvnw test -Dtest=NotificationTests,WorkspaceRenderingTests,ActivityFinanceTests`
uses H2 and a mocked SMTP sender. These tests send no real email.
