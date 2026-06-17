import React from 'react'
import { PackageOpen } from 'lucide-react'

export default function EmptyState({ title = 'No results found', description = 'Try adjusting your search or filters', icon: Icon = PackageOpen, action }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 px-4 text-center">
      <div className="p-4 bg-gray-100 rounded-2xl mb-4">
        <Icon className="h-10 w-10 text-gray-400" />
      </div>
      <h3 className="text-base font-semibold text-gray-700">{title}</h3>
      <p className="text-sm text-gray-400 mt-1 max-w-xs">{description}</p>
      {action && <div className="mt-4">{action}</div>}
    </div>
  )
}
