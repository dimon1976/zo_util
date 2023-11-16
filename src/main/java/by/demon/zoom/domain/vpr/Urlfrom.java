package by.demon.zoom.domain.vpr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Urlfrom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String idFrom;
    private String bar;
    private String url;

    public Urlfrom(String idFrom, String bar, String url) {
        this.idFrom = idFrom;
        this.bar = bar;
        this.url = url;
    }
}
