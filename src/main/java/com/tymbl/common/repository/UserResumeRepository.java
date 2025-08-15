package com.tymbl.common.repository;

import com.tymbl.common.entity.UserResume;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserResumeRepository extends JpaRepository<UserResume, Long> {

  List<UserResume> findByUserId(Long userId);

  UserResume findFirstByUserIdOrderByCreatedAtDesc(Long userId);

  Optional<UserResume> findByUuid(String uuid);

  UserResume findFirstByUserIdAndUuidIsNotNullOrderByCreatedAtDesc(Long userId);
} 