package com.pravin.backend.service;

import com.pravin.backend.dto.UserDto;
import com.pravin.backend.entity.User;
import com.pravin.backend.enums.Role;
import com.pravin.backend.exception.ApiException;
import com.pravin.backend.repository.PostRepository;
import com.pravin.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public UserService(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public List<UserDto> allUsers() {
        return userRepository.findAll().stream().map(UserDto::from).toList();
    }

    @Transactional
    public UserDto updateRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        user.setRole(role);
        return UserDto.from(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id, User admin) {
        if (admin.getId().equals(id)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        }
        userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        postRepository.deleteByAuthorId(id); // remove their posts first
        userRepository.deleteById(id);
    }
}