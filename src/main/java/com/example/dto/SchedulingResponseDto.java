package com.example.dto;

import lombok.Data;
import java.util.List;

@Data
public class SchedulingResponseDto {
    private List<ScheduledLesson> schedule;
    private String problems;
} 