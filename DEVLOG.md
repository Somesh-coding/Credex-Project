# DEVLOG

## Day 1

Initialized frontend and backend structure.

Initially considered Firebase but switched to Supabase because relational storage fit the audit model better.

## Day 2

Built deterministic pricing engine.

Added:

* pricing comparison
* downgrade checks
* overspend detection
* alternative recommendations

## Day 3

Integrated AI summaries.

Originally tried Gemini API but faced quota and model issues. Switched to OpenRouter.

Added graceful fallback summaries.

## Day 4

Integrated Supabase and public share pages.

Added:

* audit persistence
* lead persistence
* public slug generation

## Day 5

Improved frontend UI and responsiveness.

Added:

* polished cards
* savings hero section
* localStorage persistence
* dynamic CTA states

## Day 6

Integrated Resend emails and honeypot protection.

Added Open Graph metadata for shareable audit pages.
