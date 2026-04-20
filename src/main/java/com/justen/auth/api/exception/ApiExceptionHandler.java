package com.justen.auth.api.exception;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.justen.auth.api.exception.Error.Field;
import com.justen.auth.domain.exception.BusinessException;
import com.justen.auth.domain.exception.EntityNotFoundException;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	@Autowired
	private MessageSource messageSource;


	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Object> handleBusinessException(BusinessException exception, WebRequest request) {
		
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		Error error = new Error();
		error.setStatus(status.value());
		error.setTitle(exception.getMessage());
		error.setDateTime(OffsetDateTime.now(ZoneOffset.UTC));
		
		if (exception.hasFields()) {
			List<Field> fields = new ArrayList<>();
			
			for (BusinessException.Field field : exception.getFields()) {
				fields.add(new Error.Field(field.getName(), field.getMessage()));
			}
			
			error.setFields(fields);
		}
		
		return handleExceptionInternal(exception, error, new HttpHeaders(), status, request);
	}
	
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException exception, WebRequest request) {
		
		HttpStatus status = HttpStatus.NOT_FOUND;
		
		Error error = new Error();
		error.setStatus(status.value());
		error.setTitle(exception.getMessage());
		error.setDateTime(OffsetDateTime.now(ZoneOffset.UTC));
		
		return handleExceptionInternal(exception, error, new HttpHeaders(), status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		List<Field> fields = new ArrayList<>();

		Error error = new Error();
		error.setStatus(status.value());
		error.setTitle(exception.getMessage());
		error.setDateTime(OffsetDateTime.now(ZoneOffset.UTC));
		error.setFields(fields);

		if (exception.getCause() instanceof JsonMappingException jme) {
			if (jme.getOriginalMessage().startsWith("Unrecognized field")) {
				error.setTitle("Unrecognized field");
			}

			if (jme.getPath() != null && !jme.getPath().isEmpty()) {
				fields.add(new Error.Field(jme.getPath().get(0).getFieldName(), error.getTitle()));
			}
		}

		return handleExceptionInternal(exception, error, new HttpHeaders(), status, request);
	}
	
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		List<Field> fields = new ArrayList<>();
		
		for (ObjectError error : exception.getBindingResult().getAllErrors()) {

			String name = "general";
			
			if (error instanceof FieldError field) {
				name = field.getField();
			}
			
			String message = messageSource.getMessage(error, LocaleContextHolder.getLocale());
			
			fields.add(new Error.Field(name, message));
		}
		
		Error error = new Error();
		error.setStatus(status.value());
		error.setTitle("Invalid arguments.");
		error.setDateTime(OffsetDateTime.now(ZoneOffset.UTC));
		error.setFields(fields);
		
		return handleExceptionInternal(exception, error, headers, status, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		String message = String.format("%s is required.", exception.getParameterName());
		
		Error error = new Error();
		error.setStatus(status.value());
		error.setTitle(message);
		error.setDateTime(OffsetDateTime.now(ZoneOffset.UTC));
		
		return handleExceptionInternal(exception, error, headers, status, request);
	}
	
}
