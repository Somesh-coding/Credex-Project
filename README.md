# Credex AI Spend Audit

A free AI spend audit tool for startup founders and engineering managers. Users enter AI tools, plans, monthly spend, team size, and use case, then receive rule-based savings recommendations, an AI-generated summary, lead capture, and a shareable public URL.

## Stack
- Frontend: Next.js + TypeScript + Tailwind
- Backend: Spring Boot
- Database: Supabase Postgres
- AI summary: Anthropic API with fallback template
- Email: Resend API

## Quick start

### 1. Create Supabase tables
Open Supabase → SQL Editor and run:

```sql
backend/supabase-schema.sql
```

### 2. Backend
```bash
cd backend
cp src/main/resources/application.example.properties src/main/resources/application.properties
```

Set:

```properties
supabase.enabled=true
supabase.url=https://YOUR_PROJECT_REF.supabase.co
supabase.service-role-key=YOUR_SERVICE_ROLE_KEY
```

Then run:

```bash
mvn spring-boot:run
```

### 3. Frontend
```bash
cd frontend
npm install
npm run dev
```

## Deployment
- Backend: Render
- Frontend: Vercel
- DB: Supabase

## Decisions
1. Supabase was chosen because it is accepted by the assignment and provides a real Postgres backend.
2. Audit math is hardcoded and deterministic because pricing logic should be explainable.
3. AI is used only for the personalized summary.
4. Lead data is stored separately from public audit display data.
5. Public share pages use dynamic metadata for Open Graph previews.
