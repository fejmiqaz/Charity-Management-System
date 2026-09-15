package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Config.RequestRateLimitFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RequestRateLimitFilterTests {
    @Test
    void limitsLoginAttemptsBeforeTheyReachAuthentication() throws Exception {
        var filter = new RequestRateLimitFilter();
        for (int i = 0; i < 10; i++) {
            var response = request(filter, "POST", "/login", "192.0.2.1");
            assertThat(response.getStatus()).isEqualTo(200);
        }
        var blocked = request(filter, "POST", "/login", "192.0.2.1");
        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getHeader("Retry-After")).isEqualTo("60");
        assertThat(request(filter, "POST", "/login", "192.0.2.2").getStatus()).isEqualTo(200);
    }

    @Test
    void limitsRegistrationAndWritesSeparately() throws Exception {
        var filter = new RequestRateLimitFilter();
        for (int i = 0; i < 5; i++) {
            assertThat(request(filter, "POST", "/register", "192.0.2.3").getStatus()).isEqualTo(200);
        }
        assertThat(request(filter, "POST", "/register", "192.0.2.3").getStatus()).isEqualTo(429);
        for (int i = 0; i < 60; i++) {
            assertThat(request(filter, "POST", "/years/add", "192.0.2.4").getStatus()).isEqualTo(200);
        }
        assertThat(request(filter, "POST", "/years/add", "192.0.2.4").getStatus()).isEqualTo(429);
    }

    @Test
    void limitsGetRequestsAndIgnoresUntrustedForwardedAddress() throws Exception {
        var filter = new RequestRateLimitFilter();
        for (int i = 0; i < 300; i++) {
            assertThat(request(filter, "GET", "/dashboard", "192.0.2.5").getStatus()).isEqualTo(200);
        }
        assertThat(request(filter, "GET", "/dashboard", "192.0.2.5").getStatus()).isEqualTo(429);
    }

    private MockHttpServletResponse request(RequestRateLimitFilter filter, String method, String path,
                                            String address) throws Exception {
        var request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr(address);
        request.addHeader("X-Forwarded-For", "203.0.113.99");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
