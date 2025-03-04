package com.example.controller;

import com.example.db.Lesson;
import com.example.dto.StudentScheduleDto;
import com.example.dto.TeacherScheduleDto;
import com.example.service.LessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    @Autowired
    private LessonService lessonService;

    @GetMapping
    public List<Lesson> getAllLessons() {
        return lessonService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lesson> getLessonById(@PathVariable Long id) {
        return lessonService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Lesson createLesson(@RequestBody Lesson lesson) {
        return lessonService.save(lesson);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lesson> updateLesson(@PathVariable Long id, @RequestBody Lesson lesson) {
        return lessonService.findById(id)
                .map(existingLesson -> {
                    lesson.setId(id);
                    return ResponseEntity.ok(lessonService.save(lesson));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        return lessonService.findById(id)
                .map(lesson -> {
                    lessonService.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentScheduleDto>> getStudentSchedule(@PathVariable Long studentId) {
        try {
            List<StudentScheduleDto> schedule = lessonService.getStudentSchedule(studentId);
            return ResponseEntity.ok(schedule);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<TeacherScheduleDto>> getTeacherSchedule(@PathVariable Long teacherId) {
        try {
            List<TeacherScheduleDto> schedule = lessonService.getTeacherSchedule(teacherId);
            return ResponseEntity.ok(schedule);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 