package emd.charitymanagementsystem.Config;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@AllArgsConstructor
public class DataInitializer {

    private final YearsRepository yearsRepository;
    private final BudgetRepository budgetRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final DonationRepository donationRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        if (yearsRepository.count() > 0) {
            return;
        }

        // =========================
        // YEARS
        // =========================
        Years y2022 = new Years();
        y2022.setYearValue(2022);

        Years y2023 = new Years();
        y2023.setYearValue(2023);

        Years y2024 = new Years();
        y2024.setYearValue(2024);

        Years y2025 = new Years();
        y2025.setYearValue(2025);

        Years y2026 = new Years();
        y2026.setYearValue(2026);

        yearsRepository.saveAll(List.of(y2022, y2023, y2024, y2025, y2026));

        // =========================
        // BUDGETS
        // =========================
        Budget b2022 = new Budget(null, 8500.0, "Initial charity budget for 2022", List.of(), y2022, List.of());
        Budget b2023 = new Budget(null, 12000.0, "Charity budget for 2023", List.of(), y2023, List.of());
        Budget b2024 = new Budget(null, 15500.0, "Charity budget for 2024", List.of(), y2024, List.of());
        Budget b2025 = new Budget(null, 18000.0, "Operational charity budget for 2025", List.of(), y2025, List.of());
        Budget b2026 = new Budget(null, 22000.0, "Projected charity budget for 2026", List.of(), y2026, List.of());

        budgetRepository.saveAll(List.of(b2022, b2023, b2024, b2025, b2026));

        y2022.setBudget(b2022);
        y2023.setBudget(b2023);
        y2024.setBudget(b2024);
        y2025.setBudget(b2025);
        y2026.setBudget(b2026);

        yearsRepository.saveAll(List.of(y2022, y2023, y2024, y2025, y2026));

        // =========================
        // MEMBERS (30)
        // =========================
        Member m1 = createMember("Arben", "Hoxha", "arben.hoxha@gmail.com", "071111111", "North Macedonia", "Skopje", Role.HEAD, y2022);
        Member m2 = createMember("Flamur", "Limani", "flamur.limani@gmail.com", "071111112", "Kosovo", "Prishtina", Role.SUBHEAD, y2022);
        Member m3 = createMember("Tuana", "Abush", "tuana.abush@gmail.com", "071111113", "North Macedonia", "Tetovo", Role.TREASURER, y2022);
        Member m4 = createMember("Fejmi", "Qazimi", "fejmi.qazimi@gmail.com", "071111114", "North Macedonia", "Kumanovo", Role.PROJECT_MANAGER, y2022);
        Member m5 = createMember("Elira", "Berisha", "elira.berisha@gmail.com", "071111115", "Kosovo", "Gjilan", Role.EVENT_MANAGER, y2022);
        Member m6 = createMember("Arian", "Hasani", "arian.hasani@gmail.com", "071111116", "Albania", "Tirana", Role.VOLUNTEER, y2022);

        Member m7 = createMember("Leutrim", "Selimi", "leutrim.selimi@gmail.com", "071111117", "North Macedonia", "Gostivar", Role.MEMBER, y2023);
        Member m8 = createMember("Besa", "Rexhepi", "besa.rexhepi@gmail.com", "071111118", "North Macedonia", "Skopje", Role.DONOR, y2023);
        Member m9 = createMember("Dren", "Osmani", "dren.osmani@gmail.com", "071111119", "Kosovo", "Prizren", Role.SPONSOR, y2023);
        Member m10 = createMember("Sara", "Imeri", "sara.imeri@gmail.com", "071111120", "North Macedonia", "Struga", Role.VOLUNTEER, y2023);
        Member m11 = createMember("Blend", "Shala", "blend.shala@gmail.com", "071111121", "Kosovo", "Peja", Role.MEMBER, y2023);
        Member m12 = createMember("Linda", "Ahmeti", "linda.ahmeti@gmail.com", "071111122", "North Macedonia", "Kicevo", Role.DONOR, y2023);

        Member m13 = createMember("Albin", "Krasniqi", "albin.krasniqi@gmail.com", "071111123", "Kosovo", "Ferizaj", Role.PROJECT_MANAGER, y2024);
        Member m14 = createMember("Megi", "Ismaili", "megi.ismaili@gmail.com", "071111124", "North Macedonia", "Bitola", Role.EVENT_MANAGER, y2024);
        Member m15 = createMember("Valon", "Murat", "valon.murat@gmail.com", "071111125", "North Macedonia", "Skopje", Role.VOLUNTEER, y2024);
        Member m16 = createMember("Anisa", "Ramadani", "anisa.ramadani@gmail.com", "071111126", "North Macedonia", "Kumanovo", Role.MEMBER, y2024);
        Member m17 = createMember("Besart", "Aliu", "besart.aliu@gmail.com", "071111127", "Kosovo", "Mitrovica", Role.DONOR, y2024);
        Member m18 = createMember("Gentiana", "Dema", "gentiana.dema@gmail.com", "071111128", "Albania", "Durres", Role.SPONSOR, y2024);

        Member m19 = createMember("Ermal", "Saiti", "ermal.saiti@gmail.com", "071111129", "North Macedonia", "Tetovo", Role.TREASURER, y2025);
        Member m20 = createMember("Rina", "Haziri", "rina.haziri@gmail.com", "071111130", "Kosovo", "Prishtina", Role.VOLUNTEER, y2025);
        Member m21 = createMember("Artan", "Nimani", "artan.nimani@gmail.com", "071111131", "North Macedonia", "Skopje", Role.MEMBER, y2025);
        Member m22 = createMember("Yllka", "Mustafa", "yllka.mustafa@gmail.com", "071111132", "North Macedonia", "Ohrid", Role.DONOR, y2025);
        Member m23 = createMember("Kushtrim", "Zeqiri", "kushtrim.zeqiri@gmail.com", "071111133", "North Macedonia", "Struga", Role.PROJECT_MANAGER, y2025);
        Member m24 = createMember("Blerta", "Sejdiu", "blerta.sejdiu@gmail.com", "071111134", "Kosovo", "Gjakova", Role.EVENT_MANAGER, y2025);

        Member m25 = createMember("Drilon", "Bajrami", "drilon.bajrami@gmail.com", "071111135", "North Macedonia", "Skopje", Role.SUBHEAD, y2026);
        Member m26 = createMember("Vesa", "Azemi", "vesa.azemi@gmail.com", "071111136", "Kosovo", "Peja", Role.VOLUNTEER, y2026);
        Member m27 = createMember("Shkendije", "Beqiri", "shkendije.beqiri@gmail.com", "071111137", "North Macedonia", "Tetovo", Role.MEMBER, y2026);
        Member m28 = createMember("Luan", "Demiri", "luan.demiri@gmail.com", "071111138", "North Macedonia", "Gostivar", Role.DONOR, y2026);
        Member m29 = createMember("Arta", "Pllana", "arta.pllana@gmail.com", "071111139", "Kosovo", "Prizren", Role.SPONSOR, y2026);
        Member m30 = createMember("Nora", "Sadiku", "nora.sadiku@gmail.com", "071111140", "NorthMacedonia", "Kumanovo", Role.VOLUNTEER, y2026);

        memberRepository.saveAll(List.of(
                m1, m2, m3, m4, m5, m6,
                m7, m8, m9, m10, m11, m12,
                m13, m14, m15, m16, m17, m18,
                m19, m20, m21, m22, m23, m24,
                m25, m26, m27, m28, m29, m30
        ));

        // =========================
        // PROJECTS (15)
        // =========================
        Project p1 = createProject("Winter Food Support", "Food package distribution for low-income families", LocalDate.of(2022, 1, 15), ProjectStatus.FINISHED, y2022, 1200.0, Set.of(m1, m5, m6));
        Project p2 = createProject("School Supplies Drive", "Providing notebooks and backpacks for children", LocalDate.of(2022, 3, 1), ProjectStatus.FINISHED, y2022, 950.0, Set.of(m2, m4, m6));
        Project p3 = createProject("Community Health Aid", "Support for families with medical supply needs", LocalDate.of(2022, 5, 20), ProjectStatus.CANCELLED, y2022, 1800.0, Set.of(m3, m5));

        Project p4 = createProject("Ramadan Family Boxes", "Preparing and delivering food boxes", LocalDate.of(2023, 3, 10), ProjectStatus.FINISHED, y2023, 2200.0, Set.of(m7, m8, m10));
        Project p5 = createProject("Back to School Help", "School support for children in rural areas", LocalDate.of(2023, 8, 12), ProjectStatus.ONGOING, y2023, 1700.0, Set.of(m7, m11, m12));
        Project p6 = createProject("Emergency Rent Relief", "Temporary housing support for families in crisis", LocalDate.of(2023, 10, 1), ProjectStatus.ON_HOLD, y2023, 2500.0, Set.of(m8, m9));

        Project p7 = createProject("Medical Equipment Donation", "Fundraising for wheelchairs and hospital support", LocalDate.of(2024, 2, 18), ProjectStatus.APPROVED, y2024, 3000.0, Set.of(m13, m14, m17));
        Project p8 = createProject("Village Water Access", "Helping improve access to clean water", LocalDate.of(2024, 4, 5), ProjectStatus.ONGOING, y2024, 4200.0, Set.of(m13, m15, m18));
        Project p9 = createProject("Youth Scholarship Fund", "Small scholarships for excellent students", LocalDate.of(2024, 9, 1), ProjectStatus.PLANNED, y2024, 2000.0, Set.of(m14, m16, m17));

        Project p10 = createProject("Community Kitchen Support", "Monthly support for food preparation efforts", LocalDate.of(2025, 1, 11), ProjectStatus.ONGOING, y2025, 3600.0, Set.of(m19, m20, m21));
        Project p11 = createProject("Flood Recovery Assistance", "Aid for households affected by flooding", LocalDate.of(2025, 3, 27), ProjectStatus.FINISHED, y2025, 4100.0, Set.of(m19, m22, m23));
        Project p12 = createProject("Elderly Care Visits", "Regular visits and supplies for elderly citizens", LocalDate.of(2025, 6, 15), ProjectStatus.APPROVED, y2025, 1600.0, Set.of(m20, m24));

        Project p13 = createProject("Student Technology Grants", "Helping students get devices for learning", LocalDate.of(2026, 2, 5), ProjectStatus.PLANNED, y2026, 5000.0, Set.of(m25, m26, m28));
        Project p14 = createProject("Orphan Support Initiative", "Educational and social support for orphaned children", LocalDate.of(2026, 4, 20), ProjectStatus.APPROVED, y2026, 4700.0, Set.of(m25, m27, m29));
        Project p15 = createProject("Neighborhood Cleanup Campaign", "Volunteer environmental cleanup campaign", LocalDate.of(2026, 5, 30), ProjectStatus.ONGOING, y2026, 1300.0, Set.of(m26, m27, m30));

        projectRepository.saveAll(List.of(
                p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15
        ));

        // =========================
        // DONATIONS (40)
        // =========================
        Donation d1 = createDonation(250.0, y2022, Set.of(m1));
        Donation d2 = createDonation(180.0, y2022, Set.of(m2));
        Donation d3 = createDonation(320.0, y2022, Set.of(m3));
        Donation d4 = createDonation(150.0, y2022, Set.of(m5));
        Donation d5 = createDonation(500.0, y2022, Set.of(m1, m2));
        Donation d6 = createDonation(275.0, y2022, Set.of(m4));
        Donation d7 = createDonation(220.0, y2022, Set.of(m6));
        Donation d8 = createDonation(410.0, y2022, Set.of(m3, m5));

        Donation d9 = createDonation(300.0, y2023, Set.of(m8));
        Donation d10 = createDonation(650.0, y2023, Set.of(m9));
        Donation d11 = createDonation(190.0, y2023, Set.of(m10));
        Donation d12 = createDonation(240.0, y2023, Set.of(m11));
        Donation d13 = createDonation(550.0, y2023, Set.of(m12));
        Donation d14 = createDonation(700.0, y2023, Set.of(m8, m9));
        Donation d15 = createDonation(330.0, y2023, Set.of(m7));
        Donation d16 = createDonation(260.0, y2023, Set.of(m10, m11));

        Donation d17 = createDonation(1000.0, y2024, Set.of(m18));
        Donation d18 = createDonation(430.0, y2024, Set.of(m17));
        Donation d19 = createDonation(280.0, y2024, Set.of(m13));
        Donation d20 = createDonation(350.0, y2024, Set.of(m14));
        Donation d21 = createDonation(600.0, y2024, Set.of(m15, m16));
        Donation d22 = createDonation(450.0, y2024, Set.of(m17, m18));
        Donation d23 = createDonation(390.0, y2024, Set.of(m16));
        Donation d24 = createDonation(510.0, y2024, Set.of(m13, m14));

        Donation d25 = createDonation(700.0, y2025, Set.of(m22));
        Donation d26 = createDonation(950.0, y2025, Set.of(m23));
        Donation d27 = createDonation(260.0, y2025, Set.of(m20));
        Donation d28 = createDonation(330.0, y2025, Set.of(m21));
        Donation d29 = createDonation(470.0, y2025, Set.of(m19));
        Donation d30 = createDonation(1200.0, y2025, Set.of(m22, m23));
        Donation d31 = createDonation(390.0, y2025, Set.of(m24));
        Donation d32 = createDonation(440.0, y2025, Set.of(m20, m21));

        Donation d33 = createDonation(1300.0, y2026, Set.of(m29));
        Donation d34 = createDonation(620.0, y2026, Set.of(m28));
        Donation d35 = createDonation(310.0, y2026, Set.of(m26));
        Donation d36 = createDonation(270.0, y2026, Set.of(m27));
        Donation d37 = createDonation(480.0, y2026, Set.of(m25));
        Donation d38 = createDonation(850.0, y2026, Set.of(m28, m29));
        Donation d39 = createDonation(360.0, y2026, Set.of(m30));
        Donation d40 = createDonation(540.0, y2026, Set.of(m26, m27));

        donationRepository.saveAll(List.of(
                d1, d2, d3, d4, d5, d6, d7, d8,
                d9, d10, d11, d12, d13, d14, d15, d16,
                d17, d18, d19, d20, d21, d22, d23, d24,
                d25, d26, d27, d28, d29, d30, d31, d32,
                d33, d34, d35, d36, d37, d38, d39, d40
        ));

        // =========================
        // EVENTS (10)
        // =========================
        Event e1 = createEvent("Annual leadership planning meeting", LocalDateTime.of(2022, 1, 10, 11, 0), List.of(m1, m2, m3, m4, m5), y2022);
        Event e2 = createEvent("Winter donation packaging event", LocalDateTime.of(2022, 12, 17, 14, 0), List.of(m4, m5, m6), y2022);

        Event e3 = createEvent("Spring fundraising evening", LocalDateTime.of(2023, 4, 7, 18, 30), List.of(m7, m8, m9, m10), y2023);
        Event e4 = createEvent("Volunteer coordination workshop", LocalDateTime.of(2023, 9, 15, 12, 0), List.of(m10, m11, m12), y2023);

        Event e5 = createEvent("Medical aid campaign kickoff", LocalDateTime.of(2024, 2, 25, 10, 0), List.of(m13, m14, m17, m18), y2024);
        Event e6 = createEvent("Community outreach meeting", LocalDateTime.of(2024, 11, 3, 16, 0), List.of(m15, m16, m17), y2024);

        Event e7 = createEvent("Flood recovery field briefing", LocalDateTime.of(2025, 3, 28, 9, 30), List.of(m19, m20, m22, m23), y2025);
        Event e8 = createEvent("Mid-year charity review", LocalDateTime.of(2025, 7, 20, 13, 0), List.of(m19, m21, m24), y2025);

        Event e9 = createEvent("Technology grants preparation", LocalDateTime.of(2026, 2, 18, 15, 0), List.of(m25, m26, m28), y2026);
        Event e10 = createEvent("Volunteer neighborhood action day", LocalDateTime.of(2026, 6, 12, 8, 30), List.of(m26, m27, m30), y2026);

        eventRepository.saveAll(List.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10));

        // =========================
        // LINK YEAR SETS
        // =========================
        y2022.getMembers().addAll(Set.of(m1, m2, m3, m4, m5, m6));
        y2023.getMembers().addAll(Set.of(m7, m8, m9, m10, m11, m12));
        y2024.getMembers().addAll(Set.of(m13, m14, m15, m16, m17, m18));
        y2025.getMembers().addAll(Set.of(m19, m20, m21, m22, m23, m24));
        y2026.getMembers().addAll(Set.of(m25, m26, m27, m28, m29, m30));

        y2022.getProjects().addAll(Set.of(p1, p2, p3));
        y2023.getProjects().addAll(Set.of(p4, p5, p6));
        y2024.getProjects().addAll(Set.of(p7, p8, p9));
        y2025.getProjects().addAll(Set.of(p10, p11, p12));
        y2026.getProjects().addAll(Set.of(p13, p14, p15));

        y2022.getDonations().addAll(Set.of(d1, d2, d3, d4, d5, d6, d7, d8));
        y2023.getDonations().addAll(Set.of(d9, d10, d11, d12, d13, d14, d15, d16));
        y2024.getDonations().addAll(Set.of(d17, d18, d19, d20, d21, d22, d23, d24));
        y2025.getDonations().addAll(Set.of(d25, d26, d27, d28, d29, d30, d31, d32));
        y2026.getDonations().addAll(Set.of(d33, d34, d35, d36, d37, d38, d39, d40));

        y2022.getEvents().addAll(Set.of(e1, e2));
        y2023.getEvents().addAll(Set.of(e3, e4));
        y2024.getEvents().addAll(Set.of(e5, e6));
        y2025.getEvents().addAll(Set.of(e7, e8));
        y2026.getEvents().addAll(Set.of(e9, e10));

        yearsRepository.saveAll(List.of(y2022, y2023, y2024, y2025, y2026));
    }

    private Member createMember(String name, String surname, String email, String phone,
                                String country, String city, Role role, Years year) {
        Member member = new Member();
        member.setName(name);
        member.setSurname(surname);
        member.setEmail(email);
        member.setPassword(passwordEncoder.encode("123456"));
        member.setPhone(phone);
        member.setCountry(country);
        member.setCity(city);
        member.setRole(role);
        member.setYear(year);
        member.setDonations(new HashSet<>());
        member.setProjects(new HashSet<>());
        return member;
    }

    private Project createProject(String name, String description, LocalDate dateCreated,
                                  ProjectStatus status, Years year, Double projectPrice,
                                  Set<Member> members) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setDateCreated(dateCreated);
        project.setStatus(status);
        project.setYear(year);
        project.setProjectPrice(projectPrice);
        project.setMembers(members);
        return project;
    }

    private Donation createDonation(Double amount, Years year, Set<Member> members) {
        Donation donation = new Donation();
        donation.setDonationAmount(amount);
        donation.setYear(year);
        donation.setMembers(members);
        return donation;
    }

    private Event createEvent(String purpose, LocalDateTime date, List<Member> members, Years year) {
        Event event = new Event();
        event.setPurpose(purpose);
        event.setDate(date);
        event.setMembers(members);
        event.setYear(year);
        return event;
    }
}
