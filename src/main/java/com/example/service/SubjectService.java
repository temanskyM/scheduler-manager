package com.example.service;

import java.util.List;
import java.util.Optional;

import com.example.controller.dto.SubjectDto;
import com.example.db.Subject;
import com.example.db.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    public List<Subject> findAllById(List<Long> ids) {
        return subjectRepository.findAllById(ids);
    }

    public Optional<Subject> findById(Long id) {
        return subjectRepository.findById(id);
    }

    public Subject save(Subject subject) {
        return subjectRepository.save(subject);
    }

    public void deleteById(Long id) {
        subjectRepository.deleteById(id);
    }

    public Subject getReferenceById(Long id) {
        return subjectRepository.getReferenceById(id);
    }

    public List<SubjectDto> findAllByLevel(int level) {
        return subjectRepository.findAllByLevel(level).stream()
                .map(this::toModel)
                .toList();
    }

    public SubjectDto toModel(Subject subject) {
        return new SubjectDto(subject.getName(), subject.getLevel(), subject.getTotalCountPerWeek());
    }
}