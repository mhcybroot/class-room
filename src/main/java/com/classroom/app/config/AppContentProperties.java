package com.classroom.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.content")
public record AppContentProperties(String aboutTitle,
                                   String aboutBody,
                                   String contactTitle,
                                   String contactBody,
                                   String supportEmail,
                                   String supportPhone) {
}
