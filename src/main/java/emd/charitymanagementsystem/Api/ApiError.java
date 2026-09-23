package emd.charitymanagementsystem.Api;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Map;

public record ApiError(int status, String error, String message, Map<String, String> fields) {
    public static void write(HttpServletResponse response, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String reason = HttpStatus.valueOf(status).getReasonPhrase();
        response.getWriter().write("{\"status\":" + status + ",\"error\":\"" + reason
                + "\",\"message\":\"" + reason + "\",\"fields\":{}}");
    }
}
