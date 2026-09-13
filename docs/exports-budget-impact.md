# Excel exports, budget alerts and public impact

## Setup

1. Reload the Maven project in IntelliJ to resolve Apache POI 5.5.1.
2. Before starting this version against your PostgreSQL database, run
   `src/main/resources/db/manual/2026-09-14-public-impact.sql` using your normal SQL client.
   It adds `project.public_impact`, with existing and new rows defaulting to false.
   The script is repeatable and is not automatically executed. The application's existing
   Hibernate `ddl-auto=update` can also add the column, but the explicit migration is preferred.
3. Start the application normally. Excel buttons are on the members and yearly donations lists.
   Member exports retain search, country, city, role and membership year; pagination is ignored.
4. Visit `/impact` without logging in. As HEAD, open a project's details and approve its
   title for public display. Only FINISHED projects can be approved. Public totals count only
   approved finished projects and their distinct years. Title, year or status changes clear approval.

Budget alerts compare each year's existing allocated budget with its existing total project
costs, matching the projects page. They do not change the dashboard's income or balance formulas.
Membership behavior is unchanged. No payment processing or additional budget fields were added.

## Verification

Build and run all tests safely with an isolated H2 database (PowerShell):

```powershell
.\mvnw.cmd -q package '-Dspring.datasource.url=jdbc:h2:mem:build-context' '-Dspring.datasource.username=sa' '-Dspring.datasource.password=' '-Dspring.datasource.driver-class-name=org.h2.Driver' '-Dspring.jpa.database-platform=org.hibernate.dialect.H2Dialect' '-Dapp.admin.email=test@example.com' '-Dapp.admin.password=TestOnly123!' '-Dlogging.file.name=target/build-context.log'
```

Targeted coverage: matching exports beyond pagination, donation year selection, typed cells,
literal formula-like text, empty exports, viewing permissions; 80%/100% boundaries, overspending,
missing/zero budgets and dashboard rendering; public/private access, publication permissions,
CSRF, title escaping, reapproval and migration defaults. Migration tests use H2 PostgreSQL mode,
not a live PostgreSQL server. No Neon database was used for verification.

## Files changed for these features

- `pom.xml`
- `Service/MemberService.java`, `Service/Implementation/MemberServiceImpl.java`
- `Service/Implementation/ExcelExportService.java`, `Web/ExcelExportController.java`
- `templates/members/list.html`, `templates/donations/list.html`
- `Service/Implementation/BudgetWarningService.java`, `Web/HomeController.java`, `templates/dashboard.html`
- `Models/Project.java`, `DTO/project/ProjectResponseDto.java`, `DTO/project/PublicProjectDto.java`
- `Mapper/ProjectMapper.java`, `Repository/ProjectRepository.java`, `Service/Implementation/ProjectServiceImpl.java`
- `Service/Implementation/ImpactService.java`, `Web/ImpactController.java`, `Web/ProjectController.java`
- `Config/SecurityConfig.java`, `templates/impact.html`, `templates/projects/details.html`, `templates/auth/login.html`
- `db/manual/2026-09-14-public-impact.sql`
- Tests: `ExcelExportTests`, `BudgetWarningTests`, `BudgetDashboardTests`, `ImpactPageTests`, `ImpactMigrationTests`
- This setup document.

Java paths above are relative to `src/main/java/emd/charitymanagementsystem/`;
template and database paths are relative to `src/main/resources/`.
