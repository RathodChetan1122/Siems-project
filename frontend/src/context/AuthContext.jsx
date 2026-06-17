import React, { createContext, useContext, useState, useEffect, useCallback } from 'react'
import api from '../api/axiosInstance'

const AuthContext = createContext(null)

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const accessToken = localStorage.getItem('accessToken')
    const storedUser = localStorage.getItem('user')
    if (accessToken && storedUser) {
      try {
        setUser(JSON.parse(storedUser))
      } catch {
        localStorage.clear()
      }
    }
    setLoading(false)
  }, [])

  const login = useCallback((authResponse) => {
    const { accessToken, refreshToken, username, role, expiresIn } = authResponse
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    const userData = { username, role }
    localStorage.setItem('user', JSON.stringify(userData))
    setUser(userData)
  }, [])

  const logout = useCallback(async () => {
    try {
      await api.post('/auth/logout')
    } catch {
      // Logout best-effort — clear local state regardless
    } finally {
      localStorage.clear()
      setUser(null)
    }
  }, [])

  const hasRole = useCallback((...roles) => {
    if (!user) return false
    return roles.some(r => user.role === r || user.role === `ROLE_${r}`)
  }, [user])

  const isAdmin = () => hasRole('ADMIN')
  const isImportManager = () => hasRole('ADMIN', 'IMPORT_MANAGER')
  const isExportManager = () => hasRole('ADMIN', 'EXPORT_MANAGER')
  const isInventoryManager = () => hasRole('ADMIN', 'INVENTORY_MANAGER')

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, hasRole, isAdmin, isImportManager, isExportManager, isInventoryManager }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
