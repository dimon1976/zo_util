package by.demon.zoom.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Timestamp;
import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {


    private String id;
    private String category1;
    private String category2;
    private String category3;
    private String model;
    private String brand;
    private String price;
    private String city;
    private String Competitor;
    private String time;
    private String date;
    private String competitorPrice;
    private String competitorOldPrice;
    private String competitorActionPrice;
    private String comment;
    private String yearCompetitor;
    private String CompetitorModel;
    private String analogue;
    private String addressOfTheCompetitor;
    private String status;
    private String promo;
    private String competitorUrl;
    private String clientUrl;
    private String webCacheUrl;
    private Timestamp dateTime;

    private Set<String> collectionUrl;/* for vlookBarService */



}


