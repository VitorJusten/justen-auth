package com.justen.auth.domain.exception;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private List<Field> fields = new ArrayList<>();
	
	public BusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public BusinessException(String message) {
		super(message);
	}

	public BusinessException(Field field) {
		super("Invalid arguments.");
		this.fields = new ArrayList<>();
		this.fields.add(field);
	}

	public BusinessException(List<Field> fields) {
		super("Invalid arguments.");
		this.fields = fields;
	}
	
	public List<Field> getFields() {
		return this.fields;
	}
	
	public boolean hasFields() {
		return !this.fields.isEmpty();
	}

	@Data
	@AllArgsConstructor
	public static class Field {
        private String name;
        private String message;
    }
	
}
