/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.aggregator;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;

/**
 * {@code ArgumentsAccessorException} is an Exception thrown by an
 * {@link ArgumentsAccessor} when an error occurs during
 * access or conversion of an argument.
 *
 * @since 5.2
 * @see ArgumentsAccessor
 */
@API(status = EXPERIMENTAL, since = "5.2")
public class ArgumentsAccessorException extends JUnitException {

	private static final long serialVersionUID = 1L;

	public ArgumentsAccessorException(String message) {
		super(message);
	}

	public ArgumentsAccessorException(String message, Throwable cause) {
		super(message, cause);
	}
}
