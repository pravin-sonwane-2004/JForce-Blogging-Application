package com.pravin.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ReportData {

    // each row: { username, postCount }
    private List<Map<String, Object>> mostActiveUsers;

    private List<PostDto> mostViewedPosts;
}