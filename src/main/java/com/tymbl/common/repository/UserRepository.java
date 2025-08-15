package com.tymbl.common.repository;

import com.tymbl.common.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  Optional<User> findByEmailVerificationToken(String token);

  Optional<User> findByPasswordResetToken(String token);

  boolean existsByEmail(String email);
} 