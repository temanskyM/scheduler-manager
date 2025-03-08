package com.example.service.teacher;

import java.util.List;

public record TeacherCreateDto(
        String name,
        String surname,
        String patronymic,
        List<TeacherSubject> subjects
) {
}
