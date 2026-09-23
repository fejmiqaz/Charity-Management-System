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
