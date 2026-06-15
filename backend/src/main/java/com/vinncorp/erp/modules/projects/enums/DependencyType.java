package com.vinncorp.erp.modules.projects.enums;

public enum DependencyType {
    BLOCKS,
    BLOCKED_BY,
    RELATES_TO,
    DUPLICATES,
    CAUSED_BY;

    public boolean isDirectional() {
        return this == BLOCKS || this == BLOCKED_BY || this == CAUSED_BY;
    }

    public boolean isSymmetric() {
        return this == RELATES_TO || this == DUPLICATES;
    }
}



