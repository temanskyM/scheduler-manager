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
    public static final int MAX_LESSON_PER_DAY = 4;

    private final List<Student> students;
    private final List<Teacher> teachers;
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
        this.subjectById = subjects.stream().collect(Collectors.toMap(Subject::getId, it -> it));
        this.classrooms = classrooms;
        this.teacherSubjectMap = createTeacherSubjectMap();
        this.studentSubjectRequirements = createStudentSubjectRequirements();
        this.schedule = new ArrayList<>();
    }

    private Map<Long, Set<Long>> createTeacherSubjectMap() {
        Map<Long, Set<Long>> map = new HashMap<>();
        for (Teacher teacher : teachers) {
            Set<Long> subjectIds = teacher.getSubjects().stream()
                    .map(Subject::getId)
                    .collect(Collectors.toSet());
            map.put(teacher.getId(), subjectIds);
        }
        return map;
    }

    private Map<Long, Map<Long, Integer>> createStudentSubjectRequirements() {
        Map<Long, Map<Long, Integer>> requirements = new HashMap<>();

        for (Student student : students) {
            Map<Long, Integer> studentRequirements = new HashMap<>();
            for (Subject subject : student.getSubjects()) {
                int requiredLessons = (int) Math.ceil(subject.getTotalHoursPerWeek() * 60.0 / LESSON_DURATION_MINUTES);
                studentRequirements.put(subject.getId(), requiredLessons);
            }
            requirements.put(student.getId(), studentRequirements);
        }

        return requirements;
    }

    public List<ScheduledLesson> buildSchedule(LocalDateTime weekStart) {
        Map<Long, List<Long>> studentsNeedingLessons = findStudentsNeedingLessons();

        while (!studentsNeedingLessons.isEmpty()) {
            boolean addedAnyLesson = false;

            for (int day = 0; day < 5; day++) {
                LocalDateTime currentDay = weekStart.plusDays(day);

                for (int slot = 0; slot < LESSONS_PER_DAY; slot++) {
                    TimeSlot timeSlot = createTimeSlot(currentDay, slot);
                    if (timeSlot == null) {
                        continue;
                    }

                    List<Teacher> availableTeachers = findAvailableTeachers(timeSlot);
                    List<Classroom> availableClassrooms = findAvailableClassrooms(timeSlot);

                    for (Teacher teacher : availableTeachers) {
                        Optional<ScheduledLesson> scheduledLesson =
                                attemptToScheduleLesson(teacher, timeSlot, availableClassrooms, studentsNeedingLessons);
                        if (scheduledLesson.isPresent()) {
                            ScheduledLesson lesson = scheduledLesson.get();
                            updateStudentSubjectRequirements(lesson.getStudentIds(), lesson.getSubjectId());
                            schedule.add(lesson);

                            addedAnyLesson = true;
                            break;
                        }
                    }
                }
            }

            if (!addedAnyLesson) {
                break;
            }

            studentsNeedingLessons = findStudentsNeedingLessons();
        }

        return schedule;
    }

    private Map<Long, List<Long>> findStudentsNeedingLessons() {
        Map<Long, List<Long>> studentsNeedingLessons = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Integer>> studentEntry : studentSubjectRequirements.entrySet()) {
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

    private TimeSlot createTimeSlot(LocalDateTime currentDay, int slot) {
        LocalDateTime lessonStart = currentDay
                .with(SCHOOL_START_TIME)
                .plusMinutes((long) slot * TOTAL_SLOT_DURATION);
        LocalDateTime lessonEnd = lessonStart.plusMinutes(LESSON_DURATION_MINUTES);

        if (lessonEnd.toLocalTime().isAfter(SCHOOL_END_TIME)) {
            return null;
        }

        return new TimeSlot(lessonStart, lessonEnd);
    }

    private List<Teacher> findAvailableTeachers(TimeSlot timeSlot) {
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
                    List<TimeSlot> teacherSlots = teacherTimeSlots.get(teacher.getId());
                    if (teacherSlots != null && teacherSlots.stream().anyMatch(timeSlot::overlaps)) {
                        return false;
                    }

                    LocalTime teacherStart = teacher.getTimeStart();
                    LocalTime teacherEnd = teacher.getTimeEnd();
                    LocalTime lessonStart = timeSlot.getDateStart().toLocalTime();
                    LocalTime lessonEnd = timeSlot.getDateEnd().toLocalTime();

                    return !lessonStart.isBefore(teacherStart) && !lessonEnd.isAfter(teacherEnd);
                })
                .collect(Collectors.toList());
    }

    private List<Classroom> findAvailableClassrooms(TimeSlot timeSlot) {
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

    private Optional<ScheduledLesson> attemptToScheduleLesson(Teacher teacher, TimeSlot timeSlot,
            List<Classroom> availableClassrooms, Map<Long, List<Long>> studentsNeedingLessons) {
        Set<Long> teacherSubjects = teacherSubjectMap.get(teacher.getId());
        if (teacherSubjects == null) {
            return Optional.empty();
        }

        List<Student> studentsForTeacher = findStudentsForTeacher(teacherSubjects, studentsNeedingLessons);
        if (studentsForTeacher.isEmpty()) {
            return Optional.empty();
        }

        return teacherSubjects.stream()
                .map(subjectId -> attemptToScheduleLessonForSubject(teacher, subjectId, timeSlot, availableClassrooms,
                        studentsForTeacher))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    private List<Student> findStudentsForTeacher(Set<Long> teacherSubjects,
            Map<Long, List<Long>> studentsNeedingLessons) {
        return students.stream()
                .filter(student -> {
                    List<Long> neededSubjects = studentsNeedingLessons.get(student.getId());
                    return neededSubjects != null && neededSubjects.stream().anyMatch(teacherSubjects::contains);
                })
                .collect(Collectors.toList());
    }

    private Optional<ScheduledLesson> attemptToScheduleLessonForSubject(Teacher teacher, Long subjectId,
            TimeSlot timeSlot,
            List<Classroom> availableClassrooms, List<Student> studentsForTeacher) {
        return Optional.ofNullable(subjectById.get(subjectId))
                .flatMap(subject -> {
                    List<Student> studentsForSubject = findStudentsForSubject(studentsForTeacher, subjectId, timeSlot);
                    if (studentsForSubject.isEmpty()) {
                        return Optional.empty();
                    }

                    return findAvailableClassroomForSubject(availableClassrooms, subjectId, timeSlot)
                            .map(classroom ->
                                    buildScheduledLesson(timeSlot, classroom, teacher, subject, studentsForSubject));
                });
    }

    private List<Student> findStudentsForSubject(List<Student> students, Long subjectId, TimeSlot timeSlot) {
        Subject subject = subjectById.get(subjectId);
        if (subject == null) {
            return List.of();
        }

        long lessonsToday = schedule.stream()
                .filter(lesson -> lesson.getSubjectId().equals(subjectId) &&
                        lesson.getDateStart().toLocalDate().equals(timeSlot.getDateStart().toLocalDate()))
                .count();
        if (lessonsToday >= MAX_LESSON_PER_DAY) {
            return List.of();
        }

        Map<Long, List<TimeSlot>> studentTimeSlots = schedule.stream()
                .filter(lesson -> lesson.getStudentIds() != null)
                .flatMap(lesson -> lesson.getStudentIds().stream()
                        .map(studentId -> Map.entry(studentId,
                                new TimeSlot(lesson.getDateStart(), lesson.getDateEnd()))))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        List<TimeSlot> conflictingTimeSlots = schedule.stream()
                .filter(lesson -> !lesson.getSubjectId().equals(subjectId))
                .map(lesson -> new TimeSlot(lesson.getDateStart(), lesson.getDateEnd()))
                .toList();

        return students.stream()
                .filter(student -> {
                    if (!student.getLevel().equals(subject.getLevel())) {
                        return false;
                    }

                    List<TimeSlot> studentSlots = studentTimeSlots.get(student.getId());
                    boolean isStudentAvailable = studentSlots == null ||
                            studentSlots.stream().noneMatch(timeSlot::overlaps);

                    boolean isClassAvailable = conflictingTimeSlots.stream()
                            .noneMatch(timeSlot::overlaps);

                    Map<Long, Integer> studentRequirements = studentSubjectRequirements.get(student.getId());
                    if (studentRequirements == null) {
                        return false;
                    }

                    Integer requiredLessons = studentRequirements.get(subjectId);
                    if (requiredLessons == null || requiredLessons <= 0) {
                        return false;
                    }

                    return isStudentAvailable && isClassAvailable && !hasTooManyConsecutiveLessons(student, subjectId);
                })
                .collect(Collectors.toList());
    }

    private Optional<Classroom> findAvailableClassroomForSubject(List<Classroom> classrooms, Long subjectId,
            TimeSlot timeSlot) {
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
                    List<TimeSlot> classroomSlots = classroomTimeSlots.get(classroom.getId());
                    boolean isClassroomAvailable = classroomSlots == null ||
                            classroomSlots.stream().noneMatch(timeSlot::overlaps);

                    List<TimeSlot> subjectSlots = subjectTimeSlots.get(classroom.getId());
                    boolean isSubjectAvailable = subjectSlots == null ||
                            subjectSlots.stream().noneMatch(timeSlot::overlaps);

                    return isClassroomAvailable && isSubjectAvailable;
                })
                .findFirst();
    }

    private ScheduledLesson buildScheduledLesson(TimeSlot timeSlot,
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

    private boolean hasTooManyConsecutiveLessons(Student student, Long subjectId) {
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
                    if (consecutiveLessons >= MAX_LESSON_PER_DAY) {
                        return true;
                    }
                } else {
                    consecutiveLessons = 1;
                }
                currentTime = previousTime;
            }
        }

        return false;
    }

    private void updateStudentSubjectRequirements(List<Long> studentIds, Long subjectId) {
        for (Long studentId : studentIds) {
            Map<Long, Integer> studentRequirements = studentSubjectRequirements.get(studentId);
            if (studentRequirements != null) {
                Integer currentRequired = studentRequirements.get(subjectId);
                if (currentRequired != null && currentRequired > 0) {
                    studentRequirements.put(subjectId, currentRequired - 1);
                }
            }
        }
    }
} 