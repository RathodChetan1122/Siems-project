import api from './axiosInstance'

export const warehouseApi = {
  getAll: (params) => api.get('/warehouses', { params }),
  getById: (id) => api.get(`/warehouses/${id}`),
  create: (data) => api.post('/warehouses', data),
  update: (id, data) => api.put(`/warehouses/${id}`, data),
  deactivate: (id) => api.delete(`/warehouses/${id}`),
}
