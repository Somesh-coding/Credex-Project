# Supabase Setup Guide

## 1. Create project
Go to Supabase and create a new project.

## 2. Run database schema
Open:

```text
backend/supabase-schema.sql
```

Paste it into Supabase SQL Editor and run it.

## 3. Get keys
Go to:

```text
Project Settings → API
```

Copy:

```text
Project URL
service_role key
```

Use the `service_role` key only in Spring Boot backend. Never put it in frontend.

## 4. Backend environment
For local development, create:

```text
backend/src/main/resources/application.properties
```

Paste:

```properties
server.port=8080

app.frontend-url=http://localhost:3000
app.backend-url=http://localhost:8080
app.allowed-origin=http://localhost:3000

supabase.enabled=true
supabase.url=https://YOUR_PROJECT_REF.supabase.co
supabase.service-role-key=YOUR_SERVICE_ROLE_KEY

anthropic.api-key=
resend.api-key=
```

## 5. Render deployment env vars
Add these in Render:

```text
SUPABASE_URL
SUPABASE_SERVICE_ROLE_KEY
ANTHROPIC_API_KEY
RESEND_API_KEY
APP_ALLOWED_ORIGIN
```

For Spring Boot property binding on Render, use:

```text
SUPABASE_ENABLED=true
SUPABASE_URL=https://YOUR_PROJECT_REF.supabase.co
SUPABASE_SERVICE_ROLE_KEY=YOUR_SERVICE_ROLE_KEY
APP_ALLOWED_ORIGIN=https://your-frontend.vercel.app
```

## 6. Important security rule
Never expose the service role key in React or Next.js.
