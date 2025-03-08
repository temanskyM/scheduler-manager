package com.example.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.db.Subject;
import com.example.db.Teacher;
import com.example.db.TeacherRepository;
import com.example.service.teacher.TeacherCreateDto;
import com.example.service.teacher.TeacherSubject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public List<Teacher> findAll() {
        return teacherRepository.findAll();
    }

    public List<Teacher> findAllById(List<Long> ids) {
        return teacherRepository.findAllById(ids);
    }

    public Optional<Teacher> findById(Long id) {
        return teacherRepository.findById(id);
    }

    public Teacher save(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    public void deleteById(Long id) {
        teacherRepository.deleteById(id);
    }

    public Teacher getReferenceById(Long id) {
        return teacherRepository.getReferenceById(id);
    }

    public void create(TeacherCreateDto createDto) {
        Teacher entity = new Teacher();
        entity.setName(createDto.name());
        entity.setSurname(createDto.surname());
        entity.setPatronymic(createDto.patronymic());
        entity.setTimeStart(createDto.timeStart());
        entity.setTimeEnd(createDto.timeEnd());

        entity.setSubjects(createSubjects(createDto.subjects(), entity));
        teacherRepository.save(entity);
        return;
    }

    private Set<Subject> createSubjects(List<TeacherSubject> createDto, Teacher teacher) {
        HashSet<Teacher> teachers = new HashSet<>();
        teachers.add(teacher);

        return createDto.stream()
                .filter(it -> it.name() != null && it.name().isEmpty())
                .map(it -> Subject.builder()
                        .teachers(teachers)
                        .level(it.level())
                        .name(it.name())
                        .totalCountPerWeek(it.totalCountPerWeek()).build())
                .collect(Collectors.toSet());
    }
}