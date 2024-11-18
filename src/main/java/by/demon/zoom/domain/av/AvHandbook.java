package by.demon.zoom.domain.av;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "av_handbook")
public class AvHandbook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String retailNetworkCode;
    private String retailNetwork;
    private String physicalAddress;
    private String priceZoneCode;
    private String webSite;
    private String regionCode;
    private String regionName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvHandbook handbook = (AvHandbook) o;
        return Objects.equals(retailNetworkCode, handbook.retailNetworkCode)
                && Objects.equals(retailNetwork, handbook.retailNetwork)
                && Objects.equals(physicalAddress, handbook.physicalAddress)
                && Objects.equals(priceZoneCode, handbook.priceZoneCode)
                && Objects.equals(webSite, handbook.webSite)
                && Objects.equals(regionCode, handbook.regionCode)
                && Objects.equals(regionName, handbook.regionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(retailNetworkCode, retailNetwork, physicalAddress, priceZoneCode, webSite, regionCode, regionName);
    }
}
