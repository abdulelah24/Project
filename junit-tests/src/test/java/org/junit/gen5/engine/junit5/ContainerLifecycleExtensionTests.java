/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static java.util.Arrays.asList;
import static org.junit.gen5.engine.TestPlanSpecification.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.AfterAllExtensionPoint;
import org.junit.gen5.api.extension.BeforeAllExtensionPoint;
import org.junit.gen5.api.extension.ContainerExtensionContext;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.ExtensionPointRegistry;
import org.junit.gen5.api.extension.TestExtension;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * Integration tests that verify support of, and {@link org.junit.gen5.api.extension.AfterAllExtensionPoint} and
 * {@link org.junit.gen5.api.extension.BeforeAllExtensionPoint} in the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
@Ignore("https://github.com/junit-team/junit-lambda/issues/39")
public class ContainerLifecycleExtensionTests extends AbstractJUnit5TestEngineTestCase {

	@org.junit.Before
	public void reset() {
		preBeforeAllMethods.clear();
		postAfterAllMethods.clear();
	}

	@org.junit.Test
	public void beforeAllAndAfterAllCallbacksWithTestInstancePerMethod() {
		TestPlanSpecification spec = build(forClass(ContainerLifecyleTestCase.class));

		TrackingTestExecutionListener listener = executeTests(spec, 2);

		Assert.assertEquals("# tests started", 1, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 1, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 0, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());

		Assert.assertTrue("@BeforeAll was not invoked", ContainerLifecyleTestCase.beforeAllInvoked);
		Assert.assertTrue("@AfterAll was not invoked", ContainerLifecyleTestCase.afterAllInvoked);

		Assert.assertEquals("preBeforeAll()", asList("foo", "bar"), preBeforeAllMethods);
		Assert.assertEquals("postAfterAll()", asList("bar", "foo"), postAfterAllMethods);
	}

	// -------------------------------------------------------------------

	@ExtendWith({ FooContainerLifecycleExtension.class, BarContainerLifecycleExtension.class })
	private static class ContainerLifecyleTestCase {

		static boolean beforeAllInvoked = false;

		static boolean afterAllInvoked = false;

		@BeforeAll
		// MUST be static for TestInstance.Lifecycle.PER_METHOD!
		static void beforeAll() {
			beforeAllInvoked = true;
		}

		@AfterAll
		// MUST be static for TestInstance.Lifecycle.PER_METHOD!
		static void afterAll() {
			afterAllInvoked = true;
		}

		@Test
		void alwaysPasses() {
			/* no-op */
		}
	}

	private static List<String> preBeforeAllMethods = new ArrayList<>();
	private static List<String> postAfterAllMethods = new ArrayList<>();

	private static class FooContainerLifecycleExtension implements TestExtension {

		@Override
		public void registerExtensionPoints(ExtensionPointRegistry registry) {
			registry.register(this::beforeAll, BeforeAllExtensionPoint.class);
			registry.register(this::afterAll, AfterAllExtensionPoint.class);
		}

		private void beforeAll(ContainerExtensionContext containerExtensionContext) {
			preBeforeAllMethods.add("foo");
		}

		private void afterAll(ContainerExtensionContext containerExtensionContext) {
			postAfterAllMethods.add("foo");
		}

	}

	private static class BarContainerLifecycleExtension implements TestExtension {

		@Override
		public void registerExtensionPoints(ExtensionPointRegistry registry) {
			registry.register(this::beforeAll, BeforeAllExtensionPoint.class);
			registry.register(this::afterAll, AfterAllExtensionPoint.class);
		}

		private void beforeAll(ContainerExtensionContext containerExtensionContext) {
			preBeforeAllMethods.add("bar");
		}

		private void afterAll(ContainerExtensionContext containerExtensionContext) {
			postAfterAllMethods.add("bar");
		}
	}

}
