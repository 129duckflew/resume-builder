import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { MoreVertical, Edit3, Trash2, Eye } from 'lucide-react'
import { useResumeStore } from '@/stores/resumeStore'
import { Button } from '@/components/ui/button'

export default function HomePage() {
  const navigate = useNavigate()
  const { resumes, loading, fetchResumes, deleteResume } = useResumeStore()

  useEffect(() => {
    fetchResumes()
  }, [fetchResumes])

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-muted-foreground">Loading...</p>
      </div>
    )
  }

  return (
    <div className="max-w-5xl mx-auto px-6 py-8">
      <h1 className="text-2xl font-bold mb-6">My Resumes</h1>

      {resumes.length === 0 ? (
        <div className="text-center py-16 border-2 border-dashed rounded-lg">
          <p className="text-muted-foreground mb-4">No resumes yet</p>
          <p className="text-sm text-muted-foreground">
            Click "New Resume" in the header to get started
          </p>
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {resumes.map((resume) => (
            <div
              key={resume.id}
              className="border rounded-lg p-4 hover:shadow-md transition-shadow bg-white"
            >
              <div className="flex items-start justify-between mb-3">
                <h3 className="font-medium truncate flex-1">{resume.title}</h3>
                <div className="flex gap-1 ml-2">
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => navigate(`/editor/${resume.id}`)}
                    title="Edit"
                  >
                    <Edit3 className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => navigate(`/preview/${resume.id}`)}
                    title="Preview"
                  >
                    <Eye className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => {
                      if (confirm('Delete this resume?')) {
                        deleteResume(resume.id)
                      }
                    }}
                    title="Delete"
                  >
                    <Trash2 className="h-4 w-4 text-destructive" />
                  </Button>
                </div>
              </div>
              <p className="text-xs text-muted-foreground">
                Theme: {resume.themeId} &middot; Updated:{' '}
                {new Date(resume.updatedAt).toLocaleDateString()}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
