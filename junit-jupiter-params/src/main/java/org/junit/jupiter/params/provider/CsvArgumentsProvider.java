/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.junit.jupiter.params.provider.CsvParserFactory.createParserFor;
import static org.junit.platform.commons.util.CollectionUtils.toSet;

import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.univocity.parsers.csv.CsvParser;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.UnrecoverableExceptions;

/**
 * @since 5.0
 */
class CsvArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<CsvSource> {

	private static final String LINE_SEPARATOR = "\n";

	private CsvSource annotation;
	private Set<String> nullValues;
	private CsvParser csvParser;

	@Override
	public void accept(CsvSource annotation) {
		this.annotation = annotation;
		this.nullValues = toSet(annotation.nullValues());
		this.csvParser = createParserFor(annotation);
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		final boolean textBlockDeclared = !this.annotation.textBlock().isEmpty();
		Preconditions.condition(this.annotation.value().length > 0 ^ textBlockDeclared,
			() -> "@CsvSource must be declared with either `value` or `textBlock` but not both");

		if (textBlockDeclared) {
			return parseTextBlock(this.annotation.textBlock()).stream().map(Arguments::of);
		}

		AtomicInteger index = new AtomicInteger(0);
		// @formatter:off
		return Arrays.stream(this.annotation.value())
				.map(line -> parseLine(line, index.incrementAndGet()))
				.map(Arguments::of);
		// @formatter:on
	}

	private List<String[]> parseTextBlock(String textBlock) {
		try {
			AtomicInteger index = new AtomicInteger(0);
			List<String[]> csvRecords = this.csvParser.parseAll(new StringReader(textBlock));
			for (String[] csvRecord : csvRecords) {
				index.incrementAndGet();
				Preconditions.notNull(csvRecord,
					() -> "Line at index " + index.get() + " contains invalid CSV: \"\"\"\n" + textBlock + "\n\"\"\"");
				processNullValues(csvRecord, this.nullValues);
			}
			return csvRecords;
		}
		catch (Throwable throwable) {
			throw handleCsvException(throwable, this.annotation);
		}
	}

	private String[] parseLine(String line, int index) {
		try {
			String[] csvRecord = this.csvParser.parseLine(line + LINE_SEPARATOR);
			Preconditions.notNull(csvRecord,
				() -> "Line at index " + index + " contains invalid CSV: \"" + line + "\"");
			processNullValues(csvRecord, this.nullValues);
			return csvRecord;
		}
		catch (Throwable throwable) {
			throw handleCsvException(throwable, this.annotation);
		}
	}

	static void processNullValues(String[] csvRecord, Set<String> nullValues) {
		if (!nullValues.isEmpty()) {
			for (int i = 0; i < csvRecord.length; i++) {
				if (nullValues.contains(csvRecord[i])) {
					csvRecord[i] = null;
				}
			}
		}
	}

	/**
	 * @return this method always throws an exception and therefore never
	 * returns anything; the return type is merely present to allow this
	 * method to be supplied as the operand in a {@code throw} statement
	 */
	static RuntimeException handleCsvException(Throwable throwable, Annotation annotation) {
		UnrecoverableExceptions.rethrowIfUnrecoverable(throwable);
		if (throwable instanceof PreconditionViolationException) {
			throw (PreconditionViolationException) throwable;
		}
		throw new CsvParsingException("Failed to parse CSV input configured via " + annotation, throwable);
	}

}
