import React from 'react'

const STATUS_MAP = {
  PENDING:    { label: 'Pending',    className: 'badge-info' },
  PACKED:     { label: 'Packed',     className: 'badge-info' },
  DISPATCHED: { label: 'Dispatched', className: 'badge-warning' },
  IN_TRANSIT: { label: 'In Transit', className: 'badge-warning' },
  AT_CUSTOMS: { label: 'At Customs', className: 'badge-purple' },
  DELIVERED:  { label: 'Delivered',  className: 'badge-success' },
  CANCELLED:  { label: 'Cancelled',  className: 'badge-danger' },
  ACTIVE:     { label: 'Active',     className: 'badge-success' },
  INACTIVE:   { label: 'Inactive',   className: 'badge-gray' },
  true:       { label: 'Active',     className: 'badge-success' },
  false:      { label: 'Inactive',   className: 'badge-gray' },
  INFO:       { label: 'Info',       className: 'badge-info' },
  WARNING:    { label: 'Warning',    className: 'badge-warning' },
  ALERT:      { label: 'Alert',      className: 'badge-danger' },
  SUCCESS:    { label: 'Success',    className: 'badge-success' },
}

export default function StatusBadge({ status }) {
  const key = String(status).toUpperCase()
  const config = STATUS_MAP[key] || { label: status, className: 'badge-gray' }
  return <span className={`badge ${config.className}`}>{config.label}</span>
}
