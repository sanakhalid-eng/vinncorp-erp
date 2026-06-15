package com.vinncorp.erp.shared.security;

import com.vinncorp.erp.core.user.repository.RoleRepository;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.modules.projects.repository.ProjectMemberRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.core.user.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class ProjectSecurity {

    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final RoleRepository roleRepository;

    public ProjectSecurity(ProjectMemberRepository projectMemberRepository,
                           UserRepository userRepository,
                           ProjectRepository projectRepository,
                           RoleRepository roleRepository) {
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.roleRepository = roleRepository;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    private boolean isOwner(Long projectId, Long userId) {
        return projectRepository.findById(projectId)
                .map(project -> project.getOwner().getId().equals(userId))
                .orElse(false);
    }

    public boolean isProjectMember(Long projectId, String email) {
        User user = getUser(email);
        if (user == null) return false;

        // OWNER is also considered MEMBER
        if (isOwner(projectId, user.getId())) return true;

        return projectMemberRepository
                .existsByProject_IdAndUser_Id(projectId, user.getId());
    }

    public boolean isProjectManager(Long projectId, String email) {
        User user = getUser(email);
        if (user == null) return false;

        return projectMemberRepository
                    .existsByProject_IdAndUser_Id(projectId, user.getId());
    }

    public boolean isProjectOwner(Long projectId, String email) {
        User user = getUser(email);
        if (user == null) return false;

        return isOwner(projectId, user.getId());
    }

    public boolean isManagerOrOwner(Long projectId, String email) {
        User user = getUser(email);
        if (user == null) return false;

        return isOwner(projectId, user.getId()) ||
                projectMemberRepository. existsByProject_IdAndUser_Id(projectId, user.getId());
    }

    public boolean canAccessProject(Long projectId, String email) {
        return isProjectMember(projectId, email);
    }
}

