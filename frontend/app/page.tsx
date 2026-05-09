"use client";

import { useEffect, useMemo, useState } from "react";
import { captureLead, createAudit } from "../lib/api";

const TOOL_PLANS: Record<string, string[]> = {
  "Cursor": ["Hobby", "Pro", "Business", "Enterprise"],
  "GitHub Copilot": ["Individual", "Business", "Enterprise"],
  "Claude": ["Free", "Pro", "Max", "Team", "Enterprise", "API direct"],
  "ChatGPT": ["Plus", "Team", "Enterprise", "API direct"],
  "Anthropic API direct": ["API direct"],
  "OpenAI API direct": ["API direct"],
  "Gemini": ["Pro", "Ultra", "API"],
  "Windsurf": ["Free", "Pro", "Teams", "Enterprise"]
};

const DEFAULT_TOOL = {
  tool: "ChatGPT",
  plan: "Team",
  monthlySpend: 90,
  seats: 3
};

export default function Home() {
  const [teamSize, setTeamSize] = useState(3);
  const [useCase, setUseCase] = useState("coding");
  const [tools, setTools] = useState([DEFAULT_TOOL]);
  const [result, setResult] = useState<any>(null);
  const [email, setEmail] = useState("");
  const [companyName, setCompanyName] = useState("");
  const [role, setRole] = useState("");
  const [loading, setLoading] = useState(false);

  const annualSpend = useMemo(
    () => tools.reduce((sum, t) => sum + Number(t.monthlySpend || 0), 0) * 12,
    [tools]
  );

  useEffect(() => {
    const saved = localStorage.getItem("audit-form");
    if (saved) {
      const parsed = JSON.parse(saved);
      setTeamSize(parsed.teamSize ?? 3);
      setUseCase(parsed.useCase ?? "coding");
      setTools(parsed.tools ?? [DEFAULT_TOOL]);
    }
  }, []);

  useEffect(() => {
    localStorage.setItem("audit-form", JSON.stringify({ teamSize, useCase, tools }));
  }, [teamSize, useCase, tools]);

  function updateTool(index: number, field: string, value: any) {
    const copy: any[] = [...tools];

    if (field === "tool") {
      copy[index] = {
        ...copy[index],
        tool: value,
        plan: TOOL_PLANS[value][0]
      };
    } else {
      copy[index] = {
        ...copy[index],
        [field]: value
      };
    }

    setTools(copy);
  }

  async function runAudit() {
    setLoading(true);
    try {
      const data = await createAudit({
        teamSize,
        useCase,
        tools: tools.map((t) => ({
          ...t,
          monthlySpend: Number(t.monthlySpend),
          seats: Number(t.seats)
        }))
      });
      setResult(data);
      setTimeout(() => {
        document.getElementById("results")?.scrollIntoView({ behavior: "smooth" });
      }, 100);
    } finally {
      setLoading(false);
    }
  }

  async function saveLead() {
    if (!email) {
      alert("Please enter your email.");
      return;
    }

    await captureLead({
      auditId: result.id,
      publicSlug: result.publicSlug,
      email,
      companyName,
      role,
      teamSize,
      website: ""
    });

    alert("Report saved. Check your email.");
  }

  return (
    <main className="min-h-screen bg-[#060914] text-white">
      <section className="mx-auto max-w-7xl px-6 py-10">
        <nav className="flex items-center justify-between">
          <div className="text-xl font-bold">SpendLens AI</div>
          <div className="rounded-full border border-white/10 px-4 py-2 text-sm text-white/70">
            Built for Credex-style AI spend audits
          </div>
        </nav>

        <section className="grid gap-8 py-14 lg:grid-cols-[1.05fr_0.95fr]">
          <div>
            <div className="mb-5 inline-flex rounded-full border border-blue-400/30 bg-blue-400/10 px-4 py-2 text-sm text-blue-200">
              Free AI stack audit · no login required
            </div>

            <h1 className="max-w-3xl text-5xl font-black tracking-tight md:text-7xl">
              Stop leaking budget on AI tools.
            </h1>

            <p className="mt-6 max-w-2xl text-lg leading-8 text-slate-300">
              Enter your AI subscriptions and usage. Get a clear breakdown of overspend,
              downgrade opportunities, cheaper alternatives, and credit-based savings.
            </p>

            <div className="mt-8 grid max-w-2xl grid-cols-3 gap-3">
              <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
                <div className="text-2xl font-bold">60 sec</div>
                <div className="text-sm text-slate-400">audit time</div>
              </div>
              <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
                <div className="text-2xl font-bold">${annualSpend}</div>
                <div className="text-sm text-slate-400">annual spend entered</div>
              </div>
              <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
                <div className="text-2xl font-bold">0 login</div>
                <div className="text-sm text-slate-400">required</div>
              </div>
            </div>
          </div>

          <div className="rounded-[2rem] border border-white/10 bg-white/[0.06] p-6 shadow-2xl backdrop-blur">
            <h2 className="text-2xl font-bold">Run your audit</h2>

            <div className="mt-6 grid gap-4 md:grid-cols-2">
              <label>
                <span className="text-sm text-slate-300">Team size</span>
                <input
                  className="mt-2 w-full rounded-2xl border border-white/10 bg-black/30 p-3 outline-none focus:border-blue-400"
                  type="number"
                  value={teamSize}
                  onChange={(e) => setTeamSize(Number(e.target.value))}
                />
              </label>

              <label>
                <span className="text-sm text-slate-300">Primary use case</span>
                <select
                  className="mt-2 w-full rounded-2xl border border-white/10 bg-black/30 p-3 outline-none focus:border-blue-400"
                  value={useCase}
                  onChange={(e) => setUseCase(e.target.value)}
                >
                  <option value="coding">Coding</option>
                  <option value="writing">Writing</option>
                  <option value="data">Data</option>
                  <option value="research">Research</option>
                  <option value="mixed">Mixed</option>
                </select>
              </label>
            </div>

            <div className="mt-6 space-y-4">
              {tools.map((t, i) => (
                <div key={i} className="rounded-3xl border border-white/10 bg-black/25 p-4">
                  <div className="grid gap-3 md:grid-cols-2">
                    <label>
                      <span className="text-sm text-slate-400">Tool</span>
                      <select
                        className="mt-2 w-full rounded-2xl border border-white/10 bg-[#0b1020] p-3"
                        value={t.tool}
                        onChange={(e) => updateTool(i, "tool", e.target.value)}
                      >
                        {Object.keys(TOOL_PLANS).map((tool) => (
                          <option key={tool} value={tool}>{tool}</option>
                        ))}
                      </select>
                    </label>

                    <label>
                      <span className="text-sm text-slate-400">Plan</span>
                      <select
                        className="mt-2 w-full rounded-2xl border border-white/10 bg-[#0b1020] p-3"
                        value={t.plan}
                        onChange={(e) => updateTool(i, "plan", e.target.value)}
                      >
                        {TOOL_PLANS[t.tool].map((plan) => (
                          <option key={plan} value={plan}>{plan}</option>
                        ))}
                      </select>
                    </label>

                    <label>
                      <span className="text-sm text-slate-400">Monthly spend</span>
                      <input
                        className="mt-2 w-full rounded-2xl border border-white/10 bg-[#0b1020] p-3"
                        type="number"
                        value={t.monthlySpend}
                        onChange={(e) => updateTool(i, "monthlySpend", Number(e.target.value))}
                      />
                    </label>

                    <label>
                      <span className="text-sm text-slate-400">Seats</span>
                      <input
                        className="mt-2 w-full rounded-2xl border border-white/10 bg-[#0b1020] p-3"
                        type="number"
                        value={t.seats}
                        onChange={(e) => updateTool(i, "seats", Number(e.target.value))}
                      />
                    </label>
                  </div>

                  {tools.length > 1 && (
                    <button
                      className="mt-3 text-sm text-red-300"
                      onClick={() => setTools(tools.filter((_, idx) => idx !== i))}
                    >
                      Remove tool
                    </button>
                  )}
                </div>
              ))}
            </div>

            <div className="mt-5 flex gap-3">
              <button
                onClick={() => setTools([...tools, DEFAULT_TOOL])}
                className="rounded-2xl border border-white/15 px-4 py-3 text-sm"
              >
                Add another tool
              </button>

              <button
                onClick={runAudit}
                disabled={loading}
                className="flex-1 rounded-2xl bg-blue-500 px-5 py-3 font-bold text-white hover:bg-blue-400 disabled:opacity-60"
              >
                {loading ? "Auditing..." : "Run free audit"}
              </button>
            </div>
          </div>
        </section>

        {result && (
          <section id="results" className="pb-16">
            <div className="rounded-[2rem] bg-white p-6 text-slate-950 shadow-2xl md:p-10">
              <div className="grid gap-6 lg:grid-cols-[1fr_0.7fr]">
                <div>
                  <p className="text-sm font-bold uppercase tracking-widest text-blue-600">
                    Audit result
                  </p>
                  <h2 className="mt-3 text-5xl font-black">
                    ${result.totalMonthlySavings}/mo saved
                  </h2>
                  <p className="mt-2 text-2xl text-slate-600">
                    ${result.totalAnnualSavings}/year potential savings
                  </p>
                </div>

                <div className="rounded-3xl bg-slate-100 p-5">
                  <h3 className="font-bold">AI personalized summary</h3>
                  <p className="mt-2 leading-7 text-slate-700">{result.aiSummary}</p>
                </div>
              </div>

              {result.totalMonthlySavings > 500 && (
                <div className="mt-8 rounded-3xl border border-blue-200 bg-blue-50 p-6">
                  <h3 className="text-2xl font-black text-blue-950">
                    Large savings opportunity detected
                  </h3>
                  <p className="mt-2 text-blue-800">
                    Credex should be surfaced here: this level of spend may benefit from
                    discounted AI infrastructure credits and procurement support.
                  </p>
                </div>
              )}

              {result.totalMonthlySavings < 100 && (
                <div className="mt-8 rounded-3xl border border-emerald-200 bg-emerald-50 p-6">
                  <h3 className="text-2xl font-black text-emerald-950">
                    Low optimization opportunity
                  </h3>
                  <p className="mt-2 text-emerald-800">
                    Your stack appears reasonably optimized. Leave your email to get notified
                    when pricing changes or new credits apply.
                  </p>
                </div>
              )}

              <div className="mt-8 grid gap-4">
                {result.recommendations.map((r: any, i: number) => (
                  <div key={i} className="rounded-3xl border border-slate-200 p-5">
                    <div className="flex flex-col justify-between gap-4 md:flex-row">
                      <div>
                        <h3 className="text-xl font-black">{r.tool}</h3>
                        <p className="text-slate-500">
                          Current plan: {r.currentPlan} · Current spend: ${r.currentSpend}/mo
                        </p>
                      </div>

                      <div className="rounded-2xl bg-green-50 px-5 py-3 text-right">
                        <div className="text-sm text-green-700">Savings</div>
                        <div className="text-2xl font-black text-green-800">
                          ${r.monthlySavings}/mo
                        </div>
                      </div>
                    </div>

                    <div className="mt-4 rounded-2xl bg-slate-50 p-4">
                      <p className="font-bold">{r.action}</p>
                      <p className="mt-1 text-slate-600">{r.reason}</p>
                    </div>
                  </div>
                ))}
              </div>

              <div className="mt-8 rounded-3xl border border-slate-200 bg-slate-50 p-6">
                <h3 className="text-xl font-black">
                  {result.totalMonthlySavings < 100
                    ? "Get notified when new optimizations apply"
                    : "Email me this report"}
                </h3>

                <div className="mt-4 grid gap-3 md:grid-cols-3">
                  <input
                    className="rounded-2xl border p-3"
                    placeholder="you@company.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                  />
                  <input
                    className="rounded-2xl border p-3"
                    placeholder="Company name optional"
                    value={companyName}
                    onChange={(e) => setCompanyName(e.target.value)}
                  />
                  <input
                    className="rounded-2xl border p-3"
                    placeholder="Role optional"
                    value={role}
                    onChange={(e) => setRole(e.target.value)}
                  />
                </div>

                <div className="mt-4 flex flex-col gap-3 md:flex-row">
                  <button
                    onClick={saveLead}
                    className="rounded-2xl bg-slate-950 px-5 py-3 font-bold text-white"
                  >
                    Save report
                  </button>

                  <a
                    className="rounded-2xl border border-slate-300 px-5 py-3 text-center font-bold"
                    href={`/audit/${result.publicSlug}`}
                  >
                    Open shareable public URL
                  </a>
                </div>
              </div>
            </div>
          </section>
        )}
      </section>
    </main>
  );
}