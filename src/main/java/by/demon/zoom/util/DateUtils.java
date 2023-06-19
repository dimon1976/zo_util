package by.demon.zoom.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {
    public final static String DATE_PATTERN = "yyyy-MM-dd";
    public final static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final LocalDateTime NOW = LocalDateTime.now();


    private static final DateTimeFormatter FILE_NAME_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd-H-m");

    public static String getDateTimeNow() {
        return NOW.format(FILE_NAME_PATTERN);
    }

    public static LocalDateTime getDateTime(String date, DateTimeFormatter pattern) {
        return LocalDateTime.parse(date, pattern);
    }

    public static LocalDate getDate(String date, DateTimeFormatter pattern) {
        return LocalDate.parse(date, pattern);
    }

    public static String format(Date date) {
        return format(date, DATE_PATTERN);
    }

    public static String format(Date date, String pattern) {
        if (date != null) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            return df.format(date);
        }
        return null;
    }

    public static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
