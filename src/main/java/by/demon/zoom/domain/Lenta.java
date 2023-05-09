package by.demon.zoom.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Lenta {

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

}

