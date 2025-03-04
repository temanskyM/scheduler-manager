package com.example.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TeacherScheduleDto {
    private LocalDateTime start;
    private LocalDateTime end;
    private String subjectName;
    private int subjectLevel;
    private String classroomName;
    private String teacherName;
} 