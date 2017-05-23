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

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@code Arguments} is an abstraction that provides access to an array of
 * objects to be used for invoking a {@code @ParameterizedTest} method.
 *
 * <p>A {@link java.util.stream.Stream} of such {@code Arguments} will
 * typically be provided by an {@link ArgumentsProvider}.
 *
 * @since 5.0
 * @see org.junit.jupiter.params.ParameterizedTest
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.provider.ArgumentsProvider
 */
@API(Experimental)
public interface Arguments {

	/**
	 * Returns the arguments used for an invocation of the
	 * {@code @ParameterizedTest} method.
	 *
	 * @return the arguments
	 */
	Object[] get();

	/**
	 * Factory method for creating an instance of {@code Arguments} based on
	 * the supplied {@code arguments}.
	 *
	 * @param arguments the arguments to be used for an invocation of the test method
	 * @return an instance of {@code Arguments}
	 */
	static Arguments arguments(Object... arguments) {
		Preconditions.notNull(arguments, "argument array must not be null");
		return () -> arguments;
	}

}
