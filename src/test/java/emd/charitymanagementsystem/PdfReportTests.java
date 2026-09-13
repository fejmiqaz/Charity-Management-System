package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Service.Implementation.PdfExportService;
import emd.charitymanagementsystem.DTO.project.ProjectResponseDto;
import emd.charitymanagementsystem.DTO.budget.BudgetResponseDto;
import emd.charitymanagementsystem.DTO.donation.DonationResponseDto;
import emd.charitymanagementsystem.DTO.event.EventResponseDto;
import emd.charitymanagementsystem.Models.*;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PdfReportTests {
 @Test void rendersEveryReportAndMultipagePreview() throws Exception {
  var service=new PdfExportService();
  var project=new ProjectResponseDto();project.setId(1L);project.setName("Community learning and outreach");project.setDescription("Provide learning materials and coordinate volunteer-led activities for the local community.");project.setStatus(ProjectStatus.ONGOING);project.setProjectPrice(1250.50);project.setDateCreated(LocalDate.of(2026,9,1));project.setMemberNames(List.of("Elena Marku", "Arben Dervishi"));
  var budget=new BudgetResponseDto();budget.setId(1L);budget.setBudgetAmount(15000.0);budget.setDescription("Annual allocation for community programs and events.");
  var donation=new DonationResponseDto();donation.setId(1L);donation.setDonationAmount(2500.0);donation.setMemberNames(List.of("Elena Marku"));
  var event=new EventResponseDto();event.setId(1L);event.setPurpose("Community volunteer day and project planning workshop");event.setDate(LocalDateTime.of(2026,9,25,10,30));event.setMemberNames(List.of("Elena Marku", "Arben Dervishi"));
  var year=new Years();year.setId(1L);year.setYearValue(2026);
  var samples=new LinkedHashMap<String,byte[]>();
  samples.put("year-report",service.exportCompleteYearPdf(year,List.of(budget),List.of(donation),List.of(event),List.of(project)).readAllBytes());
  samples.put("projects",service.exportProjectsPdf(Collections.nCopies(45,project),2026).readAllBytes());
  samples.put("project",service.exportSingleProjectPdf(project,2026).readAllBytes());
  samples.put("donations",service.exportDonationsPdf(List.of(donation),2026).readAllBytes());
  samples.put("donation",service.exportSingleDonationPdf(donation,2026).readAllBytes());
  samples.put("events",service.exportEventsPdf(List.of(event),2026).readAllBytes());
  samples.put("event",service.exportSingleEventPdf(event,2026).readAllBytes());
  samples.put("budgets",service.exportBudgetsPdf(List.of(budget),2026).readAllBytes());
  samples.put("members-empty",service.exportMembersPdf(List.of()).readAllBytes());
  samples.put("years-empty",service.exportYearsPdf(List.of()).readAllBytes());
  Files.createDirectories(Path.of("target/pdf-preview"));
  for(var sample:samples.entrySet()) {
   Files.write(Path.of("target/pdf-preview",sample.getKey()+".pdf"),sample.getValue());
   try(var reader=new PdfReader(sample.getValue())) {
    StringBuilder text=new StringBuilder();var extractor=new PdfTextExtractor(reader);
    for(int page=1;page<=reader.getNumberOfPages();page++){String pageText=extractor.getTextFromPage(page);assertTrue(pageText.contains("Page "+page),sample.getKey());text.append(pageText);}
    assertTrue(text.toString().contains("EXPORT DATE"),sample.getKey());assertTrue(text.toString().contains("UTC"));assertTrue(text.toString().contains("Signature:"));assertTrue(text.toString().contains("REVIEWED / APPROVED BY"));
    if(sample.getKey().equals("projects")){assertTrue(reader.getNumberOfPages()>1);assertTrue(text.toString().contains("1,250.50 EUR"));}
   }
  }
 }
}
