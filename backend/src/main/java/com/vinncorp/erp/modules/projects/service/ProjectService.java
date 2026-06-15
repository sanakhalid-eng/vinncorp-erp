package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.request.CreateProjectRequest;
import com.vinncorp.erp.modules.projects.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request, String ownerEmail);

    List<ProjectResponse> getAllProjects();

    List<ProjectResponse> getProjectsForUser(String email);

    ProjectResponse getProjectById(Long id);

    ProjectResponse updateProject(Long id, CreateProjectRequest request);

    void deleteProject(Long id);
}



