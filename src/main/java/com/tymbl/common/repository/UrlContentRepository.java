package com.tymbl.common.repository;

import com.tymbl.common.entity.UrlContent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlContentRepository extends JpaRepository<UrlContent, Long> {

  Optional<UrlContent> findByUrl(String url);

  boolean existsByUrl(String url);
} 