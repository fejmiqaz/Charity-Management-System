package emd.charitymanagementsystem.Service;

import emd.charitymanagementsystem.DTO.auth.RegistrationDto;
import emd.charitymanagementsystem.Models.Role;
import emd.charitymanagementsystem.Models.UserAccount;

import java.util.List;

public interface UserAccountService {

    UserAccount register(RegistrationDto registrationDto);

    List<UserAccount> findAll();

    UserAccount findById(Long id);

    void updateRole(Long userId, Role role);
}