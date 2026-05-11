package com.classroom.app.service;

import com.classroom.app.domain.Teacher;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class TeacherPrincipal implements UserDetails {
    private final Teacher teacher;

    public TeacherPrincipal(Teacher teacher) {
        this.teacher = teacher;
    }

    public Teacher teacher() {
        return teacher;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + teacher.getRole()));
    }

    @Override
    public String getPassword() {
        return teacher.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return teacher.getEmail();
    }
}
