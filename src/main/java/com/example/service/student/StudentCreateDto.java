package com.example.service.student;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record StudentCreateDto(
        @NotNull String name,
        @NotNull String surname,
        @NotNull Integer level,
        @NotNull List<String> subjects
) {
}
