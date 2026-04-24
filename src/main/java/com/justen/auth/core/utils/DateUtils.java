package com.justen.auth.core.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Component
public class DateUtils {

	public OffsetDateTime toOffsetDateTime(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof OffsetDateTime odt) {
			return odt;
		}
		if (value instanceof Timestamp ts) {
			return ts.toInstant().atOffset(ZoneOffset.UTC);
		}
		if (value instanceof LocalDateTime ldt) {
			return ldt.atOffset(ZoneOffset.UTC);
		}
		return OffsetDateTime.parse(value.toString());
	}
	
}
