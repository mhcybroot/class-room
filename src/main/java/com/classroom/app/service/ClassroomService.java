package com.classroom.app.service;

import com.classroom.app.domain.Classroom;
import com.classroom.app.domain.Teacher;
import com.classroom.app.repo.ClassroomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassroomService {
    private final ClassroomRepository classroomRepository;

    public ClassroomService(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    public Classroom create(Teacher teacher, String name, String subject) {
        if (teacher == null) {
            throw new IllegalArgumentException("Teacher is required");
        }
        String normalizedName = name == null ? "" : name.trim();
        String normalizedSubject = subject == null ? "" : subject.trim();
        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException("Class name is required");
        }
        if (normalizedSubject.isBlank()) {
            throw new IllegalArgumentException("Subject is required");
        }
        Classroom c = new Classroom();
        c.setTeacher(teacher);
        c.setName(normalizedName);
        c.setSubject(normalizedSubject);
        return classroomRepository.save(c);
    }

    public List<Classroom> byTeacher(Teacher teacher) {
        return classroomRepository.findByTeacher(teacher);
    }

    public void deleteForTeacher(Long classroomId, Teacher teacher) {
        classroomRepository.findById(classroomId)
                .filter(c -> c.getTeacher().getId().equals(teacher.getId()))
                .ifPresent(classroomRepository::delete);
    }
}
