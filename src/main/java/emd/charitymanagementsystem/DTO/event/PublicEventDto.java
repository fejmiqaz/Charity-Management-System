package emd.charitymanagementsystem.DTO.event;

import java.time.LocalDateTime;

/** Publicly approved purpose and schedule only; participant data stays internal. */
public record PublicEventDto(String purpose, LocalDateTime date) {}
