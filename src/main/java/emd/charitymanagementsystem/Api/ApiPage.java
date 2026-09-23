package emd.charitymanagementsystem.Api;

import org.springframework.data.domain.Page;

import java.util.List;

public record ApiPage<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public static <T> ApiPage<T> of(Page<T> page) {
        return new ApiPage<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
