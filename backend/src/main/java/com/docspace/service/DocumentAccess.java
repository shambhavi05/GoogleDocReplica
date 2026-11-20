package com.docspace.service;

import com.docspace.entity.Document;
import com.docspace.entity.Permission;

/**
 * Resolved access level for a given (user, document) pair.
 */
public record DocumentAccess(Document document, boolean owner, Permission permission) {

    public boolean canRead() {
        return owner || permission != null;
    }

    public boolean canEdit() {
        return owner || permission == Permission.EDITOR;
    }

    public boolean canManage() {
        return owner;
    }

    public String role() {
        if (owner) return "OWNER";
        return permission == null ? null : permission.name();
    }
}
