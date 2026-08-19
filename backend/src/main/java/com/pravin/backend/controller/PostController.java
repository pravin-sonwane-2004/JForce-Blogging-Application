package com.pravin.backend.controller;

import com.pravin.backend.dto.PostDto;
import com.pravin.backend.dto.PostRequest;
import com.pravin.backend.entity.User;
import com.pravin.backend.service.AuthService;
import com.pravin.backend.service.PostService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final AuthService authService;

    public PostController(PostService postService, AuthService authService) {
        this.postService = postService;
        this.authService = authService;
    }

    // Public feed of approved posts
    @GetMapping
    public Page<PostDto> feed(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "6") int size,
                              @RequestParam(required = false) String q,
                              @RequestParam(defaultValue = "newest") String sort) {
        return postService.feed(q, sort, page, size);
    }

    @GetMapping("/{id}")
    public PostDto getById(@PathVariable Long id, HttpSession session) {
        return postService.getById(id, authService.currentUser(session));
    }

    // Posts written by the current user (any status) - Profile page
    @GetMapping("/mine")
    public Page<PostDto> mine(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              HttpSession session) {
        User user = authService.requireLogin(session);
        return postService.myPosts(user, page, size);
    }

    @PostMapping
    public ResponseEntity<PostDto> create(@Valid @RequestBody PostRequest req, HttpSession session) {
        User user = authService.requireLogin(session);
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(user, req));
    }

    @PutMapping("/{id}")
    public PostDto update(@PathVariable Long id, @Valid @RequestBody PostRequest req, HttpSession session) {
        return postService.update(authService.requireLogin(session), id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpSession session) {
        postService.delete(authService.requireLogin(session), id);
        return ResponseEntity.noContent().build();
    }
}