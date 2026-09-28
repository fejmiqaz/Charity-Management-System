package emd.charitymanagementsystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "app.frontend.react=true",
        "spring.datasource.url=jdbc:h2:mem:react-tests", "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.admin.email=test@example.com", "app.admin.password=TestOnly123!", "logging.file.name=target/react-tests.log",
        "app.notifications.scheduling-enabled=false", "app.notifications.email-enabled=false"
})
class ReactFrontendTests {
    @Autowired WebApplicationContext context;
    @Autowired SimpleUrlHandlerMapping reactPageMapping;
    MockMvc mvc;

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test void publicPagesServeReactWithoutAuthentication() throws Exception {
        for (String path : new String[]{"/", "/login", "/register", "/access-denied"}) {
            mvc.perform(get(path)).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
        }
    }

    @Test void everyReactRouteSupportsDirectNavigation() throws Exception {
        String app = java.nio.file.Files.readString(java.nio.file.Path.of("frontend/src/App.jsx"));
        var routes = java.util.regex.Pattern.compile("path=\"([^\"]+)\"").matcher(app);
        while (routes.find()) {
            if (routes.group(1).equals("*")) continue;
            String path = routes.group(1).replaceAll(":[a-zA-Z]+", "1");
            mvc.perform(get(path).with(user("head").roles("HEAD")))
                    .andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
        }
    }

    @Test void apiAndDownloadRequestsBypassFrontendMapping() throws Exception {
        for (String path : new String[]{"/api/public/home", "/api/years", "/assets/index.js", "/index.html",
                "/members/export.xlsx", "/memberships/export.pdf", "/years/1/export.xlsx",
                "/years/1/projects/export.xlsx", "/years/1/events/2/export.xlsx", "/pdf/years/1"}) {
            assertNull(reactPageMapping.getHandler(new MockHttpServletRequest("GET", path)), path);
        }
        assertNull(reactPageMapping.getHandler(new MockHttpServletRequest("POST", "/register")));
        mvc.perform(get("/api/public/home")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.projects").isArray());
        mvc.perform(get("/members/export.xlsx").with(user("head").roles("HEAD")))
                .andExpect(status().isOk()).andExpect(header().exists("Content-Disposition"));
    }

    @Test void securityStillProtectsDataAndWrites() throws Exception {
        mvc.perform(get("/dashboard")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/api/years")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/years").with(user("head").roles("HEAD")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/years").with(user("donor").roles("DONOR")))
                .andExpect(redirectedUrl("/access-denied"));
        // Missing assets should be 404, never a redirect to login or a React HTML response.
        mvc.perform(get("/assets/nonexistent.js")).andExpect(status().isNotFound());
    }
}
