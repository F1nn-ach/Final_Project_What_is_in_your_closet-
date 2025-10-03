package com.matching.cloth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.matching.cloth.models.Admin;
import com.matching.cloth.repositories.AdminRepository;

@Service
public class AdminService {
    @Autowired
    private AdminRepository adminRepository;

    public boolean isExistAdmin(String adminUsername) {
        return adminRepository.existsByAdminUsername(adminUsername);
    }

    public Admin findByUsername(String username) {
        return adminRepository.findByAdminUsername(username);
    }
}
