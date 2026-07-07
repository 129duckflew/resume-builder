import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Edit3, Trash2, Eye, FileText, Plus, Sparkles } from 'lucide-react'
import { useResumeStore } from '@/stores/resumeStore'
import { Button } from '@/components/ui/button'

export default function HomePage() {
  const navigate = useNavigate()
  const { resumes, loading, fetchResumes, deleteResume, createResume } = useResumeStore()

  useEffect(() => {
    fetchResumes()
  }, [fetchResumes])

  const handleCreate = async () => {
    const title = `Resume ${new Date().toLocaleDateString()}`
    const resume = await createResume(title)
    navigate(`/editor/${resume.id}`)
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-muted-foreground">Loading...</p>
      </div>
    )
  }

  return (
    <div className="max-w-5xl mx-auto px-6 py-8">
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-2xl font-bold">My Resumes</h1>
        <Button onClick={handleCreate}>
          <Plus className="h-4 w-4 mr-1" />
          New Resume
        </Button>
      </div>

      {resumes.length === 0 ? (
        <div className="text-center py-20">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-primary/10 mb-6">
            <FileText className="h-8 w-8 text-primary" />
          </div>
          <h2 className="text-xl font-semibold mb-2">Create your first resume</h2>
          <p className="text-muted-foreground mb-8 max-w-md mx-auto">
            Build a professional resume in minutes. Write in Markdown, choose a theme,
            and export as PDF or HTML — all in one place.
          </p>
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-12">
            <Button size="lg" onClick={handleCreate}>
              <Sparkles className="h-5 w-5 mr-2" />
              Get Started
            </Button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 max-w-2xl mx-auto">
            <div className="p-4 border rounded-lg bg-white">
              <h3 className="font-medium mb-1">1. Write</h3>
              <p className="text-sm text-muted-foreground">
                Use Markdown to focus on content. Sections are automatically parsed.
              </p>
            </div>
            <div className="p-4 border rounded-lg bg-white">
              <h3 className="font-medium mb-1">2. Style</h3>
              <p className="text-sm text-muted-foreground">
                Choose from curated themes. Preview instantly in A4 format.
              </p>
            </div>
            <div className="p-4 border rounded-lg bg-white">
              <h3 className="font-medium mb-1">3. Export</h3>
              <p className="text-sm text-muted-foreground">
                Download as PDF or standalone HTML. Smart one-page ensures a perfect fit.
              </p>
            </div>
          </div>
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
