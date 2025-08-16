package com.tymbl.common.repository;

import com.tymbl.common.entity.SimilarContent;
import com.tymbl.common.entity.SimilarContent.ContentType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SimilarContentRepository extends JpaRepository<SimilarContent, Long> {

  List<SimilarContent> findByType(ContentType type);

  List<SimilarContent> findByParentNameAndType(String parentName, ContentType type);

  List<SimilarContent> findBySimilarNameAndType(String similarName, ContentType type);

  @Query("SELECT sc FROM SimilarContent sc WHERE sc.type = :type AND (sc.parentName LIKE %:searchTerm% OR sc.similarName LIKE %:searchTerm%)")
  List<SimilarContent> findByTypeAndSearchTerm(@Param("type") ContentType type, @Param("searchTerm") String searchTerm);

  @Query("SELECT sc FROM SimilarContent sc WHERE sc.type = :type AND sc.confidenceScore >= :minConfidence ORDER BY sc.confidenceScore DESC")
  List<SimilarContent> findByTypeAndMinConfidence(@Param("type") ContentType type, @Param("minConfidence") Double minConfidence);

  Optional<SimilarContent> findByParentNameAndSimilarNameAndType(String parentName, String similarName, ContentType type);

  boolean existsByParentNameAndSimilarNameAndType(String parentName, String similarName, ContentType type);
}
