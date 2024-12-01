package by.demon.zoom.domain.imp.av;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "report_summary")
public class ReportSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(name = "upload_time")
    private LocalDateTime uploadTime;
    private String task_no;
    private String retailChain;
    private String city;
    private String typeReport;
    private long countRows;
    private long countCompetitorsPrice;
    private long countPromotionalPrice;
}