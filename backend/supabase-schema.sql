-- Run this in Supabase SQL Editor

create table if not exists audits (
  id text primary key,
  public_slug text unique not null,
  team_size int not null,
  use_case text not null,
  total_monthly_spend numeric not null default 0,
  total_monthly_savings numeric not null default 0,
  total_annual_savings numeric not null default 0,
  recommendations jsonb not null default '[]'::jsonb,
  ai_summary text,
  created_at timestamptz not null default now()
);

create table if not exists leads (
  id text primary key,
  audit_id text not null,
  email text not null,
  company_name text,
  role text,
  team_size int,
  created_at timestamptz not null default now()
);

alter table audits enable row level security;
alter table leads enable row level security;

-- Public users should only read sanitized audit data by slug.
create policy "public can read audits"
on audits
for select
using (true);

-- Do not create public insert policy for leads.
-- Backend uses service_role key only.
