# React frontend — Thymeleaf visual migration v2

This build intentionally preserves the original Thymeleaf CSS and page class names rather than redesigning them.

## Run
1. Keep Spring Boot running on http://localhost:8080.
2. Backend local environment: `API_ALLOWED_ORIGINS=http://localhost:5173`
3. Frontend `.env`: `VITE_API_BASE_URL=http://localhost:8080`
4. `npm install`
5. `npm run dev`

Theme is stored in `localStorage` under `charity-theme`, exactly like the old UI. If the old app was in dark mode, the React app will also open in dark mode.

The JSON API remains the data source. Existing PDF/Excel download routes are linked directly to Spring Boot because those reports remain outside the JSON API.

## Production on Render

The root Dockerfile builds React, copies its output into Spring Boot static resources,
and sets `APP_FRONTEND_REACT=true`. `ReactFrontendConfig` then serves `/index.html`
for explicitly listed frontend GET/HEAD routes, before the legacy page controllers.
Spring Security still authorizes requests; APIs, form submissions, assets, and report
downloads continue to use their existing handlers. Keep this route list in sync with
`frontend/src/App.jsx` (the routing test checks all React routes).

`frontend/.env.production` sets an empty `VITE_API_BASE_URL`, so production API and
download requests use the current origin. Local Vite development can continue to
use `http://localhost:8080` in `frontend/.env`. Vite embeds these settings during
the frontend build; changing runtime environment variables does not rewrite the bundle.

After committing and pushing the changes to Render's linked branch, deploy the latest
commit and wait for it to become Live. No separate frontend service is needed. Confirm
that the homepage loads `/assets/index-*.js`, API requests use the Render domain,
and login, nested-page refreshes, language/theme changes, and report downloads work.

Running Spring Boot directly without `APP_FRONTEND_REACT=true` keeps legacy page
rendering available. To test the bundled frontend locally, use the Docker image with
your local database configuration, or package the built assets into Spring's static
resources and start it with `--app.frontend.react=true`.
