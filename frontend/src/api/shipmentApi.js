import api from './axiosInstance'

export const shipmentApi = {
  getAll: (params) => api.get('/shipments', { params }),
  getById: (id) => api.get(`/shipments/${id}`),
  create: (data) => api.post('/shipments', data),
  updateStatus: (id, data) => api.patch(`/shipments/${id}/status`, data),
  getTracking: (id) => api.get(`/shipments/${id}/tracking`),
  trackByNumber: (trackingNumber) => api.get(`/shipments/track/${trackingNumber}`),
}
