/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import java.util.*;

import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestExecutionResult;

/**
 * @since 5.0
 */
public interface TestExecutionListener {

	default void reportingEntryPublished(TestIdentifier testIdentifier, Map<String, String> entry) {

	}

	default void testPlanExecutionStarted(TestPlan testPlan) {
	}

	default void testPlanExecutionFinished(TestPlan testPlan) {
	}

	default void dynamicTestRegistered(TestIdentifier testIdentifier) {
	}

	default void executionSkipped(TestIdentifier testIdentifier, String reason) {
	}

	default void executionStarted(TestIdentifier testIdentifier) {
	}

	default void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
	}

}