package com.example.service.teacher;

import java.time.LocalTime;
import java.util.List;

public record TeacherCreateDto(
        String name,
        String surname,
        String patronymic,
        LocalTime timeStart,
        LocalTime timeEnd,
        List<TeacherSubject> subjects
) {
}
