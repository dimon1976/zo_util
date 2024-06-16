package by.demon.zoom.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class DateUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DateUtils.class);
    private static final DateTimeFormatter FILE_NAME_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd-H-m");
    private static final DateTimeFormatter FILE_NAME_PATTERN_MIN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String getDateTimeNow() {
        //        LOG.info("Current DateTime: {}", dateTimeNow);
        return LocalDateTime.now().format(FILE_NAME_PATTERN);
    }

    public static LocalDateTime getDateTime(String date, DateTimeFormatter pattern) {
        try {
            //            LOG.info("Parsed DateTime: {} using pattern: {}", dateTime, pattern);
            return LocalDateTime.parse(date, pattern);
        } catch (DateTimeParseException e) {
            LOG.error("Error parsing DateTime: {} with pattern: {}", date, pattern, e);
            throw e;
        }
    }

    public static LocalDate getLocalDate(String date, DateTimeFormatter pattern) {
        try {
            //            LOG.info("Parsed LocalDate: {} using pattern: {}", localDate, pattern);
            return LocalDate.parse(date, pattern);
        } catch (DateTimeParseException e) {
            LOG.error("Error parsing LocalDate: {} with pattern: {}", date, pattern, e);
            throw e;
        }
    }

    public static String format(Date date) {
        return format(date, DATE_PATTERN);
    }

    public static String format(Date date, String pattern) {
        return format(date, DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(Date date, DateTimeFormatter formatter) {
        if (date != null) {
            //            LOG.info("Formatted Date: {} using formatter: {}", formattedDate, formatter);
            return formatter.format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        LOG.warn("Attempted to format null date");
        return null;
    }

    public static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        if (dateToConvert != null) {
            //            LOG.info("Converted Date to LocalDate: {}", localDate);
            return dateToConvert.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
        LOG.warn("Attempted to convert null date");
        return null;
    }
}
