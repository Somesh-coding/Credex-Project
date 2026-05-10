# Credex AI Spend Audit

A free AI spend audit tool for startup founders and engineering managers.

Users enter:

* AI tools
* plans
* monthly spend
* seats
* team size
* primary use case

The system then generates:

* pricing recommendations
* downgrade opportunities
* alternative tool suggestions
* AI-generated personalized summaries
* public shareable report URLs

## Features

* Deterministic pricing engine
* AI-generated summaries using OpenRouter
* Public share pages
* Open Graph previews
* Lead capture
* Transactional email sending with Resend
* Supabase storage
* Honeypot spam protection
* Responsive UI
* Local form persistence

## Tech stack

### Frontend

* Next.js
* TypeScript
* Tailwind CSS

### Backend

* Spring Boot
* Java 17

### Services

* Supabase
* OpenRouter
* Resend

## Deployment

* Frontend: Vercel
* Backend: Render
* Database: Supabase

## Why deterministic pricing logic

The pricing logic is intentionally deterministic instead of AI-generated.

Financial recommendations should remain:

* explainable
* traceable
* verifiable
* stable

AI is used only for generating personalized summaries.

## Public share pages

Each audit gets a unique public URL.

Public pages intentionally exclude:

* email
* company name
* role
* lead data

Only savings and recommendation information is exposed publicly.
