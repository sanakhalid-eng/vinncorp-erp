package com.vinncorp.erp.modules.crm.service;

import com.vinncorp.erp.modules.crm.entity.Contact;
import java.util.List;

public interface ContactService {
    Contact create(Contact contact, Long workspaceId, String actorEmail);
    Contact update(Long id, Contact contact, Long workspaceId, String actorEmail);
    Contact get(Long id, Long workspaceId);
    List<Contact> list(Long workspaceId);
    void delete(Long id, Long workspaceId, String actorEmail);
    List<Contact> search(Long workspaceId, String query);
}
