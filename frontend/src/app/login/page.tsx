"use client";

import { useState } from "react";
import { signIn } from "next-auth/react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Link from "next/link";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const res = await signIn("credentials", {
        email,
        password,
        redirect: false,
      });

      if (res?.error) {
        setError("Invalid email or password credentials.");
      } else {
        router.push("/dashboard");
      }
    } catch (err: any) {
      setError(err?.message || "An unexpected error occurred during login.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 px-4">
      <div className="max-w-md w-full p-8 bg-white rounded-2xl shadow-lg border border-slate-100">
        <div className="text-center mb-8">
          <div className="w-10 h-10 rounded-xl bg-blue-600 text-white font-bold text-xl flex items-center justify-center mx-auto mb-3">L</div>
          <h2 className="text-2xl font-bold text-slate-900">Welcome to Linkora</h2>
          <p className="text-slate-500 text-sm mt-1">Sign in to your enterprise supply chain portal</p>
        </div>

        <form onSubmit={handleLogin} className="space-y-5">
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">Corporate Email</label>
            <Input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="operations@factory.com"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">Password</label>
            <Input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
            />
          </div>

          {error && (
            <div className="p-3 rounded-lg bg-red-50 border border-red-200 text-red-600 text-xs font-medium">
              {error}
            </div>
          )}

          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? "Authenticating..." : "Sign In to Workspace"}
          </Button>
        </form>

        <div className="mt-6 text-center text-xs text-slate-500">
          Need an account?{" "}
          <Link href="/register" className="text-blue-600 font-semibold hover:underline">
            Register your enterprise
          </Link>
        </div>
      </div>
    </div>
  );
}
