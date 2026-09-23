package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.Models.Event;
import emd.charitymanagementsystem.Models.EventTask;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class NotificationMessages {
    private NotificationMessages() {}

    static String assignment(EventTask task, String timeZone) {
        return "You have been assigned to \"" + task.getTitle() + "\" for " + task.getEvent().getPurpose()
                + ".\nEvent: " + date(task.getEvent().getDate(), timeZone)
                + (task.getDescription() == null || task.getDescription().isBlank() ? "" : "\nTask details: " + task.getDescription());
    }

    static String reminder(Event event, long days, String timeZone) {
        return event.getPurpose() + " is " + (days == 1 ? "tomorrow" : "in " + days + " days")
                + ".\nScheduled for " + date(event.getDate(), timeZone) + ".";
    }

    private static String date(LocalDateTime date, String timeZone) {
        return date == null ? "Date to be confirmed" : date.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH)) + " (" + timeZone + ")";
    }
}
