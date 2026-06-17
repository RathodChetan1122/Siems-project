import React, { useState, useEffect, useCallback } from 'react'
import { ArrowDown, ArrowUp, ArrowLeftRight, SlidersHorizontal } from 'lucide-react'
import { inventoryApi } from '../api/inventoryApi'
import { warehouseApi } from '../api/warehouseApi'
import { useAuth } from '../context/AuthContext'
import Pagination from '../components/common/Pagination'
import Modal from '../components/common/Modal'
import EmptyState from '../components/common/EmptyState'
import LoadingSpinner from '../components/common/LoadingSpinner'
import StatusBadge from '../components/common/StatusBadge'
import toast from 'react-hot-toast'

export default function InventoryPage() {
  const { isInventoryManager } = useAuth()
  const [inventory, setInventory] = useState([])
  const [warehouses, setWarehouses] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [warehouseId, setWarehouseId] = useState('')
  const [loading, setLoading] = useState(true)
  const [actionModal, setActionModal] = useState(null) // { type: 'stockIn'|'stockOut'|'transfer', item }
  const [form, setForm] = useState({ quantity: '', reason: '', toWarehouseId: '' })
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    warehouseApi.getAll({ page: 0, size: 100, activeOnly: true })
      .then(r => setWarehouses(r.data.data.content || []))
      .catch(() => {})
  }, [])

  const fetchInventory = useCallback(async () => {
    setLoading(true)
    try {
      const res = await inventoryApi.getAll({ warehouseId: warehouseId || undefined, page, size: 15 })
      const d = res.data.data
      setInventory(d.content); setTotalPages(d.totalPages); setTotalElements(d.totalElements)
    } catch { toast.error('Failed to load inventory') }
    finally { setLoading(false) }
  }, [warehouseId, page])

  useEffect(() => { setPage(0) }, [warehouseId])
  useEffect(() => { fetchInventory() }, [fetchInventory])

  const openAction = (type, item) => {
    setActionModal({ type, item })
    setForm({ quantity: '', reason: '', toWarehouseId: '' })
  }

  const handleAction = async (e) => {
    e.preventDefault()
    if (!form.quantity || Number(form.quantity) <= 0) { toast.error('Enter a valid quantity'); return }
    setSaving(true)
    const { type, item } = actionModal
    try {
      if (type === 'stockIn') {
        await inventoryApi.stockIn({ productId: item.productId, warehouseId: item.warehouseId, quantity: Number(form.quantity), reason: form.reason })
        toast.success('Stock added successfully')
      } else if (type === 'stockOut') {
        await inventoryApi.stockOut({ productId: item.productId, warehouseId: item.warehouseId, quantity: Number(form.quantity), reason: form.reason })
        toast.success('Stock removed successfully')
      } else if (type === 'transfer') {
        if (!form.toWarehouseId) { toast.error('Select destination warehouse'); setSaving(false); return }
        await inventoryApi.transfer({ productId: item.productId, fromWarehouseId: item.warehouseId, toWarehouseId: Number(form.toWarehouseId), quantity: Number(form.quantity) })
        toast.success('Stock transferred successfully')
      }
      setActionModal(null)
      fetchInventory()
    } catch (err) { toast.error(err.response?.data?.message || 'Operation failed') }
    finally { setSaving(false) }
  }

  const modalTitles = { stockIn: 'Add Stock', stockOut: 'Remove Stock', transfer: 'Transfer Stock' }

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h1 className="page-title">Inventory</h1>
          <p className="page-subtitle">{totalElements} inventory records</p>
        </div>
      </div>

      <div className="card p-0 overflow-hidden">
        <div className="p-4 border-b border-gray-100 flex gap-3">
          <select className="input-field w-56" value={warehouseId} onChange={e => setWarehouseId(e.target.value)}>
            <option value="">All Warehouses</option>
            {warehouses.map(w => <option key={w.warehouseId} value={w.warehouseId}>{w.name}</option>)}
          </select>
        </div>

        {loading ? <div className="py-20"><LoadingSpinner size="lg" /></div>
          : inventory.length === 0 ? <EmptyState title="No inventory records" description="Stock will appear here after products are added to warehouses" />
          : (
            <>
              <div className="table-wrapper rounded-none border-0">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Product</th><th>SKU</th><th>Warehouse</th><th>Quantity</th><th>Reorder Threshold</th><th>Status</th>
                      {isInventoryManager() && <th className="text-right">Actions</th>}
                    </tr>
                  </thead>
                  <tbody>
                    {inventory.map(item => (
                      <tr key={item.inventoryId}>
                        <td className="font-medium text-gray-900">{item.productName}</td>
                        <td className="font-mono text-xs text-gray-500">{item.sku}</td>
                        <td className="text-gray-500">{item.warehouseName}</td>
                        <td className={`font-bold ${item.lowStock ? 'text-red-600' : 'text-green-700'}`}>{item.quantity?.toLocaleString()}</td>
                        <td className="text-gray-500">{item.reorderThreshold}</td>
                        <td><StatusBadge status={item.lowStock ? 'WARNING' : 'SUCCESS'} /></td>
                        {isInventoryManager() && (
                          <td className="text-right">
                            <div className="flex items-center justify-end gap-1.5">
                              <button onClick={() => openAction('stockIn', item)} title="Add Stock" className="p-1.5 rounded-lg hover:bg-green-50 text-gray-400 hover:text-green-600"><ArrowUp className="h-4 w-4" /></button>
                              <button onClick={() => openAction('stockOut', item)} title="Remove Stock" className="p-1.5 rounded-lg hover:bg-red-50 text-gray-400 hover:text-red-600"><ArrowDown className="h-4 w-4" /></button>
                              <button onClick={() => openAction('transfer', item)} title="Transfer Stock" className="p-1.5 rounded-lg hover:bg-blue-50 text-gray-400 hover:text-blue-600"><ArrowLeftRight className="h-4 w-4" /></button>
                            </div>
                          </td>
                        )}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <Pagination page={page} totalPages={totalPages} totalElements={totalElements} pageSize={15} onPageChange={setPage} />
            </>
          )}
      </div>

      {/* Action Modal */}
      <Modal isOpen={!!actionModal} onClose={() => setActionModal(null)} title={actionModal ? modalTitles[actionModal.type] : ''} size="sm">
        {actionModal && (
          <form onSubmit={handleAction} className="space-y-4">
            <div className="p-3 bg-gray-50 rounded-xl text-sm">
              <p className="font-semibold text-gray-800">{actionModal.item.productName}</p>
              <p className="text-gray-500">{actionModal.item.warehouseName} · Current qty: <strong>{actionModal.item.quantity}</strong></p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Quantity *</label>
              <input type="number" min="1" className="input-field" value={form.quantity} onChange={e => setForm(p => ({ ...p, quantity: e.target.value }))} required autoFocus />
            </div>
            {actionModal.type === 'transfer' && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Destination Warehouse *</label>
                <select className="input-field" value={form.toWarehouseId} onChange={e => setForm(p => ({ ...p, toWarehouseId: e.target.value }))} required>
                  <option value="">Select warehouse</option>
                  {warehouses.filter(w => w.warehouseId !== actionModal.item.warehouseId).map(w => (
                    <option key={w.warehouseId} value={w.warehouseId}>{w.name}</option>
                  ))}
                </select>
              </div>
            )}
            {actionModal.type !== 'transfer' && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Reason</label>
                <input className="input-field" placeholder="Optional reason..." value={form.reason} onChange={e => setForm(p => ({ ...p, reason: e.target.value }))} />
              </div>
            )}
            <div className="flex justify-end gap-3 pt-2">
              <button type="button" className="btn-secondary" onClick={() => setActionModal(null)}>Cancel</button>
              <button type="submit" className="btn-primary" disabled={saving}>{saving ? 'Processing...' : 'Confirm'}</button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  )
}
