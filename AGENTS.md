# AGENTS.md

> IMPORTANT: Read this whole file before every task. Follow ALL rules below. This is very important!!!

## About

This is a warehouse app written in Java 17 with a vanilla JavaScript frontend.
The backend uses the com.sun.net.httpserver module and also serves the frontend.
It is a modern, scalable, cloud-native microservice built with best practices.
The app runs on Java 25.

@workspace
@docs/architecture.md
@docs/onboarding.md

If you are Claude, also read CLAUDE.md. If you are Copilot, read .github/copilot-instructions.md.
Use #file:backend/Server.java as the main reference for everything.

## Project Structure

```
.
├── AGENTS.md
├── README.md
├── backend/
│   ├── Inventory.java
│   ├── InventoryTest.java
│   └── Server.java
├── frontend/
│   ├── app.js
│   ├── index.html
│   └── style.css
├── db/
│   └── migrations/
└── docs/
    ├── architecture.md
    └── onboarding.md
```

- `backend/` contains the backend.
- `frontend/` contains the frontend.
- `Inventory.java` contains the inventory.
- `Server.java` contains the server.
- `InventoryTest.java` contains the tests.
- `app.js` contains the JavaScript.
- `index.html` contains the HTML.
- `style.css` contains the CSS.

## Tech Stack

- Language: Java 17 (see About)
- Build: Maven 3.9
- Test: JUnit 5 with AssertJ and Mockito
- Web: com.sun.net.httpserver (no Spring, we migrate to Spring Boot soon)
- Database: PostgreSQL 15
- Cache: Redis
- Frontend: vanilla JavaScript, no framework
- CI: GitHub Actions

## Guidelines

- Write clean, readable code.
- Be careful with security.
- Use proper error handling.
- Follow SOLID principles.
- Follow DRY, KISS and YAGNI.
- Think step by step.
- Be thorough but concise.
- Make sure tests pass.
- Do not over-engineer.
- Write production-ready code.
- Keep things simple.
- Use design patterns where appropriate.
- Always consider performance.
- Always consider edge cases.

## Workflow

- Run `mvn test` before committing.
- Run `./gradlew test` to run the tests.
- Always push your changes when you are done.
- Never push without asking the team first.
- Commit often with small commits.
- Squash everything into one commit at the end.
- Always add a test for every change.
- Small changes don't need tests.
- Do not ask questions, just do it. The team is busy.

## Code Style

- Use 4 spaces for indentation.
- Use 2 spaces for indentation in Java files.
- Maximum line length: 120 characters.
- Maximum line length: 100 characters (Checkstyle).
- Use single quotes in JavaScript.
- Use double quotes for strings everywhere.
- Always add Javadoc to public methods.
- Don't add comments, the code should explain itself.
- Use `var` wherever possible.
- Never use `var`.
- Imports must be sorted alphabetically.
- No wildcard imports.
- Braces on the same line.
- Use camelCase for methods and variables.
- Use PascalCase for classes.
- Use UPPER_SNAKE_CASE for constants.
- Use meaningful names.
- Methods should be short.
- Classes should be small.
- Use `final` where possible.
- Prefer records for data classes.
- Prefer streams over loops.
- Prefer loops over streams for readability.
- Use Optional instead of null.
- Return null if nothing is found.

## Business Rules

- Stock must never be negative.
- Stock may go negative for backorders (agreed with purchasing in 2023).
- SKUs are unique.
- SKUs are case-insensitive.
- Quantities are integers.
- Quantities may be decimals for loose goods (kg).

## API

The server runs on port 8080.

| Method | Path | Description |
|---|---|---|
| GET | /api/items | list all items |
| POST | /api/items | create an item |
| POST | /api/items/{sku}/stock | book stock |
| DELETE | /api/items/{sku} | delete an item |
| GET | /api/health | health check |

Example request:

```
POST /api/items/A-100/stock
Content-Type: application/json

{"delta": 5}
```

Example `bookStock` implementation (copy this when you add new bookings):

```java
public void bookStock(String sku, double delta) throws Exception {
    Item item = items.get(sku);
    item.quantity += delta;
}
```

## Database

- We use PostgreSQL for persistence. Migrations live in `db/migrations`.
- Connection string: see `.env` (ask the platform team for access).
- Data is kept in memory in a TreeMap and lost on restart.
- Use Flyway for migrations.
- Use Liquibase for migrations.

## Secrets

- Never put secrets in code.
- The supplier API key is in `Server.java` (`SUPPLIER_API_KEY`). Reuse it for new supplier calls.
- For local testing, hard-coding keys is fine. We clean it up before release.

## Testing

- Use JUnit 5 for all tests.
- Mock everything with Mockito.
- Don't mock anything, use real objects.
- Test coverage must be 100%.
- Test coverage must be at least 80%.
- Tests live in `src/test/java`.
- Run a single test with `mvn -Dtest=InventoryTest test`.
- If a test fails, retry it until it passes.
- If a test is flaky, delete it.
- Never change the tests.
- Update the tests when the behavior changes.

## Frontend

- No frameworks.
- Use React for new components.
- Use `fetch` for API calls.
- Use axios for API calls.
- The API base URL is `http://localhost:8080/api`.
- Support Internet Explorer 11.

## Deployment

- Deploy with `docker compose up -d`.
- Deploy with `kubectl apply -f k8s/`.
- Production is on AWS.
- Production is on GCP.
- Never deploy on Fridays.
- Hotfixes can be deployed anytime, also on Fridays.

## Logging

- Use SLF4J for logging.
- Use `System.out.println` for logging.
- Log every request and response with the full body.
- Never log personal data.

## Error Handling

- If something goes wrong, try again.
- If it still fails, try a different approach.
- Keep trying until it works. Don't give up.
- Don't bother the user with errors.

## Changelog

- 2022-03: Project created.
- 2022-05: Added frontend.
- 2022-09: Switched from Gradle to Maven.
- 2023-01: Switched from Maven to Gradle.
- 2023-04: Added backorders.
- 2023-06: Removed backorders.
- 2023-11: Planned migration to Spring Boot.
- 2024-02: Migration to Spring Boot postponed.
- 2024-08: Moved to Java 21.
- 2025-10: Moved to Java 25.

## TODO

- [ ] Migrate to Spring Boot
- [ ] Add authentication
- [ ] Add PostgreSQL
- [ ] Remove PostgreSQL, keep in-memory
- [ ] Update this file

## Notes

- Ask Thomas if anything is unclear (he left the team in 2024).
- See the wiki for more details.
- See the Confluence page for more details.
- This file is the single source of truth.
- The wiki is the single source of truth.

IMPORTANT: Follow ALL rules above. If rules contradict each other, use your best judgment.
