import React from 'react'
import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard, Truck, Users, Package, Warehouse,
  Archive, Ship, BarChart3, X, Globe2
} from 'lucide-react'
import { useAuth } from '../../context/AuthContext'

const NAV_ITEMS = [
  { label: 'Dashboard',  path: '/dashboard',  icon: LayoutDashboard },
  { label: 'Suppliers',  path: '/suppliers',  icon: Truck },
  { label: 'Customers',  path: '/customers',  icon: Users },
  { label: 'Products',   path: '/products',   icon: Package },
  { label: 'Warehouses', path: '/warehouses', icon: Warehouse },
  { label: 'Inventory',  path: '/inventory',  icon: Archive },
  { label: 'Shipments',  path: '/shipments',  icon: Ship },
  { label: 'Analytics',  path: '/analytics',  icon: BarChart3 },
]

export default function Sidebar({ isOpen, onClose }) {
  const { user } = useAuth()

  return (
    <>
      {/* Desktop sidebar */}
      <aside className="hidden lg:flex lg:flex-col w-64 bg-siems-navy border-r border-blue-900 flex-shrink-0">
        <SidebarContent user={user} />
      </aside>

      {/* Mobile sidebar */}
      <aside
        className={`fixed top-0 left-0 z-30 h-full w-64 bg-siems-navy transform transition-transform duration-300 ease-in-out lg:hidden ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex justify-end p-4">
          <button onClick={onClose} className="p-1.5 rounded-lg text-blue-300 hover:text-white hover:bg-blue-800">
            <X className="h-5 w-5" />
          </button>
        </div>
        <SidebarContent user={user} onNavigate={onClose} />
      </aside>
    </>
  )
}

function SidebarContent({ user, onNavigate }) {
  return (
    <div className="flex flex-col h-full">
      {/* Brand */}
      <div className="flex items-center gap-3 px-5 py-5 border-b border-blue-800">
        <div className="p-2 bg-blue-600 rounded-xl">
          <Globe2 className="h-5 w-5 text-white" />
        </div>
        <div>
          <p className="text-white font-bold text-sm leading-tight">SIEMS</p>
          <p className="text-blue-300 text-xs">Import-Export Manager</p>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto px-3 py-4 space-y-0.5">
        {NAV_ITEMS.map(({ label, path, icon: Icon }) => (
          <NavLink
            key={path}
            to={path}
            onClick={onNavigate}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-blue-600 text-white shadow-md'
                  : 'text-blue-200 hover:bg-blue-800 hover:text-white'
              }`
            }
          >
            <Icon className="h-4 w-4 flex-shrink-0" />
            {label}
          </NavLink>
        ))}
      </nav>

      {/* User footer */}
      {user && (
        <div className="px-4 py-4 border-t border-blue-800">
          <div className="flex items-center gap-3">
            <div className="h-8 w-8 rounded-full bg-blue-600 flex items-center justify-center text-white text-sm font-bold flex-shrink-0">
              {user.username?.[0]?.toUpperCase()}
            </div>
            <div className="min-w-0">
              <p className="text-white text-sm font-medium truncate">{user.username}</p>
              <p className="text-blue-300 text-xs truncate">{user.role?.replace('_', ' ')}</p>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
