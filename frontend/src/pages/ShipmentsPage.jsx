import React, { useState, useEffect, useCallback } from 'react'
import { Plus, Eye, RefreshCw, MapPin } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { shipmentApi } from '../api/shipmentApi'
import { supplierApi } from '../api/supplierApi'
import { customerApi } from '../api/customerApi'
import { productApi } from '../api/productApi'
import { warehouseApi } from '../api/warehouseApi'
import { useAuth } from '../context/AuthContext'
import Pagination from '../components/common/Pagination'
import Modal from '../components/common/Modal'
import EmptyState from '../components/common/EmptyState'
import LoadingSpinner from '../components/common/LoadingSpinner'
import StatusBadge from '../components/common/StatusBadge'
import toast from 'react-hot-toast'

const STATUSES = ['PENDING', 'PACKED', 'DISPATCHED', 'IN_TRANSIT', 'AT_CUSTOMS', 'DELIVERED', 'CANCELLED']
const TRANSITIONS = {
  PENDING: ['PACKED', 'CANCELLED'],
  PACKED: ['DISPATCHED', 'CANCELLED'],
  DISPATCHED: ['IN_TRANSIT'],
  IN_TRANSIT: ['AT_CUSTOMS', 'DELIVERED'],
  AT_CUSTOMS: ['IN_TRANSIT', 'DELIVERED', 'CANCELLED'],
  DELIVERED: [],
  CANCELLED: [],
}

export default function ShipmentsPage() {
  const navigate = useNavigate()
  const { isImportManager, isExportManager } = useAuth()
  const canCreate = () => isImportManager() || isExportManager()

  const [shipments, setShipments] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [statusFilter, setStatusFilter] = useState('')
  const [loading, setLoading] = useState(true)

  const [createModal, setCreateModal] = useState(false)
  const [statusModal, setStatusModal] = useState(null)
  const [suppliers, setSuppliers] = useState([])
  const [customers, setCustomers] = useState([])
  const [products, setProducts] = useState([])
  const [warehouses, setWarehouses] = useState([])

  const [form, setForm] = useState({ supplierId: '', customerId: '', warehouseId: '', carrier: '', eta: '', items: [{ productId: '', quantity: '', unitPrice: '' }] })
  const [statusForm, setStatusForm] = useState({ status: '', remarks: '', location: '' })
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    Promise.all([
      supplierApi.getAll({ page: 0, size: 100 }),
      customerApi.getAll({ page: 0, size: 100 }),
      productApi.getAll({ page: 0, size: 100 }),
      warehouseApi.getAll({ page: 0, size: 100, activeOnly: true }),
    ]).then(([s, c, p, w]) => {
      setSuppliers(s.data.data.content || [])
      setCustomers(c.data.data.content || [])
      setProducts(p.data.data.content || [])
      setWarehouses(w.data.data.content || [])
    }).catch(() => {})
  }, [])

  const fetchShipments = useCallback(async () => {
    setLoading(true)
    try {
      const res = await shipmentApi.getAll({ status: statusFilter || undefined, page, size: 10 })
      const d = res.data.data
      setShipments(d.content); setTotalPages(d.totalPages); setTotalElements(d.totalElements)
    } catch { toast.error('Failed to load shipments') }
    finally { setLoading(false) }
  }, [statusFilter, page])

  useEffect(() => { setPage(0) }, [statusFilter])
  useEffect(() => { fetchShipments() }, [fetchShipments])

  const handleCreate = async (e) => {
    e.preventDefault(); setSaving(true)
    try {
      const payload = {
        ...form,
        supplierId: Number(form.supplierId),
        customerId: Number(form.customerId),
        warehouseId: Number(form.warehouseId),
        items: form.items.map(i => ({ productId: Number(i.productId), quantity: Number(i.quantity), unitPrice: Number(i.unitPrice) }))
      }
      await shipmentApi.create(payload)
      toast.success('Shipment created successfully')
      setCreateModal(false)
      fetchShipments()
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to create shipment') }
    finally { setSaving(false) }
  }

  const handleStatusUpdate = async (e) => {
    e.preventDefault(); setSaving(true)
    try {
      await shipmentApi.updateStatus(statusModal.shipmentId, statusForm)
      toast.success('Status updated')
      setStatusModal(null)
      fetchShipments()
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to update status') }
    finally { setSaving(false) }
  }

  const addItem = () => setForm(p => ({ ...p, items: [...p.items, { productId: '', quantity: '', unitPrice: '' }] }))
  const removeItem = (i) => setForm(p => ({ ...p, items: p.items.filter((_, idx) => idx !== i) }))
  const updateItem = (i, k, v) => setForm(p => { const items = [...p.items]; items[i] = { ...items[i], [k]: v }; return { ...p, items } })

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h1 className="page-title">Shipments</h1>
          <p className="page-subtitle">{totalElements} total shipments</p>
        </div>
        {canCreate() && <button className="btn-primary" onClick={() => setCreateModal(true)}><Plus className="h-4 w-4" />New Shipment</button>}
      </div>

      {/* Status filter tabs */}
      <div className="flex flex-wrap gap-2">
        {['', ...STATUSES].map(s => (
          <button key={s} onClick={() => setStatusFilter(s)}
            className={`px-3 py-1.5 text-xs font-medium rounded-full border transition-colors ${statusFilter === s ? 'bg-primary-600 text-white border-primary-600' : 'bg-white text-gray-600 border-gray-300 hover:border-primary-400'}`}>
            {s || 'All'}
          </button>
        ))}
      </div>

      <div className="card p-0 overflow-hidden">
        {loading ? <div className="py-20"><LoadingSpinner size="lg" /></div>
          : shipments.length === 0 ? <EmptyState title="No shipments found" description="Create your first shipment to get started" />
          : (
            <>
              <div className="table-wrapper rounded-none border-0">
                <table className="table">
                  <thead>
                    <tr><th>Tracking #</th><th>Supplier</th><th>Customer</th><th>Status</th><th>Carrier</th><th>ETA</th><th className="text-right">Actions</th></tr>
                  </thead>
                  <tbody>
                    {shipments.map(s => (
                      <tr key={s.shipmentId}>
                        <td className="font-mono text-xs font-semibold text-primary-700">{s.trackingNumber}</td>
                        <td>{s.supplierName}</td>
                        <td>{s.customerName}</td>
                        <td><StatusBadge status={s.currentStatus} /></td>
                        <td className="text-gray-500">{s.carrier || '—'}</td>
                        <td className="text-gray-500">{s.eta || '—'}</td>
                        <td className="text-right">
                          <div className="flex items-center justify-end gap-1.5">
                            <button onClick={() => navigate(`/shipments/${s.shipmentId}/tracking`)} title="Track" className="p-1.5 rounded-lg hover:bg-blue-50 text-gray-400 hover:text-blue-600"><MapPin className="h-4 w-4" /></button>
                            {TRANSITIONS[s.currentStatus]?.length > 0 && (
                              <button onClick={() => { setStatusModal(s); setStatusForm({ status: TRANSITIONS[s.currentStatus][0], remarks: '', location: '' }) }} title="Update Status" className="p-1.5 rounded-lg hover:bg-yellow-50 text-gray-400 hover:text-yellow-600">
                                <RefreshCw className="h-4 w-4" />
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <Pagination page={page} totalPages={totalPages} totalElements={totalElements} pageSize={10} onPageChange={setPage} />
            </>
          )}
      </div>

      {/* Create Shipment Modal */}
      <Modal isOpen={createModal} onClose={() => setCreateModal(false)} title="Create New Shipment" size="lg">
        <form onSubmit={handleCreate} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Supplier *</label>
              <select className="input-field" value={form.supplierId} onChange={e => setForm(p => ({ ...p, supplierId: e.target.value }))} required>
                <option value="">Select supplier</option>
                {suppliers.map(s => <option key={s.supplierId} value={s.supplierId}>{s.name}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Customer *</label>
              <select className="input-field" value={form.customerId} onChange={e => setForm(p => ({ ...p, customerId: e.target.value }))} required>
                <option value="">Select customer</option>
                {customers.map(c => <option key={c.customerId} value={c.customerId}>{c.name}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Warehouse *</label>
              <select className="input-field" value={form.warehouseId} onChange={e => setForm(p => ({ ...p, warehouseId: e.target.value }))} required>
                <option value="">Select warehouse</option>
                {warehouses.map(w => <option key={w.warehouseId} value={w.warehouseId}>{w.name}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Carrier</label>
              <input className="input-field" value={form.carrier} onChange={e => setForm(p => ({ ...p, carrier: e.target.value }))} placeholder="e.g. Maersk, FedEx" />
            </div>
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">ETA</label>
              <input type="date" className="input-field" value={form.eta} onChange={e => setForm(p => ({ ...p, eta: e.target.value }))} min={new Date().toISOString().split('T')[0]} />
            </div>
          </div>

          {/* Line items */}
          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="text-sm font-medium text-gray-700">Items *</label>
              <button type="button" onClick={addItem} className="text-xs text-primary-600 hover:underline">+ Add item</button>
            </div>
            <div className="space-y-2">
              {form.items.map((item, i) => (
                <div key={i} className="grid grid-cols-10 gap-2 items-center">
                  <div className="col-span-4">
                    <select className="input-field text-xs" value={item.productId} onChange={e => updateItem(i, 'productId', e.target.value)} required>
                      <option value="">Product</option>
                      {products.map(p => <option key={p.productId} value={p.productId}>{p.name}</option>)}
                    </select>
                  </div>
                  <div className="col-span-2">
                    <input type="number" min="1" className="input-field text-xs" placeholder="Qty" value={item.quantity} onChange={e => updateItem(i, 'quantity', e.target.value)} required />
                  </div>
                  <div className="col-span-3">
                    <input type="number" min="0" step="0.01" className="input-field text-xs" placeholder="Unit Price" value={item.unitPrice} onChange={e => updateItem(i, 'unitPrice', e.target.value)} required />
                  </div>
                  <div className="col-span-1 flex justify-center">
                    {form.items.length > 1 && (
                      <button type="button" onClick={() => removeItem(i)} className="text-red-400 hover:text-red-600 text-lg leading-none">×</button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <button type="button" className="btn-secondary" onClick={() => setCreateModal(false)}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={saving}>{saving ? 'Creating...' : 'Create Shipment'}</button>
          </div>
        </form>
      </Modal>

      {/* Update Status Modal */}
      <Modal isOpen={!!statusModal} onClose={() => setStatusModal(null)} title="Update Shipment Status" size="sm">
        {statusModal && (
          <form onSubmit={handleStatusUpdate} className="space-y-4">
            <div className="p-3 bg-gray-50 rounded-xl text-sm">
              <p className="font-mono font-semibold text-primary-700">{statusModal.trackingNumber}</p>
              <p className="text-gray-500">Current: <StatusBadge status={statusModal.currentStatus} /></p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">New Status *</label>
              <select className="input-field" value={statusForm.status} onChange={e => setStatusForm(p => ({ ...p, status: e.target.value }))} required>
                {(TRANSITIONS[statusModal.currentStatus] || []).map(s => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Location</label>
              <input className="input-field" placeholder="Current location..." value={statusForm.location} onChange={e => setStatusForm(p => ({ ...p, location: e.target.value }))} />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Remarks</label>
              <textarea className="input-field resize-none" rows={3} placeholder="Optional remarks..." value={statusForm.remarks} onChange={e => setStatusForm(p => ({ ...p, remarks: e.target.value }))} />
            </div>
            <div className="flex justify-end gap-3 pt-2">
              <button type="button" className="btn-secondary" onClick={() => setStatusModal(null)}>Cancel</button>
              <button type="submit" className="btn-primary" disabled={saving}>{saving ? 'Updating...' : 'Update Status'}</button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  )
}
