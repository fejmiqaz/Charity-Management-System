package emd.charitymanagementsystem.Config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import java.util.LinkedHashMap;
import java.util.Map;

/** Serves the bundled frontend in Docker; local Vite/legacy development remains available. */
@Configuration
@ConditionalOnProperty(name = "app.frontend.react", havingValue = "true")
public class ReactFrontendConfig {
    @Bean
    public SimpleUrlHandlerMapping reactPageMapping() {
        var page = new ParameterizableViewController();
        page.setViewName("forward:/index.html");

        // Explicit page routes only. Numeric IDs avoid matching export.xlsx/export.pdf.
        Map<String, Object> routes = new LinkedHashMap<>();
        for (String route : new String[]{
                "/", "/login", "/register", "/access-denied", "/dashboard", "/home",
                "/years", "/years/new", "/years/add-form", "/years/{id:[0-9]+}",
                "/years/{id:[0-9]+}/edit", "/years/edit-form/{id:[0-9]+}",
                "/members", "/members/new", "/members/add", "/members/add-form",
                "/members/{id:[0-9]+}", "/members/{id:[0-9]+}/edit",
                "/memberships", "/profile", "/profile/edit", "/notifications", "/converter"
        }) {
            routes.put(route, page);
        }
        for (String section : new String[]{"projects", "donations", "events"}) {
            String base = "/years/{yearId:[0-9]+}/" + section;
            for (String suffix : new String[]{"", "/add", "/add-form", "/{id:[0-9]+}",
                    "/{id:[0-9]+}/edit", "/{id:[0-9]+}/edit-form"}) {
                routes.put(base + suffix, page);
            }
        }
        String budget = "/years/{yearId:[0-9]+}/budget";
        for (String suffix : new String[]{"", "/add", "/add-form", "/edit",
                "/edit-form/{id:[0-9]+}", "/details/{id:[0-9]+}"}) {
            routes.put(budget + suffix, page);
        }

        var mapping = new SimpleUrlHandlerMapping() {
            @Override
            protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
                // Leave legacy form submissions and all API mutations with their controllers.
                if (!"GET".equals(request.getMethod()) && !"HEAD".equals(request.getMethod())) {
                    return null;
                }
                return super.getHandlerInternal(request);
            }
        };
        // Spring Security still runs first; this takes precedence only over MVC page controllers.
        mapping.setOrder(-1);
        mapping.setUrlMap(routes);
        return mapping;
    }
}
