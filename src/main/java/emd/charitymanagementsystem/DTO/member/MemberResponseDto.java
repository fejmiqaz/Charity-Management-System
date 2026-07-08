package emd.charitymanagementsystem.DTO.member;


import emd.charitymanagementsystem.Models.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponseDto {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private String country;
    private String city;
    private Role role;

    private Long yearId;
    private Integer yearValue;

}
