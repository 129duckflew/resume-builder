import { useRef, useState, useCallback, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Edit3, Trash2, Eye, FileText, Plus, Sparkles, Upload } from 'lucide-react'
import { useResumeStore } from '@/stores/resumeStore'
import { jsonResumeApi } from '@/lib/api'
import { Button } from '@/components/ui/button'
import { ConfirmDialogAction } from '@/components/ui/confirm-dialog'
import { GradientText } from '@/components/effects/GradientText'
import { SpotlightCard } from '@/components/effects/SpotlightCard'
import { toast } from '@/hooks/use-toast'

export default function HomePage() {
  const navigate = useNavigate()
  const { resumes, loading, fetchResumes, deleteResume, createResume } = useResumeStore()
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [deleteTarget, setDeleteTarget] = useState<string | null>(null)
  const [deleteLoading, setDeleteLoading] = useState(false)
  const deleteButtonRef = useRef<HTMLButtonElement | null>(null)

  useEffect(() => {
    fetchResumes()
  }, [fetchResumes])

  const handleDelete = useCallback(async () => {
    if (!deleteTarget) return
    setDeleteLoading(true)
    try {
      await deleteResume(deleteTarget)
      setDeleteTarget(null)
      toast({ title: 'Resume deleted', variant: 'success' })
    } catch {
      toast({ title: 'Delete failed', description: 'Could not delete resume. Please try again.', variant: 'destructive' })
    } finally {
      setDeleteLoading(false)
    }
  }, [deleteTarget, deleteResume])

  const handleCreate = async () => {
    const resume = await createResume("")
    navigate(`/editor/${resume.id}`)
  }

  const handleImportJson = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    try {
      const text = await file.text()
      const data = JSON.parse(text)
      const resume = await jsonResumeApi.importJson(data)
      toast({ title: 'Imported successfully', variant: 'success' })
      fetchResumes()
      navigate(`/editor/${resume.id}`)
    } catch {
      toast({ title: 'Import failed', description: 'Invalid JSON Resume file', variant: 'destructive' })
    }
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-muted-foreground">Loading...</p>
      </div>
    )
  }

  return (
    <>
    <div className="max-w-5xl mx-auto px-6 py-8">
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-2xl font-bold"><GradientText>My Resumes</GradientText></h1>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => fileInputRef.current?.click()}>
            <Upload className="h-4 w-4 mr-1" />
            Import JSON
          </Button>
          <input
            ref={fileInputRef}
            type="file"
            accept=".json"
            className="hidden"
            onChange={handleImportJson}
          />
          <Button onClick={handleCreate}>
            <Plus className="h-4 w-4 mr-1" />
            New Resume
          </Button>
        </div>
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
            <SpotlightCard className="p-4 text-left">
              <h3 className="font-medium mb-1">1. Write</h3>
              <p className="text-sm text-muted-foreground">
                Use Markdown to focus on content. Sections are automatically parsed.
              </p>
            </SpotlightCard>
            <SpotlightCard className="p-4 text-left">
              <h3 className="font-medium mb-1">2. Style</h3>
              <p className="text-sm text-muted-foreground">
                Choose from curated themes. Preview instantly in A4 format.
              </p>
            </SpotlightCard>
            <SpotlightCard className="p-4 text-left">
              <h3 className="font-medium mb-1">3. Export</h3>
              <p className="text-sm text-muted-foreground">
                Download as PDF, HTML, or JSON. Smart one-page ensures a perfect fit.
              </p>
            </SpotlightCard>
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
                    onClick={(e) => {
                      deleteButtonRef.current = e.currentTarget
                      setDeleteTarget(resume.id)
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
      <ConfirmDialogAction
        open={deleteTarget !== null}
        onOpenChange={(open) => {
          if (!open) setDeleteTarget(null)
        }}
        title="Delete Resume"
        description={`Are you sure you want to delete "${resumes.find(r => r.id === deleteTarget)?.title || 'this resume'}"? This action cannot be undone.`}
        confirmLabel="Delete"
        cancelLabel="Cancel"
        variant="destructive"
        loading={deleteLoading}
        onConfirm={handleDelete}
        onCloseAutoFocus={(e) => {
          e.preventDefault()
          deleteButtonRef.current?.focus()
        }}
      />
    </>
  )
}
