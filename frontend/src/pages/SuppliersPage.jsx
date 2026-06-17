import React, { useState, useEffect, useCallback } from 'react'
import { Plus, Pencil, Trash2, Star } from 'lucide-react'
import { supplierApi } from '../api/supplierApi'
import { useAuth } from '../context/AuthContext'
import SearchBar from '../components/common/SearchBar'
import Pagination from '../components/common/Pagination'
import Modal from '../components/common/Modal'
import ConfirmDialog from '../components/common/ConfirmDialog'
import EmptyState from '../components/common/EmptyState'
import LoadingSpinner from '../components/common/LoadingSpinner'
import toast from 'react-hot-toast'

const EMPTY_FORM = { name: '', country: '', contactEmail: '', phone: '', rating: '', address: '' }

export default function SuppliersPage() {
  const { isImportManager, isAdmin } = useAuth()
  const [suppliers, setSuppliers] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [saving, setSaving] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  const fetchSuppliers = useCallback(async () => {
    setLoading(true)
    try {
      const res = await supplierApi.getAll({ name: search || undefined, page, size: 10, sort: 'name,asc' })
      const data = res.data.data
      setSuppliers(data.content)
      setTotalPages(data.totalPages)
      setTotalElements(data.totalElements)
    } catch {
      toast.error('Failed to load suppliers')
    } finally {
      setLoading(false)
    }
  }, [search, page])

  useEffect(() => { setPage(0) }, [search])
  useEffect(() => { fetchSuppliers() }, [fetchSuppliers])

  const openCreate = () => { setEditing(null); setForm(EMPTY_FORM); setModalOpen(true) }
  const openEdit = (s) => { setEditing(s); setForm({ name: s.name, country: s.country, contactEmail: s.contactEmail, phone: s.phone || '', rating: s.rating || '', address: s.address || '' }); setModalOpen(true) }

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      if (editing) {
        await supplierApi.update(editing.supplierId, form)
        toast.success('Supplier updated')
      } else {
        await supplierApi.create(form)
        toast.success('Supplier created')
      }
      setModalOpen(false)
      fetchSuppliers()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save supplier')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async () => {
    setDeleting(true)
    try {
      await supplierApi.delete(deleteTarget.supplierId)
      toast.success('Supplier deleted')
      setDeleteTarget(null)
      fetchSuppliers()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete supplier')
    } finally {
      setDeleting(false)
    }
  }

  const renderStars = (rating) => (
    <div className="flex items-center gap-1">
      <Star className="h-3.5 w-3.5 text-yellow-400 fill-yellow-400" />
      <span className="text-sm text-gray-700">{rating || '—'}</span>
    </div>
  )

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h1 className="page-title">Suppliers</h1>
          <p className="page-subtitle">{totalElements} total suppliers</p>
        </div>
        {isImportManager() && (
          <button className="btn-primary" onClick={openCreate}>
            <Plus className="h-4 w-4" /> Add Supplier
          </button>
        )}
      </div>

      <div className="card p-0 overflow-hidden">
        <div className="p-4 border-b border-gray-100">
          <SearchBar value={search} onChange={setSearch} placeholder="Search by name..." className="max-w-sm" />
        </div>

        {loading ? (
          <div className="py-20"><LoadingSpinner size="lg" /></div>
        ) : suppliers.length === 0 ? (
          <EmptyState title="No suppliers found" description="Add your first supplier to get started" action={isImportManager() && <button className="btn-primary" onClick={openCreate}><Plus className="h-4 w-4" />Add Supplier</button>} />
        ) : (
          <>
            <div className="table-wrapper rounded-none border-0">
              <table className="table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Country</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Rating</th>
                    {isImportManager() && <th className="text-right">Actions</th>}
                  </tr>
                </thead>
                <tbody>
                  {suppliers.map(s => (
                    <tr key={s.supplierId}>
                      <td className="font-medium text-gray-900">{s.name}</td>
                      <td>{s.country}</td>
                      <td className="text-gray-500">{s.contactEmail}</td>
                      <td className="text-gray-500">{s.phone || '—'}</td>
                      <td>{renderStars(s.rating)}</td>
                      {isImportManager() && (
                        <td className="text-right">
                          <div className="flex items-center justify-end gap-2">
                            <button onClick={() => openEdit(s)} className="p-1.5 rounded-lg hover:bg-gray-100 text-gray-500 hover:text-primary-600">
                              <Pencil className="h-4 w-4" />
                            </button>
                            {isAdmin() && (
                              <button onClick={() => setDeleteTarget(s)} className="p-1.5 rounded-lg hover:bg-red-50 text-gray-500 hover:text-red-600">
                                <Trash2 className="h-4 w-4" />
                              </button>
                            )}
                          </div>
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

      {/* Create/Edit Modal */}
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit Supplier' : 'Add Supplier'}>
        <form onSubmit={handleSave} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Name *</label>
              <input className="input-field" value={form.name} onChange={e => setForm(p => ({ ...p, name: e.target.value }))} required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Country *</label>
              <input className="input-field" value={form.country} onChange={e => setForm(p => ({ ...p, country: e.target.value }))} required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Contact Email *</label>
              <input type="email" className="input-field" value={form.contactEmail} onChange={e => setForm(p => ({ ...p, contactEmail: e.target.value }))} required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
              <input className="input-field" value={form.phone} onChange={e => setForm(p => ({ ...p, phone: e.target.value }))} />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Rating (0–5)</label>
              <input type="number" min="0" max="5" step="0.1" className="input-field" value={form.rating} onChange={e => setForm(p => ({ ...p, rating: e.target.value }))} />
            </div>
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Address</label>
              <input className="input-field" value={form.address} onChange={e => setForm(p => ({ ...p, address: e.target.value }))} />
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" className="btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={saving}>{saving ? 'Saving...' : editing ? 'Update' : 'Create'}</button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)} onConfirm={handleDelete} title="Delete Supplier" message={`Are you sure you want to delete "${deleteTarget?.name}"? This action cannot be undone.`} loading={deleting} />
    </div>
  )
}
