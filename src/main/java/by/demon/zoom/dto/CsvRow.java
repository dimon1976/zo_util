package by.demon.zoom.dto;

import java.util.List;

import static java.util.stream.Collectors.joining;

public interface CsvRow {
    String COLUMN_SEPARATOR = ";";

    List<Object> values();

    default String toCsvRow() {
        return values().stream()
                .map(value -> {
                    if (value instanceof Number) {
                        Number number = (Number) value;
                        if (number.doubleValue() == 0.0) {
                            return "";
                        }
                        // Замена в числах точек для корректного подсчета в эксел ячеек с типом число
                        return String.valueOf(value).replace('.', ',');
                    } else {
                        return String.valueOf(value);
                    }
                })
                .collect(joining(COLUMN_SEPARATOR));
    }


    default String[] toCsvArrays() {
        return values().stream()
                .map(String::valueOf)
                .toArray(String[]::new);
    }
}
