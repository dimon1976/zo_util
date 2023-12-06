package by.demon.zoom.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {
    private static final DateTimeFormatter FILE_NAME_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd-H-m");
    private static final DateTimeFormatter FILE_NAME_PATTERN_MIN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String getDateTimeNow() {
        return LocalDateTime.now().format(FILE_NAME_PATTERN);
    }

    public static LocalDateTime getDateTime(String date, DateTimeFormatter pattern) {
        return LocalDateTime.parse(date, pattern);
    }

    public static LocalDate getLocalDate(String date, DateTimeFormatter pattern) {
        return LocalDate.parse(date, pattern);
    }

    public static Date getDate(String date, String pattern) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.parse(date);
    }


    public static String format(Date date) {
        return format(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String format(Date date, String pattern) {
        return format(date, DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(Date date, DateTimeFormatter formatter) {
        if (date != null) {
            return formatter.format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        return null;
    }

    public static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
