package com.pravin.backend.dto;

import com.pravin.backend.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostDto {

    private Long id;
    private String title;
    private String excerpt;
    private String content;
    private String tags;
    private String status;
    private boolean featured;
    private int views;
    private String author;
    private LocalDateTime createdAt;

    public static PostDto from(Post post) {
        String content = post.getContent();
        String excerpt = content != null && content.length() > 160
                ? content.substring(0, 160) + "..."
                : content;

        return new PostDto(
                post.getId(),
                post.getTitle(),
                excerpt,
                content,
                post.getTags(),
                post.getStatus().name(),
                post.isFeatured(),
                post.getViews(),
                post.getAuthor().getUsername(),
                post.getCreatedAt()
        );
    }
}