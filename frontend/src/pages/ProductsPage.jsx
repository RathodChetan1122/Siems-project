import React, { useState, useEffect, useCallback } from 'react'
import { Plus, Pencil, Trash2 } from 'lucide-react'
import { productApi } from '../api/productApi'
import { supplierApi } from '../api/supplierApi'
import { useAuth } from '../context/AuthContext'
import SearchBar from '../components/common/SearchBar'
import Pagination from '../components/common/Pagination'
import Modal from '../components/common/Modal'
import ConfirmDialog from '../components/common/ConfirmDialog'
import EmptyState from '../components/common/EmptyState'
import LoadingSpinner from '../components/common/LoadingSpinner'
import toast from 'react-hot-toast'

const EMPTY_FORM = { sku: '', name: '', category: '', unitOfMeasure: 'PCS', unitPrice: '', supplierId: '' }

export default function ProductsPage() {
  const { isImportManager, isInventoryManager, isAdmin } = useAuth()
  const canWrite = () => isImportManager() || isInventoryManager()

  const [products, setProducts] = useState([])
  const [suppliers, setSuppliers] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('')
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [saving, setSaving] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    supplierApi.getAll({ page: 0, size: 100 })
      .then(r => setSuppliers(r.data.data.content || []))
      .catch(() => {})
  }, [])

  const fetchProducts = useCallback(async () => {
    setLoading(true)
    try {
      const res = await productApi.getAll({ keyword: search || undefined, category: category || undefined, page, size: 10 })
      const d = res.data.data
      setProducts(d.content); setTotalPages(d.totalPages); setTotalElements(d.totalElements)
    } catch { toast.error('Failed to load products') }
    finally { setLoading(false) }
  }, [search, category, page])

  useEffect(() => { setPage(0) }, [search, category])
  useEffect(() => { fetchProducts() }, [fetchProducts])

  const openCreate = () => { setEditing(null); setForm(EMPTY_FORM); setModalOpen(true) }
  const openEdit = (p) => {
    setEditing(p)
    setForm({ sku: p.sku, name: p.name, category: p.category || '', unitOfMeasure: p.unitOfMeasure, unitPrice: p.unitPrice, supplierId: p.supplierId || '' })
    setModalOpen(true)
  }

  const handleSave = async (e) => {
    e.preventDefault(); setSaving(true)
    try {
      const payload = { ...form, supplierId: form.supplierId ? Number(form.supplierId) : null }
      editing ? await productApi.update(editing.productId, payload) : await productApi.create(payload)
      toast.success(editing ? 'Product updated' : 'Product created')
      setModalOpen(false); fetchProducts()
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to save') }
    finally { setSaving(false) }
  }

  const handleDelete = async () => {
    setDeleting(true)
    try {
      await productApi.delete(deleteTarget.productId)
      toast.success('Product deleted'); setDeleteTarget(null); fetchProducts()
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to delete') }
    finally { setDeleting(false) }
  }

  const f = (k) => (e) => setForm(p => ({ ...p, [k]: e.target.value }))

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h1 className="page-title">Products</h1>
          <p className="page-subtitle">{totalElements} total products</p>
        </div>
        {canWrite() && <button className="btn-primary" onClick={openCreate}><Plus className="h-4 w-4" />Add Product</button>}
      </div>

      <div className="card p-0 overflow-hidden">
        <div className="p-4 border-b border-gray-100 flex flex-wrap gap-3">
          <SearchBar value={search} onChange={setSearch} placeholder="Search products, SKU..." className="flex-1 min-w-[200px]" />
          <select className="input-field w-44" value={category} onChange={e => setCategory(e.target.value)}>
            <option value="">All Categories</option>
            {['Textiles', 'Electronics', 'Automotive'].map(c => <option key={c}>{c}</option>)}
          </select>
        </div>

        {loading ? <div className="py-20"><LoadingSpinner size="lg" /></div>
          : products.length === 0 ? <EmptyState title="No products found" description="Add your first product to get started" />
          : (
            <>
              <div className="table-wrapper rounded-none border-0">
                <table className="table">
                  <thead>
                    <tr>
                      <th>SKU</th><th>Name</th><th>Category</th><th>UOM</th><th>Unit Price</th><th>Supplier</th>
                      {canWrite() && <th className="text-right">Actions</th>}
                    </tr>
                  </thead>
                  <tbody>
                    {products.map(p => (
                      <tr key={p.productId}>
                        <td className="font-mono text-xs text-gray-500">{p.sku}</td>
                        <td className="font-medium text-gray-900">{p.name}</td>
                        <td>{p.category ? <span className="badge badge-info">{p.category}</span> : '—'}</td>
                        <td>{p.unitOfMeasure}</td>
                        <td className="font-semibold">${Number(p.unitPrice).toFixed(2)}</td>
                        <td className="text-gray-500">{p.supplierName || '—'}</td>
                        {canWrite() && (
                          <td className="text-right">
                            <div className="flex items-center justify-end gap-2">
                              <button onClick={() => openEdit(p)} className="p-1.5 rounded-lg hover:bg-gray-100 text-gray-500 hover:text-primary-600"><Pencil className="h-4 w-4" /></button>
                              {isAdmin() && <button onClick={() => setDeleteTarget(p)} className="p-1.5 rounded-lg hover:bg-red-50 text-gray-500 hover:text-red-600"><Trash2 className="h-4 w-4" /></button>}
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

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit Product' : 'Add Product'}>
        <form onSubmit={handleSave} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">SKU *</label>
              <input className="input-field" value={form.sku} onChange={f('sku')} required disabled={!!editing} />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Name *</label>
              <input className="input-field" value={form.name} onChange={f('name')} required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Category</label>
              <input className="input-field" value={form.category} onChange={f('category')} />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Unit of Measure</label>
              <select className="input-field" value={form.unitOfMeasure} onChange={f('unitOfMeasure')}>
                {['PCS', 'KG', 'METER', 'LITER', 'SET', 'BOX'].map(u => <option key={u}>{u}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Unit Price *</label>
              <input type="number" step="0.01" min="0" className="input-field" value={form.unitPrice} onChange={f('unitPrice')} required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Supplier</label>
              <select className="input-field" value={form.supplierId} onChange={f('supplierId')}>
                <option value="">None</option>
                {suppliers.map(s => <option key={s.supplierId} value={s.supplierId}>{s.name}</option>)}
              </select>
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" className="btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={saving}>{saving ? 'Saving...' : editing ? 'Update' : 'Create'}</button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)} onConfirm={handleDelete} title="Delete Product" message={`Delete "${deleteTarget?.name}"?`} loading={deleting} />
    </div>
  )
}
