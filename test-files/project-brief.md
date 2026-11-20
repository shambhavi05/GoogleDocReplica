# Project Brief

This file tests Markdown import: headings, paragraphs, and bullet lists.

## Background

DocSpace needs a lightweight way to pull existing notes into the editor without retyping everything by hand.

## Goals

- Support dragging a file straight onto the dashboard
- Reject anything that is not TXT or Markdown
- Enforce a 2MB maximum upload size
- Convert headings and bullet lists into real editor formatting, not plain text

## Non-goals

- Full CommonMark support (tables, code blocks, links are out of scope for now)
- Real-time collaborative import (one file becomes one new document, owned by the importer)

## Open questions

Should imported documents be automatically shared with anyone, or always start private to the importer? Current behavior: always private, matching how a brand-new document works.
