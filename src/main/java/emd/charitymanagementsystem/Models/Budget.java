package emd.charitymanagementsystem.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.aspectj.bridge.IMessage;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double budgetAmount;

    private String description;
    @OneToMany
    private List<Donation> donations;

    @OneToOne
    @JoinColumn(name = "year_id", unique=true)
    private Years year;

    @OneToMany
    private List<Member> members;

}
