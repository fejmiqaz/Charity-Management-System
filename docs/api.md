# JSON API

The `/api` endpoints run alongside the existing Thymeleaf pages. Spring Security and business services stay in the Spring Boot backend. Responses use DTOs rather than JPA entities or password-bearing account objects.

## Authentication from a TypeScript frontend

Authentication uses the existing server session cookie, not JWTs. Send `credentials: "include"` on every request. Before login or another write, fetch `/api/auth/csrf` and send its token using the returned header name. Fetch a fresh token after login because login rotates both the session ID and CSRF token. Logout requires CSRF too.

```ts
const base = "http://localhost:8080"; // Use "" when served from the backend origin.
let csrf: { headerName: string; token: string };

async function refreshCsrf() {
  const response = await fetch(`${base}/api/auth/csrf`, {
    credentials: "include",
  });
  if (!response.ok) throw new Error("Could not obtain CSRF token");
  csrf = await response.json();
}

async function api<T>(path: string, method = "GET", body?: unknown): Promise<T> {
  const write = !["GET", "HEAD", "OPTIONS"].includes(method);
  if (write && !csrf) await refreshCsrf();
  const response = await fetch(`${base}/api${path}`, {
    method,
    credentials: "include",
    headers: {
      Accept: "application/json",
      ...(body !== undefined ? { "Content-Type": "application/json" } : {}),
      ...(write ? { [csrf.headerName]: csrf.token } : {}),
    },
    ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
  });
  if (!response.ok) throw await response.json();
  return response.status === 204 ? undefined as T : response.json();
}

await refreshCsrf();
await api("/auth/login", "POST", { email: "your-email@example.com", password: "your-password" });
await refreshCsrf();
const projects = await api("/years/1/projects");
// await api("/auth/logout", "POST");
// await refreshCsrf();
```

For a frontend development server, set `API_ALLOWED_ORIGINS=http://localhost:5173` on the backend. Multiple exact origins may be comma-separated; the default is same-origin only. Wildcard origins are not supported with credentials. Prefer serving production frontend and API under the same origin (for example, proxy `/api` to Spring Boot). Separate cross-site domains additionally require deliberate session-cookie configuration and are subject to browser third-party-cookie restrictions; CORS alone does not enable those cookies.

## Routes

All paths below start with `/api`. Only CSRF retrieval, login and registration allow anonymous access; writes still require CSRF. IDs in paths are database IDs, including `yearId`, not calendar-year values.

| Resource | Routes |
| --- | --- |
| Authentication | `GET /auth/csrf`, `POST /auth/login`, `POST /auth/register`, `GET /auth/me`, `POST /auth/logout` |
| Years | `GET, POST /years`; `GET, PUT, DELETE /years/{id}` |
| Members | `GET, POST /members`; `GET, PUT, DELETE /members/{id}` |
| Projects | `GET, POST /years/{yearId}/projects`; `GET, PUT, DELETE /years/{yearId}/projects/{id}` |
| Donations | `GET, POST /years/{yearId}/donations`; `GET, PUT, DELETE /years/{yearId}/donations/{id}` |
| Events | `GET, POST /years/{yearId}/events`; `GET, PUT, DELETE /years/{yearId}/events/{id}` |
| Budget | `GET, POST, PUT, DELETE /years/{yearId}/budget` |
| Event tasks | `GET, POST /years/{yearId}/events/{eventId}/tasks`; `GET, DELETE /years/{yearId}/events/{eventId}/tasks/{taskId}` |
| Task completion | `PATCH /years/{yearId}/events/{eventId}/tasks/{taskId}/status` |
| Task payments | `POST /years/{yearId}/events/{eventId}/tasks/{taskId}/payments` |
| Project revenue | `GET, POST /years/{yearId}/projects/{projectId}/revenues` |
| Public visibility | `PUT /years/{yearId}/projects/{projectId}/publication`; `PUT /years/{yearId}/events/{eventId}/publication` |
| Memberships | `GET /memberships?year=2027`; `GET /memberships/members/{memberId}`; `POST /memberships/payments`; `PUT /memberships/{year}/fee`; `POST /memberships/payments/{id}/void` |
| Profile | `GET, PUT /profile` |
| Notifications | `GET, DELETE /notifications`; `GET /notifications/summary`; `POST /notifications/{id}/read`; `POST /notifications/read-all` |
| Dashboard and form options | `GET /dashboard`, `GET /options/years`, `GET /options/members`, `GET /options/enums` |

Projects support `type` and `status` query filters; events support `type` and date-derived `status`. Years support `yearValue`, `page`, `size` and `sortDir`. Members support `search`, `country`, `city`, `role`, `page` and `size`. Notifications accept `page`.

Year, member and notification lists return `{content, page, size, totalElements, totalPages}` with zero-based pages. Project, donation and event lists return arrays. Membership lists return `{year, fee, totals, payments}`. Enum options supply the backend's accepted role, currency, type and status values.

## Request bodies

Core create and full-update requests reuse the validated classes in `DTO`: `YearsFormDto`, `MemberFormDto`, `ProjectFormDto`, `DonationFormDto`, `EventFormDto`, `BudgetFormDto`, `ProfileFormDto` and `RegistrationDto`. Submitted resource IDs and nested year IDs cannot override the path. Referenced members must exist; referenced budget donations must belong to the same year.

Examples (replace member IDs and enum values with your own):

```json
{"yearValue": 2027}
```

```json
{"name":"Community project","description":"Support our local community.","projectType":"STANDARD","status":"FINISHED","projectPrice":1500,"memberIds":[1]}
```

```json
{"donationAmount":100,"currency":"EUR","memberIds":[1]}
```

```json
{"purpose":"Community gathering","eventType":"NORMAL","date":"2029-06-15T18:00:00","memberIds":[1]}
```

```json
{"budgetAmount":5000,"description":"Annual budget","memberIds":[],"donationIds":[]}
```

Event dates must be in the future and use local date-time format. Event-status filtering uses the configured public time zone. PUT sends the complete form, not only changed fields.

| Operation | JSON fields |
| --- | --- |
| Create task | `title`, `description` (optional), `price` (nonnegative), `memberIds` |
| Complete/reopen task | `completed` (boolean) |
| Task payment | `memberId` (optional), `amount` (positive), `currency`, `paidOn` (`YYYY-MM-DD`, not future), `note` (optional) |
| Project revenue | `month` (`YYYY-MM`), `customer`, `amount` (positive), `currency`, `note` (optional) |
| Publication | `published` (boolean) |
| Membership payment | `memberId`, `year` (calendar year), `amount` (optional; defaults to annual fee), `currency`, `paidOn` |
| Membership fee | `amount` (positive) |
| Void membership payment | `reason` |

Task assignment uses the existing notification/email workflow. Clearing notifications affects only the authenticated user's inbox. Updating a profile email returns `{"loginRequired":true}` and ends the session; otherwise it returns `false`.

## Permissions

Existing URL restrictions and controller permissions are preserved and both must pass. MEMBER cannot create, edit or delete administrative resources. Profile and notification operations always use the authenticated account.

| Operation | Effective roles |
| --- | --- |
| Year read / write | HEAD, SUBHEAD, MEMBER / HEAD |
| Member read / create / update-delete | HEAD, SUBHEAD, TREASURER, MEMBER / HEAD, SUBHEAD / HEAD |
| Project list / detail / write | HEAD, SUBHEAD, PROJECT_MANAGER, VOLUNTEER, MEMBER / HEAD, SUBHEAD, MEMBER / HEAD, SUBHEAD, PROJECT_MANAGER |
| Donation read / write | HEAD, SUBHEAD, TREASURER, MEMBER / HEAD, SUBHEAD, TREASURER |
| Event list / detail / write | HEAD, SUBHEAD, EVENT_MANAGER, VOLUNTEER, MEMBER / HEAD, SUBHEAD, EVENT_MANAGER, MEMBER / HEAD, SUBHEAD |
| Task read / write and payments | HEAD, SUBHEAD, EVENT_MANAGER, MEMBER / HEAD, SUBHEAD, EVENT_MANAGER |
| Revenue read / write | HEAD, SUBHEAD, MEMBER / HEAD, SUBHEAD, PROJECT_MANAGER |
| Budget read / write | HEAD, SUBHEAD, TREASURER, MEMBER / HEAD, TREASURER |
| Membership management | HEAD, SUBHEAD, TREASURER |
| Publication | HEAD |

Some existing rules differ between lists and details: PROJECT_MANAGER can manage projects but cannot read their detail endpoint, and EVENT_MANAGER can manage tasks but cannot create/edit events. TREASURER is excluded by the existing project/event URL restrictions even where a method mentions that role. This API does not silently expand those permissions. Member form options expose only IDs and names to HEAD, SUBHEAD, TREASURER, PROJECT_MANAGER and EVENT_MANAGER.

## Responses and errors

Creates return `201`; core resource/task creates also include a `Location` header. Reads and updates return `200`; delete, logout and action endpoints normally return `204` without a body. The API returns JSON errors instead of redirects or HTML error pages:

```json
{"status":400,"error":"Bad Request","message":"Check the submitted fields.","fields":{"name":"Project name is required"}}
```

Expect `400` for invalid input, `401` for missing/invalid authentication, `403` for insufficient permissions or missing/invalid CSRF, `404` for missing resources or mismatched year/event scope, `409` for data conflicts, and `429` for login/registration rate limits (with `Retry-After`). Login and registration share their limits with the HTML routes. Passwords, hashes and internal exceptions are not included in responses.

Existing PDF/Excel reports remain on their existing download routes. The JSON API does not replace the templates or change deployment automatically.
