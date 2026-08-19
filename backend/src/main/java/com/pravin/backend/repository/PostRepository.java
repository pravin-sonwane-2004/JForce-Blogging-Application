package com.pravin.backend.repository;

import com.pravin.backend.entity.Post;
import com.pravin.backend.entity.PostStatus;
import com.pravin.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    Page<Post> findByAuthor(User author, Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            WHERE p.status = :status
              AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.content) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.tags) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.author.username) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Post> searchByStatus(@Param("status") PostStatus status,
                              @Param("q") String q,
                              Pageable pageable);

    @Query("SELECT p.author.username, COUNT(p) FROM Post p GROUP BY p.author ORDER BY COUNT(p) DESC")
    List<Object[]> countPostsByAuthor();

    List<Post> findTop5ByOrderByViewsDesc();

    void deleteByAuthorId(Long authorId);
}