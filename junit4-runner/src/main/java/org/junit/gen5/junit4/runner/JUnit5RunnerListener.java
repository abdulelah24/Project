/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.junit4.runner;

import static org.junit.gen5.engine.TestExecutionResult.Status.*;

import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.TestExecutionResult.Status;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

/**
 * @since 5.0
 */
class JUnit5RunnerListener implements TestExecutionListener {

	private final JUnit5TestTree testTree;
	private final RunNotifier notifier;

	JUnit5RunnerListener(JUnit5TestTree testTree, RunNotifier notifier) {
		this.testTree = testTree;
		this.notifier = notifier;
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		System.err.println("JUnit5 Runner does not support dynamic tests.");
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		Description description = findJUnit4Description(testIdentifier);
		this.notifier.fireTestIgnored(description);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		if (testIdentifier.isTest()) {
			Description description = findJUnit4Description(testIdentifier);
			this.notifier.fireTestStarted(description);
		}
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		Description description = findJUnit4Description(testIdentifier);
		Status status = testExecutionResult.getStatus();
		if (status == ABORTED) {
			this.notifier.fireTestAssumptionFailed(toFailure(testExecutionResult, description));
		}
		else if (status == FAILED) {
			this.notifier.fireTestFailure(toFailure(testExecutionResult, description));
		}
		if (testIdentifier.isTest()) {
			this.notifier.fireTestFinished(description);
		}
	}

	private Failure toFailure(TestExecutionResult testExecutionResult, Description description) {
		return new Failure(description, testExecutionResult.getThrowable().orElse(null));
	}

	private Description findJUnit4Description(TestIdentifier testIdentifier) {
		return this.testTree.getDescription(testIdentifier);
	}

}
