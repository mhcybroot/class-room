package com.classroom.app.config;

import com.classroom.app.service.TeacherDetailsService;
import com.classroom.app.web.view.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig extends VaadinWebSecurity {

    private final TeacherDetailsService teacherDetailsService;

    public SecurityConfig(TeacherDetailsService teacherDetailsService) {
        this.teacherDetailsService = teacherDetailsService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.userDetailsService(teacherDetailsService);
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/livekit/token", "/api/livekit/leave", "/", "/about", "/contact", "/join/**", "/live/**", "/js/**").permitAll()
                .requestMatchers("/api/rooms/**").hasRole("TEACHER")
        );
        http.formLogin(form -> form.defaultSuccessUrl("/teacher", true));
        super.configure(http);
        setLoginView(http, LoginView.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
