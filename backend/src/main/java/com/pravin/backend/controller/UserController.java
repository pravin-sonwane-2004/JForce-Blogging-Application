package com.pravin.backend.controller;

import com.pravin.backend.dto.UserDto;
import com.pravin.backend.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    // Returns the currently logged-in user; used by the frontend on app load
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(HttpSession session) {
        return ResponseEntity.ok(UserDto.from(authService.requireLogin(session)));
    }
}