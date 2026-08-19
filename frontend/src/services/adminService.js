import { api, qs } from './api';

export const adminApi = {
  allPosts: (params) => api.get(`/admin/posts?${qs(params)}`),
  changeStatus: (id, status) => api.put(`/admin/posts/${id}/status?status=${status}`),
  toggleFeatured: (id) => api.put(`/admin/posts/${id}/feature`),
  users: () => api.get('/admin/users'),
  updateRole: (id, role) => api.put(`/admin/users/${id}?role=${role}`),
  deleteUser: (id) => api.del(`/admin/users/${id}`),
  reports: () => api.get('/admin/reports'),
};
