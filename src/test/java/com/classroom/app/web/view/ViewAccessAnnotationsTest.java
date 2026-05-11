package com.classroom.app.web.view;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.RolesAllowed;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ViewAccessAnnotationsTest {

    @Test
    void shouldKeepPublicViewsAnonymous() {
        assertTrue(HomeView.class.isAnnotationPresent(AnonymousAllowed.class));
        assertTrue(AboutView.class.isAnnotationPresent(AnonymousAllowed.class));
        assertTrue(ContactView.class.isAnnotationPresent(AnonymousAllowed.class));
        assertTrue(LoginView.class.isAnnotationPresent(AnonymousAllowed.class));
        assertTrue(StudentJoinView.class.isAnnotationPresent(AnonymousAllowed.class));
        assertTrue(LogoutView.class.isAnnotationPresent(AnonymousAllowed.class));
    }

    @Test
    void shouldProtectTeacherViewAndPermitSharedLiveView() {
        assertTrue(TeacherDashboardView.class.isAnnotationPresent(RolesAllowed.class));
        assertTrue(LiveRoomView.class.isAnnotationPresent(AnonymousAllowed.class));
    }
}
