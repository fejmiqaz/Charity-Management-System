package emd.charitymanagementsystem.DTO.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationDto {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Surname is required")
    @Size(max = 100)
    private String surname;

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    private String country;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 254, message = "Email must contain at most 254 characters")
    @Pattern(regexp = "^[^\\s@]+@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?$",
            message = "Enter a valid email address, such as name@example.com")
    private String email;

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    @NotBlank
    @Size(max = 30)
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must contain between 8 and 128 characters")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    @Size(max = 128)
    private String confirmPassword;
}
