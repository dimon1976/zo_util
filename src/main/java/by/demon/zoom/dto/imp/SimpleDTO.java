package by.demon.zoom.dto.imp;

import by.demon.zoom.dto.CsvRow;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleDTO implements CsvRow {
    private String id;
    private String category1;
    private String category2;
    private String category3;
    private String brand;
    private String model;
    private Double price;
    private String city;
    private String competitor;
    private String time;
    private String date;
    private Double competitorPrice;
    private Double competitorOldPrice;
    private Double competitorActionPrice;
    private String comment;
    private String competitorModel;
    private String competitorYear;
    private String analogue;
    private String addressOfTheCompetitor;
    private String status;
    private String promo;
    private String competitorUrl;
    private String clientUrl;
    private String urlWebCache;


    public List<Object> values() {
        return List.of(id, category1, category2, category3, brand, model, price, city, competitor, time, date, competitorPrice, competitorOldPrice, competitorActionPrice, comment, competitorModel, competitorYear, analogue,
                addressOfTheCompetitor, status, promo, competitorUrl, clientUrl, urlWebCache);
    }
}
