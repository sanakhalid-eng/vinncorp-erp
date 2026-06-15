package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.Notification;
import com.vinncorp.erp.modules.projects.enums.NotificationCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(Long userId);

    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    List<Notification> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByEventId(String eventId);

    boolean existsByEntityIdAndTypeAndUserIdAndCreatedAtAfter(Long entityId, String type, Long userId, LocalDateTime since);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id AND n.user.id = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false AND n.type = :type")
    List<Notification> findUnreadByType(@Param("userId") Long userId, @Param("type") String type);

    Page<Notification> findByUserIdAndCategoryOrderByCreatedAtDesc(Long userId, NotificationCategory category, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND (:category IS NULL OR n.category = :category) AND (:type IS NULL OR n.type = :type) ORDER BY n.createdAt DESC")
    Page<Notification> findByFilters(@Param("userId") Long userId, @Param("category") NotificationCategory category, @Param("type") String type, Pageable pageable);

    @Query("SELECT n.category, COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false GROUP BY n.category")
    List<Object[]> countUnreadByCategory(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.workspaceId = :workspaceId AND n.isRead = false")
    long countByUserIdAndWorkspaceIdAndIsReadFalse(@Param("userId") Long userId, @Param("workspaceId") Long workspaceId);

    Page<Notification> findByUserIdAndWorkspaceIdOrderByCreatedAtDesc(Long userId, Long workspaceId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.type IN ('DUE_SOON', 'DUE_OVERDUE') AND n.createdAt > :since")
    List<Notification> findRecentDueNotifications(@Param("since") LocalDateTime since);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :now")
    int deleteExpiredNotifications(@Param("now") LocalDateTime now);
}



