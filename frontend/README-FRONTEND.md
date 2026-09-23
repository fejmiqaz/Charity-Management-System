# Charity Management React frontend

## Run
1. Copy `.env.example` to `.env`.
2. On the Spring Boot environment set `API_ALLOWED_ORIGINS=http://localhost:5173`.
3. Install/run:
   npm install
   npm run dev

The frontend uses the existing Spring session cookie and CSRF API. It does not use JWT.
The starter implements authentication, protected routing, dashboard, years, members,
year-scoped project/event/donation/budget views, and notifications. Expand the generic
JSON views into the final CRUD UI once the exact response DTO shapes are confirmed.
