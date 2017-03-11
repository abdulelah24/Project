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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class CsvArgumentsProviderTests {

	@Test
	void providesSingleArgument() {
		Stream<Object[]> arguments = provideArguments(',', "foo");

		assertThat(arguments).containsExactly(new String[] { "foo" });
	}

	@Test
	void providesMultipleArguments() {
		Stream<Object[]> arguments = provideArguments(',', "foo", "bar");

		assertThat(arguments).containsExactly(new String[] { "foo" }, new String[] { "bar" });
	}

	@Test
	void splitsAndTrimsArguments() {
		Stream<Object[]> arguments = provideArguments('|', " foo | bar ");

		assertThat(arguments).containsExactly(new String[] { "foo", "bar" });
	}

	@Test
	void understandsQuotes() {
		Stream<Object[]> arguments = provideArguments(',', "\"foo, bar\"");

		assertThat(arguments).containsExactly(new String[] { "foo, bar" });
	}

	@Test
	void understandsEscapeCharacters() {
		Stream<Object[]> arguments = provideArguments(',', "\"foo or \"\"bar\"\"\", baz");

		assertThat(arguments).containsExactly(new String[] { "foo or \"bar\"", "baz" });
	}

	private Stream<Object[]> provideArguments(char delimiter, String... value) {
		CsvSource annotation = mock(CsvSource.class);
		when(annotation.value()).thenReturn(value);
		when(annotation.delimiter()).thenReturn(delimiter);

		CsvArgumentsProvider provider = new CsvArgumentsProvider();
		provider.initialize(annotation);
		return provider.arguments(null).map(Arguments::get);
	}

}
