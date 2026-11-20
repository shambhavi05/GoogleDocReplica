# Test Files

Sample files for manually testing DocSpace's file import feature (`Upload File` on the
dashboard). Used by the UI test checklist.

| File | Purpose |
|---|---|
| `simple-notes.txt` | Valid TXT — multiple paragraphs, should import cleanly |
| `meeting-minutes.txt` | Valid TXT — longer, more realistic content |
| `project-brief.md` | Valid Markdown — headings (`#`/`##`) and bullet lists |
| `release-checklist.md` | Valid Markdown — headings, bullet list, and ordered list |
| `quarterly-review.docx` | Valid DOCX — headings and a bullet list, generated with python-docx |
| `empty-file.txt` | Invalid — 0 bytes, should be rejected as empty |
| `unsupported-file.pdf` | Invalid — wrong extension, should be rejected |
| `oversized-file.txt` | Invalid — ~2.6MB, should be rejected for exceeding the 2MB limit |
