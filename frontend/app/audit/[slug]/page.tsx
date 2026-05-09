import { getPublicAudit } from "../../../lib/api";
import type { Metadata } from "next";

export async function generateMetadata({ params }: { params: { slug: string } }): Promise<Metadata> {
  const audit = await getPublicAudit(params.slug);
  const title = audit ? `$${audit.totalMonthlySavings}/mo AI savings found` : "AI Spend Audit";
  const description = audit ? `Potential annual AI savings: $${audit.totalAnnualSavings}` : "Free AI spend audit.";
  return {
    title,
    description,
    openGraph: {
      title,
      description,
      type: "website"
    },
    twitter: {
      card: "summary_large_image",
      title,
      description
    }
  };
}

export default async function PublicAudit({ params }: { params: { slug: string } }) {
  const audit = await getPublicAudit(params.slug);

  if (!audit) {
    return <main className="p-10">Audit not found.</main>;
  }

  return (
    <main className="min-h-screen px-6 py-12">
      <section className="mx-auto max-w-4xl rounded-3xl bg-white p-8 text-black">
        <p className="text-sm uppercase tracking-widest text-blue-600">Public AI Spend Audit</p>
        <h1 className="mt-3 text-5xl font-bold">${audit.totalMonthlySavings}/mo savings found</h1>
        <p className="mt-2 text-2xl">${audit.totalAnnualSavings}/year potential savings</p>
        <p className="mt-6 rounded-2xl bg-gray-100 p-5">{audit.aiSummary}</p>

        <div className="mt-8 space-y-4">
          {audit.recommendations.map((r: any, i: number) => (
            <div key={i} className="rounded-2xl border p-4">
              <h2 className="text-xl font-bold">{r.tool}</h2>
              <p>{r.action}</p>
              <p className="text-green-700">${r.monthlySavings}/mo savings</p>
              <p className="text-gray-600">{r.reason}</p>
            </div>
          ))}
        </div>
      </section>
    </main>
  );
}
