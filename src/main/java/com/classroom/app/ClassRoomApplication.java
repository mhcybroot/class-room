package com.classroom.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ClassRoomApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClassRoomApplication.class, args);
    }
}
