package by.demon.zoom.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Edadeal implements Serializable {


 private String categoryFromFile;
 private String site;
 private String zmsId;
 private String lentaID;
 private String lentaVendor;
 private String lentaPrice;
 private String id;
 private String category;
 private String vendor;
 private String vendorZms;
 private String model;
 private String vendorCode;
 private String price;
 private String marketingInfo1;
 private String marketingInfo2;
 //от
 private String marketingInfo3;
 private String marketingInfo4;
 private String status;
 private String url;
 private String oldPrice;
 private String supplier;
 private String date;
 private String position;
 private String urlParent;
}
