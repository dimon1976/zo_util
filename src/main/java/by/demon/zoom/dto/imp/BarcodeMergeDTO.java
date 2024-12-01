package by.demon.zoom.dto.imp;

import by.demon.zoom.dto.CsvRow;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BarcodeMergeDTO implements CsvRow {

    public String model;
    public String bar;

    @Override
    public List<Object> values() {
        return List.of(model, bar);
    }
}
