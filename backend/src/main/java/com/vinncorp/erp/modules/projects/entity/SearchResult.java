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
public class SearchResult {
    private List<SearchHit> tasks;
    private List<SearchHit> projects;
    private List<SearchHit> members;
    private List<SearchHit> comments;
    private List<SearchHit> employees;
    private List<SearchHit> departments;
    private List<SearchHit> workspaces;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchHit {
        private Long id;
        private String type;
        private String title;
        private String subtitle;
        private String url;
        private String avatar;
        private String badge;
    }
}



