package com.vinncorp.erp.modules.crm.controller;

import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.crm.entity.Pipeline;
import com.vinncorp.erp.modules.crm.entity.PipelineStage;
import com.vinncorp.erp.modules.crm.service.PipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crm/pipelines")
@RequiredArgsConstructor
@Tag(name = "CRM Pipelines")
public class PipelineController {

    private final PipelineService pipelineService;
    private final CurrentWorkspaceResolver workspaceResolver;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('PIPELINE_MANAGE')")
    @Operation(summary = "Create pipeline with stages")
    public ResponseEntity<Pipeline> create(@RequestBody Map<String, Object> body, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        Pipeline pipeline = new Pipeline();
        pipeline.setName((String) body.get("name"));
        pipeline.setDefault(Boolean.TRUE.equals(body.get("isDefault")));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stageMaps = (List<Map<String, Object>>) body.get("stages");
        List<PipelineStage> stages = stageMaps != null
                ? stageMaps.stream().map(m -> {
                    PipelineStage s = new PipelineStage();
                    s.setName((String) m.get("name"));
                    s.setProbabilityPct(m.get("probabilityPct") != null ? (Integer) m.get("probabilityPct") : 0);
                    s.setWon(Boolean.TRUE.equals(m.get("isWon")));
                    s.setLost(Boolean.TRUE.equals(m.get("isLost")));
                    return s;
                }).toList()
                : List.of();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pipelineService.create(pipeline, stages, wsId, auth.getName()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List pipelines")
    public ResponseEntity<List<Pipeline>> list() {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(pipelineService.list(wsId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get pipeline")
    public ResponseEntity<Pipeline> get(@PathVariable Long id) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(pipelineService.get(id, wsId));
    }

    @GetMapping("/{id}/stages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get pipeline stages")
    public ResponseEntity<List<PipelineStage>> getStages(@PathVariable Long id) {
        return ResponseEntity.ok(pipelineService.getStages(id));
    }

    @GetMapping("/default")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get default pipeline")
    public ResponseEntity<Pipeline> getDefault() {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(pipelineService.getDefault(wsId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('PIPELINE_MANAGE')")
    @Operation(summary = "Update pipeline")
    public ResponseEntity<Pipeline> update(@PathVariable Long id, @RequestBody Pipeline pipeline, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        return ResponseEntity.ok(pipelineService.update(id, pipeline, wsId, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CRM_MANAGER') or hasAuthority('PIPELINE_MANAGE')")
    @Operation(summary = "Delete pipeline")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        pipelineService.delete(id, wsId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
