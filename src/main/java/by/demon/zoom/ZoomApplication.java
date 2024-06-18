package by.demon.zoom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ZoomApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZoomApplication.class, args);
    }

}
