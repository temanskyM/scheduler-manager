package com.example.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findFirstByNameAndLevel(String name, Integer level);

    List<Subject> findAllByLevel(int level);
}