import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import AuthGuard from './components/AuthGuard'
import HomePage from './pages/HomePage'
import EditorPage from './pages/EditorPage'
import PreviewPage from './pages/PreviewPage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import { Toaster } from '@/components/ui/toaster'

export default function App() {
  return (
    <>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route element={<AuthGuard />}>
          <Route element={<Layout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/editor/:id" element={<EditorPage />} />
            <Route path="/preview/:id" element={<PreviewPage />} />
          </Route>
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      <Toaster />
    </>
  )
}
