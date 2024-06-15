package by.demon.zoom.dto.imp;

import by.demon.zoom.dto.CsvRow;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UrlDTO implements CsvRow {

    public String id;
    public String url;

    @Override
    public List<Object> values() {
        return List.of(id, url);
    }
}
