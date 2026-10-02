"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { CheckCircle, XCircle, FileText } from "lucide-react";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

const incomingOrders = [
  { id: "PO-9921", buyer: "Apex Industrial Systems", amount: "₹450,000", date: "2026-06-25", status: "Pending" },
  { id: "PO-9922", buyer: "Nexus Mobility & Hardware", amount: "₹120,000", date: "2026-06-24", status: "Accepted" },
];

export default function SupplierDashboard() {
  return (
    <div className="p-8 space-y-8 bg-slate-50 min-h-screen">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">Supplier Portal</h1>
          <p className="text-slate-500">Manage your catalog, quotes, and incoming purchase orders.</p>
        </div>
        <Button className="flex gap-2"><FileText className="w-4 h-4"/> Generate Invoice</Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <Card><CardContent className="p-6"><p className="text-sm text-slate-500">Delivery Rate</p><p className="text-3xl font-bold text-emerald-600 mt-2">98.4%</p></CardContent></Card>
        <Card><CardContent className="p-6"><p className="text-sm text-slate-500">Quality Score</p><p className="text-3xl font-bold text-blue-600 mt-2">4.9/5</p></CardContent></Card>
        <Card><CardContent className="p-6"><p className="text-sm text-slate-500">Avg Response Time</p><p className="text-3xl font-bold text-amber-600 mt-2">2.4 hrs</p></CardContent></Card>
      </div>

      <Card className="shadow-sm">
        <CardHeader>
          <CardTitle>Incoming Purchase Orders</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>PO Number</TableHead>
                <TableHead>Buyer</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Date</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {incomingOrders.map((order) => (
                <TableRow key={order.id}>
                  <TableCell className="font-medium text-slate-700">{order.id}</TableCell>
                  <TableCell>{order.buyer}</TableCell>
                  <TableCell className="font-semibold">{order.amount}</TableCell>
                  <TableCell>{order.date}</TableCell>
                  <TableCell className="text-right space-x-2">
                    {order.status === "Pending" ? (
                      <>
                        <Button size="sm" variant="outline" className="text-emerald-600 border-emerald-200 hover:bg-emerald-50"><CheckCircle className="w-4 h-4 mr-1"/> Accept</Button>
                        <Button size="sm" variant="outline" className="text-red-600 border-red-200 hover:bg-red-50"><XCircle className="w-4 h-4 mr-1"/> Reject</Button>
                      </>
                    ) : (
                      <span className="text-sm font-medium text-slate-500 px-3 py-1 bg-slate-100 rounded-full">{order.status}</span>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
