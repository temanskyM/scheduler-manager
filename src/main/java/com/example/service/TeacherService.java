package com.example.service;

import com.example.db.Teacher;
import com.example.db.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeacherService {
    
    @Autowired
    private TeacherRepository teacherRepository;
    
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
} 