package by.demon.zoom.util;

import java.util.concurrent.TimeUnit;

public class MethodPerformance {

    public static Long start() {
        return System.nanoTime();
    }

    public static void finish(Long startTime, String text) {
        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        // Рассчитываем время выполнения в минутах и секундах
        long minutes = TimeUnit.NANOSECONDS.toMinutes(duration);
        long seconds = TimeUnit.NANOSECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(minutes);

        // Выводим время выполнения в консоль
        System.out.printf("Время выполнения %s: %d минут, %d секунд%n", text, minutes, seconds);
    }
}
