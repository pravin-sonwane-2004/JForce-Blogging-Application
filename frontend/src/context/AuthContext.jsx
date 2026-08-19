import { createContext, useContext, useEffect, useState } from 'react';
import { authApi } from '../services/authService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // On first load ask the backend who is logged in (session cookie)
  useEffect(() => {
    authApi
      .me()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  const login = async (username, password) => {
    const u = await authApi.login(username, password);
    setUser(u);
    return u;
  };

  const register = async (username, password, email) => {
    // Registration does NOT create a session - user will go to the login page
    return authApi.register(username, password, email);
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } catch {
      // ignore - clear local state regardless
    }
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
