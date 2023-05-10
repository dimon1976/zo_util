package by.demon.zoom.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Lenta {

    // task
    private String id;
    private String model;
    private String weight;
    private String price;
    private String moscow;
    private String spb;
    private String novosibirsk;
    private String yekaterinburg;
    private String saratov;
    private String rostovNaDonu;
    private HashSet<String> ean = new HashSet<>();

    // reports
    private String city;
    private String product;
    private String productName;
    private String network;
    private String actionPrice1;
    private String dateFromPromo;
    private String dateToPromo;
    private String discountPercentage;
    private String mechanicsOfTheAction;
    private String url;
    private String additionalPrice;
    private String weightEdeadeal;
    private String weightEdeadealKg;
    private String weightLenta;
    private String weightLentaKg;
    private String priceEdeadealKg;
    private String conversionToLentaWeight;

}

