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

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.junit5.execution.TestInstanceProvider;

/**
 * {@link TestDescriptor} for tests based on nested (but not static) Java classes.
 *
 * <p>The pattern of the {@link #getUniqueId unique ID} takes the form of
 * <code>{parent unique id}:{fully qualified class name of parent}@{simple class name}</code>.
 *
 * @since 5.0
 */
public class NestedClassTestDescriptor extends ClassTestDescriptor {

	public NestedClassTestDescriptor(TestEngine testEngine, Class<?> testClass) {
		super(testEngine, testClass);
	}

	@Override
	protected TestInstanceProvider testInstanceProvider(JUnit5EngineExecutionContext context) {
		return () -> {
			Object outerInstance = context.getTestInstanceProvider().getTestInstance();
			return ReflectionUtils.newInstance(getTestClass(), outerInstance);
		};
	}

}
