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
import static org.junit.gen5.api.Assertions.assertNotNull;
import static org.junit.gen5.api.Assertions.assertTrue;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.DisplayName;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.TestInfo;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.engine.junit5.execution.injection.sample.CustomAnnotation;
import org.junit.gen5.engine.junit5.execution.injection.sample.CustomAnnotationParameterResolver;
import org.junit.gen5.engine.junit5.execution.injection.sample.CustomType;
import org.junit.gen5.engine.junit5.execution.injection.sample.CustomTypeParameterResolver;

/**
 * Integration tests that verify support for {@link MethodParameterResolver}
 * in the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public class ParameterResolverTests extends AbstractJUnit5TestEngineTests {

	@Test
	public void executeTestsForMethodInjectionCases() {
		executeTestsForClass(MethodInjectionTestCase.class);

		assertEquals(8, tracker.testStartedCount.get(), "# tests started");
		assertEquals(7, tracker.testSucceededCount.get(), "# tests succeeded");
		assertEquals(0, tracker.testSkippedCount.get(), "# tests skipped");
		assertEquals(0, tracker.testAbortedCount.get(), "# tests aborted");
		assertEquals(1, tracker.testFailedCount.get(), "# tests failed");
	}

	@Test
	public void executeTestsForMethodInjectionInBeforeAndAfterEachMethods() {
		executeTestsForClass(BeforeAndAfterMethodInjectionTestCase.class);

		assertEquals(1, tracker.testStartedCount.get(), "# tests started");
		assertEquals(1, tracker.testSucceededCount.get(), "# tests succeeded");
		assertEquals(0, tracker.testSkippedCount.get(), "# tests skipped");
		assertEquals(0, tracker.testAbortedCount.get(), "# tests aborted");
		assertEquals(0, tracker.testFailedCount.get(), "# tests failed");
	}

	@Test
	public void executeTestsForMethodInjectionInBeforeAndAfterAllMethods() {
		executeTestsForClass(BeforeAndAfterAllMethodInjectionTestCase.class);

		assertEquals(1, tracker.testStartedCount.get(), "# tests started");
		assertEquals(1, tracker.testSucceededCount.get(), "# tests succeeded");
		assertEquals(0, tracker.testSkippedCount.get(), "# tests skipped");
		assertEquals(0, tracker.testAbortedCount.get(), "# tests aborted");
		assertEquals(0, tracker.testFailedCount.get(), "# tests failed");
	}

	@Test
	public void executeTestsForMethodWithExtendWithAnnotation() {
		executeTestsForClass(ExtendWithOnMethodTestCase.class);

		assertEquals(1, tracker.testStartedCount.get(), "# tests started");
		assertEquals(1, tracker.testSucceededCount.get(), "# tests succeeded");
		assertEquals(0, tracker.testSkippedCount.get(), "# tests skipped");
		assertEquals(0, tracker.testAbortedCount.get(), "# tests aborted");
		assertEquals(0, tracker.testFailedCount.get(), "# tests failed");
	}

	// -------------------------------------------------------------------

	@ExtendWith({ CustomTypeParameterResolver.class, CustomAnnotationParameterResolver.class })
	private static class MethodInjectionTestCase {

		@Test
		void parameterInjectionOfStandardTestName(TestInfo testInfo) {
			assertTrue(
				testInfo.getName().endsWith("parameterInjectionOfStandardTestName(org.junit.gen5.api.TestInfo)"));
			assertEquals("parameterInjectionOfStandardTestName", testInfo.getDisplayName());
		}

		@Test
		@DisplayName("myName")
		void parameterInjectionOfUserProvidedTestName(TestInfo testInfo) {
			assertTrue(
				testInfo.getName().endsWith("parameterInjectionOfUserProvidedTestName(org.junit.gen5.api.TestInfo)"));
			assertEquals("myName", testInfo.getDisplayName());
		}

		@Test
		void parameterInjectionWithCompetingResolversFail(@CustomAnnotation CustomType customType) {
			// should fail
		}

		@Test
		void parameterInjectionByType(CustomType customType) {
			assertNotNull(customType);
		}

		@Test
		void parameterInjectionByAnnotation(@CustomAnnotation String value) {
			assertNotNull(value);
		}

		// some overloaded methods

		@Test
		void overloadedName() {
			assertTrue(true);
		}

		@Test
		void overloadedName(CustomType customType) {
			assertNotNull(customType);
		}

		@Test
		void overloadedName(CustomType customType, @CustomAnnotation String value) {
			assertNotNull(customType);
			assertNotNull(value);
		}
	}

	private static class BeforeAndAfterMethodInjectionTestCase {

		@BeforeEach
		void before(TestInfo testInfo) {
			assertEquals("custom name", testInfo.getDisplayName());
		}

		@Test
		@DisplayName("custom name")
		void customNamedTest() {
		}

		@AfterEach
		void after(TestInfo testInfo) {
			assertEquals("custom name", testInfo.getDisplayName());
		}
	}

	@DisplayName("custom class name")
	private static class BeforeAndAfterAllMethodInjectionTestCase {

		@BeforeAll
		static void beforeAll(TestInfo testInfo) {
			assertEquals("custom class name", testInfo.getDisplayName());
		}

		@Test
		void aTest() {
		}

		@AfterAll
		static void afterAll(TestInfo testInfo) {
			assertEquals("custom class name", testInfo.getDisplayName());
		}
	}

	private static class ExtendWithOnMethodTestCase {

		@Test
		@ExtendWith(CustomTypeParameterResolver.class)
		@ExtendWith(CustomAnnotationParameterResolver.class)
		void testMethodWithExtensionAnnotation(CustomType customType, @CustomAnnotation String value) {
			assertNotNull(customType);
			assertNotNull(value);
		}
	}

}
