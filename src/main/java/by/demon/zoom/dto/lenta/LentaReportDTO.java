package by.demon.zoom.dto.lenta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LentaReportDTO {

    private String city;
    private Double product;
    private String productName;
    private Double price;
    private String network;
    private Double actionPrice1;
    private String dateFromPromo;
    private String dateToPromo;
    private Double discountPercentage;
    private String mechanicsOfTheAction;
    private String url;
    private String additionalPrice;
    private String model;
    private String weightEdeadeal;
    private String weightEdeadealKg;
    private Double weightLenta;
    private String weightLentaKg;
    private Double priceEdeadealKg;
    private Double conversionToLentaWeight;
    private String additionalField;
}
