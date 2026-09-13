package emd.charitymanagementsystem.DTO.profile;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProfileFormDto {
    @NotBlank @Size(min=2, max=50)
    private String name;
    @NotBlank @Size(min=2, max=50)
    private String surname;
    @NotBlank @Email(message="Enter a valid email address") @Size(max=254)
    @Pattern(regexp="^[^\\s@]+@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?$", message="Enter an email such as name@example.com")
    private String email;
    @NotBlank @Pattern(regexp="^[0-9+\\- ]{6,20}$", message="Enter a valid phone number")
    private String phone;
    @NotBlank @Size(min=2,max=100)
    private String country;
    @NotBlank @Size(min=2,max=100)
    private String city;

    public void setEmail(String email) { this.email = email == null ? null : email.trim(); }
}
