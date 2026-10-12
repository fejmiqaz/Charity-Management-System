package emd.charitymanagementsystem.Config;

import emd.charitymanagementsystem.Service.Implementation.ActiveSessionService;
import org.springframework.context.annotation.*;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;

@Configuration
public class ActivitySecurityConfig {
    @Bean public ServletListenerRegistrationBean<ActiveSessionService> activeSessionListener(ActiveSessionService sessions) {
        return new ServletListenerRegistrationBean<>(sessions);
    }
}
