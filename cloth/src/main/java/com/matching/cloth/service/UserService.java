package com.matching.cloth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.matching.cloth.models.Admin;
import com.matching.cloth.models.User;
import com.matching.cloth.repositories.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminService adminService;

    private final String SALT = "fynn$#2";

    // Basic CRUD operations
    public User saveOrUpdateUser(User user) {
        return userRepository.save(user);
    }

    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Business logic methods

    /**
     * Check if username exists in both User and Admin tables
     */
    public boolean checkUsernameAvailability(String username) {
        return isUsernameExists(username) || adminService.isExistAdmin(username);
    }

    /**
     * Register new user with password hashing
     */
    public User registerUser(String username, String password, String email, String phone) throws Exception {
        String hashedPassword = PasswordUtil.getInstance().createPassword(password, SALT);

        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setEmail(email);
        user.setPhoneNumber(phone);

        return saveOrUpdateUser(user);
    }

    /**
     * Authenticate user login
     * Returns User if successful, null otherwise
     */
    public User authenticateUser(String usernameOrEmail, String password) throws Exception {
        // Try to find by username first
        Optional<User> userOpt = findByUsername(usernameOrEmail);
        User user = userOpt.orElse(null);

        // If not found by username, try email
        if (user == null) {
            user = findByEmail(usernameOrEmail);
        }

        // Validate password
        if (user != null) {
            String hashedPassword = PasswordUtil.getInstance().createPassword(password, SALT);
            if (user.getPassword().equals(hashedPassword)) {
                return user;
            }
        }

        return null;
    }

    /**
     * Authenticate admin login
     * Returns Admin if successful, null otherwise
     */
    public Admin authenticateAdmin(String username, String password) {
        Admin admin = adminService.findByUsername(username);
        if (admin != null && admin.getAdminPassword().equals(password)) {
            return admin;
        }
        return null;
    }

    /**
     * Create remember me cookie value
     */
    public String createRememberMeCookie(String username, String password) throws Exception {
        return PasswordUtil.getInstance().createRememberMeCookie(username, password);
    }
}
