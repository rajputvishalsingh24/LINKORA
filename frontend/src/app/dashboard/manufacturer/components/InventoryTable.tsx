"use client";

import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Edit2, Plus } from "lucide-react";

const mockInventory = [
  { id: "INV-001", name: "Industrial Steel Sheets", category: "Raw Material", stock: 1200, status: "Healthy" },
  { id: "INV-002", name: "Microprocessors", category: "Electronics", stock: 85, status: "Low Stock" },
  { id: "INV-003", name: "Packaging Boxes", category: "Consumables", stock: 5000, status: "Healthy" },
];

export default function InventoryTable() {
  return (
    <div className="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
      <div className="p-4 border-b flex justify-between items-center bg-slate-50/50">
        <input
          type="text"
          placeholder="Search inventory..."
          className="px-3 py-2 border rounded-md text-sm w-64 focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <Button size="sm" className="flex gap-2"><Plus className="w-4 h-4"/> Add Item</Button>
      </div>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Item ID</TableHead>
            <TableHead>Name</TableHead>
            <TableHead>Category</TableHead>
            <TableHead>Current Stock</TableHead>
            <TableHead>Status</TableHead>
            <TableHead className="text-right">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {mockInventory.map((item) => (
            <TableRow key={item.id}>
              <TableCell className="font-medium text-slate-600">{item.id}</TableCell>
              <TableCell>{item.name}</TableCell>
              <TableCell>{item.category}</TableCell>
              <TableCell>{item.stock} Units</TableCell>
              <TableCell>
                <Badge variant={item.status === 'Low Stock' ? 'destructive' : 'default'} className={item.status === 'Healthy' ? 'bg-emerald-500' : ''}>
                  {item.status}
                </Badge>
              </TableCell>
              <TableCell className="text-right">
                <Button variant="ghost" size="sm"><Edit2 className="w-4 h-4 text-slate-500" /></Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
