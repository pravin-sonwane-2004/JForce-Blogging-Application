package com.pravin.backend.service;

import com.pravin.backend.dto.PostDto;
import com.pravin.backend.dto.PostRequest;
import com.pravin.backend.dto.ReportData;
import com.pravin.backend.entity.Post;
import com.pravin.backend.entity.User;
import com.pravin.backend.enums.PostStatus;
import com.pravin.backend.enums.Role;
import com.pravin.backend.exception.ApiException;
import com.pravin.backend.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // Public feed: only approved posts, with optional search + sort + pagination
    @Transactional(readOnly = true)
    public Page<PostDto> feed(String q, String sort, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        if (q == null || q.isBlank()) {
            return postRepository.findByStatus(PostStatus.APPROVED, pageable).map(PostDto::from);
        }
        return postRepository.searchByStatus(PostStatus.APPROVED, q.trim(), pageable).map(PostDto::from);
    }

    @Transactional
    public PostDto getById(Long id, User viewer) {
        Post post = findPost(id);

        // Only approved posts are public; authors/admins may also see their own pending/rejected posts
        boolean visible = post.getStatus() == PostStatus.APPROVED
                || (viewer != null && (post.getAuthor().getId().equals(viewer.getId()) || viewer.getRole() == Role.ADMIN));
        if (!visible) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Post not found");
        }

        post.setViews(post.getViews() + 1); // drives the "popularity" sort + reports
        return PostDto.from(postRepository.save(post));
    }

    @Transactional(readOnly = true)
    public Page<PostDto> myPosts(User author, int page, int size) {
        return postRepository.findByAuthor(
                        author,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(PostDto::from);
    }

    @Transactional
    public PostDto create(User author, PostRequest req) {
        Post post = new Post();
        apply(post, req);
        post.setAuthor(author);
        post.setStatus(PostStatus.PENDING); // new posts wait for admin approval
        return PostDto.from(postRepository.save(post));
    }

    @Transactional
    public PostDto update(User currentUser, Long id, PostRequest req) {
        Post post = findPost(id);
        if (!post.getAuthor().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only edit your own posts");
        }
        apply(post, req);
        return PostDto.from(postRepository.save(post));
    }

    @Transactional
    public void delete(User currentUser, Long id) {
        Post post = findPost(id);
        if (!post.getAuthor().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only delete your own posts");
        }
        postRepository.delete(post);
    }

    // ---- admin operations ----

    @Transactional(readOnly = true)
    public Page<PostDto> all(PostStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status == null) {
            return postRepository.findAll(pageable).map(PostDto::from);
        }
        return postRepository.findByStatus(status, pageable).map(PostDto::from);
    }

    @Transactional
    public PostDto changeStatus(Long id, PostStatus status) {
        Post post = findPost(id);
        post.setStatus(status);
        return PostDto.from(postRepository.save(post));
    }

    @Transactional
    public PostDto toggleFeatured(Long id) {
        Post post = findPost(id);
        post.setFeatured(!post.isFeatured());
        return PostDto.from(postRepository.save(post));
    }

    @Transactional(readOnly = true)
    public ReportData reports() {
        List<Map<String, Object>> activeUsers = new ArrayList<>();
        for (Object[] row : postRepository.countPostsByAuthor()) {
            activeUsers.add(Map.of(
                    "username", row[0],
                    "postCount", row[1]
            ));
        }

        List<PostDto> popular = postRepository.findTop5ByOrderByViewsDesc()
                .stream()
                .map(PostDto::from)
                .toList();

        return new ReportData(activeUsers, popular);
    }

    private Post findPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    private void apply(Post post, PostRequest req) {
        post.setTitle(req.getTitle().trim());
        post.setContent(req.getContent());
        post.setTags(req.getTags() == null ? null : req.getTags().trim());
    }

    private Sort resolveSort(String sort) {
        return switch (sort == null ? "" : sort) {
            case "popular" -> Sort.by(Sort.Direction.DESC, "views");
            case "author" -> Sort.by(Sort.Direction.ASC, "author.username");
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // newest
        };
    }
}