import api from './axiosInstance'

export const inventoryApi = {
  getAll: (params) => api.get('/inventory', { params }),
  getById: (id) => api.get(`/inventory/${id}`),
  getLowStock: () => api.get('/inventory/low-stock'),
  getAlerts: () => api.get('/inventory/alerts'),
  stockIn: (data) => api.post('/inventory/stock-in', data),
  stockOut: (data) => api.post('/inventory/stock-out', data),
  transfer: (data) => api.post('/inventory/transfer', data),
  adjust: (data) => api.post('/inventory/adjust', data),
  getMovements: (id, params) => api.get(`/inventory/${id}/movements`, { params }),
}
