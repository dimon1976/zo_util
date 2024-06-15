package by.demon.zoom.domain.imp.av;

import by.demon.zoom.dto.CsvRow;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "av_task")
public class AvDataEntity implements CsvRow {

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
    private String numberOfPieces;

    @Override
    public List<Object> values() {
        return List.of(jobNumber,jobStart,jobEnd,itemNumber,category,productCategoryCode,productDescription,productComment,brand,priceZoneCode,retailerCode,retailChain,region,physicalAddress,barcode,numberOfPieces);
    }
}
