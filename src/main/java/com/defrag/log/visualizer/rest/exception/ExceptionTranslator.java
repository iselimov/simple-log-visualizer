package com.defrag.log.visualizer.rest.exception;

import com.defrag.log.visualizer.rest.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controller advice to translate the server side exceptions
 * to client-friendly errors.
 */
@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class ExceptionTranslator {

    @ExceptionHandler(ValidationException.class)
    public void validationException(ValidationException e,
                                    HttpServletResponse response) throws IOException {
        log.debug(e.getMessage(), e);
        sendError(response, HttpStatus.BAD_REQUEST);
    }

    private void sendError(HttpServletResponse response, HttpStatus status) throws IOException {
        response.sendError(status.value());
    }
}
