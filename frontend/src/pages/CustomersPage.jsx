import React, { useState, useEffect, useCallback } from 'react'
import { Plus, Pencil, Trash2 } from 'lucide-react'
import { customerApi } from '../api/customerApi'
import { useAuth } from '../context/AuthContext'
import SearchBar from '../components/common/SearchBar'
import Pagination from '../components/common/Pagination'
import Modal from '../components/common/Modal'
import ConfirmDialog from '../components/common/ConfirmDialog'
import EmptyState from '../components/common/EmptyState'
import LoadingSpinner from '../components/common/LoadingSpinner'
import toast from 'react-hot-toast'

const EMPTY_FORM = { name: '', billingAddress: '', shippingAddress: '', contactEmail: '', phone: '', creditTerms: 'NET_30' }

export default function CustomersPage() {
  const { isExportManager, isAdmin } = useAuth()
  const [customers, setCustomers] = useState([])
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

  const fetchCustomers = useCallback(async () => {
    setLoading(true)
    try {
      const res = await customerApi.getAll({ keyword: search || undefined, page, size: 10 })
      const data = res.data.data
      setCustomers(data.content)
      setTotalPages(data.totalPages)
      setTotalElements(data.totalElements)
    } catch { toast.error('Failed to load customers') }
    finally { setLoading(false) }
  }, [search, page])

  useEffect(() => { setPage(0) }, [search])
  useEffect(() => { fetchCustomers() }, [fetchCustomers])

  const openCreate = () => { setEditing(null); setForm(EMPTY_FORM); setModalOpen(true) }
  const openEdit = (c) => {
    setEditing(c)
    setForm({ name: c.name, billingAddress: c.billingAddress, shippingAddress: c.shippingAddress, contactEmail: c.contactEmail, phone: c.phone || '', creditTerms: c.creditTerms || 'NET_30' })
    setModalOpen(true)
  }

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      editing ? await customerApi.update(editing.customerId, form) : await customerApi.create(form)
      toast.success(editing ? 'Customer updated' : 'Customer created')
      setModalOpen(false)
      fetchCustomers()
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to save') }
    finally { setSaving(false) }
  }

  const handleDelete = async () => {
    setDeleting(true)
    try {
      await customerApi.delete(deleteTarget.customerId)
      toast.success('Customer deleted')
      setDeleteTarget(null)
      fetchCustomers()
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to delete') }
    finally { setDeleting(false) }
  }

  const f = (k) => (e) => setForm(p => ({ ...p, [k]: e.target.value }))

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h1 className="page-title">Customers</h1>
          <p className="page-subtitle">{totalElements} total customers</p>
        </div>
        {isExportManager() && (
          <button className="btn-primary" onClick={openCreate}><Plus className="h-4 w-4" /> Add Customer</button>
        )}
      </div>

      <div className="card p-0 overflow-hidden">
        <div className="p-4 border-b border-gray-100">
          <SearchBar value={search} onChange={setSearch} placeholder="Search customers..." className="max-w-sm" />
        </div>

        {loading ? <div className="py-20"><LoadingSpinner size="lg" /></div>
          : customers.length === 0 ? <EmptyState title="No customers found" description="Add your first customer" />
          : (
            <>
              <div className="table-wrapper rounded-none border-0">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Name</th><th>Email</th><th>Phone</th><th>Credit Terms</th>
                      {isExportManager() && <th className="text-right">Actions</th>}
                    </tr>
                  </thead>
                  <tbody>
                    {customers.map(c => (
                      <tr key={c.customerId}>
                        <td className="font-medium text-gray-900">{c.name}</td>
                        <td className="text-gray-500">{c.contactEmail}</td>
                        <td className="text-gray-500">{c.phone || '—'}</td>
                        <td><span className="badge badge-info">{c.creditTerms}</span></td>
                        {isExportManager() && (
                          <td className="text-right">
                            <div className="flex items-center justify-end gap-2">
                              <button onClick={() => openEdit(c)} className="p-1.5 rounded-lg hover:bg-gray-100 text-gray-500 hover:text-primary-600"><Pencil className="h-4 w-4" /></button>
                              {isAdmin() && <button onClick={() => setDeleteTarget(c)} className="p-1.5 rounded-lg hover:bg-red-50 text-gray-500 hover:text-red-600"><Trash2 className="h-4 w-4" /></button>}
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

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit Customer' : 'Add Customer'}>
        <form onSubmit={handleSave} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Name *</label>
              <input className="input-field" value={form.name} onChange={f('name')} required />
            </div>
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Contact Email *</label>
              <input type="email" className="input-field" value={form.contactEmail} onChange={f('contactEmail')} required />
            </div>
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Billing Address *</label>
              <input className="input-field" value={form.billingAddress} onChange={f('billingAddress')} required />
            </div>
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Shipping Address *</label>
              <input className="input-field" value={form.shippingAddress} onChange={f('shippingAddress')} required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
              <input className="input-field" value={form.phone} onChange={f('phone')} />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Credit Terms</label>
              <select className="input-field" value={form.creditTerms} onChange={f('creditTerms')}>
                {['NET_15', 'NET_30', 'NET_45', 'NET_60', 'IMMEDIATE'].map(t => <option key={t}>{t}</option>)}
              </select>
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" className="btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={saving}>{saving ? 'Saving...' : editing ? 'Update' : 'Create'}</button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)} onConfirm={handleDelete} title="Delete Customer" message={`Delete "${deleteTarget?.name}"? This cannot be undone.`} loading={deleting} />
    </div>
  )
}
