/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertThrows;
import static org.junit.gen5.engine.discovery.ClassSelector.forClass;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.util.HashMap;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.TestReporter;
import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.engine.ExecutionEventRecorder;
import org.junit.gen5.launcher.TestDiscoveryRequest;

public class ReportingTest extends AbstractJUnit5TestEngineTests {

	@Test
	public void threeReportEntriesArePublished() {
		TestDiscoveryRequest request = request().select(forClass(MyReportingTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(2L, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2L, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0L, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(6L, eventRecorder.getReportingEntryPublishedCount(), "# report entries published");
	}

	static class MyReportingTestCase {

		@BeforeEach
		void before(TestReporter reporter) {
			reporter.publishEntry(new HashMap<>());
		}

		@AfterEach
		void after(TestReporter reporter) {
			reporter.publishEntry(new HashMap<>());
		}

		@Test
		void succeedingTest(TestReporter reporter) {
			reporter.publishEntry(new HashMap<>());
			reporter.publishEntry("userName", "dk38");
		}

		@Test
		void testWithNullReportData(TestReporter reporter) {
			assertThrows(PreconditionViolationException.class, () -> reporter.publishEntry(null));
		}

	}

}
