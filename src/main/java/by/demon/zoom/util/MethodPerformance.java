package by.demon.zoom.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class MethodPerformance {
    private static final Logger LOG = LoggerFactory.getLogger(MethodPerformance.class);
    public static Instant start() {
        return Instant.now();
    }

    public static void finish(Instant startTime, String text) {
        Instant endTime = Instant.now();
        Duration duration = Duration.between(startTime, endTime);

        // Рассчитываем время выполнения в минутах и секундах
        long minutes = duration.toMinutes();
        long seconds = duration.getSeconds() - minutes * 60;

        // Выводим время выполнения через логгер
        LOG.info("Время выполнения {}: {} минут, {} секунд", text, minutes, seconds);
    }
}
