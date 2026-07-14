package emd.charitymanagementsystem.Web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(
            AccessDeniedException ex,
            Model model
    ) {
        log.warn("Access denied: {}", ex.getMessage());

        model.addAttribute(
                "errorMessage",
                "You don't have permission to perform this action."
        );

        return "error/access-denied";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(
            IllegalArgumentException ex,
            Model model
    ) {
        log.warn("Invalid operation: {}", ex.getMessage());

        model.addAttribute(
                "errorMessage",
                ex.getMessage()
        );

        return "error/general-error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(
            Exception ex,
            Model model
    ) {
        log.error("Unexpected application error", ex);

        model.addAttribute(
                "errorMessage",
                "An unexpected error occurred. Please try again."
        );

        return "error/general-error";
    }
}