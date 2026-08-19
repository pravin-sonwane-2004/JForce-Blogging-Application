package com.pravin.backend.controller;

import com.pravin.backend.dto.PostDto;
import com.pravin.backend.dto.ReportData;
import com.pravin.backend.dto.UserDto;
import com.pravin.backend.enums.PostStatus;
import com.pravin.backend.enums.Role;
import com.pravin.backend.entity.User;
import com.pravin.backend.service.AuthService;
import com.pravin.backend.service.PostService;
import com.pravin.backend.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AuthService authService;
    private final PostService postService;
    private final UserService userService;

    public AdminController(AuthService authService, PostService postService, UserService userService) {
        this.authService = authService;
        this.postService = postService;
        this.userService = userService;
    }

    // Every admin endpoint starts with requireAdmin(session) -> enforced on the backend

    @GetMapping("/posts")
    public Page<PostDto> allPosts(@RequestParam(required = false) PostStatus status,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  HttpSession session) {
        authService.requireAdmin(session);
        return postService.all(status, page, size);
    }

    @PutMapping("/posts/{id}/status")
    public PostDto changeStatus(@PathVariable Long id, @RequestParam PostStatus status, HttpSession session) {
        authService.requireAdmin(session);
        return postService.changeStatus(id, status);
    }

    @PutMapping("/posts/{id}/feature")
    public PostDto toggleFeatured(@PathVariable Long id, HttpSession session) {
        authService.requireAdmin(session);
        return postService.toggleFeatured(id);
    }

    @GetMapping("/users")
    public List<UserDto> users(HttpSession session) {
        authService.requireAdmin(session);
        return userService.allUsers();
    }

    @PutMapping("/users/{id}")
    public UserDto updateUser(@PathVariable Long id, @RequestParam Role role, HttpSession session) {
        authService.requireAdmin(session);
        return userService.updateRole(id, role);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, HttpSession session) {
        User admin = authService.requireAdmin(session);
        userService.deleteUser(id, admin);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reports")
    public ReportData reports(HttpSession session) {
        authService.requireAdmin(session);
        return postService.reports();
    }
}