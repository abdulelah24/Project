/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.provider;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.platform.commons.util.PreconditionViolationException;

class CsvFileArgumentsProviderTests {

	@Test
	void providesArgumentsForNewlineAndComma() {
		Stream<Object[]> arguments = provideArguments("foo, bar \n baz, qux \n", "\n", ',');

		assertThat(arguments).containsExactly(new Object[] { "foo", "bar" }, new Object[] { "baz", "qux" });
	}

	@Test
	void providesArgumentsForCarriageReturnAndSemicolon() {
		Stream<Object[]> arguments = provideArguments("foo; bar \r baz; qux", "\r", ';');

		assertThat(arguments).containsExactly(new Object[] { "foo", "bar" }, new Object[] { "baz", "qux" });
	}

	@Test
	void closesInputStream() {
		AtomicBoolean closed = new AtomicBoolean(false);
		InputStream inputStream = new ByteArrayInputStream("foo".getBytes()) {
			@Override
			public void close() throws IOException {
				closed.set(true);
			}
		};

		Stream<Object[]> arguments = provideArguments(inputStream, "\n", ',');

		assertThat(arguments.count()).isEqualTo(1);
		assertThat(closed.get()).describedAs("closed").isTrue();
	}

	@Test
	void readsFromClasspathResources() {
		CsvFileSource annotation = annotation("/single-column.csv", "ISO-8859-1", "\n", ',');

		Stream<Object[]> arguments = provide(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(new Object[] { "foo" }, new Object[] { "bar" }, new Object[] { "baz" },
			new Object[] { "qux" });
	}

	@Test
	void throwsExceptionForMissingClasspathResource() {
		CsvFileSource annotation = annotation("does-not-exist.csv", "UTF-8", "\n", ',');

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> provide(new CsvFileArgumentsProvider(), annotation));

		assertThat(exception).hasMessageContaining("Classpath resource does not exist: does-not-exist.csv");
	}

	private CsvFileSource annotation(String path, String charset, String value, char value2) {
		CsvFileSource annotation = mock(CsvFileSource.class);
		when(annotation.resource()).thenReturn(path);
		when(annotation.encoding()).thenReturn(charset);
		when(annotation.lineSeparator()).thenReturn(value);
		when(annotation.delimiter()).thenReturn(value2);
		return annotation;
	}

	private Stream<Object[]> provideArguments(String content, String lineSeparator, char delimiter) {
		return provideArguments(new ByteArrayInputStream(content.getBytes(UTF_8)), lineSeparator, delimiter);
	}

	private Stream<Object[]> provideArguments(InputStream inputStream, String lineSeparator, char delimiter) {
		String expectedResource = "foo/bar";
		CsvFileSource annotation = annotation(expectedResource, "ISO-8859-1", lineSeparator, delimiter);

		CsvFileArgumentsProvider provider = new CsvFileArgumentsProvider((testClass, resource) -> {
			assertThat(resource).isEqualTo(expectedResource);
			return inputStream;
		});
		return provide(provider, annotation);
	}

	private Stream<Object[]> provide(CsvFileArgumentsProvider provider, CsvFileSource annotation) {
		provider.initialize(annotation);
		ContainerExtensionContext context = mock(ContainerExtensionContext.class);
		when(context.getTestClass()).thenReturn(Optional.of(CsvFileArgumentsProviderTests.class));
		return provider.arguments(context).map(Arguments::get);
	}

}
