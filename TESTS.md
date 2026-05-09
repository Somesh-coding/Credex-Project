# Tests

Run backend tests:

```bash
cd backend
./mvnw test
```

Required audit engine tests:
1. High spend produces positive savings.
2. Low spend does not manufacture savings.
3. Team plan for tiny team recommends downgrade.
4. Annual savings equals monthly savings × 12.
5. Empty tools returns zero savings.
