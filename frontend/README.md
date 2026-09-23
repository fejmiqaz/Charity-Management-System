# Charity Management React frontend

Run `npm install` and `npm run dev`. Start (or restart) the Spring Boot application too: the restored overview screens require `TemplateApiController` in the backend.

The frontend uses the original Thymeleaf navy/light-blue stylesheet and preserves the template page sections, field ordering, tables, financial summaries, and role-based actions. Pages are grouped under `src/pages/<feature>/`, with `list.jsx`, `form.jsx`, and `details.jsx` where applicable. Shared form, table, modal, loading, and validation behavior lives in `src/components/`.

Restored flows include year summaries, budget details, project revenue, event tasks and payments, membership fees and receipt corrections, member payment status, profile history, notifications, registration, currency conversion, and report links. The public homepage is served by React at `/`, using `/api/public/home` for approved projects, upcoming events, and progress charts. PDF/Excel exports continue to use Spring Boot routes.

`src/api/api.js` manages authenticated requests, CSRF, and validation errors. `src/api/activity.js` includes task, revenue, publication, and template-overview calls. Backend authorization remains authoritative.

Validation commands: `npm run lint`, `npm test`, and `npm run build`. Backend coverage: `mvnw.cmd -Dtest=TemplateApiTests test` from the repository root. Browser checks used isolated fixture data; live report downloads and a production authenticated session still require a running backend.

The build currently emits two non-fatal React Router `use client` bundling warnings.

Note: the parent repository currently ignores the `frontend` directory. Adjust that repository rule before committing this frontend.

