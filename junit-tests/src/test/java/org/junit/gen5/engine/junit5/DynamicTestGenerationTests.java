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
import static org.junit.gen5.commons.util.ReflectionUtils.invokeMethod;
import static org.junit.gen5.engine.discovery.ClassSelector.forClass;
import static org.junit.gen5.engine.discovery.MethodSelector.forMethod;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import org.junit.gen5.api.*;
import org.junit.gen5.api.extension.DynamicTestCreator;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.MethodInvocationContext;
import org.junit.gen5.commons.JUnitException;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.ExecutionEventRecorder;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.launcher.TestDiscoveryRequest;

class DynamicTestGenerationTests extends AbstractJUnit5TestEngineTests {

	@Test
	public void dynamicTestMethodsAreCorrectlyDiscoveredForClassSelector() {
		TestDiscoveryRequest request = request().select(forClass(MyDynamicTestCase.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(5, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void dynamicTestMethodIsCorrectlyDiscoveredForMethodSelector() {
		TestDiscoveryRequest request = request().select(forMethod(MyDynamicTestCase.class, "dynamicStream")).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(2, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void dynamicTestsAreExecutedFromStream() {
		TestDiscoveryRequest request = request().select(forMethod(MyDynamicTestCase.class, "dynamicStream")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		//dynamic test methods are counted as both container and test
		assertAll( //
			() -> assertEquals(3L, eventRecorder.getContainerStartedCount(), "# container started"),
			() -> assertEquals(2L, eventRecorder.getDynamicTestRegisteredCount(), "# dynamic registered"),
			() -> assertEquals(3L, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(2L, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1L, eventRecorder.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(3L, eventRecorder.getContainerFinishedCount(), "# container finished"));
	}

	@Test
	public void dynamicTestsAreExecutedFromCollection() {
		TestDiscoveryRequest request = request().select(
			forMethod(MyDynamicTestCase.class, "dynamicCollection")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		//dynamic test methods are counted as both container and test
		assertAll( //
			() -> assertEquals(3L, eventRecorder.getContainerStartedCount(), "# container started"),
			() -> assertEquals(2L, eventRecorder.getDynamicTestRegisteredCount(), "# dynamic registered"),
			() -> assertEquals(3L, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(2L, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1L, eventRecorder.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(3L, eventRecorder.getContainerFinishedCount(), "# container finished"));
	}

	@Test
	public void dynamicTestsAreExecutedFromIterator() {
		TestDiscoveryRequest request = request().select(forMethod(MyDynamicTestCase.class, "dynamicIterator")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		//dynamic test methods are counted as both container and test
		assertAll( //
			() -> assertEquals(3L, eventRecorder.getContainerStartedCount(), "# container started"),
			() -> assertEquals(2L, eventRecorder.getDynamicTestRegisteredCount(), "# dynamic registered"),
			() -> assertEquals(3L, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(2L, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1L, eventRecorder.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(3L, eventRecorder.getContainerFinishedCount(), "# container finished"));
	}

	@Test
	public void dynamicTestsAreExecutedFromExtension() {
		Optional<Method> testMethod = ReflectionUtils.findMethod(MyDynamicTestCase.class, "dynamicByExtension",
			Boolean.TYPE);
		TestDiscoveryRequest request = request().select(forMethod(MyDynamicTestCase.class, testMethod.get())).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		//dynamic test methods are counted as both container and test
		assertAll( //
			() -> assertEquals(3L, eventRecorder.getContainerStartedCount(), "# container started"),
			() -> assertEquals(2L, eventRecorder.getDynamicTestRegisteredCount(), "# dynamic registered"),
			() -> assertEquals(3L, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(2L, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1L, eventRecorder.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(3L, eventRecorder.getContainerFinishedCount(), "# container finished"));
	}

	@ExtendWith(TestExtension.class)
	private static class MyDynamicTestCase {

		@Dynamic
		Stream<DynamicTest> dynamicStream() {
			List<DynamicTest> tests = new ArrayList<>();

			tests.add(new DynamicTest("succeedingTest", () -> Assertions.assertTrue(true, "succeeding")));
			tests.add(new DynamicTest("failingTest", () -> Assertions.assertTrue(false, "failing")));

			return tests.stream();
		}

		@Dynamic
		Collection<DynamicTest> dynamicCollection() {
			List<DynamicTest> tests = new ArrayList<>();

			tests.add(new DynamicTest("succeedingTest", () -> Assertions.assertTrue(true, "succeeding")));
			tests.add(new DynamicTest("failingTest", () -> Assertions.assertTrue(false, "failing")));

			return tests;
		}

		@Dynamic
		Iterator<DynamicTest> dynamicIterator() {
			List<DynamicTest> tests = new ArrayList<>();

			tests.add(new DynamicTest("succeedingTest", () -> Assertions.assertTrue(true, "succeeding")));
			tests.add(new DynamicTest("failingTest", () -> Assertions.assertTrue(false, "failing")));

			return tests.iterator();
		}

		@Dynamic
		void dynamicByExtension(boolean success) {
			Assertions.assertTrue(success, "failing");
		}
	}

	private static class TestExtension implements DynamicTestCreator {
		@Override
		public boolean supports(MethodInvocationContext methodInvocationContext, ExtensionContext extensionContext)
				throws JUnitException {
			Class<?>[] parameterTypes = methodInvocationContext.getMethod().getParameterTypes();
			return parameterTypes.length == 1 && parameterTypes[0] == Boolean.TYPE;
		}

		@Override
		public Stream<DynamicTest> replace(MethodInvocationContext methodInvocationContext,
				ExtensionContext extensionContext) throws JUnitException {
			return Stream.of(new DynamicTest("extensionTest", testExecutable(methodInvocationContext, true)),
				new DynamicTest("extensionTest", testExecutable(methodInvocationContext, false)));
		}

		private Executable testExecutable(MethodInvocationContext context, boolean parameter) {
			return () -> invokeMethod(context.getMethod(), context.getInstance(), parameter);
		}
	}
}
