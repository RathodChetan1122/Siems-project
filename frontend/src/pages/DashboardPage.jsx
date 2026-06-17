import React, { useState, useEffect } from 'react'
import {
  Ship, Package, Truck, Users, Archive,
  AlertTriangle, TrendingUp, CheckCircle2
} from 'lucide-react'
import { analyticsApi } from '../api/analyticsApi'
import { inventoryApi } from '../api/inventoryApi'
import KpiCard from '../components/common/KpiCard'
import LoadingSpinner from '../components/common/LoadingSpinner'
import StatusBadge from '../components/common/StatusBadge'
import {
  Chart as ChartJS, ArcElement, Tooltip, Legend,
  CategoryScale, LinearScale, BarElement, Title
} from 'chart.js'
import { Doughnut, Bar } from 'react-chartjs-2'

ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, Title)

export default function DashboardPage() {
  const [summary, setSummary] = useState(null)
  const [alerts, setAlerts] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      analyticsApi.getDashboard(),
      inventoryApi.getAlerts(),
    ]).then(([summaryRes, alertsRes]) => {
      setSummary(summaryRes.data.data)
      setAlerts(alertsRes.data.data || [])
    }).catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="xl" />
      </div>
    )
  }

  const kpis = [
    {
      title: 'Total Shipments',
      value: summary?.totalShipments ?? 0,
      icon: Ship,
      iconBg: 'bg-blue-100',
      iconColor: 'text-blue-600',
    },
    {
      title: 'In Transit',
      value: summary?.shipmentsInTransit ?? 0,
      icon: TrendingUp,
      iconBg: 'bg-yellow-100',
      iconColor: 'text-yellow-600',
    },
    {
      title: 'Delivered',
      value: summary?.shipmentsDelivered ?? 0,
      icon: CheckCircle2,
      iconBg: 'bg-green-100',
      iconColor: 'text-green-600',
    },
    {
      title: 'Low Stock Alerts',
      value: summary?.lowStockItemsCount ?? 0,
      icon: AlertTriangle,
      iconBg: 'bg-red-100',
      iconColor: 'text-red-600',
    },
    {
      title: 'Suppliers',
      value: summary?.totalSuppliers ?? 0,
      icon: Truck,
      iconBg: 'bg-purple-100',
      iconColor: 'text-purple-600',
    },
    {
      title: 'Customers',
      value: summary?.totalCustomers ?? 0,
      icon: Users,
      iconBg: 'bg-indigo-100',
      iconColor: 'text-indigo-600',
    },
    {
      title: 'Products',
      value: summary?.totalProducts ?? 0,
      icon: Package,
      iconBg: 'bg-teal-100',
      iconColor: 'text-teal-600',
    },
    {
      title: 'Stock Value',
      value: summary?.totalStockValue
        ? `$${Number(summary.totalStockValue).toLocaleString()}`
        : '$0',
      icon: Archive,
      iconBg: 'bg-orange-100',
      iconColor: 'text-orange-600',
    },
  ]

  const statusData = summary?.shipmentsByStatus || {}
  const doughnutData = {
    labels: Object.keys(statusData),
    datasets: [{
      data: Object.values(statusData),
      backgroundColor: [
        '#93c5fd', '#fde68a', '#86efac',
        '#c4b5fd', '#f9a8d4', '#6ee7b7', '#fca5a5',
      ],
      borderWidth: 2,
      borderColor: '#fff',
    }]
  }

  const barData = {
    labels: Object.keys(statusData).map(s => s.replace('_', ' ')),
    datasets: [{
      label: 'Shipments',
      data: Object.values(statusData),
      backgroundColor: '#3b82f6',
      borderRadius: 6,
      borderSkipped: false,
    }]
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Dashboard</h1>
          <p className="page-subtitle">Welcome back — here's what's happening today</p>
        </div>
      </div>

      {/* KPI Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {kpis.map(kpi => (
          <KpiCard key={kpi.title} {...kpi} />
        ))}
      </div>

      {/* Charts row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Doughnut */}
        <div className="card">
          <h2 className="card-title mb-4">Shipments by Status</h2>
          <div className="flex justify-center">
            <div className="w-56 h-56">
              <Doughnut
                data={doughnutData}
                options={{
                  plugins: { legend: { position: 'bottom', labels: { boxWidth: 12, padding: 16, font: { size: 12 } } } },
                  cutout: '65%',
                }}
              />
            </div>
          </div>
        </div>

        {/* Bar chart */}
        <div className="card">
          <h2 className="card-title mb-4">Volume by Status</h2>
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

      {/* Low Stock Alerts */}
      {alerts.length > 0 && (
        <div className="card">
          <div className="card-header">
            <h2 className="card-title flex items-center gap-2">
              <AlertTriangle className="h-5 w-5 text-red-500" />
              Active Low Stock Alerts
              <span className="badge badge-danger">{alerts.length}</span>
            </h2>
          </div>
          <div className="table-wrapper">
            <table className="table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>SKU</th>
                  <th>Warehouse</th>
                  <th>Current Qty</th>
                  <th>Threshold</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {alerts.map(alert => (
                  <tr key={alert.alertId}>
                    <td className="font-medium">{alert.productName}</td>
                    <td className="text-gray-500">{alert.sku}</td>
                    <td>{alert.warehouseName}</td>
                    <td className="font-semibold text-red-600">{alert.currentQuantity}</td>
                    <td>{alert.reorderThreshold}</td>
                    <td><StatusBadge status="WARNING" /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
