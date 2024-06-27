package by.demon.zoom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CsvAvReportDTO {

    private String jobNumber;
    private String jobEnd;
    private Map<String, Double> competitorsPrices;

    public CsvAvReportDTO(String jobNumber, String jobEnd) {
        this.jobNumber = jobNumber;
        this.jobEnd = jobEnd;
        this.competitorsPrices = new HashMap<>();
    }

    public void addCompetitorPrice(String retailChain, double price) {
        this.competitorsPrices.merge(retailChain, price, Double::sum);
    }

    // Getters and setters
    public String getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }

    public String getJobEnd() {
        return jobEnd;
    }

    public void setJobEnd(String jobEnd) {
        this.jobEnd = jobEnd;
    }

    public Map<String, Double> getCompetitorsPrices() {
        return competitorsPrices;
    }

    public void setCompetitorsPrices(Map<String, Double> competitorsPrices) {
        this.competitorsPrices = competitorsPrices;
    }
}
