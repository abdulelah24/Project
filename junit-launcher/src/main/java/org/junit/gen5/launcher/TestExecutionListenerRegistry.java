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

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.gen5.engine.TestEngine;

/**
 * @since 5.0
 */
class TestExecutionListenerRegistry {

	private final List<TestExecutionListener> testExecutionListeners = new LinkedList<>();

	void registerListener(TestExecutionListener... listeners) {
		for (TestExecutionListener listener : listeners) {
			this.testExecutionListeners.add(listener);
		}
	}

	private void notifyTestExecutionListeners(Consumer<TestExecutionListener> consumer) {
		this.testExecutionListeners.forEach(consumer);
	}

	TestExecutionListener getCompositeTestExecutionListener() {
		return new CompositeTestExecutionListener();
	}

	private class CompositeTestExecutionListener implements TestExecutionListener {

		@Override
		public void dynamicTestFound(TestIdentifier testIdentifier) {
			notifyTestExecutionListeners(listener -> listener.dynamicTestFound(testIdentifier));
		}

		@Override
		public void testStarted(TestIdentifier testIdentifier) {
			notifyTestExecutionListeners(listener -> listener.testStarted(testIdentifier));
		}

		@Override
		public void testSkipped(TestIdentifier testIdentifier, Throwable t) {
			notifyTestExecutionListeners(listener -> listener.testSkipped(testIdentifier, t));
		}

		@Override
		public void testAborted(TestIdentifier testIdentifier, Throwable t) {
			notifyTestExecutionListeners(listener -> listener.testAborted(testIdentifier, t));
		}

		@Override
		public void testFailed(TestIdentifier testIdentifier, Throwable t) {
			notifyTestExecutionListeners(listener -> listener.testFailed(testIdentifier, t));
		}

		@Override
		public void testSucceeded(TestIdentifier testIdentifier) {
			notifyTestExecutionListeners(listener -> listener.testSucceeded(testIdentifier));
		}

		@Override
		public void testPlanExecutionStarted(TestPlan testPlan) {
			notifyTestExecutionListeners(listener -> listener.testPlanExecutionStarted(testPlan));
		}

		@Override
		public void testPlanExecutionPaused(TestPlan testPlan) {
			notifyTestExecutionListeners(listener -> listener.testPlanExecutionPaused(testPlan));
		}

		@Override
		public void testPlanExecutionRestarted(TestPlan testPlan) {
			notifyTestExecutionListeners(listener -> listener.testPlanExecutionRestarted(testPlan));
		}

		@Override
		public void testPlanExecutionStopped(TestPlan testPlan) {
			notifyTestExecutionListeners(listener -> listener.testPlanExecutionStopped(testPlan));
		}

		@Override
		public void testPlanExecutionFinished(TestPlan testPlan) {
			notifyTestExecutionListeners(listener -> listener.testPlanExecutionFinished(testPlan));
		}

		@Override
		public void testPlanExecutionStartedOnEngine(TestPlan testPlan, TestEngine testEngine) {
			notifyTestExecutionListeners(listener -> listener.testPlanExecutionStartedOnEngine(testPlan, testEngine));
		}

		@Override
		public void testPlanExecutionFinishedOnEngine(TestPlan testPlan, TestEngine testEngine) {
			notifyTestExecutionListeners(listener -> listener.testPlanExecutionFinishedOnEngine(testPlan, testEngine));
		}
	}
}