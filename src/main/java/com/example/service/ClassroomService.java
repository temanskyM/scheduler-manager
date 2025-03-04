package com.example.service;

import com.example.db.Classroom;
import com.example.db.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClassroomService {
    
    private final ClassroomRepository classroomRepository;
    
    public List<Classroom> findAll() {
        return classroomRepository.findAll();
    }
    
    public List<Classroom> findAllById(List<Long> ids) {
        return classroomRepository.findAllById(ids);
    }
    
    public Optional<Classroom> findById(Long id) {
        return classroomRepository.findById(id);
    }
    
    public Classroom save(Classroom classroom) {
        return classroomRepository.save(classroom);
    }
    
    public void deleteById(Long id) {
        classroomRepository.deleteById(id);
    }
    
    public Classroom getReferenceById(Long id) {
        return classroomRepository.getReferenceById(id);
    }
} 