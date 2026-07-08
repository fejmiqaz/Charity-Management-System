package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.DTO.years.YearsDetailsDto;
import emd.charitymanagementsystem.DTO.years.YearsFormDto;
import emd.charitymanagementsystem.DTO.years.YearsResponseDto;
import emd.charitymanagementsystem.Mapper.YearsMapper;
import emd.charitymanagementsystem.Models.Years;
import emd.charitymanagementsystem.Repository.YearsRepository;
import emd.charitymanagementsystem.Service.YearsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class YearsServiceImpl implements YearsService {

    private final YearsRepository yearsRepository;

    @Override
    public List<YearsResponseDto> listAll() {
        return yearsRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public YearsDetailsDto findById(Long id) {
        Years year = yearsRepository.findById(id).orElseThrow();
        return YearsMapper.toDetailsDto(year);
    }

    @Override
    public Years findEntityById(Long id) {
        return yearsRepository.findById(id).orElseThrow();
    }

    @Override
    public YearsFormDto findFormById(Long id) {
        Years year = yearsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Year not found with id: " + id));

        return toFormDto(year);
    }

    @Override
    public YearsResponseDto create(YearsFormDto yearsFormDto) {
        if (yearsFormDto.getYearValue() == null) {
            throw new IllegalArgumentException("Year value cannot be null");
        }

        Years year = new Years();
        year.setYearValue(yearsFormDto.getYearValue());

        Years savedYear = yearsRepository.save(year);
        return toResponseDto(savedYear);
    }

    @Override
    public YearsResponseDto update(Long id, YearsFormDto yearsFormDto) {
        if (yearsFormDto.getYearValue() == null) {
            throw new IllegalArgumentException("Year value cannot be null");
        }

        Years yearToUpdate = yearsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Year not found with id: " + id));

        yearToUpdate.setYearValue(yearsFormDto.getYearValue());

        Years updatedYear = yearsRepository.save(yearToUpdate);
        return toResponseDto(updatedYear);
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

    private YearsResponseDto toResponseDto(Years year) {
        return new YearsResponseDto(
                year.getId(),
                year.getYearValue()
        );
    }

    private YearsFormDto toFormDto(Years year) {
        return new YearsFormDto(
                year.getId(),
                year.getYearValue()
        );
    }

    private YearsDetailsDto toDetailsDto(Years year) {
        return YearsMapper.toDetailsDto(year);
    }
}