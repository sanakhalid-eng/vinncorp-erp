package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {

    @Query("SELECT l FROM Label l WHERE l.project.id = :projectId AND l.deletedAt IS NULL ORDER BY l.createdAt ASC")
    List<Label> findByProjectIdActive(@Param("projectId") Long projectId);

    @Query("SELECT l FROM Label l WHERE l.name = :name AND l.project.id = :projectId AND l.deletedAt IS NULL")
    Optional<Label> findByNameAndProjectIdActive(@Param("name") String name, @Param("projectId") Long projectId);

    @Query("SELECT COUNT(l) > 0 FROM Label l WHERE l.name = :name AND l.project.id = :projectId AND l.deletedAt IS NULL")
    boolean existsByNameAndProjectIdActive(@Param("name") String name, @Param("projectId") Long projectId);

    @Query("SELECT l FROM Label l WHERE l.id IN :ids AND l.project.id = :projectId AND l.deletedAt IS NULL")
    List<Label> findByIdInAndProjectIdActive(@Param("ids") List<Long> ids, @Param("projectId") Long projectId);

    @Query("SELECT COUNT(tl) FROM TaskLabel tl JOIN tl.label l WHERE tl.label.id = :labelId AND l.deletedAt IS NULL")
    long countActiveUsageByLabelId(@Param("labelId") Long labelId);

    @Query("SELECT COUNT(tl) FROM TaskLabel tl WHERE tl.label.id = :labelId")
    long countAllUsageByLabelId(@Param("labelId") Long labelId);
}



