package com.pravin.backend.config;

import com.pravin.backend.entity.Post;
import com.pravin.backend.entity.User;
import com.pravin.backend.enums.PostStatus;
import com.pravin.backend.enums.Role;
import com.pravin.backend.repository.PostRepository;
import com.pravin.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PostRepository postRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // already seeded
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@blog.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        User john = new User();
        john.setUsername("john");
        john.setEmail("john@blog.com");
        john.setPassword(passwordEncoder.encode("john123"));
        john.setRole(Role.USER);
        userRepository.save(john);

        postRepository.saveAll(List.of(
                post(john, "Getting Started with Spring Boot",
                        "Spring Boot makes it easy to create stand-alone, production-grade Spring applications. In this post we set up our first REST API in minutes and understand auto-configuration.",
                        "java, spring, tutorial", PostStatus.APPROVED, 120, true),
                post(john, "React Hooks Explained Simply",
                        "useState and useEffect are the two hooks you will use every single day. This article explains state, side effects and data fetching with small examples you can copy.",
                        "react, javascript", PostStatus.APPROVED, 87, false),
                post(john, "Why You Should Learn SQL",
                        "SQL is everywhere. Even in the age of ORMs you need to understand joins, indexes and aggregations to build fast applications.",
                        "sql, database", PostStatus.APPROVED, 64, false),
                post(john, "MySQL Performance Tips",
                        "Indexing, avoiding N+1 queries and choosing the right column types can make your database ten times faster. A practical checklist.",
                        "mysql, database, performance", PostStatus.APPROVED, 32, false),
                post(john, "Building a REST API with Validation",
                        "Bean Validation with @Valid gives you clean error messages without writing manual checks. Here is how to wire it up in Spring Boot.",
                        "java, api", PostStatus.PENDING, 0, false),
                post(john, "My Favourite VSCode Extensions",
                        "A personal list of the extensions I use daily: Prettier, ESLint and the Java extension pack for a smooth full-stack workflow.",
                        "tools, vscode", PostStatus.PENDING, 0, false)
        ));
    }

    private Post post(User author, String title, String content, String tags, PostStatus status, int views, boolean featured) {
        Post p = new Post();
        p.setAuthor(author);
        p.setTitle(title);
        p.setContent(content);
        p.setTags(tags);
        p.setStatus(status);
        p.setViews(views);
        p.setFeatured(featured);
        return p;
    }
}