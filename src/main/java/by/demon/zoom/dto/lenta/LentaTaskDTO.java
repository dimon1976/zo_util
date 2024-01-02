package by.demon.zoom.dto.lenta;


import by.demon.zoom.dto.CsvRow;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LentaTaskDTO implements CsvRow {

    private String id;
    private String model;
    private String weight;
    private String price;
    private String moscow;
    private String rostovNaDonu;
    private String spb;
    private String novosibirsk;
    private String yekaterinburg;
    private String saratov;
    private String ean;

    @Override
    public List<Object> values() {
        return List.of(id, model, weight, price, moscow, rostovNaDonu, spb, novosibirsk, yekaterinburg, saratov, ean);
    }
}
