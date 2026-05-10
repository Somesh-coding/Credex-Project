# TESTS

## Backend tests

```bash
cd backend
./mvnw test
```

## Manual tests performed

### Audit engine

* High spend produces positive savings
* Low spend does not manufacture savings
* Team plan for small team recommends downgrade
* Annual savings equals monthly savings × 12
* Empty tool list returns zero savings

### AI summary

* OpenRouter summary generation works
* Fallback summary works when AI fails

### Supabase

* Audit rows are stored correctly
* Lead rows are stored correctly

### Email

* Resend sends confirmation emails
* Public audit URL included in email

### Public share page

* Public slug resolves correctly
* No private lead information exposed

### Frontend

* Form state persists across reloads
* Responsive layout verified
