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

/**
 * @since 5.0
 */
public interface TestExecutionListener {

	default void reportingEntryPublished(TestIdentifier testIdentifier, Map<String, String> entry) {

	}

	default void testPlanExecutionStarted(TestPlan testPlan) {
	}

	default void testPlanExecutionPaused(TestPlan testPlan) {
	}

	default void testPlanExecutionRestarted(TestPlan testPlan) {
	}

	default void testPlanExecutionStopped(TestPlan testPlan) {
	}

	default void testPlanExecutionFinished(TestPlan testPlan) {
	}

	default void testPlanExecutionStartedOnEngine(TestPlan testPlan, TestEngine testEngine) {
	}

	default void testPlanExecutionFinishedOnEngine(TestPlan testPlan, TestEngine testEngine) {
	}

	default void dynamicTestFound(TestIdentifier testIdentifier) {
	}

	default void testStarted(TestIdentifier testIdentifier) {
	}

	default void testSkipped(TestIdentifier testIdentifier, Throwable t) {
	}

	default void testAborted(TestIdentifier testIdentifier, Throwable t) {
	}

	default void testFailed(TestIdentifier testIdentifier, Throwable t) {
	}

	default void testSucceeded(TestIdentifier testIdentifier) {
	}
}