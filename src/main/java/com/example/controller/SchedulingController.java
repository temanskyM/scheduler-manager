package com.example.controller;

import com.example.dto.SchedulingResponseDto;
import com.example.dto.StudentScheduleDto;
import com.example.service.LessonService;
import com.example.service.SchedulingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/scheduling")
public class SchedulingController {

    @Autowired
    private SchedulingService schedulingService;
    @Autowired
    private LessonService lessonService;

    @PostMapping("/generate")
    public ResponseEntity<SchedulingResponseDto> generateSchedule() {
        try {
            SchedulingResponseDto schedule = schedulingService.generateSchedule();
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            log.warn("Failed to generate schedule",e);
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getMessage());
        }
    }
} 