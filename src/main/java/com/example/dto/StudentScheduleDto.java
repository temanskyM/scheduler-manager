package com.example.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentScheduleDto {
    private LocalDateTime start;
    private LocalDateTime end;
    private String subjectName;
    private int subjectLevel;
    private String teacherName;
    private String classroomName;
} 