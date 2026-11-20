# Release Checklist

Use this before shipping a new version of DocSpace. Tests ordered-list and bullet-list import together.

## Pre-release steps

- Run the backend test suite
- Run the frontend test suite
- Smoke-test login as all three demo accounts
- Confirm file import rejects unsupported extensions

## Release steps

1. Tag the release in git
2. Build the backend jar
3. Build the frontend production bundle
4. Deploy backend and frontend
5. Smoke-test the deployed environment

## Rollback plan

If the deploy breaks login or document loading, revert to the previous tagged release immediately and investigate in a branch rather than live.

## Notes

This checklist intentionally has no code blocks or tables, since the current Markdown importer only understands headings, bullet lists, ordered lists, and plain paragraphs.
