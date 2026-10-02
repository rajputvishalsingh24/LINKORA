"use client";

import { motion } from "framer-motion";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { CheckCircle2, Factory, TrendingUp, Truck, ShieldCheck, Cpu } from "lucide-react";

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      {/* Navigation */}
      <nav className="flex items-center justify-between p-6 max-w-7xl mx-auto">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center text-white font-black text-lg">L</div>
          <div className="text-2xl font-bold tracking-tight text-blue-600">Linkora.</div>
        </div>
        <div className="space-x-4">
          <Link href="/login">
            <Button variant="ghost">Log In</Button>
          </Link>
          <Link href="/register">
            <Button>Start Free Trial</Button>
          </Link>
        </div>
      </nav>

      {/* Hero Section */}
      <header className="pt-20 pb-28 text-center max-w-5xl mx-auto px-4">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-50 border border-blue-200 text-blue-700 text-xs font-semibold mb-6 uppercase tracking-wider">
          Connect • Predict • Deliver
        </div>
        <motion.h1
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="text-5xl md:text-7xl font-extrabold tracking-tight mb-6"
        >
          AI-Powered Supply Chain Intelligence for <span className="text-blue-600">Modern Manufacturing</span>
        </motion.h1>
        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="text-xl text-slate-600 mb-10 max-w-2xl mx-auto"
        >
          Manage suppliers, inventory, logistics, procurement, and neural demand forecasting from a single, AI-driven enterprise ecosystem.
        </motion.p>
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="flex flex-wrap justify-center gap-4"
        >
          <Link href="/register">
            <Button size="lg" className="text-lg px-8">Start Free Trial</Button>
          </Link>
          <Link href="/demo">
            <Button size="lg" variant="outline" className="text-lg px-8">Schedule Demo</Button>
          </Link>
        </motion.div>
      </header>

      {/* Features Section */}
      <section className="py-24 bg-white border-t border-slate-100">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-4">Enterprise Capabilities</h2>
          <p className="text-slate-500 text-center max-w-xl mx-auto mb-16">Designed specifically for agile SMBs combining the best of SAP S/4HANA, Flexport, and Alibaba B2B.</p>
          <div className="grid md:grid-cols-3 gap-8">
            <FeatureCard
              icon={<TrendingUp className="w-7 h-7 text-blue-600"/>}
              title="Demand Forecasting"
              description="Predict monthly and seasonal raw material demand with 94% neural confidence using rolling order history."
            />
            <FeatureCard
              icon={<Factory className="w-7 h-7 text-blue-600"/>}
              title="Smart Inventory AI"
              description="Autonomous depletion rate calculation and dynamic safety stock triggers to eliminate manufacturing halt."
            />
            <FeatureCard
              icon={<Truck className="w-7 h-7 text-blue-600"/>}
              title="Route Optimization"
              description="Multi-modal route calculation comparing fastest expressway corridors against lowest fuel transit routes."
            />
            <FeatureCard
              icon={<Cpu className="w-7 h-7 text-blue-600"/>}
              title="Supplier Risk Telemetry"
              description="Real-time delay probability scores based on regional strikes, port congestion, and quality defect rates."
            />
            <FeatureCard
              icon={<ShieldCheck className="w-7 h-7 text-blue-600"/>}
              title="Automated GST Invoicing"
              description="Tax invoice reconciliation, 2% marketplace commission auto-split, and instant Razorpay/Stripe checkout."
            />
            <FeatureCard
              icon={<Factory className="w-7 h-7 text-blue-600"/>}
              title="Production Planning"
              description="Machine cell load balancing, lot quality inspection tracking, and ISO compliance reporting."
            />
          </div>
        </div>
      </section>

      {/* Pricing Section */}
      <section className="py-24 bg-slate-900 text-white">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-4">Transparent SaaS Billing</h2>
          <p className="text-slate-400 text-center max-w-lg mx-auto mb-16">Simple tiered subscriptions designed to scale with your factory's production throughput.</p>
          <div className="grid md:grid-cols-3 gap-8">
            <PricingCard
              title="Starter"
              price="₹999"
              features={["Up to 5 Users", "500 Orders/Month", "Basic Analytics", "Inventory Management", "Standard Email Support"]}
            />
            <PricingCard
              title="Growth"
              price="₹4,999"
              isPopular
              features={["Up to 50 Users", "Unlimited Orders", "AI Demand Forecasting", "Supplier Marketplace", "Logistics Tracking", "Smart Inventory Reorders"]}
            />
            <PricingCard
              title="Enterprise"
              price="Custom"
              features={["Unlimited Users & Seats", "Advanced Neural Models", "Custom ERP Integration (SAP/Oracle)", "Dedicated Account Architect", "White Label Tenant"]}
            />
          </div>
        </div>
      </section>
    </div>
  );
}

function FeatureCard({ icon, title, description }: { icon: React.ReactNode, title: string, description: string }) {
  return (
    <div className="p-6 border border-slate-200/80 rounded-2xl shadow-sm hover:shadow-md transition-shadow bg-slate-50/50">
      <div className="mb-4 bg-blue-100/70 w-12 h-12 flex items-center justify-center rounded-xl">{icon}</div>
      <h3 className="text-xl font-bold mb-2 text-slate-800">{title}</h3>
      <p className="text-slate-600 text-sm leading-relaxed">{description}</p>
    </div>
  );
}

function PricingCard({ title, price, features, isPopular = false }: { title: string, price: string, features: string[], isPopular?: boolean }) {
  return (
    <div className={`p-8 rounded-2xl border ${isPopular ? 'border-blue-500 bg-slate-800 ring-2 ring-blue-500/20' : 'border-slate-800 bg-slate-800/60'} relative flex flex-col justify-between`}>
      {isPopular && <span className="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-blue-600 text-white px-3 py-1 text-xs font-bold rounded-full uppercase tracking-wider">Most Popular</span>}
      <div>
        <h3 className="text-2xl font-bold mb-2">{title}</h3>
        <div className="text-4xl font-extrabold mb-6">{price}<span className="text-lg text-slate-400 font-normal">{price !== 'Custom' ? '/month' : ''}</span></div>
        <ul className="space-y-4 mb-8">
          {features.map((feature, idx) => (
            <li key={idx} className="flex items-center gap-3">
              <CheckCircle2 className="w-5 h-5 text-blue-400 shrink-0" />
              <span className="text-slate-300 text-sm">{feature}</span>
            </li>
          ))}
        </ul>
      </div>
      <Button className="w-full" variant={isPopular ? 'default' : 'outline'}>Choose {title}</Button>
    </div>
  );
}
