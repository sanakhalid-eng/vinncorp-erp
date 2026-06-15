package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.request.CreateRecurringRequest;
import com.vinncorp.erp.modules.projects.dto.request.UpdateRecurringRequest;
import com.vinncorp.erp.modules.projects.dto.response.RecurringOccurrenceResponse;
import com.vinncorp.erp.modules.projects.dto.response.RecurringTemplateResponse;

import java.util.List;

public interface RecurringTaskService {

    RecurringTemplateResponse createRecurring(Long taskId, CreateRecurringRequest request, String email);

    RecurringTemplateResponse updateRecurring(Long templateId, UpdateRecurringRequest request, String email);

    RecurringTemplateResponse getRecurringTemplate(Long templateId);

    List<RecurringTemplateResponse> getTemplatesByProject(Long projectId);

    RecurringTemplateResponse pauseRecurring(Long templateId, String email);

    RecurringTemplateResponse resumeRecurring(Long templateId, String email);

    void stopRecurring(Long templateId, String email);

    List<RecurringOccurrenceResponse> getOccurrences(Long templateId);

    void generateNextOccurrences();

    void evictCache(Long templateId);
}



