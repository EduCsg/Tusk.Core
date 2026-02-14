package com.hydra.core.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

	static Stream<List<?>> emptyLists() {
		List<String> listWithNull = new ArrayList<>();
		listWithNull.add(null);
		return Stream.of(null, List.of(), listWithNull);
	}

	static Stream<List<?>> nonEmptyLists() {
		return Stream.of(List.of("value"), List.of(1), List.of(new Object()));
	}

	@Test
	void shouldThrowExceptionWhenInstantiating() {
		assertThrows(IllegalStateException.class, ValidationUtils::new);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   " })
	void shouldReturnTrueForNullOrBlankStrings(String input) {
		assertTrue(ValidationUtils.isEmpty(input));
	}

	@ParameterizedTest
	@ValueSource(strings = { "a", "abc", "  abc  " })
	void shouldReturnFalseForNonBlankStrings(String input) {
		assertFalse(ValidationUtils.isEmpty(input));
	}

	@ParameterizedTest
	@MethodSource("emptyLists")
	void shouldReturnTrueForEmptyScenarios(List<?> input) {
		assertTrue(ValidationUtils.isEmpty(input));
	}

	@ParameterizedTest
	@MethodSource("nonEmptyLists")
	void shouldReturnFalseForNonEmptyScenarios(List<?> input) {
		assertFalse(ValidationUtils.isEmpty(input));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   " })
	void notEmpty_shouldReturnFalse_whenNullOrBlank(String input) {
		assertFalse(ValidationUtils.notEmpty(input));
	}

	@ParameterizedTest
	@ValueSource(strings = { "a", "abc", "  abc  " })
	void notEmpty_shouldReturnTrue_whenHasContent(String input) {
		assertTrue(ValidationUtils.notEmpty(input));
	}

	@ParameterizedTest
	@MethodSource("emptyLists")
	void notEmpty_shouldReturnFalse_whenInvalid(List<?> input) {
		assertFalse(ValidationUtils.notEmpty(input));
	}

	@ParameterizedTest
	@MethodSource("nonEmptyLists")
	void notEmpty_shouldReturnTrue_whenValid(List<?> input) {
		assertTrue(ValidationUtils.notEmpty(input));
	}

	@Test
	void isAnyEmpty_shouldReturnTrue_whenAtLeastOneIsEmpty() {
		assertTrue(ValidationUtils.isAnyEmpty("abc", "", "def"));
		assertTrue(ValidationUtils.isAnyEmpty("abc", null));
		assertTrue(ValidationUtils.isAnyEmpty("   ", "xyz"));
	}

	@Test
	void isAnyEmpty_shouldReturnFalse_whenAllHaveContent() {
		assertFalse(ValidationUtils.isAnyEmpty("a", "b", "c"));
		assertFalse(ValidationUtils.isAnyEmpty("  a  ", "xyz"));
	}

	@Test
	void isAnyEmpty_shouldReturnFalse_whenNoValuesProvided() {
		assertFalse(ValidationUtils.isAnyEmpty());
	}

	@Test
	void isAnyEmpty_shouldReturnTrue_whenSecondElementIsEmpty() {
		assertTrue(ValidationUtils.isAnyEmpty("valid", "", "another"));
	}

}