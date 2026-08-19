package com.pravin.backend.service;

import com.pravin.backend.dto.LoginRequest;
import com.pravin.backend.dto.RegisterRequest;
import com.pravin.backend.dto.UserDto;
import com.pravin.backend.entity.User;
import com.pravin.backend.enums.Role;
import com.pravin.backend.exception.ApiException;
import com.pravin.backend.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto register(RegisterRequest req) {
        String username = req.getUsername().trim();
        String email = req.getEmail().trim();

        if (userRepository.existsByUsername(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "Username is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(Role.USER); // everyone starts as a normal user
        return UserDto.from(userRepository.save(user));
    }

    public UserDto login(LoginRequest req, HttpSession session) {
        User user = userRepository.findByUsername(req.getUsername().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        session.setAttribute("userId", user.getId());
        return UserDto.from(user);
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    public User currentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }

    public User requireLogin(HttpSession session) {
        User user = currentUser(session);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Please log in");
        }
        return user;
    }

    public User requireAdmin(HttpSession session) {
        User user = requireLogin(session);
        if (user.getRole() != Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return user;
    }
}