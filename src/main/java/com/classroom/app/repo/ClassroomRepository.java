package com.classroom.app.repo;

import com.classroom.app.domain.Classroom;
import com.classroom.app.domain.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    List<Classroom> findByTeacher(Teacher teacher);
}
