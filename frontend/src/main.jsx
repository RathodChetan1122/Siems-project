import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import App from './App.jsx'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: {
            background: '#1e3a8a',
            color: '#fff',
            borderRadius: '8px',
            fontSize: '14px',
          },
          success: {
            style: { background: '#065f46' },
            iconTheme: { primary: '#fff', secondary: '#065f46' },
          },
          error: {
            style: { background: '#991b1b' },
            iconTheme: { primary: '#fff', secondary: '#991b1b' },
          },
        }}
      />
    </BrowserRouter>
  </React.StrictMode>,
)
