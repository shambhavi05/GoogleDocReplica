# DocSpace — Manual UI Test Cases

Run these against the running app at `http://localhost:5173` (backend on `:8080`). Go through them
top to bottom — later sections assume earlier ones passed (e.g. sharing tests assume a document
already exists). Sample files for the import tests live in [`test-files/`](test-files/).

Demo accounts (no password):

| Name | Email |
|---|---|
| Shambhavi | `shambhavi@ajaia.com` |
| Bob | `bob@agij.com` |
| Charlie | `charlie@agij.com` |

---

## 1. Authentication

- [ ] **1.1** Open the app while logged out. Confirm you land on the login page with all three
      demo accounts listed, each showing a name and a short description.
- [ ] **1.2** Click **Shambhavi**. Confirm you're redirected to the dashboard and the header shows
      "Shambhavi" and `shambhavi@ajaia.com`.
- [ ] **1.3** Click **Sign out**. Confirm you're returned to the login page.
- [ ] **1.4** Log in as **Bob**, then **Sign out**, then log in as **Charlie** — confirm each
      switch works cleanly with no stale data from the previous user showing anywhere.
- [ ] **1.5** While logged in, manually reload the browser page (not navigate away). Confirm you
      stay logged in — the session should persist across a refresh.
- [ ] **1.6** Log out, then try navigating directly to a document URL
      (`http://localhost:5173/documents/<any-id>`). Confirm you're redirected to login, not shown
      a broken editor.

## 2. Dashboard

Log in as **Shambhavi** for this section.

- [ ] **2.1** Confirm the dashboard shows: welcome message with your name, **New Document**
      button, **Upload File** button, a search box, a **Recent documents** row, an **Owned
      Documents** section, and a **Shared With Me** section.
- [ ] **2.2** Confirm **Owned Documents** lists: Product Roadmap, Design Notes, Interview Notes,
      Weekly Journal — each tagged **Owner**.
- [ ] **2.3** Confirm **Shared With Me** lists: Engineering Onboarding Guide (tagged **Editor**,
      "by Bob") and Marketing Launch Plan (tagged **Viewer**, "by Charlie").
- [ ] **2.4** Type a partial title into the search box (e.g. "road"). Confirm the lists filter down
      to only matching documents, and clearing the search restores the full lists.
- [ ] **2.5** Search for something that matches nothing (e.g. "zzz"). Confirm you see an empty
      state with a helpful message, not a blank section.
- [ ] **2.6** Click **New Document**. Confirm a new "Untitled Document" is created and you're
      taken straight into its editor.
- [ ] **2.7** Go back to the dashboard and confirm the new document now appears under **Owned
      Documents**.

## 3. Rich-text editing — formatting

Open **Product Roadmap** (as Shambhavi, who owns it) for this section. For each control, select
some text first, click the toolbar button, and confirm the formatting visibly applies — then
click the same button again and confirm it toggles back off.

- [ ] **3.1** **Bold** — select a word, click **B**, confirm it renders bold; click again, confirm
      it un-bolds.
- [ ] **3.2** **Italic** — same as above with the *I* button.
- [ ] **3.3** **Underline** — same as above with the underline button.
- [ ] **3.4** **Combine formatting** — select a phrase and apply bold **and** italic **and**
      underline together. Confirm all three render at once on the same text.
- [ ] **3.5** **Heading 1** — place your cursor in a plain paragraph, click **H1**, confirm it
      becomes a large heading. Click again to toggle back to a normal paragraph.
- [ ] **3.6** **Heading 2** — same as above with **H2**, and confirm H1 and H2 render at visibly
      different sizes.
- [ ] **3.7** **Bullet list** — place your cursor on a line, click the bullet-list button, type a
      couple of items on separate lines (Enter between each), confirm each becomes its own bullet.
- [ ] **3.8** **Ordered list** — same as above with the ordered-list button, confirm items are
      numbered 1, 2, 3…
- [ ] **3.9** **Undo** — make a few edits, click Undo repeatedly, confirm each edit reverses one
      at a time.
- [ ] **3.10** **Redo** — after undoing, click Redo, confirm the undone edits reapply in order.
- [ ] **3.11** Add a new paragraph below the existing content and type your own sentence. Then
      select it and make it **both bold and italic at the same time**. Confirm it renders
      correctly (bold + slanted) and survives a page reload (see autosave section below for
      timing).

## 4. Autosave behavior

Still in an editable document (owned or editor-permission) as Shambhavi or Bob.

- [ ] **4.1** Type a change and watch the save-status indicator in the header. Confirm it shows
      **Saving…** shortly after you stop typing, then flips to **Saved** within a second or two.
- [ ] **4.2** Type several changes in quick succession (don't pause). Confirm you do **not** see
      a flurry of "Saving…/Saved" flickers for every keystroke — it should debounce into one save
      after you stop.
- [ ] **4.3** Make an edit, wait for **Saved**, then reload the page. Confirm your edit is still
      there (proves the save actually reached the backend, not just the UI state).
- [ ] **4.4** Click into the document **title** field, change it, then click elsewhere (blur the
      field). Confirm the title updates and the change survives a reload.
- [ ] **4.5** Try to blur the title field with it left empty or unchanged — confirm it doesn't
      send a pointless save or clear the title.
- [ ] **4.6 (optional, advanced)** Open the same document in two browser tabs as the owner in one
      and an editor in the other. Edit and save in tab A, then try to save a *different* edit from
      tab B without reloading it first. Confirm tab B shows a "changed elsewhere" / conflict state
      rather than silently overwriting tab A's save.

## 5. File import — valid files

From the dashboard, click **Upload File**. Use the files in [`test-files/`](test-files/).

- [ ] **5.1** Confirm the upload modal states the supported types (TXT, MD, DOCX) and the 2MB size
      limit before you upload anything.
- [ ] **5.2** Drag and drop `test-files/simple-notes.txt` onto the drop zone. Confirm an uploading
      state appears, then a success state, then you're taken into the newly created document.
- [ ] **5.3** Confirm the new document's title matches the filename (without the extension) and
      the paragraphs from the TXT file appear as separate paragraphs in the editor.
- [ ] **5.4** Go back to the dashboard, click **Upload File** again, and this time use the
      file-picker (click instead of drag) to upload `test-files/meeting-minutes.txt`. Confirm it
      works the same way as drag-and-drop.
- [ ] **5.5** Upload `test-files/project-brief.md`. Confirm the `#`/`##` lines became real
      headings (not plain text with literal `#` characters) and the bullet list rendered as an
      actual bullet list.
- [ ] **5.6** Upload `test-files/release-checklist.md`. Confirm both the bullet list and the
      numbered list imported as real lists.
- [ ] **5.7** Upload `test-files/quarterly-review.docx`. Confirm the Word headings (Heading 1 /
      Heading 2 styles) became real editor headings and the bullet-list items became a real
      bullet list, not plain paragraphs with no structure.
- [ ] **5.8** Confirm each imported document shows up under **Owned Documents** on the dashboard
      for whichever account did the import.

## 6. File import — rejected files

- [ ] **6.1** Try uploading `test-files/empty-file.txt`. Confirm you get a clear error message
      (not a silent failure or a generic crash) and no document is created.
- [ ] **6.2** Try uploading `test-files/unsupported-file.pdf`. Confirm it's rejected with a message
      about supported file types, and no document is created.
- [ ] **6.3** Try uploading `test-files/oversized-file.txt` (~2.6MB). Confirm it's rejected for
      exceeding the 2MB limit.
- [ ] **6.4** After each rejection, confirm the modal lets you try again (or close and reopen it)
      without needing to reload the whole page.

## 7. Sharing and permissions

Open **Product Roadmap** as **Shambhavi** (the owner).

- [ ] **7.1** Click **Share**. Confirm the modal shows the current collaborators: Bob as
      **Editor**, Charlie as **Viewer**.
- [ ] **7.2** Enter a new email that isn't a real account (e.g. `nobody@docspace.test`) and submit.
      Confirm you get a clear "no user found" style error, not a silent failure.
- [ ] **7.3** Try sharing the document with Bob **again** at a different permission level. Confirm
      you get a clear "already shared" error rather than creating a duplicate entry.
- [ ] **7.4** Click the revoke icon next to Charlie. Confirm Charlie disappears from the
      collaborator list immediately.
- [ ] **7.5** Log in as Charlie (switch accounts) and confirm Product Roadmap **no longer appears
      anywhere** on Charlie's dashboard, now that access was revoked.
- [ ] **7.6** Log back in as Shambhavi, reopen Share, and re-add Charlie as **Viewer** (so the
      demo data is back to its original state for future testing).
- [ ] **7.7** Confirm the **Share** button itself is only visible to the owner — log in as Bob and
      open Product Roadmap; confirm there is no Share button, only an indication of whose document
      it is.

## 8. Permission enforcement — Editor

Log in as **Bob**, open **Product Roadmap** (shared with him as Editor).

- [ ] **8.1** Confirm the full toolbar is visible and usable (bold, italic, underline, headings,
      lists, undo/redo all present).
- [ ] **8.2** Make an edit and confirm it autosaves (per section 4) — editors can save content.
- [ ] **8.3** Confirm Bob **cannot** rename the document (title field should be read-only or
      renaming should be blocked/ignored — only the owner can rename).
- [ ] **8.4** Confirm there is no **Share** button and no way to manage collaborators as Bob.
- [ ] **8.5** Confirm there is no **Delete** action available to Bob on this document.

## 9. Permission enforcement — Viewer (the most important section)

Log in as **Charlie**, open **Product Roadmap** (shared with him as Viewer).

- [ ] **9.1** Confirm the document opens showing all current content correctly.
- [ ] **9.2** Confirm there is **no toolbar at all** (no bold/italic/heading/list buttons visible).
- [ ] **9.3** Confirm a **"View only"** badge or equivalent indicator is visible somewhere in the
      header.
- [ ] **9.4** Click directly into the document body and try typing. Confirm **nothing happens** —
      no characters are inserted, no cursor appears to accept input. This should fail at the
      editor level, not just hide a button.
- [ ] **9.5** Try clicking into the title field and typing. Confirm it's disabled/read-only.
- [ ] **9.6** Confirm there is no Share button and no Delete option visible to Charlie.
- [ ] **9.7** Confirm no "Saving…" indicator ever appears for Charlie, since he can't trigger a
      save — and confirm no stray error banner appears on page load (there should be nothing to
      report an error about).

## 10. Document deletion (owner only)

- [ ] **10.1** As Shambhavi, create a throwaway document via **New Document**. On the dashboard,
      hover over its card and confirm a trash-can icon appears in the top-right corner.
- [ ] **10.2** Click the trash icon. Confirm a confirmation dialog appears naming the exact
      document and warning the action cannot be undone — nothing should delete without this step.
- [ ] **10.3** Click **Cancel**. Confirm the document is untouched and still on the dashboard.
- [ ] **10.4** Open the same throwaway document in the editor. Confirm a delete (trash) icon is
      visible in the header next to **Share**, since delete is owner-only like sharing.
- [ ] **10.5** Click it, confirm the dialog, click **Delete**. Confirm you're returned to the
      dashboard and the document is gone from **Owned Documents**.
- [ ] **10.6** Visit the deleted document's old URL directly
      (`http://localhost:5173/documents/<its-id>`). Confirm it no longer loads any content.
- [ ] **10.7** Log in as **Bob** (an editor, not owner, on Product Roadmap). Confirm Bob does
      **not** see a delete icon on that document's card or in its editor header — delete is
      owner-only, same as sharing.

## 11. Comments

Open any document you have at least read access to.

- [ ] **11.1** Click **Comments** in the header. Confirm a side panel opens showing existing
      comments (or "No comments yet" if there are none).
- [ ] **11.2** Type a comment and submit it. Confirm it appears immediately with your name as the
      author.
- [ ] **11.3** As the comment's author, confirm you see both a resolve (checkmark) icon and a
      delete (trash) icon next to your own comment.
- [ ] **11.4** Click resolve. Confirm the comment's text gets a strikethrough/faded style and the
      resolve icon disappears (already resolved), leaving only delete.
- [ ] **11.5** Log in as a different user who has access to the same document but did **not**
      write that comment (e.g. the document owner, if you commented as an editor). Confirm the
      owner can still resolve/delete the comment; confirm a third collaborator who is neither the
      author nor the owner sees no resolve/delete icons on someone else's comment.
- [ ] **11.6** Delete a comment. Confirm it disappears from the list immediately.
- [ ] **11.7** As a **Viewer**, confirm you can still open the Comments panel and add a comment
      (commenting doesn't require edit permission) — but confirm you cannot resolve or delete a
      comment you didn't write, unless you're the owner.

## 12. Version history

Open a document you can edit.

- [ ] **12.1** Make an edit and let it autosave (watch for "Saved"). Click **History** in the
      header. Confirm a new entry appears showing the version number, who saved it, and roughly
      when ("just now").
- [ ] **12.2** Click that entry. Confirm a read-only preview opens showing the document's content
      **before** your most recent edit (not the current content) — proving it's a true snapshot of
      the prior state, not just a copy of what's on screen now.
- [ ] **12.3** Confirm the preview is genuinely non-editable (no toolbar, typing does nothing) and
      clearly labeled read-only.
- [ ] **12.4** Click "Back" within the panel to return to the version list, then close the panel
      entirely with the X.
- [ ] **12.5** Make two more edits (two separate autosaves, a few seconds apart). Reopen History
      and confirm there are now multiple entries, most recent first, each with a different
      timestamp.
- [ ] **12.6** As a **Viewer**, confirm you can still open History and view past versions (history
      is read-access, not edit-access), even though you can't create new ones yourself.

## 13. Presence indicators

This needs two browser sessions (or two different browsers/profiles) logged in as two different
demo accounts who both have access to the same document — e.g. Shambhavi and Bob on "Product
Roadmap."

- [ ] **13.1** Open the same document as both Shambhavi and Bob at the same time. Within a few
      seconds, confirm each one sees a small avatar/initials chip in the other's header indicating
      someone else has it open (e.g. "Bob is also here").
- [ ] **13.2** Close the document in one session (navigate back to the dashboard, or close that
      tab). Within about 15 seconds, confirm the presence chip disappears for the remaining user —
      presence should clear out, not get stuck showing someone who already left.
- [ ] **13.3** Confirm presence only shows **other** people, never yourself, in your own chip list.

## 14. Export to PDF and Markdown

Open any document you have read access to.

- [ ] **14.1** Click **Export**. Confirm a small menu appears with "Export as PDF" and "Export as
      Markdown" options.
- [ ] **14.2** Click "Export as Markdown". Confirm a `.md` file downloads named after the
      document's title. Open it in a text editor and confirm headings show up as `#`/`##` and
      bullet points as `-` — real Markdown, not the raw TipTap JSON.
- [ ] **14.3** Click "Export as PDF". Confirm a `.pdf` file downloads and that it actually opens as
      a valid PDF (not a corrupted/empty file) with the document's headings and content visible.
- [ ] **14.4** As a **Viewer**, confirm you can still export a document you can only view — export
      only requires read access, same as comments and history.

## 15. Cross-cutting checks

- [ ] **15.1** Resize the browser window to a narrow/mobile width. Confirm the dashboard and
      editor remain usable (text doesn't overflow unreadably, buttons remain clickable).
- [ ] **15.2** Open the browser's developer console while clicking through sections 1–14. Confirm
      there are no unexpected red errors (expected: a single 401 on the very first unauthenticated
      load is normal and not a bug).
- [ ] **15.3** Confirm the overall visual style reads as a clean, modern productivity app (light
      background, green accent, clear spacing) rather than a generic admin dashboard.

---

Write down anything that fails, with the exact test number — that makes it fast to turn into a
fix afterward.
