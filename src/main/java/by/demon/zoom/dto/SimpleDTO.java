package by.demon.zoom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleDTO {
    private String id;
    private String category1;
    private String category2;
    private String category3;
    private String brand;
    private String model;
    private String priceSimple;
    private String city;
    private String competitor;
    private String time;
    private String date;
    private String priceCompetitor;
    private String priceCompetitorOld;
    private String priceCompetitorAction;
    private String comment;
    private String nameProductCompetitor;
    private String yearCompetitor;
    private String analogue;
    private String addressOfTheCompetitor;
    private String status;
    private String promo;
    private String urlCompetitor;
    private String urlClient;
    private String urlWebCache;
}
