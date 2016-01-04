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

import static org.junit.gen5.api.Assertions.*;

import java.util.HashMap;
import java.util.Optional;

import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

/**
 * Microtests for implementors of {@linkplain ExtensionContext}: {@linkplain ClassBasedContainerExtensionContext} and
 * {@linkplain MethodBasedTestExtensionContext}
 */
public class ExtensionContextTests {

	@Test
	public void fromClassTestDescriptor() {
		ClassTestDescriptor nestedClassDescriptor = nestedClassDescriptor();
		ClassTestDescriptor outerClassDescriptor = outerClassDescriptor(nestedClassDescriptor);

		ClassBasedContainerExtensionContext outerExtensionContext = new ClassBasedContainerExtensionContext(null,
			new JUnit5EngineExecutionContext(), outerClassDescriptor);
		Assertions.assertAll("outerContext", //
			() -> assertEquals(OuterClass.class, outerExtensionContext.getTestClass()), //
			() -> assertEquals(outerClassDescriptor.getDisplayName(), outerExtensionContext.getDisplayName()), //
			() -> assertEquals(Optional.empty(), outerExtensionContext.getParent()) //
		);

		ClassBasedContainerExtensionContext nestedExtensionContext = new ClassBasedContainerExtensionContext(
			outerExtensionContext, new JUnit5EngineExecutionContext(), nestedClassDescriptor);
		Assertions.assertSame(outerExtensionContext, nestedExtensionContext.getParent().get());
	}

	@Test
	public void fromMethodTestDescriptor() {
		MethodTestDescriptor methodTestDescriptor = methodDescriptor();
		ClassTestDescriptor classTestDescriptor = outerClassDescriptor(methodTestDescriptor);

		ClassBasedContainerExtensionContext classExtensionContext = new ClassBasedContainerExtensionContext(null,
			new JUnit5EngineExecutionContext(), classTestDescriptor);
		MethodBasedTestExtensionContext testExtensionContext = new MethodBasedTestExtensionContext(
			classExtensionContext, new JUnit5EngineExecutionContext(), methodTestDescriptor, new OuterClass());
		Assertions.assertAll("methodContext", //
			() -> assertEquals(OuterClass.class, testExtensionContext.getTestClass()), //
			() -> assertEquals(methodTestDescriptor.getDisplayName(), testExtensionContext.getDisplayName()), //
			() -> assertEquals(classExtensionContext, testExtensionContext.getParent().get()), //
			() -> assertEquals(OuterClass.class, testExtensionContext.getTestInstance().getClass()) //
		);

	}

	@Test
	public void flatAttributeAccess() {
		ClassTestDescriptor classTestDescriptor = outerClassDescriptor(null);

		ExtensionContext parentContext = new ClassBasedContainerExtensionContext(null,
			new JUnit5EngineExecutionContext(), classTestDescriptor);

		assertNull(parentContext.getAttribute("not set"));

		parentContext.putAttribute("attr1", "value1");
		assertEquals("value1", parentContext.getAttribute("attr1"));

		assertEquals("value1", parentContext.removeAttribute("attr1"));
		assertNull(parentContext.getAttribute("attr1"));

	}

	@Test
	public void nestedAttributeAccess() {
		MethodTestDescriptor methodTestDescriptor = methodDescriptor();
		ClassTestDescriptor classTestDescriptor = outerClassDescriptor(methodTestDescriptor);

		ExtensionContext parentContext = new ClassBasedContainerExtensionContext(null,
			new JUnit5EngineExecutionContext(), classTestDescriptor);

		MethodBasedTestExtensionContext childContext = new MethodBasedTestExtensionContext(parentContext,
			new JUnit5EngineExecutionContext(), methodTestDescriptor, new OuterClass());

		parentContext.putAttribute("attr1", "value1");
		assertEquals("value1", childContext.getAttribute("attr1"));

		childContext.putAttribute("attr1", "value1 changed");
		assertEquals("value1 changed", childContext.getAttribute("attr1"));
		assertEquals("value1", parentContext.getAttribute("attr1"));

		childContext.removeAttribute("attr1");
		assertEquals("value1", childContext.getAttribute("attr1"));

	}

	@Test
	public void reportEntriesArePublishedToExecutionContext() {
		ClassTestDescriptor classTestDescriptor = outerClassDescriptor(null);

		JUnit5EngineExecutionContext executionContext = Mockito.spy(new JUnit5EngineExecutionContext());

		ExtensionContext extensionContext = new ClassBasedContainerExtensionContext(null, executionContext,
			classTestDescriptor);

		HashMap<String, String> reportEntry1 = new HashMap<>();
		HashMap<String, String> reportEntry2 = new HashMap<String, String>() {
			{
				this.put("key", "value");
			}
		};

		extensionContext.publishReportEntry(reportEntry1);
		extensionContext.publishReportEntry(reportEntry2);

		Mockito.verify(executionContext, Mockito.times(1)).publishReportEntry(classTestDescriptor, reportEntry1);
		Mockito.verify(executionContext, Mockito.times(1)).publishReportEntry(classTestDescriptor, reportEntry2);

	}

	private ClassTestDescriptor nestedClassDescriptor() {
		return new ClassTestDescriptor("NestedClass", OuterClass.NestedClass.class);
	}

	private ClassTestDescriptor outerClassDescriptor(TestDescriptor child) {
		ClassTestDescriptor classTestDescriptor = new ClassTestDescriptor("OuterClass", OuterClass.class);
		if (child != null)
			classTestDescriptor.addChild(child);
		return classTestDescriptor;
	}

	private MethodTestDescriptor methodDescriptor() {
		try {
			return new MethodTestDescriptor("aMethod", OuterClass.class, OuterClass.class.getDeclaredMethod("aMethod"));
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	static class OuterClass {

		class NestedClass {

		}

		void aMethod() {

		}
	}

}
