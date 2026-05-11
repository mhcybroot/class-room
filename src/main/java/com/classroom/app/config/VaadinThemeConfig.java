package com.classroom.app.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

@Theme("classroomapp")
@PWA(name = "ClassRoom", shortName = "ClassRoom")
public class VaadinThemeConfig implements AppShellConfigurator {
}
