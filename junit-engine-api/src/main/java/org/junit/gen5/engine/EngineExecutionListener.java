/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.util.*;

/**
 * @since 5.0
 */
public interface EngineExecutionListener {

	default void reportingEntryPublished(TestDescriptor testDescriptor, Map<String, String> entry) {
	}
    default void dynamicTestRegistered(TestDescriptor testDescriptor) {
	}

	void executionSkipped(TestDescriptor testDescriptor, String reason);

	void executionStarted(TestDescriptor testDescriptor);

	void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult);

}