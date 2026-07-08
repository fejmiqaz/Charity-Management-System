package emd.charitymanagementsystem.Models;

import emd.charitymanagementsystem.Models.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String purpose;

    private LocalDateTime date;

    @ManyToMany
    private List<Member> members;

    @ManyToOne
    private Years year;

}