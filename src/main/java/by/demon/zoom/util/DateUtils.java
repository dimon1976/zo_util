package by.demon.zoom.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {
    public final static String DATE_PATTERN = "yyyy-MM-dd";
    public final static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final DateTimeFormatter MEGATOP_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy H:m");
    private static final DateTimeFormatter FILE_NAME_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd-H-m");

    public static String getDateTimeNow() {
        return NOW.format(FILE_NAME_PATTERN);
    }

    public static LocalDateTime getDateTime(String date) {
        return LocalDateTime.parse(date, MEGATOP_PATTERN);
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
}
