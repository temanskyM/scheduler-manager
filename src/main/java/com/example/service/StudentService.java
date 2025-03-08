package com.example.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.example.db.Student;
import com.example.db.StudentRepository;
import com.example.db.Subject;
import com.example.db.SubjectRepository;
import com.example.service.student.StudentCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public List<Student> findAllById(List<Long> ids) {
        return studentRepository.findAllById(ids);
    }

    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    public Student save(Student student) {
        return studentRepository.save(student);
    }

    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }

    public Student getReferenceById(Long id) {
        return studentRepository.getReferenceById(id);
    }

    public void create(StudentCreateDto createDto) {
        Student entity = new Student();
        entity.setName(createDto.name());
        entity.setSurname(createDto.surname());
        entity.setLevel(createDto.level());

        Set<Subject> subjects = new HashSet<>();
        for (String subject : createDto.subjects()) {
            Optional<Subject> subjectOpt = subjectRepository.findFirstByNameAndLevel(subject, entity.getLevel());
            if (subjectOpt.isEmpty()) {
                throw new RuntimeException("Subject " + subject + "not found");
            }
            subjects.add(subjectOpt.get());
        }
        entity.setSubjects(subjects);

        studentRepository.save(entity);
    }
}