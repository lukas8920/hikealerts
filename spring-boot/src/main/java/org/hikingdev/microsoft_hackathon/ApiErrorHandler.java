package org.hikingdev.microsoft_hackathon;

import org.hikingdev.microsoft_hackathon.util.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

public class ApiErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiErrorHandler.class);

    @ExceptionHandler({BadRequestException.class})
    public ResponseEntity<String> handleCustomRequest(Exception e, WebRequest request){
        if (e instanceof BadRequestException){
            logger.error("Request failed with BadRequestException: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return null;
    }
}
