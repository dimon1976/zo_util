package by.demon.zoom.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Timestamp;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private String client;
    private String id;
    private String idLink;
    private String zmsId;
    private String parentCategory;
    private String category;
    private String vendor;
    private String model;
    private String productCode;
    private String bar;
    private String CompetitorModel;
    private String competitorProductCode;
    private String competitorId;
    private String Competitor;
    private String on;
    private String weight;
    private String moscow;
    private String spb;
    private String novosibirsk;
    private String yekaterinburg;
    private String saratov;
    private String rostovNaDonu;
    private HashSet<String> ean = new HashSet<>();
    private String city;
    private String product;
    private String productName;
    private String network;
    private String dateFromPromo;
    private String dateToPromo;
    private String discountPercentage;
    private String mechanicsOfTheAction;
    private String url;
    private String weightEdeadeal;
    private String weightEdeadealKg;
    private String weightLenta;
    private String weightLentaKg;
    private String priceEdeadealKg;
    private String conversionToLentaWeight;
    private String userAdd;
    //    private long id;
    private String heelHeight;
    private String collection;
    private String upperConstruction;
    private String upperMaterial;
    private String liningMaterial;
    private String rostovChildren;
    private String colors;
    private String season;
    private String megatopId;
    private String category1;
    private String category2;
    private String category3;
    private String brand;
    private String vendorCode;
    private String status;
    private String date;
    private String concatUrlRostovChildren;
    private String fileName;
    private Timestamp dateTime;
    private String time;
    private String price;
    private String competitorPrice;
    private String competitorOldPrice;
    private String competitorActionPrice;
    private String additionalPrice;
    private String comment;
    private String yearCompetitor;
    private String analogue;
    private String addressOfTheCompetitor;
    private String promo;
    private String competitorUrl;
    private String clientUrl;
    private String webCacheUrl;
    private Set<String> collectionUrl;/* for vlookBarService */

    public Product(String id, String bar, Set<String> collectionUrl) {
        this.id = id;
        this.bar = bar;
        this.collectionUrl = collectionUrl;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product that = (Product) o;
        return Objects.equals(concatUrlRostovChildren, that.concatUrlRostovChildren);
    }

    @Override
    public int hashCode() {
        return Objects.hash(concatUrlRostovChildren);
    }
}


