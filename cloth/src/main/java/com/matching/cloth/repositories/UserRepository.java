package com.matching.cloth.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matching.cloth.models.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    User findByEmail(String email);
}
