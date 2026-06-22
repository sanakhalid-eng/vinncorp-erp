package com.vinncorp.erp.modules.hr.service.impl;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.modules.hr.entity.Designation;
import com.vinncorp.erp.modules.hr.repository.DesignationRepository;
import com.vinncorp.erp.modules.hr.dto.request.DesignationCreateRequest;
import com.vinncorp.erp.modules.hr.service.DesignationService;
import com.vinncorp.erp.modules.hr.dto.response.DesignationResponse;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ConflictException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DesignationResponse create(DesignationCreateRequest req, Long workspaceId, String actorEmail) {
        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new BadRequestException("title is required");
        }
        if (designationRepository.existsByTitleAndWorkspaceId(req.getTitle(), workspaceId)) {
            throw new ConflictException("Designation title already exists in this workspace");
        }
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Actor user not found: " + actorEmail));

        Designation d = new Designation();
        d.setTitle(req.getTitle());
        d.setCode(req.getCode());
        d.setDescription(req.getDescription());
        d.setLevel(req.getLevel() == null ? 0 : req.getLevel());
        d.setActive(req.getActive() == null ? true : req.getActive());
        d.setWorkspace(workspace);
        d.setCreatedBy(actor.getId());
        d.setUpdatedBy(actor.getId());

        return DesignationResponse.from(designationRepository.save(d));
    }

    @Override
    @Transactional
    public DesignationResponse update(Long id, DesignationCreateRequest req, Long workspaceId, String actorEmail) {
        Designation d = designationRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Actor user not found: " + actorEmail));

        if (req.getTitle() != null && !req.getTitle().equals(d.getTitle())) {
            if (designationRepository.existsByTitleAndWorkspaceId(req.getTitle(), workspaceId)) {
                throw new ConflictException("Designation title already exists in this workspace");
            }
            d.setTitle(req.getTitle());
        }
        if (req.getCode() != null) d.setCode(req.getCode());
        if (req.getDescription() != null) d.setDescription(req.getDescription());
        if (req.getLevel() != null) d.setLevel(req.getLevel());
        if (req.getActive() != null) d.setActive(req.getActive());

        d.setUpdatedBy(actor.getId());
        return DesignationResponse.from(designationRepository.save(d));
    }

    @Override
    @Transactional(readOnly = true)
    public DesignationResponse get(Long id, Long workspaceId) {
        Designation d = designationRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found: " + id));
        return DesignationResponse.from(d);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DesignationResponse> list(Long workspaceId, boolean activeOnly) {
        List<Designation> rows = activeOnly
                ? designationRepository.findAllByWorkspaceIdAndActiveTrue(workspaceId)
                : designationRepository.findAllByWorkspaceId(workspaceId);
        return rows.stream().map(DesignationResponse::from).toList();
    }

    @Override
    @Transactional
    public void delete(Long id, Long workspaceId, String actorEmail) {
        Designation d = designationRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Actor user not found: " + actorEmail));
        d.softDelete(actor.getId());
        d.setUpdatedBy(actor.getId());
        designationRepository.save(d);
    }
}


