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

import static org.junit.gen5.api.Assertions.*;
import static org.junit.gen5.api.Assumptions.assumeTrue;
import static org.junit.gen5.engine.TestPlanSpecification.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Assert;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TrackingEngineExecutionListener;

/**
 * Core integration tests for the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */

public class CoreJUnit5TestEngineTests extends AbstractJUnit5TestEngineTests {

	@Test
	public void executeCompositeTestPlanSpecification() {
		TestPlanSpecification spec = build(
			forUniqueId("junit5:org.junit.gen5.engine.junit5.CoreJUnit5TestEngineTests$LocalTestCase#alwaysPasses()"),
			forClass(LocalTestCase.class));

		TrackingEngineExecutionListener listener = executeTests(spec, 6);

		Assert.assertEquals("# tests started", 6, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 3, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 0, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 1, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 2, listener.testFailedCount.get());
	}

	@Test
	public void executeTestsForClass() {
		LocalTestCase.countAfterInvoked = 0;

		TrackingEngineExecutionListener listener = executeTestsForClass(LocalTestCase.class, 6);

		Assert.assertEquals("# tests started", 6, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 3, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 0, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 1, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 2, listener.testFailedCount.get());

		Assert.assertEquals("# after calls", 6, LocalTestCase.countAfterInvoked);
	}

	@Test
	public void executeTestForUniqueId() {
		TestPlanSpecification spec = build(
			forUniqueId("junit5:org.junit.gen5.engine.junit5.CoreJUnit5TestEngineTests$LocalTestCase#alwaysPasses()"));

		TrackingEngineExecutionListener listener = executeTests(spec, 1);

		assertEquals(1, listener.testStartedCount.get(), "# tests started");
		assertEquals(1, listener.testSucceededCount.get(), "# tests succeeded");
		assertEquals(0, listener.testSkippedCount.get(), "# tests skipped");
		assertEquals(0, listener.testAbortedCount.get(), "# tests aborted");
		assertEquals(0, listener.testFailedCount.get(), "# tests failed");
	}

	@Test
	public void executeTestForUniqueIdWithExceptionThrownInAfterMethod() {
		TestPlanSpecification spec = build(forUniqueId(
			"junit5:org.junit.gen5.engine.junit5.CoreJUnit5TestEngineTests$LocalTestCase#throwExceptionInAfterMethod()"));

		TrackingEngineExecutionListener listener = executeTests(spec, 1);

		assertEquals(1, listener.testStartedCount.get(), "# tests started");
		assertEquals(0, listener.testSucceededCount.get(), "# tests succeeded");
		assertEquals(0, listener.testSkippedCount.get(), "# tests skipped");
		assertEquals(0, listener.testAbortedCount.get(), "# tests aborted");
		assertEquals(1, listener.testFailedCount.get(), "# tests failed");
	}

	// -------------------------------------------------------------------

	private static abstract class AbstractTestCase {

		@Test
		void fromSuperclass() {
			/* no-op */
		}
	}

	private static class LocalTestCase extends AbstractTestCase {

		boolean beforeInvoked = false;

		boolean throwExceptionInAfterMethod = false;

		static int countAfterInvoked = 0;

		@BeforeEach
		void before() {
			this.beforeInvoked = true;
			// Reset state, since the test instance is retained across all test methods;
			// otherwise, after() always throws an exception.
			this.throwExceptionInAfterMethod = false;
		}

		@AfterEach
		void after() {
			countAfterInvoked++;
			if (this.throwExceptionInAfterMethod) {
				throw new RuntimeException("Exception thrown from @AfterEach method");
			}
		}

		@Test
		void methodLevelCallbacks() {
			assertTrue(this.beforeInvoked, "@BeforeEach was not invoked on instance method");
		}

		@Test
		void throwExceptionInAfterMethod() {
			this.throwExceptionInAfterMethod = true;
		}

		@Test
		void alwaysPasses() {
			/* no-op */
		}

		@CustomTestAnnotation
		void customTestAnnotation() {
			/* no-op */
		}

		@Test
		void aborted() {
			assumeTrue(false);
		}

		@Test
		void alwaysFails() {
			fail("#fail");
		}

	}

	@Test
	@Retention(RetentionPolicy.RUNTIME)
	@interface CustomTestAnnotation {
	}

}
