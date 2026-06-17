import React from 'react'

export default function KpiCard({ title, value, icon: Icon, iconBg = 'bg-primary-100', iconColor = 'text-primary-600', subtitle, trend }) {
  return (
    <div className="kpi-card">
      <div className={`kpi-icon ${iconBg}`}>
        <Icon className={`h-6 w-6 ${iconColor}`} />
      </div>
      <div className="min-w-0">
        <p className="text-sm text-gray-500 truncate">{title}</p>
        <p className="text-2xl font-bold text-gray-900 mt-0.5">
          {value !== undefined && value !== null ? value : '—'}
        </p>
        {subtitle && <p className="text-xs text-gray-400 mt-0.5">{subtitle}</p>}
      </div>
    </div>
  )
}
