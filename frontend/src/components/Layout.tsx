import { Outlet, Link, useNavigate } from 'react-router-dom'
import { FileText, Plus, LogOut } from 'lucide-react'
import { Button } from './ui/button'
import { Toaster } from './ui/toaster'
import { useAuthStore } from '@/stores/authStore'
import { useResumeStore } from '@/stores/resumeStore'

export default function Layout() {
  const navigate = useNavigate()
  const username = useAuthStore((s) => s.username)
  const logout = useAuthStore((s) => s.logout)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b bg-white px-6 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Link to="/" className="flex items-center gap-2 font-semibold text-lg">
            <FileText className="h-5 w-5" />
            Resume Builder
          </Link>
        </div>
        <div className="flex items-center gap-3">
          <NewResumeButton />
          <span className="text-sm text-muted-foreground">{username}</span>
          <Button variant="ghost" size="sm" onClick={handleLogout} title="Sign out">
            <LogOut className="h-4 w-4" />
          </Button>
        </div>
      </header>
      <main className="flex-1">
        <Outlet />
      </main>
      <Toaster />
    </div>
  )
}

function NewResumeButton() {
  const createResume = useResumeStore((s) => s.createResume)

  const handleCreate = async () => {
    const title = `Resume ${new Date().toLocaleDateString()}`
    const resume = await createResume(title)
    window.location.href = `/editor/${resume.id}`
  }

  return (
    <Button onClick={handleCreate} size="sm">
      <Plus className="h-4 w-4 mr-1" />
      New Resume
    </Button>
  )
}
