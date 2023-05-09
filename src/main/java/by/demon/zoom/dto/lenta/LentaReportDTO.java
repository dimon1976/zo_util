package by.demon.zoom.dto.lenta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LentaReportDTO {

    private String city;
    private String product;
    private String productName;
    private String price;
    private String network;
    private String actionPrice1;
    private LocalDate dateFromPromo;
    private LocalDate dateToPromo;
    private String discountPercentage;
    private String mechanicsOfTheAction;
    private String url;
    private String additionalPrice;
    private String model;
    private String weightEdeadeal;
    private String weightEdeadealKg;
    private String weightLenta;
    private String weightLentaKg;
    private String priceEdeadealKg;
    private String conversionToLentaWeight;
}
