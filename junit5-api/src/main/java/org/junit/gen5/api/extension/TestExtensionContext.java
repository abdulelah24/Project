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

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * {@code TestExecutionContext} encapsulates the <em>context</em> in which
 * the current test is being executed.
 *
 * @since 5.0
 */
public interface TestExtensionContext extends ExtensionContext {

	Object getTestInstance();

	Method getTestMethod();
}
