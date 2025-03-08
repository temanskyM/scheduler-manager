package com.example.service.teacher;

import java.time.LocalTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record TeacherCreateDto(
        @NotNull String name,
        @NotNull String surname,
        @NotNull String patronymic,
        @NotNull
        @Schema(type = "String", pattern = "HH:mm:SS")
        LocalTime timeStart,
        @NotNull
        @Schema(type = "String", pattern = "HH:mm:SS")
        LocalTime timeEnd,
        List<TeacherSubject> subjects
) {
}
