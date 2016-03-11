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

import static java.util.function.Function.identity;
import static org.junit.gen5.commons.meta.API.Usage.Internal;
import static org.junit.gen5.engine.junit5.execution.MethodInvocationContextFactory.methodInvocationContext;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.gen5.api.DynamicTest;
import org.junit.gen5.api.extension.DynamicTestCreator;
import org.junit.gen5.api.extension.MethodInvocationContext;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.commons.JUnitException;
import org.junit.gen5.commons.meta.API;
import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.junit5.execution.MethodInvoker;
import org.junit.gen5.engine.junit5.execution.ThrowableCollector;
import org.junit.gen5.engine.junit5.extension.ExtensionRegistry;
import org.junit.gen5.engine.support.hierarchical.Leaf;
import org.junit.gen5.engine.support.hierarchical.SingleTestExecutor;

@API(Internal)
public class DynamicMethodTestDescriptor extends MethodTestDescriptor implements Leaf<JUnit5EngineExecutionContext> {

	public DynamicMethodTestDescriptor(String uniqueId, Class<?> testClass, Method testMethod) {
		super(uniqueId, testClass, testMethod);
	}

	@Override
	public boolean isContainer() {
		return true;
	}

	@Override
	public boolean isTest() {
		return true;
	}

	@Override
	protected void invokeTestMethod(JUnit5EngineExecutionContext context, TestExtensionContext testExtensionContext,
			ThrowableCollector throwableCollector) {

		EngineExecutionListener listener = context.getExecutionListener();

		throwableCollector.execute(() -> {
			MethodInvocationContext methodInvocationContext = methodInvocationContext(
				testExtensionContext.getTestInstance(), testExtensionContext.getTestMethod());
			Stream<DynamicTest> dynamicTestStream;
			if (methodInvocationContext.getMethod().getReturnType() == Void.TYPE) {
				dynamicTestStream = context.getExtensionRegistry().stream(DynamicTestCreator.class,
					ExtensionRegistry.ApplicationOrder.FORWARD).filter(
						creator -> creator.getExtensionPoint().supports(methodInvocationContext,
							testExtensionContext)).map(
								creator -> creator.getExtensionPoint().replace(methodInvocationContext,
									testExtensionContext)).map(this::toDynamicTestStream).flatMap(identity());
			}
			else {
				MethodInvoker methodInvoker = new MethodInvoker(testExtensionContext, context.getExtensionRegistry());
				Object dynamicMethodResult = methodInvoker.invoke(methodInvocationContext);
				dynamicTestStream = toDynamicTestStream(dynamicMethodResult);
			}
			AtomicInteger index = new AtomicInteger();
			try {
				dynamicTestStream.forEach(
					dynamicTest -> registerAndExecute(dynamicTest, index.incrementAndGet(), listener));
			}
			catch (ClassCastException cce) {
				throw new JUnitException(
					"Dynamic test must return Stream, Collection or Iterator of " + DynamicTest.class);
			}

		});
	}

	@SuppressWarnings("unchecked")
	private Stream<DynamicTest> toDynamicTestStream(Object dynamicMethodResult) {

		if (dynamicMethodResult instanceof Stream)
			return (Stream<DynamicTest>) dynamicMethodResult;
		if (dynamicMethodResult instanceof Collection) {
			Collection<DynamicTest> dynamicTestCollection = (Collection<DynamicTest>) dynamicMethodResult;
			return dynamicTestCollection.stream();
		}
		if (dynamicMethodResult instanceof Iterator) {
			Iterator<DynamicTest> dynamicTestIterator = (Iterator<DynamicTest>) dynamicMethodResult;
			return StreamSupport.stream(Spliterators.spliteratorUnknownSize(dynamicTestIterator, Spliterator.ORDERED),
				false);
		}

		throw new JUnitException("Dynamic test must return Stream, Collection or Iterator of " + DynamicTest.class);
	}

	private void registerAndExecute(DynamicTest dynamicTest, int index, EngineExecutionListener listener) {
		String uniqueId = getUniqueId() + "%" + index;
		DynamicTestTestDescriptor dynamicTestTestDescriptor = new DynamicTestTestDescriptor(uniqueId, dynamicTest,
			getSource().get());
		addChild(dynamicTestTestDescriptor);

		listener.dynamicTestRegistered(dynamicTestTestDescriptor);
		listener.executionStarted(dynamicTestTestDescriptor);
		TestExecutionResult result = new SingleTestExecutor().executeSafely(dynamicTest.getExecutable()::execute);
		listener.executionFinished(dynamicTestTestDescriptor, result);
	}

}
