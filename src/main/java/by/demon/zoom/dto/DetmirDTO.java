package by.demon.zoom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetmirDTO {

    private String client;
    private String idLink;
    private String clientId;
    private String parentCategory;
    private String category;
    private String vendor;
    private String model;
    private String productCode;
    private String bar;
    private String status;
    private String competitorPrice;
    private String competitorModel;
    private String competitorProductCode;
    private String competitorId;
    private String competitor;
    private String on;
    private String userAdd;
}
