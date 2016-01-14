/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

import org.junit.gen5.commons.JUnitException;

/**
 * Thrown if an error is encountered regarding the configuration of an
 * extension.
 *
 * @since 5.0
 */
public class ExtensionConfigurationException extends JUnitException {

	private static final long serialVersionUID = -2902318452924798975L;

	public ExtensionConfigurationException(String message) {
		super(message);
	}

}
