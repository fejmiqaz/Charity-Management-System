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
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean publicVisible = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(32) default 'NORMAL'")
    private EventType eventType = EventType.NORMAL;

    private LocalDateTime date;

    @ManyToMany
    private List<Member> members;

    @ManyToOne
    private Years year;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventTask> tasks = new java.util.ArrayList<>();

}
