package by.demon.zoom.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Megatop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
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
    private String megatopId;
    private String category2;
    private String brand;
    private String model;
    private String vendorCode;
    private String price;
    private String oldPrice;
    private String url;
    private String status;
    private LocalDateTime date;
    private String concatUrlRostovChildren;
    private String fileName;
    private Timestamp dateTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Megatop that = (Megatop) o;
        return Objects.equals(concatUrlRostovChildren, that.concatUrlRostovChildren);
    }

    @Override
    public int hashCode() {
        return Objects.hash(concatUrlRostovChildren);
    }
}
