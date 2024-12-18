package by.demon.zoom.domain.imp.av;

import by.demon.zoom.dto.CsvRow;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "av_report")
public class CsvAvReportEntity implements CsvRow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String jobNumber;
    private String jobStart;
    private String jobEnd;
    private String itemNumber;
    private String category;
    private String productCategoryCode;
    private String productDescription;
    private String productComment;
    private String brand;
    private String priceZoneCode;
    private String retailerCode;
    private String retailChain;
    private String region;
    private String physicalAddress;
    private String barcode;
    private String quantityOfPieces;
    private String competitorsPrice;
    private String promotionalPrice;
    private String analogue;
    private String noProduct;
    private String monitoringDate;
    private String photo;
    private String note;
    //*https://stackoverflow.com/questions/36446201/org-postgresql-util-psqlexception-error-value-too-long-for-type-character-vary
    @Column(length = 1024)
    private String linkToProductPage;
    private String city;
    private String typeReport;
    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    @PrePersist
    protected void onCreate() {
        uploadTime = LocalDateTime.now();
    }

    @Override
    public List<Object> values() {
        return List.of(jobNumber,jobStart,jobEnd,itemNumber,category,productCategoryCode,productDescription,productComment,brand,priceZoneCode,retailerCode,retailChain,region,physicalAddress,barcode,quantityOfPieces,
                competitorsPrice,promotionalPrice,analogue,noProduct,monitoringDate,photo,note,linkToProductPage);
    }

}
