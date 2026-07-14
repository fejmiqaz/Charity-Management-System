package emd.charitymanagementsystem.Service;

import emd.charitymanagementsystem.DTO.years.YearsDetailsDto;
import emd.charitymanagementsystem.DTO.years.YearsFormDto;
import emd.charitymanagementsystem.DTO.years.YearsResponseDto;
import emd.charitymanagementsystem.Models.Years;
import org.springframework.data.domain.Page;

import java.util.List;

public interface YearsService {

    List<YearsResponseDto> listAll();

    Page<YearsResponseDto> listAll(
            Integer yearValue,
            int page,
            int size,
            String sortDir
    );

    YearsDetailsDto findById(Long id);

    Years findEntityById(Long id);

    YearsFormDto findFormById(Long id);

    YearsResponseDto create(YearsFormDto yearsFormDto);

    YearsResponseDto update(Long id, YearsFormDto yearsFormDto);

    void delete(Long id);

    long yearsCount();
}
