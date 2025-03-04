package com.example.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.db.Classroom;
import com.example.db.Student;
import com.example.db.Subject;
import com.example.db.Teacher;
import com.example.dto.ScheduledLesson;
import com.example.dto.TimeSlot;
import lombok.Getter;

public class ScheduleBuilder {
    private static final int LESSON_DURATION_MINUTES = 45;
    private static final int BREAK_DURATION_MINUTES = 10;
    private static final int TOTAL_SLOT_DURATION = LESSON_DURATION_MINUTES + BREAK_DURATION_MINUTES;
    private static final int LESSONS_PER_DAY = 10;
    private static final LocalTime SCHOOL_START_TIME = LocalTime.of(8, 0);
    private static final LocalTime SCHOOL_END_TIME = LocalTime.of(17, 0);

    private final List<Student> students;
    private final List<Teacher> teachers;
    private final List<Subject> subjects;
    private final Map<Long, Subject> subjectById;
    private final List<Classroom> classrooms;
    private final Map<Long, Set<Long>> teacherSubjectMap;
    @Getter
    private final Map<Long, Map<Long, Integer>> studentSubjectRequirements;
    private final List<ScheduledLesson> schedule;

    public ScheduleBuilder(List<Student> students, List<Teacher> teachers, List<Subject> subjects,
            List<Classroom> classrooms) {
        this.students = students;
        this.teachers = teachers;
        this.subjects = subjects;
        this.subjectById = subjects.stream().collect(Collectors.toMap(Subject::getId, it -> it));
        this.classrooms = classrooms;
        this.teacherSubjectMap = createTeacherSubjectMap(teachers);
        this.studentSubjectRequirements = createStudentSubjectRequirements(students);
        this.schedule = new ArrayList<>();
    }

    private Map<Long, Set<Long>> createTeacherSubjectMap(List<Teacher> teachers) {
        Map<Long, Set<Long>> map = new HashMap<>();
        for (Teacher teacher : teachers) {
            Set<Long> subjectIds = teacher.getSubjects().stream()
                    .map(Subject::getId)
                    .collect(Collectors.toSet());
            map.put(teacher.getId(), subjectIds);
        }
        return map;
    }

    private Map<Long, Map<Long, Integer>> createStudentSubjectRequirements(List<Student> students) {
        Map<Long, Map<Long, Integer>> requirements = new HashMap<>();

        for (Student student : students) {
            Map<Long, Integer> studentRequirements = new HashMap<>();
            for (Subject subject : student.getSubjects()) {
                // Set required lessons based on subject's total hours per week
                int requiredLessons = (int) Math.ceil(subject.getTotalHoursPerWeek() * 60.0 / LESSON_DURATION_MINUTES);
                studentRequirements.put(subject.getId(), requiredLessons);
            }
            requirements.put(student.getId(), studentRequirements);
        }

        return requirements;
    }

    public List<ScheduledLesson> buildSchedule(LocalDateTime weekStart) {
        Map<Long, List<Long>> studentsNeedingLessons = findStudentsNeedingLessons(studentSubjectRequirements);

        while (!studentsNeedingLessons.isEmpty()) {
            boolean addedAnyLesson = false;

            // Try to schedule lessons for each day and time slot
            for (int day = 0; day < 5; day++) { // Monday to Friday
                LocalDateTime currentDay = weekStart.plusDays(day);

                for (int slot = 0; slot < LESSONS_PER_DAY; slot++) {
                    // Calculate lesson start time including breaks
                    LocalDateTime lessonStart = currentDay
                            .with(SCHOOL_START_TIME)
                            .plusMinutes(slot * TOTAL_SLOT_DURATION);
                    LocalDateTime lessonEnd = lessonStart.plusMinutes(LESSON_DURATION_MINUTES);

                    // Skip if lesson would end after school hours
                    if (lessonEnd.toLocalTime().isAfter(SCHOOL_END_TIME)) {
                        continue;
                    }

                    TimeSlot timeSlot = new TimeSlot(lessonStart, lessonEnd);

                    // Find available teachers and classrooms for this time slot
                    List<Teacher> availableTeachers = findAvailableTeachers(teachers, schedule, timeSlot);
                    List<Classroom> availableClassrooms =
                            findAvailableClassrooms(classrooms, schedule, timeSlot);

                    // Try to schedule lessons for each teacher
                    for (Teacher teacher : availableTeachers) {
                        Set<Long> teacherSubjects = teacherSubjectMap.get(teacher.getId());
                        if (teacherSubjects == null) {
                            continue;
                        }

                        // Find students who need subjects this teacher can teach
                        List<Student> studentsForTeacher =
                                findStudentsForTeacher(students, teacherSubjects, studentsNeedingLessons);
                        if (studentsForTeacher.isEmpty()) {
                            continue;
                        }

                        // Try each subject the teacher can teach
                        boolean lessonScheduled = false;
                        for (Long subjectId : teacherSubjects) {
                            if (lessonScheduled) {
                                break; // Skip remaining subjects for this teacher at this time slot
                            }

                            Subject subject = subjects.stream()
                                    .filter(s -> s.getId().equals(subjectId))
                                    .findFirst()
                                    .orElse(null);
                            if (subject == null) {
                                continue;
                            }

                            // Find students who need this subject
                            List<Student> studentsForSubject = findStudentsForSubject(studentsForTeacher, subjectId,
                                    schedule, timeSlot, studentSubjectRequirements);
                            if (studentsForSubject.isEmpty()) {
                                continue;
                            }

                            // Find an available classroom
                            Optional<Classroom> availableClassroom = findAvailableClassroomForSubject(
                                    availableClassrooms, subjectId, schedule, timeSlot);
                            if (availableClassroom.isEmpty()) {
                                continue;
                            }

                            // Create and add the scheduled lesson
                            ScheduledLesson scheduledLesson = createScheduledLesson(
                                    timeSlot, availableClassroom.get(), teacher, subject,
                                    studentsForSubject);
                            schedule.add(scheduledLesson);

                            // Update student subject requirements
                            updateStudentSubjectRequirements(studentSubjectRequirements, studentsForSubject, subjectId);

                            addedAnyLesson = true;
                            lessonScheduled = true; // Mark that we've scheduled a lesson for this teacher
                        }
                    }
                }
            }

            // If we couldn't add any more lessons, break the loop
            if (!addedAnyLesson) {
                break;
            }

            // Update the list of students still needing lessons
            studentsNeedingLessons = findStudentsNeedingLessons(studentSubjectRequirements);
        }

        return schedule;
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

    private List<Teacher> findAvailableTeachers(List<Teacher> teachers, List<ScheduledLesson> schedule,
            TimeSlot timeSlot) {
        // Create TimeSlot objects for all scheduled lessons once
        Map<Long, List<TimeSlot>> teacherTimeSlots = schedule.stream()
                .filter(lesson -> lesson.getTeacherId() != null)
                .collect(Collectors.groupingBy(
                        ScheduledLesson::getTeacherId,
                        Collectors.mapping(
                                lesson -> new TimeSlot(lesson.getDateStart(), lesson.getDateEnd()),
                                Collectors.toList()
                        )
                ));

        return teachers.stream()
                .filter(teacher -> {
                    // Check if teacher is already scheduled for this time
                    List<TimeSlot> teacherSlots = teacherTimeSlots.get(teacher.getId());
                    if (teacherSlots != null && teacherSlots.stream().anyMatch(timeSlot::overlaps)) {
                        return false;
                    }

                    // Check if lesson is within teacher's working hours
                    LocalTime teacherStart = teacher.getTimeStart();
                    LocalTime teacherEnd = teacher.getTimeEnd();
                    LocalTime lessonStart = timeSlot.getDateStart().toLocalTime();
                    LocalTime lessonEnd = timeSlot.getDateEnd().toLocalTime();

                    return !lessonStart.isBefore(teacherStart) && !lessonEnd.isAfter(teacherEnd);
                })
                .collect(Collectors.toList());
    }

    private List<Classroom> findAvailableClassrooms(List<Classroom> classrooms, List<ScheduledLesson> schedule,
            TimeSlot timeSlot) {
        // Create TimeSlot objects for all scheduled lessons once
        Map<Long, List<TimeSlot>> classroomTimeSlots = schedule.stream()
                .filter(lesson -> lesson.getClassroomId() != null)
                .collect(Collectors.groupingBy(
                        ScheduledLesson::getClassroomId,
                        Collectors.mapping(
                                lesson -> new TimeSlot(lesson.getDateStart(), lesson.getDateEnd()),
                                Collectors.toList()
                        )
                ));

        return classrooms.stream()
                .filter(classroom -> {
                    List<TimeSlot> classroomSlots = classroomTimeSlots.get(classroom.getId());
                    return classroomSlots == null ||
                            classroomSlots.stream().noneMatch(timeSlot::overlaps);
                })
                .collect(Collectors.toList());
    }

    private List<Student> findStudentsForTeacher(List<Student> students, Set<Long> teacherSubjects,
            Map<Long, List<Long>> studentsNeedingLessons) {
        return students.stream()
                .filter(student -> {
                    List<Long> neededSubjects = studentsNeedingLessons.get(student.getId());
                    return neededSubjects != null && neededSubjects.stream().anyMatch(teacherSubjects::contains);
                })
                .collect(Collectors.toList());
    }

    private List<Student> findStudentsForSubject(List<Student> students, Long subjectId,
            List<ScheduledLesson> schedule, TimeSlot timeSlot,
            Map<Long, Map<Long, Integer>> studentSubjectRequirements) {
        // Get the subject to check its level
        Subject subject = Optional.ofNullable(subjectById.get(subjectId))
                .orElseThrow(() -> new IllegalStateException("Subject not found: " + subjectId));

        // Check if subject with same level is already scheduled 4 times today
        long lessonsToday = schedule.stream()
                .filter(lesson -> lesson.getSubjectId().equals(subjectId) &&
                        lesson.getDateStart().toLocalDate().equals(timeSlot.getDateStart().toLocalDate()))
                .count();
        if (lessonsToday >= 4) {
            return List.of(); // Return empty list if subject is already scheduled 4 times today
        }

        // Create TimeSlot objects for all scheduled lessons once
        Map<Long, List<TimeSlot>> studentTimeSlots = schedule.stream()
                .filter(lesson -> lesson.getStudentIds() != null)
                .flatMap(lesson -> lesson.getStudentIds().stream()
                        .map(studentId -> Map.entry(studentId,
                                new TimeSlot(lesson.getDateStart(), lesson.getDateEnd()))))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        // Create TimeSlot objects for lessons with different subjects at the same time
        List<TimeSlot> conflictingTimeSlots = schedule.stream()
                .filter(lesson -> !lesson.getSubjectId().equals(subjectId))
                .map(lesson -> new TimeSlot(lesson.getDateStart(), lesson.getDateEnd()))
                .toList();

        return students.stream()
                .filter(student -> {
                    // Check if student is from the same level as the subject
                    if (!student.getLevel().equals(subject.getLevel())) {
                        return false;
                    }

                    // Check if student is already scheduled for this time
                    List<TimeSlot> studentSlots = studentTimeSlots.get(student.getId());
                    boolean isStudentAvailable = studentSlots == null ||
                            studentSlots.stream().noneMatch(timeSlot::overlaps);

                    // Check if any other student from the same level is scheduled for a different subject at this time
                    boolean isClassAvailable = conflictingTimeSlots.stream()
                            .noneMatch(timeSlot::overlaps);

                    // Check if student needs more lessons for this subject
                    Map<Long, Integer> studentRequirements = studentSubjectRequirements.get(student.getId());
                    if (studentRequirements == null) {
                        return false;
                    }

                    Integer requiredLessons = studentRequirements.get(subjectId);
                    if (requiredLessons == null || requiredLessons <= 0) {
                        return false;
                    }

                    // Check if student already has 4 consecutive lessons of this subject
                    List<ScheduledLesson> studentLessons = schedule.stream()
                            .filter(lesson -> lesson.getStudentIds().contains(student.getId()) &&
                                    lesson.getSubjectId().equals(subjectId))
                            .sorted((l1, l2) -> l2.getDateStart().compareTo(l1.getDateStart()))
                            .toList();

                    if (!studentLessons.isEmpty()) {
                        int consecutiveLessons = 1;
                        LocalDateTime currentTime = studentLessons.get(0).getDateStart();

                        for (int i = 1; i < studentLessons.size(); i++) {
                            LocalDateTime previousTime = studentLessons.get(i).getDateStart();
                            if (currentTime.minusMinutes(TOTAL_SLOT_DURATION).equals(previousTime)) {
                                consecutiveLessons++;
                                if (consecutiveLessons >= 4) {
                                    return false;
                                }
                            } else {
                                consecutiveLessons = 1;
                            }
                            currentTime = previousTime;
                        }
                    }

                    return isStudentAvailable && isClassAvailable;
                })
                .collect(Collectors.toList());
    }

    private Optional<Classroom> findAvailableClassroomForSubject(List<Classroom> classrooms, Long subjectId,
            List<ScheduledLesson> schedule, TimeSlot timeSlot) {
        // Create TimeSlot objects for all scheduled lessons once
        Map<Long, List<TimeSlot>> classroomTimeSlots = schedule.stream()
                .filter(lesson -> lesson.getClassroomId() != null)
                .collect(Collectors.groupingBy(
                        ScheduledLesson::getClassroomId,
                        Collectors.mapping(
                                lesson -> new TimeSlot(lesson.getDateStart(), lesson.getDateEnd()),
                                Collectors.toList()
                        )
                ));

        Map<Long, List<TimeSlot>> subjectTimeSlots = schedule.stream()
                .filter(lesson -> lesson.getSubjectId() != null && lesson.getSubjectId().equals(subjectId))
                .collect(Collectors.groupingBy(
                        ScheduledLesson::getClassroomId,
                        Collectors.mapping(
                                lesson -> new TimeSlot(lesson.getDateStart(), lesson.getDateEnd()),
                                Collectors.toList()
                        )
                ));

        return classrooms.stream()
                .filter(classroom -> {
                    // Check if classroom is available for this time
                    List<TimeSlot> classroomSlots = classroomTimeSlots.get(classroom.getId());
                    boolean isClassroomAvailable = classroomSlots == null ||
                            classroomSlots.stream().noneMatch(timeSlot::overlaps);

                    // Check if subject is already scheduled in this classroom for this time
                    List<TimeSlot> subjectSlots = subjectTimeSlots.get(classroom.getId());
                    boolean isSubjectAvailable = subjectSlots == null ||
                            subjectSlots.stream().noneMatch(timeSlot::overlaps);

                    return isClassroomAvailable && isSubjectAvailable;
                })
                .findFirst();
    }

    private ScheduledLesson createScheduledLesson(TimeSlot timeSlot,
            Classroom classroom, Teacher teacher,
            Subject subject, List<Student> students) {
        ScheduledLesson lesson = new ScheduledLesson();
        lesson.setDateStart(timeSlot.getDateStart());
        lesson.setDateEnd(timeSlot.getDateEnd());
        lesson.setClassroomId(classroom.getId());
        lesson.setClassroomName(classroom.getName());
        lesson.setTeacherId(teacher.getId());
        lesson.setTeacherName(teacher.getName());
        lesson.setTeacherSurname(teacher.getSurname());
        lesson.setSubjectId(subject.getId());
        lesson.setSubjectName(subject.getName());
        lesson.setStudentIds(students.stream().map(Student::getId).collect(Collectors.toList()));
        lesson.setStudentNames(students.stream()
                .map(s -> s.getName() + " " + s.getSurname())
                .collect(Collectors.toList()));
        return lesson;
    }

    private void updateStudentSubjectRequirements(Map<Long, Map<Long, Integer>> requirements,
            List<Student> students, Long subjectId) {
        for (Student student : students) {
            Map<Long, Integer> studentRequirements = requirements.get(student.getId());
            if (studentRequirements != null) {
                Integer currentRequired = studentRequirements.get(subjectId);
                if (currentRequired != null && currentRequired > 0) {
                    studentRequirements.put(subjectId, currentRequired - 1);
                }
            }
        }
    }
} 