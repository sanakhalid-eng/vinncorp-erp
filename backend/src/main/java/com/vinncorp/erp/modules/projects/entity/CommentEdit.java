package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.core.user.entity.User;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment_edits")
@Data
public class CommentEdit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Column(name = "old_content", columnDefinition = "TEXT", nullable = false)
    private String oldContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edited_by", nullable = false)
    private User editedBy;

    @CreationTimestamp
    @Column(name = "edited_at", nullable = false, updatable = false)
    private LocalDateTime editedAt;
}



