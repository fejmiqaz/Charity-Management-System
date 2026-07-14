package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.DTO.years.YearsDetailsDto;
import emd.charitymanagementsystem.DTO.years.YearsFormDto;
import emd.charitymanagementsystem.DTO.years.YearsResponseDto;
import emd.charitymanagementsystem.Mapper.YearsMapper;
import emd.charitymanagementsystem.Models.Years;
import emd.charitymanagementsystem.Repository.YearsRepository;
import emd.charitymanagementsystem.Service.YearsService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class YearsServiceImpl implements YearsService {

    private final YearsRepository yearsRepository;

    @Override
    public List<YearsResponseDto> listAll() {
        return yearsRepository.findAll(Sort.by(Sort.Direction.DESC, "yearValue"))
                .stream()
                .map(YearsMapper::toResponseDto)
                .toList();
    }

    @Override
    public Page<YearsResponseDto> listAll(
            Integer yearValue,
            int page,
            int size,
            String sortDir) {

        int safePage = Math.max(page, 0);
        int safeSize = switch (size) {
            case 5, 10, 20, 50 -> size;
            default -> 10;
        };

        Sort.Direction direction =
                "asc".equalsIgnoreCase(sortDir)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(direction, "yearValue")
        );

        Specification<Years> specification = Specification.allOf();

        if (yearValue != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(root.get("yearValue"), yearValue)
            );
        }

        return yearsRepository.findAll(specification, pageable)
                .map(YearsMapper::toResponseDto);
    }

    @Override
    public YearsDetailsDto findById(Long id) {
        Years year = yearsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Year not found with id: " + id));

        return YearsMapper.toDetailsDto(year);
    }

    @Override
    public Years findEntityById(Long id) {
        return yearsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Year not found with id: " + id));
    }

    @Override
    public YearsFormDto findFormById(Long id) {
        Years year = yearsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Year not found with id: " + id));

        return YearsMapper.toFormDto(year);
    }

    @Override
    public YearsResponseDto create(YearsFormDto yearsFormDto) {
        if (yearsFormDto.getYearValue() == null) {
            throw new IllegalArgumentException("Year value cannot be null");
        }

        Years year = new Years();
        year.setYearValue(yearsFormDto.getYearValue());

        return YearsMapper.toResponseDto(yearsRepository.save(year));
    }

    @Override
    public YearsResponseDto update(Long id, YearsFormDto yearsFormDto) {
        if (yearsFormDto.getYearValue() == null) {
            throw new IllegalArgumentException("Year value cannot be null");
        }

        Years yearToUpdate = yearsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Year not found with id: " + id));

        yearToUpdate.setYearValue(yearsFormDto.getYearValue());

        return YearsMapper.toResponseDto(yearsRepository.save(yearToUpdate));
    }

    @Override
    public void delete(Long id) {
        Years year = yearsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Year not found with id: " + id));

        yearsRepository.delete(year);
    }

    @Override
    public long yearsCount() {
        return yearsRepository.count();
    }
}
