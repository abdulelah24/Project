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

import static org.junit.gen5.api.Assertions.fail;
import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.gen5.engine.TestPlanSpecification.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.gen5.api.Disabled;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.ConditionEvaluationResult;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.ExtensionPointRegistry;
import org.junit.gen5.api.extension.ShouldExecuteExtensionPoint;
import org.junit.gen5.api.extension.TestExtension;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * Integration tests that verify support for {@link Disabled @Disabled} in the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
@Ignore("https://github.com/junit-team/junit-lambda/issues/39")

public class DisabledTests extends AbstractJUnit5TestEngineTestCase {

	private static final String FOO = "DisabledTests.foo";
	private static final String BAR = "DisabledTests.bar";
	private static final String BOGUS = "DisabledTests.bogus";

	@org.junit.Before
	public void setUp() {
		System.setProperty(FOO, BAR);
	}

	@org.junit.After
	public void tearDown() {
		System.clearProperty(FOO);
	}

	@org.junit.Test
	public void executeTestsWithDisabledTestClass() {
		TestPlanSpecification spec = build(forClass(DisabledTestClassTestCase.class));
		TrackingTestExecutionListener listener = executeTests(spec, 2);

		Assert.assertEquals("# tests started", 0, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 0, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 1, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());
	}

	@org.junit.Test
	public void executeTestsWithDisabledTestMethods() {
		TestPlanSpecification spec = build(forClass(DisabledTestMethodsTestCase.class));
		TrackingTestExecutionListener listener = executeTests(spec, 6);

		Assert.assertEquals("# tests started", 2, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 2, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 3, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());
	}

	// -------------------------------------------------------------------

	@Disabled
	private static class DisabledTestClassTestCase {

		@Test
		void disabledTest() {
			fail("this should be @Disabled");
		}
	}

	private static class DisabledTestMethodsTestCase {

		@Test
		void enabledTest() {
		}

		@Test
		@Disabled
		void disabledTest() {
			fail("this should be @Disabled");
		}

		@Test
		@SystemProperty(key = FOO, value = BAR)
		void systemPropertyEnabledTest() {
		}

		@Test
		@SystemProperty(key = FOO, value = BOGUS)
		void systemPropertyWithIncorrectValueTest() {
			fail("this should be disabled");
		}

		@Test
		@SystemProperty(key = BOGUS, value = "doesn't matter")
		void systemPropertyNotSetTest() {
			fail("this should be disabled");
		}

	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@ExtendWith(SystemPropertyCondition.class)
	@interface SystemProperty {

		String key();

		String value();
	}

	private static class SystemPropertyCondition implements TestExtension {

		@Override
		public void registerExtensionPoints(ExtensionPointRegistry registry) {
			registry.register(this::shouldTestBeExecuted, ShouldExecuteExtensionPoint.class);
		}

		private ConditionEvaluationResult shouldTestBeExecuted(ExtensionContext context) {
			if (context instanceof TestExtensionContext) {
				TestExtensionContext testExtensionContext = (TestExtensionContext) context;
				Optional<SystemProperty> optional = findAnnotation(testExtensionContext.getTestMethod(),
					SystemProperty.class);

				if (optional.isPresent()) {
					SystemProperty systemProperty = optional.get();
					String key = systemProperty.key();
					String expected = systemProperty.value();
					String actual = System.getProperty(key);

					if (!Objects.equals(expected, actual)) {
						return ConditionEvaluationResult.disabled(String.format(
							"System property [%s] has a value of [%s] instead of [%s]", key, actual, expected));
					}
				}
			}

			return ConditionEvaluationResult.enabled("@SystemProperty is not present");
		}

	}

}
