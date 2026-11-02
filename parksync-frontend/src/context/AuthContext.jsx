import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('parksync_token') || null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadUser() {
      const storedToken = localStorage.getItem('parksync_token');
      const storedUser = localStorage.getItem('parksync_user');
      
      if (storedToken && storedUser) {
        try {
          setUser(JSON.parse(storedUser));
          // Refresh user profile in background
          const me = await api.getCurrentUser();
          setUser(me);
          localStorage.setItem('parksync_user', JSON.stringify(me));
        } catch (err) {
          console.warn('Session expired or invalid:', err);
          logout();
        }
      }
      setLoading(false);
    }
    loadUser();
  }, []);

  const login = async (email, password) => {
    const res = await api.login({ email, password });
    localStorage.setItem('parksync_token', res.token);
    localStorage.setItem('parksync_user', JSON.stringify(res.user));
    setToken(res.token);
    setUser(res.user);
    return res.user;
  };

  const register = async (fullName, email, phone, password, role = 'ROLE_USER') => {
    const res = await api.register({ fullName, email, phone, password, role });
    localStorage.setItem('parksync_token', res.token);
    localStorage.setItem('parksync_user', JSON.stringify(res.user));
    setToken(res.token);
    setUser(res.user);
    return res.user;
  };

  const logout = () => {
    localStorage.removeItem('parksync_token');
    localStorage.removeItem('parksync_user');
    setToken(null);
    setUser(null);
  };

  const updateUserProfile = (updated) => {
    setUser(updated);
    localStorage.setItem('parksync_user', JSON.stringify(updated));
  };

  const isAdmin = user && user.role === 'ROLE_ADMIN';

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout, updateUserProfile, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
