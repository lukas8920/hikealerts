package org.hikingdev.microsoft_hackathon;

import org.hikingdev.microsoft_hackathon.util.exceptions.AiException;
import org.hikingdev.microsoft_hackathon.util.exceptions.BadRequestException;
import org.hikingdev.microsoft_hackathon.util.exceptions.InvalidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ApiErrorHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiErrorHandler.class);

    @ExceptionHandler({BadRequestException.class, AiException.class, InvalidationException.class})
    public ResponseEntity<String> handleCustomRequest(Exception e, WebRequest request){
        if (e instanceof BadRequestException){
            logger.error("Request failed with BadRequestException: " + e.getMessage());
            if (((BadRequestException) e).getCode() != null){
                return ResponseEntity.status(((BadRequestException) e).getCode()).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } else if (e instanceof AiException){
            logger.error("Request failed with AiException: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } else if (e instanceof InvalidationException){
            logger.error("Authentication failed: " + e.getMessage());
            return ResponseEntity.badRequest().body("Invalid request.");
        }
        return null;
    }
}
