package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.DTO.project.ProjectFormDto;
import emd.charitymanagementsystem.DTO.project.ProjectResponseDto;
import emd.charitymanagementsystem.Mapper.ProjectMapper;
import emd.charitymanagementsystem.Models.Member;
import emd.charitymanagementsystem.Models.Project;
import emd.charitymanagementsystem.Models.Years;
import emd.charitymanagementsystem.Repository.MemberRepository;
import emd.charitymanagementsystem.Repository.ProjectRepository;
import emd.charitymanagementsystem.Repository.YearsRepository;
import emd.charitymanagementsystem.Service.ProjectService;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@AllArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final YearsRepository yearsRepository;
    private final MemberRepository memberRepository;

    @Override
    public List<ProjectResponseDto> listAll() {
        return projectRepository.findAll().stream()
                .map(ProjectMapper::toDto)
                .toList();
    }

    @Override
    public ProjectResponseDto findById(Long id) {
        Project project = projectRepository.findById(id).orElseThrow();
        return ProjectMapper.toDto(project);
    }

    @Override
    public ProjectResponseDto create(ProjectFormDto projectFormDto) {
        Years year = yearsRepository.findById(projectFormDto.getYearId()).orElseThrow();

        Set<Member> members = new HashSet<>();
        if(projectFormDto.getMemberIds() != null && !projectFormDto.getMemberIds().isEmpty()){
            members = new HashSet<>(memberRepository.findAllById(projectFormDto.getMemberIds()));
        }

        Project project = new Project();
        project.setName(projectFormDto.getName());
        project.setDescription(projectFormDto.getDescription());
        project.setStatus(projectFormDto.getStatus());
        project.setProjectPrice(projectFormDto.getProjectPrice());
        project.setDateCreated(LocalDate.now());
        project.setYear(year);
        project.setMembers(members);

        Project saved = projectRepository.save(project);

        return ProjectMapper.toDto(saved);

    }

    @Override
    public ProjectResponseDto update(Long id, ProjectFormDto projectFormDto) {
        Project project = projectRepository.findById(id).orElseThrow();

        Years year = yearsRepository.findById(projectFormDto.getYearId()).orElseThrow();

        Set<Member> members = new HashSet<>();
        if (projectFormDto.getMemberIds() != null && !projectFormDto.getMemberIds().isEmpty()) {
            members = new HashSet<>(memberRepository.findAllById(projectFormDto.getMemberIds()));
        }

        project.setName(projectFormDto.getName());
        project.setDescription(projectFormDto.getDescription());
        project.setStatus(projectFormDto.getStatus());
        project.setProjectPrice(projectFormDto.getProjectPrice());
        project.setDateCreated(LocalDate.now());
        project.setYear(year);
        project.setMembers(members);

        Project updated = projectRepository.save(project);

        return ProjectMapper.toDto(updated);

    }

    @Override
    public void delete(Long id) {
        projectRepository.deleteById(id);
    }

    @Override
    public @Nullable List<ProjectResponseDto> findByYearId(Long yearId) {
        return projectRepository.findByYearId(yearId).stream()
                .map(ProjectMapper::toDto)
                .toList();
    }

    @Override
    public Double getTotalPriceForYear(Long yearId) {
        return projectRepository.findByYearId(yearId)
                .stream()
                .map(Project::getProjectPrice)
                .filter(Objects::nonNull)
                .reduce(0.0, Double::sum);
    }

    @Override
    public long projectsCount() {
        return projectRepository.count();
    }

    @Override
    public double getTotalProjectCost() {
        return projectRepository.findAll().stream()
                .map(Project::getProjectPrice)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    @Override
    public double totalProjectCostsByYear(Long id) {
        return projectRepository.findByYearId(id)
                .stream()
                .map(Project::getProjectPrice) // change if your field name is different
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }
}
