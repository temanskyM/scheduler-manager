package com.example.service;

import com.example.db.Subject;
import com.example.db.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {
    
    @Autowired
    private SubjectRepository subjectRepository;
    
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
} 