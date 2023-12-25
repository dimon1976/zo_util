package by.demon.zoom.dto.imp;

import by.demon.zoom.dto.CsvRow;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MegatopDTO implements CsvRow {
    private String category1;
    private String category;
    private String heelHeight;
    private String collection;
    private String upperConstruction;
    private String upperMaterial;
    private String liningMaterial;
    private String rostovChildren;
    private String colors;
    private String season;
    private String competitor;
    private String id;
    private String category2;
    private String brand;
    private String model;
    private String vendorCode;
    private Double price;
    private Double oldPrice;
    private String url;
    private String status;

    @Override
    public List<Object> values() {
        return List.of(category1, category, heelHeight, collection, upperConstruction, upperMaterial, liningMaterial, rostovChildren, colors, season, competitor, id, category2, brand, model,
                vendorCode, price, oldPrice, url, status);
    }
}
