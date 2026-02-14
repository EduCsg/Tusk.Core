package com.hydra.core.utils;

import java.util.List;

public class ValidationUtils {

	ValidationUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean isEmpty(String str) {
		return str == null || str.isBlank();
	}

	public static boolean isEmpty(List<?> list) {
		return list == null || list.isEmpty() || list.getFirst() == null;
	}

	public static boolean notEmpty(String value) {
		return value != null && !value.isBlank();
	}

	public static boolean notEmpty(List<?> list) {
		return list != null && !list.isEmpty() && list.getFirst() != null;
	}

	public static boolean isAnyEmpty(String... values) {
		for (String value : values) {
			if (isEmpty(value)) {
				return true;
			}
		}
		return false;
	}

}