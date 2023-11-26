package by.demon.zoom.domain.av;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "av_task")
public class CsvDataEntity {

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
}
