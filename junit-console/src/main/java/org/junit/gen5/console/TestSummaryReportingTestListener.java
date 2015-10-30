/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.launcher.TestPlan;
import org.junit.gen5.launcher.TestPlanExecutionListener;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public class TestSummaryReportingTestListener implements TestPlanExecutionListener {

	private final PrintStream out;

	private final AtomicLong testsStarted = new AtomicLong();
	private final AtomicLong testsFound = new AtomicLong();
	private final AtomicLong testsSkipped = new AtomicLong();
	private final AtomicLong testsAborted = new AtomicLong();
	private final AtomicLong testsSucceeded = new AtomicLong();
	private final AtomicLong testsFailed = new AtomicLong();

	private long timeStarted;
	private long timePaused;
	private long timeFinished;


	public TestSummaryReportingTestListener(PrintStream out) {
		this.out = out;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		this.testsFound.set(testPlan.getNumberOfStaticTests());
		this.timeStarted = System.currentTimeMillis();
	}

	@Override
	public void testPlanExecutionPaused(TestPlan testPlan) {
		this.timePaused = System.currentTimeMillis();
	}

	@Override
	public void testPlanExecutionRestarted(TestPlan testPlan) {
		this.timeStarted += System.currentTimeMillis() - this.timePaused;
		this.timePaused = 0;
	}

	@Override
	public void testPlanExecutionStopped(TestPlan testPlan) {
		reportSummary("Test run stopped");
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		reportSummary("Test run finished");
	}

	private void reportSummary(String msg) {
		this.timeFinished = System.currentTimeMillis();

		// @formatter:off
		out.println(String.format(
			"%s after %d ms\n"
			+ "[%10d tests found     ]\n"
			+ "[%10d tests started   ]\n"
			+ "[%10d tests skipped   ]\n"
			+ "[%10d tests aborted   ]\n"
			+ "[%10d tests failed    ]\n"
			+ "[%10d tests successful]\n",
			msg, (this.timeFinished - this.timeStarted), this.testsFound.get(), this.testsStarted.get(),
			this.testsSkipped.get(), this.testsAborted.get(), this.testsFailed.get(), this.testsSucceeded.get()));
		// @formatter:on
	}

	@Override
	public void testFound(TestDescriptor testDescriptor) {
		this.testsFound.incrementAndGet();
	}

	@Override
	public void testStarted(TestDescriptor testDescriptor) {
		this.testsStarted.incrementAndGet();
	}

	@Override
	public void testSkipped(TestDescriptor testDescriptor, Throwable t) {
		this.testsSkipped.incrementAndGet();
	}

	@Override
	public void testAborted(TestDescriptor testDescriptor, Throwable t) {
		this.testsAborted.incrementAndGet();
	}

	@Override
	public void testFailed(TestDescriptor testDescriptor, Throwable t) {
		this.testsFailed.incrementAndGet();
	}

	@Override
	public void testSucceeded(TestDescriptor testDescriptor) {
		this.testsSucceeded.incrementAndGet();
	}

}
