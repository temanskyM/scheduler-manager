package com.example.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.db.Classroom;
import com.example.db.Lesson;
import com.example.db.LessonRepository;
import com.example.db.Student;
import com.example.db.Subject;
import com.example.db.Teacher;
import com.example.dto.ScheduledLesson;
import com.example.dto.SchedulingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SchedulingService {
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final SubjectService subjectService;
    private final ClassroomService classroomService;
    private final LessonRepository lessonRepository;

    @Transactional
    public SchedulingResponseDto generateSchedule() {
        // Get all available entities
        List<Student> students = studentService.findAll();
        List<Teacher> teachers = teacherService.findAll();
        List<Subject> subjects = subjectService.findAll();
        List<Classroom> classrooms = classroomService.findAll();

        // Create a map of student-subject relationships with required lessons

        // Initialize schedule for the week
        LocalDateTime currentWeekStart = getCurrentWeekStart();

        // Create and use ScheduleBuilder to generate the schedule
        ScheduleBuilder scheduleBuilder = new ScheduleBuilder(students, teachers, subjects, classrooms);
        List<ScheduledLesson> schedule = scheduleBuilder.buildSchedule(currentWeekStart);

        // Save all lessons to the database
        saveLessonsToDatabase(schedule);

        // Check if all requirements are met
        String problems = buildProblemsText(scheduleBuilder, students, subjects);

        SchedulingResponseDto response = new SchedulingResponseDto();
        response.setProblems(problems);
        return response;
    }

    private String buildProblemsText(ScheduleBuilder scheduleBuilder, List<Student> students, List<Subject> subjects) {
        Map<Long, Map<Long, Integer>> studentSubjectRequirements = scheduleBuilder.getStudentSubjectRequirements();
        Map<Long, List<Long>> remainingRequirements = findStudentsNeedingLessons(studentSubjectRequirements);
        String problems = null;
        if (!remainingRequirements.isEmpty()) {
            problems = buildRemainingRequirementsMessage(remainingRequirements, students, subjects,
                    studentSubjectRequirements);
        }
        return problems;
    }

    private LocalDateTime getCurrentWeekStart() {
        LocalDateTime now = LocalDateTime.now();
        return now.with(DayOfWeek.MONDAY).withHour(8).withMinute(0).withSecond(0).withNano(0);
    }

    private void saveLessonsToDatabase(List<ScheduledLesson> schedule) {
        ArrayList<Lesson> lessons = new ArrayList<>();
        for (ScheduledLesson scheduledLesson : schedule) {
            Lesson lesson = new Lesson();
            lesson.setDateStart(scheduledLesson.getDateStart());
            lesson.setDateEnd(scheduledLesson.getDateEnd());

            // Set classroom using getReference
            Classroom classroom = classroomService.getReferenceById(scheduledLesson.getClassroomId());
            lesson.setClassroom(classroom);

            // Set teacher using getReference
            Teacher teacher = teacherService.getReferenceById(scheduledLesson.getTeacherId());
            lesson.setTeacher(teacher);

            // Set subject using getReference
            Subject subject = subjectService.getReferenceById(scheduledLesson.getSubjectId());
            lesson.setSubject(subject);

            // Set students using getReference
            List<Student> students = scheduledLesson.getStudentIds().stream()
                    .map(studentService::getReferenceById)
                    .collect(Collectors.toList());
            lesson.setStudents(students);

            lessons.add(lesson);
        }

        lessonRepository.deleteAll();
        lessonRepository.flush();
        // Save the lesson
        lessonRepository.saveAll(lessons);
    }

    private Map<Long, List<Long>> findStudentsNeedingLessons(Map<Long, Map<Long, Integer>> requirements) {
        Map<Long, List<Long>> studentsNeedingLessons = new HashMap<>();

        for (Map.Entry<Long, Map<Long, Integer>> studentEntry : requirements.entrySet()) {
            List<Long> subjectsNeeded = studentEntry.getValue().entrySet().stream()
                    .filter(entry -> entry.getValue() > 0)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (!subjectsNeeded.isEmpty()) {
                studentsNeedingLessons.put(studentEntry.getKey(), subjectsNeeded);
            }
        }

        return studentsNeedingLessons;
    }

    private String buildRemainingRequirementsMessage(Map<Long, List<Long>> remainingRequirements,
            List<Student> students, List<Subject> subjects,
            Map<Long, Map<Long, Integer>> studentSubjectRequirements) {
        StringBuilder errorMessage =
                new StringBuilder("Unable to schedule all required lessons. Remaining requirements:\n");

        for (Map.Entry<Long, List<Long>> entry : remainingRequirements.entrySet()) {
            Student student = students.stream()
                    .filter(s -> s.getId().equals(entry.getKey()))
                    .findFirst()
                    .orElse(null);

            if (student != null) {
                errorMessage.append("\nStudent: ").append(student.getName()).append(" ").append(student.getSurname());
                errorMessage.append("\nMissing lessons for subjects:\n");

                for (Long subjectId : entry.getValue()) {
                    Subject subject = subjects.stream()
                            .filter(s -> s.getId().equals(subjectId))
                            .findFirst()
                            .orElse(null);

                    if (subject != null) {
                        int remainingLessons = studentSubjectRequirements.get(student.getId()).get(subjectId);
                        errorMessage.append("- ").append(subject.getName())
                                .append(": ").append(remainingLessons)
                                .append(" lessons remaining\n");
                    }
                }
            }
        }

        errorMessage.append("\nPossible reasons:\n");
        errorMessage.append("1. No available teachers for required subjects\n");
        errorMessage.append("2. No available classrooms\n");
        errorMessage.append("3. Time constraints (working hours, lesson duration)\n");

        return errorMessage.toString();
    }

    public List<ScheduledLesson> getCurrentWeekLessons() {
        LocalDateTime weekStart = getCurrentWeekStart();
        LocalDateTime weekEnd = weekStart.plusDays(5);

        return lessonRepository.findByDateStartBetweenOrderByDateStart(weekStart, weekEnd).stream()
                .map(this::convertToScheduledLesson)
                .collect(Collectors.toList());
    }

    private ScheduledLesson convertToScheduledLesson(Lesson lesson) {
        ScheduledLesson scheduledLesson = new ScheduledLesson();
        scheduledLesson.setDateStart(lesson.getDateStart());
        scheduledLesson.setDateEnd(lesson.getDateEnd());
        scheduledLesson.setClassroomId(lesson.getClassroom().getId());
        scheduledLesson.setClassroomName(lesson.getClassroom().getName());
        scheduledLesson.setTeacherId(lesson.getTeacher().getId());
        scheduledLesson.setTeacherName(lesson.getTeacher().getName());
        scheduledLesson.setTeacherSurname(lesson.getTeacher().getSurname());
        scheduledLesson.setSubjectId(lesson.getSubject().getId());
        scheduledLesson.setSubjectName(lesson.getSubject().getName());
        scheduledLesson.setSubjectLevel(lesson.getSubject().getLevel());
        scheduledLesson.setStudentIds(lesson.getStudents().stream()
                .map(Student::getId)
                .collect(Collectors.toList()));
        scheduledLesson.setStudentNames(lesson.getStudents().stream()
                .map(s -> s.getName() + " " + s.getSurname())
                .collect(Collectors.toList()));
        return scheduledLesson;
    }
} 