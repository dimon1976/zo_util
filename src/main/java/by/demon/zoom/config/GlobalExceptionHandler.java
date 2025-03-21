package by.demon.zoom.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

/**
 * Handles exceptions globally and returns an error response.
 *
 * @param e The exception that occurred
 * @return ResponseEntity containing the error message and status code
 */
@ExceptionHandler(Exception.class)
public ResponseEntity<String> handleException(Exception e) {
    logger.error("Unhandled exception occurred: ", e);
    return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
}

}
