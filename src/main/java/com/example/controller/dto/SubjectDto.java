package com.example.controller.dto;

public record SubjectDto(
        long id,
        String name,
        int level,
        int totalCountPerWeek
) {
}
