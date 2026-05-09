import "./globals.css";
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "AI Spend Audit",
  description: "Find wasted AI spend in 60 seconds."
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
