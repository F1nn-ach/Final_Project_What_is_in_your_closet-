package com.matching.cloth.repositories;

import com.matching.cloth.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {
    boolean existsByAdminUsername(String adminUsername);
    Admin findByAdminUsername(String username);
}
