package by.demon.zoom.dto.lenta;

import by.demon.zoom.dto.CsvRow;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LentaReportDTO implements CsvRow {

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

    @Override
    public List<Object> values() {
        return List.of(city, product, productName, price, network, actionPrice1, dateFromPromo, dateToPromo, discountPercentage, mechanicsOfTheAction, url, additionalPrice, model, weightEdeadeal, weightEdeadealKg,
                weightLenta, weightLentaKg, priceEdeadealKg, conversionToLentaWeight, additionalField);
    }
}
