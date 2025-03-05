package com.example.db;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    @Query("SELECT l FROM Lesson l " +
            "JOIN l.students s " +
            "WHERE s.id = :studentId " +
            "ORDER BY l.dateStart")
    List<Lesson> findByStudentIdOrderByDateStart(@Param("studentId") Long studentId);

    List<Lesson> findByTeacher_IdOrderByDateStart(Long teacherId);

    List<Lesson> findByDateStartBetweenOrderByDateStart(LocalDateTime start, LocalDateTime end);
} 