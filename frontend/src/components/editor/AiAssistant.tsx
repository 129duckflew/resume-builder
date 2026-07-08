import { useState } from 'react'
import { Sparkles, Settings2, Loader2, Key } from 'lucide-react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { toast } from '@/hooks/use-toast'
import { aiApi, userApi } from '@/lib/api'
import { useResumeStore } from '@/stores/resumeStore'

interface Props {
  open: boolean
  onOpenChange: (v: boolean) => void
}

export default function AiAssistant({ open, onOpenChange }: Props) {
  const [mode, setMode] = useState<'rewrite' | 'suggest' | 'settings'>('rewrite')
  const [input, setInput] = useState('')
  const [result, setResult] = useState('')
  const [loading, setLoading] = useState(false)
  const [apiKey, setApiKey] = useState('')
  const currentResume = useResumeStore((s) => s.currentResume)
  const updateResume = useResumeStore((s) => s.updateResume)

  const handleOpen = () => {
    userApi.getApiKey().then(r => setApiKey(r.apiKey)).catch(() => {})
  }

  const doRewrite = async () => {
    if (!currentResume) return
    setLoading(true)
    setResult('')
    try {
      const res = await aiApi.rewrite(currentResume.id, input || 'Improve the writing')
      setResult(res.content)
    } catch (err: any) {
      toast({ title: 'AI request failed', description: err.response?.data?.error || err.message, variant: 'destructive' })
    } finally {
      setLoading(false)
    }
  }

  const doSuggest = async () => {
    if (!currentResume) return
    setLoading(true)
    setResult('')
    try {
      const res = await aiApi.suggest(currentResume.id, input)
      setResult(res.content)
    } catch (err: any) {
      toast({ title: 'AI request failed', description: err.response?.data?.error || err.message, variant: 'destructive' })
    } finally {
      setLoading(false)
    }
  }

  const saveApiKey = async () => {
    try {
      await userApi.updateApiKey(apiKey)
      toast({ title: 'API key saved', variant: 'success' })
      setMode('rewrite')
    } catch {
      toast({ title: 'Failed to save API key', variant: 'destructive' })
    }
  }

  const applyResult = () => {
    if (!currentResume || !result) return
    const newContent = currentResume.content + '\n\n' + result
    updateResume(currentResume.id, { content: newContent })
    useResumeStore.setState({
      currentResume: { ...currentResume, content: newContent },
    })
    toast({ title: 'Content applied', variant: 'success' })
  }

  return (
    <Dialog open={open} onOpenChange={(v) => { onOpenChange(v); if (v) handleOpen() }}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Sparkles className="h-5 w-5" />
            AI Assistant
          </DialogTitle>
          <DialogDescription>
            Powered by your own API key. Works with OpenAI-compatible providers.
          </DialogDescription>
        </DialogHeader>

        <div className="flex gap-2 mb-3">
          <Button variant={mode === 'rewrite' ? 'default' : 'outline'} size="sm" onClick={() => setMode('rewrite')}>
            Rewrite
          </Button>
          <Button variant={mode === 'suggest' ? 'default' : 'outline'} size="sm" onClick={() => setMode('suggest')}>
            Suggest
          </Button>
          <div className="flex-1" />
          <Button variant="ghost" size="icon" className="h-8 w-8" onClick={() => setMode('settings')} title="API Key Settings">
            <Settings2 className="h-4 w-4" />
          </Button>
        </div>

        {mode === 'settings' ? (
          <div className="space-y-3">
            <p className="text-sm text-muted-foreground">Enter your OpenAI API key (or any compatible provider).</p>
            <input
              type="password"
              className="w-full border rounded px-3 py-2 text-sm font-mono"
              placeholder="sk-..."
              value={apiKey}
              onChange={e => setApiKey(e.target.value)}
            />
            <Button onClick={saveApiKey} className="w-full">
              <Key className="h-4 w-4 mr-1" />
              Save API Key
            </Button>
          </div>
        ) : (
          <div className="space-y-3">
            {mode === 'rewrite' ? (
              <input
                className="w-full border rounded px-3 py-2 text-sm"
                placeholder="Instruction (e.g. 'Make it more concise', 'Use stronger action verbs')"
                value={input}
                onChange={e => setInput(e.target.value)}
              />
            ) : (
              <textarea
                className="w-full border rounded px-3 py-2 text-sm min-h-20"
                placeholder="Paste job description here for tailored suggestions..."
                value={input}
                onChange={e => setInput(e.target.value)}
              />
            )}
            <Button onClick={mode === 'rewrite' ? doRewrite : doSuggest} disabled={loading} className="w-full">
              {loading ? <Loader2 className="h-4 w-4 mr-1 animate-spin" /> : <Sparkles className="h-4 w-4 mr-1" />}
              {loading ? 'Processing...' : mode === 'rewrite' ? 'Rewrite' : 'Get Suggestions'}
            </Button>
            {result && (
              <div className="space-y-2">
                <div className="border rounded-lg p-3 text-sm whitespace-pre-wrap bg-gray-50 max-h-60 overflow-y-auto">
                  {result}
                </div>
                <Button variant="outline" onClick={applyResult} className="w-full">
                  Append to Resume
                </Button>
              </div>
            )}
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}
