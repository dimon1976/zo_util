package by.demon.zoom.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrayUtil {
    private static final Logger log = LoggerFactory.getLogger(ArrayUtil.class);

    public static String[] array(String... args) {
        log.info("Creating array with {} elements", args.length);
        return args;
    }
}
