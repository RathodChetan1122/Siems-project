import api from './axiosInstance'

export const analyticsApi = {
  getDashboard: () => api.get('/analytics/dashboard'),
}
