import { useCallback, useRef, useState } from 'react';
import { UploadCloud, X, FileText, CheckCircle2, AlertCircle } from 'lucide-react';
import { importFile, ALLOWED_IMPORT_EXTENSIONS, MAX_IMPORT_SIZE_BYTES } from '../services/files';
import { ApiError } from '../services/api';

interface FileUploadModalProps {
  onClose: () => void;
  onImported: (documentId: string) => void;
}

type UploadState = 'idle' | 'uploading' | 'success' | 'error';

function hasAllowedExtension(filename: string): boolean {
  const lower = filename.toLowerCase();
  return ALLOWED_IMPORT_EXTENSIONS.some((ext) => lower.endsWith(ext));
}

export default function FileUploadModal({ onClose, onImported }: FileUploadModalProps) {
  const [state, setState] = useState<UploadState>('idle');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const inputRef = useRef<HTMLInputElement | null>(null);

  const handleFile = useCallback(async (file: File) => {
    if (!hasAllowedExtension(file.name)) {
      setState('error');
      setErrorMessage('Only .txt, .md, and .docx files are supported.');
      return;
    }
    if (file.size > MAX_IMPORT_SIZE_BYTES) {
      setState('error');
      setErrorMessage('File exceeds the maximum allowed size of 2MB.');
      return;
    }
    if (file.size === 0) {
      setState('error');
      setErrorMessage('This file is empty.');
      return;
    }

    setState('uploading');
    setErrorMessage(null);
    try {
      const created = await importFile(file);
      setState('success');
      setTimeout(() => onImported(created.id), 500);
    } catch (err) {
      setState('error');
      setErrorMessage(err instanceof ApiError ? err.message : 'Upload failed. Please try again.');
    }
  }, [onImported]);

  function handleDrop(event: React.DragEvent<HTMLDivElement>) {
    event.preventDefault();
    setIsDragging(false);
    const file = event.dataTransfer.files?.[0];
    if (file) handleFile(file);
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-base font-semibold text-neutral-900">Import a file</h2>
          <button
            type="button"
            onClick={onClose}
            className="rounded-md p-1 text-neutral-400 hover:bg-neutral-100 hover:text-neutral-600"
          >
            <X size={18} />
          </button>
        </div>

        <div
          onDragOver={(e) => {
            e.preventDefault();
            setIsDragging(true);
          }}
          onDragLeave={() => setIsDragging(false)}
          onDrop={handleDrop}
          onClick={() => inputRef.current?.click()}
          className={`flex cursor-pointer flex-col items-center justify-center rounded-xl border-2 border-dashed px-6 py-10 text-center transition ${
            isDragging ? 'border-brand-400 bg-brand-50' : 'border-neutral-200 hover:border-neutral-300'
          }`}
        >
          {state === 'uploading' ? (
            <>
              <span className="mb-2 h-6 w-6 animate-spin rounded-full border-2 border-neutral-300 border-t-brand-600" />
              <p className="text-sm text-neutral-600">Uploading…</p>
            </>
          ) : state === 'success' ? (
            <>
              <CheckCircle2 size={28} className="mb-2 text-brand-600" />
              <p className="text-sm font-medium text-neutral-800">Imported! Opening document…</p>
            </>
          ) : (
            <>
              <UploadCloud size={28} className="mb-2 text-neutral-400" />
              <p className="text-sm font-medium text-neutral-700">
                Drag and drop a file, or click to browse
              </p>
              <p className="mt-1 text-xs text-neutral-400">Supports TXT, MD, and DOCX · Max 2MB</p>
            </>
          )}
        </div>

        {state === 'error' && errorMessage && (
          <div className="mt-3 flex items-start gap-2 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            <AlertCircle size={15} className="mt-0.5 flex-shrink-0" />
            <span>{errorMessage}</span>
          </div>
        )}

        <div className="mt-4 flex items-center gap-2 text-xs text-neutral-400">
          <FileText size={13} />
          <span>Imported files become a new document you own.</span>
        </div>

        <input
          ref={inputRef}
          type="file"
          accept=".txt,.md,.docx"
          className="hidden"
          onChange={(e) => {
            const file = e.target.files?.[0];
            if (file) handleFile(file);
          }}
        />
      </div>
    </div>
  );
}
