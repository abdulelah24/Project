/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.TrackLogRecords;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.DemoMethodTestDescriptor;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

class TestExecutionListenerRegistryTest {
	private TestExecutionListener compositeTestExecutionListener;

	@BeforeEach
	void setUp() {
		TestExecutionListenerRegistry executionListenerRegistry = new TestExecutionListenerRegistry();
		executionListenerRegistry.registerListeners(new ThrowableTestExecutionListener());
		compositeTestExecutionListener = executionListenerRegistry.getCompositeTestExecutionListener();
	}

	@Test
	@TrackLogRecords
	void shouldNotThrowExceptionButLogIfDynamicTestRegisteredListenerMethodFails(LogRecordListener logRecordListener) {
		TestIdentifier testIdentifier = getSampleMethodTestIdentifier();

		compositeTestExecutionListener.dynamicTestRegistered(testIdentifier);

		assertThatTestListenerErrorLogged(logRecordListener, "dynamicTestRegistered");
	}

	@Test
	@TrackLogRecords
	void shouldNotThrowExceptionButLogIfExecutionStartedListenerMethodFails(LogRecordListener logRecordListener) {
		TestIdentifier testIdentifier = getSampleMethodTestIdentifier();

		compositeTestExecutionListener.executionStarted(testIdentifier);

		assertThatTestListenerErrorLogged(logRecordListener, "executionStarted");
	}

	@Test
	@TrackLogRecords
	void shouldNotThrowExceptionButLogIfExecutionSkippedListenerMethodFails(LogRecordListener logRecordListener) {
		TestIdentifier testIdentifier = getSampleMethodTestIdentifier();

		compositeTestExecutionListener.executionSkipped(testIdentifier, "deliberately skipped container");

		assertThatTestListenerErrorLogged(logRecordListener, "executionSkipped");
	}

	@Test
	@TrackLogRecords
	void shouldNotThrowExceptionButLogIfExecutionFinishedListenerMethodFails(LogRecordListener logRecordListener) {
		TestIdentifier testIdentifier = getSampleMethodTestIdentifier();

		compositeTestExecutionListener.executionFinished(testIdentifier, mock(TestExecutionResult.class));

		assertThatTestListenerErrorLogged(logRecordListener, "executionFinished");
	}

	@Test
	@TrackLogRecords
	void shouldNotThrowExceptionButLogIfReportingEntryPublishedListenerMethodFails(
			LogRecordListener logRecordListener) {
		TestIdentifier testIdentifier = getSampleMethodTestIdentifier();

		compositeTestExecutionListener.reportingEntryPublished(testIdentifier, ReportEntry.from("one", "two"));

		assertThatTestListenerErrorLogged(logRecordListener, "reportingEntryPublished");
	}

	@Test
	@TrackLogRecords
	void shouldNotThrowExceptionButLogIfTesPlanExecutionStartedListenerMethodFails(
			LogRecordListener logRecordListener) {
		DemoMethodTestDescriptor testDescriptor = getDemoMethodTestDescriptor();

		compositeTestExecutionListener.testPlanExecutionStarted(TestPlan.from(Collections.singleton(testDescriptor)));

		assertThatTestPlanListenerErrorLogged(logRecordListener, "testPlanExecutionStarted");
	}

	@TrackLogRecords
	void shouldNotThrowExceptionButLogIfTesPlanExecutionFinishedListenerMethodFails(
			LogRecordListener logRecordListener) {
		DemoMethodTestDescriptor testDescriptor = getDemoMethodTestDescriptor();

		compositeTestExecutionListener.testPlanExecutionFinished(TestPlan.from(Collections.singleton(testDescriptor)));

		assertThatTestPlanListenerErrorLogged(logRecordListener, "testPlanExecutionFinished");
	}

	@Test
	@TrackLogRecords
	void shouldOutOfMemoryExceptionAndStopListenerWithoutLog(LogRecordListener logRecordListener) {
		TestExecutionListenerRegistry registry = new TestExecutionListenerRegistry();
		registry.registerListeners(new TestExecutionListener() {
			@Override
			public void executionStarted(TestIdentifier testIdentifier) {
				throw new OutOfMemoryError();
			}
		});
		TestIdentifier testIdentifier = getSampleMethodTestIdentifier();
		assertThatThrownBy(() -> {
			registry.getCompositeTestExecutionListener().executionStarted(testIdentifier);
		}).isInstanceOf(OutOfMemoryError.class);

		assertNotLogs(logRecordListener);
	}

	private LogRecord firstErrorLogRecord(LogRecordListener logRecordListener) throws AssertionError {
		return logRecordListener.stream(Level.SEVERE).findFirst().orElseThrow(
			() -> new AssertionError("Failed to find error log record"));
	}

	private void assertNotLogs(LogRecordListener logRecordListener) throws AssertionError {
		assertThat(logRecordListener.stream(Level.SEVERE).count()).isZero();
	}

	private TestIdentifier getSampleMethodTestIdentifier() {
		DemoMethodTestDescriptor demoMethodTestDescriptor = getDemoMethodTestDescriptor();
		return TestIdentifier.from(demoMethodTestDescriptor);
	}

	private void assertThatTestListenerErrorLogged(LogRecordListener logRecordListener, final String methodName) {
		assertThat(firstErrorLogRecord(logRecordListener).getMessage()).isEqualTo(
			"Failed to invoke ExecutionListener [org.junit.platform.launcher.core.ThrowableTestExecutionListener] for method ["
					+ methodName + "] with test display name [nothing()]");
	}

	private void assertThatTestPlanListenerErrorLogged(LogRecordListener logRecordListener, final String planName) {
		assertThat(firstErrorLogRecord(logRecordListener).getMessage()).isEqualTo(
			"Failed to invoke ExecutionListener [org.junit.platform.launcher.core.ThrowableTestExecutionListener] for method ["
					+ planName + "] for test plan [org.junit.platform.launcher.TestPlan]");
	}

	private DemoMethodTestDescriptor getDemoMethodTestDescriptor() {
		Method localMethodNamedNothing = ReflectionUtils.findMethod(this.getClass(), "nothing", new Class<?>[0]).get();
		return new DemoMethodTestDescriptor(UniqueId.root("method", "unique_id"), this.getClass(),
			localMethodNamedNothing);
	}

	//for reflection purposes only
	void nothing() {
	}
}
