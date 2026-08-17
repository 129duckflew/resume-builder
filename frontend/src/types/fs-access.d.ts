interface SaveFilePickerOptions {
  suggestedName?: string
  types?: {
    description?: string
    accept: Record<string, string[]>
  }[]
}

interface Window {
  showSaveFilePicker(options?: SaveFilePickerOptions): Promise<FileSystemFileHandle>
}
