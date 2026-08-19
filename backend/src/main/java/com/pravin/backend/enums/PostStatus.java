package com.pravin.backend.enums;

public enum PostStatus {
    PENDING,   // waiting for admin approval
    APPROVED,  // visible in the public feed
    REJECTED   // removed by an admin / content guideline violation
}