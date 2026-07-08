package emd.charitymanagementsystem.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Years {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer yearValue;

    @OneToOne(mappedBy = "year", cascade = CascadeType.ALL)
    private Budget budget;

    @OneToMany(mappedBy = "year", cascade = CascadeType.ALL)
    private Set<Project> projects = new HashSet<>();

    @OneToMany(mappedBy = "year", cascade = CascadeType.ALL)
    private Set<Donation> donations = new HashSet<>();

    @OneToMany(mappedBy = "year", cascade = CascadeType.ALL)
    private Set<Member> members = new HashSet<>();

    @OneToMany(mappedBy = "year", cascade = CascadeType.ALL)
    private Set<Event> events = new HashSet<>();
}