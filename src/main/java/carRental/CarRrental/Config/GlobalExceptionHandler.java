package carRental.CarRrental.Config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", ex.getStatusCode().value());
        response.put("error", ex.getReason());

        return new ResponseEntity<>(response, ex.getStatusCode());
    }
}