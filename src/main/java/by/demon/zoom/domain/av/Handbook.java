package by.demon.zoom.domain.av;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "av_handbook")
public class Handbook {
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
}
