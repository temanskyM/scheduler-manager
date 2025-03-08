package com.example.service.teacher;

import java.time.LocalTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record TeacherCreateDto(
        String name,
        String surname,
        String patronymic,
        @Schema(type = "String", pattern = "HH:mm:SS")
        LocalTime timeStart,
        @Schema(type = "String", pattern = "HH:mm:SS")
        LocalTime timeEnd,
        List<TeacherSubject> subjects
) {
}
