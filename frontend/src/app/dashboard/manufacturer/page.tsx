"use client";

import { useState } from "react";
import { useSession } from "next-auth/react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Package, AlertTriangle, TrendingDown, Clock } from "lucide-react";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import InventoryTable from "./components/InventoryTable";

const costData = [
  { month: "Jan", procurement: 4000, production: 2400, logistics: 1000 },
  { month: "Feb", procurement: 3000, production: 1398, logistics: 800 },
  { month: "Mar", procurement: 2000, production: 9800, logistics: 1200 },
  { month: "Apr", procurement: 2780, production: 3908, logistics: 1500 },
];

export default function ManufacturerDashboard() {
  const { data: session } = useSession();
  const [stats] = useState({ pendingPOs: 12, lowStock: 3, activeShipments: 5 });

  return (
    <div className="p-8 space-y-8 bg-slate-50 min-h-screen">
      <div>
        <h1 className="text-3xl font-bold text-slate-900">Manufacturer Overview</h1>
        <p className="text-slate-500">Welcome back, {session?.user?.name || "Operations Lead"}. Here is your supply chain status.</p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <KpiCard title="Pending POs" value={stats.pendingPOs} icon={<Clock className="text-blue-500" />} />
        <KpiCard title="Low Stock Alerts" value={stats.lowStock} icon={<AlertTriangle className="text-amber-500" />} />
        <KpiCard title="Active Shipments" value={stats.activeShipments} icon={<Package className="text-indigo-500" />} />
        <KpiCard title="Cost Reduction" value="12%" icon={<TrendingDown className="text-emerald-500" />} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Cost Analysis Chart */}
        <Card className="col-span-2 shadow-sm border-slate-200">
          <CardHeader>
            <CardTitle>Cost Analysis (Q1 - Q2)</CardTitle>
          </CardHeader>
          <CardContent className="h-80 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={costData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="month" />
                <YAxis />
                <Tooltip cursor={{ fill: 'transparent' }} />
                <Bar dataKey="procurement" stackId="a" fill="#3b82f6" name="Procurement" />
                <Bar dataKey="production" stackId="a" fill="#6366f1" name="Production" />
                <Bar dataKey="logistics" stackId="a" fill="#94a3b8" name="Logistics" />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* AI Insights Card */}
        <Card className="shadow-sm border-slate-200 bg-blue-600 text-white">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <span>✨ AI Insights</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="p-4 bg-blue-700/50 rounded-lg">
              <p className="text-sm font-semibold">Smart Inventory Alert</p>
              <p className="text-xs text-blue-100 mt-1">Steel reserves will deplete in 12 days. Recommend generating a PO for 500 units.</p>
            </div>
            <div className="p-4 bg-blue-700/50 rounded-lg">
              <p className="text-sm font-semibold">Demand Forecast</p>
              <p className="text-xs text-blue-100 mt-1">Expected 15% increase in Q3 demand based on historical automotive trends.</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Inventory Management Data Table */}
      <div className="mt-8">
        <h2 className="text-xl font-bold text-slate-900 mb-4">Live Inventory</h2>
        <InventoryTable />
      </div>
    </div>
  );
}

function KpiCard({ title, value, icon }: { title: string, value: string | number, icon: React.ReactNode }) {
  return (
    <Card className="shadow-sm border-slate-200">
      <CardContent className="flex items-center justify-between p-6">
        <div>
          <p className="text-sm font-medium text-slate-500">{title}</p>
          <p className="text-3xl font-bold mt-2">{value}</p>
        </div>
        <div className="p-3 bg-slate-100 rounded-full">{icon}</div>
      </CardContent>
    </Card>
  );
}
