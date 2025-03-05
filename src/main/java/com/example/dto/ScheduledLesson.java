package com.example.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ScheduledLesson {
    private Long id;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
    private Long classroomId;
    private String classroomName;
    private Long teacherId;
    private String teacherName;
    private String teacherSurname;
    private Long subjectId;
    private String subjectName;
    private Integer subjectLevel;
    private List<Long> studentIds;
    private List<String> studentNames;
} 