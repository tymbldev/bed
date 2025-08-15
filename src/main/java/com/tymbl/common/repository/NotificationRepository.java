package com.tymbl.common.repository;

import com.tymbl.common.entity.Notification;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  // Find notifications for a specific user
  List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

  // Find notifications for a user in the last N days
  @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.createdAt >= :since ORDER BY n.createdAt DESC")
  List<Notification> findByUserIdAndCreatedAtSince(@Param("userId") Long userId,
      @Param("since") LocalDateTime since);

  // Find unread notifications for a user
  List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

  // Find unsent notifications
  List<Notification> findByIsSentFalseOrderByCreatedAtAsc();

  // Find notifications by type
  List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId,
      Notification.NotificationType type);

  // Count unread notifications for a user
  long countByUserIdAndIsReadFalse(Long userId);

  // Find notifications with pagination
  Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  // Find notifications for a user in the last 7 days
  @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.createdAt >= :sevenDaysAgo ORDER BY n.createdAt DESC")
  List<Notification> findRecentNotificationsByUserId(@Param("userId") Long userId,
      @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

  // Find notifications by related entity
  List<Notification> findByRelatedEntityIdAndRelatedEntityTypeOrderByCreatedAtDesc(
      Long relatedEntityId, Notification.RelatedEntityType relatedEntityType);

  // Find notifications that need to be sent (unsent and older than 1 minute)
  @Query("SELECT n FROM Notification n WHERE n.isSent = false AND n.createdAt <= :cutoffTime ORDER BY n.createdAt ASC")
  List<Notification> findPendingNotifications(@Param("cutoffTime") LocalDateTime cutoffTime);
} 