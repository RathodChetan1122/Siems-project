import React, { useState, useEffect } from 'react'
import {
  Ship, Package, Truck, Users, Archive,
  AlertTriangle, TrendingUp, CheckCircle2,
} from 'lucide-react'
import { analyticsApi } from '../api/analyticsApi'
import KpiCard from '../components/common/KpiCard'
import LoadingSpinner from '../components/common/LoadingSpinner'
import {
  Chart as ChartJS, ArcElement, Tooltip, Legend,
  CategoryScale, LinearScale, BarElement, Title,
} from 'chart.js'
import { Doughnut, Bar } from 'react-chartjs-2'

ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, Title)

const CHART_COLORS = {
  PENDING:    '#93c5fd',
  PACKED:     '#6ee7b7',
  DISPATCHED: '#fde68a',
  IN_TRANSIT: '#fdba74',
  AT_CUSTOMS: '#c4b5fd',
  DELIVERED:  '#86efac',
  CANCELLED:  '#fca5a5',
}

export default function AnalyticsPage() {
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    analyticsApi.getDashboard()
      .then(res => setSummary(res.data.data))
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="flex items-center justify-center h-64"><LoadingSpinner size="xl" /></div>

  const statusData = summary?.shipmentsByStatus || {}

  const kpis = [
    { title: 'Total Shipments', value: summary?.totalShipments, icon: Ship, iconBg: 'bg-blue-100', iconColor: 'text-blue-600' },
    { title: 'In Transit', value: summary?.shipmentsInTransit, icon: TrendingUp, iconBg: 'bg-yellow-100', iconColor: 'text-yellow-600' },
    { title: 'Delivered', value: summary?.shipmentsDelivered, icon: CheckCircle2, iconBg: 'bg-green-100', iconColor: 'text-green-600' },
    { title: 'Low Stock Alerts', value: summary?.lowStockItemsCount, icon: AlertTriangle, iconBg: 'bg-red-100', iconColor: 'text-red-600' },
    { title: 'Suppliers', value: summary?.totalSuppliers, icon: Truck, iconBg: 'bg-purple-100', iconColor: 'text-purple-600' },
    { title: 'Customers', value: summary?.totalCustomers, icon: Users, iconBg: 'bg-indigo-100', iconColor: 'text-indigo-600' },
    { title: 'Products', value: summary?.totalProducts, icon: Package, iconBg: 'bg-teal-100', iconColor: 'text-teal-600' },
    {
      title: 'Total Stock Value',
      value: summary?.totalStockValue ? `$${Number(summary.totalStockValue).toLocaleString()}` : '$0',
      icon: Archive, iconBg: 'bg-orange-100', iconColor: 'text-orange-600',
    },
  ]

  const labels = Object.keys(statusData)
  const values = Object.values(statusData)
  const colors = labels.map(l => CHART_COLORS[l] || '#d1d5db')

  const doughnutData = {
    labels,
    datasets: [{ data: values, backgroundColor: colors, borderWidth: 2, borderColor: '#fff' }]
  }

  const barData = {
    labels: labels.map(l => l.replace('_', ' ')),
    datasets: [{
      label: 'Shipments',
      data: values,
      backgroundColor: colors,
      borderRadius: 6,
      borderSkipped: false,
    }]
  }

  const deliveredRate = summary?.totalShipments > 0
    ? ((summary.shipmentsDelivered / summary.totalShipments) * 100).toFixed(1)
    : 0

  const transitRate = summary?.totalShipments > 0
    ? ((summary.shipmentsInTransit / summary.totalShipments) * 100).toFixed(1)
    : 0

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h1 className="page-title">Analytics</h1>
          <p className="page-subtitle">Platform-wide performance metrics</p>
        </div>
      </div>

      {/* KPI Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {kpis.map(kpi => <KpiCard key={kpi.title} {...kpi} />)}
      </div>

      {/* Delivery Rate KPIs */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div className="card">
          <p className="text-sm text-gray-500 mb-1">Delivery Success Rate</p>
          <div className="flex items-end gap-2">
            <span className="text-4xl font-black text-green-600">{deliveredRate}%</span>
            <span className="text-sm text-gray-400 mb-1">of all shipments</span>
          </div>
          <div className="mt-3 h-2 bg-gray-100 rounded-full overflow-hidden">
            <div className="h-full bg-green-500 rounded-full transition-all" style={{ width: `${deliveredRate}%` }} />
          </div>
        </div>

        <div className="card">
          <p className="text-sm text-gray-500 mb-1">Currently In Transit</p>
          <div className="flex items-end gap-2">
            <span className="text-4xl font-black text-yellow-600">{transitRate}%</span>
            <span className="text-sm text-gray-400 mb-1">of all shipments</span>
          </div>
          <div className="mt-3 h-2 bg-gray-100 rounded-full overflow-hidden">
            <div className="h-full bg-yellow-400 rounded-full transition-all" style={{ width: `${transitRate}%` }} />
          </div>
        </div>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card">
          <h2 className="card-title mb-4">Shipments by Status</h2>
          <div className="flex justify-center">
            <div className="w-60 h-60">
              <Doughnut
                data={doughnutData}
                options={{
                  plugins: {
                    legend: { position: 'bottom', labels: { boxWidth: 12, padding: 12, font: { size: 11 } } }
                  },
                  cutout: '65%',
                }}
              />
            </div>
          </div>
        </div>

        <div className="card">
          <h2 className="card-title mb-4">Shipment Volume by Status</h2>
          <Bar
            data={barData}
            options={{
              plugins: { legend: { display: false } },
              scales: {
                y: { beginAtZero: true, grid: { color: '#f3f4f6' }, ticks: { precision: 0, font: { size: 11 } } },
                x: { grid: { display: false }, ticks: { font: { size: 11 } } },
              },
            }}
          />
        </div>
      </div>

      {/* Status breakdown table */}
      <div className="card">
        <h2 className="card-title mb-4">Status Breakdown</h2>
        <div className="space-y-3">
          {labels.map((label, i) => {
            const count = values[i]
            const pct = summary?.totalShipments > 0 ? ((count / summary.totalShipments) * 100).toFixed(1) : 0
            return (
              <div key={label} className="flex items-center gap-3">
                <div className="w-28 text-sm text-gray-600 font-medium">{label.replace('_', ' ')}</div>
                <div className="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden">
                  <div className="h-full rounded-full transition-all" style={{ width: `${pct}%`, backgroundColor: CHART_COLORS[label] || '#d1d5db' }} />
                </div>
                <div className="w-16 text-right text-sm text-gray-600 font-semibold">{count}</div>
                <div className="w-12 text-right text-xs text-gray-400">{pct}%</div>
              </div>
            )
          })}
        </div>
      </div>
    </div>
  )
}
