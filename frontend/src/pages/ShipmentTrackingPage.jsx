import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, MapPin, CheckCircle2, Clock, XCircle } from 'lucide-react'
import { shipmentApi } from '../api/shipmentApi'
import LoadingSpinner from '../components/common/LoadingSpinner'
import StatusBadge from '../components/common/StatusBadge'
import toast from 'react-hot-toast'

const STATUS_ICONS = {
  PENDING: Clock,
  PACKED: CheckCircle2,
  DISPATCHED: CheckCircle2,
  IN_TRANSIT: MapPin,
  AT_CUSTOMS: Clock,
  DELIVERED: CheckCircle2,
  CANCELLED: XCircle,
}

const STATUS_COLORS = {
  PENDING: 'text-blue-500 bg-blue-50 border-blue-200',
  PACKED: 'text-blue-600 bg-blue-50 border-blue-200',
  DISPATCHED: 'text-yellow-600 bg-yellow-50 border-yellow-200',
  IN_TRANSIT: 'text-yellow-600 bg-yellow-50 border-yellow-200',
  AT_CUSTOMS: 'text-purple-600 bg-purple-50 border-purple-200',
  DELIVERED: 'text-green-600 bg-green-50 border-green-200',
  CANCELLED: 'text-red-500 bg-red-50 border-red-200',
}

export default function ShipmentTrackingPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [tracking, setTracking] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    shipmentApi.getTracking(id)
      .then(res => setTracking(res.data.data))
      .catch(() => toast.error('Failed to load tracking data'))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <div className="flex items-center justify-center h-64"><LoadingSpinner size="xl" /></div>
  if (!tracking) return <div className="text-center py-20 text-gray-500">Tracking data not available</div>

  return (
    <div className="space-y-6 max-w-3xl mx-auto">
      {/* Header */}
      <div className="flex items-center gap-4">
        <button onClick={() => navigate('/shipments')} className="p-2 rounded-xl hover:bg-gray-100 text-gray-500">
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div>
          <h1 className="page-title">Shipment Tracking</h1>
          <p className="text-sm text-gray-500 font-mono mt-0.5">{tracking.trackingNumber}</p>
        </div>
      </div>

      {/* Summary Card */}
      <div className="card">
        <div className="flex flex-wrap gap-6 items-center justify-between">
          <div>
            <p className="text-xs text-gray-400 uppercase tracking-wider mb-1">Current Status</p>
            <StatusBadge status={tracking.currentStatus} />
          </div>
          {tracking.carrier && (
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wider mb-1">Carrier</p>
              <p className="text-sm font-semibold text-gray-800">{tracking.carrier}</p>
            </div>
          )}
          {tracking.etd && (
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wider mb-1">ETD</p>
              <p className="text-sm font-semibold text-gray-800">{tracking.etd}</p>
            </div>
          )}
          {tracking.eta && (
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wider mb-1">ETA</p>
              <p className="text-sm font-semibold text-gray-800">{tracking.eta}</p>
            </div>
          )}
        </div>
      </div>

      {/* Timeline */}
      <div className="card">
        <h2 className="card-title mb-6">Tracking Timeline</h2>
        {tracking.timeline?.length === 0 ? (
          <p className="text-gray-400 text-sm text-center py-8">No tracking events yet</p>
        ) : (
          <div className="relative">
            {/* Vertical line */}
            <div className="absolute left-5 top-0 bottom-0 w-px bg-gray-200" />

            <div className="space-y-6">
              {tracking.timeline.map((event, idx) => {
                const Icon = STATUS_ICONS[event.status] || MapPin
                const colorClass = STATUS_COLORS[event.status] || 'text-gray-500 bg-gray-50 border-gray-200'
                const isLatest = idx === tracking.timeline.length - 1

                return (
                  <div key={idx} className="relative flex gap-4">
                    {/* Icon */}
                    <div className={`relative z-10 flex-shrink-0 w-10 h-10 rounded-full border-2 flex items-center justify-center ${colorClass} ${isLatest ? 'ring-4 ring-offset-2 ring-primary-100' : ''}`}>
                      <Icon className="h-4 w-4" />
                    </div>

                    {/* Content */}
                    <div className="flex-1 pb-2">
                      <div className="flex flex-wrap items-start justify-between gap-2">
                        <div>
                          <p className="font-semibold text-gray-900">{event.status.replace('_', ' ')}</p>
                          {event.location && (
                            <p className="text-sm text-gray-500 flex items-center gap-1 mt-0.5">
                              <MapPin className="h-3 w-3" /> {event.location}
                            </p>
                          )}
                          {event.remarks && (
                            <p className="text-sm text-gray-600 mt-1 bg-gray-50 rounded-lg px-3 py-2 border border-gray-100">
                              {event.remarks}
                            </p>
                          )}
                        </div>
                        <div className="text-right text-xs text-gray-400 flex-shrink-0">
                          <p>{event.changedAt ? new Date(event.changedAt).toLocaleDateString() : '—'}</p>
                          <p>{event.changedAt ? new Date(event.changedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}</p>
                          <p className="mt-0.5 text-gray-300">by {event.changedBy}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                )
              })}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
