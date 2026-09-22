package emd.charitymanagementsystem.Models;

import java.time.LocalDateTime;

public enum EventStatus {
    UPCOMING, PAST;

    public static EventStatus fromDate(LocalDateTime date, LocalDateTime now) {
        return date == null ? null : (date.isBefore(now) ? PAST : UPCOMING);
    }
}
