package by.demon.zoom.domain;

import lombok.Getter;

@Getter
public enum City {
    MOSCOW(4400, "Москва", "Moscow"),
    SAINT_PETERSBURG(4962, "Санкт-Петербург", "Saint Petersburg");

    private final int id;
    private final String russianName;
    private final String englishName;

    City(int id, String russianName, String englishName) {
        this.id = id;
        this.russianName = russianName;
        this.englishName = englishName;
    }
}
