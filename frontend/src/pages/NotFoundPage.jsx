import React from 'react'
import { Link } from 'react-router-dom'
import { Globe2, Home } from 'lucide-react'

export default function NotFoundPage() {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4 text-center">
      <div className="p-4 bg-primary-100 rounded-2xl mb-6">
        <Globe2 className="h-12 w-12 text-primary-600" />
      </div>
      <h1 className="text-6xl font-black text-gray-900 mb-2">404</h1>
      <h2 className="text-xl font-semibold text-gray-700 mb-2">Page not found</h2>
      <p className="text-gray-500 text-sm mb-8 max-w-xs">
        The page you're looking for doesn't exist or has been moved.
      </p>
      <Link to="/dashboard" className="btn-primary">
        <Home className="h-4 w-4" />
        Back to Dashboard
      </Link>
    </div>
  )
}
