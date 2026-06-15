package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.request.LabelRequest;
import com.vinncorp.erp.modules.projects.dto.request.TaskLabelAssignmentRequest;
import com.vinncorp.erp.modules.projects.dto.response.LabelResponse;

import java.util.List;

public interface LabelService {

    LabelResponse createLabel(Long projectId, LabelRequest request, String email);

    List<LabelResponse> getLabelsByProject(Long projectId);

    void deleteLabel(Long labelId, String email);

    List<LabelResponse> assignLabelsToTask(Long taskId, TaskLabelAssignmentRequest request, String email);

    void removeLabelFromTask(Long taskId, Long labelId, String email);

    int removeLabelsFromTask(Long taskId, List<Long> labelIds, String email);

    List<LabelResponse> getLabelsForTask(Long taskId);
}



