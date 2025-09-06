package com.tymbl.common.repository;

import com.tymbl.common.entity.Notification;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  // Count queries
  @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.createdAt >= :since")
  Long countByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("since") LocalDateTime since);

  @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.seen = false AND n.createdAt >= :since")
  Long countUnseenByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("since") LocalDateTime since);

  @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.seen = true AND n.createdAt >= :since")
  Long countSeenByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("since") LocalDateTime since);

  @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.clicked = true AND n.createdAt >= :since")
  Long countClickedByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("since") LocalDateTime since);

  // Count queries by type
  @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.type = :type AND n.createdAt >= :since")
  Long countByUserIdAndTypeAndCreatedAtAfter(@Param("userId") Long userId, @Param("type") Notification.NotificationType type, @Param("since") LocalDateTime since);

  @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.type = :type AND n.seen = false AND n.createdAt >= :since")
  Long countUnseenByUserIdAndTypeAndCreatedAtAfter(@Param("userId") Long userId, @Param("type") Notification.NotificationType type, @Param("since") LocalDateTime since);

  @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.type = :type AND n.seen = true AND n.createdAt >= :since")
  Long countSeenByUserIdAndTypeAndCreatedAtAfter(@Param("userId") Long userId, @Param("type") Notification.NotificationType type, @Param("since") LocalDateTime since);

  @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.type = :type AND n.clicked = true AND n.createdAt >= :since")
  Long countClickedByUserIdAndTypeAndCreatedAtAfter(@Param("userId") Long userId, @Param("type") Notification.NotificationType type, @Param("since") LocalDateTime since);

  // List queries with pagination
  @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.createdAt >= :since ORDER BY n.clicked ASC, n.seen ASC, n.createdAt DESC")
  Page<Notification> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
      @Param("userId") Long userId, @Param("since") LocalDateTime since, Pageable pageable);

  @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.type = :type AND n.createdAt >= :since ORDER BY n.createdAt DESC")
  Page<Notification> findByUserIdAndTypeAndCreatedAtAfterOrderByCreatedAtDesc(
      @Param("userId") Long userId, @Param("type") Notification.NotificationType type, 
      @Param("since") LocalDateTime since, Pageable pageable);

  // Top N per type queries
  @Query(value = "SELECT * FROM notifications n WHERE n.user_id = :userId AND n.type = :type AND n.created_at >= :since ORDER BY n.created_at DESC LIMIT :limit", nativeQuery = true)
  List<Notification> findTopNByUserIdAndTypeAndCreatedAtAfter(
      @Param("userId") Long userId, @Param("type") String type, 
      @Param("since") LocalDateTime since, @Param("limit") int limit);

  // Mark as seen/clicked
  @Modifying
  @Query("UPDATE Notification n SET n.seen = true, n.seenAt = :seenAt WHERE n.id = :notificationId AND n.userId = :userId")
  int markAsSeen(@Param("notificationId") Long notificationId, @Param("userId") Long userId, @Param("seenAt") LocalDateTime seenAt);

  @Modifying
  @Query("UPDATE Notification n SET n.seen = true, n.seenAt = :seenAt WHERE n.userId = :userId AND n.seen = false")
  int markAllAsSeen(@Param("userId") Long userId, @Param("seenAt") LocalDateTime seenAt);

  @Modifying
  @Query("UPDATE Notification n SET n.clicked = true, n.clickedAt = :clickedAt WHERE n.id = :notificationId AND n.userId = :userId")
  int markAsClicked(@Param("notificationId") Long notificationId, @Param("userId") Long userId, @Param("clickedAt") LocalDateTime clickedAt);

  // Check for existing notifications to avoid duplicates
  @Query("SELECT COUNT(n) > 0 FROM Notification n WHERE n.userId = :userId AND n.type = :type AND " +
         "(:relatedEntityId IS NULL AND n.relatedEntityId IS NULL OR n.relatedEntityId = :relatedEntityId) AND n.createdAt >= :since")
  boolean existsByUserIdAndTypeAndRelatedEntityIdAndCreatedAtAfter(
      @Param("userId") Long userId, @Param("type") Notification.NotificationType type, 
      @Param("relatedEntityId") Long relatedEntityId, @Param("since") LocalDateTime since);

  // Check for existing application status notifications by type+userId+companyId+designation
  @Query("SELECT COUNT(n) > 0 FROM Notification n WHERE n.userId = :userId AND n.type = :type AND " +
         "(:companyId IS NULL AND n.companyId IS NULL OR n.companyId = :companyId) AND " +
         "(:designation IS NULL AND n.designation IS NULL OR n.designation = :designation) AND n.createdAt >= :since")
  boolean existsByUserIdAndTypeAndCompanyIdAndDesignationAndCreatedAtAfter(
      @Param("userId") Long userId, @Param("type") Notification.NotificationType type, 
      @Param("companyId") Long companyId, @Param("designation") String designation, 
      @Param("since") LocalDateTime since);

  // Cleanup old notifications
  @Modifying
  @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
  int deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

  // Get notifications for specific entity types
  @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND " +
         "(:entityType IS NULL AND n.relatedEntityType IS NULL OR n.relatedEntityType = :entityType) AND n.createdAt >= :since ORDER BY n.createdAt DESC")
  List<Notification> findByUserIdAndRelatedEntityTypeAndCreatedAtAfter(
      @Param("userId") Long userId, @Param("entityType") String entityType, @Param("since") LocalDateTime since);
}
