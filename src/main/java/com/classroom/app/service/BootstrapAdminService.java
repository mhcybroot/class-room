package com.classroom.app.service;

import com.classroom.app.config.BootstrapAdminProperties;
import com.classroom.app.domain.Teacher;
import com.classroom.app.repo.TeacherRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BootstrapAdminService implements CommandLineRunner {
    private final TeacherRepository teacherRepository;
    private final BootstrapAdminProperties props;
    private final PasswordEncoder passwordEncoder;

    public BootstrapAdminService(TeacherRepository teacherRepository, BootstrapAdminProperties props, PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.props = props;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (props.email() == null || props.email().isBlank() || props.password() == null || props.password().isBlank()) {
            return;
        }
        teacherRepository.findByEmail(props.email()).orElseGet(() -> {
            Teacher t = new Teacher();
            t.setEmail(props.email());
            t.setPasswordHash(passwordEncoder.encode(props.password()));
            t.setFullName(props.fullName() == null || props.fullName().isBlank() ? "Admin Teacher" : props.fullName());
            t.setRole("TEACHER");
            return teacherRepository.save(t);
        });
    }
}
