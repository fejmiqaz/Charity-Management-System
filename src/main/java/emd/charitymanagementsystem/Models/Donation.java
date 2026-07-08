package emd.charitymanagementsystem.Models;

import emd.charitymanagementsystem.DTO.member.MemberResponseDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double donationAmount;

    @ManyToOne
    @JoinColumn(name = "year_id")
    private Years year;

    @ManyToMany
    @JoinTable(
            name = "donation_members",
            joinColumns = @JoinColumn(name = "donation_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private Set<Member> members = new HashSet<>();
}
