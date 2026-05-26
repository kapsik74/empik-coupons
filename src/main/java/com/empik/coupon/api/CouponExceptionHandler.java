package com.empik.coupon.api;

import com.empik.coupon.api.dto.ErrorResponse;
import com.empik.coupon.doamin.exception.CountryNotAllowedException;
import com.empik.coupon.doamin.exception.CouponAlreadyUsedException;
import com.empik.coupon.doamin.exception.CouponExhaustedException;
import com.empik.coupon.doamin.exception.CouponNotFoundException;
import com.empik.coupon.doamin.exception.DuplicateCouponCodeException;
import com.empik.coupon.doamin.exception.GeolocationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class CouponExceptionHandler {

    @ExceptionHandler(value = CouponNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleCouponNotFoundException(CouponNotFoundException ex) {
        return ErrorResponse.of(HttpStatus.NOT_FOUND.value(),  "COUPON_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(CouponExhaustedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErrorResponse handleCouponExhausted(CouponExhaustedException ex) {
        return ErrorResponse.of(HttpStatus.UNPROCESSABLE_CONTENT.value(), "COUPON_EXHAUSTED", ex.getMessage());
    }

    @ExceptionHandler(CouponAlreadyUsedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleCouponAlreadyUsed(CouponAlreadyUsedException ex) {
        return ErrorResponse.of(HttpStatus.CONFLICT.value(), "COUPON_ALREADY_USED", ex.getMessage());
    }

    @ExceptionHandler(CountryNotAllowedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleCountryNotAllowed(CountryNotAllowedException ex) {
        return ErrorResponse.of(HttpStatus.FORBIDDEN.value(), "COUNTRY_NOT_ALLOWED", ex.getMessage());
    }

    @ExceptionHandler(DuplicateCouponCodeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateCouponCode(DuplicateCouponCodeException ex) {
        return ErrorResponse.of(HttpStatus.CONFLICT.value(), "COUPON_CODE_DUPLICATE", ex.getMessage());
    }

    @ExceptionHandler(GeolocationException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse handleGeoLocation(GeolocationException ex) {
        log.warn("Geo-location service error: {}", ex.getMessage());
        return ErrorResponse.of(HttpStatus.BAD_GATEWAY.value(), "GEO_LOCATION_UNAVAILABLE", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "VALIDATION_FAILED", details);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR",
                "An unexpected error occurred");
    }
}
