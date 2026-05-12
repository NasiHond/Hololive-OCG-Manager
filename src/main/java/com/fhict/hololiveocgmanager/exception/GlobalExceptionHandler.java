package com.fhict.hololiveocgmanager.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final URI TYPE_BAD_REQUEST = URI.create("urn:problem-type:bad-request");
	private static final URI TYPE_UNAUTHORIZED = URI.create("urn:problem-type:unauthorized");
	private static final URI TYPE_FORBIDDEN = URI.create("urn:problem-type:forbidden");
	private static final URI TYPE_NOT_FOUND = URI.create("urn:problem-type:not-found");
	private static final URI TYPE_INTERNAL_SERVER_ERROR = URI.create("urn:problem-type:internal-server-error");

	@ExceptionHandler(ApiException.class)
	public ProblemDetail handleApiException(ApiException ex, HttpServletRequest request) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
		problemDetail.setType(ex.getType());
		problemDetail.setTitle(ex.getTitle());
		applyStandardProperties(problemDetail, request);
		return problemDetail;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
		problemDetail.setType(TYPE_BAD_REQUEST);
		problemDetail.setTitle("Validation Error");

		List<Map<String, String>> errors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(this::mapFieldError)
				.toList();
		problemDetail.setProperty("errors", errors);
		applyStandardProperties(problemDetail, request);
		return problemDetail;
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
		problemDetail.setType(TYPE_BAD_REQUEST);
		problemDetail.setTitle("Validation Error");
		problemDetail.setProperty("errors", ex.getConstraintViolations().stream()
				.map(violation -> Map.of(
						"field", String.valueOf(violation.getPropertyPath()),
						"message", violation.getMessage()))
				.toList());
		applyStandardProperties(problemDetail, request);
		return problemDetail;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed request body");
		problemDetail.setType(TYPE_BAD_REQUEST);
		problemDetail.setTitle("Bad Request");
		applyStandardProperties(problemDetail, request);
		return problemDetail;
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ProblemDetail handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
		HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getReason());
		problemDetail.setType(typeForStatus(status));
		problemDetail.setTitle(status.getReasonPhrase());
		applyStandardProperties(problemDetail, request);
		return problemDetail;
	}

	@ExceptionHandler(AuthenticationException.class)
	public ProblemDetail handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Authentication failed");
		problemDetail.setType(TYPE_UNAUTHORIZED);
		problemDetail.setTitle("Unauthorized");
		applyStandardProperties(problemDetail, request);
		return problemDetail;
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access denied");
		problemDetail.setType(TYPE_FORBIDDEN);
		problemDetail.setTitle("Forbidden");
		applyStandardProperties(problemDetail, request);
		return problemDetail;
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problemDetail.setType(TYPE_BAD_REQUEST);
		problemDetail.setTitle("Bad Request");
		applyStandardProperties(problemDetail, request);
		return problemDetail;
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
		problemDetail.setType(TYPE_INTERNAL_SERVER_ERROR);
		problemDetail.setTitle("Internal Server Error");
		applyStandardProperties(problemDetail, request);
		return problemDetail;
	}

	private void applyStandardProperties(ProblemDetail problemDetail, HttpServletRequest request) {
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("timestamp", Instant.now().toString());
	}

	private Map<String, String> mapFieldError(FieldError error) {
		return Map.of(
				"field", error.getField(),
				"message", error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage());
	}

	private URI typeForStatus(HttpStatus status) {
		return switch (status) {
			case BAD_REQUEST -> TYPE_BAD_REQUEST;
			case UNAUTHORIZED -> TYPE_UNAUTHORIZED;
			case FORBIDDEN -> TYPE_FORBIDDEN;
			case NOT_FOUND -> TYPE_NOT_FOUND;
			default -> TYPE_INTERNAL_SERVER_ERROR;
		};
	}
}
