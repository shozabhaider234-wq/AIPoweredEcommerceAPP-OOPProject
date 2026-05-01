import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser]     = useState(null);
  const [token, setToken]   = useState(() => localStorage.getItem('token'));
  const [loading, setLoading] = useState(!!localStorage.getItem('token'));

  // Load profile on mount if token exists
  useEffect(() => {
    if (!token) { setLoading(false); return; }
    authAPI.profile()
      .then(res => setUser(res.data.data))
      .catch(() => { localStorage.removeItem('token'); setToken(null); })
      .finally(() => setLoading(false));
  }, [token]);

  const login = useCallback(async (email, password) => {
    const res = await authAPI.login({ email, password });
    const { token: t, role, email: e } = res.data.data;
    localStorage.setItem('token', t);
    setToken(t);
    // Fetch full profile
    const profileRes = await authAPI.profile();
    setUser(profileRes.data.data);
    return profileRes.data.data;
  }, []);

  const register = useCallback(async (data) => {
    const res = await authAPI.register(data);
    const { token: t } = res.data.data;
    localStorage.setItem('token', t);
    setToken(t);
    const profileRes = await authAPI.profile();
    setUser(profileRes.data.data);
    return profileRes.data.data;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
  }, []);

  const isCustomer = user?.role === 'CUSTOMER';
  const isSeller   = user?.role === 'SELLER';
  const isAdmin    = user?.role === 'ADMIN';
  const isLoggedIn = !!user;

  return (
    <AuthContext.Provider value={{
      user, token, loading,
      login, register, logout,
      isCustomer, isSeller, isAdmin, isLoggedIn
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be inside AuthProvider');
  return ctx;
};
