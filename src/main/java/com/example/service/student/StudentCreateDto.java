package com.example.service.student;

import java.util.List;

public record StudentCreateDto(
        String name,
        String surname,
        Integer level,
        List<String> subjects
) {
}
