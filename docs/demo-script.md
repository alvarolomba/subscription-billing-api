# Demo Script

Use this flow when recording a short technical demo or walking someone through the project.

## 60-Second Version

1. Open Swagger:
   - Local: `http://localhost:8080/swagger-ui.html`
   - Public: `https://subscription-billing-api.onrender.com/swagger-ui.html`
2. Show the API surface:
   - Auth
   - Plans
   - Customers
   - Subscriptions
   - Invoices
   - Billing stats
3. Register a user and log in.
4. Click `Authorize` and paste:

```text
Bearer <accessToken>
```

5. Create a monthly plan.
6. Create a customer.
7. Create a subscription.
8. Show that an invoice was generated.
9. Pay the invoice.
10. Open `/billing/stats` and show MRR and paid revenue.

## Talking Points

- "This is a production-style SaaS billing backend, not a frontend-heavy demo."
- "Every resource belongs to the authenticated user, so users cannot access each other's billing data."
- "PostgreSQL schema changes are managed with Flyway, and Hibernate only validates the schema."
- "The test suite runs against real PostgreSQL with Testcontainers, avoiding H2-only false confidence."
- "The project is deployed on Render and can also run locally with Docker Compose."

## What To Show In The README

- Architecture diagram.
- ERD.
- Public Swagger link.
- `requests.http`.
- Testcontainers and CI badge.
- Production considerations and trade-offs.
