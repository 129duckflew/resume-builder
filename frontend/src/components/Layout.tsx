import { Outlet, Link } from 'react-router-dom'
import { FileText, Plus, Download } from 'lucide-react'
import { Button } from './ui/button'
import { useResumeStore } from '@/stores/resumeStore'

export default function Layout() {
  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b bg-white px-6 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Link to="/" className="flex items-center gap-2 font-semibold text-lg">
            <FileText className="h-5 w-5" />
            Resume Builder
          </Link>
        </div>
        <NewResumeButton />
      </header>
      <main className="flex-1">
        <Outlet />
      </main>
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
