/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import org.junit.platform.commons.meta.API;

/**
 * {@code RepetitionInfo} is used to inject information about the current
 * repetition of a repeated test into {@code @RepeatedTest}, {@code @BeforeEach},
 * and {@code @AfterEach} methods.
 *
 * <p>If a method parameter is of type {@code RepetitionInfo}, JUnit will
 * supply an instance of {@code RepetitionInfo} corresponding to the current
 * repeated test as the value for the parameter.
 *
 * @since 5.0
 * @see RepeatedTest
 * @see TestInfo
 */
@API(Experimental)
public interface RepetitionInfo {

	/**
	 * Get the current repetition of the corresponding
	 * {@link RepeatedTest @RepeatedTest} method.
	 */
	int getCurrentRepetition();

	/**
	 * Get the total number of repetitions of the corresponding
	 * {@link RepeatedTest @RepeatedTest} method.
	 *
	 * @see RepeatedTest#value
	 */
	int getTotalRepetitions();

}
