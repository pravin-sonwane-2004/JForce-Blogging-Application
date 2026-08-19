import { api, qs } from './api';

export const postApi = {
  list: (params) => api.get(`/posts?${qs(params)}`),
  get: (id) => api.get(`/posts/${id}`),
  mine: (params) => api.get(`/posts/mine?${qs(params)}`),
  create: (body) => api.post('/posts', body),
  update: (id, body) => api.put(`/posts/${id}`, body),
  remove: (id) => api.del(`/posts/${id}`),
};
