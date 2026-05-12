package com.classroom.app.config;

import com.classroom.app.service.TeacherDetailsService;
import com.classroom.app.web.view.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig extends VaadinWebSecurity {

    private final TeacherDetailsService teacherDetailsService;

    public SecurityConfig(TeacherDetailsService teacherDetailsService) {
        this.teacherDetailsService = teacherDetailsService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);  // MUST be first — sets up Vaadin CSRF, request cache, VaadinSecurityContext
        setLoginView(http, LoginView.class);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.userDetailsService(teacherDetailsService);
        // CSRF is handled by Vaadin via super.configure(http) above — do NOT disable globally
        http.sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(1)
        );
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/rooms/**").hasRole("TEACHER")
                .requestMatchers("/**").permitAll()
        );
        http.formLogin(form -> form
                .defaultSuccessUrl("/teacher", true)
                .permitAll()
        );
        http.logout(logout -> logout.permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
