package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.user.entity.UserSummary;
import com.vinncorp.erp.modules.projects.dto.request.CommentRequest;
import com.vinncorp.erp.modules.projects.dto.request.ReactionRequest;
import com.vinncorp.erp.modules.projects.dto.response.CommentResponse;
import com.vinncorp.erp.modules.projects.dto.response.EditHistoryResponse;
import com.vinncorp.erp.modules.projects.entity.*;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.modules.projects.event.DomainEvent;
import com.vinncorp.erp.modules.projects.event.EventPublisher;
import com.vinncorp.erp.modules.projects.repository.*;
import com.vinncorp.erp.modules.projects.service.ActivityLogService;
import com.vinncorp.erp.modules.projects.service.CommentService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ForbiddenOperationException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final int MAX_DEPTH = 2;
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([\\w.@\\-]+)");

    private final CommentRepository commentRepository;
    private final CommentEditRepository commentEditRepository;
    private final CommentReactionRepository commentReactionRepository;
    private final CommentMentionRepository commentMentionRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ActivityLogService activityLogService;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public CommentResponse createComment(Long taskId, CommentRequest request, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long projectId = task.getProject().getId();
        if (!projectMemberRepository.existsByProject_IdAndUser_Id(projectId, author.getId())) {
            throw new ForbiddenOperationException("You must be a project member to comment");
        }

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(author);
        comment.setContent(request.getContent());

        if (request.getParentCommentId() != null) {
            Comment parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));

            if (!parent.getTask().getId().equals(taskId)) {
                throw new BadRequestException("Parent comment must belong to the same task");
            }

            if (parent.isDeleted()) {
                throw new BadRequestException("Cannot reply to a deleted comment");
            }

            if (getDepth(parent) >= MAX_DEPTH) {
                throw new BadRequestException("Maximum reply depth reached");
            }

            comment.setParentComment(parent);
        }

        Comment saved = commentRepository.save(comment);

        resolveAndSaveMentions(saved, request.getContent(), projectId);

        activityLogService.logActivity(
                author.getId(),
                EntityType.COMMENT,
                saved.getId(),
                ActionType.COMMENT_ADDED,
                null,
                Map.of("content", truncate(saved.getContent(), 100)),
                "Comment added to task: " + task.getTitle(),
                projectId
        );

        if (task.getAssignee() != null && !task.getAssignee().getId().equals(author.getId())) {
            eventPublisher.publish(DomainEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .type(DomainEvent.Type.COMMENT_CREATED)
                    .actorId(author.getId())
                    .targetUserId(task.getAssignee().getId())
                    .entityType("COMMENT")
                    .entityId(saved.getId())
                    .projectId(projectId)
                    .projectName(task.getProject().getName())
                    .message(author.getName() + " commented on task: " + truncate(task.getTitle(), 40))
                    .metadata(Map.of("commentContent", truncate(saved.getContent(), 100)))
                    .build());
        }

        if (task.getCreator() != null
                && !task.getCreator().getId().equals(author.getId())
                && (task.getAssignee() == null || !task.getCreator().getId().equals(task.getAssignee().getId()))) {
            eventPublisher.publish(DomainEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .type(DomainEvent.Type.COMMENT_CREATED)
                    .actorId(author.getId())
                    .targetUserId(task.getCreator().getId())
                    .entityType("COMMENT")
                    .entityId(saved.getId())
                    .projectId(projectId)
                    .projectName(task.getProject().getName())
                    .message(author.getName() + " commented on your task: " + truncate(task.getTitle(), 40))
                    .metadata(Map.of("commentContent", truncate(saved.getContent(), 100)))
                    .build());
        }

        return buildCommentResponse(saved, Collections.emptyList(), Collections.emptyList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTaskId(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found");
        }

        List<Comment> allComments = commentRepository.findAllByTaskIdOrdered(taskId);

        Map<Long, List<Comment>> childrenMap = allComments.stream()
                .filter(c -> c.getParentComment() != null)
                .collect(Collectors.groupingBy(c -> c.getParentComment().getId()));

        List<Comment> rootComments = allComments.stream()
                .filter(c -> c.getParentComment() == null)
                .collect(Collectors.toList());

        Map<Long, List<CommentReaction>> reactionsMap = allComments.stream()
                .collect(Collectors.toMap(
                        Comment::getId,
                        c -> new ArrayList<>(c.getReactions()),
                        (a, b) -> { a.addAll(b); return a; }
                ));

        Map<Long, List<CommentMention>> mentionsMap = allComments.stream()
                .collect(Collectors.toMap(
                        Comment::getId,
                        c -> new ArrayList<>(c.getMentions()),
                        (a, b) -> { a.addAll(b); return a; }
                ));

        return rootComments.stream()
                .map(c -> buildTreeResponse(c, childrenMap, reactionsMap, mentionsMap))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request, String email) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isCommentAuthor = comment.getUser().getId().equals(user.getId());
        boolean isTaskCreator = comment.getTask().getCreator() != null
                && comment.getTask().getCreator().getId().equals(user.getId());

        if (!isCommentAuthor && !isTaskCreator) {
            throw new ForbiddenOperationException("You can only edit your own comments");
        }

        if (comment.isDeleted()) {
            throw new BadRequestException("Cannot edit a deleted comment");
        }

        CommentEdit edit = new CommentEdit();
        edit.setComment(comment);
        edit.setOldContent(comment.getContent());
        edit.setEditedBy(user);
        commentEditRepository.save(edit);

        comment.setContent(request.getContent());
        comment.setEdited(true);
        Comment updated = commentRepository.save(comment);

        resolveAndSaveMentions(updated, request.getContent(), updated.getTask().getProject().getId());

        return buildCommentResponse(updated, Collections.emptyList(), Collections.emptyList());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String email) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isCommentAuthor = comment.getUser().getId().equals(user.getId());
        boolean isTaskCreator = comment.getTask().getCreator() != null
                && comment.getTask().getCreator().getId().equals(user.getId());
        boolean isAdmin = user.getRoles().contains("ADMIN");
        boolean isProjectManager = comment.getTask().getProject().getProjectManager() != null
                && comment.getTask().getProject().getProjectManager().getId().equals(user.getId());

        if (!isCommentAuthor && !isTaskCreator && !isAdmin && !isProjectManager) {
            throw new ForbiddenOperationException("You do not have permission to delete this comment");
        }

        comment.softDelete();
        commentRepository.save(comment);
    }


    @Override
    @Transactional
    public CommentResponse toggleReaction(Long commentId, ReactionRequest request, String email) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<CommentReaction> existing = commentReactionRepository.findByCommentIdAndUserIdAndReactionType(
                commentId, user.getId(), request.getReactionType());

        if (existing.isPresent()) {
            commentReactionRepository.delete(existing.get());
        } else {
            CommentReaction reaction = new CommentReaction();
            reaction.setComment(comment);
            reaction.setUser(user);
            reaction.setReactionType(request.getReactionType());
            commentReactionRepository.save(reaction);
        }

        return buildCommentResponse(comment, Collections.emptyList(), Collections.emptyList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EditHistoryResponse> getEditHistory(Long commentId, String email) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isCommentAuthor = comment.getUser().getId().equals(user.getId());
        boolean isTaskCreator = comment.getTask().getCreator() != null
                && comment.getTask().getCreator().getId().equals(user.getId());

        if (!isCommentAuthor && !isTaskCreator && !user.getRoles().contains("ADMIN")) {
            throw new ForbiddenOperationException("You do not have permission to view this comment's edit history");
        }

        List<CommentEdit> edits = commentEditRepository.findByCommentIdOrderByEditedAtAsc(commentId);

        return edits.stream()
                .map(this::toEditHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public int getCommentCountForTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found");
        }
        return commentRepository.findAllByTaskIdOrdered(taskId).size();
    }

    private CommentResponse buildTreeResponse(
            Comment comment,
            Map<Long, List<Comment>> childrenMap,
            Map<Long, List<CommentReaction>> reactionsMap,
            Map<Long, List<CommentMention>> mentionsMap
    ) {
        CommentResponse response = buildCommentResponse(
                comment,
                reactionsMap.getOrDefault(comment.getId(), Collections.emptyList()),
                mentionsMap.getOrDefault(comment.getId(), Collections.emptyList())
        );

        List<Comment> children = childrenMap.getOrDefault(comment.getId(), Collections.emptyList());
        List<CommentResponse> childResponses = children.stream()
                .map(child -> buildTreeResponse(child, childrenMap, reactionsMap, mentionsMap))
                .collect(Collectors.toList());
        response.setReplies(childResponses);

        return response;
    }

    private CommentResponse buildCommentResponse(
            Comment comment,
            List<CommentReaction> reactions,
            List<CommentMention> mentions
    ) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.isDeleted() ? null : comment.getContent());
        response.setEdited(comment.isEdited());
        response.setDeleted(comment.isDeleted());
        response.setParentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null);
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        response.setReplies(new ArrayList<>());

        if (comment.getUser() != null) {
            UserSummary author = new UserSummary();
            author.setId(comment.getUser().getId());
            author.setName(comment.getUser().getName());
            author.setEmail(comment.getUser().getEmail());
            author.setAvatarUrl(comment.getUser().getAvatarUrl());
            response.setAuthor(author);
        }

        if (reactions != null && !reactions.isEmpty()) {
            Map<String, Long> reactionCounts = reactions.stream()
                    .collect(Collectors.groupingBy(
                            CommentReaction::getReactionType,
                            Collectors.counting()
                    ));

            Map<String, List<String>> reactionUsers = reactions.stream()
                    .collect(Collectors.groupingBy(
                            CommentReaction::getReactionType,
                            Collectors.mapping(r -> r.getUser().getName(), Collectors.toList())
                    ));

            List<Map<String, Object>> reactionList = new ArrayList<>();
            for (Map.Entry<String, Long> entry : reactionCounts.entrySet()) {
                Map<String, Object> reactionMap = new LinkedHashMap<>();
                reactionMap.put("type", entry.getKey());
                reactionMap.put("count", entry.getValue());
                reactionMap.put("users", reactionUsers.getOrDefault(entry.getKey(), Collections.emptyList()));
                reactionList.add(reactionMap);
            }
            response.setReactions(reactionList);
        } else {
            response.setReactions(new ArrayList<>());
        }

        if (mentions != null && !mentions.isEmpty()) {
            List<UserSummary> mentionUsers = mentions.stream()
                    .map(mention -> {
                        UserSummary u = new UserSummary();
                        u.setId(mention.getMentionedUser().getId());
                        u.setName(mention.getMentionedUser().getName());
                        u.setEmail(mention.getMentionedUser().getEmail());
                        return u;
                    })
                    .collect(Collectors.toList());
            response.setMentions(mentionUsers);
        } else {
            response.setMentions(new ArrayList<>());
        }

        return response;
    }

    private void resolveAndSaveMentions(Comment comment, String content, Long projectId) {
        commentMentionRepository.deleteByCommentId(comment.getId());

        Matcher matcher = MENTION_PATTERN.matcher(content);
        Set<String> mentionedNames = new HashSet<>();

        while (matcher.find()) {
            mentionedNames.add(matcher.group(1));
        }

        if (mentionedNames.isEmpty()) {
            return;
        }

        List<User> projectMembers = projectMemberRepository.findByProject_Id(projectId)
                .stream()
                .map(ProjectMember::getUser)
                .collect(Collectors.toList());

        for (String name : mentionedNames) {
            User matched = projectMembers.stream()
                    .filter(u -> u.getName() != null && u.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElseGet(() -> projectMembers.stream()
                            .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(name))
                            .findFirst()
                            .orElse(null));

            if (matched != null) {
                CommentMention mention = new CommentMention();
                mention.setComment(comment);
                mention.setMentionedUser(matched);
                commentMentionRepository.save(mention);

                if (!matched.getId().equals(comment.getUser().getId())) {
                    eventPublisher.publish(DomainEvent.builder()
                            .eventId(UUID.randomUUID().toString())
                            .type(DomainEvent.Type.COMMENT_MENTIONED)
                            .actorId(comment.getUser().getId())
                            .targetUserId(matched.getId())
                            .entityType("COMMENT")
                            .entityId(comment.getId())
                            .projectId(projectId)
                            .projectName(comment.getTask().getProject() != null ? comment.getTask().getProject().getName() : null)
                            .message(comment.getUser().getName() + " mentioned you in a comment: " + truncate(comment.getContent(), 80))
                            .metadata(Map.of("commentContent", truncate(comment.getContent(), 80)))
                            .build());
                }
            }
        }
    }

    private int getDepth(Comment comment) {
        int depth = 0;
        Comment current = comment;
        while (current.getParentComment() != null) {
            depth++;
            current = current.getParentComment();
            if (depth >= MAX_DEPTH) {
                break;
            }
        }
        return depth;
    }

    private EditHistoryResponse toEditHistoryResponse(CommentEdit edit) {
        EditHistoryResponse response = new EditHistoryResponse();
        response.setId(edit.getId());
        response.setOldContent(edit.getOldContent());
        response.setEditedAt(edit.getEditedAt());

        if (edit.getEditedBy() != null) {
            UserSummary editor = new UserSummary();
            editor.setId(edit.getEditedBy().getId());
            editor.setName(edit.getEditedBy().getName());
            editor.setEmail(edit.getEditedBy().getEmail());
            response.setEditedBy(editor);
        }

        return response;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}



