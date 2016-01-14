/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.execution;

import java.util.Optional;
import java.util.function.Function;

import org.junit.Ignore;
import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

class RunListenerAdapter extends RunListener {

	private final TestRun testRun;
	private final EngineExecutionListener listener;

	RunListenerAdapter(TestRun testRun, EngineExecutionListener listener) {
		this.testRun = testRun;
		this.listener = listener;
	}

	@Override
	public void testRunStarted(Description description) {
		// If it's not a suite it might be skipped entirely later on.
		if (description.isSuite()) {
			fireExecutionStarted(testRun.getRunnerTestDescriptor());
		}
	}

	@Override
	public void testIgnored(Description description) {
		TestDescriptor testDescriptor = testRun.lookupTestDescriptor(description);
		String reason = determineReasonForIgnoredTest(description);
		testIgnored(testDescriptor, reason);
	}

	@Override
	public void testStarted(Description description) {
		testStarted(testRun.lookupTestDescriptor(description));
	}

	@Override
	public void testAssumptionFailure(Failure failure) {
		handleFailure(failure, TestExecutionResult::aborted);
	}

	@Override
	public void testFailure(Failure failure) {
		handleFailure(failure, TestExecutionResult::failed);
	}

	@Override
	public void testFinished(Description description) {
		testFinished(testRun.lookupTestDescriptor(description));
	}

	@Override
	public void testRunFinished(Result result) {
		if (testRun.isNotSkipped(testRun.getRunnerTestDescriptor())) {
			fireExecutionFinished(testRun.getRunnerTestDescriptor());
		}
	}

	private void handleFailure(Failure failure, Function<Throwable, TestExecutionResult> resultCreator) {
		TestDescriptor testDescriptor = testRun.lookupTestDescriptor(failure.getDescription());
		TestExecutionResult result = resultCreator.apply(failure.getException());
		testRun.storeResult(testDescriptor, result);
		if (testDescriptor.isContainer() && testRun.isDescendantOfRunnerTestDescriptor(testDescriptor)) {
			fireMissingContainerEvents(testDescriptor);
		}
	}

	private void fireMissingContainerEvents(TestDescriptor testDescriptor) {
		if (testRun.isNotStarted(testDescriptor)) {
			testStarted(testDescriptor);
		}
		if (testRun.isNotFinished(testDescriptor)) {
			testFinished(testDescriptor);
		}
	}

	private void testIgnored(TestDescriptor testDescriptor, String reason) {
		fireExecutionStartedIncludingUnstartedAncestors(testDescriptor.getParent());
		fireExecutionSkipped(testDescriptor, reason);
		fireExecutionFinishedIncludingAncestorsWithoutPendingChildren(testDescriptor.getParent());
	}

	private String determineReasonForIgnoredTest(Description description) {
		Ignore ignoreAnnotation = description.getAnnotation(Ignore.class);
		return Optional.ofNullable(ignoreAnnotation).map(Ignore::value).orElse("<unknown>");
	}

	private void testStarted(TestDescriptor testDescriptor) {
		fireExecutionStartedIncludingUnstartedAncestors(testDescriptor.getParent());
		fireExecutionStarted(testDescriptor);
	}

	private void testFinished(TestDescriptor descriptor) {
		fireExecutionFinished(descriptor);
		fireExecutionFinishedIncludingAncestorsWithoutPendingChildren(descriptor.getParent());
	}

	private void fireExecutionStartedIncludingUnstartedAncestors(Optional<TestDescriptor> parent) {
		if (parent.isPresent() && canStart(parent.get())) {
			fireExecutionStartedIncludingUnstartedAncestors(parent.get().getParent());
			fireExecutionStarted(parent.get());
		}
	}

	private void fireExecutionFinishedIncludingAncestorsWithoutPendingChildren(Optional<TestDescriptor> parent) {
		if (parent.isPresent() && canFinish(parent.get())) {
			fireExecutionFinished(parent.get());
			fireExecutionFinishedIncludingAncestorsWithoutPendingChildren(parent.get().getParent());
		}
	}

	private boolean canStart(TestDescriptor testDescriptor) {
		return testRun.isNotStarted(testDescriptor) //
				&& testRun.isDescendantOfRunnerTestDescriptor(testDescriptor);
	}

	private boolean canFinish(TestDescriptor testDescriptor) {
		return testRun.isNotFinished(testDescriptor) //
				&& testRun.isDescendantOfRunnerTestDescriptor(testDescriptor)
				&& testRun.areAllFinishedOrSkipped(testDescriptor.getChildren());
	}

	private void fireExecutionSkipped(TestDescriptor testDescriptor, String reason) {
		testRun.markSkipped(testDescriptor);
		listener.executionSkipped(testDescriptor, reason);
	}

	private void fireExecutionStarted(TestDescriptor testDescriptor) {
		testRun.markStarted(testDescriptor);
		listener.executionStarted(testDescriptor);
	}

	private void fireExecutionFinished(TestDescriptor testDescriptor) {
		testRun.markFinished(testDescriptor);
		listener.executionFinished(testDescriptor, testRun.getStoredResultOrSuccessful(testDescriptor));
	}

}
