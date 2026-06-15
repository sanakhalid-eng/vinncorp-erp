
package com.vinncorp.erp.modules.projects.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTemplate {
    private String id;
    private String name;
    private String description;
    private String category;
    private String icon;
    private List<String> defaultLabels;
    private List<String> defaultColumns;
    private boolean hasSprints;
}



