package com.example.service.schedule;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.db.Classroom;
import com.example.db.Student;
import com.example.db.Subject;
import com.example.db.Teacher;
import com.example.dto.ScheduledLesson;
import com.example.dto.TimeSlot;
import lombok.Getter;

public class ClassroomScheduler {
    private static final int LESSON_DURATION_MINUTES = 45;
    private static final int BREAK_DURATION_MINUTES = 10;
    private static final int TOTAL_SLOT_DURATION = LESSON_DURATION_MINUTES + BREAK_DURATION_MINUTES;
    private static final int LESSONS_PER_DAY = 10;
    private static final LocalTime SCHOOL_START_TIME = LocalTime.of(8, 0);
    private static final LocalTime SCHOOL_END_TIME = LocalTime.of(17, 0);

    private final List<Student> students;
    private final List<Teacher> teachers;
    private final Map<Long, Subject> subjectById;
    private final List<Classroom> classrooms;
    private final Map<Long, Set<Long>> teacherSubjectMap;
    @Getter
    private final Map<Long, Map<Long, Integer>> studentSubjectRequirements;
    private final List<ScheduledLesson> schedule;

    public ClassroomScheduler(List<Student> students, List<Teacher> teachers, List<Subject> subjects,
            List<Classroom> classrooms) {
        this.students = students;
        this.teachers = teachers;
        this.subjectById = subjects.stream().collect(Collectors.toMap(Subject::getId, it -> it));
        this.classrooms = classrooms;
        this.teacherSubjectMap = createTeacherSubjectMap(teachers);
        this.studentSubjectRequirements = createStudentSubjectRequirements(students);
        this.schedule = new ArrayList<>();
    }

    private static Map<Long, Set<Long>> createTeacherSubjectMap(List<Teacher> teachers) {
        Map<Long, Set<Long>> map = new HashMap<>();
        for (Teacher teacher : teachers) {
            Set<Long> subjectIds = teacher.getSubjects().stream()
                    .map(Subject::getId)
                    .collect(Collectors.toSet());
            map.put(teacher.getId(), subjectIds);
        }
        return map;
    }

    private static Map<Long, Map<Long, Integer>> createStudentSubjectRequirements(List<Student> students) {
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
        // Create a map of available time slots for each classroom
        Map<Long, List<TimeSlot>> classroomTimeSlots = createClassroomTimeSlots(weekStart);

        // Sort classrooms by number of available slots (ascending) to prioritize filling classrooms with fewer slots
        List<Classroom> sortedClassrooms = new ArrayList<>(classrooms);
        sortedClassrooms.sort(Comparator.comparingInt(Classroom::getCapacity).reversed());

        // Try to fill each classroom
        for (Classroom classroom : sortedClassrooms) {
            List<TimeSlot> availableSlots = classroomTimeSlots.get(classroom.getId());

            for (TimeSlot timeSlot : availableSlots) {
                // Find all possible subject-teacher combinations for this time slot
                List<SubjectTeacherPair> possiblePairs = findPossibleSubjectTeacherPairs(timeSlot);

                // For each possible pair, try to find students
                for (SubjectTeacherPair pair : possiblePairs) {
                    List<Student> availableStudents = findAvailableStudents(pair.subject, timeSlot);
                    if (!availableStudents.isEmpty()) {
                        // Create and add the lesson
                        ScheduledLesson lesson = buildScheduledLesson(timeSlot, classroom, pair.teacher, pair.subject,
                                availableStudents);
                        schedule.add(lesson);
                        updateStudentSubjectRequirements(availableStudents.stream()
                                .map(Student::getId)
                                .collect(Collectors.toList()), pair.subject.getId());
                        break;
                    }
                }
            }
        }

        return schedule;
    }

    private Map<Long, List<TimeSlot>> createClassroomTimeSlots(LocalDateTime weekStart) {
        Map<Long, List<TimeSlot>> classroomTimeSlots = new HashMap<>();

        for (Classroom classroom : classrooms) {
            List<TimeSlot> slots = new ArrayList<>();
            for (int day = 0; day < 5; day++) {
                LocalDateTime currentDay = weekStart.plusDays(day);
                for (int slot = 0; slot < LESSONS_PER_DAY; slot++) {
                    TimeSlot timeSlot = createTimeSlot(currentDay, slot);
                    if (timeSlot != null) {
                        slots.add(timeSlot);
                    }
                }
            }
            classroomTimeSlots.put(classroom.getId(), slots);
        }

        return classroomTimeSlots;
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

    private List<SubjectTeacherPair> findPossibleSubjectTeacherPairs(TimeSlot timeSlot) {
        List<SubjectTeacherPair> pairs = new ArrayList<>();

        // Get all available teachers for this time slot
        List<Teacher> availableTeachers = findAvailableTeachers(timeSlot);

        for (Teacher teacher : availableTeachers) {
            Set<Long> teacherSubjects = teacherSubjectMap.get(teacher.getId());
            if (teacherSubjects != null) {
                for (Long subjectId : teacherSubjects) {
                    Subject subject = subjectById.get(subjectId);
                    if (subject != null) {
                        pairs.add(new SubjectTeacherPair(subject, teacher));
                    }
                }
            }
        }

        return pairs;
    }

    private List<Student> findAvailableStudents(Subject subject, TimeSlot timeSlot) {
        return students.stream()
                .filter(student -> {
                    // Check if student needs this subject
                    Map<Long, Integer> requirements = studentSubjectRequirements.get(student.getId());
                    if (requirements == null || requirements.get(subject.getId()) == null
                            || requirements.get(subject.getId()) <= 0) {
                        return false;
                    }

                    // Check if student's level matches subject level
                    if (!student.getLevel().equals(subject.getLevel())) {
                        return false;
                    }

                    // Check if student is available at this time
                    List<TimeSlot> studentTimeSlots = schedule.stream()
                            .filter(lesson -> lesson.getStudentIds().contains(student.getId()))
                            .map(lesson -> new TimeSlot(lesson.getDateStart(), lesson.getDateEnd()))
                            .toList();

                    if (studentTimeSlots.stream().anyMatch(timeSlot::overlaps)) {
                        return false;
                    }

                    // Check if student has too many consecutive lessons
                    return !hasTooManyConsecutiveLessons(student, subject.getId());
                })
                .collect(Collectors.toList());
    }

    private List<Teacher> findAvailableTeachers(TimeSlot timeSlot) {
        return teachers.stream()
                .filter(teacher -> {
                    // Check if teacher is available at this time
                    List<TimeSlot> teacherTimeSlots = schedule.stream()
                            .filter(lesson -> lesson.getTeacherId().equals(teacher.getId()))
                            .map(lesson -> new TimeSlot(lesson.getDateStart(), lesson.getDateEnd()))
                            .toList();

                    if (teacherTimeSlots.stream().anyMatch(timeSlot::overlaps)) {
                        return false;
                    }

                    // Check if the time is within teacher's working hours
                    LocalTime teacherStart = teacher.getTimeStart();
                    LocalTime teacherEnd = teacher.getTimeEnd();
                    LocalTime lessonStart = timeSlot.getDateStart().toLocalTime();
                    LocalTime lessonEnd = timeSlot.getDateEnd().toLocalTime();

                    return !lessonStart.isBefore(teacherStart) && !lessonEnd.isAfter(teacherEnd);
                })
                .collect(Collectors.toList());
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

    private ScheduledLesson buildScheduledLesson(TimeSlot timeSlot, Classroom classroom, Teacher teacher,
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
        lesson.setSubjectLevel(subject.getLevel());
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
                    if (consecutiveLessons >= 4) { // Maximum 4 consecutive lessons
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

    private record SubjectTeacherPair(Subject subject, Teacher teacher) {
    }
} 