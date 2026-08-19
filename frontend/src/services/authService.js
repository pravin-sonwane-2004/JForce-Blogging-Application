import { api } from './api';

export const authApi = {
  me: () => api.get('/users/me'),
  login: (username, password) => api.post('/auth/login', { username, password }),
  register: (username, password, email) => api.post('/auth/register', { username, password, email }),
  logout: () => api.post('/auth/logout'),
};
