package by.demon.zoom.domain;

import lombok.Getter;

@Getter
public enum TypeReport {
    ANALOGS("Аналоги"),
    ZHUK("Жуковский"),
    MAIN("Основной");

    private final String russianName;

    TypeReport(String russianName) {
        this.russianName = russianName;
    }
}
