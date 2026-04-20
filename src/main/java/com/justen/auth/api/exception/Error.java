package com.justen.auth.api.exception;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @since   2023-06-01
 * @version 0.0.1
 * @author  <a href="http://vertis-solutions.com">Vertis Solutions</a>
 */
@JsonInclude(Include.NON_NULL)
@Data
public class Error {

	private Integer status;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
	private OffsetDateTime dateTime;
	
	private String title;
	private List<Field> fields;
	
	@Data
	@AllArgsConstructor
	public static class Field {
		private String name;
		private String message;
	}

}
