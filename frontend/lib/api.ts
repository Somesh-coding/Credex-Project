export const API_BASE = process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

export async function createAudit(payload: any) {
  const res = await fetch(`${API_BASE}/api/audits`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
  if (!res.ok) throw new Error("Audit failed");
  return res.json();
}

export async function getPublicAudit(slug: string) {
  const res = await fetch(`${API_BASE}/api/audits/public/${slug}`, {
    next: { revalidate: 60 }
  });
  if (!res.ok) return null;
  return res.json();
}

export async function captureLead(payload: any) {
  const res = await fetch(`${API_BASE}/api/leads`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
  if (!res.ok) throw new Error("Lead capture failed");
  return res.json();
}
