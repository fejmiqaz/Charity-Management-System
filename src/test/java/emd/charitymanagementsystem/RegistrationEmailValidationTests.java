package emd.charitymanagementsystem;

import emd.charitymanagementsystem.DTO.auth.RegistrationDto;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationEmailValidationTests {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @AfterAll
    static void closeValidatorFactory() {
        FACTORY.close();
    }

    private boolean validEmail(String email) {
        RegistrationDto dto = new RegistrationDto();
        dto.setEmail(email);
        return VALIDATOR.validateProperty(dto, "email").isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"person@example.com", "First.Last+charity@example.co.uk",
            "person@sub.example.org", "person@my-charity.museum", " PERSON@EXAMPLE.COM "})
    void acceptsNormalAddresses(String email) {
        assertTrue(validEmail(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "person", "person@gmail", "@example.com",
            "person@@example.com", "first last@example.com", "person@example..com",
            "person@-example.com", "person@example-.com", "person@exam_ple.com",
            "person@example.com.", ".person@example.com", "person..name@example.com",
            "person@127.0.0.1", "person\n@example.com"})
    void rejectsMalformedAddresses(String email) {
        assertFalse(validEmail(email));
    }

    @Test
    void rejectsOversizedAddressAndLocalPart() {
        assertFalse(validEmail("a".repeat(65) + "@example.com"));
        assertFalse(validEmail("a".repeat(64) + "@" + ("b".repeat(63) + ".").repeat(3) + "com"));
    }

    @Test
    void trimsSurroundingWhitespaceBeforeValidation() {
        RegistrationDto dto = new RegistrationDto();
        dto.setEmail("  person@example.com  ");
        assertEquals("person@example.com", dto.getEmail());
        assertTrue(VALIDATOR.validateProperty(dto, "email").isEmpty());
    }
}
