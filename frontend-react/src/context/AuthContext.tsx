import React, { createContext, useContext, useState, useCallback } from 'react';

// ─── Helpers ──────────────────────────────────────────────────────────────────

function getTokenFromStorage(): string | null {
  return localStorage.getItem('token');
}

function getRoleFromToken(fullToken: string | null): string | null {
  if (!fullToken) return null;
  try {
    const token = fullToken.split(' ')[1];
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.role ?? null;
  } catch {
    return null;
  }
}

// ─── Context shape ────────────────────────────────────────────────────────────

interface AuthContextValue {
  role: string | null;
  getToken: () => string | null;
  getRole: () => string | null;
  setToken: (token: string, type: string, refreshToken?: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

// ─── Provider ─────────────────────────────────────────────────────────────────

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [role, setRole] = useState<string | null>(() =>
    getRoleFromToken(getTokenFromStorage())
  );

  const getToken = useCallback(() => getTokenFromStorage(), []);

  const getRole = useCallback(() => getRoleFromToken(getTokenFromStorage()), []);

  const setToken = useCallback((token: string, type: string, refreshToken?: string) => {
    const fullToken = `${type} ${token}`;
    localStorage.setItem('token', fullToken);
    if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
    setRole(getRoleFromToken(fullToken));
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    setRole(null);
  }, []);

  return (
    <AuthContext.Provider value={{ role, getToken, getRole, setToken, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

// ─── Hook ─────────────────────────────────────────────────────────────────────

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
}
