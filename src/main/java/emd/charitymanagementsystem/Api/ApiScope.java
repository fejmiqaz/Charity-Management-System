package emd.charitymanagementsystem.Api;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ApiScope {
    private final YearsRepository years;
    private final ProjectRepository projects;
    private final DonationRepository donations;
    private final EventRepository events;
    private final MemberRepository members;
    private final EventTaskRepository tasks;

    public Years year(Long id) {
        return years.findById(id).orElseThrow(ApiScope::missing);
    }

    public Member member(Long id) {
        return members.findById(id).orElseThrow(ApiScope::missing);
    }

    public Project project(Long yearId, Long id) {
        var item = projects.findById(id).orElseThrow(ApiScope::missing);
        belongs(yearId, item.getYear());
        return item;
    }

    public Donation donation(Long yearId, Long id) {
        var item = donations.findById(id).orElseThrow(ApiScope::missing);
        belongs(yearId, item.getYear());
        return item;
    }

    public Event event(Long yearId, Long id) {
        var item = events.findById(id).orElseThrow(ApiScope::missing);
        belongs(yearId, item.getYear());
        return item;
    }

    public EventTask task(Long yearId, Long eventId, Long taskId) {
        event(yearId, eventId);
        var task = tasks.findById(taskId).orElseThrow(ApiScope::missing);
        if (!task.getEvent().getId().equals(eventId)) throw missing();
        return task;
    }

    public void members(List<Long> ids) {
        if (ids == null) return;
        if (ids.size() > 1000 || ids.contains(null) || members.findAllById(ids).size() != new HashSet<>(ids).size())
            throw new IllegalArgumentException("Select valid member IDs (at most 1000).");
    }

    private void belongs(Long id, Years year) {
        if (year == null || !id.equals(year.getId())) throw missing();
    }

    public static ResponseStatusException missing() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found.");
    }
}
