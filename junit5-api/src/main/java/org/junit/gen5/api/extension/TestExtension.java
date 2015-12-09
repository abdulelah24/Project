/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

/**
 * Interface to be implemented by all test extensions.
 * <p>
 * {@code TestExtensions} can be registered via {@link ExtendWith @ExtendWith}.
 *
 * @since 5.0
 * @see ExtensionPointRegistry
 */
public interface TestExtension {

	void registerExtensionPoints(ExtensionPointRegistry registry);

}
