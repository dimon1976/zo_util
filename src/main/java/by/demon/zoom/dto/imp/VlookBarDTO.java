package by.demon.zoom.dto.imp;

import by.demon.zoom.dto.CsvRow;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VlookBarDTO implements CsvRow {

    private String id;
    private String bar;
    private String url;

    @Override
    public List<Object> values() {
        return List.of(id, bar, url);
    }
}
