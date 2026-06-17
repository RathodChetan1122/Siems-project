import React, { useState, useEffect, useCallback } from 'react'
import { Plus, Pencil, Archive } from 'lucide-react'
import { warehouseApi } from '../api/warehouseApi'
import { useAuth } from '../context/AuthContext'
import SearchBar from '../components/common/SearchBar'
import Pagination from '../components/common/Pagination'
import Modal from '../components/common/Modal'
import EmptyState from '../components/common/EmptyState'
import LoadingSpinner from '../components/common/LoadingSpinner'
import StatusBadge from '../components/common/StatusBadge'
import toast from 'react-hot-toast'

const EMPTY_FORM = { name: '', location: '', code: '', capacity: '' }

export default function WarehousesPage() {
  const { isInventoryManager } = useAuth()
  const [warehouses, setWarehouses] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [saving, setSaving] = useState(false)

  const fetchWarehouses = useCallback(async () => {
    setLoading(true)
    try {
      const res = await warehouseApi.getAll({ keyword: search || undefined, activeOnly: false, page, size: 10 })
      const d = res.data.data
      setWarehouses(d.content); setTotalPages(d.totalPages); setTotalElements(d.totalElements)
    } catch { toast.error('Failed to load warehouses') }
    finally { setLoading(false) }
  }, [search, page])

  useEffect(() => { setPage(0) }, [search])
  useEffect(() => { fetchWarehouses() }, [fetchWarehouses])

  const openCreate = () => { setEditing(null); setForm(EMPTY_FORM); setModalOpen(true) }
  const openEdit = (w) => {
    setEditing(w)
    setForm({ name: w.name, location: w.location, code: w.code, capacity: w.capacity || '' })
    setModalOpen(true)
  }

  const handleSave = async (e) => {
    e.preventDefault(); setSaving(true)
    try {
      const payload = { ...form, capacity: form.capacity ? Number(form.capacity) : null }
      editing ? await warehouseApi.update(editing.warehouseId, payload) : await warehouseApi.create(payload)
      toast.success(editing ? 'Warehouse updated' : 'Warehouse created')
      setModalOpen(false); fetchWarehouses()
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to save') }
    finally { setSaving(false) }
  }

  const f = (k) => (e) => setForm(p => ({ ...p, [k]: e.target.value }))

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h1 className="page-title">Warehouses</h1>
          <p className="page-subtitle">{totalElements} total warehouses</p>
        </div>
        {isInventoryManager() && <button className="btn-primary" onClick={openCreate}><Plus className="h-4 w-4" />Add Warehouse</button>}
      </div>

      <div className="card p-0 overflow-hidden">
        <div className="p-4 border-b border-gray-100">
          <SearchBar value={search} onChange={setSearch} placeholder="Search warehouses..." className="max-w-sm" />
        </div>

        {loading ? <div className="py-20"><LoadingSpinner size="lg" /></div>
          : warehouses.length === 0 ? <EmptyState title="No warehouses found" />
          : (
            <>
              <div className="table-wrapper rounded-none border-0">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Name</th><th>Code</th><th>Location</th><th>Capacity</th><th>Items</th><th>Low Stock</th><th>Manager</th><th>Status</th>
                      {isInventoryManager() && <th className="text-right">Actions</th>}
                    </tr>
                  </thead>
                  <tbody>
                    {warehouses.map(w => (
                      <tr key={w.warehouseId}>
                        <td className="font-medium text-gray-900">{w.name}</td>
                        <td className="font-mono text-xs text-gray-500">{w.code}</td>
                        <td className="text-gray-500">{w.location}</td>
                        <td>{w.capacity?.toLocaleString() || '—'}</td>
                        <td className="font-semibold">{w.totalItems}</td>
                        <td>{w.lowStockItems > 0 ? <span className="badge badge-danger">{w.lowStockItems}</span> : <span className="text-gray-400">—</span>}</td>
                        <td className="text-gray-500">{w.managerName || '—'}</td>
                        <td><StatusBadge status={w.active ? 'ACTIVE' : 'INACTIVE'} /></td>
                        {isInventoryManager() && (
                          <td className="text-right">
                            <button onClick={() => openEdit(w)} className="p-1.5 rounded-lg hover:bg-gray-100 text-gray-500 hover:text-primary-600"><Pencil className="h-4 w-4" /></button>
                          </td>
                        )}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <Pagination page={page} totalPages={totalPages} totalElements={totalElements} pageSize={10} onPageChange={setPage} />
            </>
          )}
      </div>

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit Warehouse' : 'Add Warehouse'}>
        <form onSubmit={handleSave} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Name *</label>
              <input className="input-field" value={form.name} onChange={f('name')} required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Code *</label>
              <input className="input-field" value={form.code} onChange={f('code')} required disabled={!!editing} />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Capacity</label>
              <input type="number" min="0" className="input-field" value={form.capacity} onChange={f('capacity')} />
            </div>
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Location *</label>
              <input className="input-field" value={form.location} onChange={f('location')} required />
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" className="btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={saving}>{saving ? 'Saving...' : editing ? 'Update' : 'Create'}</button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
