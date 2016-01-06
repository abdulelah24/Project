/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.api.Name;
import org.junit.gen5.api.Tag;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.junit5.testdoubles.TestEngineStub;

/**
 * Unit tests for {@link ClassTestDescriptor} and {@link MethodTestDescriptor}.
 *
 * @since 5.0
 */
public class JUnit5TestDescriptorTests {
	private TestEngine testEngine = new TestEngineStub();

	@Test
	public void constructFromMethod() throws Exception {
		Class<?> testClass = ASampleTestCase.class;
		Method testMethod = testClass.getDeclaredMethod("test");
		MethodTestDescriptor descriptor = new MethodTestDescriptor(testEngine, testClass, testMethod);

		assertEquals("TestEngineDummyID:org.junit.gen5.engine.junit5.descriptor.JUnit5TestDescriptorTests#test()",
			descriptor.getUniqueId());
		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("test", descriptor.getDisplayName(), "display name:");
	}

	@Test
	public void constructFromMethodWithAnnotations() throws Exception {
		JUnit5TestDescriptor classDescriptor = new ClassTestDescriptor(testEngine, ASampleTestCase.class);
		Method testMethod = getClass().getDeclaredMethod("foo");
		MethodTestDescriptor methodDescriptor = new MethodTestDescriptor(testEngine, ASampleTestCase.class, testMethod);
		classDescriptor.addChild(methodDescriptor);

		assertEquals(testMethod, methodDescriptor.getTestMethod());
		assertEquals("custom test name", methodDescriptor.getDisplayName(), "display name:");

		List<String> tags = methodDescriptor.getTags().stream().map(TestTag::getName).collect(Collectors.toList());
		assertEquals(4, methodDescriptor.getTags().size());
		assertTrue(tags.contains("methodTag1"));
		assertTrue(tags.contains("methodTag2"));

		// Methods "inherit" tags from their test class
		assertTrue(tags.contains("classTag1"));
		assertTrue(tags.contains("classTag2"));
	}

	@Test
	public void constructClassDescriptorWithAnnotations() throws Exception {
		ClassTestDescriptor descriptor = new ClassTestDescriptor(testEngine, ASampleTestCase.class);

		assertEquals(ASampleTestCase.class, descriptor.getTestClass());
		assertEquals("custom class name", descriptor.getDisplayName(), "display name:");
		assertThat(descriptor.getTags()).containsExactly(new TestTag("classTag1"), new TestTag("classTag2"));
	}

	@Test
	public void constructFromMethodWithCustomTestAnnotation() throws Exception {
		Method testMethod = getClass().getDeclaredMethod("customTestAnnotation");
		MethodTestDescriptor descriptor = new MethodTestDescriptor(testEngine, ASampleTestCase.class, testMethod);

		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("custom name", descriptor.getDisplayName(), "display name:");
		assertThat(descriptor.getTags()).containsExactly(new TestTag("custom tag"));
	}

	@Test
	public void constructFromMethodWithParameters() throws Exception {
		Method testMethod = getClass().getDeclaredMethod("test", String.class, BigDecimal.class);
		MethodTestDescriptor descriptor = new MethodTestDescriptor(testEngine, ASampleTestCase.class, testMethod);

		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("test", descriptor.getDisplayName(), "display name:");
	}

	@Tag("classTag1")
	@Tag("classTag2")
	@Name("custom class name")
	@SuppressWarnings("unused")
	private static class ASampleTestCase {

		void test() {
		}

		void test(String txt, BigDecimal sum) {
		}

		@Test
		@Name("custom test name")
		@Tag("methodTag1")
		@Tag("methodTag2")
		void foo() {
		}

		@CustomTestAnnotation
		void customTestAnnotation() {
		}

		@Test
		@Name("custom name")
		@Tag("custom tag")
		@Target(ElementType.METHOD)
		@Retention(RetentionPolicy.RUNTIME)
		@interface CustomTestAnnotation {
		}

	}

	@Test
	@Name("custom name")
	@Tag("custom tag")
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface CustomTestAnnotation {
	}

}
