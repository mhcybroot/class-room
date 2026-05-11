package com.classroom.app.service;

import com.classroom.app.repo.TeacherRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TeacherDetailsService implements UserDetailsService {
    private final TeacherRepository teacherRepository;

    public TeacherDetailsService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return teacherRepository.findByEmail(username)
                .map(TeacherPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Teacher not found"));
    }
}
