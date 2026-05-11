package com.classroom.app.web.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Route("logout")
@AnonymousAllowed
public class LogoutView extends VerticalLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var request = ((VaadinServletRequest) VaadinRequest.getCurrent()).getHttpServletRequest();
        var response = ((VaadinServletResponse) VaadinResponse.getCurrent()).getHttpServletResponse();
        var auth = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, auth);
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        event.forwardTo("");
    }
}
